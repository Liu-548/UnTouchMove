package com.untouchmove.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Cham tron con tro cho M2 (SPEC muc 4.2), ve bang TYPE_ACCESSIBILITY_OVERLAY
 * tu AccessibilityService (CLAUDE.md muc 4.2, khong dung SYSTEM_ALERT_WINDOW).
 * Khac voi OverlayRenderer (icon dung yen 1 goc man hinh): vi tri cham tron
 * thay doi lien tuc theo CursorMove, nen dung updateViewLayout thay vi tao
 * lai view moi lan.
 */
class CursorView(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val sizePx = (SIZE_DP * context.resources.displayMetrics.density).toInt()
    private var view: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var currentColor = Color.CYAN

    /** xPx, yPx la tam cham tron (theo goc tren-trai man hinh). */
    fun show(xPx: Int, yPx: Int) {
        val left = xPx - sizePx / 2
        val top = yPx - sizePx / 2
        val layoutParams = params
        if (layoutParams == null) {
            val newParams = layoutParams(left, top)
            params = newParams
            view = View(context).also {
                it.background = circleDrawable()
                windowManager.addView(it, newParams)
            }
            return
        }
        layoutParams.x = left
        layoutParams.y = top
        windowManager.updateViewLayout(view, layoutParams)
    }

    /**
     * Doi mau cham tron theo trang thai/hanh dong (yeu cau nguoi dung
     * 2026-09-20: can phan biet truc quan cac trang thai de tu test M4, xem
     * GestureStateMachine.CursorPhase). Khong tao lai view, chi doi mau
     * GradientDrawable dang co.
     */
    fun setColor(color: Int) {
        if (currentColor == color) return
        currentColor = color
        (view?.background as? GradientDrawable)?.setColor(color)
    }

    fun hide() {
        val currentView = view ?: return
        windowManager.removeView(currentView)
        view = null
        params = null
    }

    private fun layoutParams(leftPx: Int, topPx: Int) = WindowManager.LayoutParams(
        sizePx,
        sizePx,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = leftPx
        y = topPx
    }

    private fun circleDrawable() = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(currentColor)
        alpha = 180
    }

    private companion object {
        const val SIZE_DP = 20
    }
}
