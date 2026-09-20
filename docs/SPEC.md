# SPEC — Đặc tả chức năng UnTouchMove

Phiên bản: 0.1 (bản đầu, chưa hiệu chuẩn trên máy thật)
Mọi con số ngưỡng trong file này là **giá trị khởi điểm cần đo lại** ở Phase 1.

---

## 1. Quy ước gọi tên ngón tay

| Ký hiệu | Ngón | Landmark đầu ngón | Landmark khớp gốc (MCP) |
|---|---|---|---|
| a | cái | 4 | 2 |
| b | trỏ | 8 | 5 |
| c | giữa | 12 | 9 |
| d | áp út | 16 | 13 |
| e | út | 20 | 17 |

Landmark khác: cổ tay = 0.

## 2. Các đại lượng đặc trưng (features)

Tính trên **world landmarks** (tọa độ 3D đã chuẩn hóa) để bớt sai khi tay nghiêng.

| Ký hiệu | Công thức | Ý nghĩa |
|---|---|---|
| `S` | `dist(0, 9)` | kích thước bàn tay, dùng để chuẩn hóa |
| `P` | trung bình của 0, 5, 9, 13, 17 | tâm lòng bàn tay |
| `r_x` | `dist(tip_x, P) / S` | ngón x có đang dựng không (x ∈ b,c,d,e) |
| `g_xy` | `dist(tip_x, tip_y) / dist(mcp_x, mcp_y)` | cặp ngón x,y khép hay tách |
| `t` | `dist(tip_a, mcp_b) / S` | ngón cái đang xòe hay khép |

Ghi chú thiết kế: `g` chia cho khoảng cách giữa hai khớp gốc nên **tự chuẩn hóa
theo cỡ tay từng người**; khép thì `g ≈ 1`, tách thì `g` tăng rõ rệt. Đây là
điểm khác biệt quan trọng so với việc đo khoảng cách pixel thô.

Ngón cái đo riêng vì cấu trúc bàn tay khiến nó **không bao giờ thực sự sát ngón
trỏ**, nên "khép a" được định nghĩa tương đối so với chính nó lúc vào chế độ
(xem mục 4.2).

## 3. Quy tắc nhận ngón dựng / gập

- Ngón x **dựng** nếu `r_x ≥ R_UP`.
- Ngón x **gập** nếu `r_x ≤ R_DOWN`.
- Khoảng giữa hai ngưỡng là **vùng mờ** — không kết luận, không kích hoạt gì.
- Kết luận cuối cùng lấy theo **đa số 4/5 khung hình gần nhất**.
- Ngón cái: **xòe** nếu `t ≥ T_OUT`, **khép** nếu `t ≤ T_IN`.

## 4. Máy trạng thái

```
                        ┌──────────────┐
                        │     IDLE     │◄──────── mất tay > 300ms
                        └──────┬───────┘          hoặc tay ra khỏi khung
                               │ nhận đúng tư thế vào
                               ▼
                        ┌──────────────┐
                        │    ARMING    │  giữ yên 500ms
                        └──────┬───────┘  (lệch quá → về IDLE)
                               │ đủ điều kiện → hiện icon góc màn hình
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
      ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
      │  M1: SWIPE2  │ │  M2: CURSOR  │ │ M5: SYSTEM4  │
      └──────────────┘ └──────┬───────┘ └──────────────┘
                              │
                     ┌────────┴────────┐
                     ▼                 ▼
              ┌────────────┐    ┌────────────┐
              │ M3: CLICK  │    │  M4: HOLD  │
              └────────────┘    └────────────┘
```

**Bản đầu không cho chuyển thẳng giữa M1 / M2 / M5.** Muốn đổi chế độ phải hủy
hoặc đưa tay ra khỏi khung hình, rồi vào lại từ IDLE.

### 4.0 Trạng thái nghỉ — bàn tay mở 5 ngón

Nếu nhận thấy **cả a, b, c, d, e đều dựng** thì **không kích hoạt gì cả**, bất kể
đang ở trạng thái nào (trừ khi đang HOLD thì phải nhả ra an toàn trước). Đây là
tư thế "tay rảnh", dùng để người dùng đưa tay vào khung mà không sợ lỡ tay.

