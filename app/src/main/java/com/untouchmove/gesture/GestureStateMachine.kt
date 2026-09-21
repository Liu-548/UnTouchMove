package com.untouchmove.gesture

import kotlin.math.abs

/**
 * Trang thai hien thi don gian cho overlay icon (SPEC muc 5.2). Tach rieng
 * mau cho tung TU THE con (khong chi tung CHE DO) de nguoi dung phan biet
 * duoc qua icon dang o dung tu the nao (yeu cau nguoi dung 2026-09-20):
 * - M1_VERTICAL (2 ngon, Len/Xuong) vs M1_HORIZONTAL (3 ngon, Trai/Phai),
 *   lan 11: "mau khi dua 3 ngon dang trung voi 2 ngon".
 * - M5_CLOSED (4 ngon khep) vs M5_SPREAD (4 ngon tach), lan 12: "sua luon
 *   ca 2 dang 4 ngon" - cung ly do, truoc day ca 2 deu chung DisplayState.M5.
 */
enum class DisplayState { NONE, ARMING, M1_VERTICAL, M1_HORIZONTAL, M2, M5_CLOSED, M5_SPREAD }

/**
 * Trang thai/hanh dong hien tai cua con tro trong M2 - CHI dung DE HIEN THI
 * (doi mau cham tron, xem UnTouchAccessibilityService.showCursorPhase),
 * khong anh huong logic. Them theo yeu cau nguoi dung 2026-09-20: "kho dieu
 * khien khoang cach chon de keo, nhieu thu toi khong thuc su ro" - can phan
 * biet truc quan cac trang thai de nguoi dung tu test/bao lai chinh xac hon.
 * Doc qua GestureStateMachine.cursorPhase BAT KY LUC NAO sau onFrame(),
 * KHONG phu thuoc GestureAction tra ve khung do (vd dang trong cua so "vua
 * tach, chua ro click hay giu" thi onFrame co the tra ve null nhung phase
 * van la SEPARATED).
 */
enum class CursorPhase {
    NONE,
    /** Dang o M2, di chuyen binh thuong (b,c chua tach). */
    MOVING,
    /** b,c vua tach, dang trong cua so cho xem la click hay chuyen thanh giu. */
    SEPARATED,
    /** Da qua CLICK_MAX_MS, dang giu/keo that su (SPEC muc 4.4). */
    HOLDING,
    /** Vua tu nha do HOLD_MAX_MS, dang khoa cho toi khi khep tay lai that su. */
    BLOCKED,
}

private data class TimedPoint(val timeMs: Long, val p: Point3D)

/**
 * Tu the vao quyet dinh san truc duoc phep vuot (yeu cau nguoi dung
 * 2026-09-20): 2 ngon (b,c) = chi Len/Xuong; 3 ngon (b,c,d) = chi Trai/Phai.
 * Thay vi doan huong qua van toc/ty le truc (de lan), tu the tay da loai tru
 * san huong khong thuoc ve.
 */
private enum class SwipeAxisMode { VERTICAL, HORIZONTAL }

/**
 * M5 (SPEC muc 4.5, doi lai 2026-09-20 lan 8): 2 tu the RIENG cho 2 cap hanh
 * dong, chi con dung Len/Xuong de kich hoat (xem GestureAction.SystemAction).
 * CLOSED (4 ngon khep lai voi nhau) -> Back/Da nhiem; SPREAD (4 ngon tach ra,
 * tu the M5 cu) -> Home/Thanh thong bao. Huy: CLOSED thoat bang tach ra,
 * SPREAD thoat bang khep lai (nguoc voi tu the vao cua chinh no).
 */
private enum class SystemPoseMode { CLOSED, SPREAD }

/** Dich vao che do nao tu tu the ban dau (dung trong Arming, xem entryTargetOf). */
private sealed class ArmTarget {
    data class Swipe(val axis: SwipeAxisMode) : ArmTarget()
    object Cursor : ArmTarget()
    data class System(val mode: SystemPoseMode) : ArmTarget()
}

private sealed class InternalState {
    object Idle : InternalState()
    data class Arming(val startTimeMs: Long, val anchorP: Point3D, val target: ArmTarget) : InternalState()

    // history: cac diem gan day trong GestureThresholds.VELOCITY_WINDOW_MS, dung
    // de tinh van toc theo cua so thay vi tung khung don le (xem detectSwipe).
    // awaitingRest: sau 1 cu vuot, bat buoc tay phai VE TRANG THAI DUNG YEN
    // (toc do < SWIPE_REST_VEL_MAX) truoc khi cho vuot tiep - khong chi doi
    // het SWIPE_COOLDOWN_MS - de tranh 1 lan vay dai/tay chua dung han lai
    // kich hoat lien tiep nhieu cu vuot (yeu cau nguoi dung 2026-09-20).
    data class SwipeActive(
        val cooldownUntilMs: Long,
        val history: List<TimedPoint>,
        val mode: SwipeAxisMode,
        val awaitingRest: Boolean = false,
    ) : InternalState()

