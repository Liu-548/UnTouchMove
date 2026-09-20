package com.untouchmove.gesture

enum class FingerState { UP, DOWN, AMBIGUOUS }
enum class ThumbState { OUT, IN, AMBIGUOUS }
enum class PairState { CLOSE, OPEN, AMBIGUOUS }

/** Ket luan da on dinh sau vote 4/5 khung (SPEC muc 3). */
data class StablePose(
    val b: FingerState,
    val c: FingerState,
    val d: FingerState,
    val e: FingerState,
    val thumb: ThumbState,
    val bc: PairState,
    val cd: PairState,
    // "Ha" rieng cho truc Y cua che do "theo huong ngon tro" (yeu cau nguoi
    // dung 2026-09-20, lan 6) - dung nguong long hon R_DOWN thuong (xem
    // GestureThresholds.CURSOR_POINTING_R_DOWN) vi nghieng trai/phai lam gap
    // het co van ly doc duoc r khac nhau. CHI dung o GestureStateMachine.
    // detectCursor, khong thay the b/c thuong o cac tu the M1/M2 khac.
    val bDownRelaxed: FingerState,
    val cDownRelaxed: FingerState,
    // Cap lien ke "roi rong" (nguong G_OPEN4, xem GestureThresholds) - CHI
    // dung cho tu the vao/huy M5 (SPEC muc 4.5): "hoi roi nhau" khi vao (it
    // nhat 1 cap dat OPEN o nguong nay) va "khep ca 4 ngon" khi huy (moi cap
    // dat CLOSE). Khac voi bc/cd o tren (dung nguong G_OPEN cho M2, co the
    // nguoi dung tu chinh qua Cai dat) - G_OPEN4 la nguong CO DINH, cao hon,
    // vi day la tu the "xoe rong 4 ngon" chu khong phai "tach 2 dau ngon".
    val bcWide: PairState,
    val cdWide: PairState,
    val deWide: PairState,
    // "Dung" rieng cho ngon ut (e), nguong long hon R_UP thuong (xem
    // GestureThresholds.SYSTEM_E_UP_RELAXED) - CHI dung de xet tu the vao M5
    // (4 ngon), vi ngon ut thuong kho duoi thang het co bang 3 ngon kia.
    // KHONG thay the pose.e o cac tu the khac (M1 ngang van dung pose.e binh
    // thuong de xet "e ha").
    val eUpRelaxed: FingerState,
) {
    /** SPEC muc 4.0: ca 5 ngon deu dung ("tay ranh") -> khong kich hoat gi. */
    val allFiveUp: Boolean
        get() = b == FingerState.UP && c == FingerState.UP && d == FingerState.UP &&
            e == FingerState.UP && thumb == ThumbState.OUT
}

/**
 * On dinh hoa mot chuoi gia tri roi rac theo da so 4/5 khung gan nhat (SPEC
 * muc 3). Neu khong gia tri nao dat da so trong cua so, giu nguyen ket luan
 * on dinh truoc do (tranh nhap nhay khi dao dong quanh nguong).
 */
private class Voter<T>(private val neutral: T) {
    private val window = ArrayDeque<T>()
    private var stable: T = neutral

    fun push(value: T): T {
        window.addLast(value)
        if (window.size > WINDOW_SIZE) window.removeFirst()
        val winner = window.groupingBy { it }.eachCount().entries
            .filter { it.key != neutral }
            .firstOrNull { it.value >= VOTE_THRESHOLD }
            ?.key
        if (winner != null) stable = winner
        return stable
    }

    private companion object {
        const val WINDOW_SIZE = 5
        const val VOTE_THRESHOLD = 4
    }
}

/**
 * Phan loai dung/gap tung ngon, xoe/khep ngon cai, khep/tach cap b-c tu
 * Features, co hysteresis (hai nguong R_UP/R_DOWN) + vote 4/5 khung (SPEC
 * muc 3). Co trang thai (giu cua so 5 khung gan nhat) nen phai dung 1
 * instance xuyen suot cho moi tay dang theo doi, khong dung lai giua chung.
 */
class PoseClassifier {
    private val bVoter = Voter(FingerState.AMBIGUOUS)
    private val cVoter = Voter(FingerState.AMBIGUOUS)
    private val dVoter = Voter(FingerState.AMBIGUOUS)
    private val eVoter = Voter(FingerState.AMBIGUOUS)
    private val thumbVoter = Voter(ThumbState.AMBIGUOUS)
    private val bcVoter = Voter(PairState.AMBIGUOUS)
    private val cdVoter = Voter(PairState.AMBIGUOUS)
    private val bDownRelaxedVoter = Voter(FingerState.AMBIGUOUS)
    private val cDownRelaxedVoter = Voter(FingerState.AMBIGUOUS)
    private val bcWideVoter = Voter(PairState.AMBIGUOUS)
    private val cdWideVoter = Voter(PairState.AMBIGUOUS)
    private val deWideVoter = Voter(PairState.AMBIGUOUS)
    private val eUpRelaxedVoter = Voter(FingerState.AMBIGUOUS)

    fun classify(f: Features): StablePose = StablePose(
        b = bVoter.push(rawFinger(f.rB)),
        c = cVoter.push(rawFinger(f.rC)),
        d = dVoter.push(rawFinger(f.rD)),
        e = eVoter.push(rawFinger(f.rE)),
        thumb = thumbVoter.push(rawThumb(f.t)),
        bc = bcVoter.push(rawPair(f.gBC)),
        cd = cdVoter.push(rawPair(f.gCD)),
        bDownRelaxed = bDownRelaxedVoter.push(rawFinger(f.rB, downThreshold = GestureThresholds.CURSOR_POINTING_R_DOWN)),
        cDownRelaxed = cDownRelaxedVoter.push(rawFinger(f.rC, downThreshold = GestureThresholds.CURSOR_POINTING_R_DOWN)),
        bcWide = bcWideVoter.push(rawPair(f.gBC, openThreshold = GestureThresholds.G_OPEN4)),
        cdWide = cdWideVoter.push(rawPair(f.gCD, openThreshold = GestureThresholds.G_OPEN4)),
        deWide = deWideVoter.push(rawPair(f.gDE, openThreshold = GestureThresholds.G_OPEN4)),
        eUpRelaxed = eUpRelaxedVoter.push(rawFinger(f.rE, upThreshold = GestureThresholds.SYSTEM_E_UP_RELAXED)),
    )

    private fun rawFinger(
        r: Float,
        upThreshold: Float = GestureThresholds.R_UP,
        downThreshold: Float = GestureThresholds.R_DOWN,
    ): FingerState = when {
        r >= upThreshold -> FingerState.UP
        r <= downThreshold -> FingerState.DOWN
        else -> FingerState.AMBIGUOUS
    }

    private fun rawThumb(t: Float): ThumbState = when {
        t >= GestureThresholds.T_OUT -> ThumbState.OUT
        t <= GestureThresholds.T_IN -> ThumbState.IN
        else -> ThumbState.AMBIGUOUS
    }

    private fun rawPair(
        g: Float,
        closeThreshold: Float = GestureThresholds.G_CLOSE,
        openThreshold: Float = GestureThresholds.G_OPEN,
    ): PairState = when {
        g <= closeThreshold -> PairState.CLOSE
        g >= openThreshold -> PairState.OPEN
        else -> PairState.AMBIGUOUS
    }
}