### 4.1 M1 — Lướt (2 ngón cho Lên/Xuống, 3 ngón cho Trái/Phải)

**Thay đổi thiết kế 2026-09-20 (yêu cầu người dùng)**: ban đầu SPEC chỉ có 1 tư
thế 2 ngón cho cả 4 hướng, dùng vận tốc + tỉ lệ trục để đoán hướng. Test thật
cho thấy cách đoán này dễ lẫn Trái/Phải sang Lên/Xuống (vẫy ngang tự nhiên có
lệch trục dọc do cơ chế cổ tay/khuỷu tay). Chuyển sang **2 tư thế tay riêng
biệt loại trừ lẫn nhau** — tư thế đã quyết định sẵn trục được phép, không cần
đoán qua vận tốc nữa:

- **Tư thế 2 ngón (VERTICAL) — chỉ Lên/Xuống**: b và c dựng, khép nhau
  (`g_bc ≤ G_CLOSE`); d, e gập.
- **Tư thế 3 ngón (HORIZONTAL) — chỉ Trái/Phải**: b, c, d dựng, khép nhau cả
  ba (`g_bc ≤ G_CLOSE` VÀ `g_cd ≤ G_CLOSE`); e gập.
- Ở tư thế 2 ngón, chuyển động ngang trội hơn (dù nhanh cỡ nào) bị BỎ QUA
  hoàn toàn, không tính là lướt gì cả — và ngược lại với tư thế 3 ngón.
- **Thao tác**: vẩy bàn tay theo hướng muốn lướt. Một cú vẩy vượt ngưỡng vận
  tốc = một cú vuốt được bơm ra hệ thống.
- **Hủy**: đổi sang tư thế khác (kể cả tư thế kia) hoặc thả lỏng tay, hoặc
  tay ra khỏi khung.
- **Chống vuốt ngược**: sau mỗi cú vuốt, có thời gian nghỉ tối thiểu
  `SWIPE_COOLDOWN` VÀ bắt buộc tay phải về trạng thái **đứng yên** (vận tốc
  xuống dưới `SWIPE_REST_VEL_MAX`) mới cho vuốt tiếp — không chỉ chờ hết
  thời gian nghỉ. Nếu tay vẫn đang di chuyển nhanh sau khi hết `SWIPE_COOLDOWN`,
  tiếp tục bỏ qua cho đến khi tay dừng lại.
- **Hướng trong cùng 1 tư thế**: chỉ tính khi thành phần theo trục được phép
  của tư thế đó lớn hơn trục kia ít nhất `AXIS_RATIO` lần; nếu không thì bỏ
  qua (chuyển động chéo không tính).
- **Ngưỡng vận tốc RIÊNG theo hướng** (yêu cầu người dùng 2026-09-20 sau khi
  test thật thấy độ nhạy lệch hẳn giữa các hướng do đặc điểm vận động tay):
  `SWIPE_VEL_MIN_UP` (nhạy nhất), `SWIPE_VEL_MIN_DOWN` (trung bình),
  `SWIPE_VEL_MIN_LEFT_RIGHT` (kém nhạy nhất, cần di chuyển xa hơn).

### 4.2 M2 — Con trỏ ảo

- **Tư thế vào**: a, b, c dựng; b và c khép nhau (`g_bc ≤ G_CLOSE`);
  a xòe (`t ≥ T_OUT`); d, e gập.