    /**
     * M2/M3 (SPEC muc 4.2, 4.3).
     * filterX/filterY: loc One Euro cho vi tri P, tinh delta giua 2 khung da
     * loc (khong loc truc tiep delta) - xem detectCursor.
     * cumulativeX/Y: TONG delta (don vi truu tuong, da nhan CURSOR_GAIN) tich
     * luy tu luc vao M2 - dung de chup toa do click dung luc bat dau tach,
     * KHONG dung de ve con tro (lop Android tu tich luy rieng tu CursorMove).
     * separatedAtMs: null = dang khep; khac null = thoi diem bat dau tach b,c.
     * separatedCursorX/Y: gia tri cumulativeX/Y CHUP luc bat dau tach, dung
     * lam toa do click (SPEC: "toa do click lay tai thoi diem bat dau tach").
     *
     * Yeu cau nguoi dung 2026-09-20: SAU KHI VAO M2 chi con quan tam b,c (de
     * click/hold), KHONG con theo doi ngon cai (a) nua - bo hoan toan dieu
     * kien huy theo t0 (do goc xoe ngon cai qua nhieu nhieu tren may that,
     * xem Phase 1 DECISIONS.md ve T_OUT/T_IN khong tach biet ro). Vi vay
     * KHONG con field t0 trong state nay.
     *
     * sessionS: kich thuoc ban tay (S) chup 1 LAN LUC VAO M2, dung xuyen suot
     * phien - KHONG tinh lai S moi khung nhu cac che do khac. Ly do (phat
     * hien tu test tren may that 2026-09-20): uoc luong world-landmark cua
     * MediaPipe cang kem on dinh (dac biet o cu ly gan camera) thi S do moi
     * khung cang nhieu, ma moi chuyen dong con tro deu chia cho S nen nhieu
     * do lan het vao ca chuyen dong - gay giat va doi hoi CURSOR_GAIN cao bat
     * thuong (nguoi dung phai dung toi 5000-6000 moi dung duoc).
     *
     * refDirX: huong ngon tro theo truc X (KHONG dao dau, khac P - xem
     * detectCursor) chup 1 LAN LUC VAO M2, lam moc "chi thang vao giua man
     * hinh" cho truc X cua che do CURSOR_POINTING_MODE. Truc Y cua che do nay
     * KHONG dung huong ngon tro nua (xem detectCursor - dung trang thai roi
     * rac b,c thay the, yeu cau nguoi dung 2026-09-20 lan 3), nen khong can
     * refDirY.
     *
     * pendingVerticalStep/pendingVerticalStepSinceMs/confirmedVerticalStep:
     * chong nhan nham lenh LEN luc dang chuyen giua dung yen/xuong (yeu cau
     * nguoi dung 2026-09-20, lan 4 va lan 5) - xem giai thich chi tiet o
     * GestureThresholds.CURSOR_POINTING_UP_CONFIRM_MS. Tom tat: "deu dung"/
     * "deu ha" ap dung ngay; rieng "LEN" (b dung, c ha) phai giu ON DINH lien
     * tuc du lau (CURSOR_POINTING_UP_CONFIRM_MS) moi duoc cong nhan, vi day
     * chinh la trang thai giao nhau tam thoi khi 2 Voter doc lap cua b,c
     * chua vote xong dong bo.
     *
     * holding: da phat HoldStart, dang trong 1 phien giu/keo (SPEC muc 4.4).
     * holdBlockedUntilClose: 1 trong 3 lop an toan bat buoc (CLAUDE.md muc
     * 4.5, HOLD_MAX_MS) da tu nha trong khi b,c VAN dang tach - phai khoa
     * khong cho tu dong vao lai phien giu moi cho toi khi nguoi dung THAT SU
     * khep tay lai it nhat 1 lan (khong chi doi het HOLD_MAX_MS la lai giu
     * tiep duoc ngay).
     */
    data class CursorActive(
        val filterX: OneEuroFilter,
        val filterY: OneEuroFilter,
        val lastFilteredX: Float,
        val lastFilteredY: Float,
        val cumulativeX: Float,
        val cumulativeY: Float,
        val sessionS: Float,
        val refDirX: Float,
        val pendingVerticalStep: Float,
        val pendingVerticalStepSinceMs: Long,
        val confirmedVerticalStep: Float,
        val separatedAtMs: Long?,
        val separatedCursorX: Float,
        val separatedCursorY: Float,
        val clickCooldownUntilMs: Long,
        val holding: Boolean,
        val holdBlockedUntilClose: Boolean,
    ) : InternalState()

    /**
     * M5 (SPEC muc 4.5). mode: CLOSED hay SPREAD (xem SystemPoseMode) - quyet
     * dinh dieu kien huy VA cap hanh dong Len/Xuong nao dang dung. fired: da
     * thuc hien DUNG 1 hanh dong trong phien nay - tu choi moi cu vay them
     * (SPEC: "chi cho phep mot hanh dong moi lan vao che do - sau khi thuc
     * hien, phai huy roi vao lai") cho toi khi huy (xem detectSystem).
     */
    data class SystemActive(
        val mode: SystemPoseMode,
        val history: List<TimedPoint>,
        val fired: Boolean = false,
    ) : InternalState()
}

/**
 * May trang thai cu chi, thuan Kotlin, khong phu thuoc Android (ARCHITECTURE
 * muc 2). Nhan vao tung HandFrame, tra ra GestureAction? neu co cu chi can
 * bom ra ngoai. Co trang thai (ARMING timer, cooldown, vi tri khung truoc) -
 * moi tay dang theo doi dung 1 instance rieng.
 *
 * Da co M1-M5 (Phase 3-6).
 */
class GestureStateMachine {
    private val poseClassifier = PoseClassifier()
    private var state: InternalState = InternalState.Idle

