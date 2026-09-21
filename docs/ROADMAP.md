# ROADMAP — UnTouchMove

Nguyên tắc: **đo trước, code sau**. Phase 1 tồn tại để thay các con số phỏng đoán
trong SPEC §6 bằng số đo thật từ tay của chính chủ dự án.

Đánh dấu `[x]` khi xong. Claude Code cập nhật file này sau mỗi task.

---

## Phase 0 — Dựng khung (nửa ngày)

- [x] Tạo project Gradle, Kotlin, Compose, minSdk 29 / targetSdk 36
- [x] Thêm CameraX + MediaPipe Tasks Vision, tải `hand_landmarker.task` vào `assets/`
- [x] Manifest: quyền camera, foreground service type camera, KHÔNG có INTERNET
- [x] `.gitignore`, `LICENSE`, `README.md`
- [x] Build chạy được (`./gradlew assembleDebug` thành công), hiện preview camera trước

**Xong khi**: cài lên GT Neo 2, thấy khung hình camera trước. ✅ Đã test trên
máy thật (RMX3370 / GT Neo 2): preview hiện được, không bị lật gương.

## Phase 1 — Công cụ đo (quan trọng nhất, đừng bỏ qua)

- [x] `FeatureExtractor` tính r, g, t, P, S (thuần Kotlin, có unit test)
- [x] `DebugActivity`: hiện preview + vẽ skeleton + **bảng số liệu trực tiếp**
      (r_b, r_c, r_d, r_e, g_bc, g_cd, g_de, t, fps, confidence)
- [x] Nút ghi log: bấm ghi 5 giây feature ra file CSV trong bộ nhớ riêng của app
      (kèm nút chia sẻ file mới nhất qua share sheet để lấy dữ liệu ra khỏi máy)
- [x] Ghi bộ dữ liệu mẫu: mỗi tư thế trong SPEC làm 20 lần, cộng thêm chuyển động
      tay bình thường **không** có ý định điều khiển (dữ liệu âm tính) — đã ghi
      140 lần trên RMX3370 (6 tư thế × 20 + 20 lần ghi lại riêng cho M2)
- [x] Vẽ phân bố, chọn ngưỡng nằm ở khoảng trống giữa hai cụm — phân tích percentile
      10/90 trên ~6900 dòng dữ liệu
- [x] Ghi ngưỡng thật vào `GestureThresholds.kt`, ghi lại lý do vào `DECISIONS.md`
      — `R_*`, `G_*`, `T_*` đã có số thật; `SWIPE_VEL_MIN` và các hằng số thời
      gian/vận tốc còn lại vẫn là phỏng đoán, chưa đo được (cần chuyển động thật
      ở Phase 2/3). Rủi ro còn lại: ngón cái (`t`) tách biệt kém, xem DECISIONS.md.

**Xong khi**: bảng ngưỡng trong SPEC §6 được thay bằng số đo thật.
**Nếu các cụm chồng lấn nhiều** → ghi vào BACKLOG, cân nhắc bộ phân loại TFLite nhỏ.

## Phase 2 — Bơm cử chỉ (đi trước nhận diện)

- [x] `UnTouchAccessibilityService` + màn hình hướng dẫn bật trong Cài đặt
      (`GestureTestActivity`, kèm hướng dẫn "Allow restricted settings" Android 13+)
- [x] Bơm thử bằng nút bấm tay (chưa có camera): vuốt 4 hướng, click một điểm,
      drag, và 4 global action — đã test trên RMX3370, chủ dự án xác nhận toàn
      bộ hoạt động đúng (nút vuốt/click/kéo có trễ 2.5s để kịp chuyển sang màn
      hình chủ/app khác quan sát, vì cử chỉ luôn bắn vào màn hình đang hiện)
- [x] Đo xem drag bằng `continueStroke` có mượt chấp nhận được không — chủ dự
      án xác nhận ổn qua "Kéo thử"

**Xong khi**: bấm nút trong app thì hệ thống thật sự vuốt / click / back / home.
**Đây là phase rủi ro kỹ thuật cao nhất — làm sớm để biết sớm.**