- **Di chuyển con trỏ — 2 chế độ, chuyển qua lại bằng công tắc trong Cài đặt**
  (thay đổi 2026-09-20, yêu cầu người dùng — chế độ "theo tay" một mình khó
  bao hết màn hình nếu tay không di chuyển được xa):
  - **Theo tay (mặc định)**: theo **chuyển động tương đối** của điểm neo `P`
    (tâm lòng bàn tay, KHÔNG phải đầu ngón — lý do: khi b,c tách ra để
    click/hold, đầu ngón dịch chuyển và sẽ kéo con trỏ lệch đi), nhân hệ số
    `CURSOR_GAIN`, qua bộ lọc thích nghi (One Euro filter). Tích luỹ qua từng
    khung như dùng chuột — nhấc tay đặt lại không làm con trỏ nhảy.
  - **Theo hướng ngón trỏ**: **2 trục dùng 2 cơ chế khác nhau** (chốt sau
    3 lần thử, xem DECISIONS.md):
    - **Trái/phải**: vị trí tuyệt đối = độ lệch góc của hướng ngón trỏ (từ
      khớp gốc tới đầu ngón, KHÔNG đảo dấu — khác với vị trí `P` ở chế độ
      "theo tay", 2 đại lượng vật lý khác nhau không dùng chung 1 quy ước
      dấu) so với hướng lúc vào M2, nhân `CURSOR_GAIN`. Tính lại tuyệt đối
      mỗi khung, không tích luỹ — như điều khiển tia laser.
    - **Lên/xuống**: KHÔNG dùng góc chỉ tay (đã thử — cổ tay hết biên độ khi
      giữ tư thế vào M2, xem DECISIONS.md). Dùng **trạng thái rời rạc của
      b, c** kiểu nút bấm: b,c đều **dựng** (giống tư thế vào) → đứng yên;
      b,c đều **hạ** → di chuyển XUỐNG liên tục; b dựng, c hạ → di chuyển
      LÊN liên tục; b hạ, c dựng → chưa định nghĩa, tạm coi đứng yên. Đây là
      DI CHUYỂN LIÊN TỤC (như giữ phím) chứ không phải vị trí tuyệt đối.
  - Cả 2 chế độ dùng chung `CURSOR_GAIN` cho trục X, nhưng con số hợp lý
    khác nhau hẳn giữa 2 chế độ — cần tự chỉnh lại khi đổi chế độ.
- **Sau khi vào M2, ngón cái (a) di chuyển thế nào cũng không ảnh hưởng gì**
  (đã thử dùng ngón cái điều khiển chiều Lên, không hiệu quả — xem
  DECISIONS.md). **Thay đổi 2026-09-20 (yêu cầu người dùng)**: bản đầu có
  huỷ theo `t0` của ngón cái, nhưng góc xòe ngón cái đo trên máy thật quá
  nhiễu (xem Phase 1 — `T_OUT`/`T_IN` không tách biệt rõ), khiến M2 tự
  huỷ/vào lại liên tục ngoài ý muốn. Đã bỏ hẳn điều kiện huỷ theo ngón cái.
- **Hủy: CHỈ bằng cách xoè cả 5 ngón** (mục 4.0). **Thiết kế chính thức
  2026-09-20 (yêu cầu người dùng)** — khác với các chế độ khác:
  - **Mất tay KHÔNG huỷ M2.** Con trỏ đứng yên tại vị trí cuối cùng còn bắt
    được tay (không ẩn đi); khi bắt lại được tay, con trỏ tiếp tục di chuyển
    tiếp từ vị trí hiện tại theo hướng tay di chuyển — không nhảy, không reset.
  - **Đổi sang tư thế vuốt (M1, 2/3 ngón) trong lúc đang ở M2 KHÔNG kích hoạt
    được M1** — chỉ được coi là chuyển động tay bình thường trong M2. Muốn
    dùng M1 phải thoát M2 (xoè cả 5 ngón) trước.
  - Mỗi lần **vào M2 mới**, con trỏ luôn xuất hiện lại từ **giữa màn hình**.
- **Con trỏ hiển thị**: chấm tròn nhỏ bán trong suốt, vẽ bằng overlay của
  AccessibilityService.

### 4.3 M3 — Click

Chỉ tồn tại khi đang ở M2.

- **Điều kiện**: b và c **tách ra rồi khép lại** trong vòng `CLICK_MAX_MS`
  (điều kiện ngón cái `a` đã bỏ cùng lúc bỏ điều kiện huỷ theo `t0` ở mục 4.2
  — sau khi vào M2 chỉ còn xét b, c).