    val displayState: DisplayState
        get() = when (val s = state) {
            is InternalState.Idle -> DisplayState.NONE
            is InternalState.Arming -> DisplayState.ARMING
            is InternalState.SwipeActive -> when (s.mode) {
                SwipeAxisMode.VERTICAL -> DisplayState.M1_VERTICAL
                SwipeAxisMode.HORIZONTAL -> DisplayState.M1_HORIZONTAL
            }
            is InternalState.CursorActive -> DisplayState.M2
            is InternalState.SystemActive -> when (s.mode) {
                SystemPoseMode.CLOSED -> DisplayState.M5_CLOSED
                SystemPoseMode.SPREAD -> DisplayState.M5_SPREAD
            }
        }

    val cursorPhase: CursorPhase
        get() = when (val s = state) {
            is InternalState.CursorActive -> when {
                s.holdBlockedUntilClose -> CursorPhase.BLOCKED
                s.holding -> CursorPhase.HOLDING
                s.separatedAtMs != null -> CursorPhase.SEPARATED
                else -> CursorPhase.MOVING
            }
            else -> CursorPhase.NONE
        }

    /**
     * Goi khi mat tay qua LOST_HAND_MS (do o lop goi, khong phai o day).
     *
     * M2 la NGOAI LE (yeu cau nguoi dung 2026-09-20): mat tay KHONG huy M2 -
     * con tro dung yen tai vi tri cuoi cung con bat duoc tay (khong xoa
     * history/filter), roi tiep tuc di chuyen tiep tu do khi bat lai duoc
     * tay. M2 gio CHI thoat duoc bang xoe ca 5 ngon (xem onFrame).
     *
     * An toan bat buoc thu 1/3 (CLAUDE.md muc 4.5, SPEC muc 4.4): neu dang
     * giu (M4), mat tay qua LOST_HAND_MS phai NHA NGAY - LOST_HAND_MS va
     * HOLD_LOST_MS deu = 300ms (GestureThresholds) nen dung luon thoi diem
     * ham nay duoc goi (da qua LOST_HAND_MS o lop goi) lam moc nha, khong can
     * dem thoi gian rieng. Tra ve HoldEnd de lop Android that su "nhac ngon
     * tay len" (khong chi la doi trang thai noi bo).
     */
    fun onHandLost(): GestureAction? {
        val s = state
        if (s is InternalState.CursorActive) {
            if (s.holding) {
                state = s.copy(holding = false, separatedAtMs = null, holdBlockedUntilClose = false)
                return GestureAction.HoldEnd
            }
            return null
        }
        state = InternalState.Idle
        return null
    }

    fun onFrame(frame: HandFrame): GestureAction? {
        val features = runCatching { FeatureExtractor.extract(frame.worldLandmarks) }.getOrNull()
            ?: return null
        val pose = poseClassifier.classify(features)
        val now = frame.timestampMs

        if (pose.allFiveUp) {
            // SPEC muc 4.0: tay ranh, khong kich hoat gi bat ke dang o dau.
            // Voi M2 (yeu cau nguoi dung 2026-09-20): day la CACH DUY NHAT de
            // thoat - mat tay khong con huy M2 nua (xem onHandLost).
            state = InternalState.Idle
            return null
        }

        return when (val s = state) {
            is InternalState.Idle -> {
                val target = entryTargetOf(pose)
                if (target != null) {
                    state = InternalState.Arming(startTimeMs = now, anchorP = features.p, target = target)
                }
                null
            }

            is InternalState.Arming -> {
                if (entryTargetOf(pose) != s.target) {
                    state = InternalState.Idle
                    return null
                }
                val displacement = features.p.distanceTo(s.anchorP) / features.s
                if (displacement > GestureThresholds.ARM_JITTER) {
                    // SPEC muc 5.1: lech qua nguong jitter -> ve IDLE
                    state = InternalState.Idle
                    return null
                }
                if (now - s.startTimeMs >= GestureThresholds.ARM_HOLD_MS) {
                    state = when (val target = s.target) {
                        is ArmTarget.Swipe -> InternalState.SwipeActive(
                            cooldownUntilMs = 0L,
                            history = listOf(TimedPoint(now, features.p)),
                            mode = target.axis,
                        )
                        ArmTarget.Cursor -> InternalState.CursorActive(
                            filterX = OneEuroFilter(),
                            filterY = OneEuroFilter(),
                            // Dao dau: xac nhan tren may that 2026-09-20 che
                            // do "theo tay" (P la VI TRI) can dao dau giong
                            // M1, khac voi "theo huong ngon tro" (huong,
                            // KHONG dao dau) - 2 dai luong vat ly khac nhau,
                            // khong dung chung 1 quy uoc dau duoc.
                            lastFilteredX = -features.p.x / features.s,
                            lastFilteredY = -features.p.y / features.s,
                            cumulativeX = 0f,
                            cumulativeY = 0f,
                            sessionS = features.s,
                            refDirX = features.pointDirX,
                            pendingVerticalStep = 0f,
                            pendingVerticalStepSinceMs = now,
                            confirmedVerticalStep = 0f,
                            separatedAtMs = null,
                            separatedCursorX = 0f,
                            separatedCursorY = 0f,
                            clickCooldownUntilMs = 0L,
                            holding = false,
                            holdBlockedUntilClose = false,
                        )
                        is ArmTarget.System -> InternalState.SystemActive(mode = target.mode, history = listOf(TimedPoint(now, features.p)))
                    }
                }
                null
            }

            is InternalState.SwipeActive -> {
                if (entryTargetOf(pose) != ArmTarget.Swipe(s.mode)) {
                    // SPEC muc 4.1: roi khoi dung tu the cua che do dang o -> huy M1
                    state = InternalState.Idle
                    return null
                }
                detectSwipe(s, features, now)
            }

            is InternalState.CursorActive -> detectCursor(s, pose, features, now)

            is InternalState.SystemActive -> detectSystem(s, pose, features, now)
        }
    }

