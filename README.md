# UnTouchMove

Điều khiển điện thoại Android **không chạm** bằng cử chỉ tay qua camera trước,
hoạt động trên **mọi ứng dụng**.

Dự án cá nhân, không thương mại. Lấy cảm hứng từ tính năng Air Gestures của
realme nhưng không bị giới hạn ở một vài app được hỗ trợ sẵn.

> Trạng thái: **đang phát triển, chưa dùng được.** Xem `docs/ROADMAP.md`.

## Bộ cử chỉ

Ký hiệu ngón: a = cái, b = trỏ, c = giữa, d = áp út, e = út.

| Chế độ | Tư thế vào | Thao tác | Hủy |
|---|---|---|---|
| Lướt | b, c dựng và khép | vẩy 4 hướng để cuộn | tách b, c |
| Con trỏ | a, b, c dựng; b,c khép; a xòe | di chuyển tay | khép cả 3 ngón |
| Click | (đang ở chế độ con trỏ) | tách b,c rồi khép ngay | — |
| Kéo | (đang ở chế độ con trỏ) | tách b,c và giữ, rồi di chuyển | khép b,c |
| Hệ thống | b, c, d, e dựng và rời; a gập | vẩy: trái=back, phải=đa nhiệm, lên=home, xuống=thông báo | khép 4 ngón |
| Nghỉ | mở cả 5 ngón | không kích hoạt gì | — |

Mọi chế độ đều cần giữ tay yên nửa giây để kích hoạt, tránh nhận nhầm. Khi kích
hoạt, một icon nhỏ hiện ở góc màn hình — không rung, không âm thanh.

## Riêng tư

- **Không có quyền truy cập mạng.** App không khai báo quyền INTERNET.
- Mô hình nhận diện tay chạy hoàn toàn trên máy, đóng gói sẵn trong APK.
- **Không lưu, không gửi đi bất kỳ khung hình nào.**
- Camera chỉ chạy khi bạn tự bật, và tự tắt khi màn hình tắt.

App cần quyền Camera và quyền Trợ năng (Accessibility). Quyền Trợ năng chỉ được
dùng để gửi thao tác chạm/vuốt giả lập, không dùng để đọc nội dung màn hình.

## Yêu cầu

- Android 10 (API 29) trở lên, có camera trước
- Máy mục tiêu: Realme GT Neo 2 (mốc dưới về hiệu năng), Realme GT 8 Pro

## Build

```bash
./gradlew assembleDebug
```

Cần JDK 17 và Android SDK. Model `hand_landmarker.task` đã nằm trong `app/src/main/assets/`.

Sau khi cài, cần bật thủ công:
1. Cấp quyền Camera.
2. Bật UnTouchMove trong Cài đặt → Trợ năng.
   Nếu cài từ file APK, Android 13+ yêu cầu vào thông tin app → menu ba chấm →
   "Allow restricted settings" trước.
3. Tắt tối ưu pin cho app (realme UI hay tự dừng service nền).

## Tài liệu

- `docs/SPEC.md` — đặc tả cử chỉ và ngưỡng
- `docs/ARCHITECTURE.md` — kiến trúc kỹ thuật
- `docs/ROADMAP.md` — kế hoạch theo giai đoạn
- `docs/BACKLOG.md` — tính năng hoãn lại
- `docs/DECISIONS.md` — nhật ký quyết định kỹ thuật
- `CLAUDE.md` — chỉ dẫn cho Claude Code

## Giấy phép

MIT. Xem `LICENSE`.

Dự án dùng MediaPipe của Google (Apache License 2.0).
