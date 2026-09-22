package com.untouchmove.service

import com.untouchmove.gesture.CursorPhase
import com.untouchmove.gesture.DisplayState
import java.lang.ref.WeakReference

/**
 * Cau noi nhe giua UI (hoac sau nay la GestureStateMachine) va
 * UnTouchAccessibilityService dang chay (ARCHITECTURE muc 5). Dung tham chieu
 * yeu vi service do he thong quan ly vong doi, khong phai app.
 */
object ActionDispatcher {
    private var serviceRef: WeakReference<UnTouchAccessibilityService>? = null

    val isConnected: Boolean
        get() = serviceRef?.get() != null

    fun attach(service: UnTouchAccessibilityService) {
        serviceRef = WeakReference(service)
    }

    fun detach(service: UnTouchAccessibilityService) {
        if (serviceRef?.get() === service) {
            serviceRef = null
        }
    }

    fun swipe(direction: UnTouchAccessibilityService.SwipeDirection) {
        serviceRef?.get()?.performSwipe(direction)
    }

    fun click(x: Float, y: Float) {
        serviceRef?.get()?.performClick(x, y)
    }

    fun resetCursor() {
        serviceRef?.get()?.resetCursor()
    }

    fun moveCursor(dx: Float, dy: Float) {
        serviceRef?.get()?.moveCursor(dx, dy)
    }

    fun gestureClick(x: Float, y: Float) {
        serviceRef?.get()?.performGestureClick(x, y)
    }

    fun holdStart(x: Float, y: Float) {
        serviceRef?.get()?.performHoldStart(x, y)
    }

    fun holdMove(x: Float, y: Float) {
        serviceRef?.get()?.performHoldMove(x, y)
    }

    fun holdEnd() {
        serviceRef?.get()?.performHoldEnd()
    }

    fun showCursorPhase(phase: CursorPhase) {
        serviceRef?.get()?.showCursorPhase(phase)
    }

    fun testDrag() {
        serviceRef?.get()?.performTestDrag()
    }

    fun globalAction(action: Int) {
        serviceRef?.get()?.performGlobalAction(action)
    }

    fun showBlackCurtain() {
        serviceRef?.get()?.showBlackCurtain()
    }

    fun hideBlackCurtain() {
        serviceRef?.get()?.hideBlackCurtain()
    }

    fun showStatus(state: DisplayState) {
        serviceRef?.get()?.showStatus(state)
    }
}
