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

## 2026-09-20 — Lật gương xử lý bằng ma trận bitmap trong HandLandmarkerHelper
**Chọn**: trước khi đưa khung hình vào HandLandmarker, xoay theo
`rotationDegrees` rồi lật ngang (`postScale(-1f, 1f, ...)`) bằng `Matrix` trên
Bitmap, ngay trong `HandLandmarkerHelper.detectAsync`.
**Vì**: đây là cách làm chuẩn của mẫu chính thức MediaPipe cho Android, xử lý
đúng một chỗ duy nhất (ARCHITECTURE mục 6) — mọi landmark (normalized lẫn
world) trả ra sau đó đã nhất quán với những gì người dùng thấy trên preview.
**Đã loại**: lật toạ độ landmark sau khi suy luận (dễ rải rác, dễ quên áp dụng
cho world landmarks lẫn normalized landmarks).

## 2026-09-20 — Delegate CPU cho HandLandmarker ở Phase 1
**Chọn**: `Delegate.CPU` thay vì GPU khi khởi tạo HandLandmarker.
**Vì**: chưa có máy thật để kiểm chứng GPU delegate ổn định trên GT Neo 2 / GT
8 Pro; CPU chạy được mọi nơi, ưu tiên đo đạc đúng số liệu hơn là fps cao ở
Phase 1. Đánh dấu `ponytail:` trong code, nâng cấp khi cần thêm fps.
**Đã loại**: GPU delegate ngay từ đầu (rủi ro crash không kiểm chứng được).

## 2026-09-20 — Xuất file CSV qua share sheet (FileProvider)
**Chọn**: file log ghi vào `filesDir/logs/` (bộ nhớ riêng của app, đúng yêu
cầu ROADMAP), kèm nút "Chia sẻ file mới nhất" dùng `FileProvider` +
`Intent.ACTION_SEND` để đưa file CSV ra khỏi máy.
**Vì**: bộ nhớ riêng của app không thể lấy ra qua trình quản lý file thông
thường; nếu không có cách xuất, bước "vẽ phân bố, chọn ngưỡng" của Phase 1 sẽ
bị kẹt (phải bật USB debugging + adb pull, quá phức tạp cho người không viết
code). `FileProvider` là AndroidX core có sẵn, không thêm dependency mới.
**Đã loại**: lưu vào bộ nhớ ngoài (external storage) — không cần thiết, và
vi phạm tinh thần "không lưu khung hình/log ra ngoài" nếu không kiểm soát rõ.

## 2026-09-19 — Tải sẵn hand_landmarker.task vào assets lúc dựng khung
**Chọn**: tải model MediaPipe HandLandmarker (float16) trực tiếp từ
`storage.googleapis.com/mediapipe-models` vào `app/src/main/assets/` ngay ở
Phase 0, đóng gói trong APK.
**Vì**: ROADMAP Phase 0 yêu cầu, và model chạy hoàn toàn offline trong máy —
đây là tải lúc build (máy dev), không phải app tự gọi mạng lúc chạy, nên không
vi phạm ràng buộc "không xin quyền INTERNET" ở CLAUDE.md mục 4.1.
**Đã loại**: để trống, tải thủ công sau — sẽ chặn Phase 1 (DebugActivity cần
model để chạy HandLandmarker).

## 2026-09-20 — Ngưỡng thật thay cho phỏng đoán trong GestureThresholds.kt
**Chọn**: ghi 140 lần (6 tư thế SPEC × 20 lần, cộng 20 lần ghi lại riêng cho
M2 với ngón cái xòe cố tình xa hơn) trên RMX3370 (GT Neo 2), qua DebugActivity.
Từ ~6900 dòng dữ liệu, chọn ngưỡng nằm giữa khoảng trống percentile 10/90 của
từng cặp tư thế đối lập:
- `R_UP=0.90`, `R_DOWN=0.65` (dựng đo được p10=0.92, gập p90=0.54) — đoán ban
  đầu 1.6/1.1 sai hoàn toàn, ngón dựng thật không bao giờ vượt 1.4.
- `G_CLOSE=1.3`, `G_OPEN=1.85` (khép p90=1.30, tách p10=1.88) — gần khớp đoán
  ban đầu.
- `G_OPEN4=2.0` (M5 gmax p10=2.17) — nâng nhẹ so với đoán 1.7 để chắc hơn.
- `T_OUT=0.62`, `T_IN=0.45` (M1-khép p10-p90=0.43-0.53, M2-xòe hết cỡ
  p10-p90=0.62-0.75) — đoán ban đầu 1.1 không bao giờ đạt tới trong 140 lần đo.
**Vì**: SPEC mục 6 ghi rõ các con số ban đầu là phỏng đoán, ROADMAP Phase 1 yêu
cầu thay bằng số đo thật trước khi tin dùng.
**Đã loại**: giữ nguyên đoán ban đầu (sẽ khiến M1/M2/M5 gần như không bao giờ
kích hoạt được, đã xác nhận qua dữ liệu thật — ngón dựng chỉ đạt ~0.85-1.4,
không bao giờ chạm ngưỡng đoán 1.6).
**Rủi ro chưa giải quyết**: `t` (ngón cái) tách biệt kém hơn hẳn `r` và `g_bc`
— khoảng cách giữa cụm khép và cụm xòe chỉ ~0.1 (so với `r` cách nhau ~0.4).
Đã thử ghi lại với ngón cái xòe cố tình xa hơn (`M2_retry`, t p10-p90=
0.62-0.75) nhưng người dùng báo "không thể xòe thêm" — đây có thể là giới hạn
sinh lý của khớp ngón cái, không phải do đo sai. Nếu thực tế dùng bị lẫn M1/M2,
xem `BACKLOG.md` mục "bộ phân loại TFLite nhỏ" (đã lường trước ở SPEC mục 8).
**Ghi chú**: các hằng số vận tốc/thời gian (`SWIPE_VEL_MIN`, `ARM_HOLD_MS`,
`HOLD_MAX_MS`, ...) chưa đo được ở Phase 1 vì cần chuyển động thật (vẫy tay,
giữ/nhả), không phải tư thế tĩnh — giữ nguyên giá trị phỏng đoán của SPEC, để
đo ở Phase 2/3 khi có cử chỉ thật chạy qua AccessibilityService.

## 2026-09-20 — GestureTestActivity bơm thử cử chỉ bằng nút bấm tay (Phase 2)
**Chọn**: dựng `UnTouchAccessibilityService` (dispatchGesture cho vuốt/click/
kéo, performGlobalAction cho back/home/đa nhiệm/thông báo) và `ActionDispatcher`
(singleton tham chiếu yếu, ARCHITECTURE mục 5) để một màn hình test riêng
(`GestureTestActivity`) bấm nút gọi thẳng, chưa gắn camera/GestureStateMachine.
Tham số vuốt/click/kéo (thời lượng, khoảng cách) là giá trị thử nghiệm hợp lý,
chưa phải số đo thật.
**Vì**: ROADMAP Phase 2 yêu cầu kiểm chứng cơ chế bơm cử chỉ hoạt động trên máy
thật trước khi ghép với nhận diện — đây là rủi ro kỹ thuật cao nhất, làm sớm để
biết sớm. Test trên RMX3370: Back/Home/Đa nhiệm/Thanh thông báo xác nhận đúng
bằng mắt (đóng activity, về home, mở đa nhiệm, kéo thanh thông báo); chuỗi 5
đoạn `continueStroke` cho kéo thử chạy hết ~750ms liên tục không lỗi qua log.
**Đã loại**: gắn thẳng nút test vào `DebugActivity` — tách riêng để không phải
bật camera khi chỉ cần test bơm cử chỉ, và tránh trộn lẫn hai mục đích debug
khác nhau (đo đặc trưng tay vs. kiểm tra cơ chế injection).
**Ghi chú**: độ mượt thực tế của `continueStroke` (kéo) và cảm giác vuốt/click
là thứ chỉ đo được qua log/ảnh chụp một phần — cần chủ dự án tự bấm và đánh giá
trên máy khi rảnh, ghi lại ROADMAP Phase 2 mục cuối.

## 2026-09-20 — MainActivity chỉ bind camera của riêng nó khi service chưa chạy
**Chọn**: `FrontCameraPreview` nhận tham số `enabled` (= `!running`), chỉ
`bindToLifecycle` khi service chưa bật; khi service đang chạy thì hiện chữ
thay preview, không giữ camera.
**Vì**: test trên máy thật phát hiện khi bật "Điều khiển không chạm", preview
của MainActivity bị đứng hình — do `ProcessCameraProvider` dùng chung 1 camera
cho cả process, `GestureForegroundService.startCamera()` gọi `unbindAll()` xóa
mất preview của MainActivity. Đây cũng là nghi vấn chính khiến pipeline nhận
diện không nhận được khung hình (mục sau).
**Đã loại**: để preview MainActivity luôn bật — Phase 0 cần preview để xác
nhận CameraX nối dây được, nhưng SPEC mục 7 (giao diện cuối) không có preview
tự chụp, nên tắt khi service thật đang chạy là đúng hướng, không phải cắt bớt.

## 2026-09-20 — Nâng T_IN từ 0.45 lên 0.58 (ngón cái "khép")
**Chọn**: `T_IN = 0.58` thay vì 0.45.
**Vì**: test M1 thật trên máy không thấy icon trạng thái hiện ra dù giữ đúng
tư thế. Soát lại dữ liệu Phase 1: tư thế M1 thật đo `t` dao động 0.32–0.62,
**trung vị 0.48** — với `T_IN=0.45`, quá nửa số khung hình M1 thật không bao
giờ được xếp là "ngón cái khép", khiến `isM1EntryPose` gần như không bao giờ
đúng. T_IN mới đặt sát `T_OUT=0.62` để bắt được tư thế M1 thật, đánh đổi lấy
vùng mờ rất hẹp (0.58–0.62).
**Đã loại**: giữ T_IN=0.45 (đã xác nhận sai qua test thật) hoặc đặt T_IN ở
giữa khoảng đo được (~0.48) — vẫn bỏ sót gần một nửa khung hình M1 thật.
**Ghi chú**: vì M2 chưa được xây dựng, nâng T_IN sát T_OUT chưa gây rủi ro lẫn
lộn ngay bây giờ; khi làm M2 (Phase 4) phải xem lại ngưỡng này cùng lúc, có
thể cần công thức đo ngón cái khác (xem BACKLOG.md).

## 2026-09-20 — Bọc detectAsync trong try-catch, không để một khung lỗi sập app
**Chọn**: `HandLandmarkerHelper.detectAsync` bọc `handLandmarker.detectAsync(...)`
trong try-catch `RuntimeException`, gọi `onError` thay vì để ném lên.
**Vì**: test thật phát hiện app **crash toàn bộ tiến trình** ngay khi bật
`GestureForegroundService` — log cho thấy
`MediaPipeException: failed precondition: The task graph hasn't been
successfully started` ném ra từ `detectAsync`, xảy ra ở khung hình đầu tiên
gửi vào ngay sau khi `HandLandmarker` vừa tạo xong (graph MediaPipe khởi tạo
bất đồng bộ bên trong, chưa chắc sẵn sàng ngay khi constructor trả về).
`DebugActivity`/`GestureTestActivity` không gặp lỗi này vì luồng khởi động có
Compose/UI xen vào nên vô tình có đủ thời gian cho graph khởi tạo xong;
`GestureForegroundService` khởi động thẳng, gửi khung đầu tiên quá nhanh.
**Đã loại**: thêm khoảng trễ cố định trước khi bind camera — không chắc đủ
robust (thời gian khởi tạo graph phụ thuộc máy), bọc try-catch xử lý được
mọi trường hợp lỗi thoáng qua tương tự, không chỉ riêng lần đầu.
**Hệ quả các triệu chứng đã thấy**: crash tiến trình giải thích toàn bộ —
camera "tắt hẳn" (tiến trình chết), không thấy icon overlay (Accessibility
service mất kết nối theo), MainActivity bị hệ thống đóng cưỡng bức.

## 2026-09-20 — Chuyển callback HandLandmarker về main thread trong HandLandmarkerHelper
**Chọn**: `HandLandmarkerHelper` tự `mainHandler.post { }` quanh cả 3 điểm gọi
`onResult`/`onHandLost`/`onError` ra ngoài, thay vì để từng caller tự lo.
**Vì**: bắt được tombstone thật (signal 6, "JNI DETECTED ERROR... Can't create
handler inside thread Thread-8 that has not called Looper.prepare()") —
`HandLandmarker` LIVE_STREAM gọi kết quả trên thread nội bộ của nó (không có
Looper); `GestureForegroundService.handleFrame()` dùng kết quả đó gọi thẳng
`OverlayRenderer.show()` → `WindowManager.addView()`, thao tác bắt buộc chạy
trên main thread. Gọi sai thread → RuntimeException giữa lúc đang trong lời
gọi JNI → crash native, không phải Java exception thường nên try-catch không
bắt được và không để lại `FATAL EXCEPTION` bình thường.
**Đã loại**: bọc `mainHandler.post` riêng trong `GestureForegroundService`
(caller duy nhất gặp lỗi lúc đó) — chọn sửa trong `HandLandmarkerHelper` vì
đó là nơi DUY NHẤT mọi caller (kể cả `DebugActivity` sau này) đi qua, tránh
lặp lại lỗi tương tự nếu thêm caller mới quên tự post.

## 2026-09-20 — MainActivity chỉ unbind() use case của mình, không unbindAll()
**Chọn**: `FrontCameraPreview.onDispose` gọi `provider.unbind(preview)` (chỉ
gỡ Preview của MainActivity), không còn gọi `provider.unbindAll()`.
**Vì**: `ProcessCameraProvider` dùng chung cho cả process. MainActivity dò
`GestureForegroundService.isRunning` bằng polling mỗi 1s (không tức thời).
Khi bấm bật, service bind `ImageAnalysis` của nó trước; tối đa 1s sau,
MainActivity mới phát hiện và dọn dẹp Preview cũ của mình — nhưng
`unbindAll()` gỡ luôn `ImageAnalysis` mà service vừa bind, khiến camera "bật
lên rồi tắt sau vài giây" dù không có exception nào (xác nhận qua log: service
bind lúc 26.697, bị gỡ lúc 27.354, ~0.66s sau).
**Đã loại**: đồng bộ hoá polling xuống dưới 1s hoặc dùng callback thay vì
polling — sửa được nhưng phức tạp hơn không cần thiết; gỡ đúng use case của
mình là đủ và đúng bản chất vấn đề (MainActivity không nên đụng tới use case
nó không sở hữu).