- **Tọa độ click** lấy tại **thời điểm bắt đầu tách**, không phải lúc khép lại.
- Sau khi click có cooldown `CLICK_COOLDOWN` để tránh nhân đôi.
- **`CLICK_MAX_MS` 300ms → 600ms (2026-09-20)**: người dùng báo chưa từng
  click được — nghi ngờ chính là độ trễ vote/hysteresis của `PoseClassifier`
  (mỗi lần đổi trạng thái tách/khép cần ~4-5 khung để "vote" được coi là thật
  sự xảy ra, xem mục 3) ăn hết phần lớn ngân sách 300ms trước khi kịp xác
  nhận khép lại. Tăng gấp đôi để bù độ trễ này — vẫn là **số đoán**, chưa đo
  thật số khung/ms cần thiết.

### 4.4 M4 — Hold và kéo

Chỉ tồn tại khi đang ở M2. Dùng chung logic với M3:

- b,c **tách ra** → bắt đầu tính giờ.
- Khép lại trước `CLICK_MAX_MS` → đó là **click** (M3).
- Giữ tách quá `CLICK_MAX_MS` → chuyển thành **nhấn giữ**; từ lúc này tay di
  chuyển thì con trỏ kéo theo (vừa hold vừa move).
- **Nhả**: khép b và c lại.
- **An toàn bắt buộc** (xem CLAUDE.md §4.5):
  - mất tay quá `HOLD_LOST_MS` → nhả ngay;
  - quá `HOLD_MAX_MS` → tự nhả kể cả tay vẫn còn;
  - service bị tắt vì bất cứ lý do gì → nhả trong `onDestroy`.

### 4.5 M5 — Cử chỉ hệ thống 4 ngón

- **Tư thế vào**: b, c, d, e dựng và **hơi rời nhau** (`g` lớn nhất trong các cặp
  liền kề ≥ `G_OPEN4`); a gập.
  - a **bắt buộc gập** để không lẫn với bàn tay mở 5 ngón (trạng thái nghỉ).
- **Thao tác** (vẩy theo hướng):

| Hướng | Hành động | API |
|---|---|---|
| Trái | Back | `GLOBAL_ACTION_BACK` |
| Phải | Đa nhiệm | `GLOBAL_ACTION_RECENTS` |
| Lên | Home | `GLOBAL_ACTION_HOME` |
| Xuống | Thanh thông báo | `GLOBAL_ACTION_NOTIFICATIONS` |

- **Hủy**: khép cả 4 ngón (4 đầu ngón gần sát nhau, mọi `g` cặp liền kề ≤ `G_CLOSE`).
- **Ngưỡng nghiêm ngặt hơn M1**: các hành động này rất khó chịu khi nhận nhầm,
  nên yêu cầu vận tốc cao hơn (`SYS_VEL_MIN > SWIPE_VEL_MIN`) và chỉ cho phép
  **một hành động mỗi lần vào chế độ** — sau khi thực hiện, phải hủy rồi vào lại.
- **Lưu ý gương**: camera trước lật ngang, phải đảo trục X trước khi ánh xạ
  trái/phải, nếu không back và đa nhiệm sẽ ngược nhau.

## 5. Kích hoạt và phản hồi

### 5.1 Điều kiện kích hoạt (ARMING)

Với M1, M2, M5: sau khi nhận đúng tư thế vào, người dùng phải **giữ tay tương
đối yên trong 500ms** (`ARM_HOLD_MS`) mới ghi nhận kích hoạt.

"Yên" = độ dịch chuyển của `P` trong cửa sổ 500ms nhỏ hơn `ARM_JITTER` (chuẩn
hóa theo `S`), cố ý đặt rộng để chấp nhận rung tay tự nhiên.

M3 và M4 **không cần arming** vì đã ở sẵn trong M2.

### 5.2 Phản hồi cho người dùng

- **Chỉ bằng hình ảnh.** Không rung, không âm thanh, không toast, không log che
  nội dung màn hình.
- Một **icon nhỏ ở góc màn hình** (mặc định góc trên bên phải, gần khu vực
  camera trước), kích thước khoảng 24dp, bán trong suốt:
  - trong lúc ARMING: icon mờ dần hiện lên / vòng tròn chạy 500ms;
  - khi đã vào chế độ: icon rõ, **hình dạng khác nhau cho từng chế độ** (M1 / M2 / M5)
    để người dùng liếc là biết đang ở chế độ nào;
  - khi hủy: icon tắt.
