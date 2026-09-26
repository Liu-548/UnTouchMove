package com.untouchmove

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.untouchmove.ui.ScreenSurface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.untouchmove.service.ActionDispatcher
import com.untouchmove.service.GestureForegroundService
import com.untouchmove.ui.UnTouchTheme
import kotlinx.coroutines.delay

/**
 * Man hinh chinh (SPEC muc 7, ROADMAP Phase 7 "man hinh chinh gon: nut bat/tat
 * + 3 dong trang thai quyen"). Nut bat/tat lam trong tam, kem trang thai
 * Camera/Tro nang/Thong bao va loi tat sang Huong dan su dung + Cai dat.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnTouchTheme {
                ScreenSurface() {
                    CameraPermissionGate()
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionGate() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            android.os.Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val requestCameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val requestNotificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission) {
            requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (hasCameraPermission) {
        MainScreen(hasNotificationPermission = hasNotificationPermission)
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                androidx.compose.ui.res.stringResource(id = R.string.camera_permission_rationale),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun MainScreen(hasNotificationPermission: Boolean) {
    val context = LocalContext.current
    var running by remember { mutableStateOf(GestureForegroundService.isRunning) }
    var accessibilityOn by remember { mutableStateOf(ActionDispatcher.isConnected) }
    var showAdvanced by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            running = GestureForegroundService.isRunning
            accessibilityOn = ActionDispatcher.isConnected
            delay(1000)
        }
    }

    // Toan bo man hinh CO THE CUON (yeu cau nguoi dung 2026-09-20: cac nut
    // Huong dan/Cai dat bi khuat o cuoi man hinh, khong bam toi duoc) - truoc
    // day dung Spacer(weight(1f)) trong 1 Column khong cuon, khien phan nut
    // phia duoi bi day khoi man hinh tren may man hinh nho/preview cao. Da bo
    // han weight() (khong dung chung duoc voi verticalScroll) va gioi han
    // chieu cao khung preview co dinh.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLogoBadge(size = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("UnTouchMove", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Điều khiển điện thoại không chạm, bằng cử chỉ tay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(260.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        ) {
            FrontCameraPreview(enabled = !running)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            StatusCard(accessibilityOn = accessibilityOn, hasNotificationPermission = hasNotificationPermission)

            Button(
                onClick = {
                    if (running) {
                        context.stopService(Intent(context, GestureForegroundService::class.java))
                    } else {
                        GestureForegroundService.requestStart(context)
                    }
                },
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(8.dp, CircleShape, clip = false),
            ) {
                Text(
                    if (running) "Tắt điều khiển không chạm" else "Bật điều khiển không chạm",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, GuideActivity::class.java)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Hướng dẫn sử dụng", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cài đặt độ nhạy", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }

            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Ẩn công cụ nâng cao" else "Công cụ nâng cao (gỡ lỗi)")
            }
            if (showAdvanced) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { context.startActivity(Intent(context, DebugActivity::class.java)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Màn hình Debug")
                    }
                    TextButton(
                        onClick = { context.startActivity(Intent(context, GestureTestActivity::class.java)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Bơm thử cử chỉ")
                    }
                }
            }
        }
    }
}

/**
 * Logo tron nho o dau man hinh chinh - dung LAI dung 3 hinh "vien thuoc" cua
 * `ic_launcher_foreground.xml`/`HandPoseIcon` (2 ngon + long ban tay) thu nho
 * lai, ve truc tiep bang Canvas thay vi nap vector resource rieng - giu 1
 * nguon "chan ly" duy nhat ve hinh dang logo, tranh lech nhau giua icon app
 * that va icon hien thi trong app.
 */