## 2026-09-20 — Tính vận tốc vuốt theo cửa sổ thời gian, không theo 1 khung đơn lẻ
**Chọn**: `GestureStateMachine.detectSwipe` tính vận tốc bằng
`(vị_trí_hiện_tại - vị_trí_cũ_nhất_trong_VELOCITY_WINDOW_MS) / khoảng_thời_gian`,
thay vì chỉ dùng khung liền trước.
**Vì**: test thật cho thấy nhận cử chỉ vuốt thất thường — đôi khi vẫy nhẹ
cũng kích hoạt, đôi khi vẫy mạnh vẫn không nhận. Đo được frame rate MediaPipe
dao động mạnh (~23-97ms/khung) trên máy thật; tính theo 1 khung đơn lẻ khiến
`dtMs` nhỏ tình cờ thổi phồng giả vận tốc, `dtMs` lớn tình cờ pha loãng giả.
**Đã loại**: giữ tính theo khung đơn lẻ và chỉ hạ ngưỡng — không sửa được gốc
rễ vì vấn đề nằm ở phép tính, không phải ở giá trị ngưỡng.

## 2026-09-20 — Hạ SWIPE_VEL_MIN (1.0→0.4) và AXIS_RATIO (1.8→1.5) theo đo thật
**Chọn**: `SWIPE_VEL_MIN = 0.4f`, `AXIS_RATIO = 1.5f` (đo trên máy thật sau khi
đổi sang tính vận tốc theo cửa sổ ở trên).
**Vì**: cách tính theo cửa sổ làm giảm đỉnh vận tốc đo được (cú vẫy mạnh nhất
đo được chỉ ~1.2-1.3, so với ~2.6 khi tính theo khung đơn lẻ trước đó);
`SWIPE_VEL_MIN=1.0` cũ gần như không bao giờ đạt tới với thao tác thật (chỉ
10/347 rồi 0/42 mẫu vượt ngưỡng qua 2 lần đo). `AXIS_RATIO=1.8` loại nhầm 1
cú vẩy nhanh có ratio thực tế 1.64.
**Hệ quả biết trước**: nhận cử chỉ dễ hơn cũng đồng nghĩa dễ nhận nhầm chuyển
động không cố ý hơn — cần theo dõi tiếp trên máy thật, có thể phải đo lại nếu
xảy ra kích hoạt nhầm.

## 2026-09-20 — Bắt buộc tay "đứng yên" mới cho vuốt tiếp, không chỉ chờ SWIPE_COOLDOWN
**Chọn**: thêm `awaitingRest` vào state Active — sau 1 cú vuốt, bỏ qua mọi
chuyển động cho đến khi vận tốc xuống dưới `SWIPE_REST_VEL_MAX`, VÀ đã qua
`SWIPE_COOLDOWN_MS`, mới cho vuốt tiếp.
**Vì**: yêu cầu trực tiếp của người dùng sau khi test trên máy thật — 1 lần
vẫy tay dài (chưa kịp dừng hẳn) có thể bắn ra nhiều cú vuốt liên tiếp ngoài ý
muốn nếu chỉ chờ hết thời gian cooldown cố định.
**Đã loại**: chỉ tăng `SWIPE_COOLDOWN_MS` — không giải quyết được trường hợp
tay vẫn đang di chuyển nhanh sau khi hết thời gian chờ.

## 2026-09-20 — Tách SWIPE_VEL_MIN thành 3 ngưỡng riêng theo hướng
**Chọn**: `SWIPE_VEL_MIN_UP=0.15`, `SWIPE_VEL_MIN_DOWN=0.4`,
`SWIPE_VEL_MIN_LEFT_RIGHT=0.8` thay vì 1 `SWIPE_VEL_MIN` chung cho cả 4 hướng.
`GestureStateMachine.detectSwipe` xác định trục trội hơn trước, rồi chọn
ngưỡng theo hướng đó.
**Vì**: yêu cầu trực tiếp của người dùng sau khi test — độ nhạy tự nhiên lệch
hẳn giữa các hướng do đặc điểm vận động tay (Xuống được trọng lực hỗ trợ nên
nhanh hơn hẳn, đã thấy rõ ở lần đo 2026-09-20 trước đó); 1 ngưỡng chung không
thể vừa nhạy cho Lên vừa không quá nhạy cho Trái/Phải cùng lúc.
**Đã loại**: giữ 1 ngưỡng chung và chỉ chỉnh AXIS_RATIO/VELOCITY_WINDOW_MS —
không giải quyết được vì vấn đề nằm ở SỰ KHÁC BIỆT vốn có giữa các hướng, không
phải ở cách tính chung.
**Lưu ý**: các số 0.15/0.4/0.8 là bước điều chỉnh đầu tiên đúng theo yêu cầu
hướng (nhạy hơn/kém nhạy hơn/giữ nguyên), chưa phải số đo chính xác — người
dùng tự test và có thể yêu cầu chỉnh tiếp.

**Cập nhật cùng ngày**: `SWIPE_VEL_MIN_LEFT_RIGHT` 0.8→0.65 (0.8 quá khó theo
test thật), `SWIPE_COOLDOWN_MS` 500→800 (tăng giãn cách giữa 2 lần vuốt theo
yêu cầu người dùng).

## 2026-09-20 — Tăng AXIS_RATIO (1.5→2.2) để giảm lẫn hướng Trái/Phải với Lên/Xuống
**Chọn**: `AXIS_RATIO = 2.2f`.
**Vì**: sau khi tách ngưỡng vận tốc riêng theo hướng
(`SWIPE_VEL_MIN_UP=0.15`, `DOWN=0.4` thấp hơn hẳn `LEFT_RIGHT=0.65`), người
dùng báo vuốt Trái/Phải rất dễ bị nhận nhầm thành Lên/Xuống. Nguyên nhân: vẫy
tay Trái/Phải thật luôn có một chút lệch trục dọc tự nhiên (cơ chế cổ
tay/khuỷu tay); vì ngưỡng Lên/Xuống thấp hơn nhiều, chỉ cần thành phần lệch
trục đó vượt ngưỡng thấp là bị chốt nhầm hướng, dù `AXIS_RATIO=1.5` tưởng như
đã lọc chuyển động chéo. Tăng lên 2.2 để bắt buộc trục chính phải trội hơn
hẳn mới chốt hướng.
**Đã loại**: giữ `AXIS_RATIO` thấp và chỉ chỉnh riêng `SWIPE_VEL_MIN_*` — đây
chính là điều đã làm trước đó, chưa giải quyết được lẫn hướng vì gốc rễ nằm ở
sự bất đối xứng ngưỡng giữa các hướng, không phải ở giá trị ngưỡng vận tốc.
**Đánh đổi biết trước**: cú vẩy không thật sự thẳng trục sẽ bị bỏ qua (yêu
cầu vẫy lại) thay vì nhận sai hướng — chấp nhận được vì nhận sai hướng gây
hành động sai trên màn hình, còn bị bỏ qua chỉ cần vẫy lại.

## 2026-09-20 — Tách M1 thành 2 tư thế loại trừ nhau: 2 ngón (dọc) và 3 ngón (ngang)
**Chọn**: tư thế 2 ngón (b,c dựng khép, d,e gập) chỉ cho phép Lên/Xuống; tư
thế 3 ngón (b,c,d dựng khép cả ba, e gập) chỉ cho phép Trái/Phải. Thêm
`SwipeAxisMode` (VERTICAL/HORIZONTAL) vào state Arming/Active của
`GestureStateMachine`, gắn từ lúc vào tư thế, dùng để chặn hẳn hướng không
thuộc tư thế đang giữ trước khi xét vận tốc/tỉ lệ trục.
**Vì**: yêu cầu trực tiếp của người dùng — dù đã tăng `AXIS_RATIO` lên 2.2,
Trái/Phải vẫn dễ lẫn sang Lên/Xuống. Root cause thật sự không phải ở độ chặt
của tỉ lệ trục mà ở việc CHỈ DÙNG 1 TƯ THẾ cho cả 4 hướng rồi đoán qua vận
tốc — vốn dĩ không thể phân biệt hoàn toàn được vì vẫy tay thật luôn có
thành phần chéo. Tách tư thế loại trừ hướng ngay từ đầu, không cần đoán nữa.
**Đã loại**: tiếp tục tăng `AXIS_RATIO` cao hơn nữa — chỉ giảm xác suất lẫn
chứ không loại bỏ được, và càng tăng thì càng khó kích hoạt cả 2 hướng.
**Hệ quả**: thêm 1 field `cd: PairState` vào `StablePose`/`PoseClassifier`
(tái dùng ngưỡng `G_CLOSE`/`G_OPEN` của cặp b-c cho cặp c-d, chưa có số đo
riêng — xem TODO trong code). Cần cập nhật lại toàn bộ hướng dẫn cử chỉ
trong app khi làm Phase 7 (màn hình hướng dẫn) vì tư thế M1 giờ có 2 biến
thể thay vì 1.

## 2026-09-20 — Thêm màn hình Cài đặt độ nhạy (DataStore Preferences)
**Chọn**: `SettingsActivity` (Compose) với 3 thanh trượt độ nhạy (Lên, Xuống,
Trái/Phải), lưu qua `data/SettingsRepository.kt` (DataStore Preferences —
đã chốt sẵn trong `ARCHITECTURE.md` mục 1, giờ mới triển khai). 3 hằng số
`SWIPE_VEL_MIN_UP/DOWN/LEFT_RIGHT` trong `GestureThresholds` đổi từ
`const val` sang `var` để có thể gán runtime; các giá trị suy ra
(`SWIPE_VEL_MIN`, `SYS_VEL_MIN`, `SWIPE_REST_VEL_MAX`) đổi thành `val get()`
để luôn phản ánh giá trị mới nhất thay vì đông cứng lúc biên dịch.
`GestureForegroundService.observeSettings()` lắng nghe Flow từ
DataStore xuyên suốt vòng đời service, áp dụng ngay khi người dùng chỉnh
thanh trượt mà không cần khởi động lại pipeline.
**Vì**: yêu cầu trực tiếp của người dùng, sau nhiều vòng lặp "báo cảm giác →
Claude sửa số → build → cài lại → test" — thanh trượt cho phép người dùng tự
tinh chỉnh ngay trên máy, không cần vòng lặp build mỗi lần.
**Đã loại**: ViewModel/Hilt cho state management — codebase hiện tại dùng
`remember`/`mutableStateOf` đơn giản ở mọi màn hình khác (MainActivity,
DebugActivity), giữ nhất quán, không thêm phức tạp.
**Đã test**: cài lên máy thật, chụp màn hình xác nhận 3 thanh trượt hiển thị
đúng vị trí tương ứng giá trị đang dùng.

## 2026-09-20 — Phase 4 (M2/M3): GestureAction phát đơn vị trừu tượng, không phải pixel
**Chọn**: `GestureAction.CursorMove(dx, dy)` và `Click(x, y)` mang giá trị
"đơn vị trừu tượng" (chuyển động tay đã chuẩn hoá theo S, lọc One Euro, nhân
`CURSOR_GAIN`) — không phải pixel màn hình. `UnTouchAccessibilityService` là
nơi DUY NHẤT quy đổi sang pixel (cộng vào tâm màn hình, kẹp theo
`EDGE_MARGIN`), dùng CHUNG 1 công thức cho cả di chuyển lẫn click.
**Vì**: giữ `GestureStateMachine` thuần Kotlin không phụ thuộc Android
(ARCHITECTURE mục 2, để unit test không cần thiết bị). Toạ độ click phải chụp
đúng lúc bắt đầu tách ngón (SPEC mục 4.3), có thể vài khung trước lúc khép lại
— nên bản thân state machine phải tự tích luỹ vị trí, không thể nhờ lớp
Android "nhớ hộ" vị trí tại một thời điểm trong quá khứ.
**Đã loại**: để state machine biết luôn kích thước màn hình (tính pixel trực
tiếp) — phá vỡ ranh giới kiến trúc đã chọn, và làm state machine phụ thuộc
Android chỉ vì 1 phép nhân đơn giản.
**Rủi ro đã biết, chưa xử lý**: nếu con trỏ đang bị kẹp ở biên màn hình
(`EDGE_MARGIN`) khi giá trị tích luỹ phía state machine vẫn tăng không kẹp,
toạ độ click tính lại từ đầu có thể lệch nhẹ so với vị trí hiển thị thực tế.
Chấp nhận cho v1 vì hiếm gặp (chỉ khi cố tình đưa con trỏ ra sát mép).

## 2026-09-20 — Đảo trục X, Y cho con trỏ M2 theo suy luận từ M1
**Chọn**: `detectCursor` dùng `rawX = -features.p.x / S`, `rawY = -features.p.y / S`
(đảo dấu cả hai) trước khi đưa vào One Euro filter, để `CursorMove.dx > 0`
nghĩa là "sang phải" và `dy > 0` nghĩa là "xuống dưới" — đúng quy ước pixel
màn hình thông thường.
**Vì**: đã xác nhận trên máy thật (2026-09-20, xem mục "Fix trục X/Y cho vuốt"
ở trên) world landmark X dương = tay đưa sang TRÁI, Y dương = tay đưa LÊN —
ngược quy ước pixel (phải=+x, xuống=+y) cho cả 2 trục. Áp dụng lại đúng phép
đảo đã xác nhận đó cho M2 để `UnTouchAccessibilityService` chỉ cần cộng thẳng
delta vào toạ độ, không cần đảo dấu rải rác ở lớp Android.
**CHƯA XÁC NHẬN RIÊNG cho con trỏ**: chiều Y tin được vì dùng chung cơ chế đo
với M1 (đã test), nhưng chiều X mới chỉ suy luận theo cùng quy luật, chưa
test riêng cảm giác "đưa tay phải, con trỏ chạy phải" trên máy thật. Cần xác
nhận khi test Phase 4.

## 2026-09-20 — Bỏ điều kiện huỷ M2 theo ngón cái (t0), sau khi vào M2 chỉ quan tâm b,c
**Chọn**: xoá field `t0` khỏi `CursorActive`, xoá điều kiện
`t ≤ t0×T_CLOSE_RATIO && bc CLOSE → huỷ`. Sau khi vào M2 (tư thế vào vẫn giữ
nguyên: a,b,c dựng, a xoè, b,c khép), state machine không còn theo dõi ngón
cái nữa — chỉ b,c quyết định click/hold (mục 4.3, 4.4).
**Vì**: yêu cầu trực tiếp của người dùng sau khi test — con trỏ "rất khó di
chuyển" dù đã tăng `CURSOR_GAIN` lên mức tối đa. Nguyên nhân thật: dữ liệu đo
ngón cái từ Phase 1 đã biết là rất nhiễu (`T_OUT`/`T_IN` không tách biệt rõ,
xem `BACKLOG.md`) — điều kiện huỷ theo `t0` bị kích hoạt nhầm liên tục ngay
cả khi tay chỉ hơi rung, khiến M2 tự huỷ rồi vào lại (reset con trỏ về giữa
màn hình) thay vì thực sự di chuyển được. Bỏ điều kiện này giải quyết cả 2
việc: hết reset thất thường VÀ đơn giản hoá state machine.
**Đã loại**: đo lại/hiệu chỉnh công thức tính `t` cho ổn định hơn — đã biết
từ Phase 1 là vấn đề nằm ở bản chất chuyển động ngón cái (xoay 2 trục, phần
chính rơi vào trục z camera đơn ước lượng kém), không phải do chọn sai
ngưỡng; sửa đúng cần đổi hẳn công thức đo (đo góc thay vì khoảng cách) — để
dành cho sau nếu thực sự cần thoát M2 bằng cử chỉ tay (xem BACKLOG.md).
**Hệ quả**: M2 hiện KHÔNG có cách thoát chủ động bằng cử chỉ tay nữa — chỉ
thoát khi tay ra khỏi khung hoặc xoè cả 5 ngón. Ghi vào BACKLOG.md để theo
dõi, chưa rõ có bất tiện trong thực tế hay không.