## Phase 3 — M1 Lướt 2 ngón (cử chỉ đầu tiên chạy thật)

- [x] `PoseClassifier` với hysteresis + vote 4/5 khung (6 unit test pass)
- [x] `GestureStateMachine`: IDLE → ARMING (500ms giữ yên) → M1 (unit test pass)
- [x] Phát hiện vẩy theo vận tốc + cooldown chống vuốt ngược + lọc trục (unit
      test pass; đã hiệu chuẩn `SWIPE_VEL_MIN=0.4`, `AXIS_RATIO=1.5` trên máy
      thật 2026-09-20, xem DECISIONS.md — vẫn có thể cần đo thêm)
- [x] `GestureForegroundService` + thông báo thường trú + nút tắt — **đã hết
      crash và hết tự tắt** sau khi sửa 3 lỗi (JNI/thread cho overlay, race
      condition unbindAll() ở MainActivity, xem DECISIONS.md 2026-09-20)
- [x] Icon trạng thái nhỏ ở góc màn hình qua `OverlayRenderer` — chạy ổn định
      trên máy thật sau khi sửa lỗi threading ở trên
- [x] Bắt buộc tay "đứng yên" mới cho vuốt tiếp, không chỉ chờ cooldown thời
      gian (yêu cầu người dùng 2026-09-20, xem SPEC §4.1 + DECISIONS.md)

**✅ Hết chặn (2026-09-20)**: 3 lỗi khiến camera crash/tự tắt đã sửa xong và
xác nhận trên máy thật (RMX3370). Chi tiết điều tra ở `docs/DEBUG_NOTES_Phase3.md`.

- [x] Tách tư thế M1 thành 2 biến thể loại trừ nhau (2 ngón = Lên/Xuống, 3
      ngón = Trái/Phải) để hết lẫn hướng — xem SPEC §4.1 + DECISIONS.md
- [x] Màn hình Cài đặt độ nhạy (`SettingsActivity` + `SettingsRepository`,
      DataStore) với 5 thanh trượt: Vuốt Lên/Xuống/Trái-Phải, độ nhạy bắt
      tay (`HAND_DETECTION_CONFIDENCE`), giãn cách giữa 2 lần thao tác
      (`SWIPE_COOLDOWN_MS`) — chỉnh xong áp dụng ngay, không cần build lại
- [x] Xoá log debug tạm `UnTouchSwipeDebug` sau khi người dùng xác nhận
      ngưỡng ổn định qua nhiều vòng test thật

**Xong khi**: cuộn được Facebook / YouTube bằng hai ngón tay, không chạm màn hình.
**✅ Đạt (2026-09-20)** — người dùng xác nhận ổn định qua test thật nhiều vòng.
Ngưỡng cụ thể vẫn có thể chỉnh tiếp qua màn hình Cài đặt mà không cần code.

## Phase 4 — M2 Con trỏ + M3 Click

- [x] Điểm neo = P, chuyển động tương đối, `CURSOR_GAIN` — `GestureStateMachine`
      tổng quát hoá `Arming`/`Active` thành `ArmTarget` (Swipe/Cursor) dùng
      chung, thêm `CursorActive` state
- [x] One Euro filter chống rung (`gesture/OneEuroFilter.kt`, thuần Kotlin)
- [x] Vẽ con trỏ bằng overlay (`overlay/CursorView.kt`, cập nhật vị trí qua
      `WindowManager.updateViewLayout`)
- [x] Logic khép a theo `t0` cá nhân hóa để hủy
- [x] Click: tách rồi khép trong `CLICK_MAX_MS`, lấy toạ độ lúc bắt đầu tách
      (`GestureAction.Click` mang toạ độ tích luỹ đơn vị trừu tượng, quy đổi
      pixel ở `UnTouchAccessibilityService` bằng đúng công thức dùng cho
      `CursorMove` để khớp vị trí)
