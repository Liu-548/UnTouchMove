# BACKLOG — những gì cố ý hoãn

Đã cân nhắc và quyết định **không làm ở bản đầu**. Đừng tự ý kéo lên làm sớm.

---

---

## Đã chốt hoãn

### Chuyển chế độ giữa chừng
Hiện tại muốn đổi từ M2 sang M1 phải hủy hoặc đưa tay ra khỏi khung rồi vào lại.
Cho phép chuyển thẳng sẽ tiện hơn nhưng dễ gây nhấp nháy giữa hai chế độ khi ngón
cái ở vùng mờ. Nếu làm: cần thời gian trễ khi chuyển và hysteresis chặt cho ngón cái.

### Bản Windows dùng webcam
Logic tính trên landmark đã chuẩn hóa nên **tái dùng được gần như nguyên vẹn**.
Khác biệt cần xử lý: webcam có góc nhìn, khoảng cách và độ phân giải khác nên
phải hiệu chuẩn lại toàn bộ ngưỡng; thay AccessibilityService bằng API chuột/bàn
phím của Windows. Nên tách phần `gesture/` thành module dùng chung ngay từ đầu
để sau này đỡ phải viết lại — nhưng **chỉ tách khi thực sự bắt đầu làm Windows**,
đừng trừu tượng hóa sớm.

### Chạy nền full-time / tự đánh thức
Chủ dự án chỉ cần bật thủ công. Nếu sau này muốn: tham khảo cách dùng cảm biến
tiệm cận và gia tốc để đánh thức camera, hạ fps, giới hạn khung giờ hoạt động.

### Tự khởi động cùng máy
Không làm. Android 14 còn chặn khởi động foreground service camera từ
`BOOT_COMPLETED`.

### Nhận diện hai tay
`numHands = 1` cho tới khi có lý do rõ ràng. Hai tay làm tăng gấp đôi chi phí
tính toán và mở ra cả một lớp nhập nhằng mới.

### Tùy biến ánh xạ cử chỉ
Người dùng tự đổi "vẩy trái = gì". Hay, nhưng cần UI phức tạp. Sau.

---

## Ý tưởng chưa quyết

- **Hút con trỏ vào nút gần nhất** bằng cách đọc cây node của Accessibility rồi
  `performAction` thay vì click theo tọa độ. Chính xác hơn nhiều với app thường,
  nhưng vô dụng với game, WebView và trình phát video, đồng thời cần quyền đọc
  nội dung màn hình — đi ngược nguyên tắc riêng tư ở CLAUDE.md §4.6. Cân nhắc kỹ.
- **Bộ phân loại TFLite nhỏ** thay cho ngưỡng thủ công, nếu Phase 1 cho thấy các
  cụm đặc trưng chồng lấn nhiều.
- **Công thức đo ngón cái (`t`) tách biệt kém**: dữ liệu Phase 1 (2026-09-20) cho
  thấy `t = dist(đầu cái, gốc trỏ)/S` chỉ tách được tư thế khép (M1, t≈0.43-0.53)
  và xòe hết cỡ (M2, t≈0.62-0.75) một khoảng ~0.1 — hẹp hơn nhiều so với `r`
  (~0.4). Lý do: ngón cái xoay ở khớp gốc theo 2 trục (kiểu yên ngựa) chứ không
  gập một mặt phẳng như 4 ngón kia, nên phần chuyển động chính khi "xòe" lại rơi
  vào trục chiều sâu (z) — trục camera đơn ước lượng kém chính xác nhất. **Tạm
  thời chấp nhận ngưỡng đo được** (`T_OUT=0.62`, `T_IN=0.45`, xem
  `DECISIONS.md`), dùng thử thật trên máy trước. Nếu thực tế M1/M2 hay bị lẫn,
  nghiên cứu lại công thức (đo góc thay vì khoảng cách, hoặc dùng riêng trục
  ngang thay vì khoảng cách 3D) thay vì chỉnh ngưỡng.
- **Cử chỉ chủ động thoát M2**: đã bỏ hẳn điều kiện huỷ M2 theo khép ngón cái
  (`t0`, 2026-09-20) đúng vì lý do ở mục ngay trên — đo góc xòe ngón cái quá
  nhiễu, khiến M2 tự huỷ/vào lại thất thường. Hiện tại M2 chỉ thoát khi tay ra
  khỏi khung hoặc xoè cả 5 ngón. Nếu người dùng thấy bất tiện (phải rút tay ra
  mới đổi được chế độ), cần nghĩ cách khác để thoát chủ động — có thể chờ công
  thức đo ngón cái tốt hơn (mục trên) thay vì thêm điều kiện tạm bợ.
- **Lướt liên tục theo quãng đường tay** (kiểu touchpad) thay vì vẩy từng cú.
  Có thể mượt hơn cho việc đọc bài dài.
- **Hiệu chuẩn cá nhân một lần**: bảo người dùng làm từng tư thế một lần lúc cài
  đặt để tự sinh ngưỡng riêng, thay vì dùng ngưỡng cố định.
- Chế độ tiết kiệm pin hạ fps khi tay đứng yên lâu.
- Hỗ trợ SmartTV / Android TV.
- **M4 Hold/kéo cần cải thiện thêm** (người dùng 2026-09-20: "tạm thời cứ để
  vậy, note cần cải thiện giai đoạn sau" sau khi test thật thấy "không thực
  sự tốt"). Đã sửa được: con trỏ chạm hết viền màn hình (lỗi dùng nhầm
  `EDGE_MARGIN`), thêm màu con trỏ theo trạng thái để tự chẩn đoán, lọc
  tách-khép quá nhanh (`CLICK_MIN_MS`), phân biệt gập ngón/tách ngón, thêm
  thanh trượt độ nhạy tách ngón (`G_OPEN`). Còn CHƯA đo/xác nhận: cảm giác
  kéo có mượt không (`HOLD_STEP_DURATION_MS=80ms` là số đoán), độ trễ
  `continueStroke` nối tiếp có chấp nhận được không, và liệu thanh trượt độ
  nhạy tách ngón mới thêm có thực sự giải quyết được vấn đề "tay rơi khỏi
  khung hình" hay cần hướng khác (vd đổi cách đo `gBC`, hoặc cho phép "tách"
  bằng cử chỉ khác không cần xoè ngón rộng). Xem chi tiết các quyết định
  ngày 2026-09-20 trong `DECISIONS.md`.

---

## Đã loại bỏ

- **Chụm ngón (pinch) để click** — đã thay bằng khép/tách, vì chụm làm đầu ngón
  dịch chuyển đúng lúc click và kéo con trỏ lệch đi.
- **Neo con trỏ vào đầu ngón trỏ** — cùng lý do trên, đã đổi sang tâm lòng bàn tay.
- **Đo khép/tách bằng khoảng cách pixel thô** — phụ thuộc cỡ tay và khoảng cách
  tới camera, đã đổi sang tỷ lệ chia cho khoảng cách hai khớp gốc.