    private fun detectSwipe(s: InternalState.SwipeActive, features: Features, now: Long): GestureAction? {
        val current = TimedPoint(now, features.p)
        if (now < s.cooldownUntilMs) {
            // Reset cua so ngay khi het cooldown, tranh tinh van toc lan sang
            // doan tay dang di chuyen ve vi tri cu giua hai cu vuot.
            state = s.copy(history = listOf(current))
            return null
        }

        // Van toc tinh theo CUA SO (VELOCITY_WINDOW_MS gan nhat), khong phai
        // tung khung don le: frame rate MediaPipe dao dong manh (~23-97ms/khung,
        // do thuc te 2026-09-20), tinh theo 1 khung lien truoc de nhieu dtMs
        // lam van toc luc bi thoi phong gia (dtMs nho tinh co), luc bi pha loang
        // gia (dtMs lon tinh co) - gay ket qua that thuong da bao cao tren may that.
        val history = (s.history + current).filter { now - it.timeMs <= GestureThresholds.VELOCITY_WINDOW_MS }

        val oldest = history.first()
        val dtMs = now - oldest.timeMs
        if (dtMs < GestureThresholds.VELOCITY_WINDOW_MS / 2) {
            state = s.copy(history = history)
            return null // chua du du lieu trong cua so
        }

        val dx = (features.p.x - oldest.p.x) / features.s
        val dy = (features.p.y - oldest.p.y) / features.s
        val velX = dx / dtMs * 1000f
        val velY = dy / dtMs * 1000f

        val absX = abs(velX)
        val absY = abs(velY)
        val speed = maxOf(absX, absY)

        if (s.awaitingRest) {
            // Tay chua dung yen lai sau cu vuot truoc - bo qua, cho den khi
            // toc do xuong duoi nguong nghi (SPEC muc 4.1 "chong vuot nguoc").
            val stillWaiting = speed > GestureThresholds.SWIPE_REST_VEL_MAX
            state = s.copy(history = history, awaitingRest = stillWaiting)
            return null
        }
        state = s.copy(history = history)

        // Tu the vao (s.mode) da loai tru san truc: VERTICAL chi xet Len/Xuong,
        // HORIZONTAL chi xet Trai/Phai - khong con doan huong qua ty le truc
        // nua (yeu cau nguoi dung 2026-09-20, xem SwipeAxisMode).
        val dominantIsX = absX > absY
        if (s.mode == SwipeAxisMode.VERTICAL && dominantIsX) return null
        if (s.mode == SwipeAxisMode.HORIZONTAL && !dominantIsX) return null

        val velMinForDirection = when {
            s.mode == SwipeAxisMode.HORIZONTAL -> GestureThresholds.SWIPE_VEL_MIN_LEFT_RIGHT
            velY > 0 -> GestureThresholds.SWIPE_VEL_MIN_UP
            else -> GestureThresholds.SWIPE_VEL_MIN_DOWN
        }
        if (speed < velMinForDirection) return null

        val ratio = if (dominantIsX) absX / absY.coerceAtLeast(MIN_DIVISOR) else absY / absX.coerceAtLeast(MIN_DIVISOR)
        if (ratio < GestureThresholds.AXIS_RATIO) return null // chuyen dong cheo, bo qua (SPEC muc 4.1)

        // Da xac nhan tren may that (2026-09-20): ca X va Y cua world landmark
        // nguoc voi truc man hinh - velX > 0 la tay dua sang TRAI, velY > 0 la
        // tay dua LEN.
        val direction = when {
            dominantIsX && velX > 0 -> GestureAction.Direction.LEFT
            dominantIsX -> GestureAction.Direction.RIGHT
            velY > 0 -> GestureAction.Direction.UP
            else -> GestureAction.Direction.DOWN
        }

        state = (state as InternalState.SwipeActive).copy(
            cooldownUntilMs = now + GestureThresholds.SWIPE_COOLDOWN_MS,
            awaitingRest = true,
        )
        return GestureAction.Swipe(direction)
    }