- [x] Unit test: vào M2, hướng di chuyển đúng quy ước pixel, click nhanh,
      giữ lâu không phải click, huỷ theo t0 (11 test tổng, tất cả pass)

**Cập nhật 2026-09-20 (đã test trên máy thật, nhiều vòng)**: cấu trúc M2 đã
đổi so với bản đầu — bật bằng 3 ngón a,b,c, CHỈ tắt bằng xoè cả 5 ngón, mất
tay không huỷ mà giữ nguyên vị trí, luôn xuất hiện từ giữa màn hình. Có 2 chế
độ di chuyển độc lập (theo tay / theo hướng ngón trỏ) với hệ quy chiếu và hệ
số riêng (`CURSOR_GAIN_TRANSLATION`/`CURSOR_GAIN_POINTING`, chỉnh qua Cài
đặt). Trục Y của chế độ "theo hướng ngón trỏ" dùng trạng thái rời rạc b,c
(không dùng góc/ngón cái liên tục — không đủ biên độ vật lý), có chống khựng
2 khung và coi tay mất dấu (AMBIGUOUS) là đứng yên. Click đã dùng được
(`CLICK_MAX_MS=600ms`).

**Cập nhật 2026-09-20 (vòng 2)**: người dùng xác nhận cả 2 rủi ro trên THỰC
SỰ xảy ra (giật LÊN dù cả 2 ngón đều hạ, và click nhầm khi đổi lên/xuống).
Root cause: `b`,`c` là 2 Voter vote độc lập trong `PoseClassifier`, luôn có
khoảng lệch vote khi chuyển trạng thái đồng thời — đã sửa bằng hold-time
200ms riêng cho lệnh LÊN (`CURSOR_POINTING_UP_CONFIRM_MS`) + loại trừ phát
hiện click bất cứ khi nào `pose.b != pose.c`. Chi tiết: `docs/DECISIONS.md`
mục "Root cause thật của giật LEN/click nhầm".

**Cập nhật 2026-09-20 (vòng 3)**: người dùng báo thêm biến thể — nghiêng
TRÁI khi hạ cả 2 ngón vẫn bị giật LÊN, nghiêng phải thì không. Root cause
thật (đo bằng màn hình Debug): gập hết cỡ ngón trỏ lúc nghiêng trái chỉ đo
được `r_b≈0.80`, không bao giờ chạm ngưỡng "hạ" thường (`R_DOWN=0.65`) do
góc camera bị biến dạng phối cảnh theo hướng nghiêng — không phải lỗi logic
debounce. Đã thêm ngưỡng "hạ" riêng, lỏng hơn, CHỈ áp dụng cho trục Y của
chế độ theo hướng ngón trỏ (`CURSOR_POINTING_R_DOWN=0.85`), không đụng
ngưỡng `R_DOWN` toàn cục (M1/M2 khác không bị ảnh hưởng). Chi tiết:
`docs/DECISIONS.md` mục "Root cause thật của nghiêng trái thì giật LÊN".

**Rủi ro còn lại, cần theo dõi khi test**:
- `CURSOR_POINTING_R_DOWN=0.85` chỉ dựa trên 1 điểm đo, chưa xác nhận đủ ở
  các góc nghiêng khác hoặc có làm "HẠ" quá dễ kích hoạt hay không.
- `CURSOR_POINTING_UP_CONFIRM_MS=200ms` là số đoán, chưa đo có làm lệnh LÊN
  bị chậm/khó chịu khi người dùng thực sự cố ý muốn đi lên hay không.
- `CURSOR_POINTING_VERTICAL_STEP=8f` và cả 2 `CURSOR_GAIN_*` vẫn là số đoán/
  người dùng tự chỉnh qua Cài đặt, chưa có giá trị "chuẩn" cố định.

**Xong khi**: bấm trúng một nút cỡ trung bình trong 3 lần thử trở xuống.

## Phase 5 — M4 Hold và kéo

