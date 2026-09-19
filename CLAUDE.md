# CLAUDE.md — UnTouchMove

File này là chỉ dẫn bắt buộc cho Claude Code khi làm việc trong repo này.
Đọc file này trước mọi thay đổi. Khi có mâu thuẫn, thứ tự ưu tiên:
`CLAUDE.md` > `docs/SPEC.md` > `docs/ARCHITECTURE.md` > phần còn lại.

---

## 1. Dự án là gì

UnTouchMove là app Android điều khiển điện thoại **không chạm**, bằng cử chỉ tay
qua **camera trước**, hoạt động trên **mọi ứng dụng** nhờ AccessibilityService.

Gồm 3 nhóm cử chỉ: lướt 2 ngón, con trỏ ảo 3 ngón (kèm click / hold-drag),
và điều khiển hệ thống 4 ngón. Bật/tắt **thủ công**, không chạy full-time.

Đặc tả đầy đủ về cử chỉ: `docs/SPEC.md`. Không tự suy diễn ngoài đặc tả.

## 2. Bối cảnh làm việc — ĐỌC KỸ

Chủ dự án **không viết code**, chỉ duyệt kết quả và test trên máy thật.
Vì vậy:

- **Không hỏi chủ dự án các câu hỏi kỹ thuật** kiểu "nên dùng Hilt hay Koin".
  Tự quyết theo `docs/ARCHITECTURE.md`, ghi lại quyết định vào `docs/DECISIONS.md`.
- **Chỉ hỏi khi cần thông tin mà chỉ con người mới có**: kết quả test trên máy
  thật, cảm giác dùng có mượt không, ngưỡng nào thấy dễ chịu hơn.
- **Báo cáo bằng ngôn ngữ người dùng cuối**: "cử chỉ click hiện bị nhận nhầm khi
  tay nghiêng" chứ không phải "threshold g_bc vượt hysteresis band".
- Mọi phản hồi và comment trong code viết bằng **tiếng Việt**. Tên biến, tên hàm,
  tên file, commit message viết bằng **tiếng Anh**.

## 3. Nguyên tắc code

- **Đơn giản trước, tối ưu sau.** Không thêm abstraction cho tính năng chưa có.
  Không tạo interface khi chỉ có một implementation.
- **Không thêm thư viện ngoài** nếu thư viện chuẩn hoặc AndroidX làm được.
  Mọi dependency mới phải ghi lý do vào `docs/DECISIONS.md`.
- **Không tự ý thêm tính năng** ngoài `docs/SPEC.md`. Ý tưởng mới thì ghi vào
  `docs/BACKLOG.md`, không code.
- Mọi hằng số ngưỡng cử chỉ phải nằm trong **một file duy nhất**
  (`gesture/GestureThresholds.kt`), không rải rác. Xem SPEC mục 6.
- File nào dài quá ~300 dòng thì tách. Hàm nào dài quá ~50 dòng thì tách.
- Khi cố ý làm tắt / hoãn việc gì, để lại comment `// TODO(untouch):` kèm lý do.

## 4. Ràng buộc kỹ thuật KHÔNG ĐƯỢC VI PHẠM

1. **Không xin quyền INTERNET.** App chạy hoàn toàn offline. Model MediaPipe
   đóng gói sẵn trong APK. Không analytics, không upload, không lưu khung hình
   ra file dưới bất kỳ hình thức nào.
2. **Không dùng SYSTEM_ALERT_WINDOW** (quyền vẽ đè). Con trỏ và icon trạng thái
   vẽ bằng `TYPE_ACCESSIBILITY_OVERLAY` từ AccessibilityService.
3. **Camera chỉ chạy khi người dùng chủ động bật** và màn hình đang mở khóa.
   Từ Android 14, không được tạo foreground service loại `camera` khi app đang ở
   nền. Do đó việc khởi động phải đi qua một Activity trong suốt (xem ARCHITECTURE).
4. **Không tự khởi động cùng máy.** Không đăng ký `BOOT_COMPLETED`.
5. **Không bao giờ để màn hình kẹt ở trạng thái đang nhấn.** Mọi cử chỉ hold
   phải có timeout tuyệt đối và phải nhả ngay khi mất tay. Đây là lỗi nghiêm
   trọng nhất có thể xảy ra với app này.
6. AccessibilityService **chỉ dùng để gửi cử chỉ**, không đọc nội dung màn hình,
   không nghe `AccessibilityEvent` trừ khi SPEC yêu cầu rõ.

## 5. Quy trình làm việc mỗi lần được giao task

1. Đọc `docs/SPEC.md` phần liên quan và `docs/ROADMAP.md` để biết đang ở phase nào.
2. Nêu ngắn gọn kế hoạch (3–6 gạch đầu dòng) trước khi code.
3. Code theo phạm vi task, không mở rộng.
4. Chạy `./gradlew assembleDebug` để chắc chắn build được.
5. Cập nhật `docs/ROADMAP.md` (đánh dấu xong) và `docs/DECISIONS.md` (nếu có
   quyết định kỹ thuật mới).
6. Báo cáo: đã làm gì, cần test thủ công cái gì trên máy thật.

**Không tự commit và push** trừ khi được yêu cầu rõ ràng. Khi được yêu cầu, dùng
commit message dạng `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`.

## 6. Test

Không có CI, không yêu cầu độ phủ test. Nhưng **bắt buộc** có unit test thuần
Kotlin (không cần thiết bị) cho:

- `FeatureExtractor` — tính r, g, t từ bộ landmark cho sẵn.
- `GestureStateMachine` — nạp một chuỗi frame giả lập, kiểm tra trạng thái
  chuyển đúng, đặc biệt là: không kích hoạt khi 5 ngón mở, không click khi chưa
  vào chế độ con trỏ, hold luôn được nhả.

Dữ liệu test lấy từ file log ghi ở Phase 1 (xem ROADMAP).

## 7. Thiết bị mục tiêu

| Máy | Vai trò |
|---|---|
| Realme GT Neo 2 | **mốc dưới** về hiệu năng — phải mượt ở đây |
| Realme GT 8 Pro | máy chính, Android 16 / realme UI 7 |

Realme UI hay kill service nền: app phải hướng dẫn người dùng tắt tối ưu pin.
Android 13+ chặn bật Accessibility cho app cài ngoài Play Store cho tới khi
người dùng mở "Allow restricted settings" — phải có hướng dẫn trong app.

## 8. Những gì KHÔNG làm ở bản đầu

Đã chốt hoãn, đừng tự làm: chuyển chế độ giữa chừng (phải hủy rồi vào lại),
bản Windows/webcam, chạy nền full-time, tự khởi động cùng máy, đa ngôn ngữ,
nhận diện hai tay, tùy biến ánh xạ cử chỉ. Chi tiết: `docs/BACKLOG.md`.