## 2026-09-20 — CURSOR_GAIN: đổi thanh trượt thành ô nhập số tự do
**Chọn**: bỏ `MIN_CURSOR_GAIN`/`MAX_CURSOR_GAIN`, thay `Slider` bằng
`OutlinedTextField` (bàn phím số) trong `SettingsActivity` — người dùng gõ
thẳng giá trị `CURSOR_GAIN`, không giới hạn trên/dưới cố định.
**Vì**: yêu cầu trực tiếp của người dùng — con trỏ vẫn quá chậm dù đã ở mức
tối đa của thang trượt cũ (30-500); cần nhân hệ số lên nhiều lần và tự tính
toán con số phù hợp, không phù hợp với 1 thang 0-1 cố định như các mục khác.
**Đã loại**: mở rộng khoảng min/max của thanh trượt thay vì đổi hẳn sang ô
nhập số — không giải quyết được gốc rễ vì bất kỳ khoảng cố định nào cũng có
thể lại không đủ, đúng như những gì vừa xảy ra với 30-500.

## 2026-09-20 — Giam nhieu One Euro filter (minCutoff 1.0→0.3, beta 0.007→0.015)
**Chọn**: `OneEuroFilter` mặc định `minCutoff=0.3f`, `beta=0.015f` (giữ
`dCutoff=1.0f`).
**Vì**: người dùng báo con trỏ "hơi giật/rung" sau khi đã sửa được lỗi
CursorMove; đồng thời `CURSOR_GAIN` giờ có thể rất lớn (người dùng tự nhập,
không giới hạn) — nhiễu còn sót lại sau lọc sẽ bị nhân lên đúng theo tỷ lệ
gain, nên phải lọc mạnh tay hơn ở tốc độ thấp (`minCutoff` thấp) để bù lại.
Tăng `beta` một chút để giữ phản hồi nhanh khi tay di chuyển thật, tránh lọc
quá tay gây trễ rõ rệt lúc vẫy nhanh.
**Chưa đo**: đây là bước chỉnh đầu tiên dựa trên lý thuyết bộ lọc, chưa có số
đo thật — cần người dùng test và phản hồi tiếp cảm giác mượt/trễ.

## 2026-09-20 — TẠM THỜI bỏ điều kiện huỷ M2 để cách ly lỗi "con trỏ biến mất"
**Chọn**: `onHandLost()` và check `allFiveUp` trong `onFrame()` bỏ qua hoàn
toàn khi đang ở `CursorActive`. Đánh dấu rõ là nợ kỹ thuật tạm thời trong
`BACKLOG.md`, không phải hành vi đúng lâu dài.
**Vì**: yêu cầu người dùng — con trỏ "thường xuyên biến mất giữa đường" khi
đang dùng M2. Nghi ngờ chính: mất tracking thoáng qua lúc tay di chuyển
nhanh (motion blur giảm độ tin cậy MediaPipe) khiến `onHandLost()` huỷ M2
sau khi vượt `LOST_HAND_MS=300ms`, hoặc tư thế tay khi di chuyển nhanh vô
tình gần giống "xoè cả 5 ngón". Bỏ hẳn 2 điều kiện này để xác nhận: nếu con
trỏ vẫn biến mất, lỗi nằm ở chỗ khác (vd `CursorView`/`WindowManager`), nếu
hết biến mất thì xác nhận đúng nghi ngờ trên.
**Đã loại**: tăng `LOST_HAND_MS` lên cao hơn thay vì bỏ hẳn — chưa làm vì
muốn cô lập nguyên nhân trước, tăng số mà đoán sai vẫn có thể còn hiện tượng.
**QUAN TRỌNG — phải khôi phục đúng cách sau khi xác định nguyên nhân**, xem
mục "Nợ kỹ thuật cần dọn sớm" đầu `BACKLOG.md`.

## 2026-09-20 — Giảm nhiễu One Euro filter lần 2 (minCutoff 0.3→0.05, beta 0.015→0.01)
**Chọn**: `minCutoff=0.05f`, `beta=0.01f`.
**Vì**: người dùng báo vẫn giật dù đã tăng `CURSOR_GAIN` lên 5000 và đã chỉnh
filter lần 1 (0.3/0.015). Lưu ý kỹ thuật quan trọng đã giải thích cho người
dùng: nhiễu còn sót lại sau lọc bị nhân lên ĐÚNG THEO TỶ LỆ gain — gain 5000
là ~33 lần giá trị mặc định (150), nên dù lọc chặt cỡ nào cũng khó tránh giật
rõ rệt ở mức gain này. Đây là đánh đổi vật lý giữa "nhạy" và "mượt", không
phải lỗi lọc chưa đủ tốt.
**Khuyến nghị cho người dùng**: thử lại ở mức gain vừa phải hơn (vài trăm)
trước, xem có mượt hơn không, rồi tăng dần thay vì nhảy thẳng lên số rất lớn.

## 2026-09-20 — Đo S (kích thước bàn tay) một lần lúc vào M2, không tính lại mỗi khung
**Chọn**: thêm field `sessionS` vào `CursorActive`, chụp `features.s` đúng 1
lần tại thời điểm vào M2 (kết thúc Arming), dùng xuyên suốt phiên thay vì gọi
`features.s` (đo lại mỗi khung) như các chế độ khác.
**Vì**: người dùng tự phân tích và xác nhận — con trỏ dùng được ở gain
5000-6000 khi để tay THẬT XA camera, nhưng gần như không di chuyển nổi ở
gain 500 khi tay ở khoảng cách thường. Giả thuyết: ước lượng world-landmark
của MediaPipe (đặc biệt kích thước bàn tay S) kém ổn định hơn ở cự ly gần;
vì MỌI chuyển động con trỏ đều chia cho S, nhiễu trong S lan thẳng vào toàn
bộ phép tính, và bị `CURSOR_GAIN` khuếch đại thêm — giải thích tại sao cần
gain cực cao mới "át" được nhiễu. Chụp S một lần loại bỏ hẳn nguồn nhiễu
frame-to-frame này cho riêng phần di chuyển con trỏ.
**Tiện thể sửa 1 bug nhỏ phát hiện được**: giá trị khởi tạo `lastFilteredX/Y`
lúc vào M2 trước đây thiếu dấu trừ (không khớp dấu với `rawX/rawY` tính mỗi
khung sau đó) — gây 1 cú nhảy nhỏ sai hướng ở khung đầu tiên mỗi lần vào M2.
Đã sửa cho khớp dấu.
**Đã loại**: giữ tính S mỗi khung cho M2 như các chế độ khác — không giải
quyết được vì đây chính là nguồn nhiễu, không phải do thiếu lọc.
**Chưa đo**: giả thuyết hợp lý dựa trên dữ liệu người dùng cung cấp, nhưng
chưa có số đo trực tiếp về độ ổn định của S theo cự ly — cần người dùng test
lại xem gain cần thiết có giảm xuống mức hợp lý hơn (vài trăm - vài nghìn)
không, ở cả cự ly gần lẫn xa.

## 2026-09-20 — Chính thức hoá: M2 chỉ thoát bằng xoè cả 5 ngón, mất tay giữ nguyên vị trí
**Chọn**: biến "hack tạm thời" của lần sửa trước (bỏ điều kiện huỷ M2 khi mất
tay/xoè 5 ngón, để cách ly lỗi con trỏ biến mất) thành **thiết kế chính thức
lâu dài**, với 1 điều chỉnh: xoè cả 5 ngón VẪN huỷ M2 (không bị bỏ qua nữa),
chỉ riêng **mất tay** là không huỷ. Cụ thể: `onHandLost()` bỏ qua hoàn toàn
khi đang `CursorActive` (không xoá filter/lastFilteredX/Y/cumulative), con
trỏ đứng yên tại vị trí cuối; khi bắt lại được tay, `detectCursor` tính delta
tiếp tục từ đúng vị trí đó. Vì nhánh `CursorActive` trong `onFrame` không gọi
`entryTargetOf()`, đổi sang tư thế M1 (2/3 ngón) trong luc o M2 tu dong khong
kich hoat duoc M1 (khong can code them).
**Vì**: yêu cầu trực tiếp của người dùng, dựa trên kết quả test thực tế của
hack tạm thời trước đó — mất tay không huỷ đúng là hành vi mong muốn (không
phải chỉ để chẩn đoán), giúp cầm tay dùng con trỏ lâu dài mà không sợ tracking
chập chờn làm mất tiến trình đang làm; đồng thời xác nhận M1 không được phép
"chen ngang" khi đang ở M2.
**Đã loại**: khôi phục điều kiện huỷ theo mất tay với grace period dài hơn —
người dùng chọn thẳng "không bao giờ huỷ vì mất tay", đơn giản hơn và đúng
với cách họ muốn dùng con trỏ.
**Hệ quả tài liệu**: xoá mục nợ kỹ thuật tương ứng trong `BACKLOG.md`, cập
nhật `SPEC.md` §4.2 phần "Hủy" mô tả đầy đủ hành vi mới.

## 2026-09-20 — Giới hạn độ phân giải camera + chuyển MediaPipe sang GPU delegate
**Chọn**: `GestureForegroundService` giới hạn `ImageAnalysis` về ~480x360 qua
`ResolutionSelector` (trước đó CHƯA TỪNG giới hạn dù `ARCHITECTURE.md` mục 6
đã ghi rõ "480p hoặc thấp hơn" từ đầu). `HandLandmarkerHelper` chuyển
`Delegate.CPU` sang `Delegate.GPU`, có fallback tự động về CPU nếu máy không
khởi tạo được GPU delegate (tránh crash trên thiết bị lạ).
**Vì**: người dùng báo độ trễ xuyên suốt từ đầu, và hay mất tracking khi tay
di chuyển nhanh. Cả 2 triệu chứng cùng chỉ về 1 nguyên nhân khả dĩ: fps xử
lý thấp (do xử lý ảnh phân giải cao hơn cần thiết + delegate CPU chậm) khiến
khung hình thưa, tay di chuyển nhanh giữa các khung dễ mất dấu. Đây là sửa
gốc rễ tốc độ xử lý, không phải chữa cháy — GPU delegate cũng chính là
"nâng cấp khi cần thêm fps" đã dự tính từ Phase 0/1.
**Đã test**: cài lên máy thật, xác nhận qua log GPU delegate khởi tạo thành
công, không có crash mới.
**Rủi ro chấp nhận**: GPU delegate từng bị hoãn vì ưu tiên ổn định trên cả 2
máy mục tiêu — có fallback CPU nhưng chưa test được tren realme GT 8 Pro
(máy còn lại), chỉ mới xác nhận trên RMX3370.

## 2026-09-20 — Tăng CLICK_MAX_MS (300→600ms)
**Chọn**: `CLICK_MAX_MS = 600L`.
**Vì**: người dùng báo chưa từng click được lần nào. Nghi ngờ chính: mỗi lần
b,c đổi trạng thái tách/khép cần PoseClassifier vote 4/5 khung mới được coi
là "thật sự xảy ra" (SPEC mục 3, hysteresis chống nhiễu) - ở fps thấp, riêng
việc XÁC NHẬN đã tách xong có thể đã ăn hết phần lớn 300ms, không còn đủ thời
gian xác nhận khép lại kịp trong ngân sách còn lại. Tăng gấp đôi để bù độ trễ
vote này.
**Chưa đo**: vẫn là số đoán, chưa đo trực tiếp số khung/ms thực tế cần để
vote xong trọn 1 chu kỳ tách-khép trên máy thật.

## 2026-09-20 — Thêm chế độ con trỏ "theo hướng ngón trỏ", đổi CursorMove sang toạ độ tuyệt đối
**Chọn**: thêm `GestureThresholds.CURSOR_POINTING_MODE` (công tắc trong màn
hình Cài đặt, mặc định tắt = giữ hành vi cũ). Khi bật: `detectCursor` tính vị
trí con trỏ theo **độ lệch góc của hướng ngón trỏ** (vector từ `INDEX_MCP`
tới `INDEX_TIP`, đã chuẩn hoá vector đơn vị — thêm field `pointDirX/Y` vào
`Features`/`FeatureExtractor`) so với hướng lúc vào M2 (`refDirX/Y`, chụp 1
lần), thay vì chuyển động tương đối của `P`. Để 2 chế độ dùng chung 1 định
dạng dữ liệu, đổi `GestureAction.CursorMove` từ mang **delta** sang mang
**toạ độ tuyệt đối** (lệch so với tâm màn hình) — `UnTouchAccessibilityService.moveCursor`
giờ SET vị trí thay vì cộng dồn.
**Vì**: yêu cầu trực tiếp của người dùng — chế độ "theo tay" một mình đòi hỏi
di chuyển cả bàn tay đủ xa để bao hết màn hình, khó với người không tiện di
chuyển tay nhiều; "theo hướng ngón trỏ" (kiểu tia laser) chỉ cần xoay cổ
tay/ngón tay, phạm vi chuyển động vật lý nhỏ hơn nhiều.
**Đã loại**: giữ `CursorMove` là delta và tính pointing-mode bằng cách tích
luỹ delta góc mỗi khung — phức tạp hơn không cần thiết, vì bản chất pointing
mode là ánh xạ TUYỆT ĐỐI (góc hiện tại so với mốc), không phải tương đối.
**Đánh đổi đã ghi rõ cho người dùng**: 2 chế độ dùng chung `CURSOR_GAIN`
nhưng đơn vị/độ lớn hợp lý khác hẳn nhau (theo tay: tỷ lệ vị trí/S; theo
hướng: độ lệch vector đơn vị, thường nhỏ hơn nhiều) - cần tự nhập lại số khi
đổi chế độ.
**Chưa test trên máy thật.**