- [x] Gộp M3/M4 thành một logic: tách = bắt đầu tính giờ, khép trước
      `CLICK_MAX_MS` = click (M3), giữ tách quá đó = chuyển thành giữ (M4) —
      `GestureAction.HoldStart/HoldMove/HoldEnd` mới, phát từ `detectCursor`
- [x] Ba lớp timeout an toàn (CLAUDE.md mục 4.5):
      1. mất tay ≥ `HOLD_LOST_MS` → `onHandLost()` trả về `HoldEnd` ngay
         (tái dùng đúng thời điểm `LOST_HAND_MS` ở lớp gọi, hai hằng số này
         cùng 300ms nên không cần đếm giờ riêng)
      2. giữ quá `HOLD_MAX_MS` dù tay vẫn còn tách → tự nhả, khoá
         (`holdBlockedUntilClose`) không cho vào lại phiên giữ mới cho tới
         khi khép tay lại thật sự
      3. `UnTouchAccessibilityService.onDestroy()` → `cancelActiveDrag()`
         nhả nốt nếu service bị tắt giữa lúc đang giữ
- [x] Kéo thật bằng chuỗi `continueStroke` nối tiếp theo vị trí con trỏ mỗi
      khung — vị trí đến dồn dập trong lúc đang chờ callback trước được GỘP
      vào 1 điểm đích mới nhất (không xếp hàng) để tránh giật/trễ
- [x] Unit test: HoldStart đúng toạ độ lúc bắt đầu tách, HoldMove khi tay di
      chuyển lúc đang giữ, cả 2 lớp an toàn (HOLD_MAX_MS tự nhả + khoá đến
      khi khép tay, mất tay nhả ngay) — 30 test tổng, tất cả pass
- [ ] Test kỹ trên máy thật: kéo icon màn hình chính, kéo thanh trượt âm lượng

**Cập nhật 2026-09-20 (đã test trên máy thật)**: giữ được, kéo được, nhưng
người dùng báo "không thực sự tốt" + phát hiện thêm lỗi con trỏ không chạm
được viền màn hình. Đã sửa:
- **Lỗi con trỏ bị chặn 4 hướng**: `toScreenPx()` dùng nhầm `EDGE_MARGIN`
  (vốn là ngưỡng "tay chạm lề khung hình CAMERA", không phải vị trí con trỏ
  trên MÀN HÌNH) để kẹp con trỏ vào 5%-95% màn hình — bỏ hẳn, giờ con trỏ
  chạm được toàn bộ 0..100% màn hình. Chi tiết: `docs/DECISIONS.md`.
- **Thêm màu con trỏ theo trạng thái** để người dùng tự chẩn đoán rõ hơn khi
  nào là "đang di chuyển" (xanh cyan) / "vừa tách, chờ xem có phải click
  không" (vàng) / "đang giữ/kéo thật" (đỏ) / "vừa bị khoá do quá
  HOLD_MAX_MS" (xám) — theo đúng đề nghị của người dùng, không đoán mù thêm
  về nguyên nhân "kéo không tốt". Đang chờ người dùng test lại và báo cụ thể
  hơn dựa trên màu sắc quan sát được.

**Cập nhật 2026-09-20 (vòng 2)**: người dùng phản hồi thêm 3 ý:
1. Tách-khép quá nhanh (lúc chỉ định di chuyển, không cố ý click) không nên
   tính là click — đã thêm `CLICK_MIN_MS=100ms`.
2. Phân biệt "gập ngón c" và "tách 2 ngón thật" — đã tổng quát hoá (trước
   chỉ áp dụng ở pointing mode, giờ áp dụng mọi chế độ): chỉ tính "tách" khi
   cả 2 ngón vẫn dựng thẳng, không phải khi 1 ngón gập cong.
3. Thảo luận nguyên nhân "khó điều khiển khi tách 2 ngón" → xác định là:
   xoè 2 ngón đúng mức đo thật (`G_OPEN=1.85`) khiến tay dễ bị đẩy ra khỏi
   khung hình camera. Đã thêm thanh trượt "Độ nhạy tách ngón" trong Cài đặt
   để người dùng tự chỉnh (`G_OPEN` giờ chỉnh được từ 1.45 đến 1.85) thay vì
   đoán 1 số cố định. Chi tiết đầy đủ: `docs/DECISIONS.md`.

