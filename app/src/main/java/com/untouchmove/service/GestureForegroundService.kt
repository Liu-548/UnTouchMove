package com.untouchmove.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.untouchmove.camera.HandLandmarkerHelper
import com.untouchmove.data.SettingsRepository
import com.untouchmove.gesture.DisplayState
import com.untouchmove.gesture.GestureAction
import com.untouchmove.gesture.GestureStateMachine
import com.untouchmove.gesture.GestureThresholds
import com.untouchmove.gesture.HandFrame
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

/**
 * Giu camera + pipeline nhan dien song, co thong bao thuong tru (ARCHITECTURE
 * muc 5). Nguoi dung bat/tat duoc tu MainActivity hoac tu nut "Tat" trong
 * thong bao. Chi bom cu chi qua ActionDispatcher -> UnTouchAccessibilityService,
 * khong tu minh dieu khien man hinh.
 *
 * Dung camera khi man hinh tat/khoa (CLAUDE.md muc 4.3) bang cach lang nghe
 * ACTION_SCREEN_OFF/ON.
 */
class GestureForegroundService : LifecycleService() {

    private lateinit var cameraExecutor: ExecutorService
    private var handLandmarkerHelper: HandLandmarkerHelper? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val stateMachine = GestureStateMachine()
    private var lastHandSeenAtMs = 0L
    private var errorFrameCount = 0
    private var lastDisplayState = DisplayState.NONE

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> stopCamera()
                Intent.ACTION_SCREEN_ON -> startCamera()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        cameraExecutor = Executors.newSingleThreadExecutor()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            },
        )
        observeSettings()
        startCamera()
    }

    /**
     * Ap dung cac nguong tu man hinh Cai dat ngay khi doc duoc (khong doi
     * khoi dong lai), va tiep tuc cap nhat neu nguoi dung doi trong luc dang
     * bat (Flow.collect chay xuyen suot vong doi service).
     */
    private fun observeSettings() {
        val repo = SettingsRepository(this)
        lifecycleScope.launch { repo.velUp.collect { GestureThresholds.SWIPE_VEL_MIN_UP = it } }
        lifecycleScope.launch { repo.velDown.collect { GestureThresholds.SWIPE_VEL_MIN_DOWN = it } }
        lifecycleScope.launch { repo.velLeft.collect { GestureThresholds.SWIPE_VEL_MIN_LEFT = it } }
        lifecycleScope.launch { repo.velRight.collect { GestureThresholds.SWIPE_VEL_MIN_RIGHT = it } }
        lifecycleScope.launch { repo.swipeCooldownMs.collect { GestureThresholds.SWIPE_COOLDOWN_MS = it } }
        lifecycleScope.launch { repo.cursorGainTranslation.collect { GestureThresholds.CURSOR_GAIN_TRANSLATION = it } }
        lifecycleScope.launch { repo.cursorGainPointing.collect { GestureThresholds.CURSOR_GAIN_POINTING = it } }
        lifecycleScope.launch { repo.cursorPointingMode.collect { GestureThresholds.CURSOR_POINTING_MODE = it } }
        lifecycleScope.launch { repo.gOpen.collect { GestureThresholds.G_OPEN = it } }
        lifecycleScope.launch { repo.sysVelMin.collect { GestureThresholds.SYS_VEL_MIN = it } }
        lifecycleScope.launch { repo.enableM1Vertical.collect { GestureThresholds.ENABLE_M1_VERTICAL = it } }
        lifecycleScope.launch { repo.enableM1Horizontal.collect { GestureThresholds.ENABLE_M1_HORIZONTAL = it } }
        lifecycleScope.launch { repo.enableM2Cursor.collect { GestureThresholds.ENABLE_M2_CURSOR = it } }
        lifecycleScope.launch { repo.enableM5System.collect { GestureThresholds.ENABLE_M5_SYSTEM = it } }
        lifecycleScope.launch { repo.enableM6ScreenOff.collect { GestureThresholds.ENABLE_M6_SCREEN_OFF = it } }
        lifecycleScope.launch {
            repo.enableScreenOffReopenGesture.collect { GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE = it }
        }
        lifecycleScope.launch {
            // Khac voi cac nguong tren: HAND_DETECTION_CONFIDENCE chi doc luc
            // tao HandLandmarker (khong phai moi khung) - phai dong+tao lai
            // camera moi ap dung duoc, nhung KHONG lam vay o lan doc dau tien
            // (startCamera() ben duoi se tu doc gia tri moi nhat khi tao).
            var isFirst = true
            repo.handDetectionConfidence.collect { value ->
                GestureThresholds.HAND_DETECTION_CONFIDENCE = value
                if (!isFirst) {
                    stopCamera()
                    startCamera()
                }
                isFirst = false
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        stopCamera()
        unregisterReceiver(screenReceiver)
        ActionDispatcher.showStatus(DisplayState.NONE)
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun startCamera() {
        if (handLandmarkerHelper != null) return // da chay roi

        handLandmarkerHelper = buildHandLandmarkerHelper()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                // ARCHITECTURE muc 6 da ghi "480p hoac thap hon" tu dau nhung
                // code chua tung gioi han - CameraX se tu chon do phan giai
                // mac dinh (thuong cao hon nhieu 480p), lam moi khung xu ly
                // (tao bitmap, xoay/lat, chay model) cham hon can thiet. Gioi
                // han that su o day de giam do tre + tang fps hieu dung (yeu
                // cau nguoi dung 2026-09-20: do tre va hay mat tracking luc di
                // chuyen nhanh - ca hai deu lien quan fps thap).
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(480, 360), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER),
                        )
                        .build(),
                )
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val helper = handLandmarkerHelper
                        if (helper != null) helper.detectAsync(imageProxy) else imageProxy.close()
                    }
                }
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        handLandmarkerHelper?.close()
        handLandmarkerHelper = null
    }

    private fun buildHandLandmarkerHelper(): HandLandmarkerHelper = HandLandmarkerHelper(
        context = this,
        onResult = { frame, _, _ -> handleFrame(frame) },
        onHandLost = { handleHandLost() },
        // ponytail: khong tu recreate HandLandmarkerHelper o day nua - ban vi
        // tao lai dong bo tren cameraExecutor moi vai khung loi tung nghi la
        // nguyen nhan chan luong xu ly gay ANR/kill tien trinh (xem
        // docs/DEBUG_NOTES_Phase3.md). Chi log de xac nhan loi co lap lai o
        // MOI khung hinh hay khong truoc khi quyet dinh huong sua tiep theo.
        onError = { message ->
            errorFrameCount++
            Log.e(TAG, "HandLandmarker loi (khung #$errorFrameCount): $message")
        },
    )

    private fun handleFrame(frame: HandFrame) {
        lastHandSeenAtMs = frame.timestampMs
        dispatch(stateMachine.onFrame(frame))
        updateDisplayState(stateMachine.displayState)
        ActionDispatcher.showCursorPhase(stateMachine.cursorPhase)
    }

    private fun handleHandLost() {
        val now = System.currentTimeMillis()
        // ponytail: dung System.currentTimeMillis() lam moc, khong phai
        // timestamp cua frame (khung "mat tay" khong co frame that de lay moc).
        if (lastHandSeenAtMs != 0L && now - lastHandSeenAtMs < GestureThresholds.LOST_HAND_MS) return
        dispatch(stateMachine.onHandLost())
        updateDisplayState(stateMachine.displayState)
        ActionDispatcher.showCursorPhase(stateMachine.cursorPhase)
    }

    private fun dispatch(action: GestureAction?) {
        when (action) {
            is GestureAction.Swipe -> ActionDispatcher.swipe(action.direction.toServiceDirection())
            is GestureAction.CursorMove -> ActionDispatcher.moveCursor(action.x, action.y)
            is GestureAction.Click -> ActionDispatcher.gestureClick(action.x, action.y)
            is GestureAction.HoldStart -> ActionDispatcher.holdStart(action.x, action.y)
            is GestureAction.HoldMove -> ActionDispatcher.holdMove(action.x, action.y)
            GestureAction.HoldEnd -> ActionDispatcher.holdEnd()
            is GestureAction.SystemAction -> ActionDispatcher.globalAction(action.type.toGlobalAction())
            // M6 (yeu cau nguoi dung 2026-09-22, sua lai sau khi hieu nham
            // thanh khoa man hinh that): ENABLE_SCREEN_OFF_REOPEN_GESTURE=false
            // (mac dinh) -> khoa man hinh THAT (GLOBAL_ACTION_LOCK_SCREEN,
            // giong bam nut nguon, khong mo lai duoc bang cu chi vi camera se
            // tu tat theo dung CLAUDE.md muc 4.3, xem screenReceiver). =true
            // -> CHI phu 1 lop man DEN che kin man hinh, KHONG khoa/tat gi
            // that ca - camera van chay binh thuong xuyen suot, nen bat lai
            // duoc cu chi mo lai bat ky luc nao, khong can ngoai le camera nao.
            GestureAction.ScreenOff -> if (GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE) {
                ActionDispatcher.showBlackCurtain()
            } else {
                ActionDispatcher.globalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            }
            GestureAction.ScreenOn -> ActionDispatcher.hideBlackCurtain()
            null -> Unit
        }
    }

    /**
     * GestureStateMachine chi phat GestureAction.CursorMove/Click, khong co
     * "vua vao M2" rieng - phat hien qua doi trang thai hien thi de dat lai
     * con tro ve giua man hinh dung 1 lan luc bat dau M2 (SPEC muc 4.2).
     */
    private fun updateDisplayState(newState: DisplayState) {
        if (newState == DisplayState.M2 && lastDisplayState != DisplayState.M2) {
            ActionDispatcher.resetCursor()
        }
        lastDisplayState = newState
        ActionDispatcher.showStatus(newState)
    }

    private fun GestureAction.Direction.toServiceDirection() = when (this) {
        GestureAction.Direction.UP -> UnTouchAccessibilityService.SwipeDirection.UP
        GestureAction.Direction.DOWN -> UnTouchAccessibilityService.SwipeDirection.DOWN
        GestureAction.Direction.LEFT -> UnTouchAccessibilityService.SwipeDirection.LEFT
        GestureAction.Direction.RIGHT -> UnTouchAccessibilityService.SwipeDirection.RIGHT
    }

    /**
     * SPEC muc 4.5 (doi lai 2026-09-20 lan 8): tu the KHEP -> Back/Da nhiem,
     * tu the TACH -> Home/Thanh thong bao (xem GestureStateMachine.SystemPoseMode).
     */
    private fun GestureAction.SystemActionType.toGlobalAction(): Int = when (this) {
        GestureAction.SystemActionType.BACK -> AccessibilityService.GLOBAL_ACTION_BACK
        GestureAction.SystemActionType.RECENTS -> AccessibilityService.GLOBAL_ACTION_RECENTS
        GestureAction.SystemActionType.HOME -> AccessibilityService.GLOBAL_ACTION_HOME
        GestureAction.SystemActionType.NOTIFICATIONS -> AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "UnTouchMove", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, GestureForegroundService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UnTouchMove dang bat")
            .setContentText("Dieu khien khong cham dang hoat dong")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .addAction(0, "Tat", stopPendingIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP = "com.untouchmove.action.STOP"
        private const val CHANNEL_ID = "untouchmove_gesture"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "GestureForegroundSvc"

        var isRunning: Boolean = false
            private set
    }
}