    /**
     * M2 (di chuyen) + M3 (click). Khac voi M1: tach b,c la thao tac BINH
     * THUONG trong che do nay (chuan bi click/hold), KHONG phai dieu kien huy
     * - vi vay khong dung entryTargetOf() lien tuc nhu SwipeActive.
     *
     * Yeu cau nguoi dung 2026-09-20: sau khi vao M2 KHONG con dieu kien huy
     * nao theo tu the tay nua (bo han dieu kien khep ngon cai theo t0 - do
     * goc xoe ngon cai qua nhieu, tu vao/thoat lien tuc that thuong khien con
     * tro nhu khong di chuyen duoc). M2 gio CHI thoat duoc bang xoe ca 5 ngon
     * (SPEC muc 4.0, xu ly o dau onFrame) - mat tay KHONG con huy M2 nua (xem
     * onHandLost), va vi nhanh nay khong goi entryTargetOf() nen doi sang tu
     * the M1 (2/3 ngon) trong luc dang o M2 cung KHONG kich hoat duoc M1.
     */
    private fun detectCursor(s: InternalState.CursorActive, pose: StablePose, features: Features, now: Long): GestureAction? {
        // 2 CHE DO (yeu cau nguoi dung 2026-09-20, GestureThresholds.CURSOR_POINTING_MODE)
        // dung 2 HE QUY CHIEU DAU RIENG - khong dung chung 1 quy uoc dau vi la
        // 2 dai luong vat ly khac nhau (vi tri P vs huong ngon tro):
        // - Di chuyen tay (mac dinh): P la VI TRI - dao dau (giong M1, xac
        //   nhan tren may that) roi tinh delta giua 2 khung da loc, TICH LUY
        //   qua tung khung (dung s.sessionS, xem giai thich o sessionS).
        // - Theo huong ngon tro (truc X): huong ngon tro - KHONG dao dau (xac
        //   nhan tren may that khac voi vi tri). Truc X = vi tri TUYET DOI
        //   lech so voi huong luc vao M2 (s.refDirX), tinh lai tu dau moi
        //   khung, khong tich luy.
        val pointing = GestureThresholds.CURSOR_POINTING_MODE

        val newFilteredX: Float
        val newFilteredY: Float
        val newCumulativeX: Float
        val newCumulativeY: Float

        var pendingVerticalStep = s.pendingVerticalStep
        var pendingVerticalStepSinceMs = s.pendingVerticalStepSinceMs
        var confirmedVerticalStep = s.confirmedVerticalStep

        // "HA" dung pose.bDownRelaxed/cDownRelaxed (nguong long hon, xem
        // GestureThresholds.CURSOR_POINTING_R_DOWN) THAY VI pose.b/c == DOWN
        // thuong - yeu cau nguoi dung 2026-09-20 lan 6: nghieng TRAI lam gap
        // het co van chi doc duoc r~0.80 (khong bao gio xuong duoi
        // R_DOWN=0.65 thuong), khien Voter ket cung mai o "DUNG" cu. "UP"
        // (pose.b/c == UP) VAN dung nguong thuong - khong bi anh huong theo
        // du lieu do duoc.
        val bDown = pose.bDownRelaxed == FingerState.DOWN
        val cDown = pose.cDownRelaxed == FingerState.DOWN

        if (pointing) {
            newFilteredX = s.filterX.filter(features.pointDirX, now)
            newFilteredY = s.lastFilteredY // truc Y che do nay khong dung loc vi tri/huong nua, xem duoi
            newCumulativeX = (newFilteredX - s.refDirX) * GestureThresholds.CURSOR_GAIN_POINTING

            // Truc Y (yeu cau nguoi dung 2026-09-20, lan 3 - thay het cho goc
            // chi tay/ngon cai da thu truoc do, khong du bien do): dung trang
            // thai RIENG RAC cua b,c (giong nut bam, khong phai gia tri lien
            // tuc). PHAI kiem tra dung UP/DOWN cu the (khong phai "!= UP"),
            // vi AMBIGUOUS (tay mat dau/tracking kem, yeu cau nguoi dung
            // 2026-09-20 lan 4) se bi tinh nham thanh "HA" neu chi kiem tra
            // "!= UP", gay con tro tu di chuyen tiep khi khong con bat duoc
            // tay ro rang:
            //   - b,c deu UP (giong tu the vao M2) -> dung yen
            //   - b,c deu HA -> di chuyen XUONG lien tuc (nhu giu nut)
            //   - b UP, c HA -> di chuyen LEN lien tuc (nhu giu nut)
            //   - con lai (AMBIGUOUS, hoac b HA c UP chua dinh nghia) -> dung yen
            val rawVerticalStep = when {
                pose.b == FingerState.UP && pose.c == FingerState.UP -> 0f
                bDown && cDown -> GestureThresholds.CURSOR_POINTING_VERTICAL_STEP
                pose.b == FingerState.UP && cDown -> -GestureThresholds.CURSOR_POINTING_VERTICAL_STEP
                else -> 0f
            }
            if (rawVerticalStep != pendingVerticalStep) {
                pendingVerticalStep = rawVerticalStep
                pendingVerticalStepSinceMs = now
            }
            // "Deu dung"/"deu ha" ap dung NGAY (2 dich den on dinh that su).
            // Rieng "LEN" phai giu on dinh du lau (xem
            // GestureThresholds.CURSOR_POINTING_UP_CONFIRM_MS) moi cong nhan,
            // vi day chinh la trang thai giao nhau tam thoi giua 2 Voter doc
            // lap cua b,c luc dang chuyen "deu dung" <-> "deu ha" (nguoi dung
            // bao 2026-09-20 lan 5: "ca 2 ngon deu ha ma con tro van di
            // chuyen len" - la san pham phu cua qua trinh vote, khong phai y
            // dinh nguoi dung).
            val heldMs = now - pendingVerticalStepSinceMs
            val isUpCandidate = rawVerticalStep < 0f
            confirmedVerticalStep = if (isUpCandidate && heldMs < GestureThresholds.CURSOR_POINTING_UP_CONFIRM_MS) {
                s.confirmedVerticalStep
            } else {
                pendingVerticalStep
            }
            newCumulativeY = s.cumulativeY + confirmedVerticalStep
        } else {
            newFilteredX = s.filterX.filter(-features.p.x / s.sessionS, now)
            newFilteredY = s.filterY.filter(-features.p.y / s.sessionS, now)
            newCumulativeX = s.cumulativeX + (newFilteredX - s.lastFilteredX) * GestureThresholds.CURSOR_GAIN_TRANSLATION
            newCumulativeY = s.cumulativeY + (newFilteredY - s.lastFilteredY) * GestureThresholds.CURSOR_GAIN_TRANSLATION
        }

        // "Tach that" (chuan bi click/hold) CHI tinh khi CA HAI b,c VAN dang
        // DUNG (r >= R_UP) va rieng KHOANG CACH 2 dau ngon (gBC) da rong ra -
        // tuc la 2 ngon van thang, chi xoe rong sang 2 ben. KHONG chi dua vao
        // pose.bc == OPEN don thuan, vi gap 2 dau ngon cung ro ra y het khi
        // MOT ngon GAP LAI (vd c cong xuong long ban tay trong khi b van
        // thang) - do la tu the GAP NGON (dung cho truc Y "theo huong ngon
        // tro", xem tren), khong phai tu the TACH NGON that (yeu cau nguoi
        // dung 2026-09-20 lan 7: "phan biet giua gap ngon c va tach 2 ngon
        // b,c" - ap dung CHUNG cho moi che do, khong rieng gi pointing mode
        // nhu bien verticalAsymmetric truoc day (da bo, khong con can nua).
        val bcOpen = pose.bc == PairState.OPEN && pose.b == FingerState.UP && pose.c == FingerState.UP

        // "blocked": an toan bat buoc thu 2/3 (CLAUDE.md muc 4.5) vua tu nha
        // trong khi b,c VAN dang tach (giu qua HOLD_MAX_MS) - khoa khong cho
        // tu dong vao lai phien giu/click moi cho toi khi bcOpen THAT SU
        // xuong false (khep tay lai) it nhat 1 lan, khong chi doi het
        // HOLD_MAX_MS. Con tro van duoc phep di chuyen THEO DOI (CursorMove
        // binh thuong o cuoi ham) trong luc bi khoa - chi khong tinh
        // click/hold moi.
        val blocked = s.holdBlockedUntilClose && bcOpen

        val justSeparated = bcOpen && !blocked && s.separatedAtMs == null
        val newSeparatedAtMs = when {
            justSeparated -> now
            bcOpen && !blocked -> s.separatedAtMs
            else -> null
        }
        // SPEC muc 4.3/4.4: toa do click/hold chup TAI THOI DIEM BAT DAU
        // TACH, khong phai luc khep lai hay luc bat dau giu.
        val separatedCursorX = if (justSeparated) newCumulativeX else s.separatedCursorX
        val separatedCursorY = if (justSeparated) newCumulativeY else s.separatedCursorY

        var holding = s.holding
        var newHoldBlockedUntilClose = if (!bcOpen) false else s.holdBlockedUntilClose
        var action: GestureAction? = null

        when {
            // Dang tach (khong bi khoa): b,c da tach du CLICK_MAX_MS ma van
            // chua khep -> chuyen thanh GIU (SPEC muc 4.4), tu day tro di moi
            // khung di chuyen phat HoldMove thay vi CursorMove.
            bcOpen && !blocked && newSeparatedAtMs != null -> {
                val heldMs = now - newSeparatedAtMs
                if (!holding && heldMs >= GestureThresholds.CLICK_MAX_MS) {
                    holding = true
                    action = GestureAction.HoldStart(separatedCursorX, separatedCursorY)
                } else if (holding && heldMs >= GestureThresholds.HOLD_MAX_MS) {
                    // An toan bat buoc thu 2/3 (CLAUDE.md muc 4.5): giu qua
                    // HOLD_MAX_MS du tay van con -> tu nha, khoa lai (xem
                    // holdBlockedUntilClose o tren).
                    holding = false
                    newHoldBlockedUntilClose = true
                    action = GestureAction.HoldEnd
                } else if (holding && (newCumulativeX != s.cumulativeX || newCumulativeY != s.cumulativeY)) {
                    action = GestureAction.HoldMove(newCumulativeX, newCumulativeY)
                }
            }
            // Khep lai trong luc dang giu -> nha (SPEC muc 4.4 "nha: khep b va c").
            !bcOpen && s.holding -> {
                holding = false
                action = GestureAction.HoldEnd
            }
            // Chua bao gio vao GIU, khep lai truoc CLICK_MAX_MS -> click (SPEC
            // muc 4.3). PHAI giu du CLICK_MIN_MS moi tinh (yeu cau nguoi dung
            // 2026-09-20 lan 7: tach-khep QUA NHANH luc dinh di chuyen, khong
            // phai co y click, khong duoc tinh la click).
            !bcOpen && s.separatedAtMs != null && !s.holdBlockedUntilClose -> {
                val heldMs = now - s.separatedAtMs
                if (heldMs in GestureThresholds.CLICK_MIN_MS..GestureThresholds.CLICK_MAX_MS && now >= s.clickCooldownUntilMs) {
                    action = GestureAction.Click(s.separatedCursorX, s.separatedCursorY)
                }
            }
        }

        val moved = newCumulativeX != s.cumulativeX || newCumulativeY != s.cumulativeY
        state = s.copy(
            lastFilteredX = newFilteredX,
            lastFilteredY = newFilteredY,
            cumulativeX = newCumulativeX,
            cumulativeY = newCumulativeY,
            pendingVerticalStep = pendingVerticalStep,
            pendingVerticalStepSinceMs = pendingVerticalStepSinceMs,
            confirmedVerticalStep = confirmedVerticalStep,
            separatedAtMs = newSeparatedAtMs,
            separatedCursorX = separatedCursorX,
            separatedCursorY = separatedCursorY,
            clickCooldownUntilMs = if (action is GestureAction.Click) now + GestureThresholds.CLICK_COOLDOWN_MS else s.clickCooldownUntilMs,
            holding = holding,
            holdBlockedUntilClose = newHoldBlockedUntilClose,
        )

        if (action != null) return action
        if (!moved) return null
        return GestureAction.CursorMove(newCumulativeX, newCumulativeY)
    }