## 2026-09-20 — Sửa trục con trỏ: KHÔNG đảo dấu như M1 (khác quy ước vuốt)
**Chọn**: `detectCursor` dùng thẳng `features.p.x/p.y` và `pointDirX/Y`,
KHÔNG đảo dấu (bỏ dấu `-` đã copy từ `detectSwipe` khi mới viết code).
**Vì**: người dùng xác nhận trên máy thật — cả 2 trục lên/xuống và trái/phải
đều bị ngược. Quy ước đảo dấu dùng cho M1 (đo cho VẬN TỐC cú vuốt, xem mục
"Fix trục X/Y cho vuốt") hoá ra KHÔNG áp dụng trực tiếp được cho VỊ TRÍ con
trỏ — 2 ngữ cảnh khác nhau dù cùng dùng world landmark. Đã sửa cho khớp thực
tế thay vì suy luận lại lý do tại sao khác nhau (không quan trọng bằng việc
đúng trên máy thật).
**Đã loại**: tiếp tục suy luận lý thuyết để tìm lý do khác biệt giữa 2 ngữ
cảnh — tốn thời gian không cần thiết, ưu tiên khớp kết quả đo thật.

## 2026-09-20 — Bù độ nhạy chiều Lên riêng cho chế độ "theo hướng ngón trỏ"
**Chọn**: thêm `GestureThresholds.CURSOR_POINTING_UP_BOOST = 2.5f`, nhân
thêm vào gain CHỈ khi độ lệch hướng là "lên" (`devY < 0`), trong
`detectCursor` nhánh `pointing`.
**Vì**: người dùng chỉ ra nguyên nhân hình học — để giữ tư thế vào M2 (ngón
trỏ dựng), cổ tay tự nhiên đã hơi nghiêng lên sẵn, nên hướng "chuẩn" (mốc
lúc vào M2) đã gần sát giới hạn xoay lên, còn dư rất ít biên độ vật lý để
trỏ lên thêm — khác hẳn xuống/trái/phải vốn còn nhiều biên độ. Đây là vấn đề
BIÊN ĐỘ CHUYỂN ĐỘNG, không phải nhiễu hay sai hướng, nên giải pháp đúng là
tăng độ nhạy riêng cho chiều đó — cùng cách tiếp cận đã dùng cho
`SWIPE_VEL_MIN_UP` (khác lý do: ở đó là do trọng lực hỗ trợ chiều Xuống,
không phải giới hạn biên độ).
**Đã loại**: đổi cách chụp `refDirX/Y` để "giả vờ" mốc trung tính thấp hơn
thực tế — phức tạp hơn và khó đoán đúng độ lệch cần thiết bằng cách nhân
thẳng hệ số vào kết quả đo được.
**Chưa đo**: 2.5 là số đoán đầu tiên, cần người dùng test và phản hồi tiếp.

## 2026-09-20 — Thêm kênh riêng bằng ngón cái cho chiều Lên (CURSOR_POINTING_UP_BOOST không đủ)
**Chọn**: thêm `refT` (độ xoè ngón cái `t` lúc vào M2) vào `CursorActive`.
Trong che do pointing, `thumbExtra = max(0, features.t - refT) * CURSOR_POINTING_THUMB_UP_GAIN`,
trừ thẳng vào `newCumulativeY` (cộng dồn với phần tính từ góc chỉ tay, kể cả
`CURSOR_POINTING_UP_BOOST` ở quyết định trước) — CHỈ tính phần xoè THÊM
(`coerceAtLeast(0f)`), khép lại không đẩy xuống.
**Vì**: yêu cầu trực tiếp của người dùng sau khi xác nhận `CURSOR_POINTING_UP_BOOST`
đơn thuần không đủ (chỉ lên được ~nửa màn hình) — giới hạn là do BIÊN ĐỘ VẬT
LÝ của cổ tay khi giữ tư thế vào M2, tăng hệ số không tạo thêm biên độ, chỉ
làm phần biên độ ít ỏi sẵn có nhạy hơn. Ngón cái là khớp CÁCH LY (không cần
giữ ở 1 góc cố định để duy trì tư thế vào M2 như cổ tay), nên có biên độ xoè
độc lập, dùng làm kênh bổ sung không bị giới hạn tương tự.
**Đã loại**: tiếp tục tăng `CURSOR_POINTING_UP_BOOST` cao hơn nữa — không
giải quyết được vì bản chất là thiếu BIÊN ĐỘ chứ không phải thiếu ĐỘ NHẠY
trên biên độ đã có.
**Chưa đo**: `CURSOR_POINTING_THUMB_UP_GAIN = 300f` là số đoán đầu tiên.

## 2026-09-20 — 2 chế độ con trỏ cần 2 quy ước dấu trục RIÊNG (sửa lỗi tự gây ra)
**Chọn**: chế độ "theo tay" quay lại đảo dấu `-features.p.x/y` (giống M1,
đúng như bản gốc); chế độ "theo hướng ngón trỏ" giữ KHÔNG đảo dấu
`features.pointDirX` (trục X, trục Y đã đổi cơ chế hoàn toàn — xem quyết
định dưới). 2 chế độ dùng 2 field riêng trong `CursorActive` (`lastFilteredX/Y`
cho theo tay, `refDirX` cho theo hướng — đã xoá `refDirY` vì trục Y không
còn dùng huong nua).
**Vì**: ở lần sửa trước (mục "Sửa trục con trỏ: KHÔNG đảo dấu như M1"), sau
khi người dùng báo "chế độ đang ngược", tôi đã bỏ đảo dấu cho CẢ HAI chế độ
cùng lúc — nhưng người dùng chỉ đang test chế độ "theo hướng ngón trỏ" lúc
đó. Việc bỏ đảo dấu vô tình làm HỎNG chế độ "theo tay" (vốn đang đúng, kế
thừa từ M1 đã xác nhận). Bài học: vị trí (`P`) và hướng (`pointDirX/Y`) là 2
đại lượng vật lý khác nhau dù cùng tính từ world landmark — không có lý do
gì chúng phải chia sẻ chung 1 quy ước dấu, và thực tế trên máy xác nhận
chúng KHÔNG chung quy ước.
**Đã loại**: tiếp tục dùng 1 quy ước dấu chung cho toàn bộ `detectCursor` —
đây chính là nguyên nhân gây lỗi, không lặp lại.

## 2026-09-20 — Đổi hẳn cơ chế trục Lên/Xuống của "theo hướng ngón trỏ" sang trạng thái rời rạc b,c
**Chọn**: bỏ hoàn toàn cơ chế dựa vào góc chỉ tay + kênh ngón cái cho trục Y
(2 quyết định ngay phía trên) — thay bằng đọc trực tiếp `pose.b`/`pose.c`
(trạng thái UP/DOWN đã vote ổn định, giống PoseClassifier dùng cho SPEC mục
3): b,c đều UP → đứng yên; b,c đều DOWN → cộng dồn `CURSOR_POINTING_VERTICAL_STEP`
mỗi khung (di chuyển xuống liên tục); b UP c DOWN → trừ dần (lên liên tục);
b DOWN c UP → chưa định nghĩa, tạm đứng yên. Xoá hẳn `refT`,
`CURSOR_POINTING_UP_BOOST`, `CURSOR_POINTING_THUMB_UP_GAIN` (khong con dung).
**Vì**: yêu cầu trực tiếp của người dùng — cả 2 lần thử trước (boost góc,
kênh ngón cái) đều KHÔNG giải quyết được vì bản chất là thiếu BIÊN ĐỘ VẬT LÝ
của cổ tay/ngón cái khi giữ tư thế vào M2, cộng thêm hệ số vào 1 đại lượng
liên tục vẫn bị chặn trên bởi biên độ đó. Chuyển sang trạng thái RỜI RẠC
(b,c dựng hay hạ) loại bỏ hẳn vấn đề biên độ — không còn là "xoay được bao
nhiêu độ" mà là "đang giữ trạng thái nào", có thể giữ vô thời hạn để cứ tiếp
tục di chuyển, không bị chặn trên.
**Đã loại**: tiếp tục tăng hệ số cho cơ chế liên tục (góc/ngón cái) — đã
chứng minh không hiệu quả 2 lần liên tiếp, đổi hẳn cơ chế thay vì chỉnh số.
**Rủi ro chưa xử lý**: `pose.bc` (khoảng cách b-c) dùng để phát hiện
tách/khép cho click (mục 4.3) VẪN đang được tính độc lập với `pose.b`/`pose.c`
(trạng thái dựng/hạ riêng từng ngón) — tư thế "b dựng, c hạ" (để đi lên) gần
như chắc chắn cũng làm `g_bc` tăng vượt `G_OPEN` (2 đầu ngón xa nhau), tức
đồng thời bị coi là "tách b,c" (chuẩn bị click/hold). Chưa rõ đây có gây
click/hold ngoài ý muốn khi dùng chiều Lên hay không — cần test thật để xác
nhận, xử lý sau nếu thực sự xảy ra.
**Chưa đo**: `CURSOR_POINTING_VERTICAL_STEP = 8f` (dp/khung) là số đoán đầu
tiên, phụ thuộc fps thực tế của máy.

## 2026-09-20 — Chống khựng khi đổi trạng thái b,c + coi AMBIGUOUS là "đứng yên"
**Chọn**: thêm 2 field `pendingVerticalStep`/`confirmedVerticalStep` vào
`InternalState.CursorActive`. Mỗi khung tính `rawVerticalStep` từ `pose.b`/
`pose.c` như quyết định phía trên, nhưng chỉ CÔNG NHẬN (`confirmedVerticalStep`)
khi giá trị thô giống hệt khung ngay trước đó — tức cần 2 khung liên tiếp
đồng thuận mới đổi hướng di chuyển. Đồng thời sửa điều kiện từ kiểu `!= UP`
(ngầm coi mọi thứ không phải UP là DOWN) sang so sánh tường minh
`FingerState.UP`/`FingerState.DOWN`, để `FingerState.AMBIGUOUS` (mất dấu/
tracking kém) rơi vào nhánh `else -> 0f` (đứng yên) thay vì bị hiểu nhầm
thành "hạ ngón" → con trỏ tự chạy tiếp dù đã mất tay.
**Vì**: người dùng báo khi chuyển từ đứng yên sang hạ (b,c cùng hạ), con trỏ
giật lên 1 chút trước khi xuống — do 2 ngón không đổi trạng thái đồng bộ
tuyệt đối, thoáng qua 1 khung "b UP c DOWN" (=lên) trước khi cả 2 kịp thành
DOWN. Và khi mất tay, `pose.b`/`pose.c` có thể trả về AMBIGUOUS chứ không
phải giữ nguyên UP/DOWN cũ — kiểm tra `!= UP` khiến AMBIGUOUS lọt vào nhánh
DOWN, con trỏ chạy tiếp thay vì dừng.
**Đã loại**: tăng ngưỡng vote của PoseClassifier (ảnh hưởng toàn bộ hệ thống,
không chỉ cursor) — chọn debounce cục bộ trong `detectCursor` để không đụng
các cơ chế khác đang chạy ổn.
**Lưu ý kỹ thuật dễ bỏ sót**: field debounce phải được ghi lại vào
`state = s.copy(...)` ở cuối `detectCursor`, nếu không state không lưu được
qua từng khung và debounce vô tác dụng — đã kiểm tra kỹ khi sửa.

## 2026-09-20 — Tách `CURSOR_GAIN` thành 2 hệ số riêng theo chế độ
**Chọn**: `CURSOR_GAIN_TRANSLATION` (chế độ theo tay) và `CURSOR_GAIN_POINTING`
(chế độ theo hướng ngón trỏ) là 2 hằng số/setting độc lập, xuyên suốt
`GestureThresholds` → `SettingsRepository` (2 khoá DataStore riêng) →
`GestureForegroundService.observeSettings` (2 Flow collector riêng) →
`SettingsActivity` (2 ô nhập số riêng, luôn hiện cả hai).
**Vì**: yêu cầu người dùng — đơn vị/độ lớn hợp lý của 2 đại lượng rất khác
nhau (tỷ lệ vị trí/S so với độ lệch vector hướng đơn vị, thường nhỏ hơn
nhiều), dùng chung 1 hệ số khiến người dùng phải gõ lại số mỗi lần đổi chế độ
qua Settings để test.
**Đã loại**: giữ 1 hệ số chung và tự động quy đổi theo tỷ lệ cố định giữa 2
chế độ — không đủ căn cứ để chọn tỷ lệ đúng, để người dùng tự nhập từng bên
đơn giản hơn và không đoán sai.

## 2026-09-20 — Root cause thật của "giật LEN"/"click nhầm": 2 Voter độc lập của b,c
**Vấn đề**: sau khi thêm debounce 2-khung (quyết định phía trên), người dùng
vẫn báo "đôi khi dù cả 2 ngón đều hạ, con trỏ lại di chuyển lên" và "chuyển
lên xuống đôi khi kích hoạt nhầm click".
**Root cause thật sự** (khác với chẩn đoán ban đầu "1-2 khung nhiễu"): `b` và
`c` là 2 `Voter` **độc lập** trong `PoseClassifier` (mỗi ngón tự vote 4/5
khung riêng, xem `PoseClassifier.kt`). Khi tay thật chuyển từ "b,c đều DỰNG"
sang "b,c đều HẠ", 2 Voter gần như không bao giờ vote xong CÙNG LÚC — trạng
thái bất đối xứng "b DỰNG, c HẠ" (= định nghĩa lệnh LÊN) chính là trạng thái
GIAO NHAU tất yếu giữa 2 trạng thái ổn định, không phải nhiễu ngẫu nhiên. Vì
vậy debounce "2 khung giống nhau liên tiếp" là không đủ — khoảng lệch vote
giữa 2 Voter hoàn toàn có thể kéo dài hơn 2 khung. Trạng thái bất đối xứng
này đồng thời làm 2 đầu ngón xa nhau về hình học → `pose.bc` cũng bị vote
thành OPEN → bị hiểu nhầm là "tách" chuẩn bị click.
**Chọn**: 2 phần sửa tách biệt, cả hai áp dụng trên **raw** pose (không đợi
debounce) để chặn từ gốc:
1. Chỉ áp dụng hold-time cho riêng lệnh LÊN: `rawVerticalStep < 0` phải giữ
   ỔN ĐỊNH LIÊN TỤC ít nhất `CURSOR_POINTING_UP_CONFIRM_MS` (200ms, đo bằng
   thời gian thực chứ không phải số khung, vì fps dao động 23-97ms/khung)
   mới được công nhận. "Đều DỰNG"/"đều HẠ" vẫn áp dụng NGAY vì đây là 2 đích
   đến ổn định thật sự, không phải trạng thái giao nhau.
2. Loại trừ hẳn phát hiện "tách" (click) khi `pose.b != pose.c` (bất đối
   xứng) trong chế độ pointing — không chỉ khi đã xác nhận là LÊN, vì ngay
   cả lúc CHƯA đủ 200ms, 2 đầu ngón vẫn đã xa nhau về hình học.
