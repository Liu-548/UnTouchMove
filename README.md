# UnTouchMove

Điều khiển điện thoại Android **không chạm** bằng cử chỉ tay qua camera trước,
hoạt động trên **mọi ứng dụng**.

Dự án cá nhân, không thương mại. Lấy cảm hứng từ tính năng Air Gestures của
realme nhưng không bị giới hạn ở một vài app được hỗ trợ sẵn.

> Trạng thái: **đang phát triển, đã dùng được cơ bản trên máy thật** (test
> trên Realme GT Neo 2) — vuốt, con trỏ/click/giữ-kéo, cử chỉ hệ thống 4
> ngón đều chạy. Một số ngưỡng độ nhạy vẫn đang tinh chỉnh dần. Xem
> `docs/ROADMAP.md`.

## Bộ cử chỉ

Ký hiệu ngón: a = cái, b = trỏ, c = giữa, d = áp út, e = út.

| Chế độ | Tư thế vào | Thao tác | Hủy |
|---|---|---|---|
| Lướt 2 ngón | b, c dựng và khép; d, e gập | vẩy Lên/Xuống | đổi tư thế khác hoặc thả lỏng tay |
| Lướt 3 ngón | b, c, d dựng và khép; e gập | vẩy Trái/Phải | đổi tư thế khác hoặc thả lỏng tay |
| Con trỏ | a, b, c dựng; b,c khép; a xòe; d,e gập | di chuyển tay (hoặc theo hướng ngón trỏ, đổi trong Cài đặt) | **chỉ** xòe cả 5 ngón |
| Click | (đang ở chế độ con trỏ) | tách b,c rồi khép lại nhanh | — |
| Giữ và kéo | (đang ở chế độ con trỏ) | tách b,c giữ lâu hơn click, rồi di chuyển | khép b,c lại; tự nhả nếu mất tay hoặc giữ quá lâu |
| Hệ thống — tư thế khép | b,c,d,e dựng, khép sát nhau; a gập | vẩy: trái=Back, phải=Đa nhiệm | tách 4 ngón ra |
| Hệ thống — tư thế tách | b,c,d,e dựng, tách rời nhau; a gập | vẩy: trái=Home, phải=Thông báo | khép 4 ngón lại |
| Nghỉ | mở cả 5 ngón | không kích hoạt gì, luôn thoát mọi chế độ | — |

Mọi chế độ đều cần giữ tay yên nửa giây để kích hoạt, tránh nhận nhầm. Khi kích
hoạt, một icon nhỏ hiện ở góc màn hình — không rung, không âm thanh. Từng nhóm
cử chỉ (Lướt / Con trỏ / Hệ thống) có thể bật/tắt riêng trong màn hình Cài đặt.

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