    /** VERTICAL (Len/Xuong): b,c dung khep nhau; d,e gap (tu the M1 cu). */
    private fun isVerticalEntryPose(pose: StablePose): Boolean =
        pose.b == FingerState.UP && pose.c == FingerState.UP &&
            pose.d == FingerState.DOWN && pose.e == FingerState.DOWN &&
            pose.thumb == ThumbState.IN &&
            pose.bc == PairState.CLOSE

    /**
     * HORIZONTAL (Trai/Phai): b,c,d dung khep nhau ca 3; e gap (yeu cau nguoi
     * dung 2026-09-20). Dung lai nguong khep/tach G_CLOSE/G_OPEN cua cap b-c
     * cho cap c-d - TODO(untouch): chua co so do rieng cho c-d, gia dinh
     * tuong tu b-c, can kiem chung tren may that.
     */
    private fun isHorizontalEntryPose(pose: StablePose): Boolean =
        pose.b == FingerState.UP && pose.c == FingerState.UP && pose.d == FingerState.UP &&
            pose.e == FingerState.DOWN &&
            pose.thumb == ThumbState.IN &&
            pose.bc == PairState.CLOSE && pose.cd == PairState.CLOSE

    /** M2 (SPEC muc 4.2): a,b,c dung; b,c khep; a xoe (thumb OUT); d,e gap. */
    private fun isCursorEntryPose(pose: StablePose): Boolean =
        pose.b == FingerState.UP && pose.c == FingerState.UP &&
            pose.d == FingerState.DOWN && pose.e == FingerState.DOWN &&
            pose.thumb == ThumbState.OUT &&
            pose.bc == PairState.CLOSE

