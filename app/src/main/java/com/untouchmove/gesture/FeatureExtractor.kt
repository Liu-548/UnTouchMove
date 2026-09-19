package com.untouchmove.gesture

import kotlin.math.sqrt

/** Cac dai luong dac trung tinh tu world landmarks, xem docs/SPEC.md muc 2. */
data class Features(
    val s: Float,
    val p: Point3D,
    val rB: Float,
    val rC: Float,
    val rD: Float,
    val rE: Float,
    val gBC: Float,
    val gCD: Float,
    val gDE: Float,
    val t: Float,
)

/** Tinh r, g, t, P, S tu 21 world landmark cua HandLandmarker (thuan Kotlin, khong phu thuoc Android). */
object FeatureExtractor {

    // Landmark index theo quy uoc MediaPipe HandLandmarker (SPEC muc 1)
    private const val WRIST = 0
    private const val THUMB_TIP = 4
    private const val INDEX_MCP = 5
    private const val INDEX_TIP = 8
    private const val MIDDLE_MCP = 9
    private const val MIDDLE_TIP = 12
    private const val RING_MCP = 13
    private const val RING_TIP = 16
    private const val PINKY_MCP = 17
    private const val PINKY_TIP = 20

    fun extract(worldLandmarks: List<Point3D>): Features {
        require(worldLandmarks.size == 21) {
            "Can dung 21 landmark, nhan duoc ${worldLandmarks.size}"
        }

        val p = average(
            worldLandmarks[WRIST],
            worldLandmarks[INDEX_MCP],
            worldLandmarks[MIDDLE_MCP],
            worldLandmarks[RING_MCP],
            worldLandmarks[PINKY_MCP],
        )
        val s = dist(worldLandmarks[WRIST], worldLandmarks[MIDDLE_MCP])

        fun r(tip: Int) = dist(worldLandmarks[tip], p) / s
        fun g(tipX: Int, tipY: Int, mcpX: Int, mcpY: Int) =
            dist(worldLandmarks[tipX], worldLandmarks[tipY]) /
                dist(worldLandmarks[mcpX], worldLandmarks[mcpY])

        return Features(
            s = s,
            p = p,
            rB = r(INDEX_TIP),
            rC = r(MIDDLE_TIP),
            rD = r(RING_TIP),
            rE = r(PINKY_TIP),
            gBC = g(INDEX_TIP, MIDDLE_TIP, INDEX_MCP, MIDDLE_MCP),
            gCD = g(MIDDLE_TIP, RING_TIP, MIDDLE_MCP, RING_MCP),
            gDE = g(RING_TIP, PINKY_TIP, RING_MCP, PINKY_MCP),
            t = dist(worldLandmarks[THUMB_TIP], worldLandmarks[INDEX_MCP]) / s,
        )
    }

    private fun average(vararg points: Point3D): Point3D = Point3D(
        x = points.sumOf { it.x.toDouble() }.toFloat() / points.size,
        y = points.sumOf { it.y.toDouble() }.toFloat() / points.size,
        z = points.sumOf { it.z.toDouble() }.toFloat() / points.size,
    )

    private fun dist(a: Point3D, b: Point3D): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