**Vì**: gốc rễ là xung đột thiết kế — dùng chung 2 ngón b,c vừa cho điều
khiển trục Y rời rạc vừa cho phát hiện click (đã tự ghi nhận là rủi ro chưa
xử lý ở quyết định phía trên, nay đã xác nhận thực sự xảy ra). Sửa tại đúng
nơi xung đột (raw pose, trước khi vào bất kỳ logic nào dùng chung) thay vì
vá từng biểu hiện riêng lẻ.
**Đã loại**: tăng số khung debounce (vd 3-4 khung) thay vì đổi sang thời
gian thực — không giải quyết được vì khoảng lệch vote giữa 2 Voter tính bằng
thời gian, không phải số khung cố định (fps dao động mạnh).
**Chưa đo**: `CURSOR_POINTING_UP_CONFIRM_MS = 200ms` là số đoán ban đầu dựa
trên quan sát fps, chưa đo độ trễ thực tế khi người dùng CỐ Ý muốn đi lên có
bị chậm khó chịu hay không — cần xác nhận trên máy thật.

## 2026-09-20 — Root cause thật của "nghiêng trái thì giật LÊN dù cả 2 ngón đều hạ"
**Vấn đề**: sau 2 lần sửa ở trên, người dùng báo thêm: "nếu cả 2 ngón đều hạ
xuống và chỉ về bên trái thì con trỏ đi lên, nhưng nếu nghiêng bên phải thì
không sao, hoặc nghiêng phải rồi mới qua trái thì cũng không sao".
**Chẩn đoán ban đầu (SAI, đã loại)**: nghi ngờ `r_b` nhảy vọt lên giá trị rác
(người dùng báo nhầm "80.0" thay vì "0.80" lúc đầu) do MediaPipe mất track —
đã thử thêm ngưỡng chặn giá trị rác (`R_MAX_PLAUSIBLE`), sau khi người dùng
sửa lại số liệu chính xác thì thấy giả thuyết này sai, đã bỏ thay đổi đó.
**Root cause thật** (đo bằng màn hình Debug, số liệu chính xác): gập hết cỡ
ngón trỏ (b) trong lúc nghiêng PHẢI đo được `r_b=0.55` (dưới `R_DOWN=0.65`,
qua ngưỡng "hạ" bình thường); NHƯNG cùng một cử động gập hết cỡ đó trong lúc
nghiêng TRÁI chỉ đo được `r_b=0.80` — **không bao giờ** xuống dưới 0.65, dù
đã gập hết mức có thể. Đây là do góc nhìn của camera bị biến dạng phối cảnh
khác nhau tuỳ hướng nghiêng, không phải lỗi logic. Vì `Voter` (PoseClassifier)
có "trí nhớ" (giữ kết luận ổn định cũ nếu không có giá trị mới đủ đa số
4/5 phiếu) — hễ `r_b` không bao giờ chạm ngưỡng 0.65 lúc nghiêng trái, `b`
KHÔNG BAO GIỜ tự chuyển được từ "DỰNG" (đã chốt từ lúc vào M2 hoặc lúc dùng
trục X) sang "HẠ" bằng ngưỡng thường — kết luận "DỰNG" cũ bị kẹt mãi, kết
hợp `c` hạ bình thường → đúng tổ hợp "b DỰNG, c HẠ" = lệnh LÊN, dù người
dùng đang cố hạ cả hai. Giải thích luôn vì sao "nghiêng phải rồi mới qua
trái" lại KHÔNG lỗi: nghiêng phải trước cho phép `r_b` chạm dưới 0.65 thật
sự một lần, chốt phiếu bầu "HẠ" — sau đó xoay sang trái, `r_b` chỉ tăng lại
lên vùng xám 0.65–0.90 (AMBIGUOUS, phiếu trung lập, không tính cho bên nào)
chứ không thắng phiếu "DỰNG" lại, nên kết luận "HẠ" vẫn được giữ nguyên nhờ
tính "dính" của Voter.
**Chọn**: thêm ngưỡng "hạ" RIÊNG, LỎNG HƠN — `CURSOR_POINTING_R_DOWN=0.85`
— CHỈ áp dụng cho việc xác định "b/c đã hạ" trong `detectCursor` (trục Y chế
độ theo hướng ngón trỏ). Thêm 2 `Voter<FingerState>` mới trong
`PoseClassifier` (`bDownRelaxed`/`cDownRelaxed`), dùng chung hàm `rawFinger`
đã tham số hoá ngưỡng "hạ". Điều kiện "DỰNG" (`pose.b`/`pose.c == UP`) GIỮ
NGUYÊN ngưỡng thường (`R_UP=0.90`) — không đổi vì không có bằng chứng bị ảnh
hưởng. `verticalAsymmetric` (dùng để loại trừ click, xem quyết định lần 5)
cũng cập nhật theo dùng `bDownRelaxed`/`cDownRelaxed` cho nhất quán.
**Vì**: `R_DOWN=0.65` toàn cục còn được dùng cho `d`,`e` ở M1/M2 (tư thế vào)
— đổi giá trị này sẽ ảnh hưởng ngoài phạm vi yêu cầu. `b`,`c` bị phân loại
"HẠ" chỉ được dùng ở đúng một chỗ (`detectCursor`), nên thêm ngưỡng/Voter
riêng cho đúng chỗ đó là an toàn, không đụng M1 hay tư thế vào M2.
**Đã loại**: hạ `R_DOWN` toàn cục xuống 0.85 — ảnh hưởng cả M1 (`d`,`e`) dù
chưa có bằng chứng M1 gặp vấn đề tương tự; không muốn sửa thứ đang chạy ổn
dựa trên suy đoán.
**Chưa đo**: `CURSOR_POINTING_R_DOWN=0.85` chỉ dựa trên ĐÚNG 1 điểm đo
(0.80 lúc nghiêng trái hết cỡ) cộng biên độ an toàn nhỏ — chưa có nhiều dữ
liệu, cần người dùng xác nhận thêm trên máy thật (đặc biệt các góc nghiêng
khác, và có vô tình làm "HẠ" quá dễ kích hoạt khi ngón chưa thật sự gập hay
không).

## 2026-09-20 — Phase 5 (M4 Hold và kéo): 1 hành động/khung, gộp thay vì xếp hàng vị trí kéo
**Chọn**: `GestureStateMachine.detectCursor` chỉ trả về ĐÚNG 1
`GestureAction` mỗi khung (giữ nguyên chữ ký `onFrame` cũ, không đổi sang trả
về danh sách hành động). Trong lúc đang giữ (M4), KHÔNG còn phát `CursorMove`
nữa mà thay bằng `HoldMove` — lớp Android (`UnTouchAccessibilityService`) tự
vừa cập nhật vị trí chấm tròn vừa tiếp tục đoạn kéo trong 1 hàm, tránh phải
đổi kiến trúc trả-về-nhiều-hành-động chỉ để xử lý đúng 1 trường hợp này.
**Vì**: đơn giản hơn nhiều so với đổi `onFrame` trả về `List<GestureAction>`
(sẽ phải sửa mọi nơi gọi `onFrame` và mọi `when` xử lý action hiện có), trong
khi M4 là ngoại lệ duy nhất cần "vừa vẽ vừa kéo" cùng lúc.
**Đã loại**: đổi `onFrame` trả về danh sách hành động — quá tốn cho 1
trường hợp, vi phạm "đơn giản trước" (CLAUDE.md mục 3).

**Chọn** (kéo thật): nối nhiều đoạn `GestureDescription.StrokeDescription.
continueStroke` theo từng vị trí con trỏ mới (tái dùng ý tưởng
`performTestDrag` có sẵn từ Phase 2), nhưng KHÔNG xếp hàng (queue) từng vị
trí đến — nếu một đoạn đang chờ `onCompleted` (`dragInFlight`) mà có vị trí
mới hơn đến, chỉ GHI ĐÈ lên `dragPendingTargetPx` (gộp lại thành 1 điểm đích
mới nhất), gửi đi ngay khi đoạn cũ xong.
**Vì**: camera bơm khung hình (10-40fps) gần như chắc chắn nhanh hơn tốc độ
`dispatchGesture` xử lý xong 1 đoạn kéo (mỗi đoạn có `HOLD_STEP_DURATION_MS`
riêng) — nếu xếp hàng đầy đủ từng điểm, độ trễ giữa tay thật và điểm kéo trên
màn hình sẽ CỘNG DỒN theo thời gian thay vì cố định, gây cảm giác "trễ dần
đều" càng kéo lâu càng tệ. Gộp về 1 điểm mới nhất giữ độ trễ luôn ~cố định
(khoảng 1 `HOLD_STEP_DURATION_MS`), đánh đổi bỏ qua các điểm trung gian —
chấp nhận được vì SPEC đã ghi nhận rủi ro này ở bảng rủi ro ("Kéo giật vì
phải nối nhiều đoạn dispatchGesture | trung bình | chấp nhận ở bản đầu, đo
lại sau").
**Đã loại**: xếp hàng (queue) đầy đủ từng vị trí — trung thực hơn với đường
đi thật của tay nhưng gây trễ cộng dồn không giới hạn nếu tay di chuyển
nhanh hơn tốc độ dispatch, tệ hơn cho trải nghiệm thực tế.

**Chọn** (khoá sau HOLD_MAX_MS): thêm field `holdBlockedUntilClose` trong
`CursorActive` — bật khi tự nhả do chạm `HOLD_MAX_MS` trong lúc `b,c` VẪN
đang tách, chỉ tắt khi `bc` THẬT SỰ khép lại (không phải chỉ hết giờ). Trong
lúc bị khoá, con trỏ vẫn di chuyển theo tay bình thường (`CursorMove`), chỉ
không tính click/hold mới.
**Vì**: SPEC mục 4.4 yêu cầu "an toàn tuyệt đối" cho hold — nếu chỉ tự nhả
mà không khoá, ngay khung tiếp theo (tay vẫn đang tách) sẽ lập tức tính là
"vừa tách" mới và đếm lại giờ, vào lại hold ngay lập tức — về bản chất
KHÔNG hề nhả, chỉ là gửi 1 cặp lên/xuống chớp nhoáng rồi giữ tiếp, không
giải quyết được rủi ro kẹt trạng thái nhấn mà SPEC lo ngại.

**Đã đo lại được (tận dụng trùng hợp có sẵn)**: `HOLD_LOST_MS` và
`LOST_HAND_MS` (đã có từ SPEC, không phải giá trị mới) đều = 300ms — nhờ vậy
`onHandLost()` (chỉ được lớp gọi gọi SAU KHI đã qua `LOST_HAND_MS`) vừa đúng
lúc để cũng dùng làm mốc nhả hold, không cần thêm bộ đếm thời gian riêng cho
`HOLD_LOST_MS`. Nếu sau này 2 hằng số này tách giá trị khác nhau, chỗ này
phải sửa lại (thêm mốc thời gian riêng trong `CursorActive`).
**Chưa đo**: `HOLD_STEP_DURATION_MS=80ms` (thời lượng mỗi đoạn `continueStroke`
khi đang kéo) là số đoán, ngắn hơn `DRAG_STEP_DURATION_MS=150ms` (kéo thử cố
định của Phase 2) vì đây là kéo theo thời gian thực — chưa đo trên máy thật
xem có bị hệ thống từ chối đoạn quá ngắn, hoặc có giật hay không.

## 2026-09-20 — Sửa lỗi con trỏ không chạm được viền màn hình (dùng nhầm hằng số)
**Vấn đề**: người dùng báo "con trỏ không thể thực sự chạm tới mọi vị trí
trên màn hình, bị chặn bởi cả 4 hướng" sau khi test M4.
**Root cause**: `UnTouchAccessibilityService.toScreenPx()` dùng
`GestureThresholds.EDGE_MARGIN` để kẹp vị trí PIXEL con trỏ vào khoảng
5%-95% chiều rộng/cao màn hình. Nhưng theo đúng định nghĩa gốc trong SPEC
mục 6 (bảng ngưỡng), `EDGE_MARGIN` là ngưỡng "**tay** chạm lề **khung hình
camera** = coi như ra ngoài" — hoàn toàn khác mục đích, bị tái sử dụng nhầm
sang việc giới hạn vị trí hiển thị trên màn hình từ lúc code Phase 4, không
ai phát hiện ra vì lúc đó con trỏ hiếm khi được test tới sát biên.
**Chọn**: bỏ hẳn việc dùng `EDGE_MARGIN` trong `toScreenPx()` — kẹp toạ độ
con trỏ vào đúng `0..chiều rộng/cao màn hình thật`, không trừ hao biên nào.
**Vì**: `EDGE_MARGIN` không có ý nghĩa gì với vị trí hiển thị trên màn hình;
một cử chỉ điều khiển thay chuột phải chạm được MỌI điểm, kể cả icon/nút sát
mép màn hình (thanh trạng thái, thanh điều hướng, nút gạt thông báo...).
**Đã loại**: giữ nguyên `EDGE_MARGIN` cho mục đích này nhưng giảm phần trăm
xuống — không hợp lý vì đây không phải "chỉnh độ lớn margin", mà là dùng SAI
hằng số ngay từ đầu cho 2 khái niệm khác nhau hoàn toàn (tay ra khỏi khung
hình camera ≠ vị trí con trỏ trên màn hình).

## 2026-09-20 — Thêm màu con trỏ theo trạng thái để người dùng tự chẩn đoán M4
**Chọn**: thêm `GestureStateMachine.cursorPhase` (enum `CursorPhase`: NONE/
MOVING/SEPARATED/HOLDING/BLOCKED) - đọc được bất kỳ lúc nào sau `onFrame()`,
KHÔNG phụ thuộc `GestureAction` trả về khung đó (vì trạng thái "vừa tách,
chưa rõ click hay giữ" hiện tại không phát hành động nào nếu vị trí không
đổi). `UnTouchAccessibilityService.showCursorPhase()` đổi màu chấm tròn:
MOVING=xanh cyan (như cũ), SEPARATED=vàng, HOLDING=đỏ, BLOCKED=xám.
**Vì**: người dùng báo "giữ/kéo không thực sự tốt... nhiều thứ tôi không
thực sự rõ" và chủ động đề nghị đổi màu theo state để tự test — đây là công
cụ chẩn đoán (giống tinh thần `DebugActivity` ở Phase 1), không phải sửa
logic, giúp người dùng tự phân biệt bằng mắt lúc nào hệ thống nghĩ là "đang
di chuyển" / "đang chờ xem có phải click không" / "đang giữ/kéo thật" /
"vừa bị khoá do quá HOLD_MAX_MS" mà không cần đoán mù hay hỏi lại tôi.
**Đã loại**: chỉ đổi màu lúc phát `HoldStart`/`HoldEnd` (không cần field
`cursorPhase` riêng) — không đủ vì bỏ sót trạng thái SEPARATED (không có
GestureAction chuyên dụng nào đại diện, chỉ tồn tại ngầm trong state).

## 2026-09-20 — Lọc tách-khép quá nhanh (CLICK_MIN_MS) + tổng quát hoá phân biệt gập/tách
**Chọn** (2 phần, cả hai theo yêu cầu người dùng 2026-09-20 lan 7):
1. `CLICK_MIN_MS=100ms` — một lần tách rồi khép phải kéo dài ÍT NHẤT khoảng
   này mới được tính là click; ngắn hơn thì bỏ qua hoàn toàn (không phải
   click, cũng không phải hold).
2. Tổng quát hoá điều kiện "tách" (`bcOpen`): trước đây chỉ loại trừ tổ hợp
   bất đối xứng b,c khi ĐANG Ở CHẾ ĐỘ POINTING (biến `verticalAsymmetric`,
   xem quyết định lần 5). Nay đổi thành quy tắc chung, áp dụng MỌI CHẾ ĐỘ:
   `bcOpen` chỉ đúng khi CẢ HAI `pose.b == UP` VÀ `pose.c == UP` (còn dựng
   thẳng) VÀ khoảng cách 2 đầu ngón đã nới rộng — gập một ngón xuống (dù ở
   chế độ nào) không còn bị hiểu nhầm thành "tách" nữa. Xoá hẳn biến
   `verticalAsymmetric`, không còn cần thiết.
**Vì**: (1) tách-khép rất nhanh khi tay đang di chuyển thường là rung
tay/nhiễu tracking thoáng qua, không phải ý định click thật; (2) gập ngón
(về mặt hình học, 2 đầu ngón cũng xa nhau) và tách ngón (xoè ngang, 2 đầu
ngón vẫn cùng "dựng") là 2 tư thế vật lý khác nhau nhưng trước đây bị tính
chung, gây click/hold nhầm — vấn đề này không chỉ xảy ra ở pointing mode như
tưởng ban đầu, mà có thể xảy ra bất cứ lúc nào ngón tay tự nhiên hơi cong
khi di chuyển tay.
**Đã loại**: (1) tăng ngưỡng vote 4/5 khung của Voter — ảnh hưởng toàn hệ
thống, trong khi CLICK_MIN_MS chỉ cần tác động đúng phạm vi click; (2) giữ
`verticalAsymmetric` chỉ áp dụng cho pointing mode — bỏ sót translation mode,
không phải fix tận gốc.
**Chưa đo**: `CLICK_MIN_MS=100ms` là số đoán ban đầu, chưa đo có lọc mất
click thật (nhanh, có ý) hay không.

## 2026-09-20 — Thêm thanh trượt "Độ nhạy tách ngón" (G_OPEN)
**Chọn**: đổi `G_OPEN` (hằng số đo thật Phase 1 = 1.85) thành `var`, thêm
thanh trượt "Độ nhạy tách ngón (click/giữ, M2)" trong Cài đặt — kéo sang
phải làm GIẢM `G_OPEN` (khoảng cho phép: `MIN_G_OPEN=1.45` đến
`MAX_G_OPEN=1.85`), tức xoè 2 ngón ít hơn vẫn được tính là "tách". `G_CLOSE`
giữ nguyên `const val = 1.3` (không đổi, không có bằng chứng cần chỉnh).
**Vì**: người dùng báo "khi tách ra tay dễ rơi khỏi màn hình [khung hình
camera]" — phải xoè ngón đúng bằng mức đo thật (1.85) khiến tay/ngón bị đẩy
ra gần mép khung hình camera, mất tracking giữa lúc đang click/giữ. Đây là
vấn đề CẢM GIÁC DÙNG cá nhân (tuỳ cách cầm máy, khoảng cách, cỡ tay) — đúng
loại quyết định nên để người dùng tự chỉnh qua thanh trượt (giống các
ngưỡng vuốt trước đó) thay vì tôi đoán 1 con số cố định mới, tránh thêm
nhiều vòng đoán-test-báo lại.
**Đã loại**: hạ thẳng `G_OPEN` xuống 1 giá trị cố định thấp hơn — không có
căn cứ chọn đúng số, và hạ quá thấp sẽ tăng nguy cơ nhận nhầm "tách" trong
lúc di chuyển bình thường (đánh đổi ngược lại với CLICK_MIN_MS vừa thêm).
**Chưa đo**: `MIN_G_OPEN=1.45` (giữ cách `G_CLOSE=1.3` ít nhất 0.15) là biên
dưới đoán để tránh xoá hết vùng đệm hysteresis — chưa xác nhận trên máy thật
đây có phải mức thấp nhất còn dùng được hay đã đủ thấp để tay không cần xoè
rộng nữa.