    /**
     * M5 SPREAD (SPEC muc 4.5, tu the goc): b,c,d,e deu dung va "hoi roi
     * nhau" (it nhat 1 cap lien ke dat nguong rong G_OPEN4, xem
     * PoseClassifier/StablePose.bcWide/cdWide/deWide); a bat buoc gap (thumb
     * IN) de khong lan voi tay ranh 5 ngon xoe (allFiveUp yeu cau thumb OUT).
     *
     * Dung pose.eUpRelaxed (nguong long hon R_UP thuong) THAY VI pose.e cho
     * rieng ngon ut - yeu cau nguoi dung 2026-09-20 lan 10: "4 ngon dua len
     * de bi lan voi 3 ngon" (M1 ngang) - ngon ut kho duoi thang het co bang
     * 3 ngon kia, dung R_UP thuong de xet "e dung" cho M5 khien du co y dua
     * ca 4 ngon len van hay bi doc thanh "e ha" -> lan sang M1 ngang.
     */
    private fun isSystemSpreadEntryPose(pose: StablePose): Boolean =
        pose.b == FingerState.UP && pose.c == FingerState.UP && pose.d == FingerState.UP && pose.eUpRelaxed == FingerState.UP &&
            pose.thumb == ThumbState.IN &&
            (pose.bcWide == PairState.OPEN || pose.cdWide == PairState.OPEN || pose.deWide == PairState.OPEN)

    /**
     * M5 CLOSED (them 2026-09-20, lan 8): b,c,d,e deu dung nhung KHEP lai voi
     * nhau (ca 3 cap lien ke <= G_CLOSE) - nguoc voi SPREAD o tren; a van gap
     * (thumb IN). Yeu cau nguoi dung: vay Trai/Phai (Back/Da nhiem cu) qua
     * kho on dinh trong tu the SPREAD - tach thanh tu the rieng, chi dung
     * Len/Xuong de kich hoat (xem detectSystem). Dung pose.eUpRelaxed cho
     * ngon ut, cung ly do voi SPREAD o tren (lan 10).
     */
    private fun isSystemClosedEntryPose(pose: StablePose): Boolean =
        pose.b == FingerState.UP && pose.c == FingerState.UP && pose.d == FingerState.UP && pose.eUpRelaxed == FingerState.UP &&
            pose.thumb == ThumbState.IN &&
            pose.bcWide == PairState.CLOSE && pose.cdWide == PairState.CLOSE && pose.deWide == PairState.CLOSE

