package com.untouchmove.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureStateMachineTest {

    /** Tu the VERTICAL (Len/Xuong): b,c dung khep nhau; d,e gap (SPEC muc 4.1). */
    private fun m1Landmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        fun p(x: Float, y: Float, z: Float = 0f) = Point3D(x + offsetX, y + offsetY, z)
        val points = MutableList(21) { p(0f, 0f) }
        points[0] = p(0f, 0f) // co tay
        points[5] = p(0f, 0f) // mcp tro
        points[9] = p(0f, 10f) // mcp giua -> S = 10
        points[13] = p(0f, 0f) // mcp ap ut
        points[17] = p(0f, 20f) // mcp ut
        points[4] = p(2f, 0f) // dau ngon cai (khep, t=0.2)
        points[8] = p(0f, 22f) // dau ngon tro (dung, r_b=1.6)
        points[12] = p(0f, 17f) // dau ngon giua (dung, r_c=1.1, g_bc=0.5 khep)
        points[16] = p(0f, 11f) // dau ngon ap ut (gap, r_d=0.5)
        points[20] = p(0f, 9f) // dau ngon ut (gap, r_e=0.3)
        return points
    }

    /**
     * Tu the HORIZONTAL (Trai/Phai, yeu cau nguoi dung 2026-09-20): b,c,d dung
     * khep nhau ca 3; e gap.
     */
    private fun horizontalLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        fun p(x: Float, y: Float, z: Float = 0f) = Point3D(x + offsetX, y + offsetY, z)
        val points = MutableList(21) { p(0f, 0f) }
        points[0] = p(0f, 0f) // co tay
        points[5] = p(0f, 0f) // mcp tro
        points[9] = p(0f, 10f) // mcp giua -> S = 10
        points[13] = p(0f, 0f) // mcp ap ut
        points[17] = p(0f, 20f) // mcp ut
        points[4] = p(2f, 0f) // dau ngon cai (khep, t=0.2)
        points[8] = p(0f, 22f) // dau ngon tro (dung, r_b=1.6)
        points[12] = p(0f, 17f) // dau ngon giua (dung, r_c=1.1, g_bc=0.5 khep)
        points[16] = p(0f, 16f) // dau ngon ap ut (dung, r_d=1.0, g_cd=0.1 khep)
        points[20] = p(0f, 9f) // dau ngon ut (gap, r_e=0.3)
        return points
    }

    /** Tu the M2 (SPEC muc 4.2): a,b,c dung; b,c khep; a xoe (t=0.7); d,e gap. */
    private fun cursorLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        fun p(x: Float, y: Float, z: Float = 0f) = Point3D(x + offsetX, y + offsetY, z)
        val points = MutableList(21) { p(0f, 0f) }
        points[0] = p(0f, 0f) // co tay
        points[5] = p(0f, 0f) // mcp tro
        points[9] = p(0f, 10f) // mcp giua -> S = 10
        points[13] = p(0f, 0f) // mcp ap ut
        points[17] = p(0f, 20f) // mcp ut
        points[4] = p(7f, 0f) // dau ngon cai xoe, t=0.7 >= T_OUT=0.62
        points[8] = p(0f, 22f) // dau ngon tro (dung, r_b=1.6)
        points[12] = p(0f, 17f) // dau ngon giua (dung, r_c=1.1, g_bc=0.5 khep)
        points[16] = p(0f, 11f) // dau ngon ap ut (gap, r_d=0.5)
        points[20] = p(0f, 9f) // dau ngon ut (gap, r_e=0.3)
        return points
    }

    /** Tu the nghi: ca 5 ngon deu dung (SPEC muc 4.0). */
    private fun allFiveUpLandmarks(): List<Point3D> {
        val points = MutableList(21) { Point3D(0f, 0f, 0f) }
        points[0] = Point3D(0f, 0f, 0f)
        points[5] = Point3D(0f, 0f, 0f)
        points[9] = Point3D(0f, 10f, 0f) // S = 10
        points[13] = Point3D(0f, 0f, 0f)
        points[17] = Point3D(0f, 20f, 0f)
        points[4] = Point3D(8f, 0f, 0f) // t=0.8 xoe
        points[8] = Point3D(0f, 22f, 0f) // r_b=1.6
        points[12] = Point3D(0f, 17f, 0f) // r_c=1.1
        points[16] = Point3D(0f, 16f, 0f) // r_d=1.0
        points[20] = Point3D(0f, 17f, 0f) // r_e=1.1
        return points
    }

    /**
     * Tu the M5 SPREAD (SPEC muc 4.5): b,c,d,e deu dung; a gap (thumb IN,
     * khac allFiveUpLandmarks dung thumb OUT); cap d-e roi rong qua nguong
     * G_OPEN4 (day pinky tip ra xa) - chi can 1 cap roi la du vao duoc.
     */
    private fun systemSpreadLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        fun p(x: Float, y: Float, z: Float = 0f) = Point3D(x + offsetX, y + offsetY, z)
        val points = MutableList(21) { p(0f, 0f) }
        points[0] = p(0f, 0f) // co tay
        points[5] = p(0f, 0f) // mcp tro
        points[9] = p(0f, 10f) // mcp giua -> S = 10
        points[13] = p(0f, 0f) // mcp ap ut
        points[17] = p(0f, 20f) // mcp ut
        points[4] = p(2f, 0f) // dau ngon cai khep, t=0.2 (IN)
        points[8] = p(0f, 22f) // dau ngon tro (dung, r_b=1.6)
        points[12] = p(0f, 17f) // dau ngon giua (dung, r_c=1.1)
        points[16] = p(0f, 16f) // dau ngon ap ut (dung, r_d=1.0)
        points[20] = p(50f, 17f) // dau ngon ut (dung, r_e~5.1) - day xa -> g_de roi rong (~2.5 >= G_OPEN4)
        return points
    }

    /**
     * Tu the M5 CLOSED (them 2026-09-20, lan 8): giong systemSpreadLandmarks
     * nhung dau ngon ut o gan lai (khong day xa) - ca 3 cap lien ke deu khep
     * (<= G_CLOSE), NGUOC voi SPREAD.
     */
    private fun systemClosedLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        val points = systemSpreadLandmarks(offsetX, offsetY).toMutableList()
        points[20] = Point3D(0f + offsetX, 17f + offsetY, 0f) // dau ngon ut ve gan lai -> g_de khep
        return points
    }

    /** Tu the "nam tay" (M6): ca 4 ngon deu gap sat, ngon cai khep. */
    private fun fistLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        fun p(x: Float, y: Float, z: Float = 0f) = Point3D(x + offsetX, y + offsetY, z)
        val points = MutableList(21) { p(0f, 0f) }
        points[0] = p(0f, 0f) // co tay
        points[5] = p(0f, 0f) // mcp tro
        points[9] = p(0f, 10f) // mcp giua -> S = 10
        points[13] = p(0f, 0f) // mcp ap ut
        points[17] = p(0f, 20f) // mcp ut
        points[4] = p(2f, 0f) // dau ngon cai khep, t=0.2 (IN)
        points[8] = p(0f, 9f) // dau ngon tro gap, r_b=0.3 (DOWN)
        points[12] = p(0f, 9f) // dau ngon giua gap, r_c=0.3 (DOWN)
        points[16] = p(0f, 9f) // dau ngon ap ut gap, r_d=0.3 (DOWN)
        points[20] = p(0f, 9f) // dau ngon ut gap, r_e=0.3 (DOWN)
        return points
    }

    private fun frame(landmarks: List<Point3D>, timestampMs: Long) =
        HandFrame(worldLandmarks = landmarks, confidence = 0.95f, timestampMs = timestampMs)

    @Test
    fun `khong kich hoat M1-M2-M5 khi 5 ngon mo`() {
        val machine = GestureStateMachine()
        var lastAction: GestureAction? = null
        for (i in 0 until 20) {
            lastAction = machine.onFrame(frame(allFiveUpLandmarks(), i * 50L))
        }
        assertNull(lastAction)
        // KHONG con la NONE: M6 (them 2026-09-22) coi 5 ngon mo la buoc dau
        // "san sang tat man hinh" - 950ms (20 khung) chua du SCREEN_LOCK_ARM_HOLD_MS
        // (1000ms) nen dang la ARMING (chua chuyen cam, chua phat hanh dong gi).
        assertEquals(DisplayState.ARMING, machine.displayState)
    }

    @Test
    fun `vao M1 sau khi giu tu the du ARM_HOLD_MS`() {
        val machine = GestureStateMachine()
        var t = 0L
        // vai khung dau de vote on dinh + du 500ms giu yen
        repeat(20) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_VERTICAL, machine.displayState)
    }

    @Test
    fun `khong vao M1 neu chua giu du thoi gian`() {
        val machine = GestureStateMachine()
        var t = 0L
        // chi giu duoc ~200ms, chua du ARM_HOLD_MS=500ms
        repeat(4) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.ARMING, machine.displayState)
    }

    @Test
    fun `tu the 3 ngon vay ngang phat ra swipe dung huong, co cooldown chong vuot lien tuc`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(horizontalLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_HORIZONTAL, machine.displayState)

        // vay nhanh theo X trong thoi gian ngan. velX > 0 = huong TRAI (da xac
        // nhan tren may that 2026-09-20, xem GestureStateMachine.detectSwipe).
        val swipeAction = machine.onFrame(frame(horizontalLandmarks(offsetX = 20f), t))
        assertTrue(swipeAction is GestureAction.Swipe)
        assertEquals(GestureAction.Direction.LEFT, (swipeAction as GestureAction.Swipe).direction)
        t += 50L

        // ngay sau do van dich chuyen tiep nhung dang trong SWIPE_COOLDOWN -> khong ban tiep
        val duringCooldown = machine.onFrame(frame(horizontalLandmarks(offsetX = 40f), t))
        assertNull(duringCooldown)
    }

    @Test
    fun `tu the 2 ngon vay doc phat ra swipe Len`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_VERTICAL, machine.displayState)

        // velY > 0 = huong LEN (da xac nhan tren may that 2026-09-20)
        val swipeAction = machine.onFrame(frame(m1Landmarks(offsetY = 20f), t))
        assertTrue(swipeAction is GestureAction.Swipe)
        assertEquals(GestureAction.Direction.UP, (swipeAction as GestureAction.Swipe).direction)
    }

    @Test
    fun `tu the 2 ngon KHONG kich hoat duoc Trai Phai du vay nhanh theo X`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_VERTICAL, machine.displayState)

        // Yeu cau nguoi dung 2026-09-20: Trai/Phai bat buoc tu the 3 ngon -
        // tu the 2 ngon (m1Landmarks) du vay manh theo X cung khong duoc tinh
        // la swipe (khac voi hanh vi cu truoc khi tach tu the).
        val action = machine.onFrame(frame(m1Landmarks(offsetX = 20f), t))
        assertNull(action)
    }

    @Test
    fun `khong vuot tiep duoc cho den khi tay dung yen lai, du da het cooldown`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(horizontalLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_HORIZONTAL, machine.displayState)

        val firstSwipe = machine.onFrame(frame(horizontalLandmarks(offsetX = 20f), t))
        assertTrue(firstSwipe is GestureAction.Swipe)
        t += 50L

        // Tay van dich chuyen nhanh xuyen suot 600ms (qua het SWIPE_COOLDOWN_MS)
        // - khong duoc vuot tiep vi chua "dung yen" (yeu cau nguoi dung 2026-09-20).
        var offset = 20f
        repeat(12) {
            offset += 20f
            val action = machine.onFrame(frame(horizontalLandmarks(offsetX = offset), t))
            assertNull(action)
            t += 50L
        }

        // Tay dung yen lai (khong dich chuyen them) du lau de cua so van toc
        // "sach" khoi doan di chuyen truoc
        repeat(6) {
            val action = machine.onFrame(frame(horizontalLandmarks(offsetX = offset), t))
            assertNull(action)
            t += 50L
        }

        // Da dung yen roi - vuot tiep duoc
        val secondSwipe = machine.onFrame(frame(horizontalLandmarks(offsetX = offset + 20f), t))
        assertTrue(secondSwipe is GestureAction.Swipe)
    }

    @Test
    fun `huy M1 khi tach b va c`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_VERTICAL, machine.displayState)

        // tach dau ngon tro va giua ra xa (g_bc tang vuot G_OPEN)
        val openLandmarks = m1Landmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f) // keo dau ngon giua ra xa
        repeat(5) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }
        assertEquals(DisplayState.NONE, machine.displayState)
    }

    @Test
    fun `vao M2 sau khi giu tu the du ARM_HOLD_MS`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)
    }

    @Test
    fun `M2 phat ra CursorMove dung chieu theo quy uoc pixel (phai = +x, xuong = +y)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        // Che do "theo tay" DAO DAU giong M1 (xac nhan tren may that 2026-09-20,
        // rieng che do "theo huong ngon tro" moi KHONG dao dau - xem test khac).
        val action = machine.onFrame(frame(cursorLandmarks(offsetX = -20f, offsetY = -20f), t))
        assertTrue(action is GestureAction.CursorMove)
        val move = action as GestureAction.CursorMove
        assertTrue("x phai duong (man hinh: phai = +x)", move.x > 0f)
        assertTrue("y phai duong (man hinh: xuong = +y)", move.y > 0f)
    }

    @Test
    fun `che do theo huong ngon tro phat ra vi tri tuyet doi theo do lech huong so voi luc vao M2`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Doi huong ngon tro: dau ngon tro (landmark 8) lech sang X duong
            // (world) - KHONG dao dau nhu M1 (xac nhan tren may that 2026-09-20).
            val pointedLandmarks = cursorLandmarks().toMutableList()
            pointedLandmarks[8] = Point3D(10f, 22f, 0f)
            val action = machine.onFrame(frame(pointedLandmarks, t))
            assertTrue(action is GestureAction.CursorMove)
            assertTrue("x phai duong khi ngon tro chi ve phia phai", (action as GestureAction.CursorMove).x > 0f)
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `che do theo huong ngon tro - b,c deu ha thi di chuyen XUONG lien tuc (yeu cau nguoi dung 2026-09-20 lan 3)`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Ha ca b va c xuong (r_b, r_c <= R_DOWN) - can nhieu khung de vote lat
            val bothDownLandmarks = cursorLandmarks().toMutableList()
            bothDownLandmarks[8] = Point3D(0f, 3f, 0f) // dau ngon tro, r_b~0.3
            bothDownLandmarks[12] = Point3D(0f, 2f, 0f) // dau ngon giua, r_c~0.4
            var lastAction: GestureAction? = null
            repeat(5) {
                lastAction = machine.onFrame(frame(bothDownLandmarks, t))
                t += 50L
            }
            assertTrue(lastAction is GestureAction.CursorMove)
            assertTrue("y phai duong (xuong) khi b,c deu ha", (lastAction as GestureAction.CursorMove).y > 0f)
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `che do theo huong ngon tro - r_b trong vung nghieng (0,65 den 0,85) van tinh la HA (yeu cau nguoi dung 2026-09-20 lan 6)`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Mo phong do dac tren may that (nghieng TRAI): r_b chi toi ~0.80
            // (nam giua R_DOWN=0.65 va R_UP=0.90, khong bao gio vuot nguong
            // "ha" thuong), r_c ha binh thuong. Truoc khi sua (dung pose.b ==
            // DOWN thuong), test nay se that bai vi pose.b ket cung o UP.
            val tiltedDownLandmarks = cursorLandmarks().toMutableList()
            tiltedDownLandmarks[8] = Point3D(0f, 14f, 0f) // dau ngon tro, r_b~0.8 (vung nghieng)
            tiltedDownLandmarks[12] = Point3D(0f, 2f, 0f) // dau ngon giua, r_c~0.4 (ha binh thuong)
            var lastAction: GestureAction? = null
            repeat(8) {
                lastAction = machine.onFrame(frame(tiltedDownLandmarks, t))
                t += 50L
            }
            assertTrue(lastAction is GestureAction.CursorMove)
            assertTrue(
                "y phai duong (xuong) du r_b chi o vung nghieng, khong xuong duoi R_DOWN thuong",
                (lastAction as GestureAction.CursorMove).y > 0f,
            )
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `che do theo huong ngon tro - b dung c ha thi di chuyen LEN lien tuc sau khi giu du CONFIRM_MS (yeu cau nguoi dung 2026-09-20 lan 3 va 5)`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Chi ha dau ngon giua (c), giu ngon tro (b) dung nhu cu. Can du
            // khung de: (1) Voter cua c vote xong DOWN (4/5 khung), (2) sau do
            // giu on dinh du CURSOR_POINTING_UP_CONFIRM_MS (lan 5 - "LEN" phai
            // giu on dinh du lau moi cong nhan, xem GestureThresholds).
            val bUpCDownLandmarks = cursorLandmarks().toMutableList()
            bUpCDownLandmarks[12] = Point3D(0f, 2f, 0f) // dau ngon giua, r_c~0.4
            var lastAction: GestureAction? = null
            repeat(12) {
                lastAction = machine.onFrame(frame(bUpCDownLandmarks, t))
                t += 50L
            }
            assertTrue(lastAction is GestureAction.CursorMove)
            assertTrue("y phai am (len) khi b dung c ha", (lastAction as GestureAction.CursorMove).y < 0f)
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `che do theo huong ngon tro - doi khong dong bo tu dung yen sang XUONG khong duoc giat LEN (yeu cau nguoi dung 2026-09-20 lan 5)`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Mo phong tay that: c bat dau ha TRUOC b khoang 100ms (2 khung) -
            // dung LA nguyen nhan thuc te gay "b dung, c ha" (=LEN) thoang qua
            // trong luc ca 2 ngon dang cung chuyen sang "deu ha", khong phai
            // nguoi dung co y dinh di chuyen len. Con tro KHONG duoc di chuyen
            // len (cumulativeY am) tai bat ky khung nao trong suot qua trinh.
            val bDownLandmark = Point3D(0f, 3f, 0f) // dau ngon tro, r_b~0.3
            val cDownLandmark = Point3D(0f, 2f, 0f) // dau ngon giua, r_c~0.4
            val yValues = mutableListOf<Float>()
            repeat(10) { i ->
                val landmarks = cursorLandmarks().toMutableList()
                if (i >= 1) landmarks[12] = cDownLandmark // c bat dau ha o khung thu 2
                if (i >= 3) landmarks[8] = bDownLandmark // b bat dau ha o khung thu 4 (tre hon c)
                val action = machine.onFrame(frame(landmarks, t))
                if (action is GestureAction.CursorMove) yValues.add(action.y)
                t += 50L
            }
            assertTrue("khong duoc co lan nao con tro di chuyen LEN (y am)", yValues.all { it >= 0f })
            assertTrue("cuoi cung phai di chuyen XUONG khi ca 2 ngon deu ha xong", (yValues.lastOrNull() ?: 0f) > 0f)
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `che do theo huong ngon tro - tu the LEN (b dung c ha) khong duoc tinh la click (yeu cau nguoi dung 2026-09-20 lan 5)`() {
        val originalMode = GestureThresholds.CURSOR_POINTING_MODE
        GestureThresholds.CURSOR_POINTING_MODE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M2, machine.displayState)

            // Tu the LEN (b dung, c ha) cung lam 2 dau ngon xa nhau ve mat
            // hinh hoc, thoa dieu kien "tach" (g_bc >= G_OPEN) y het tu the
            // click - PHAI khong duoc tinh la mot lan tach/khep click.
            val bUpCDownLandmarks = cursorLandmarks().toMutableList()
            bUpCDownLandmarks[12] = Point3D(0f, 2f, 0f)
            repeat(6) {
                machine.onFrame(frame(bUpCDownLandmarks, t))
                t += 50L
            }

            // Quay lai tu the dung yen (b,c deu dung, khep lai) nhanh, trong
            // CLICK_MAX_MS neu tinh tu luc "tach" o tren.
            var lastAction: GestureAction? = null
            repeat(4) {
                lastAction = machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertTrue("khong duoc la click", lastAction !is GestureAction.Click)
        } finally {
            GestureThresholds.CURSOR_POINTING_MODE = originalMode
        }
    }

    @Test
    fun `tach roi khep nhanh b,c trong CLICK_MAX_MS phat ra Click`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f) // keo dau ngon giua ra xa -> g_bc vuot G_OPEN

        // Can >=4 khung "tach" de vote lat sang OPEN (Voter can 4/5, xem PoseClassifier)
        repeat(4) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }

        // Khep lai nhanh - can >=4 khung de vote lat lai CLOSE
        var lastAction: GestureAction? = null
        repeat(4) {
            lastAction = machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertTrue(lastAction is GestureAction.Click)
    }

    @Test
    fun `giu tach qua CLICK_MAX_MS roi khep thi KHONG phai la click`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)

        repeat(4) { // lat vote sang OPEN
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }
        repeat(10) { // giu tach lau, vuot han CLICK_MAX_MS=300ms
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }

        var lastAction: GestureAction? = null
        repeat(4) { // khep lai
            lastAction = machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertTrue(lastAction !is GestureAction.Click)
    }

    @Test
    fun `tach qua CLICK_MAX_MS phat HoldStart tai toa do luc bat dau tach (SPEC M4)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)
        repeat(4) { // lat vote sang OPEN - cung la luc "bat dau tach"
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }

        var holdStart: GestureAction.HoldStart? = null
        repeat(15) { // du CLICK_MAX_MS=600ms (15*50ms=750ms)
            val action = machine.onFrame(frame(openLandmarks, t))
            if (action is GestureAction.HoldStart) holdStart = action
            t += 50L
        }
        assertTrue("phai phat HoldStart sau khi giu qua CLICK_MAX_MS", holdStart != null)
        // Tay dung yen tu luc vao M2 toi luc tach - con tro van o giua man hinh.
        assertEquals(0f, holdStart!!.x, 0.01f)
        assertEquals(0f, holdStart!!.y, 0.01f)
    }

    @Test
    fun `trong luc dang giu, tay di chuyen thi phat HoldMove (SPEC M4 vua hold vua move)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)
        repeat(4) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }

        var holding = false
        repeat(15) {
            val action = machine.onFrame(frame(openLandmarks, t))
            if (action is GestureAction.HoldStart) holding = true
            t += 50L
        }
        assertTrue(holding)

        // Di chuyen ca ban tay (giu nguyen do tach b,c) - offsetX am -> x
        // man hinh duong (phai), cung quy uoc voi test CursorMove khac.
        val movedOpenLandmarks = cursorLandmarks(offsetX = -20f).toMutableList()
        movedOpenLandmarks[12] = Point3D(10f, 17f, 0f) // 30-20, giu nguyen do tach tuong doi
        var holdMove: GestureAction.HoldMove? = null
        repeat(5) {
            val action = machine.onFrame(frame(movedOpenLandmarks, t))
            if (action is GestureAction.HoldMove) holdMove = action
            t += 50L
        }
        assertTrue("phai phat HoldMove khi tay di chuyen trong luc dang giu", holdMove != null)
        assertTrue("x phai duong (phai) giong quy uoc CursorMove", holdMove!!.x > 0f)
    }

    @Test
    fun `giu tach qua HOLD_MAX_MS tu nha va khoa khong cho giu lai cho toi khi khep tay that su (SPEC M4, an toan bat buoc)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)
        repeat(4) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }
        val separatedAtMs = t

        var sawHoldStart = false
        var forcedHoldEndCount = 0
        while (t < separatedAtMs + GestureThresholds.HOLD_MAX_MS + 500L) {
            val action = machine.onFrame(frame(openLandmarks, t))
            if (action is GestureAction.HoldStart) sawHoldStart = true
            if (action is GestureAction.HoldEnd) forcedHoldEndCount++
            t += 50L
        }
        assertTrue("phai vao duoc GIU truoc khi bi tu nha", sawHoldStart)
        assertEquals("phai tu nha DUNG 1 lan khi cham HOLD_MAX_MS", 1, forcedHoldEndCount)

        // Van dang tach (chua khep) - bi khoa, khong duoc tu dong giu/click lai.
        repeat(10) {
            val action = machine.onFrame(frame(openLandmarks, t))
            assertTrue(
                "dang bi khoa sau khi tu nha, khong duoc phat hanh dong giu/click nao",
                action !is GestureAction.HoldStart && action !is GestureAction.Click,
            )
            t += 50L
        }

        // Khep tay lai that su -> mo khoa.
        repeat(4) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        // Tach lai va giu du lau -> phai vao duoc PHIEN GIU MOI binh thuong.
        repeat(4) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }
        var sawSecondHoldStart = false
        repeat(15) {
            val action = machine.onFrame(frame(openLandmarks, t))
            if (action is GestureAction.HoldStart) sawSecondHoldStart = true
            t += 50L
        }
        assertTrue("phai vao lai duoc GIU sau khi da khep tay that su", sawSecondHoldStart)
    }

    @Test
    fun `mat tay trong luc dang giu thi nha ngay (SPEC M4 an toan bat buoc, onHandLost tra ve HoldEnd)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)
        repeat(4) {
            machine.onFrame(frame(openLandmarks, t))
            t += 50L
        }

        var holding = false
        repeat(15) {
            val action = machine.onFrame(frame(openLandmarks, t))
            if (action is GestureAction.HoldStart) holding = true
            t += 50L
        }
        assertTrue(holding)

        val released = machine.onHandLost()
        assertTrue("mat tay trong luc dang giu phai nha ngay", released is GestureAction.HoldEnd)
        // M2 khong bi huy khi mat tay (da xac nhan o test rieng) - o day chi
        // kiem tra dung phan nha giu.
        assertEquals(DisplayState.M2, machine.displayState)
    }

    @Test
    fun `tach roi khep qua nhanh (duoi CLICK_MIN_MS) thi KHONG phai click (yeu cau nguoi dung 2026-09-20 lan 7)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }

        val openLandmarks = cursorLandmarks().toMutableList()
        openLandmarks[12] = Point3D(30f, 17f, 0f)

        // Dung buoc khung NHANH (20ms/khung, van trong pham vi thuc te do
        // duoc 23-97ms/khung) de tao 1 lan tach-khep RAT NGAN qua Voter (4
        // khung lat OPEN + 4 khung lat lai CLOSE = ~80ms, duoi
        // CLICK_MIN_MS=100ms) - mo phong rung tay/nhieu tracking thoang qua
        // luc dang dinh di chuyen, khong phai co y click that.
        repeat(4) { // lat vote sang OPEN
            machine.onFrame(frame(openLandmarks, t))
            t += 20L
        }
        var lastAction: GestureAction? = null
        repeat(4) { // khep lai ngay, lat vote ve CLOSE
            lastAction = machine.onFrame(frame(cursorLandmarks(), t))
            t += 20L
        }
        assertTrue("tach-khep qua nhanh khong duoc tinh la click", lastAction !is GestureAction.Click)
    }

    @Test
    fun `gap rieng ngon c (b van dung) khong duoc tinh la tach du gBC no ra - ap dung ca che do theo tay (yeu cau nguoi dung 2026-09-20 lan 7)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        // KHONG bat CURSOR_POINTING_MODE - dung che do mac dinh "theo tay".
        // Chi gap dau ngon giua (c) xuong gan long ban tay (khong tach
        // ngang), b van dung nhu cu - gBC vAn no ra (>= G_OPEN) vi 2 dau ngon
        // xa nhau, nhung day la GAP NGON, khong phai TACH NGON that.
        val cCurledLandmarks = cursorLandmarks().toMutableList()
        cCurledLandmarks[12] = Point3D(0f, 2f, 0f) // dau ngon giua, r_c~0.4 (ha)
        var sawClickOrHoldStart = false
        repeat(15) { // du de vote xong VA du CLICK_MAX_MS neu bi tinh nham
            val action = machine.onFrame(frame(cCurledLandmarks, t))
            if (action is GestureAction.Click || action is GestureAction.HoldStart) sawClickOrHoldStart = true
            t += 50L
        }
        assertTrue("gap ngon c khong duoc tinh la tach/click/hold", !sawClickOrHoldStart)
    }

    @Test
    fun `M2 khong huy du ngon cai khep lai - yeu cau nguoi dung 2026-09-20`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        // Sau khi vao M2, khep ngon cai lai gan (t giam manh) KHONG con huy
        // che do nua - chi con quan tam b,c (yeu cau nguoi dung: do goc xoe
        // ngon cai qua nhieu, theo doi no de huy gay vao/thoat M2 that thuong).
        val closedThumbLandmarks = cursorLandmarks().toMutableList()
        closedThumbLandmarks[4] = Point3D(2f, 0f, 0f) // t = 0.2, truoc day se huy
        machine.onFrame(frame(closedThumbLandmarks, t))
        assertEquals(DisplayState.M2, machine.displayState)
    }

    @Test
    fun `M2 khong huy khi mat tay - chi thoat bang xoe ca 5 ngon (yeu cau nguoi dung 2026-09-20)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        // Mat tay (vd tam thoi ra khoi khung) - KHONG con huy M2 nua, con tro
        // phai dung yen tai vi tri cuoi cung thay vi bien mat.
        machine.onHandLost()
        assertEquals(DisplayState.M2, machine.displayState)

        // Bat lai duoc tay, tiep tuc o M2 binh thuong
        machine.onFrame(frame(cursorLandmarks(), t))
        assertEquals(DisplayState.M2, machine.displayState)

        // Chi xoe ca 5 ngon moi thoat duoc - can nhieu khung de vote lat
        // (Voter can 4/5, xem PoseClassifier)
        repeat(5) {
            machine.onFrame(frame(allFiveUpLandmarks(), t))
            t += 50L
        }
        // Da roi M2 that (khong con DisplayState.M2). Ket qua la ARMING chu
        // khong phai NONE vi M6 (them 2026-09-22) coi xoe 5 ngon la buoc dau
        // "san sang tat man hinh" - 250ms (5 khung) chua du SCREEN_LOCK_ARM_HOLD_MS
        // (1000ms) nen chua chuyen mau cam, dung DisplayState.ARMING chung.
        assertEquals(DisplayState.ARMING, machine.displayState)
    }

    @Test
    fun `dang o M2 thi doi sang tu the vuot 2-3 ngon KHONG kich hoat duoc M1`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(cursorLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)

        // Doi sang dung tu the vuot doc (2 ngon) trong luc dang o M2 - khong
        // duoc coi la roi M2 hay vao M1, chi la chuyen dong tay binh thuong.
        repeat(10) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M2, machine.displayState)
    }

    @Test
    fun `tat rieng M1 2 ngon thi tu the Len-Xuong khong kich hoat gi ca`() {
        val original = GestureThresholds.ENABLE_M1_VERTICAL
        GestureThresholds.ENABLE_M1_VERTICAL = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(m1Landmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)
        } finally {
            GestureThresholds.ENABLE_M1_VERTICAL = original
        }
    }

    @Test
    fun `tat rieng M1 3 ngon thi tu the Trai-Phai khong kich hoat gi ca, 2 ngon van binh thuong`() {
        val original = GestureThresholds.ENABLE_M1_HORIZONTAL
        GestureThresholds.ENABLE_M1_HORIZONTAL = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(horizontalLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)

            machine.onHandLost()
            repeat(20) {
                machine.onFrame(frame(m1Landmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.M1_VERTICAL, machine.displayState)
        } finally {
            GestureThresholds.ENABLE_M1_HORIZONTAL = original
        }
    }

    /**
     * Tu the TRUNG GIAN khi tay dang chuyen tu 3 ngon (M1 ngang) sang 4 ngon
     * (M5): b,c,d nhu horizontalLandmarks nhung dau ngon ut nhich len toi
     * r_e~0.8 - nam trong "vung xam" cua pose.e THUONG (AMBIGUOUS, giua
     * R_DOWN=0.65 va R_UP=0.90, KHONG ghi de duoc gia tri DOWN cu con luu
     * trong Voter) nhung da vuot SYSTEM_E_UP_RELAXED=0.78 nen pose.eUpRelaxed
     * doc duoc UP ngay. Day chinh la khung hinh gay nham theo bao cao nguoi
     * dung 2026-09-22.
     */
    private fun intermediateThreeToFourFingerLandmarks(offsetX: Float = 0f, offsetY: Float = 0f): List<Point3D> {
        val points = horizontalLandmarks(offsetX, offsetY).toMutableList()
        points[20] = Point3D(0f + offsetX, 14f + offsetY, 0f) // r_e~0.8
        return points
    }

    @Test
    fun `tat M5 thi tu the trung gian 3-sang-4-ngon khong duoc nham thanh M1 ngang`() {
        val original = GestureThresholds.ENABLE_M5_SYSTEM
        GestureThresholds.ENABLE_M5_SYSTEM = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            // On dinh pose.e = DOWN that (dung tu the 3 ngon that truoc), mo
            // phong nguoi dung vua vay 3 ngon xong.
            repeat(6) {
                machine.onFrame(frame(horizontalLandmarks(), t))
                t += 50L
            }
            // Nhich dau ngon ut len vung xam - dang co y dua 4 ngon len (M5,
            // dang tat) chu khong phai co y giu tu the 3 ngon.
            repeat(10) {
                machine.onFrame(frame(intermediateThreeToFourFingerLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)
        } finally {
            GestureThresholds.ENABLE_M5_SYSTEM = original
        }
    }

    @Test
    fun `tat M2 thi tu the con tro khong kich hoat gi ca`() {
        val original = GestureThresholds.ENABLE_M2_CURSOR
        GestureThresholds.ENABLE_M2_CURSOR = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(cursorLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)
        } finally {
            GestureThresholds.ENABLE_M2_CURSOR = original
        }
    }

    @Test
    fun `tat M5 thi tu the 4 ngon khong kich hoat gi ca`() {
        val original = GestureThresholds.ENABLE_M5_SYSTEM
        GestureThresholds.ENABLE_M5_SYSTEM = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(20) {
                machine.onFrame(frame(systemSpreadLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)
        } finally {
            GestureThresholds.ENABLE_M5_SYSTEM = original
        }
    }

    @Test
    fun `mat tay ve IDLE ngay lap tuc`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(m1Landmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M1_VERTICAL, machine.displayState)

        machine.onHandLost()
        assertEquals(DisplayState.NONE, machine.displayState)
    }

    @Test
    fun `M5 nhan dien duoc du ngon ut chi dua len mot phan, khong bi lan sang M1 3 ngon (eUpRelaxed, SPEC M5 lan 10)`() {
        val machine = GestureStateMachine()
        val landmarks = systemSpreadLandmarks().toMutableList()
        landmarks[8] = Point3D(40f, 22f, 0f) // day ngon tro ra xa -> bcWide OPEN (thay cho de lam cap "roi rong")
        // Ngon ut chi dua len MOT PHAN: r_e~0.85 - duoi R_UP=0.90 thuong (truoc
        // day se la AMBIGUOUS, khong vao duoc M5) nhung tren
        // SYSTEM_E_UP_RELAXED=0.78 (yeu cau nguoi dung 2026-09-20 lan 10: "4
        // ngon dua len de bi lan voi 3 ngon [M1 ngang]").
        landmarks[20] = Point3D(0f, 14.5f, 0f)
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(landmarks, t))
            t += 50L
        }
        assertEquals(DisplayState.M5_SPREAD, machine.displayState)
    }

    @Test
    fun `chuyen thang tu 3 ngon sang 4 ngon khep khong bi dinh pose_e cu, vao dung M5_CLOSED (SPEC M5 lan 13)`() {
        val machine = GestureStateMachine()
        var t = 0L
        // Lam tu the 3 ngon (M1 ngang) du de eVoter binh thuong on dinh DOWN
        // (chua can vao het M1, chi can du phieu cho Voter).
        repeat(8) {
            machine.onFrame(frame(horizontalLandmarks(), t))
            t += 50L
        }

        // Chuyen THANG sang 4 ngon khep, nhung r_e chi ~0.8 (do that tren may
        // 2026-09-20: "3 ngon" r_e~0.45, "4 ngon khep" r_e~0.8) - nam trong
        // vung AMBIGUOUS cua pose.e thang do THUONG (R_DOWN=0.65-R_UP=0.90),
        // khong du phieu THAT de ghi de gia tri DOWN cu con sot lai. Neu
        // isHorizontalEntryPose duoc xet TRUOC va dua vao pose.e nay, se bi
        // dinh nham thanh M1 ngang thay vi M5_CLOSED.
        val landmarks = systemClosedLandmarks().toMutableList()
        landmarks[20] = Point3D(0f, 14f, 0f) // r_e~0.8
        repeat(20) {
            machine.onFrame(frame(landmarks, t))
            t += 50L
        }
        assertEquals(DisplayState.M5_CLOSED, machine.displayState)
    }

    @Test
    fun `M5 tu the TACH (SPREAD) - vay TRAI phat HOME, vay lan 2 trong cung phien bi chan, huy bang khep lai (SPEC M5 lan 9)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(systemSpreadLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M5_SPREAD, machine.displayState)

        // Vay nhanh TRAI. velX > 0 = TRAI (quy uoc giong M1, xem detectSwipe).
        // Yeu cau nguoi dung 2026-09-20 lan 9: doi lai dung Trai/Phai de kich hoat.
        val action = machine.onFrame(frame(systemSpreadLandmarks(offsetX = 20f), t))
        assertTrue(action is GestureAction.SystemAction)
        assertEquals(GestureAction.SystemActionType.HOME, (action as GestureAction.SystemAction).type)
        t += 50L

        // Da vay 1 lan trong phien nay - vay tiep KHONG duoc tinh them (SPEC
        // muc 4.5: "chi cho phep mot hanh dong moi lan vao che do").
        var sawSecondAction = false
        repeat(10) {
            val next = machine.onFrame(frame(systemSpreadLandmarks(offsetX = 20f * (it + 2)), t))
            if (next is GestureAction.SystemAction) sawSecondAction = true
            t += 50L
        }
        assertTrue("khong duoc vay lan 2 trong cung 1 phien", !sawSecondAction)
        assertEquals(DisplayState.M5_SPREAD, machine.displayState)

        // Huy bang cach KHEP LAI - NGUOC voi tu the vao TACH (yeu cau nguoi dung lan 8).
        repeat(4) { // du khung de vote lat ve CLOSE
            machine.onFrame(frame(systemClosedLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.NONE, machine.displayState)
    }

    @Test
    fun `M5 tu the KHEP (CLOSED) - vay PHAI phat RECENTS, huy bang tach ra, vao lai TACH van vay duoc (SPEC M5 lan 9)`() {
        val machine = GestureStateMachine()
        var t = 0L
        repeat(20) {
            machine.onFrame(frame(systemClosedLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M5_CLOSED, machine.displayState)

        // Vay nhanh PHAI. velX < 0 = PHAI.
        val action = machine.onFrame(frame(systemClosedLandmarks(offsetX = -20f), t))
        assertTrue(action is GestureAction.SystemAction)
        assertEquals(GestureAction.SystemActionType.RECENTS, (action as GestureAction.SystemAction).type)
        t += 50L

        // Huy bang cach TACH RA - NGUOC voi tu the vao KHEP (yeu cau nguoi dung lan 8).
        repeat(4) { // du khung de vote lat ve OPEN
            machine.onFrame(frame(systemSpreadLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.NONE, machine.displayState)

        // Vao lai tu the TACH va vay - phai hoat dong binh thuong (khong con
        // bi khoa "da vay 1 lan" cua phien truoc, va khong con bi ket o
        // SystemPoseMode.CLOSED cua phien truoc).
        repeat(20) {
            machine.onFrame(frame(systemSpreadLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.M5_SPREAD, machine.displayState)
        val action2 = machine.onFrame(frame(systemSpreadLandmarks(offsetX = 20f), t))
        assertTrue("phai vao lai va vay duoc sau khi huy", action2 is GestureAction.SystemAction)
        assertEquals(GestureAction.SystemActionType.HOME, (action2 as GestureAction.SystemAction).type)
    }

    // --- M6: xoe 5 ngon dung yen roi nam tay -> tat man hinh (yeu cau nguoi dung 2026-09-22) ---

    @Test
    fun `xoe 5 ngon duoi 1 giay van la ARMING, qua 1 giay moi chuyen mau cam roi nam tay moi tat man hinh`() {
        val machine = GestureStateMachine()
        var t = 0L
        // Duoi 1000ms (toi 700ms): van la ARMING, chua san sang.
        repeat(15) {
            machine.onFrame(frame(allFiveUpLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.ARMING, machine.displayState)

        // Vuot qua 1000ms: chuyen SCREEN_LOCK_ARMED (mau cam, san sang nam tay).
        repeat(10) {
            machine.onFrame(frame(allFiveUpLandmarks(), t))
            t += 50L
        }
        assertEquals(DisplayState.SCREEN_LOCK_ARMED, machine.displayState)

        // Nam tay lai (can vai khung de Voter vote lat) -> phat ScreenOff.
        var action: GestureAction? = null
        repeat(5) {
            val a = machine.onFrame(frame(fistLandmarks(), t))
            if (a != null) action = a
            t += 50L
        }
        assertEquals(GestureAction.ScreenOff, action)
    }

    @Test
    fun `tat M6 thi xoe 5 ngon dung yen roi nam tay khong tat man hinh`() {
        val original = GestureThresholds.ENABLE_M6_SCREEN_OFF
        GestureThresholds.ENABLE_M6_SCREEN_OFF = false
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(30) {
                machine.onFrame(frame(allFiveUpLandmarks(), t))
                t += 50L
            }
            assertEquals(DisplayState.NONE, machine.displayState)

            var action: GestureAction? = null
            repeat(5) {
                val a = machine.onFrame(frame(fistLandmarks(), t))
                if (a != null) action = a
                t += 50L
            }
            assertNull(action)
        } finally {
            GestureThresholds.ENABLE_M6_SCREEN_OFF = original
        }
    }

    @Test
    fun `mac dinh tat cong tac mo lai - sau khi tat man hinh xoe 5 ngon lai khong phat ScreenOn`() {
        assertEquals(false, GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE)
        val machine = GestureStateMachine()
        var t = 0L
        repeat(25) {
            machine.onFrame(frame(allFiveUpLandmarks(), t))
            t += 50L
        }
        var screenOffAction: GestureAction? = null
        repeat(5) {
            val a = machine.onFrame(frame(fistLandmarks(), t))
            if (a != null) screenOffAction = a
            t += 50L
        }
        assertEquals(GestureAction.ScreenOff, screenOffAction)

        var afterAction: GestureAction? = null
        repeat(10) {
            val a = machine.onFrame(frame(allFiveUpLandmarks(), t))
            if (a != null) afterAction = a
            t += 50L
        }
        assertNull(afterAction)
    }

    @Test
    fun `bat cong tac mo lai bang cu chi - nam tay du 0,5 giay roi xoe 5 ngon se phat ScreenOn`() {
        val original = GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE
        GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE = true
        try {
            val machine = GestureStateMachine()
            var t = 0L
            repeat(25) {
                machine.onFrame(frame(allFiveUpLandmarks(), t))
                t += 50L
            }
            var screenOffAction: GestureAction? = null
            repeat(5) {
                val a = machine.onFrame(frame(fistLandmarks(), t))
                if (a != null) screenOffAction = a
                t += 50L
            }
            assertEquals(GestureAction.ScreenOff, screenOffAction)

            // Giu nam tay them cho qua nguong SCREEN_OFF_REOPEN_HOLD_MS (500ms).
            repeat(10) {
                machine.onFrame(frame(fistLandmarks(), t))
                t += 50L
            }

            var screenOnAction: GestureAction? = null
            repeat(5) {
                val a = machine.onFrame(frame(allFiveUpLandmarks(), t))
                if (a != null) screenOnAction = a
                t += 50L
            }
            assertEquals(GestureAction.ScreenOn, screenOnAction)
        } finally {
            GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE = original
        }
    }
}
