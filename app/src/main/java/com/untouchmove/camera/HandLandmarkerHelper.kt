package com.untouchmove.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.untouchmove.gesture.GestureThresholds
import com.untouchmove.gesture.HandFrame
import com.untouchmove.gesture.Point3D

/**
 * Boc MediaPipe HandLandmarker (LIVE_STREAM, 1 tay). Day la noi DUY NHAT xu ly
 * lat guong cua camera truoc (ARCHITECTURE muc 6): xoay + lat bitmap truoc khi
 * dua vao model, nen moi toa do tra ra (ca normalized lan world) da nhat quan
 * voi nhung gi nguoi dung thay tren preview.
 */
class HandLandmarkerHelper(
    context: Context,
    private val onResult: (frame: HandFrame, normalizedLandmarks: List<Point3D>, inferenceMs: Long) -> Unit,
    private val onHandLost: () -> Unit,
    private val onError: (String) -> Unit,
) {
    private var lastInferenceStartMs = 0L
    // MediaPipe goi onResult/onError tren thread noi bo cua no (khong co
    // Looper) - phai chuyen ve main thread truoc khi tra ve caller, vi caller
    // (vd GestureForegroundService) co the dung ket qua nay de ve overlay qua
    // WindowManager, thao tac bat buoc chay tren main thread. Thieu buoc nay
    // gay crash native SIGABRT (JNI DETECTED ERROR ... Looper.prepare()).
    private val mainHandler = Handler(Looper.getMainLooper())

    // Nang cap tu CPU len GPU (yeu cau nguoi dung 2026-09-20: do tre + hay mat
    // tracking luc di chuyen nhanh - GPU nhanh hon dang ke cho model nay).
    // Day chinh la "nang cap khi can them fps o Phase sau" da du tinh tu Phase
    // 0. Co fallback ve CPU neu may khong ho tro GPU delegate (tranh crash
    // luc khoi tao tren thiet bi la).
    private val handLandmarker: HandLandmarker = buildLandmarker(context, Delegate.GPU)
        ?: buildLandmarker(context, Delegate.CPU)!!

    private fun buildLandmarker(context: Context, delegate: Delegate): HandLandmarker? = try {
        HandLandmarker.createFromOptions(
            context,
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath(MODEL_ASSET_PATH)
                        .setDelegate(delegate)
                        .build()
                )
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(1)
                // Doc luc tao (khong phai moi khung) - doi luc dang chay phai dong
                // + tao lai HandLandmarkerHelper moi ap dung duoc gia tri moi
                // (xem GestureThresholds.HAND_DETECTION_CONFIDENCE va
                // GestureForegroundService.observeSettings).
                .setMinHandDetectionConfidence(GestureThresholds.HAND_DETECTION_CONFIDENCE)
                .setMinHandPresenceConfidence(GestureThresholds.HAND_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(GestureThresholds.HAND_DETECTION_CONFIDENCE)
                .setResultListener { result, _ -> mainHandler.post { handleResult(result) } }
                .setErrorListener { error ->
                    mainHandler.post { onError(error.message ?: "Loi HandLandmarker khong ro") }
                }
                .build()
        ).also { Log.i(TAG, "HandLandmarker khoi tao thanh cong voi delegate $delegate") }
    } catch (e: Exception) {
        Log.w(TAG, "Khong the khoi tao HandLandmarker voi delegate $delegate: ${e.message}")
        null
    }

    fun detectAsync(imageProxy: ImageProxy) {
        val startMs = SystemClock.uptimeMillis()
        val bitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        imageProxy.close()

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            // camera truoc: lat ngang de toa do khop voi guong tu nhien tren preview
            postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        lastInferenceStartMs = startMs
        try {
            handLandmarker.detectAsync(BitmapImageBuilder(rotated).build(), startMs)
        } catch (e: RuntimeException) {
            // MediaPipe co the nem MediaPipeException "task graph hasn't been
            // successfully started" cho vai khung hinh dau tien ngay sau khi
            // HandLandmarker vua tao xong (graph con dang khoi tao ben trong).
            // Bo qua khung nay va tiep tuc, khong de mot khung loi lam sap ca
            // pipeline dang chay nen (xay ra that: GestureForegroundService
            // gui khung dau tien qua nhanh, crash toan bo tien trinh).
            mainHandler.post { onError(e.message ?: "Loi gui khung hinh vao HandLandmarker") }
        }
    }

    private fun handleResult(result: HandLandmarkerResult) {
        val worldHands = result.worldLandmarks()
        val normalizedHands = result.landmarks()
        if (worldHands.isEmpty() || normalizedHands.isEmpty()) {
            onHandLost()
            return
        }

        val world = worldHands[0].map { Point3D(it.x(), it.y(), it.z()) }
        val normalized = normalizedHands[0].map { Point3D(it.x(), it.y(), it.z()) }
        val confidence = result.handedness().getOrNull(0)?.getOrNull(0)?.score() ?: 0f
        val inferenceMs = SystemClock.uptimeMillis() - lastInferenceStartMs

        onResult(
            HandFrame(worldLandmarks = world, confidence = confidence, timestampMs = result.timestampMs()),
            normalized,
            inferenceMs,
        )
    }

    fun close() {
        handLandmarker.close()
    }

    private companion object {
        const val MODEL_ASSET_PATH = "hand_landmarker.task"
        const val TAG = "HandLandmarkerHelper"
    }
}