## 2026-09-20 — Phase 6 (M5): thêm PairState riêng ở ngưỡng G_OPEN4, không tái dùng bc/cd của M2
**Chọn**: thêm 3 field mới vào `StablePose` (`bcWide`, `cdWide`, `deWide`),
tính bằng đúng công thức `rawPair` đã có (nay tham số hoá ngưỡng, giống mẫu
đã dùng cho `rawFinger`/`CURSOR_POINTING_R_DOWN`) nhưng dùng `G_OPEN4=2.0`
(cố định) làm ngưỡng "rộng", thay vì tái dùng `pose.bc`/`pose.cd` (dùng
`G_OPEN`, nay là `var` người dùng tự chỉnh 1.45-1.85 cho M2). Tư thế vào M5
= b,c,d,e dựng, a gập, ÍT NHẤT 1 trong 3 cặp liền kề đạt `*Wide == OPEN`.
Huỷ M5 = CẢ 3 cặp `*Wide == CLOSE`.
**Vì**: `G_OPEN` và `G_OPEN4` đo 2 khái niệm khác nhau (Phase 1: tách 2 ngón
p10=1.88 vs xoè 4 ngón p10=2.17) và giờ `G_OPEN` còn bị người dùng tự chỉnh
qua thanh trượt cho mục đích RIÊNG của M2 — nếu M5 tái dùng `pose.bc`/`pose.cd`
(ngưỡng `G_OPEN`), người dùng hạ độ nhạy tách ngón cho M2 sẽ VÔ TÌNH làm M5
dễ kích hoạt nhầm hơn (tác dụng phụ không mong muốn, không liên quan gì tới
mục đích họ đang chỉnh).
**Đã loại**: tái dùng trực tiếp `pose.bc`/`pose.cd` cho M5 — tiết kiệm code
hơn nhưng tạo phụ thuộc chéo ẩn giữa thanh trượt M2 và ngưỡng vào M5, khó
phát hiện khi debug sau này.
**Chọn** (huỷ M5): dùng đúng điều kiện SPEC "khép cả 4 ngón" (mọi cặp liền
kề `<= G_CLOSE`) làm cách DUY NHẤT thoát M5 trong lúc đang hoạt động — KHÔNG
áp dụng kiểu `entryTargetOf(pose) != target -> huỷ` như M1 (rời tư thế vào là
huỷ ngay). **Vì**: SPEC chỉ định nghĩa "huỷ: khép cả 4 ngón", không nói gì về
rời tư thế vào; và bản chất M5 chỉ cần 1 cú vẩy nhanh rồi có thể tay đã lệch
tư thế trong lúc vẩy — áp dụng cách huỷ nghiêm ngặt của M1 sẽ dễ huỷ M5 giữa
chừng trước khi kịp vẩy xong.
**Chọn** (trục trái/phải): tái dùng NGUYÊN VẸN công thức velocity/direction
của `detectSwipe` (đã xác nhận đúng trên máy thật ở M1, kể cả phần "lật
gương" mà SPEC mục 4.5 nhắc riêng cho M5) — không suy diễn lại quy ước trục
từ đầu cho M5.
**Chưa đo**: toàn bộ M5 (Phase 6) code xong nhưng CHƯA test trên máy thật —
chưa xác nhận tư thế "4 ngón xoè hơi rời" tới `G_OPEN4=2.0` có tự nhiên/thoải
mái hay gặp vấn đề tương tự M4 (khó điều khiển, dễ mất tracking khi xoè rộng).

## 2026-09-20 — M5 (lần 8): bỏ hẳn Trái/Phải, tách 2 tư thế Khép/Tách để giữ đủ 4 hành động
**Vấn đề**: test thật đầu tiên — vào M5 dễ, khép tay tự huỷ đúng, NHƯNG vẫy
Trái/Phải (Back/Đa nhiệm) gần như không hoạt động ("Back chưa từng thành
công, Home chỉ 1 lần"), trong khi Xuống (Thanh thông báo) hoạt động tốt. Đã
thử thêm log tạm (`UnTouchSystemDebug`) để đo velX/velY thật nhưng log KHÔNG
bắt được dòng nào — thiết bị (Realme UI) xả log hệ thống rất dày (dump pin,
window manager...) khiến buffer logcat mặc định bị ghi đè trước khi kịp lấy
ra; không kịp điều tra sâu hơn bằng log trước khi người dùng đã tự chỉ ra
hướng giải quyết.
**Chọn** (theo đúng yêu cầu người dùng, không phải tôi tự suy ra ngưỡng
mới): bỏ HẲN việc dùng trục Trái/Phải cho M5. Tách thành 2 TƯ THẾ riêng biệt
(`SystemPoseMode`), mỗi tư thế chỉ dùng Lên/Xuống để kích hoạt 1 trong 2
hành động:
- **KHÉP** (`CLOSED`, mới thêm): b,c,d,e dựng, cả 3 cặp liền kề đều khép
  (`<= G_CLOSE`, dùng lại `bcWide/cdWide/deWide` nhưng đọc nhánh CLOSE) —
  Lên = Back, Xuống = Đa nhiệm. Huỷ bằng cách TÁCH ra (ngược lại tư thế vào).
- **TÁCH** (`SPREAD`, tư thế M5 gốc, giữ nguyên): Lên = Home, Xuống = Thanh
  thông báo (đã xác nhận Xuống hoạt động tốt trên máy thật). Huỷ bằng cách
  KHÉP lại (như cũ, đã xác nhận hoạt động tốt).
`GestureAction.SystemAction` đổi từ mang `Direction` sang mang thẳng
`SystemActionType` (BACK/RECENTS/HOME/NOTIFICATIONS) — lớp Android không cần
biết gì về tư thế/hướng nữa, chỉ map thẳng loại hành động sang `GLOBAL_ACTION_*`.
**Vì**: dữ liệu thật cho thấy vẫy dọc (Lên/Xuống) dễ kiểm soát hơn hẳn vẫy
ngang (Trái/Phải) khi tay đang giữ tư thế 4 ngón — đúng nhận định của người
dùng ("vấn đề nằm ở chỗ thao tác"), không phải do ngưỡng vận tốc/tỷ lệ trục
sai. Dùng 2 tư thế (Khép/Tách) thay vì 4 hướng để vẫn giữ đủ 4 hành động mà
KHÔNG cần trục ngang.
**Đã loại**: hạ `SYS_VEL_MIN`/`AXIS_RATIO` để Trái/Phải dễ kích hoạt hơn —
không giải quyết đúng vấn đề gốc (khó điều khiển ổn định theo trục ngang khi
giữ tư thế này), chỉ là vá triệu chứng; người dùng đã tự xác định hướng giải
quyết đúng hơn (đổi hẳn cơ chế) nên làm theo.
**Ánh xạ Lên/Xuống → hành động cụ thể trong mỗi tư thế**: KHÉP dùng
Lên=Back/Xuống=Đa nhiệm; TÁCH dùng Lên=Home/Xuống=Thanh thông báo — đây là
lựa chọn của tôi (không do người dùng chỉ định chính xác cặp nào ứng với
hướng nào), giữ nguyên cặp Home/Thông báo đã xác nhận hoạt động tốt ở tư thế
TÁCH, gán Back/Đa nhiệm còn lại cho tư thế KHÉP mới. Cần người dùng test và
xác nhận/đổi lại nếu thấy ghép cặp khác dễ nhớ hơn.
**Chưa đo**: toàn bộ thiết kế mới CHƯA test trên máy thật — chưa xác nhận tư
thế KHÉP (4 ngón dựng thẳng, khép sát nhau) có dễ vào nhầm từ cử chỉ nghỉ tay
bình thường hay không (rủi ro mới, khác với tư thế TÁCH cũ ít giống dáng tay
nghỉ hơn).

## 2026-09-20 — M5 (lần 9): đổi lại trục kích hoạt thành Trái/Phải
**Chọn**: theo yêu cầu trực tiếp của người dùng, đổi trục kích hoạt trong
`detectSystem` từ Lên/Xuống (lần 8) TRỞ LẠI Trái/Phải, GIỮ NGUYÊN cấu trúc 2
tư thế Khép/Tách vừa thêm (không quay lại 1 tư thế 4 hướng cũ). Mỗi tư thế
vẫn cho ra 1 cặp hành động, chỉ đổi cách CHỌN giữa 2 hành động trong cặp đó
từ Lên/Xuống sang Trái/Phải: KHÉP (Trái=Back, Phải=Đa nhiệm — đúng khớp cặp
gốc trong SPEC), TÁCH (Trái=Home, Phải=Thanh thông báo — không có tiền lệ
trong SPEC gốc vì SPEC gốc dùng Lên/Xuống cho cặp này, chọn Trái=Home/
Phải=Thông báo theo đúng thứ tự đã dùng cho cặp Khép, cho nhất quán).
**Vì**: người dùng yêu cầu trực tiếp, không giải thích thêm — không phải kỹ
thuật nên làm theo, không tự tranh luận lại. Cấu trúc 2 tư thế (đã xác nhận
vào/huỷ hoạt động tốt ở lần 8) được giữ nguyên vì người dùng không yêu cầu
đổi phần đó.
**Đã loại**: quay lại thiết kế gốc (1 tư thế, 4 hướng) — người dùng không
yêu cầu bỏ cấu trúc 2 tư thế, chỉ yêu cầu đổi trục kích hoạt.
**Chưa đo**: trục Trái/Phải là đúng cái đã gây khó khăn ở lần test trước
("Back chưa từng thành công") — CHƯA rõ liệu vấn đề trước đó là do bản thân
trục Trái/Phải khó thao tác, hay do TƯ THẾ 4 ngón TÁCH (xoè rộng) cụ thể lúc
đó làm khó thêm; nếu vẫn khó sau lần đổi này, nghi ngờ chính nên chuyển sang
tư thế cụ thể (KHÉP mới thêm có thể dễ vẫy ngang hơn TÁCH cũ) thay vì trục.

## 2026-09-20 — M5 (lần 10): ngưỡng "dựng" riêng cho ngón út, tránh lẫn với M1 3 ngón
**Vấn đề**: người dùng báo "4 ngón dơ lên dễ bị lẫn với 3 ngón nhưng vẫn
hoạt động được" — tức là đôi khi cố dựng cả 4 ngón lên (định vào M5) lại bị
hệ thống nhận thành tư thế 3 ngón (M1 ngang, `isHorizontalEntryPose` yêu cầu
`e` HẠ). Không sai logic, chỉ là ranh giới giữa 2 tư thế quá sát.
**Root cause**: điểm khác biệt DUY NHẤT giữa "3 ngón" (M1 ngang: b,c,d dựng,
e hạ) và "4 ngón" (M5, cả 2 biến thể: b,c,d,e đều dựng) là trạng thái của
đúng 1 ngón — ngón út (e). Ngón út về mặt giải phẫu thường khó duỗi thẳng
hết cỡ bằng 3 ngón kia, nên khi người dùng cố dựng cả 4 ngón, tỷ lệ `r_e`
thực tế thường không đạt tới `R_UP=0.90` (ngưỡng dùng chung cho mọi ngón),
rơi vào vùng xám hoặc bị đọc thành "hạ" — lọt vào đúng điều kiện của M1 3
ngón thay vì M5.
**Chọn**: thêm `GestureThresholds.SYSTEM_E_UP_RELAXED=0.78` (giữa
`R_DOWN=0.65` và `R_UP=0.90`) và `StablePose.eUpRelaxed` (Voter riêng, cùng
mẫu với `bDownRelaxed`/`cDownRelaxed`/`CURSOR_POINTING_R_DOWN` đã làm trước
đó) — CHỈ dùng cho việc xét "ngón út đã dựng" trong 2 tư thế vào M5
(`isSystemSpreadEntryPose`/`isSystemClosedEntryPose`). M1 ngang vẫn dùng
`pose.e` bình thường (xét "hạ" bằng `R_DOWN`, không đổi, không bị ảnh hưởng).
**Vì**: `pose.e` (dùng `R_UP` toàn cục) còn được M1 ngang dùng để xét chiều
NGƯỢC LẠI ("hạ") — hạ thẳng `R_UP` toàn cục sẽ khiến M1 ngang dễ bị lẫn
ngược lại thành M5 (đánh đổi 1-1, không giải quyết gì). Thêm ngưỡng riêng
CHỈ áp dụng cho hướng kiểm tra mà M1 không dùng (M1 không bao giờ cần xét
"e dựng") nên an toàn, không tạo đánh đổi ở phía M1.
**Đã loại**: hạ `R_UP` toàn cục — ảnh hưởng M1 ngang không có bằng chứng cần
sửa, vi phạm nguyên tắc chỉ sửa đúng nơi có bằng chứng lỗi.
**Chưa đo**: `SYSTEM_E_UP_RELAXED=0.78` là số đoán (giữa 2 ngưỡng đã đo),
chưa xác nhận trên máy thật đã đủ lỏng để hết nhầm lẫn 4↔3 ngón hay chưa,
và ngược lại có làm M1 ngang (3 ngón, ngón út thật sự hạ) dễ bị lẫn thành M5
hay không (rủi ro lý thuyết: nếu ngón út "hạ" của người dùng vẫn còn hơi
duỗi, `r_e` có thể rơi vào vùng 0.78-0.90 và giờ bị `eUpRelaxed` đọc thành
"dựng" — cần theo dõi thêm).

## 2026-09-20 — Tách màu icon cho M1 2 ngón và 3 ngón
**Vấn đề**: người dùng báo "màu khi dơ 3 ngón đang trùng với 2 ngón" — cả
`SwipeAxisMode.VERTICAL` (2 ngón, Lên/Xuống) và `HORIZONTAL` (3 ngón,
Trái/Phải) đều chỉ map ra chung 1 `DisplayState.M1` (từ khi tách 2 tư thế
loại trừ nhau, 2026-09-19), nên `OverlayRenderer` không thể tô 2 màu khác
nhau dù đây là 2 tư thế hoàn toàn khác biệt về finger pose.
**Chọn**: tách `DisplayState.M1` thành `M1_VERTICAL`/`M1_HORIZONTAL`.
`GestureStateMachine.displayState` đọc `SwipeAxisMode` đã có sẵn trong
`InternalState.SwipeActive` để chọn đúng nhánh. `OverlayRenderer`: VERTICAL
giữ màu xanh lá (như M1 cũ), HORIZONTAL đổi sang vàng.
**Vì**: đơn giản nhất — `SwipeAxisMode` (VERTICAL/HORIZONTAL) đã tồn tại sẵn
trong state, chỉ cần expose ra `DisplayState` thay vì thêm cơ chế "phase"
song song (kiểu `CursorPhase`) vốn dành cho khi 1 state cần NHIỀU hơn 1
chiều thông tin hiển thị — ở đây chỉ cần 1 enum tách đôi là đủ.
**Đã loại**: thêm `SwipeAxisMode` như 1 phase riêng (mẫu `CursorPhase`) —
thừa, vì display state ở đây là ánh xạ 1-1 đơn giản, không có tổ hợp nhiều
chiều nào cần biểu diễn thêm.

## 2026-09-20 — Tách màu icon cho M5 Khép và Tách
**Chọn**: nối tiếp quyết định ngay trên (tách màu M1_VERTICAL/M1_HORIZONTAL),
người dùng yêu cầu làm luôn cho cặp M5 — tách `DisplayState.M5` thành
`M5_CLOSED`/`M5_SPREAD`, đọc `SystemPoseMode` đã có sẵn trong
`InternalState.SystemActive` (cùng mẫu `SwipeAxisMode`). `OverlayRenderer`:
SPREAD giữ màu tím/magenta (như M5 cũ), CLOSED dùng màu xanh dương.
**Vì**: cùng lý do với M1 — 2 tư thế con khác biệt hoàn toàn về finger pose
nên cần phân biệt được qua icon, không chỉ phân biệt ở cấp "chế độ".
**Bảng màu icon đầy đủ sau 2 lần tách này**: ARMING=xám, M1_VERTICAL=xanh
lá, M1_HORIZONTAL=vàng, M2=cyan, M5_SPREAD=tím, M5_CLOSED=xanh dương.

## 2026-09-20 — M5 (lần 13): root cause thật của nhầm lẫn 3 ngón ↔ 4 ngón khép
**Vấn đề**: sau khi thêm `SYSTEM_E_UP_RELAXED` (lần 10), người dùng vẫn báo
"vẫn còn nhầm lẫn giữa 3 ngón và 4 ngón khép".
**Đo được (yêu cầu người dùng đọc màn hình Debug)**: `r_e` lúc "3 ngón" ≈
0.45, lúc "4 ngón khép" ≈ 0.8. Hai số này thực ra tách biệt khá rõ so với
`SYSTEM_E_UP_RELAXED=0.78` — 0.45 nằm sâu dưới `R_DOWN=0.65` (chắc chắn
"hạ"), 0.8 vượt 0.78 (chắc chắn "dựng" theo ngưỡng lỏng). Vậy bản thân
ngưỡng KHÔNG sai — vấn đề nằm ở chỗ khác.
**Root cause thật**: 0.8 nằm trong VÙNG XÁM của `pose.e` theo thang đo
THƯỜNG (`R_DOWN=0.65` đến `R_UP=0.90`) — tức là mỗi khung ở giá trị này,
`rawFinger` trả về `AMBIGUOUS`, giá trị TRUNG LẬP của `Voter`. Theo đúng
thiết kế `Voter` (tránh nhấp nháy quanh ngưỡng): giá trị trung lập KHÔNG BAO
GIỜ được tính vào phiếu bầu, nên KHÔNG THỂ ghi đè kết luận ổn định CŨ. Nếu
người dùng vừa làm "3 ngón" (đã ổn định `pose.e = DOWN`) rồi chuyển THẲNG
sang "4 ngón khép", `pose.e` (thang đo thường) bị "dính" mãi ở `DOWN` cũ vì
không có phiếu thật nào lật lại được — trong khi `pose.eUpRelaxed` (dùng
ngưỡng 0.78) hoàn toàn bình thường vì 0.8 nằm NGOÀI vùng xám của NÓ, có
phiếu `UP` thật mỗi khung. Vì `entryTargetOf` kiểm tra `isHorizontalEntryPose`
(dùng `pose.e` bị dính) TRƯỚC `isSystemClosedEntryPose` (dùng `pose.eUpRelaxed`
không bị dính), tư thế 4 ngón khép bị nhận nhầm thành 3 ngón ngay khi vào
`when`, trước khi bao giờ chạm tới nhánh System.
**Chọn**: đổi THỨ TỰ kiểm tra trong `entryTargetOf` — xét 2 tư thế System
(SPREAD/CLOSED) TRƯỚC M1 ngang/dọc. Không đổi ngưỡng số nào cả.
**Vì**: đây là lỗi về THỨ TỰ ưu tiên giữa 2 nhánh dùng 2 nguồn dữ liệu có độ
tin cậy khác nhau khi vừa chuyển tư thế (1 nhánh dùng giá trị có thể bị
dính, 1 nhánh dùng giá trị luôn cập nhật đúng) — xếp nhánh "luôn đúng" lên
trước để nó thắng trước khi nhánh "có thể dính" kịp match.
**Đã loại**: tiếp tục hạ/nâng `SYSTEM_E_UP_RELAXED` — dữ liệu đo được cho
thấy ngưỡng số đã ổn (0.45 vs 0.8 tách biệt rõ ràng), vấn đề không nằm ở
ngưỡng nên chỉnh ngưỡng thêm sẽ không giải quyết được gì, chỉ là may rủi.
**Bài học**: khi 2 tư thế loại trừ nhau nhưng dùng CHUNG 1 nguồn tín hiệu
(ở đây là `pose.e`) với 2 cách đọc khác nhau (thường vs nới lỏng), thứ tự
kiểm tra trong chuỗi `when` PHẢI ưu tiên nhánh đọc tín hiệu KHÔNG bị ảnh
hưởng bởi hiệu ứng "dính" giá trị cũ của `Voter` khi có bất kỳ chuyển tiếp
qua vùng xám nào.

## 2026-09-20 — M5 (lần 14): tách SYS_VEL_MIN thành ngưỡng độc lập, tự chỉnh được
**Chọn**: đổi `SYS_VEL_MIN` (ngưỡng vận tốc tối thiểu để 1 cú vẩy 4 ngón
được tính là hành động) từ giá trị TÍNH RA (`SWIPE_VEL_MIN_DOWN * 1.5`,
không tự chỉnh riêng được) thành `var` độc lập, có `DEFAULT/MIN/MAX` riêng
(`0.375`, khoảng cho phép `0.1`-`0.6`), thêm thanh trượt "Độ nhạy vẫy 4
ngón" trong Cài đặt (mục mới "Cử chỉ hệ thống (M5, 4 ngón)"), theo đúng mẫu
đã dùng cho các ngưỡng vuốt M1 và `G_OPEN`.
**Vì**: người dùng yêu cầu trực tiếp "tăng độ nhạy của 4 động tác 4 ngón".
Trước đây `SYS_VEL_MIN` phụ thuộc `SWIPE_VEL_MIN_DOWN` (ngưỡng vuốt Xuống
của M1) — nếu chỉnh thẳng biến đó để tăng nhạy cho M5 sẽ VÔ TÌNH đổi luôn độ
nhạy vuốt Xuống của M1 (tác dụng phụ không mong muốn), nên phải tách ra độc
lập trước khi có thể chỉnh riêng.
**Đã loại**: hạ thẳng hệ số nhân (1.5) trong công thức cũ — vẫn còn phụ
thuộc `SWIPE_VEL_MIN_DOWN`, không giải quyết được việc tách biệt 2 khái
niệm; và người dùng có thể muốn độ nhạy khác nhau cho từng lúc test, không
nên đoán 1 con số cố định mới khi đã có sẵn cơ chế thanh trượt cho việc này.
**Giá trị mặc định giữ nguyên hành vi cũ**: `0.375` = đúng bằng
`0.25 (SWIPE_VEL_MIN_DOWN mặc định) * 1.5` trước đây, nên người dùng chưa
từng chỉnh sẽ không thấy khác biệt cho tới khi chủ động kéo thanh trượt.

## 2026-09-20 — Phase 7 UI/UX: màn hình chính + hướng dẫn sử dụng + giao diện chung
**Chọn**: viết lại `MainActivity.kt` (nút bật/tắt lớn làm trọng tâm, thẻ
trạng thái 3 dòng Camera/Trợ năng/Thông báo, nút "Bật ngay" mở thẳng Cài đặt
Trợ năng khi tắt, ẩn Debug/Bơm thử vào mục "Công cụ nâng cao" thu gọn theo
mặc định). Thêm `GuideActivity.kt` (Hướng dẫn sử dụng) tóm tắt toàn bộ cử
chỉ trong SPEC bằng ngôn ngữ người dùng cuối, kèm hình minh hoạ bàn tay vẽ
bằng Compose Canvas (`HandPoseIcon`, không cần ảnh/tài nguyên ngoài, không
thêm dependency mới) và bảng màu icon trạng thái (khớp đúng màu đã dùng ở
`OverlayRenderer`/`CursorView` từ các quyết định trước). Thêm `ui/UnTouchTheme.kt`
(bộ màu Material3 dùng chung) áp dụng cho cả 5 màn hình. `SettingsActivity`
nhóm từng mục cài đặt vào `Card` riêng thay vì 1 cột dài liền mạch.
**Vì**: người dùng yêu cầu trực tiếp "làm giao diện trông đẹp (tối giản, dễ
dùng), làm thêm cả hdsd" - đúng mục Phase 7 đã ghi sẵn trong ROADMAP
("màn hình chính gọn", "màn hình hướng dẫn cử chỉ") nhưng chưa làm.
**Đã loại**: dùng thư viện icon Compose (`material-icons-core`) cho nút quay
lại - đổi sang ký tự "‹" (text thuần) để KHÔNG thêm dependency mới ngoài
những gì đã có sẵn (CLAUDE.md mục 3: không thêm thư viện ngoài nếu không
cần thiết).

**Lỗi phát hiện qua ảnh chụp màn hình thật (người dùng báo giữa chừng)**:
`MainScreen` ban đầu dùng `Spacer(Modifier.weight(1f))` để đẩy nhóm nút
xuống đáy màn hình, đặt trong 1 `Column` KHÔNG cuộn được — trên máy thật,
khung xem trước camera (đặt cố định theo `aspectRatio`) cộng với thẻ trạng
thái đã chiếm gần hết chiều cao màn hình, đẩy nút "Hướng dẫn sử dụng"/"Cài
đặt độ nhạy" ra khỏi mép dưới màn hình hoàn toàn, không cách nào bấm tới.
**Chọn (sửa)**: bỏ hẳn `Spacer(weight(1f))`, đổi khung preview sang chiều
cao CỐ ĐỊNH (`260.dp`, không phụ thuộc `aspectRatio`+`weight`), bọc toàn bộ
`Column` của `MainScreen` bằng `verticalScroll` — đảm bảo MỌI nội dung luôn
truy cập được bằng cách cuộn, bất kể kích thước màn hình/khung preview.
**Bài học**: `weight()` chỉ dùng được trong `Column`/`Row` có chiều cao/rộng
CỐ ĐỊNH (không cuộn) - kết hợp `weight()` với ý định "để sau này cho cuộn
được" là một mâu thuẫn tiềm ẩn; lẽ ra phải tự chạy thử trên thiết bị/chụp
màn hình để xác nhận layout đầy đủ trước khi báo xong, không chỉ dựa vào
build thành công.

**Hình minh hoạ bàn tay - phản hồi lần 1 (người dùng chê xấu) → sửa lại**:
bản đầu vẽ bằng `drawLine` mỏng (nét thẳng từ tâm lòng bàn tay), nhìn giống
các vệt kẻ rời rạc hơn là bàn tay. Đã vẽ lại bằng `drawRoundRect` (hình
"viên thuốc" bo tròn 2 đầu) cho cả 4 ngón chính và ngón cái, lòng bàn tay là
1 hình chữ nhật bo góc lớn - rõ ràng, dễ nhận ra tư thế tay hơn hẳn. Ngón
GẬP dùng màu xám trung tính cố định (`FingerDownColor`) thay vì cùng màu
với ngón DỰNG chỉ giảm độ mờ - tương phản rõ hơn giữa 2 trạng thái.

**Tiếng Việt có dấu**: theo yêu cầu người dùng, toàn bộ CHỮ HIỂN THỊ TRÊN
MÀN HÌNH (không phải comment trong code) ở `MainActivity`, `SettingsActivity`,
`GuideActivity`, `DebugActivity`, `GestureTestActivity` đổi từ tiếng Việt
không dấu sang có dấu đầy đủ. Comment trong code GIỮ NGUYÊN không dấu theo
đúng quy ước đã dùng xuyên suốt dự án từ đầu (không đổi, ngoài phạm vi được
yêu cầu và sẽ là 1 diff khổng lồ không liên quan tới giao diện).
**Chưa đo**: toàn bộ thay đổi giao diện CHƯA có phản hồi cảm giác dùng thật
từ người dùng (mới xác nhận qua ảnh chụp màn hình do Claude tự chụp) - cần
người dùng tự cầm máy dùng thử và cho ý kiến thêm.

## 2026-09-20 — Không thể tự động bật/tắt Trợ năng theo app
**Vấn đề**: người dùng hỏi có cách nào app tự bật Trợ năng khi mở app, tự
tắt khi đóng app không.
**Kết luận**: KHÔNG THỂ, ở mọi mức độ code - Android chặn cứng ở tầng hệ
điều hành, không cho bất kỳ app nào tự bật/tắt dịch vụ Trợ năng (kể cả của
chính nó) bằng API, bắt buộc người dùng tự bấm công tắc trong Cài đặt hệ
thống. Đây là giới hạn bảo mật chủ đích (ngăn app độc hại tự cấp quyền cực
mạnh cho mình), không phải thiếu sót có thể vá bằng code, và không lách được
nếu không root máy (ngoài phạm vi app này).
**Thử (giảm ma sát thay vì tự động hoá) rồi PHẢI REVERT — gây crash thật**:
lần đầu đổi nút "Bật ngay" sang deep link thẳng tới trang chi tiết CÔNG TẮC
CỦA UnTouchMove (`android.settings.ACCESSIBILITY_DETAILS_SETTINGS`, API 30+,
kèm `EXTRA_COMPONENT_NAME`), có `resolveActivity()` kiểm tra trước tưởng là
đủ an toàn. Trên máy thật, `resolveActivity()` trả về non-null (có Activity
xử lý action này) NHƯNG lúc thật sự `startActivity()` thì ném
`SecurityException: ... requires android.permission.OPEN_ACCESSIBILITY_DETAILS_SETTINGS`
— làm crash cả `MainActivity` (người dùng thấy hộp thoại lỗi hệ thống, phải
báo lại kèm ảnh chụp màn hình mới phát hiện ra). Nguyên nhân:
`resolveActivity()` chỉ kiểm tra CÓ activity khớp action, KHÔNG kiểm tra ứng
dụng gọi có đủ quyền runtime hay không — quyền này là quyền `signature`
(chỉ app hệ thống mới có), không cách nào một app thường xin được.
**Chọn (sau khi sửa)**: quay lại `ACTION_ACCESSIBILITY_SETTINGS` (trang danh
sách chung) — cách DUY NHẤT hoạt động ổn định cho app thường. Vẫn giữ dòng
nhắc "Cho phép cài đặt hạn chế" (Android 13+) ngay cạnh nút "Bật ngay" trên
`MainActivity` (trước đó chỉ có ở màn hình Bơm thử cử chỉ ít người vào tới).
**Đã loại**: mọi hướng "tự động hoá" (Accessibility Service API, Shizuku,
AppOps qua reflection...) - đều hoặc không tồn tại cho mục đích này, hoặc
cần root/ADB thủ công mỗi lần (không phải trải nghiệm "tự động" thật sự),
không phù hợp với app không root, không quyền đặc biệt như CLAUDE.md yêu cầu.
**Bài học**: `resolveActivity() != null` KHÔNG đồng nghĩa "gọi
`startActivity()` sẽ an toàn" — với các Intent action nội bộ của Settings
(không có hằng số công khai chính thức trong SDK, chỉ là chuỗi string), cần
thận trọng hơn (thử trên máy thật trước khi tin dùng) thay vì chỉ dựa vào
kiểm tra resolve.

## 2026-09-20 — Thêm icon app (trước đó dùng icon mặc định của Android)
**Vấn đề**: người dùng báo "App đang thiếu logo" — kiểm tra lại thì đúng là
từ đầu dự án CHƯA từng thêm tài nguyên icon nào (`AndroidManifest.xml` không
có `android:icon`, không có file nào trong `mipmap*/`), nên hệ thống tự
dùng icon robot xanh mặc định của Android cho package chưa khai báo icon.
**Chọn**: thêm 1 icon thích ứng (adaptive icon, chỉ cần XML thuần, không cần
ảnh PNG nào vì `minSdk=29 > 26` - API tối thiểu dự án hỗ trợ đã cao hơn mức
Android giới thiệu adaptive icon, không cần lo thiết bị cũ hơn):
- `res/drawable/ic_launcher_foreground.xml`: vector logo đơn giản - 2 "ngón
  tay" dạng viên thuốc + lòng bàn tay bo góc, cùng phong cách với
  `HandPoseIcon` ở `GuideActivity` (đúng tư thế cử chỉ con trỏ M2, bỏ ngón
  cái cho gọn), vẽ bằng path SVG thuần trong vector drawable.
- `res/values/colors.xml`: màu nền icon = đúng màu chính (Teal 800) của
  `UnTouchTheme` để nhất quán với giao diện trong app.
- `res/mipmap-anydpi-v26/ic_launcher.xml`: ghép nền + logo thành
  `<adaptive-icon>`. Dùng CHUNG 1 file cho cả `android:icon` và
  `android:roundIcon` trong manifest (hệ thống tự áp mặt nạ tròn/vuông bo
  góc tuỳ launcher, không cần 2 file riêng).
**Vì**: không cần thêm dependency/công cụ tạo icon nào (Android Studio Image
Asset Studio thường dùng để tạo icon từ ảnh nguồn, nhưng ở đây không có sẵn
ảnh logo nào để bắt đầu từ đó) - vẽ trực tiếp bằng vector đơn giản, tái dùng
đúng phong cách hình minh hoạ bàn tay đã làm ở màn hình Hướng dẫn sử dụng.
**Đã loại**: thêm icon PNG raster ở nhiều mật độ (`mipmap-mdpi/hdpi/...`) -
không cần thiết vì `minSdk=29` đã vượt xa API 26 (adaptive icon), chỉ cần
đúng 1 bộ tài nguyên vector cho `mipmap-anydpi-v26`.
**Đã xác nhận trên máy thật**: icon hiển thị đúng trên màn hình chính sau
khi cài lại (ô vuông bo góc màu xanh teal, hình 2 ngón tay + lòng bàn tay
màu trắng), thay cho icon robot mặc định trước đó.

## 2026-09-20 — Thêm "chiều sâu" cho giao diện (elevation/shadow trên nền tối giản có sẵn)
**Chọn**: giữ nguyên bố cục tối giản của Phase 7, chỉ thêm lớp "nổi khối":
`CardDefaults.cardElevation(defaultElevation = 3.dp)` + `shape =
RoundedCornerShape(20.dp)` cho MỌI `Card` (3 card ở `GuideActivity`, các
`SettingsSection` ở `SettingsActivity`, `StatusCard` ở `MainActivity`);
`Modifier.shadow()` cho khối xem trước camera và nút bật/tắt chính (dạng
viên thuốc, `CircleShape`); đổi 3 dòng trạng thái quyền từ chấm tròn phẳng
sang icon (✓/!) đặt trong vòng tròn nền màu nhạt; thêm nền màu nhạt (tint
10% màu của tư thế) phía sau mỗi `HandPoseIcon` ở màn Hướng dẫn. Viết lại
`ui/UnTouchTheme.kt` với bộ màu tonal Material3 đầy đủ (`primaryContainer`,
`surfaceVariant`, `outline`...) thay vì chỉ `primary`/`secondary` — cần thiết
để Card có `containerColor` tương phản đủ với nền thì elevation mới "nổi" rõ
lên được, không chỉ đổ bóng suông.
**Vì**: người dùng yêu cầu trực tiếp "sửa lại giao diện vẫn là thiết kế tối
giản nhưng làm có chiều sâu và đẹp hơn" sau khi đã duyệt qua bản Phase 7.
**Đã loại**: không thêm thư viện animation/shadow ngoài (vd Accompanist) -
`Modifier.shadow()` và `CardDefaults.cardElevation()` của Compose Material3
có sẵn đã đủ, không cần dependency mới (CLAUDE.md mục 3).
**Lỗi phát hiện qua ảnh chụp màn hình thật khi kiểm tra lại**: nút "Hướng
dẫn sử dụng" ở `MainActivity` bị ép `Modifier.height(48.dp)` cứng — khi chữ
"Hướng dẫn sử dụng" xuống dòng thành 2 dòng (do cột hẹp lại còn 1 nửa màn
hình, chia đôi với nút "Cài đặt độ nhạy"), dòng thứ 2 bị cắt mất phần dưới
vì chiều cao cố định không đủ chỗ. **Sửa**: bỏ `height(48.dp)`, để nút tự
giãn theo nội dung; đổi cỡ chữ 2 nút này sang `bodyMedium` (nhỏ hơn mặc định
`labelLarge` của `OutlinedButton`) + `textAlign = TextAlign.Center` cho cân
đối khi xuống dòng. Lỗi này không liên quan trực tiếp tới yêu cầu "chiều
sâu" nhưng phát hiện được nhờ đúng quy trình chụp ảnh xác nhận trên máy thật
sau mỗi lần sửa giao diện — tiếp tục áp dụng quy trình này cho mọi thay đổi
UI sau này.
**Đã xác nhận trên máy thật (RMX3370)**: chụp ảnh cả 3 màn hình
Main/Guide/Settings sau khi cài lại — Card nổi khối rõ so với nền, khối
camera/nút bật có shadow, icon trạng thái dạng vòng tròn màu dễ nhìn hơn
chấm phẳng cũ, nút "Hướng dẫn sử dụng" hiển thị đủ 2 dòng không bị cắt.
**Chưa đo**: cảm giác dùng thật (đẹp hơn hay không) chưa có phản hồi trực
tiếp từ người dùng, mới xác nhận bằng ảnh chụp màn hình do Claude tự chụp.

## 2026-09-21 — Cài đặt: bật/tắt từng nhóm cử chỉ (M1/M2/M5)
**Chọn**: thêm 3 `var Boolean` trong `GestureThresholds`
(`ENABLE_M1_SWIPE`/`ENABLE_M2_CURSOR`/`ENABLE_M5_SYSTEM`, mặc định `true`),
theo đúng pattern các ngưỡng chỉnh-qua-Cài-đặt đã có (`G_OPEN`,
`CURSOR_POINTING_MODE`...). Gate ở đúng 1 chỗ duy nhất —
`GestureStateMachine.entryTargetOf()` — nhóm nào tắt thì hàm này không trả
về `ArmTarget` cho tư thế vào của nhóm đó nữa, không đụng logic bên trong
`detectSwipe`/`detectCursor`/`detectSystem`. Vì `SwipeActive` gọi lại
`entryTargetOf()` mỗi khung để kiểm tra còn đúng tư thế không, tắt M1 khi
đang vuốt dở tự động huỷ về IDLE ngay khung tiếp theo — không cần code thêm.
M2/M5 không có hành vi tự huỷ này khi tắt giữa chừng (2 chế độ đó vốn không
gọi lại `entryTargetOf()` liên tục vì lý do khác, xem comment tại
`detectCursor`/`detectSystem`) — chấp nhận, chỉ chặn được lần vào tiếp theo,
giống đúng cách M2 vốn đã hoạt động (chỉ thoát bằng xoè 5 ngón).
UI: 3 công tắc (`Switch`) trong 1 `SettingsSection` mới "Bật/tắt từng cử
chỉ", đặt đầu `SettingsScreen`. Lưu qua `SettingsRepository`
(`booleanPreferencesKey`), `GestureForegroundService.observeSettings()`
collect và gán thẳng vào `GestureThresholds` — áp dụng ngay, không cần khởi
động lại service/camera (giống các cờ khác, khác `HAND_DETECTION_CONFIDENCE`
vốn cần tạo lại `HandLandmarker`).
**Vì**: SPEC mục 7 đã chốt sẵn "Chỉ có: hệ số con trỏ, chọn tay thuận, và
bật/tắt từng nhóm cử chỉ" và ROADMAP Phase 7 có mục chưa làm đúng tên này —
không phải tính năng tự thêm ngoài SPEC.
**Đã loại**: không làm bật/tắt chi tiết hơn (từng hướng vuốt, từng hành
động M5 riêng — Back/Recents/Home/Notifications) vì SPEC chỉ nói "từng
NHÓM cử chỉ", không phải từng hành động con; thêm granularity đó là đoán
thêm ngoài đặc tả.
**Chưa làm**: "chọn tay thuận" (phần còn lại của cùng mục ROADMAP) — để
riêng, không lẫn vào cùng 1 thay đổi vì không liên quan logic bật/tắt.
**Cần test trên máy thật**: tắt từng nhóm rồi thử đúng tư thế vào của nhóm
đó xem có thật sự im lặng không, và tắt/bật lại nhiều lần xem
`SettingsScreen` có đọc đúng giá trị đã lưu khi mở lại màn hình không.
