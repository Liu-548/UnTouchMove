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

- [ ] `PoseClassifier` với hysteresis + vote 4/5 khung
- [ ] `GestureStateMachine`: IDLE → ARMING (500ms giữ yên) → M1
- [ ] Phát hiện vẩy theo vận tốc + cooldown chống vuốt ngược + lọc trục
- [ ] `GestureForegroundService` + thông báo thường trú + nút tắt
- [ ] Icon trạng thái nhỏ ở góc màn hình

**Xong khi**: cuộn được Facebook / YouTube bằng hai ngón tay, không chạm màn hình.
Đây là mốc "dùng được thật", hãy dùng thử vài ngày trước khi làm tiếp.

## Phase 4 — M2 Con trỏ + M3 Click

- [ ] Điểm neo = P, chuyển động tương đối, `CURSOR_GAIN`
- [ ] One Euro filter chống rung
- [ ] Vẽ con trỏ bằng overlay
- [ ] Logic khép a theo `t0` cá nhân hóa để hủy
- [ ] Click: tách rồi khép < 300ms, lấy tọa độ lúc bắt đầu tách

**Xong khi**: bấm trúng một nút cỡ trung bình trong 3 lần thử trở xuống.

## Phase 5 — M4 Hold và kéo

- [ ] Gộp M3/M4 thành một logic: tách = nhấn xuống, khép = nhả
- [ ] Ba lớp timeout an toàn (mất tay / tối đa / onDestroy)
- [ ] Test kỹ: kéo icon màn hình chính, kéo thanh trượt âm lượng

**Xong khi**: kéo được và **không lần nào** làm màn hình kẹt ở trạng thái nhấn.

## Phase 6 — M5 Cử chỉ hệ thống 4 ngón

- [ ] Tư thế vào: b,c,d,e dựng và rời; a gập
- [ ] Ngưỡng vận tốc cao hơn M1, một hành động mỗi lần vào chế độ
- [ ] Xử lý lật gương cho trục trái/phải
- [ ] Hủy bằng khép 4 ngón

## Phase 7 — Hoàn thiện

- [ ] Quick Settings Tile + `LauncherProxyActivity`, test trên cả hai máy
- [ ] Màn hình chính gọn: nút bật/tắt + 3 dòng trạng thái quyền
- [ ] Màn hình hướng dẫn cử chỉ (hình minh họa từng tư thế)
- [ ] Cài đặt: hệ số con trỏ, tay thuận, bật/tắt từng nhóm cử chỉ
- [ ] Unit test cho FeatureExtractor và GestureStateMachine
- [ ] README đầy đủ, LICENSE, push lên Git

## Phase 8 — Sau khi ổn định

Xem `BACKLOG.md`.