- Con trỏ ở M2 là phản hồi riêng, luôn hiện khi M2 đang bật.
- Icon và con trỏ **không nhận sự kiện chạm** (cờ `FLAG_NOT_TOUCHABLE`), tránh
  chặn thao tác thật của người dùng.

## 6. Bảng ngưỡng khởi điểm

Toàn bộ nằm trong `gesture/GestureThresholds.kt`. `R_*`, `G_*`, `T_*` đã được đo
thật ở Phase 1 (140 lần ghi trên RMX3370/GT Neo 2, xem `docs/DECISIONS.md` ngày
2026-09-20). Các hằng số vận tốc/thời gian còn lại vẫn là phỏng đoán, chưa đo
được vì cần chuyển động thật (Phase 2/3), không phải tư thế tĩnh.

| Hằng số | Giá trị | Ghi chú |
|---|---|---|
| `R_UP` | 0.90 | ngón dựng (đo thật; đoán ban đầu 1.6 sai hoàn toàn) |
| `R_DOWN` | 0.65 | ngón gập (đo thật; đoán ban đầu 1.1 sai hoàn toàn) |
| `G_CLOSE` | 1.3 | hai ngón khép (đo thật, khớp đoán ban đầu) |
| `G_OPEN` | 1.85 | hai ngón tách (đo thật, hạ nhẹ so với đoán 2.0) |
| `G_OPEN4` | 2.0 | tư thế vào M5 (đo thật, nâng so với đoán 1.7) |
| `T_OUT` | 0.62 | ngón cái xòe (đo thật; đoán ban đầu 1.1 không bao giờ đạt tới) |
| `T_IN` | 0.45 | ngón cái khép, dùng khi chưa có t0 (đo thật) |
| `T_CLOSE_RATIO` | 0.6 | khép a = 60% của t0 (giữ nguyên đoán, mang tính tỷ lệ) |
| `ARM_HOLD_MS` | 500 | thời gian giữ yên để kích hoạt — **chưa đo** |
| `ARM_JITTER` | 0.15 | dịch chuyển cho phép khi arming (theo S) — **chưa đo** |
| `CLICK_MAX_MS` | 600 | tách ngắn hơn = click, dài hơn = hold (tăng từ 300 ngày 2026-09-20 vì chưa từng click được — nghi do độ trễ vote 4/5 khung) — **vẫn chưa đo thật** |
| `CLICK_COOLDOWN` | 250ms | **chưa đo** |
| `SWIPE_VEL_MIN_UP` | 0.15 | vận tốc tối thiểu hướng Lên — tăng mạnh độ nhạy theo yêu cầu người dùng 2026-09-20 |
| `SWIPE_VEL_MIN_DOWN` | 0.4 | vận tốc tối thiểu hướng Xuống — giữ nguyên, đo thật 2026-09-20 |
| `SWIPE_VEL_MIN_LEFT_RIGHT` | 0.3 | vận tốc tối thiểu Trái/Phải (đã thử 0.8, 0.65 — vẫn quá khó; sau khi tách tư thế 3 ngón riêng thì hết cần ngưỡng cao để phòng lẫn hướng, hạ xuống dưới cả DOWN vì tư thế 3 ngón tự nó khó vẫy nhanh — 2026-09-20) |
| `VELOCITY_WINDOW_MS` | 120ms | cửa sổ tính vận tốc (làm mịn dtMs dao động giữa các khung) — **đoạn ban đầu, chưa đo kỹ** |
| `SWIPE_REST_VEL_MAX` | SWIPE_VEL_MIN / 3 | ngưỡng "đứng yên" để cho vuốt tiếp (yêu cầu người dùng 2026-09-20) — **đoạn ban đầu, chưa đo** |
| `SYS_VEL_MIN` | ~1.5 × SWIPE_VEL_MIN | ngưỡng cho M5 — **chưa đo** |
| `SWIPE_COOLDOWN` | 800ms | thời gian nghỉ tối thiểu (tăng từ 500ms theo yêu cầu người dùng 2026-09-20), cộng thêm điều kiện đứng yên ở trên |
| `AXIS_RATIO` | 2.2 | trục chính phải trội hơn trục phụ (đã thử 1.8 rồi 1.5, cuối cùng tăng lên 2.2 vì Trái/Phải dễ lẫn sang Lên/Xuống khi ngưỡng vận tốc mỗi hướng khác nhau — 2026-09-20) |
| `CURSOR_GAIN` | 150 (dp/đơn vị) | hệ số khuếch đại con trỏ (trục X ở chế độ theo hướng ngón trỏ, cả 2 trục ở chế độ theo tay) — chỉnh qua màn hình Cài đặt (2026-09-20), **chưa đo trên máy thật** |
| `CURSOR_POINTING_VERTICAL_STEP` | 8 (dp/khung) | bước di chuyển Lên/Xuống mỗi khung khi giữ trạng thái b,c tương ứng (chế độ theo hướng ngón trỏ, 2026-09-20) — **chưa đo** |
| `LOST_HAND_MS` | 300 | mất tay bao lâu thì coi là ra khỏi khung — **chưa đo** |
| `HOLD_LOST_MS` | 300 | mất tay khi đang hold → nhả — **chưa đo** |
| `HOLD_MAX_MS` | 15000 | hold tối đa tuyệt đối — **chưa đo** |
| `EDGE_MARGIN` | 5% | tay chạm lề khung = coi như ra ngoài — **chưa đo** |
| `CONF_MIN` | 0.6 | bỏ qua khung hình có độ tin cậy thấp (giữ nguyên đoán, phù hợp dữ liệu đo được) |

