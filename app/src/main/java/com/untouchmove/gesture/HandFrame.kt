package com.untouchmove.gesture

/** Toa do 3D thuan Kotlin, khong phu thuoc Android/MediaPipe. */
data class Point3D(val x: Float, val y: Float, val z: Float)

/** Mot khung hinh ban tay da xu ly xong: world landmarks (21 diem) + do tin cay + thoi gian. */
data class HandFrame(
    val worldLandmarks: List<Point3D>,
    val confidence: Float,
    val timestampMs: Long,
)
