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