**Rủi ro/giá trị chưa đo, cần xác nhận khi test tiếp**:
- `HOLD_STEP_DURATION_MS=80ms` (mỗi đoạn `continueStroke` khi đang kéo) là số
  đoán — kéo có mượt không, có giật do hệ thống từ chối đoạn quá ngắn không.
- Độ trễ giữa lúc tay di chuyển và lúc icon/nội dung thật sự kéo theo trên
  màn hình (do phải đợi callback `onCompleted` của đoạn trước mới gửi đoạn
  tiếp) — cảm giác dùng có ổn không.
- Rủi ro đã ghi nhận trong SPEC (bảng rủi ro): "Kéo giật vì phải nối nhiều
  đoạn dispatchGesture" — chấp nhận ở bản đầu, đo lại sau nếu người dùng báo
  khó chịu.

**Tạm dừng ở đây 2026-09-20** (quyết định người dùng): "tạm thời cứ để vậy,
note cần cải thiện giai đoạn sau" — chuyển sang Phase 6, quay lại tinh chỉnh
M4 sau khi có thêm dữ liệu test thật. Đã ghi vào `docs/BACKLOG.md`.

**Xong khi**: kéo được và **không lần nào** làm màn hình kẹt ở trạng thái nhấn.

## Phase 6 — M5 Cử chỉ hệ thống 4 ngón

- [x] Tư thế vào: b,c,d,e dựng, ít nhất 1 cặp liền kề rời rộng (`G_OPEN4`), a
      gập — thêm `bcWide/cdWide/deWide` (`PoseClassifier`/`StablePose`) dùng
      ngưỡng `G_OPEN4` cố định, tách biệt với `G_OPEN` (nay chỉnh được qua
      Cài đặt cho M2) để không lẫn 2 khái niệm "tách 2 ngón" và "xoè rộng 4 ngón"
- [x] Ngưỡng vận tốc cao hơn M1 (`SYS_VEL_MIN = SWIPE_VEL_MIN * 1.5`, đã có sẵn
      trong `GestureThresholds` từ trước), một hành động mỗi lần vào chế độ —
      `InternalState.SystemActive.fired` chặn hành động thứ 2 cho tới khi huỷ
- [x] Xử lý lật gương cho trục trái/phải — tái dùng ĐÚNG quy ước trục đã xác
      nhận trên máy thật ở M1 (`detectSwipe`: velX>0 = TRÁI), không suy diễn
      lại từ đầu
- [x] Hủy bằng khép 4 ngón (mọi cặp liền kề `<= G_CLOSE`) — quay về NONE, vào
      lại được phiên mới bình thường
- [x] Unit test: vào M5, vẫy 1 lần đúng hướng, vẫy lần 2 trong cùng phiên bị
      chặn, huỷ bằng khép 4 ngón rồi vào lại vẫy được tiếp — 34 test tổng,
      tất cả pass

**Cập nhật 2026-09-20 (đã test trên máy thật)**: vào M5 dễ (không cần xoè
rộng), khép tay tự huỷ đúng — nhưng vẫy Trái/Phải (Back/Đa nhiệm) gần như
không hoạt động ("Back chưa từng thành công, Home chỉ 1 lần"), trong khi
Xuống (Thanh thông báo) hoạt động tốt. Theo đúng đề xuất của người dùng, đã
**bỏ hẳn trục Trái/Phải**, tách thành 2 TƯ THẾ riêng (`SystemPoseMode`), mỗi
tư thế chỉ dùng Lên/Xuống:
- **KHÉP** (4 ngón dựng thẳng, khép sát nhau, mới thêm): Lên=Back,
  Xuống=Đa nhiệm. Huỷ bằng cách tách ra.
