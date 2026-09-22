package com.untouchmove.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.untouchmove.gesture.DisplayState

/**
 * Icon trang thai nho o goc man hinh (SPEC muc 5.2), ve bang
 * TYPE_ACCESSIBILITY_OVERLAY tu AccessibilityService (khong dung
 * SYSTEM_ALERT_WINDOW, CLAUDE.md muc 4.2). Khong nhan cham
 * (FLAG_NOT_TOUCHABLE) de khong chan thao tac that cua nguoi dung.
 *
 * ponytail: chua co animation "mo dan / vong tron chay 500ms" luc ARMING nhu
 * SPEC ta - chi doi mau/do trong suot cho don gian. Nang cap sau neu can.
 */
class OverlayRenderer(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val iconSizePx = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
    private val marginPx = (MARGIN_DP * context.resources.displayMetrics.density).toInt()
    private var iconView: View? = null
    private var curtainView: View? = null

    fun show(state: DisplayState) {
        if (state == DisplayState.NONE) {
            hide()
            return
        }
        val view = iconView ?: View(context).also {
            iconView = it
            windowManager.addView(it, layoutParams())
        }
        view.background = circleDrawable(colorFor(state), alphaFor(state))
    }

    fun hide() {
        val view = iconView ?: return
        windowManager.removeView(view)
        iconView = null
    }

    /**
     * M6 "phu man den" (yeu cau nguoi dung 2026-09-22, sua lai sau khi lam
     * nham thanh khoa man hinh that): phu KIN toan bo man hinh bang 1 lop den
     * dac - KHONG phai tat/khoa man hinh that (khong goi GLOBAL_ACTION_LOCK_SCREEN,
     * khong dung camera). Van dung TYPE_ACCESSIBILITY_OVERLAY (CLAUDE.md muc
     * 4.2, khong SYSTEM_ALERT_WINDOW). CO NHAN CHAM (khong dat
     * FLAG_NOT_TOUCHABLE) de chan thao tac len ung dung ben duoi trong luc
     * "tat", nhung NHAN GIU (long-press) ngay tren lop den se tu go lop den
     * ra luon - yeu cau nguoi dung 2026-09-22 (2): "khong yeu cau khoa cham
     * man hinh nhu vay, nhan giu van phai mo ra nhu thuong" - truoc do nhan
     * giu chi bi nuot mat, khong co phan hoi gi, coi nhu "khoa chet". Day la
     * duong thoat DU PHONG bang tay that, doc lap voi cu chi nam-roi-xoe.
     */
    fun showBlackCurtain() {
        hide() // an luon icon trang thai nho, khong can thiet khi da phu kin
        if (curtainView != null) return
        val detector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    hideBlackCurtain()
                }
            },
        )
        val view = View(context).apply {
            setBackgroundColor(Color.BLACK)
            setOnTouchListener { _, event -> detector.onTouchEvent(event); true }
        }
        curtainView = view
        windowManager.addView(view, curtainLayoutParams())
    }

    fun hideBlackCurtain() {
        val view = curtainView ?: return
        windowManager.removeView(view)
        curtainView = null
    }

    /**
     * FLAG_LAYOUT_IN_SCREEN + FLAG_LAYOUT_NO_LIMITS: bat buoc phai co CA HAI
     * thi MATCH_PARENT moi thuc su tran ra ca vung thanh trang thai/thanh
     * thong bao va thanh dieu huong (yeu cau nguoi dung 2026-09-22 "co phu
     * duoc ca thanh thong bao khong") - thieu FLAG_LAYOUT_NO_LIMITS thi cua
     * so bi gioi han trong vung noi dung, chua ca 2 thanh he thong do van lo
     * ra ngoai lop den. layoutInDisplayCutoutMode: tran ca vao vung tai
     * tho/notch tren may co (API 28+).
     */
    private fun curtainLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.OPAQUE,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }

    private fun layoutParams() = WindowManager.LayoutParams(
        iconSizePx,
        iconSizePx,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.END
        x = marginPx
        y = marginPx
    }

    private fun circleDrawable(color: Int, alphaValue: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        alpha = alphaValue
    }

    // M1_VERTICAL/M1_HORIZONTAL va M5_CLOSED/M5_SPREAD tach mau rieng (yeu
    // cau nguoi dung 2026-09-20, lan 11+12: "mau khi dua 3 ngon dang trung
    // voi 2 ngon" / "sua luon ca 2 dang 4 ngon") - truoc day moi cap chung 1
    // DisplayState nen chung 1 mau, khong phan biet duoc dang o tu the nao
    // qua icon.
    private fun colorFor(state: DisplayState) = when (state) {
        DisplayState.ARMING -> Color.GRAY
        DisplayState.M1_VERTICAL -> Color.GREEN
        DisplayState.M1_HORIZONTAL -> Color.YELLOW
        DisplayState.M2 -> Color.CYAN
        DisplayState.M5_SPREAD -> Color.MAGENTA
        DisplayState.M5_CLOSED -> Color.BLUE
        // Mau rieng cho M6 "san sang tat man hinh" (yeu cau nguoi dung
        // 2026-09-22: "can focus 5 ngon tay 1 mau rieng cho thong bao, vd
        // cam") - khong trung mau nao dang dung o tren.
        DisplayState.SCREEN_LOCK_ARMED -> Color.rgb(255, 140, 0)
        DisplayState.NONE -> Color.TRANSPARENT
    }

    private fun alphaFor(state: DisplayState) = when (state) {
        DisplayState.ARMING -> 150
        DisplayState.M1_VERTICAL -> 220
        DisplayState.M1_HORIZONTAL -> 220
        DisplayState.M2 -> 220
        DisplayState.M5_SPREAD -> 220
        DisplayState.M5_CLOSED -> 220
        DisplayState.SCREEN_LOCK_ARMED -> 220
        DisplayState.NONE -> 0
    }

    private companion object {
        const val ICON_SIZE_DP = 24
        const val MARGIN_DP = 16
    }
}
