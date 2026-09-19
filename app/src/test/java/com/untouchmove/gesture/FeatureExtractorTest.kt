package com.untouchmove.gesture

import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureExtractorTest {

    /** Dung Point3D(0,0,0) cho landmark khong lien quan, chi dat gia tri o index can test. */
    private fun landmarks(vararg overrides: Pair<Int, Point3D>): List<Point3D> {
        val points = MutableList(21) { Point3D(0f, 0f, 0f) }
        overrides.forEach { (index, point) -> points[index] = point }
        return points
    }

    @Test
    fun `tinh dung r, g, t tren mot ban tay tong hop`() {
        val world = landmarks(
            0 to Point3D(0f, 0f, 0f), // co tay
            5 to Point3D(0f, 0f, 0f), // mcp ngon tro
            9 to Point3D(0f, 10f, 0f), // mcp ngon giua -> S = 10
            13 to Point3D(0f, 0f, 0f), // mcp ngon ap ut
            17 to Point3D(0f, 20f, 0f), // mcp ngon ut
            4 to Point3D(12f, 0f, 0f), // dau ngon cai
            8 to Point3D(0f, 22f, 0f), // dau ngon tro
            12 to Point3D(0f, 17f, 0f), // dau ngon giua
            16 to Point3D(0f, 11f, 0f), // dau ngon ap ut
            20 to Point3D(0f, 26f, 0f), // dau ngon ut
        )

        val f = FeatureExtractor.extract(world)

        assertEquals(10f, f.s, 1e-4f)
        assertEquals(0f, f.p.x, 1e-4f)
        assertEquals(6f, f.p.y, 1e-4f)
        assertEquals(1.6f, f.rB, 1e-4f)
        assertEquals(1.1f, f.rC, 1e-4f)
        assertEquals(0.5f, f.rD, 1e-4f)
        assertEquals(2.0f, f.rE, 1e-4f)
        assertEquals(0.5f, f.gBC, 1e-4f)
        assertEquals(0.6f, f.gCD, 1e-4f)
        assertEquals(0.75f, f.gDE, 1e-4f)
        assertEquals(1.2f, f.t, 1e-4f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `bao loi khi khong du 21 landmark`() {
        FeatureExtractor.extract(listOf(Point3D(0f, 0f, 0f)))
    }
}