- **TÁCH** (tư thế M5 gốc): Lên=Home, Xuống=Thanh thông báo (giữ nguyên cặp
  đã xác nhận Xuống hoạt động tốt). Huỷ bằng cách khép lại (như cũ).

`GestureAction.SystemAction` đổi sang mang thẳng `SystemActionType`
(BACK/RECENTS/HOME/NOTIFICATIONS) thay vì `Direction`. Cập nhật 34 test
(2 test M5 viết lại theo tư thế mới), build + cài lại lên máy. Chi tiết đầy
đủ: `docs/DECISIONS.md` mục "M5 (lần 8)".

**Cập nhật 2026-09-20 (lần 9)**: người dùng yêu cầu đổi trục kích hoạt trở
lại Trái/Phải (giữ nguyên cấu trúc 2 tư thế Khép/Tách). Giờ: KHÉP
(Trái=Back, Phải=Đa nhiệm), TÁCH (Trái=Home, Phải=Thanh thông báo). Cập
nhật 34 test, build + cài lại. Chi tiết: `docs/DECISIONS.md` mục "M5 (lần 9)".

**Cập nhật 2026-09-20 (lần 10)**: người dùng báo "4 ngón dơ lên dễ bị lẫn
với 3 ngón nhưng vẫn hoạt động được". Root cause: ranh giới DUY NHẤT giữa
M1-3-ngón và M5-4-ngón là trạng thái ngón út (e) — mà ngón út khó duỗi thẳng
hết cỡ như 3 ngón kia, nên cố dựng 4 ngón vẫn hay bị đọc thành "hạ" và lẫn
sang M1. Đã thêm ngưỡng "dựng" riêng, lỏng hơn, CHỈ cho việc xét ngón út khi
vào M5 (`SYSTEM_E_UP_RELAXED=0.78`, `StablePose.eUpRelaxed`) — không đụng
`R_UP` toàn cục nên M1 3 ngón không bị ảnh hưởng. Thêm 1 test khoá lại. Chi
tiết: `docs/DECISIONS.md` mục "M5 (lần 10)".

**Cập nhật 2026-09-20 (lần 11)**: người dùng báo thêm "màu khi dơ 3 ngón
đang trùng với 2 ngón" — `SwipeAxisMode.VERTICAL`/`HORIZONTAL` (2/3 ngón)
trước đây đều map chung 1 `DisplayState.M1` nên chung 1 màu icon. Đã tách
thành `M1_VERTICAL` (xanh lá, như cũ) / `M1_HORIZONTAL` (vàng, mới). Chi
tiết: `docs/DECISIONS.md` mục "Tách màu icon cho M1 2 ngón và 3 ngón".

**Cập nhật 2026-09-20 (lần 12)**: người dùng yêu cầu làm tương tự cho cặp
M5 — tách `DisplayState.M5` thành `M5_SPREAD` (tím, như cũ) / `M5_CLOSED`
(xanh dương, mới). Bảng màu icon đầy đủ: ARMING=xám, M1_VERTICAL=xanh lá,
M1_HORIZONTAL=vàng, M2=cyan, M5_SPREAD=tím, M5_CLOSED=xanh dương. Chi tiết:
`docs/DECISIONS.md` mục "Tách màu icon cho M5 Khép và Tách".

**Cập nhật 2026-09-20 (lần 13)**: người dùng báo vẫn còn nhầm 3 ngón ↔ 4
ngón khép sau lần 10. Đo lại theo yêu cầu (màn hình Debug): `r_e` 3 ngón≈
0.45, 4 ngón khép≈0.8 — ngưỡng số vốn đã ổn, KHÔNG phải nguyên nhân. Root
cause thật: `pose.e` (thang đo thường) bị "dính" giá trị `DOWN` cũ khi
chuyển thẳng từ 3 ngón sang 4 ngón khép, vì 0.8 rơi vào vùng xám của thang
đo thường (không có phiếu bầu thật để ghi đè) — trong khi
`entryTargetOf` xét nhánh M1 ngang (dùng `pose.e` dễ dính) TRƯỚC nhánh M5.
Sửa bằng cách đổi thứ tự kiểm tra (xét M5 trước M1), không đổi ngưỡng số
nào. Thêm 1 test mô phỏng đúng kịch bản chuyển tư thế. Chi tiết:
`docs/DECISIONS.md` mục "M5 (lần 13)".

