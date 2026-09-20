package com.untouchmove.gesture

import kotlin.math.sqrt

/** Toa do 3D thuan Kotlin, khong phu thuoc Android/MediaPipe. */
data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: Point3D): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

/** Mot khung hinh ban tay da xu ly xong: world landmarks (21 diem) + do tin cay + thoi gian. */
data class HandFrame(
    val worldLandmarks: List<Point3D>,
    val confidence: Float,
    val timestampMs: Long,
)
