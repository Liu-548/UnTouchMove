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

### 4.1 M1 — Lướt 2 ngón

- **Tư thế vào**: b và c dựng, khép nhau (`g_bc ≤ G_CLOSE`); a, d, e gập.
- **Thao tác**: vẩy bàn tay theo hướng muốn lướt (lên / xuống / trái / phải).
  Một cú vẩy vượt ngưỡng vận tốc = một cú vuốt được bơm ra hệ thống.
- **Hủy**: tách b và c (`g_bc ≥ G_OPEN`), hoặc tay ra khỏi khung.
- **Chống vuốt ngược**: sau mỗi cú vuốt có thời gian nghỉ `SWIPE_COOLDOWN`,
  trong thời gian đó mọi chuyển động bị bỏ qua (để người dùng đưa tay về chỗ cũ).
- **Hướng**: chỉ tính khi thành phần theo một trục lớn hơn trục kia ít nhất
  `AXIS_RATIO` lần; nếu không thì bỏ qua (chuyển động chéo không tính).

### 4.2 M2 — Con trỏ ảo

- **Tư thế vào**: a, b, c dựng; b và c khép nhau (`g_bc ≤ G_CLOSE`);
  a xòe (`t ≥ T_OUT`); d, e gập.
- **Khi vào chế độ, lưu lại `t0 = t` tại thời điểm đó** làm mốc cá nhân hóa.
- **Di chuyển con trỏ**: theo **chuyển động tương đối** của điểm neo, nhân hệ số
  `CURSOR_GAIN`, qua bộ lọc thích nghi (One Euro filter).
  - **Điểm neo là `P` (tâm lòng bàn tay), KHÔNG phải đầu ngón.** Lý do: khi b,c
    tách ra để click hoặc hold, đầu ngón dịch chuyển và sẽ kéo con trỏ lệch đi.
  - Chuyển động tương đối cho phép "nhấc tay đặt lại" như dùng chuột, và khi tay
    tạm mất tracking rồi quay lại, con trỏ không nhảy vị trí.
- **Hủy**: khép cả a, b, c (`t ≤ t0 × T_CLOSE_RATIO` **và** `g_bc ≤ G_CLOSE`),
  hoặc tay ra khỏi khung > 300ms.
- **Con trỏ hiển thị**: chấm tròn nhỏ bán trong suốt, vẽ bằng overlay của
  AccessibilityService.

### 4.3 M3 — Click

Chỉ tồn tại khi đang ở M2.

- **Điều kiện**: a vẫn xòe (`t ≥ T_OUT`), b và c **tách ra rồi khép lại** trong
  vòng `CLICK_MAX_MS`.
- **Tọa độ click** lấy tại **thời điểm bắt đầu tách**, không phải lúc khép lại.
- Sau khi click có cooldown `CLICK_COOLDOWN` để tránh nhân đôi.

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

Toàn bộ nằm trong `gesture/GestureThresholds.kt`. **Đây là phỏng đoán**, phải đo
lại bằng công cụ ở Phase 1 trước khi tin dùng.

| Hằng số | Giá trị khởi điểm | Ghi chú |
|---|---|---|
| `R_UP` | 1.6 | ngón dựng |
| `R_DOWN` | 1.1 | ngón gập |
| `G_CLOSE` | 1.3 | hai ngón khép |
| `G_OPEN` | 2.0 | hai ngón tách |
| `G_OPEN4` | 1.7 | tư thế vào M5 (rời vừa phải) |
| `T_OUT` | 1.1 | ngón cái xòe |
| `T_IN` | 0.7 | ngón cái khép (dùng khi chưa có t0) |
| `T_CLOSE_RATIO` | 0.6 | khép a = 60% của t0 |
| `ARM_HOLD_MS` | 500 | thời gian giữ yên để kích hoạt |
| `ARM_JITTER` | 0.15 | dịch chuyển cho phép khi arming (theo S) |
| `CLICK_MAX_MS` | 300 | tách ngắn hơn = click, dài hơn = hold |
| `CLICK_COOLDOWN` | 250ms | |
| `SWIPE_VEL_MIN` | cần đo | vận tốc tối thiểu cho M1 |
| `SYS_VEL_MIN` | ~1.5 × SWIPE_VEL_MIN | ngưỡng cho M5 |
| `SWIPE_COOLDOWN` | 500ms | chống vuốt ngược |
| `AXIS_RATIO` | 1.8 | trục chính phải trội hơn trục phụ |
| `CURSOR_GAIN` | 2.0 | hệ số khuếch đại, cho chỉnh trong cài đặt |
| `LOST_HAND_MS` | 300 | mất tay bao lâu thì coi là ra khỏi khung |
| `HOLD_LOST_MS` | 300 | mất tay khi đang hold → nhả |
| `HOLD_MAX_MS` | 15000 | hold tối đa tuyệt đối |
| `EDGE_MARGIN` | 5% | tay chạm lề khung = coi như ra ngoài |
| `CONF_MIN` | 0.6 | bỏ qua khung hình có độ tin cậy thấp |

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
