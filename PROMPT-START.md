# Câu lệnh mở đầu cho Claude Code

Mở terminal tại `C:\Game\UnTouchMove`, chạy `claude`, rồi dán nguyên đoạn dưới:

---

Đọc CLAUDE.md, docs/SPEC.md, docs/ARCHITECTURE.md và docs/ROADMAP.md trước khi
làm bất cứ việc gì.

Sau đó bắt đầu Phase 0 trong ROADMAP: dựng project Android Kotlin + Compose,
minSdk 29 / targetSdk 36, thêm CameraX và MediaPipe Tasks Vision, cấu hình
manifest đúng theo ràng buộc ở CLAUDE.md mục 4, và làm sao cho app build được
và hiện được preview camera trước.

Nêu kế hoạch ngắn gọn trước khi code. Đừng hỏi tôi câu hỏi kỹ thuật, tự quyết và
ghi lại vào docs/DECISIONS.md. Xong thì nói rõ tôi cần test gì trên máy thật.

---

## Mẹo dùng cho các phiên sau

- Mỗi phiên làm **một phase**, đừng gộp. Hết phase thì `/clear` rồi bắt đầu phiên mới.
- Mở đầu phiên mới: "Đọc CLAUDE.md và docs/ROADMAP.md, rồi làm Phase N."
- Khi Claude đề xuất thêm tính năng ngoài SPEC: "Ghi vào BACKLOG, đừng code."
- Khi kết quả sai: mô tả **hiện tượng** ("click bị lệch xuống dưới khoảng 2cm"),
  đừng đoán nguyên nhân giúp nó.
- Phase 1 và Phase 2 là hai phase quyết định dự án có chạy được không. Đừng vội
  bỏ qua để nhảy tới phần cử chỉ cho vui.
