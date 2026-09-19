# DECISIONS — nhật ký quyết định kỹ thuật

Mỗi khi chọn giữa nhiều phương án, ghi lại vào đây một dòng: ngày, quyết định,
lý do, và phương án đã loại. Mục đích là để sau này không phải tranh luận lại
cùng một chuyện, và để biết tại sao code lại như vậy.

Format:

```
## YYYY-MM-DD — <tiêu đề ngắn>
**Chọn**: ...
**Vì**: ...
**Đã loại**: ... (vì ...)
```

---

## 2026-09-19 — Đo khép/tách bằng tỷ lệ, không bằng khoảng cách thô
**Chọn**: `g = dist(tip_x, tip_y) / dist(mcp_x, mcp_y)`
**Vì**: tự chuẩn hóa theo cỡ tay và khoảng cách tới camera; khép thì g ≈ 1.
**Đã loại**: khoảng cách pixel giữa hai đầu ngón (phụ thuộc cỡ tay, phải hiệu
chuẩn riêng cho từng người và từng khoảng cách cầm máy).

## 2026-09-19 — Neo con trỏ vào tâm lòng bàn tay
**Chọn**: điểm neo P = trung bình các landmark 0, 5, 9, 13, 17.
**Vì**: khi b,c tách ra để click hoặc hold, đầu ngón dịch chuyển và sẽ kéo con
trỏ lệch đi đúng lúc người dùng đang nhắm.
**Đã loại**: neo vào đầu ngón trỏ (landmark 8).

## 2026-09-19 — Khép/tách thay cho chụm ngón
**Chọn**: dùng khép/tách ngón làm cơ chế chính.
**Vì**: chụm gây cùng một vấn đề xê dịch con trỏ, và chụm 5 ngón khó nhận ổn định
hơn do các đầu ngón che nhau.
**Đã loại**: chụm cái–trỏ, chụm 5 ngón.

## 2026-09-19 — Ngón cái đo tương đối theo t0
**Chọn**: lưu `t0` lúc vào chế độ con trỏ, coi "khép a" là `t ≤ 0.6 × t0`.
**Vì**: cấu trúc bàn tay khiến ngón cái không bao giờ thực sự sát ngón trỏ, nên
ngưỡng tuyệt đối sẽ khác nhau rõ rệt giữa từng người.
**Đã loại**: ngưỡng tuyệt đối cố định.

## 2026-09-19 — minSdk 29
**Chọn**: minSdk 29, targetSdk 36.
**Vì**: `continueStroke` (cho thao tác kéo) cần API 26; cả hai máy mục tiêu đều
cao hơn 29 nên không cần hỗ trợ thấp hơn.

## 2026-09-19 — AGP 8.7.3 thay vì bản mới nhất
**Chọn**: Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Gradle wrapper 9.5.0.
**Vì**: AGP 9.3.3 (bản mới nhất có sẵn) đã đổi sang "built-in Kotlin support",
xung đột extension `kotlin` khi áp thêm plugin `org.jetbrains.kotlin.android`
riêng (lỗi `Cannot add extension with name 'kotlin'`), và không tương thích
ngược với cách khai DSL hiện tại. AGP 8.7.3 dùng mô hình plugin Kotlin tách
biệt truyền thống, ổn định, không bị lỗi này.
**Đã loại**: AGP 9.3.3 (lỗi extension), giữ nguyên `android.builtInKotlin=true`
mặc định của 9.3.3 (thử tắt bằng `android.builtInKotlin=false` cũng lỗi khác —
cast exception giữa `ApplicationExtensionImpl` và `BaseExtension`).
**Ghi chú**: AGP 8.7.3 chỉ test chính thức tới compileSdk 35, cảnh báo khi dùng
compileSdk 36 — đã tắt cảnh báo bằng `android.suppressUnsupportedCompileSdk=36`
trong `gradle.properties`. Build thực tế vẫn ra APK bình thường. Cân nhắc nâng
AGP khi có bản ổn định hỗ trợ chính thức compileSdk 36 mà không đổi mô hình
Kotlin plugin.

## 2026-09-19 — Tải sẵn hand_landmarker.task vào assets lúc dựng khung
**Chọn**: tải model MediaPipe HandLandmarker (float16) trực tiếp từ
`storage.googleapis.com/mediapipe-models` vào `app/src/main/assets/` ngay ở
Phase 0, đóng gói trong APK.
**Vì**: ROADMAP Phase 0 yêu cầu, và model chạy hoàn toàn offline trong máy —
đây là tải lúc build (máy dev), không phải app tự gọi mạng lúc chạy, nên không
vi phạm ràng buộc "không xin quyền INTERNET" ở CLAUDE.md mục 4.1.
**Đã loại**: để trống, tải thủ công sau — sẽ chặn Phase 1 (DebugActivity cần
model để chạy HandLandmarker).
