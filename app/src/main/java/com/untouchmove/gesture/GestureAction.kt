package com.untouchmove.gesture

/** Hanh dong ma GestureStateMachine phat ra, de lop Android ben ngoai bom ra that. */
sealed class GestureAction {
    data class Swipe(val direction: Direction) : GestureAction()

    /**
     * M2 (SPEC muc 4.2): vi tri TUYET DOI cua con tro (lech so voi tam man
     * hinh), don vi truu tuong (da nhan CURSOR_GAIN, chua quy doi pixel - lop
     * Android tu quy doi theo mat do man hinh, xem UnTouchAccessibilityService).
     * KHONG phai delta/chuyen dong tuong doi - doi tu delta sang tuyet doi
     * (2026-09-20) de dung chung cho ca 2 che do di chuyen (theo tay / theo
     * huong ngon tro, xem GestureThresholds.CURSOR_POINTING_MODE): che do
     * "theo tay" van tich luy delta NOI BO trong GestureStateMachine roi moi
     * phat ra tong, con "theo huong ngon tro" tinh thang vi tri tuyet doi moi
     * khung tu do lech huong - ca 2 deu ra cung 1 dang du lieu cho lop Android.
     */
    data class CursorMove(val x: Float, val y: Float) : GestureAction()

    /**
     * M3 (SPEC muc 4.3): toa do click CUNG DON VI voi CursorMove, chup tai
     * THOI DIEM BAT DAU TACH b,c - khong phai luc khep lai. Lop Android quy
     * doi bang chinh cong thuc da dung cho CursorMove de dam bao khop voi vi
     * tri con tro dang hien thi.
     */
    data class Click(val x: Float, val y: Float) : GestureAction()

    /**
     * M4 (SPEC muc 4.4): b,c tach qua CLICK_MAX_MS ma chua khep lai -> chuyen
     * thanh "giu" (khac voi Click). x,y cung don vi/cong thuc voi CursorMove,
     * la vi tri LUC BAT DAU TACH (giong Click) - lop Android dung de bat dau
     * "nham xuong" dung diem con tro dang hien.
     */
    data class HoldStart(val x: Float, val y: Float) : GestureAction()

    /**
     * Phat moi khung trong luc dang giu (sau HoldStart, truoc HoldEnd) MA vi
     * tri con tro thay doi - tay di chuyen thi keo theo (SPEC muc 4.4 "vua
     * hold vua move"). Thay the CursorMove trong khoang thoi gian nay (khong
     * phat ca 2 cung luc) vi lop Android vua phai cap nhat vi tri cham tron
     * VUA phai tiep tuc doan keo (continueStroke) - gop chung 1 hanh dong cho
     * don gian, tranh phai doi kieu tra ve nhieu hanh dong/khung.
     */
    data class HoldMove(val x: Float, val y: Float) : GestureAction()

    /**
     * Nha giu (SPEC muc 4.4): b,c khep lai sau khi da giu, HOẶC 1 trong 3 lop
     * an toan bat buoc (CLAUDE.md muc 4.5) buoc phai nha: mat tay qua
     * HOLD_LOST_MS, giu qua HOLD_MAX_MS. Khong mang toa do vi chi la thao tac
     * "nhac ngon tay len" tai vi tri hien tai, khong doi vi tri.
     */
    object HoldEnd : GestureAction()

    /**
     * M5 (SPEC muc 4.5, doi lai 2026-09-20 lan 8): nguoi dung bao vay
     * Trai/Phai (Back/Da nhiem cu) rat kho kich hoat on dinh (Back CHUA TUNG
     * thanh cong, Home chi 1 lan) trong luc giu tu the 4 ngon - CHI con vay
     * Len/Xuong (de kich hoat hon nhieu, da xac nhan qua Xuong=thanh thong
     * bao hoat dong tot). De van co 4 hanh dong, tach lam 2 TU THE rieng:
     * "4 ngon KHEP" (Back/Da nhiem) va "4 ngon TACH" (Home/Thanh thong bao,
     * tu the M5 cu) - xem GestureStateMachine.SystemPoseMode. GestureAction
     * mang thang loai hanh dong cuoi cung (khong con mang Direction/mode
     * rieng) de lop Android khong can biet gi ve tu the/huong, chi map
     * thang sang GLOBAL_ACTION_* tuong ung.
     */
    data class SystemAction(val type: SystemActionType) : GestureAction()

    enum class SystemActionType { BACK, RECENTS, HOME, NOTIFICATIONS }

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    /**
     * M6 (yeu cau nguoi dung 2026-09-22): xoe 5 ngon dung yen roi nam tay lai
     * -> "tat man hinh". Tin hieu TRUU TUONG - lop Service moi quyet dinh
     * dien dich thanh khoa man hinh THAT (GLOBAL_ACTION_LOCK_SCREEN, giong
     * bam nut nguon) hay chi PHU 1 LOP MAN DEN che kin (khong khoa/tat gi
     * that), tuy GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE. Ca 2
     * cach deu KHONG tat app/camera/service - xem GestureForegroundService.dispatch.
     */
    object ScreenOff : GestureAction()

    /**
     * M6 nguoc lai: nam tay >=SCREEN_OFF_REOPEN_HOLD_MS roi xoe 5 ngon ra sau
     * khi da ScreenOff (CHI phat khi GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE
     * = true, xem GestureStateMachine.updateScreenLock - luc do ScreenOff o
     * tren la phu man den, khong phai khoa that, nen bo lop den la du "mo lai").
     */
    object ScreenOn : GestureAction()
}
