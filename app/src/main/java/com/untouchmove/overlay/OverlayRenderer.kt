package com.untouchmove.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
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
        DisplayState.NONE -> Color.TRANSPARENT
    }

    private fun alphaFor(state: DisplayState) = when (state) {
        DisplayState.ARMING -> 150
        DisplayState.M1_VERTICAL -> 220
        DisplayState.M1_HORIZONTAL -> 220
        DisplayState.M2 -> 220
        DisplayState.M5_SPREAD -> 220
        DisplayState.M5_CLOSED -> 220
        DisplayState.NONE -> 0
    }

    private companion object {
        const val ICON_SIZE_DP = 24
        const val MARGIN_DP = 16
    }
}