**Rủi ro đã biết từ dữ liệu thật**: ngón cái (`t`) không tách biệt rõ giữa tư thế
khép (M1, t≈0.43–0.53) và xòe hết cỡ (M2, t≈0.62–0.75) — khoảng cách hẹp hơn
nhiều so với các trục khác (`r`, `g_bc` tách biệt rất rõ). Đã thử ghi lại M2 với
ngón cái xòe cố tình xa hơn nhưng không cải thiện được nhiều. Nếu thực tế dùng
bị lẫn M1/M2, cân nhắc ghi vào `BACKLOG.md` để xem xét bộ phân loại riêng cho
ngón cái, như mục 8 đã lường trước.

## 7. Giao diện app

Tối giản, đúng một màn hình:

1. Nút bật/tắt lớn ở giữa.
2. Ba dòng trạng thái: quyền camera, Accessibility đã bật chưa, tối ưu pin đã tắt chưa —
   mỗi dòng có nút dẫn thẳng tới đúng trang cài đặt.
3. Link mở màn hình Debug (xem ROADMAP Phase 1) và màn hình hướng dẫn cử chỉ.
4. **Quick Settings Tile** để bật/tắt nhanh từ thanh kéo xuống.

Không có onboarding nhiều bước, không có tài khoản, không có cài đặt phức tạp.
Chỉ có: hệ số con trỏ, chọn tay thuận, và bật/tắt từng nhóm cử chỉ.

## 8. Rủi ro đã biết

| Rủi ro | Ảnh hưởng | Cách giảm |
|---|---|---|
| b,c khép sát → MediaPipe lẫn landmark hai ngón | cao | bỏ khung độ tin cậy thấp, dùng world landmarks, lấy đa số nhiều khung |
| Nhận nhầm cử chỉ hệ thống (M5) | cao | ngưỡng vận tốc cao, một hành động mỗi lần vào chế độ |
| Kẹt trạng thái hold | rất cao | ba lớp timeout ở §4.4 |
| Kéo (drag) giật vì phải nối nhiều đoạn `dispatchGesture` | trung bình | chấp nhận ở bản đầu, đo lại sau |
| Realme UI kill service | trung bình | hướng dẫn tắt tối ưu pin trong app |
| Thiếu sáng làm mất tracking | trung bình | ghi nhận, chưa xử lý ở bản đầu |
| Mỏi tay khi dùng lâu | thấp | bản chất của thiết kế, đã chấp nhận vì dùng ngắn |
