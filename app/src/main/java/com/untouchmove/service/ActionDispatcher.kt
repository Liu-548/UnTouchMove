package com.untouchmove.service

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

    fun testDrag() {
        serviceRef?.get()?.performTestDrag()
    }

    fun globalAction(action: Int) {
        serviceRef?.get()?.performGlobalAction(action)
    }
}