**Cập nhật 2026-09-20 (lần 14)**: người dùng yêu cầu "tăng độ nhạy của 4
động tác 4 ngón". `SYS_VEL_MIN` (ngưỡng vận tốc vẫy M5) trước đây tính ra từ
`SWIPE_VEL_MIN_DOWN * 1.5` (không tự chỉnh riêng được, đụng vào sẽ ảnh
hưởng cả M1). Đã tách thành ngưỡng độc lập + thêm thanh trượt "Độ nhạy vẫy
4 ngón" trong Cài đặt (mục mới "Cử chỉ hệ thống (M5, 4 ngón)"). Mặc định giữ
nguyên hành vi cũ (0.375), người dùng tự kéo để tăng nhạy. Chi tiết:
`docs/DECISIONS.md` mục "M5 (lần 14)".

**Rủi ro/giá trị chưa đo, cần xác nhận khi test tiếp**:
- Cần người dùng xác nhận trên máy thật việc đổi thứ tự (lần 13) đã hết
  nhầm lẫn 3↔4 ngón khép hay chưa (test tự động chỉ mô phỏng đúng 1 kịch
  bản chuyển tiếp, có thể còn kịch bản khác).
- Thanh trượt "Độ nhạy vẫy 4 ngón" mới thêm — chưa có phản hồi mức nào vừa
  đủ (dễ vẫy nhưng không nhận nhầm chuyển động tay bình thường).
