package com.untouchmove.gesture

/**
 * Toan bo hang so nguong cu chi (SPEC muc 6). Khong rai rac o noi khac.
 *
 * R_*, G_*, G_OPEN4, T_* da duoc do tren du lieu that (Phase 1, 140 lan ghi
 * tren RMX3370 / GT Neo 2, xem docs/DECISIONS.md ngay 2026-09-20). Cac hang so
 * con lai (toc do, thoi gian, EDGE_MARGIN) chua do duoc vi can chuyen dong that
 * (vay tay, giu/nha) chu khong phai tu the tinh - giu nguyen gia tri phong doan
 * cua SPEC, se do o Phase 2/3.
 */
object GestureThresholds {
    // Ngon dung/gap. Du lieu that: dung ~0.85-1.4 (p10=0.92), gap ~0.2-0.66 (p90=0.54).
    // Doan ban dau (R_UP=1.6, R_DOWN=1.1) sai hoan toan - ngon dung khong bao gio
    // vuot 1.6, se khien app khong nhan duoc tu the nao.
    const val R_UP = 0.90f
    const val R_DOWN = 0.65f

    // Khep/tach hai dau ngon (b,c). Du lieu that: khep p90=1.30, tach p10=1.88.
    const val G_CLOSE = 1.3f
    const val G_OPEN = 1.85f

    // Tu the vao M5 (4 ngon roi nhau). Du lieu that: M5 gmax p10=2.17.
    const val G_OPEN4 = 2.0f

    // Ngon cai xoe/khep. Du lieu that KHONG tach biet ro (M1 khep t~0.43-0.53,
    // M2 xoe het co t~0.62-0.75, van tinh chi so nguoi dung khong xoe them duoc
    // nua) - nguy co lan giua M1/M2 cao hon cac truc khac. Neu thuc te dung sai
    // nhieu, can ghi vao BACKLOG xem xet bo phan loai rieng cho ngon cai.
    // ponytail: T_OUT/T_IN chi dua tren 1 nguoi dung, chua co bien do an toan lon.
    const val T_OUT = 0.62f
    const val T_IN = 0.45f
    const val T_CLOSE_RATIO = 0.6f

    // --- Chua do duoc o Phase 1 (can chuyen dong that, xem Phase 2/3) ---
    const val ARM_HOLD_MS = 500L
    const val ARM_JITTER = 0.15f
    const val CLICK_MAX_MS = 300L
    const val CLICK_COOLDOWN_MS = 250L
    const val SWIPE_VEL_MIN = 1.0f // TODO(untouch): can do bang cu chi vay that o Phase 3
    const val SYS_VEL_MIN = SWIPE_VEL_MIN * 1.5f
    const val SWIPE_COOLDOWN_MS = 500L
    const val AXIS_RATIO = 1.8f
    const val CURSOR_GAIN = 2.0f
    const val LOST_HAND_MS = 300L
    const val HOLD_LOST_MS = 300L
    const val HOLD_MAX_MS = 15_000L
    const val EDGE_MARGIN = 0.05f
    const val CONF_MIN = 0.6f
}
