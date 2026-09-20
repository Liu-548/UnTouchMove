package com.untouchmove.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Color
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.untouchmove.gesture.CursorPhase
import com.untouchmove.gesture.DisplayState
import com.untouchmove.gesture.GestureThresholds
import com.untouchmove.overlay.CursorView
import com.untouchmove.overlay.OverlayRenderer

/**
 * Chi dung de gui cu chi (dispatchGesture / performGlobalAction) va ve overlay
 * icon trang thai. KHONG doc noi dung man hinh, khong xu ly AccessibilityEvent
 * (CLAUDE.md muc 4.6).
 *
 * Toa do/thoi gian cua cac ham bom-thu (performSwipe co dinh, performTestDrag)
 * la gia tri thu nghiem tu Phase 2, dung rieng cho GestureTestActivity - swipe
 * that tu GestureStateMachine (Phase 3+) di qua duong khac (xem performSwipe
 * duoc goi lai boi GestureForegroundService).
 */
class UnTouchAccessibilityService : AccessibilityService() {

    enum class SwipeDirection { UP, DOWN, LEFT, RIGHT }

    private val overlayRenderer by lazy { OverlayRenderer(this) }
    private val cursorView by lazy { CursorView(this) }

    // Trang thai "giu/keo" (M4, SPEC muc 4.4) - noi lien tiep cac doan
    // continueStroke theo vi tri con tro moi khung. dragStroke != null nghia
    // la co 1 stroke CO THE noi tiep (willContinue=true lan gui gan nhat).
    // dragInFlight = dang cho callback onCompleted cua lan dispatchGesture
    // truoc - trong luc do, vi tri moi den chi GOP lai vao dragPendingTargetPx
    // (khong xep hang tung diem) de tranh keo bi tre/giat khi camera nhanh
    // hon toc do xu ly gesture cua he thong (SPEC ghi nhan day la rui ro chap
    // nhan duoc o bang r ro ro trong SPEC).
    private var dragStroke: GestureDescription.StrokeDescription? = null
    private var dragInFlight = false
    private var dragLastPx: Pair<Float, Float>? = null
    private var dragPendingTargetPx: Pair<Float, Float>? = null
    private var dragPendingEnd = false

    private val dragCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            dragInFlight = false
            val pendingEnd = dragPendingEnd
            val pendingTarget = dragPendingTargetPx
            dragPendingEnd = false
            dragPendingTargetPx = null
            when {
                pendingEnd -> finishDragSegment(pendingTarget ?: dragLastPx ?: return)
                pendingTarget != null -> sendDragSegment(pendingTarget, willContinue = true)
                dragStroke == null -> resetDragState() // vua hoan tat doan KET THUC, don dep
            }
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            // ponytail: he thong huy giua chung (hiem gap) - coi nhu da nha,
            // tranh ket dragInFlight=true mai khong con callback nao den nua.
            resetDragState()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ActionDispatcher.attach(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // co tinh de trong: khong doc noi dung man hinh
    }

    override fun onInterrupt() {}

    /**
     * An toan bat buoc thu 3/3 (CLAUDE.md muc 4.5, SPEC muc 4.4): service bi
     * tat vi bat cu ly do gi trong luc dang giu/keo -> phai nha ngay, khong
     * de ngon tay "ao" ket lai tren man hinh.
     */
    override fun onDestroy() {
        cancelActiveDrag()
        overlayRenderer.hide()
        cursorView.hide()
        ActionDispatcher.detach(this)
        super.onDestroy()
    }

    fun showStatus(state: DisplayState) {
        overlayRenderer.show(state)
        if (state != DisplayState.M2) cursorView.hide()
    }

    /** Dat con tro ve giua man hinh (SPEC muc 4.2), goi khi vua vao M2. */
    fun resetCursor() {
        val (pxX, pxY) = toScreenPx(0f, 0f)
        cursorView.show(pxX.toInt(), pxY.toInt())
        cursorView.setColor(colorFor(CursorPhase.MOVING))
    }

    /**
     * Doi mau cham tron theo GestureStateMachine.CursorPhase (yeu cau nguoi
     * dung 2026-09-20: can phan biet truc quan cac trang thai de tu test M4).
     * Chi anh huong hien thi, khong lien quan gi den logic giu/keo that o
     * duoi (dragStroke, ...).
     */
    fun showCursorPhase(phase: CursorPhase) {
        cursorView.setColor(colorFor(phase))
    }

    private fun colorFor(phase: CursorPhase): Int = when (phase) {
        CursorPhase.NONE, CursorPhase.MOVING -> Color.CYAN
        CursorPhase.SEPARATED -> Color.YELLOW
        CursorPhase.HOLDING -> Color.RED
        CursorPhase.BLOCKED -> Color.GRAY
    }

    /**
     * x,y: vi tri TUYET DOI (lech so voi tam man hinh) tu GestureAction.CursorMove,
     * don vi truu tuong (CURSOR_GAIN = dp/don vi) - khong con la delta, xem
     * GestureAction.CursorMove.
     */
    fun moveCursor(x: Float, y: Float) {
        val (pxX, pxY) = toScreenPx(x, y)
        cursorView.show(pxX.toInt(), pxY.toInt())
    }

