package com.untouchmove.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
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
    private val onError: (String) -> Unit,
) {
    private var lastInferenceStartMs = 0L

    private val handLandmarker: HandLandmarker = HandLandmarker.createFromOptions(
        context,
        HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath(MODEL_ASSET_PATH)
                    // ponytail: delegate CPU de chay on dinh tren ca hai may muc
                    // tieu, chua thu GPU. Nang cap khi can them fps o Phase sau.
                    .setDelegate(Delegate.CPU)
                    .build()
            )
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
            .setMinHandPresenceConfidence(MIN_DETECTION_CONFIDENCE)
            .setMinTrackingConfidence(MIN_DETECTION_CONFIDENCE)
            .setResultListener { result, _ -> handleResult(result) }
            .setErrorListener { onError(it.message ?: "Loi HandLandmarker khong ro") }
            .build()
    )

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
        handLandmarker.detectAsync(BitmapImageBuilder(rotated).build(), startMs)
    }

    private fun handleResult(result: HandLandmarkerResult) {
        val worldHands = result.worldLandmarks()
        val normalizedHands = result.landmarks()
        if (worldHands.isEmpty() || normalizedHands.isEmpty()) return

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
        const val MIN_DETECTION_CONFIDENCE = 0.5f
    }
}
