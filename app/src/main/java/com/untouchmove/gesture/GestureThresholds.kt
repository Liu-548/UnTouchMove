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

    // Nguong "ha" RIENG cho truc Y cua che do "theo huong ngon tro"
    // (GestureStateMachine.detectCursor, yeu cau nguoi dung 2026-09-20, lan
    // 6 - bao "ca 2 ngon deu ha nhung nghieng TRAI thi con tro lai di chuyen
    // len"). Do dac tren may that bang man hinh Debug, ngon tro (b) khi gap
    // het co (cung 1 cu dong vat ly) doc duoc: nghieng PHAI r_b=0.55 (duoi
    // R_DOWN=0.65, qua nguong binh thuong), nghieng TRAI r_b chi toi ~0.80
    // (KHONG bao gio xuong duoi 0.65) - do goc nhin camera bi bop meo khi
    // nghieng trai, khong phai loi logic. Voter (PoseClassifier) co "tri
    // nho" (giu ket luan on dinh cu neu khong co gia tri moi thang phieu) -
    // vi r_b khong bao gio cham nguong 0.65 luc nghieng trai, no KHONG BAO
    // GIO tu chuyen duoc sang "HA" bang nguong cu, ket qua la ket luan "DUNG"
    // tu truoc (vd luc vao M2) bi ket cung mai. Rieng cho truc Y cua che do
    // nay, dung nguong long hon de "HA" van nhan duoc du nghieng huong nao -
    // KHONG doi R_DOWN toan cuc (van dung cho d,e o M1/M2 va cho JSON b,c o
    // noi khac) de tranh anh huong cac tu the khac.
    // TODO(untouch): 0.85 dua tren 1 diem do (0.80 luc nghieng trai het co) +
    // bien do an toan nho, chua co nhieu du lieu, can kiem chung them tren
    // may that (dac biet la co lam M1/other bi anh huong khong - ve ly
    // thuyet khong vi bien nay chi dung o detectCursor).
    const val CURSOR_POINTING_R_DOWN = 0.85f

    // Khep/tach hai dau ngon (b,c). Du lieu that: khep p90=1.30, tach p10=1.88.
    const val G_CLOSE = 1.3f
    // G_OPEN la `var` co the chinh qua Cai dat (yeu cau nguoi dung 2026-09-20,
    // lan 7: "khi tach ra tay de roi khoi man hinh [khung hinh camera]" -
    // phai xoe 2 ngon qua rong (dung dung gia tri do that 1.85) day tay ra
    // gan mep khung hinh camera, mat tracking. Cho phep GIAM xuong (de tach
    // hon, xoe it hon van duoc tinh la "tach") de nguoi dung tu can chinh
    // toi muc vua du roi vao tay, khong doan mot con so co dinh moi thay the.
    const val DEFAULT_G_OPEN = 1.85f
    // MIN giu cach G_CLOSE=1.3 it nhat 0.15 de khong xoa het vung dem
    // hysteresis (tranh nhap nhay lien tuc quanh 1 nguong duy nhat).
    const val MIN_G_OPEN = 1.45f
    const val MAX_G_OPEN = DEFAULT_G_OPEN
    var G_OPEN = DEFAULT_G_OPEN

    // Tu the vao M5 (4 ngon roi nhau). Du lieu that: M5 gmax p10=2.17.
    const val G_OPEN4 = 2.0f

    // Nguong "dung" RIENG cho ngon ut (e) khi xet tu the vao M5 (4 ngon,
    // yeu cau nguoi dung 2026-09-20 lan 10: "4 ngon dua len de bi lan voi 3
    // ngon [M1 ngang]"). Ngon ut thuong khong duoi thang het co nhu 3 ngon
    // kia (dac diem giai phau ban tay, khong phai loi do), nen dung R_UP=0.90
    // thuong de xet "e dung" cho M5 khien nguoi dung co y dua ca 4 ngon len
    // van hay bi doc thanh "e ha" -> nham lan thanh tu the M1 ngang (3 ngon).
    // Nguong long hon nay CHI dung de xet "e dung" cho rieng tu the vao M5 -
    // KHONG doi R_UP toan cuc (M1 ngang van dung R_DOWN thuong de xet "e ha",
    // khong bi anh huong).
    // TODO(untouch): 0.78 la so doan (giua R_DOWN=0.65 va R_UP=0.90, lech ve
    // phia de "dung" hon), chua do that xem co du long de het nham M1<->M5
    // hay chua, va co lam M1 ngang de bi nham thanh M5 hay khong (danh doi
    // nguoc lai, can nguoi dung xac nhan tren may that).
    const val SYSTEM_E_UP_RELAXED = 0.78f

    // Ngon cai xoe/khep. Du lieu that KHONG tach biet ro (M1 khep t dao dong
    // 0.32-0.62, trung vi 0.48; M2 xoe het co t~0.62-0.75) - nguy co lan giua
    // M1/M2 cao hon cac truc khac. Neu thuc te dung sai nhieu, can ghi vao
    // BACKLOG xem xet bo phan loai rieng cho ngon cai.
    // T_IN dat sat T_OUT (khong phai giua khoang do duoc cua M1) vi muc tieu
    // la M1 phai nhan duoc "ngon cai khep" thuc te, khong phai tach biet dep
    // voi M2 - M2 chua duoc xay dung nen chua co gi de lan vao luc nay.
    // ponytail: T_OUT/T_IN chi dua tren 1 nguoi dung, chua co bien do an toan lon.
    const val T_OUT = 0.62f
    const val T_IN = 0.58f
    const val T_CLOSE_RATIO = 0.6f

    // --- Chua do duoc o Phase 1 (can chuyen dong that, xem Phase 2/3) ---
    const val ARM_HOLD_MS = 500L
    const val ARM_JITTER = 0.15f
    // 300 -> 600 (2026-09-20): nguoi dung bao chua tung click duoc lan nao.
    // Nghi ngo chinh: PoseClassifier vote 4/5 khung cho MOI lan doi trang thai
    // (tach VA khep lai deu can rieng ~4-5 khung de "vote" moi duoc coi la da
    // xay ra) - o fps thap, riêng viec XAC NHAN da tach xong co the da an het
    // phan lon 300ms, khong con du thoi gian de xac nhan khep lai kip. Tang
    // len gap doi de bu do tre vote nay. TODO(untouch): van la doan, chua do
    // that so khung/ms thuc te can de vote xong 1 lan tach-khep.
    const val CLICK_MAX_MS = 600L
    // Nguong duoi cho click (yeu cau nguoi dung 2026-09-20, lan 7): "khi tach
    // 2 ngon de di chuyen chu khong phai click, neu tach ra dong lai that
    // nhanh thi khong duoc tinh la click" - loc cac lan tach-khep QUA NGAN de
    // khong phai co y dinh click that (rung tay/nhieu tracking thoang qua khi
    // dang di chuyen), chi la "vua tach vua khep ngay" chu khong phai mot
    // thao tac tach ro rang. Neu heldMs < CLICK_MIN_MS thi bo qua hoan toan
    // (khong phai click, cung khong phai hold - coi nhu chua he xay ra).
    // TODO(untouch): 100ms la doan ban dau, chua do that xem co qua nghiem
    // ngat khien click that (nhanh, co y) cung bi loc mat khong.
    const val CLICK_MIN_MS = 100L
    const val CLICK_COOLDOWN_MS = 250L
    // Gia tri mac dinh + khoang cho phep tren thanh truot Cai dat (SettingsScreen.kt).
    // Da thu nhieu gia tri tren may that 2026-09-20 (xem DECISIONS.md): UP tu
    // 0.4->0.15->0.1, DOWN tu 1.0->0.4->0.25, LEFT_RIGHT tu 1.0->0.8->0.65->0.3.
    const val DEFAULT_SWIPE_VEL_MIN_UP = 0.1f
    const val MIN_SWIPE_VEL_MIN_UP = 0.03f
    const val MAX_SWIPE_VEL_MIN_UP = 0.5f
    const val DEFAULT_SWIPE_VEL_MIN_DOWN = 0.25f
    const val MIN_SWIPE_VEL_MIN_DOWN = 0.05f
    const val MAX_SWIPE_VEL_MIN_DOWN = 0.6f
    // Tach LEFT/RIGHT thanh 2 nguong doc lap (yeu cau nguoi dung 2026-09-22:
    // "tach thanh nhay trai/phai thanh 2 thanh rieng") - truoc day dung chung
    // 1 nguong SWIPE_VEL_MIN_LEFT_RIGHT cho ca 2 huong. Gia tri mac dinh/khoang
    // giu nguyen nhu cu cho ca 2 huong, nguoi dung tu chinh lech nhau qua Cai
    // dat neu can (vd tay thuan mot ben nhay hon ben kia).
    const val DEFAULT_SWIPE_VEL_MIN_LEFT = 0.3f
    const val MIN_SWIPE_VEL_MIN_LEFT = 0.1f
    const val MAX_SWIPE_VEL_MIN_LEFT = 1.0f
    const val DEFAULT_SWIPE_VEL_MIN_RIGHT = 0.3f
    const val MIN_SWIPE_VEL_MIN_RIGHT = 0.1f
    const val MAX_SWIPE_VEL_MIN_RIGHT = 1.0f

    // Do nhay MediaPipe nhan dien tay/ngon tay (yeu cau nguoi dung 2026-09-20).
    // Dung chung cho ca 3 nguong cua HandLandmarker (detection/presence/
    // tracking) - don gian hoa, chua thay ly do phai tach rieng.
    // LUU Y KHAC BIET voi cac nguong SWIPE_VEL_MIN_* o tren: gia tri nay chi
    // duoc doc 1 LAN LUC TAO HandLandmarker (xem HandLandmarkerHelper), khong
    // phai moi khung hinh - doi luc dang chay phai dong+tao lai HandLandmarker
    // moi ap dung duoc (xem GestureForegroundService.observeSettings), gay
    // giat hinh ngan luc doi.
    const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5f
    const val MIN_HAND_DETECTION_CONFIDENCE = 0.2f
    const val MAX_HAND_DETECTION_CONFIDENCE = 0.8f
    var HAND_DETECTION_CONFIDENCE = DEFAULT_HAND_DETECTION_CONFIDENCE

    // Gian cach toi thieu giua 2 lan vuot - nguoi dung chinh qua Cai dat
    // (yeu cau 2026-09-20), khong con co dinh.
    const val DEFAULT_SWIPE_COOLDOWN_MS = 800L
    const val MIN_SWIPE_COOLDOWN_MS = 200L
    const val MAX_SWIPE_COOLDOWN_MS = 2000L

    // Nguong van toc toi thieu RIENG THEO HUONG (SPEC muc 4.1). Day la 3 gia
    // tri NGUOI DUNG CHINH DUOC qua man hinh Cai dat (thanh truot do nhay,
    // xem SettingsScreen.kt + data/SettingsRepository.kt) - vi vay la `var`
    // chu khong phai `const val` nhu cac nguong khac trong file nay.
    // GestureForegroundService doc gia tri da luu (DataStore) va gan vao day
    // luc khoi dong + moi khi nguoi dung doi trong man hinh Cai dat, ap dung
    // ngay khong can khoi dong lai pipeline.
    var SWIPE_VEL_MIN_UP = DEFAULT_SWIPE_VEL_MIN_UP
    var SWIPE_VEL_MIN_DOWN = DEFAULT_SWIPE_VEL_MIN_DOWN
    var SWIPE_VEL_MIN_LEFT = DEFAULT_SWIPE_VEL_MIN_LEFT
    var SWIPE_VEL_MIN_RIGHT = DEFAULT_SWIPE_VEL_MIN_RIGHT

    // Lam moc chung cho SWIPE_REST_VEL_MAX (ben duoi) khi can 1 gia tri duy
    // nhat - KHONG con dung truc tiep de xet nguong vuot trong M1 nua (xem
    // SWIPE_VEL_MIN_UP/DOWN/LEFT_RIGHT), va KHONG con lam goc tinh SYS_VEL_MIN
    // (M5) nua - SYS_VEL_MIN gio la `var` doc lap, tu chinh rieng (xem duoi).
    val SWIPE_VEL_MIN get() = SWIPE_VEL_MIN_DOWN
    // Cua so tinh van toc lot (SPEC muc 6) - lam min nhieu dtMs giua cac khung
    // hinh (MediaPipe cho frame rate dao dong ~23-97ms/khung tren may that).
    // TODO(untouch): 120ms la doan ban dau, can tinh chinh lai cung SWIPE_VEL_MIN.
    const val VELOCITY_WINDOW_MS = 120L
    // Nguong van toc toi thieu cho 4 hanh dong M5 (yeu cau nguoi dung
    // 2026-09-20 lan 14: "tang do nhay cua 4 dong tac 4 ngon"). Truoc day la
    // gia tri TINH TU SWIPE_VEL_MIN_DOWN (*1.5, khong tu chinh rieng duoc) -
    // doi thanh `var` doc lap voi gia tri mac dinh giu nguyen ket qua cu
    // (0.25*1.5=0.375) de khong doi hanh vi ngay khi chua chinh, nhung gio
    // nguoi dung tu chinh rieng qua Cai dat ma khong anh huong nguong vuot
    // M1 (SWIPE_VEL_MIN_DOWN).
    const val DEFAULT_SYS_VEL_MIN = 0.375f
    const val MIN_SYS_VEL_MIN = 0.1f
    const val MAX_SYS_VEL_MIN = 0.6f
    var SYS_VEL_MIN = DEFAULT_SYS_VEL_MIN
    var SWIPE_COOLDOWN_MS = DEFAULT_SWIPE_COOLDOWN_MS
    // Nguong "dung yen" de cho vuot tiep sau 1 cu vuot (SPEC muc 4.1 "chong
    // vuot nguoc" - yeu cau nguoi dung 2026-09-20: phai ve tay chuan bi moi
    // vuot tiep, khong chi doi het SWIPE_COOLDOWN_MS). Doan ban dau: 1 phan
    // ba SWIPE_VEL_MIN. TODO(untouch): can do lai tren may that.
    val SWIPE_REST_VEL_MAX get() = SWIPE_VEL_MIN / 3f
    // Tang lai len 2.2 (tung ha xuong 1.5 truoc do) - yeu cau nguoi dung
    // 2026-09-20: vuot Trai/Phai rat de lan sang Len/Xuong. Nguyen nhan: tay
    // vay Trai/Phai tu nhien co mot chut lech truc doc (co tay/khuyu tay), va
    // vi SWIPE_VEL_MIN_UP/DOWN thap hon han LEFT_RIGHT nen chi can lech truc
    // doc vua du vuot nguong thap do la bi chot nham thanh Len/Xuong. Ty le
    // truc chinh/phu phai lon hon han moi chac chan la huong do, chap nhan
    // danh doi: mot so cu vay chua that thang truc se bi bo qua (phai vay lai)
    // thay vi bi nhan sai huong.
    const val AXIS_RATIO = 2.2f
    // He so con tro (SPEC muc 4.2): so dp con tro di chuyen tren man hinh cho
    // moi 1 don vi chuyen dong/lech huong tay. Lop Android
    // (UnTouchAccessibilityService) chi quy doi dp->px theo mat do man hinh,
    // khong nhan them he so nao khac - CHINH LA noi duy nhat chinh do "nhay"
    // cua con tro. Nguoi dung tu nhap so truc tiep qua man hinh Cai dat (o
    // nhap so, khong phai thanh truot - vi khong co gioi han tren co dinh
    // nao hop ly cho gia tri nay).
    //
    // TACH RIENG 2 he so cho 2 che do (yeu cau nguoi dung 2026-09-20, lan 4):
    // ban dau dung chung 1 CURSOR_GAIN, nhung don vi/do lon hop ly rat khac
    // nhau giua "di chuyen theo tay" (ty le vi tri/S) va "theo huong ngon
    // tro" (do lech vector don vi, thuong nho hon nhieu) - dung chung khien
    // nguoi dung phai go lai so moi lan doi che do.
    const val DEFAULT_CURSOR_GAIN_TRANSLATION = 150f
    var CURSOR_GAIN_TRANSLATION = DEFAULT_CURSOR_GAIN_TRANSLATION
    const val DEFAULT_CURSOR_GAIN_POINTING = 150f
    var CURSOR_GAIN_POINTING = DEFAULT_CURSOR_GAIN_POINTING

    // Che do con tro (yeu cau nguoi dung 2026-09-20): false = di chuyen theo
    // CHUYEN DONG TAY (mac dinh, SPEC muc 4.2 ban dau); true = di chuyen theo
    // HUONG NGON TRO dang chi (nhu dieu khien tia laser) - danh cho nguoi
    // thay kho di chuyen tay du xa de bao het man hinh.
    var CURSOR_POINTING_MODE = false

    // Truc Y cua che do "theo huong ngon tro" (yeu cau nguoi dung 2026-09-20,
    // lan 3 - thay het cho goc chi tay/ngon cai da thu truoc do, khong du
    // bien do vat ly): dung trang thai RIENG RAC cua b,c (nhu nut bam) thay
    // vi gia tri lien tuc - xem detectCursor. Day la BUOC DI CHUYEN MOI KHUNG
    // (don vi truu tuong, giong dx/dy) khi dang giu trang thai "di chuyen",
    // KHONG phai vi tri tuyet doi. TODO(untouch): 8 la doan ban dau, chua do
    // that tren may (phu thuoc fps thuc te).
    const val DEFAULT_CURSOR_POINTING_VERTICAL_STEP = 8f
    var CURSOR_POINTING_VERTICAL_STEP = DEFAULT_CURSOR_POINTING_VERTICAL_STEP

    // Nguoi dung bao (2026-09-20, lan 5): doi khi ca 2 ngon b,c DEU HA ma con
    // tro van giat LEN mot chut. Nguyen nhan that su: b,c la 2 Voter DOC LAP
    // trong PoseClassifier (moi Voter tu vote rieng, xem PoseClassifier.kt) -
    // khi tay that chuyen tu "deu dung" sang "deu ha", 2 Voter KHONG chac
    // chuyen xong cung luc, nen co 1 khoang (vai khung) 1 ngon da vote xong
    // DOWN con ngon kia con dang la UP (tu vote cu) - dung LA to hop "b DUNG,
    // c HA" (=lenh LEN) NHUNG chi la SAN PHAM PHU cua qua trinh chuyen trang
    // thai, khong phai nguoi dung co y dinh chi LEN. Vi "LEN" chinh la trang
    // thai bat doi xung nam GIUA 2 trang thai on dinh ("deu dung" va "deu
    // ha"), no se luon thoang qua moi lan doi giua dung yen/xuong - khong the
    // loc bang cach doi them vai khung giong nhau (van co the du "giong nhau"
    // trong dung khoang thoi gian lech vote do). Fix: rieng lenh LEN phai GIU
    // ON DINH lien tuc it nhat khoang thoi gian nay (ms, khong phai so khung,
    // vi fps thuc te dao dong 23-97ms/khung) truoc khi duoc cong nhan; "deu
    // dung"/"deu ha" van duoc ap dung ngay (khong bi cho) vi day la 2 dich
    // den ON DINH that su, khong phai trang thai giao nhau.
    // TODO(untouch): 200ms la doan ban dau dua tren fps quan sat duoc, chua
    // do that xem co qua tre khi nguoi dung CO Y muon di chuyen len khong.
    const val CURSOR_POINTING_UP_CONFIRM_MS = 200L
    const val LOST_HAND_MS = 300L
    const val HOLD_LOST_MS = 300L
    const val HOLD_MAX_MS = 15_000L
    const val EDGE_MARGIN = 0.05f
    const val CONF_MIN = 0.6f

    // Bat/tat tung nhom cu chi rieng le (SPEC muc 7 "bat/tat tung nhom cu
    // chi", ROADMAP Phase 7). Chinh qua Cai dat (SettingsScreen.kt +
    // data/SettingsRepository.kt), xet o GestureStateMachine.entryTargetOf -
    // nhom dang tat thi khong vao duoc che do do, cac nhom khac khong anh huong.
    // M1 tach thanh 2 co RIENG cho 2 ngon (Len/Xuong) va 3 ngon (Trai/Phai)
    // (yeu cau nguoi dung 2026-09-22: "thieu bat tat 2 ngon, 3 ngon rieng
    // biet") - truoc day dung chung 1 co ENABLE_M1_SWIPE cho ca 2 tu the.
    var ENABLE_M1_VERTICAL = true
    var ENABLE_M1_HORIZONTAL = true
    var ENABLE_M2_CURSOR = true
    var ENABLE_M5_SYSTEM = true

    // M6: xoe 5 ngon dung yen roi nam tay lai -> tat man hinh (yeu cau nguoi
    // dung 2026-09-22). CHI hoat dong khi KHONG dang o M2 (con tro), xem
    // GestureStateMachine.updateScreenLock.
    var ENABLE_M6_SCREEN_OFF = true
    // Phai xoe 5 ngon DUNG YEN (nhu ARM_JITTER cac che do khac) du lau nay
    // moi duoc coi la "san sang" (hien mau cam) truoc khi nam tay duoc tinh -
    // tranh tat man hinh nham luc tay chi luot qua tu the 5 ngon.
    const val SCREEN_LOCK_ARM_HOLD_MS = 1000L
    // Nguong rieng cho "nam tay" (GestureStateMachine.isFistPose) - LONG hon
    // R_DOWN=0.65 thuong. Yeu cau nguoi dung 2026-09-22: "qua kho de ghi nhan
    // nam ban tay lai" - nam tay can CA 4 ngon (rB/rC/rD/rE) cung luc duoi
    // nguong, khac han cac tu the khac chi can 1-2 ngon gap, nen can nguong
    // rong hon de du 4 ngon deu "kip" duoi nguong trong cung 1 khung (dac
    // biet dau ngon tay hay bi MediaPipe doc kem chinh xac hon khi bi che
    // khuat trong long ban tay luc nam chat).
    // TODO(untouch): 0.80 la so doan (rong hon R_DOWN kha nhieu nhung van
    // duoi han R_UP=0.90), CHUA co du lieu do that tren tay nam that - can
    // nguoi dung xac nhan lai xem da du de nam de hay chua, hoac con qua de
    // nham voi cac tu the khac khong (chua ghi nhan truong hop nao).
    const val FIST_R_DOWN = 0.80f

    // "Phien ban 2" (yeu cau nguoi dung 2026-09-22): quyet dinh CACH "tat man
    // hinh" hoat dong.
    // false (mac dinh, "phien ban 1"): khoa man hinh THAT (GLOBAL_ACTION_LOCK_SCREEN,
    // giong bam nut nguon) - camera se tu tat theo dung CLAUDE.md muc 4.3,
    // KHONG mo lai duoc bang cu chi (phai bam nguon/van tay nhu binh thuong).
    // true ("phien ban 2"): CHI phu 1 lop MAN DEN che kin man hinh
    // (OverlayRenderer.showBlackCurtain), KHONG khoa/tat gi that ca - camera
    // van chay BINH THUONG xuyen suot (khong dung den CLAUDE.md muc 4.3 chut
    // nao, vi man hinh khong he tat that), nen nam tay du
    // SCREEN_OFF_REOPEN_HOLD_MS roi xoe ra bat ky luc nao se go duoc lop den.
    // Xem GestureForegroundService.dispatch va DECISIONS.md muc "M6".
    var ENABLE_SCREEN_OFF_REOPEN_GESTURE = false
    const val SCREEN_OFF_REOPEN_HOLD_MS = 500L
}