- Trái/Phải là đúng trục đã gây khó khăn ở lần test trước ("Back chưa từng
  thành công") — chưa rõ vấn đề trước là do bản thân trục khó, hay do TƯ THẾ
  TÁCH (xoè rộng) cụ thể lúc đó gây khó thêm. Nếu vẫn khó, nghi ngờ chính
  nên chuyển sang tư thế KHÉP mới (có thể dễ vẫy ngang hơn TÁCH).
- Tư thế KHÉP (4 ngón dựng thẳng, khép sát) có dễ vào NHẦM từ dáng tay nghỉ
  bình thường không (rủi ro mới, khác tư thế TÁCH cũ vốn ít giống tay nghỉ
  hơn).
- Cặp ghép Trái/Phải → hành động cụ thể là lựa chọn của tôi (KHÉP khớp đúng
  cặp gốc SPEC, TÁCH thì không có tiền lệ vì SPEC gốc dùng Lên/Xuống cho cặp
  này) — chưa được người dùng xác nhận có dễ nhớ/hợp lý không.
- `G_OPEN4=2.0` là số đo thật từ Phase 1 nhưng CHƯA đo lại xem có cần chỉnh
  không (đã xác nhận không cần xoè quá rộng cho tư thế TÁCH, nhưng chưa
  biết ngưỡng CLOSE cho tư thế KHÉP mới có nhạy vừa phải không).

## Phase 7 — Hoàn thiện

- [x] Màn hình chính gọn: nút bật/tắt lớn làm trọng tâm + 3 dòng trạng thái
      quyền (Camera/Trợ năng/Thông báo, kèm nút "Bật ngay" nếu Trợ năng
      đang tắt) — `MainActivity.kt` viết lại hoàn toàn, camera preview/công
      cụ Debug/Bơm thử cử chỉ chuyển xuống mục "Công cụ nâng cao" thu gọn
- [x] Màn hình hướng dẫn cử chỉ (`GuideActivity.kt`, mới) - tóm tắt tất cả
      cử chỉ (2/3/con trỏ/4 ngón khép/4 ngón tách) bằng ngôn ngữ người dùng
      cuối, kèm hình minh hoạ bàn tay vẽ bằng Canvas (`HandPoseIcon`,
      không cần ảnh/tài nguyên ngoài) và bảng màu icon trạng thái
- [x] Bộ màu giao diện dùng chung (`ui/UnTouchTheme.kt`) áp dụng cho
      Main/Settings/Guide; `SettingsActivity` nhóm từng mục vào Card riêng
      cho dễ nhìn hơn
- [x] Toàn bộ chữ hiển thị trên màn hình (Main/Settings/Guide/Debug/Bơm thử)
      đổi sang tiếng Việt có dấu đầy đủ (yêu cầu người dùng 2026-09-20) -
      trước đó dùng tiếng Việt không dấu; comment trong code vẫn giữ không
      dấu theo quy ước cũ của cả dự án, chỉ đổi phần người dùng nhìn thấy
- [ ] Quick Settings Tile + `LauncherProxyActivity`, test trên cả hai máy
- [x] Cài đặt: bật/tắt từng nhóm cử chỉ (M1 Lướt / M2 Con trỏ / M5 Hệ thống 4
      ngón) — 3 công tắc mới trong `SettingsActivity`, lưu qua
      `SettingsRepository` (DataStore), áp dụng ngay cho
      `GestureForegroundService` đang chạy không cần khởi động lại. Tắt nhóm
      nào thì `GestureStateMachine.entryTargetOf` không nhận tư thế vào của
      nhóm đó nữa (`GestureThresholds.ENABLE_M1_SWIPE/ENABLE_M2_CURSOR/
      ENABLE_M5_SYSTEM`). "Tay thuận" **chưa làm** — SPEC chỉ dùng tay thuận
      để chọn `numHands`, không ảnh hưởng logic cử chỉ, tách để làm sau.
- [ ] README đầy đủ, LICENSE, push lên Git

**Cập nhật 2026-09-20**: người dùng yêu cầu "làm giao diện trông đẹp (tối
giản, dễ dùng), làm thêm cả hdsd". Trong lúc làm, phát hiện + sửa 1 lỗi thật
(bấm nút "Kéo lên" test hình chụp màn hình): layout `MainActivity` ban đầu
dùng `Spacer(weight(1f))` trong 1 `Column` KHÔNG cuộn được, khiến nút
"Hướng dẫn sử dụng"/"Cài đặt" bị đẩy khuất hẳn khỏi màn hình trên máy thật -
đã sửa bằng cách cho toàn màn hình cuộn được, bỏ hẳn `weight()`. Hình minh
hoạ bàn tay lần đầu (vẽ bằng đường kẻ mỏng) bị người dùng chê xấu - đã vẽ
lại bằng các "viên thuốc" (rounded rect) rõ ràng hơn nhiều. Chi tiết đầy đủ:
`docs/DECISIONS.md` mục "Phase 7 UI/UX".

- [x] "Vẫn tối giản nhưng có chiều sâu hơn" (yêu cầu người dùng 2026-09-20):
      thêm `elevation`/`shape` bo tròn 20dp cho mọi Card (Main/Settings/Guide),
      nút bấm chính dạng viên thuốc có shadow, khối xem trước camera có
      shadow + viền mờ, biểu tượng trạng thái đổi từ chấm tròn phẳng sang
      icon-trong-vòng-tròn màu, và thêm nền màu nhạt (tint 10%) phía sau mỗi
      `HandPoseIcon` trong màn Hướng dẫn cho nổi khối hơn. `UnTouchTheme.kt`
      viết lại với bộ màu tonal đầy đủ (container/outline/surfaceVariant)
      thay vì chỉ primary/secondary. Nhân tiện phát hiện + sửa 1 lỗi UI thật
      khi chụp ảnh kiểm tra: nút "Hướng dẫn sử dụng" bị ép `height(48.dp)`
      cứng nên chữ 2 dòng bị cắt mất dòng dưới trên máy thật — bỏ `height`
      cố định, cho nút tự giãn theo nội dung. Đã build + cài lên RMX3370,
      chụp ảnh xác nhận cả 3 màn hình (Main/Guide/Settings).

## Phase 8 — Sau khi ổn định

Xem `BACKLOG.md`.
