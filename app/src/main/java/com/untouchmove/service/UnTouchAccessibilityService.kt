package com.untouchmove.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * Chi dung de gui cu chi (dispatchGesture / performGlobalAction) va sau nay ve
 * overlay. KHONG doc noi dung man hinh, khong xu ly AccessibilityEvent
 * (CLAUDE.md muc 4.6).
 *
 * Phase 2 (ROADMAP): moi thu o day la BOM THU bang tay qua GestureTestActivity,
 * chua noi voi camera. Toa do/thoi gian la gia tri thu nghiem hop ly, se duoc
 * GestureStateMachine (Phase 3+) tinh chinh lai theo cu chi that.
 */
class UnTouchAccessibilityService : AccessibilityService() {

    enum class SwipeDirection { UP, DOWN, LEFT, RIGHT }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ActionDispatcher.attach(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // co tinh de trong: khong doc noi dung man hinh
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        ActionDispatcher.detach(this)
        super.onDestroy()
    }

    fun performSwipe(direction: SwipeDirection) {
        val metrics = resources.displayMetrics
        val cx = metrics.widthPixels / 2f
        val cy = metrics.heightPixels / 2f
        val d = minOf(metrics.widthPixels, metrics.heightPixels) * 0.35f
        val path = Path().apply {
            when (direction) {
                SwipeDirection.UP -> { moveTo(cx, cy + d / 2); lineTo(cx, cy - d / 2) }
                SwipeDirection.DOWN -> { moveTo(cx, cy - d / 2); lineTo(cx, cy + d / 2) }
                SwipeDirection.LEFT -> { moveTo(cx + d / 2, cy); lineTo(cx - d / 2, cy) }
                SwipeDirection.RIGHT -> { moveTo(cx - d / 2, cy); lineTo(cx + d / 2, cy) }
            }
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, SWIPE_DURATION_MS)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun performClick(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, CLICK_DURATION_MS)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    /**
     * Keo thu ngang qua giua man hinh bang cach noi nhieu doan continueStroke.
     * Muc dich (ROADMAP Phase 2): quan sat bang mat xem drag co giat khong,
     * chua phai logic hold/drag that (Phase 5).
     */
    fun performTestDrag() {
        val metrics = resources.displayMetrics
        val y = metrics.heightPixels * 0.5f
        val xStart = metrics.widthPixels * 0.2f
        val xEnd = metrics.widthPixels * 0.8f
        val segmentDx = (xEnd - xStart) / DRAG_STEPS

        fun runStep(stepIndex: Int, previous: GestureDescription.StrokeDescription?) {
            val fromX = xStart + segmentDx * stepIndex
            val toX = xStart + segmentDx * (stepIndex + 1)
            val path = Path().apply { moveTo(fromX, y); lineTo(toX, y) }
            val willContinue = stepIndex < DRAG_STEPS - 1
            val stroke = if (previous == null) {
                GestureDescription.StrokeDescription(path, 0, DRAG_STEP_DURATION_MS, willContinue)
            } else {
                previous.continueStroke(path, 0, DRAG_STEP_DURATION_MS, willContinue)
            }
            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (willContinue) runStep(stepIndex + 1, stroke)
                }
                // ponytail: chua xu ly onCancelled giua chung (vd he thong huy
                // gesture) - du de danh gia do muot cua continueStroke o Phase 2
            }
            dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), callback, null)
        }
        runStep(0, null)
    }

    private companion object {
        const val SWIPE_DURATION_MS = 250L
        const val CLICK_DURATION_MS = 50L
        const val DRAG_STEPS = 5
        const val DRAG_STEP_DURATION_MS = 150L
    }
}