    /**
     * Thu tu kiem tra QUAN TRONG (yeu cau nguoi dung 2026-09-20, lan 13:
     * "van con nham lan giua 3 ngon va 4 ngon khep"): xet M5 (System) TRUOC
     * M1 ngang. Ly do - do bang may that: r_e "3 ngon" ~0.45, "4 ngon khep"
     * ~0.8. 0.8 nam trong VUNG XAM cua pose.e (thang do thuong, R_DOWN=0.65
     * den R_UP=0.90) - AMBIGUOUS la gia tri TRUNG LAP trong Voter, KHONG the
     * ghi de gia tri DOWN cu con sot lai tu truoc do (vd nguoi dung vua lam
     * xong tu the 3 ngon). Neu chuyen thang tu 3 ngon sang 4 ngon khep,
     * pose.e (thang do thuong) co the van con "dinh" o DOWN du thuc te dang
     * la 4 ngon - neu isHorizontalEntryPose duoc xet truoc va dua vao
     * pose.e nay, se bi nham thanh M1 ngang. pose.eUpRelaxed thi KHONG bi
     * dinh (0.8 >= SYSTEM_E_UP_RELAXED=0.78, luon co phieu UP that moi khung)
     * - xet System truoc de no "thang" duoc truoc khi pose.e cu kip gay nham.
     */
    private fun entryTargetOf(pose: StablePose): ArmTarget? = when {
        GestureThresholds.ENABLE_M5_SYSTEM && isSystemSpreadEntryPose(pose) -> ArmTarget.System(SystemPoseMode.SPREAD)
        GestureThresholds.ENABLE_M5_SYSTEM && isSystemClosedEntryPose(pose) -> ArmTarget.System(SystemPoseMode.CLOSED)
        GestureThresholds.ENABLE_M1_SWIPE && isHorizontalEntryPose(pose) -> ArmTarget.Swipe(SwipeAxisMode.HORIZONTAL)
        GestureThresholds.ENABLE_M1_SWIPE && isVerticalEntryPose(pose) -> ArmTarget.Swipe(SwipeAxisMode.VERTICAL)
        GestureThresholds.ENABLE_M2_CURSOR && isCursorEntryPose(pose) -> ArmTarget.Cursor
        else -> null
    }

    /**
     * M5 (SPEC muc 4.5, lan 8: tach 2 tu the KHEP/TACH; lan 9: doi lai dung
     * Trai/Phai de kich hoat, xem DECISIONS.md - nguoi dung yeu cau doi lai
     * sau khi thu Len/Xuong). Moi tu the (CLOSED/SPREAD, xem SystemPoseMode)
     * cho ra 1 cap hanh dong rieng, chon bang vay Trai/Phai. Huy: NGUOC voi
     * tu the vao cua chinh no - CLOSED thoat khi tach ra (>=1 cap OPEN),
     * SPREAD thoat khi khep lai (ca 3 cap CLOSE). CHI 1 hanh dong moi phien
     * (xem SystemActive.fired) - phai huy roi vao lai moi kich hoat tiep
     * duoc. Van dung lai cong thuc van toc cua detectSwipe (da xac nhan
     * tren may that), nguong cao hon (SYS_VEL_MIN) vi hanh dong he thong
     * kho chiu hon nhieu khi nhan nham.
     */
    private fun detectSystem(s: InternalState.SystemActive, pose: StablePose, features: Features, now: Long): GestureAction? {
        val stillOpenAny = pose.bcWide == PairState.OPEN || pose.cdWide == PairState.OPEN || pose.deWide == PairState.OPEN
        val stillClosedAll = pose.bcWide == PairState.CLOSE && pose.cdWide == PairState.CLOSE && pose.deWide == PairState.CLOSE
        val cancelled = when (s.mode) {
            SystemPoseMode.CLOSED -> stillOpenAny // huy bang cach TACH ra
            SystemPoseMode.SPREAD -> stillClosedAll // huy bang cach KHEP lai (nhu cu)
        }
        if (cancelled) {
            state = InternalState.Idle
            return null
        }

        val current = TimedPoint(now, features.p)
        if (s.fired) {
            // Da thuc hien 1 hanh dong trong phien nay - tu choi them cho toi
            // khi huy (SPEC muc 4.5 "mot hanh dong moi lan vao che do").
            state = s.copy(history = listOf(current))
            return null
        }

        val history = (s.history + current).filter { now - it.timeMs <= GestureThresholds.VELOCITY_WINDOW_MS }
        val oldest = history.first()
        val dtMs = now - oldest.timeMs
        if (dtMs < GestureThresholds.VELOCITY_WINDOW_MS / 2) {
            state = s.copy(history = history)
            return null // chua du du lieu trong cua so
        }

        val dx = (features.p.x - oldest.p.x) / features.s
        val dy = (features.p.y - oldest.p.y) / features.s
        val velX = dx / dtMs * 1000f
        val velY = dy / dtMs * 1000f
        val absX = abs(velX)
        val absY = abs(velY)

        if (absX < GestureThresholds.SYS_VEL_MIN) {
            state = s.copy(history = history)
            return null
        }
        if (absY > absX) {
            // Chi con quan tam Trai/Phai (yeu cau nguoi dung 2026-09-20 lan
            // 9: doi lai tu Len/Xuong sau khi thu) - chuyen dong doc du nhanh
            // cung bo qua, khong anh xa sang Len/Xuong nua.
            state = s.copy(history = history)
            return null
        }
        val ratio = absX / absY.coerceAtLeast(MIN_DIVISOR)
        if (ratio < GestureThresholds.AXIS_RATIO) {
            state = s.copy(history = history)
            return null // chuyen dong cheo, bo qua
        }

        // Quy uoc truc giong detectSwipe (da xac nhan tren may that): velX > 0 = TRAI.
        val goingLeft = velX > 0
        val type = when (s.mode) {
            SystemPoseMode.CLOSED -> if (goingLeft) GestureAction.SystemActionType.BACK else GestureAction.SystemActionType.RECENTS
            SystemPoseMode.SPREAD -> if (goingLeft) GestureAction.SystemActionType.HOME else GestureAction.SystemActionType.NOTIFICATIONS
        }

        state = s.copy(history = history, fired = true)
        return GestureAction.SystemAction(type)
    }

    private companion object {
        const val MIN_DIVISOR = 1e-4f
    }
}