@Composable
private fun AppLogoBadge(size: androidx.compose.ui.unit.Dp) {
    val badgeColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(badgeColor),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.16f)) {
            val w = this.size.width
            val glyph = Color.White
            // Cung ty le 108x108 nhu ic_launcher_foreground.xml, quy doi ve
            // kich thuoc thuc te cua Canvas (scale = w/108).
            val s = w / 108f
            fun px(v: Float) = v * s
            drawRoundRect(
                color = glyph,
                topLeft = Offset(px(42f), px(33f)),
                size = Size(px(10f), px(28f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(px(5f)),
            )
            drawRoundRect(
                color = glyph,
                topLeft = Offset(px(56f), px(29f)),
                size = Size(px(10f), px(32f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(px(5f)),
            )
            drawRoundRect(
                color = glyph,
                topLeft = Offset(px(36f), px(62f)),
                size = Size(px(36f), px(22f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(px(8f)),
            )
        }
    }
}

@Composable
private fun StatusCard(accessibilityOn: Boolean, hasNotificationPermission: Boolean) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StatusLine(ok = true, text = "Camera: đã cấp quyền")
            StatusLine(ok = accessibilityOn, text = if (accessibilityOn) "Trợ năng: đã bật" else "Trợ năng: CHƯA bật")
            if (!accessibilityOn) {
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    // Android khong cho app tu bat/tat Tro nang cho chinh no (chan
                    // cung o tang he dieu hanh, khong the lach - yeu cau nguoi dung
                    // 2026-09-20). Da THU deep link thang toi trang chi tiet cong tac
                    // (ACTION_ACCESSIBILITY_DETAILS_SETTINGS) de do phai tim trong
                    // danh sach, nhung may that bao SecurityException: intent nay can
                    // quyen he thong OPEN_ACCESSIBILITY_DETAILS_SETTINGS ma app thuong
                    // KHONG THE co - resolveActivity() tra ve non-null (co activity xu
                    // ly action nay) nhung khong kiem tra quyen runtime, gay hieu nham
                    // la "dung duoc". Quay lai dung ACTION_ACCESSIBILITY_SETTINGS (danh
                    // sach chung) - cach duy nhat hoat dong on dinh tren app thuong.
                    Text(
                        "Android 13 trở lên có thể chặn bật nếu chưa mở \"Cho phép " +
                            "cài đặt hạn chế\" trong Cài đặt > Ứng dụng > UnTouchMove.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(
                        onClick = { context.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    ) {
                        Text("Bật ngay")
                    }
                }
            }
            StatusLine(
                ok = hasNotificationPermission,
                text = if (hasNotificationPermission) "Thông báo: đã cấp quyền" else "Thông báo: chưa cấp (không bắt buộc)",
            )
        }
    }
}

@Composable
private fun StatusLine(ok: Boolean, text: String) {
    val tint = if (ok) Color(0xFF2E7D32) else Color(0xFFC62828)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (ok) "✓" else "!",
                style = MaterialTheme.typography.labelLarge,
                color = tint,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Preview camera cua rieng MainActivity (Phase 0, chi de xac nhan CameraX
 * noi day duoc). CHI bind camera khi [enabled] - tuc khi GestureForegroundService
 * chua chay - vi ProcessCameraProvider dung chung 1 camera cho ca process,
 * bind ca hai cung luc se gianh camera cua nhau (preview dung hinh, pipeline
 * nhan dien mat frame). Xem DECISIONS.md ngay 2026-09-20.
 */
@Composable
private fun FrontCameraPreview(enabled: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(enabled) {
        // Chi gian view Preview CUA RIENG man hinh nay (unbind(preview)), KHONG
        // duoc goi unbindAll() o day: ProcessCameraProvider dung chung cho ca
        // process, GestureForegroundService co the da bind ImageAnalysis cua no
        // vao đung luc nay (do polling isRunning moi 1s, co do tre) - unbindAll()
        // se go luon camera cua service, gay "bat len roi tat ngay" (xem
        // DEBUG_NOTES_Phase3.md 2026-09-20).
        var preview: Preview? = null
        if (enabled) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose {
            preview?.let { ProcessCameraProvider.getInstance(context).get().unbind(it) }
        }
    }

    if (enabled) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { previewView })
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Camera đang được điều khiển không chạm sử dụng",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