    /**
     * x,y: cung don vi/cong thuc quy doi voi moveCursor (SPEC muc 4.3) - dam
     * bao toa do click luon khop voi vi tri cham tron dang hien thi luc bat
     * dau tach ngon.
     */
    fun performGestureClick(x: Float, y: Float) {
        val (pxX, pxY) = toScreenPx(x, y)
        performClick(pxX, pxY)
    }

    /** Bat dau giu/keo (SPEC muc 4.4): "nhan xuong" dung vi tri con tro dang hien. */
    fun performHoldStart(x: Float, y: Float) {
        val px = toScreenPx(x, y)
        cursorView.show(px.first.toInt(), px.second.toInt())
        dragLastPx = px
        dragPendingTargetPx = null
        dragPendingEnd = false
        val path = Path().apply { moveTo(px.first, px.second) }
        val stroke = GestureDescription.StrokeDescription(path, 0, HOLD_STEP_DURATION_MS, true)
        dragStroke = stroke
        dragInFlight = true
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), dragCallback, null)
    }

    /** Tiep tuc keo toi vi tri moi trong luc dang giu (SPEC muc 4.4 "vua hold vua move"). */
    fun performHoldMove(x: Float, y: Float) {
        val px = toScreenPx(x, y)
        cursorView.show(px.first.toInt(), px.second.toInt())
        if (dragInFlight) {
            dragPendingTargetPx = px // gop lai, khong xep hang - xem ghi chu o field
            return
        }
        sendDragSegment(px, willContinue = true)
    }

    /** Nha giu (SPEC muc 4.4 "nha: khep b va c", hoac 1 trong 3 lop an toan bat buoc). */
    fun performHoldEnd() {
        val lastPx = dragLastPx ?: return
        if (dragInFlight) {
            dragPendingEnd = true
            return
        }
        finishDragSegment(lastPx)
    }

    private fun finishDragSegment(targetPx: Pair<Float, Float>) {
        sendDragSegment(targetPx, willContinue = false)
    }

    private fun sendDragSegment(targetPx: Pair<Float, Float>, willContinue: Boolean) {
        val from = dragLastPx ?: targetPx
        val path = Path().apply { moveTo(from.first, from.second); lineTo(targetPx.first, targetPx.second) }
        val previous = dragStroke
        val stroke = if (previous == null) {
            GestureDescription.StrokeDescription(path, 0, HOLD_STEP_DURATION_MS, willContinue)
        } else {
            previous.continueStroke(path, 0, HOLD_STEP_DURATION_MS, willContinue)
        }
        dragStroke = if (willContinue) stroke else null
        dragLastPx = targetPx
        dragInFlight = true
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), dragCallback, null)
    }

    private fun resetDragState() {
        dragStroke = null
        dragInFlight = false
        dragLastPx = null
        dragPendingTargetPx = null
        dragPendingEnd = false
    }

    /**
     * An toan bat buoc thu 3/3 (xem onDestroy): neu dang co phien giu/keo do
     * dang, nha ngay tai vi tri cuoi cung - chap nhan co the chong voi doan
     * dang cho dispatch xong (Android se tu huy doan cu khi service ngat).
     */
    private fun cancelActiveDrag() {
        val lastPx = dragLastPx ?: return
        val path = Path().apply { moveTo(lastPx.first, lastPx.second) }
        val stroke = GestureDescription.StrokeDescription(path, 0, CLICK_DURATION_MS, false)
        runCatching { dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null) }
        resetDragState()
    }

    /** Quy doi toa do truu tuong (lech tam man hinh) sang pixel, co kep bien. */
    /**
     * KHONG dung GestureThresholds.EDGE_MARGIN o day - hang so do la nguong
     * "tay cham le KHUNG HINH CAMERA" (SPEC muc 6, dung de bat tay sap ra
     * khoi vung nhin thay cua camera), khong phai nguong vi tri con tro tren
     * MAN HINH. Dung nham truoc day khien con tro khong bao gio cham duoc
     * 5% ngoai cung moi canh man hinh (nguoi dung bao 2026-09-20: "khong the
     * thuc su cham toi moi vi tri, bi chan boi ca 4 huong") - sua bang cach
     * kep dung vao 0..kich thuoc man hinh that.
     */
    private fun toScreenPx(x: Float, y: Float): Pair<Float, Float> {
        val metrics = resources.displayMetrics
        val density = metrics.density
        val pxX = (metrics.widthPixels / 2f + x * density).coerceIn(0f, metrics.widthPixels.toFloat())
        val pxY = (metrics.heightPixels / 2f + y * density).coerceIn(0f, metrics.heightPixels.toFloat())
        return pxX to pxY
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

        // Thoi luong moi doan continueStroke khi GIU/KEO that (M4). Ngan hon
        // DRAG_STEP_DURATION_MS (keo thu Phase 2, theo 1 duong da dinh san co
        // the doi lau hon) vi day la keo THEO THOI GIAN THUC tung khung camera
        // - can ngan de it tre, nhung khong qua ngan (he thong co the tu choi
        // stroke qua nhanh). TODO(untouch): 80ms la so doan, chua do that tren
        // may xem co giat/tre hay khong.
        const val HOLD_STEP_DURATION_MS = 80L
    }
}
