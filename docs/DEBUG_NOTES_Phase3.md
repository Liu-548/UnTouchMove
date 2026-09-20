# Debug notes — Phase 3, GestureForegroundService crash khi bật (2026-09-20)

**CẬP NHẬT 2: sau khi sua xong crash native (muc "Nguyen nhan that" ben duoi),
camera van tu tat sau vai giay — nhung LAN NAY KHONG PHAI CRASH.** Xac nhan
qua `adb logcat` khong loc tag (khong con dropbox tombstone moi, khong
`FATAL EXCEPTION`, tien trinh (`pidof`) van song xuyen suot).

Nguyen nhan: race condition trong `MainActivity.kt` (`FrontCameraPreview`).
`MainActivity` do `GestureForegroundService.isRunning` bang polling moi 1s
(khong phai ngay lap tuc). Khi bam nut bat, service khoi dong va bind
`ImageAnalysis` cua no vao camera TRUOC; toi da 1s sau, `MainActivity` moi
phat hien `isRunning=true` va chuyen `enabled=false`, kich hoat `onDispose` cu
goi `ProcessCameraProvider.getInstance(context).get().unbindAll()`.
**`unbindAll()` la lenh toan cuc** - vi ca app dung chung 1
`ProcessCameraProvider`, no go luon `ImageAnalysis` service vua bind xong.
Xac nhan qua log: service bind camera luc 26.697, bi go luc 27.354 (~0.66s
sau) - khop chinh xac trieu chung "bat len roi tat sau vai giay", va khong co
dau vet crash nao vi day khong phai crash.

**Fix:** `FrontCameraPreview` chi `unbind(preview)` (dung use case cua rieng
no) trong `onDispose`, khong con goi `unbindAll()`. Xem git diff
`MainActivity.kt`. Build + install qua, **CAN TEST LAI TREN MAY THAT** (day
la fix chua duoc nguoi dung xac nhan tinh den luc ghi chu nay).

---

**ĐÃ TÌM RA NGUYÊN NHÂN GỐC VÀ SỬA crash native (2026-09-20, giua phien).** Xem
muc "Nguyên nhân thật (đã xác nhận qua tombstone)" bên dưới. Các mục phía dưới
(giả thuyết self-heal, LIVE_STREAM race...) là lịch sử điều tra, giữ lại để
tham khảo nhưng KHÔNG phải nguyên nhân thật.

## Nguyên nhân thật (đã xác nhận qua tombstone)

Bắt được qua `adb shell dumpsys dropbox --print` (không phải `data_app_crash`
thường mà là tombstone signal 6 — vì vậy các lần bắt log trước không thấy
`FATAL EXCEPTION`):

```
signal 6 (SIGABRT)
Abort message: 'JNI DETECTED ERROR IN APPLICATION: JNI GetObjectClass called
with pending exception java.lang.RuntimeException: Can't create handler
inside thread Thread[Thread-8,5,main] that has not called Looper.prepare()
  at android.view.ViewRootImpl$ViewRootHandler.<init>
  at android.view.WindowManagerImpl.addView
  at com.untouchmove.overlay.OverlayRenderer.show
  at com.untouchmove.service.UnTouchAccessibilityService.showStatus
  at com.untouchmove.service.ActionDispatcher.showStatus
  at com.untouchmove.service.GestureForegroundService.handleFrame
  at com.untouchmove.camera.HandLandmarkerHelper.handleResult
  at com.google.mediapipe.tasks.core.TaskRunner...
```

`HandLandmarker` ở chế độ `LIVE_STREAM` gọi `onResult`/`onError` trên **thread
nội bộ của MediaPipe** (`Thread-8`, không có Looper) — không phải thread gọi
`detectAsync`. `GestureForegroundService.handleFrame()` xử lý callback đó và
gọi thẳng `ActionDispatcher.showStatus()` → `OverlayRenderer.show()` →
`WindowManager.addView()`, nhưng thao tác này bắt buộc chạy trên thread có
Looper (main thread). Gọi sai thread → `RuntimeException` → xảy ra giữa lúc
đang trong lời gọi JNI nên biến thành lỗi native fatal (SIGABRT), giết toàn bộ
tiến trình ngay lập tức — try-catch Java không bắt được, không để lại
`FATAL EXCEPTION` bình thường trong logcat/dropbox `data_app_crash`.

So sánh: `DebugActivity` (Phase 1, chạy ổn) luôn bọc `mainHandler.post { ... }`
quanh xử lý `onResult`/`onHandLost`. `GestureForegroundService` thì không —
đây là khác biệt thật sự giữa hai nơi, không phải do LifecycleService vs
ComponentActivity như giả thuyết ban đầu.

**Fix:** chuyển việc `post` về main thread vào bên trong
`HandLandmarkerHelper` (nơi duy nhất mọi caller đi qua) thay vì bắt từng
caller tự nhớ — bọc `mainHandler.post { }` quanh cả 3 điểm gọi ra ngoài
(`onResult` qua `handleResult`, `onError` từ `setErrorListener`, và `onError`
từ catch-block trong `detectAsync`). Xem git diff
`HandLandmarkerHelper.kt`. Build + install qua, cần test lại trên máy thật.

Bước trước đó (gỡ self-heal đồng bộ trong `GestureForegroundService`) vẫn giữ
nguyên — không sai, chỉ là không phải nguyên nhân chính của crash này.

---

Ghi lại lịch sử điều tra bên dưới để tham khảo, không phải quyết định kỹ
thuật đã chốt (khác DECISIONS.md).

## Triệu chứng người dùng báo

1. Lần đầu bật "Điều khiển không chạm": camera trước đứng hình.
2. Sau khi sửa (MainActivity không giữ camera khi service chạy) + nâng
   `T_IN`: vẫn crash, nhưng chấm xanh camera "tồn tại lâu hơn".
3. Sau khi thêm log + tự phục hồi (recreate HandLandmarkerHelper khi lỗi liên
   tục): **vẫn không được — bật lên là camera trước tắt hẳn.**

## Đã xác nhận chắc chắn (có bằng chứng log/dropbox)

### Lỗi gốc ban đầu (trước khi thêm try-catch)

Crash toàn bộ tiến trình, bắt được đầy đủ qua `dumpsys dropbox --print
data_app_crash`:

```
com.google.mediapipe.framework.MediaPipeException: failed precondition: The task graph hasn't been successfully started or error occurs during graph initializaton.
	at com.google.mediapipe.tasks.core.TaskRunner.addPackets(TaskRunner.java:213)
	at com.google.mediapipe.tasks.core.TaskRunner.send(TaskRunner.java:149)
	at com.google.mediapipe.tasks.vision.core.BaseVisionTaskApi.sendLiveStreamData(BaseVisionTaskApi.java:181)
	at com.google.mediapipe.tasks.vision.core.BaseVisionTaskApi.sendLiveStreamData(BaseVisionTaskApi.java:163)
	at com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.detectAsync(HandLandmarker.java:387)
	at com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.detectAsync(HandLandmarker.java:357)
	at com.untouchmove.camera.HandLandmarkerHelper.detectAsync(HandLandmarkerHelper.kt:66)
	at com.untouchmove.service.GestureForegroundService.startCamera$lambda$6$lambda$5$lambda$4(GestureForegroundService.kt:107)
	... (ImageAnalysis analyzer callback, chay tren cameraExecutor - pool-2-thread-1)
```

Xảy ra trên **luồng gọi `detectAsync` của chính app** (không phải luồng nội
bộ MediaPipe) — về lý thuyết try-catch quanh `handLandmarker.detectAsync(...)`
trong `HandLandmarkerHelper.kt` phải bắt được.

### Sau khi thêm try-catch (commit chưa push)

- Không còn thấy `FATAL EXCEPTION` / `data_app_crash` mới nào trong dropbox.
- **Nhưng cũng không có bất kỳ dấu hiệu pipeline chạy thành công**: không
  overlay window nào của app (`dumpsys window windows | grep untouchmove`
  không ra `TYPE_ACCESSIBILITY_OVERLAY`), tức `ActionDispatcher.showStatus`
  chưa bao giờ được gọi với state khác NONE trong hơn 90 giây service sống.
- Kết luận: lỗi `MediaPipeException` này **xảy ra ở MỌI khung hình**, không
  chỉ khung đầu — không phải race lúc khởi tạo như giả thuyết ban đầu.

### Sau khi thêm self-heal (recreate HandLandmarkerHelper khi lỗi liên tục)

- Log KHÔNG bắt được dòng nào từ tag `GestureForegroundSvc` (log mình tự thêm
  cho lỗi/recreate) trong nhiều lần theo dõi — nghi vấn: hoặc lỗi không xảy ra
  theo đường tôi nghĩ, hoặc log bị mất vì tiến trình chết theo cách khác
  (không phải uncaught exception thông thường).
- Camera của **MainActivity** (`Preview` use case, thấy dòng log
  `SurfaceViewImpl: Surface set on Preview.`) mở/đóng lặp lại nhiều lần bất
  thường trong ~35 giây đầu:

  ```
  02:01:03.167  Opened (MainActivity)
  02:01:21.244  Opened lai (~18s sau, khong thay dong Closed tuong ung ro rang)
  02:01:24.104  Closed (3.5s sau)
  02:01:29.254  Opened
  02:01:33.641  Closed (4.4s sau)
  02:01:38.288  Opened
  02:03:04.129  Closed (86s sau - on dinh hon han cac chu ky truoc)
  ```

  Camera của MainActivity chỉ bind/unbind khi `enabled` (=
  `!GestureForegroundService.isRunning`) đổi giá trị (xem
  `MainActivity.kt` — `DisposableEffect(enabled)`). Chu kỳ mở/đóng lặp lại
  nhanh (~4-5s) ở đầu **gợi ý mạnh** là `isRunning` đang bị bật/tắt liên tục
  — tức **service đang bị tạo lại nhiều lần** (crash-restart qua
  `START_STICKY`), dù không thấy `FATAL EXCEPTION` trong log lần này.

## Giả thuyết nghi ngờ nhất (CHƯA xác nhận)

Cơ chế tự phục hồi (`buildHandLandmarkerHelper()` gọi lại chính nó bên trong
callback lỗi của chính nó) chạy **đồng bộ trên cùng luồng `cameraExecutor`**
đang xử lý khung hình — nếu lỗi này lặp lại ở MỌI khung hình (như mục trên đã
xác nhận), việc tạo lại `HandLandmarker` (load model, khởi tạo graph — việc
tốn thời gian) sẽ **lặp lại liên tục mỗi 5 khung lỗi** (~0.5s một lần nếu
chạy ~10fps), chặn luồng phân tích khung hình trong thời gian dài, có thể gây
ANR hoặc hệ thống chủ động kill tiến trình do "misbehaving" — khớp với việc
không thấy `FATAL EXCEPTION` bình thường (ANR/kill không tạo log giống crash
Java thông thường) nhưng camera vẫn cứ mở/đóng lặp lại.

**Nói cách khác: bản vá "tự phục hồi" ở commit gần nhất CÓ THỂ đang làm tình
hình tệ hơn (vòng lặp tái tạo liên tục) thay vì sửa được gốc rễ.**

## Việc CHƯA làm / hướng đi tiếp theo đề xuất

1. ~~Gỡ bỏ cơ chế tự phục hồi đồng bộ hiện tại~~ — **ĐÃ LÀM (2026-09-20)**:
   `GestureForegroundService.buildHandLandmarkerHelper()` giờ chỉ log lỗi kèm
   số thứ tự khung (`errorFrameCount`), không còn tự `close()` + tạo lại
   `HandLandmarkerHelper` trong callback lỗi. Build `assembleDebug` qua.
   **Cần test trên máy thật**: bật service, theo dõi `logcat -s
   GestureForegroundSvc` xem lỗi có lặp ở MỌI khung không, và camera
   MainActivity còn nhấp nháy mở/đóng liên tục như log cũ (mục "Đã xác nhận")
   không — nếu hết nhấp nháy thì xác nhận đúng self-heal đồng bộ là thủ phạm
   treo luồng.
2. Sau khi có log sạch (chỉ log lỗi, không recreate), xác nhận: lỗi
   `MediaPipeException` có lặp lại ở MỌI khung hình khi chạy trong
   `GestureForegroundService` hay không.
3. Nếu đúng là mọi khung hình đều lỗi — nghi vấn tiếp theo: có gì khác biệt
   giữa `LifecycleService` và `ComponentActivity` khiến MediaPipe LIVE_STREAM
   graph không "start" được. Vài hướng thử:
   - Thử tạo `HandLandmarker` với `RunningMode.IMAGE` (đồng bộ, không cần
     graph LIVE_STREAM) tạm thời trong service để xem có tránh được lỗi này
     không — nếu có, xác nhận vấn đề nằm ở chính co chế LIVE_STREAM trong
     context Service.
   - Thử KHÔNG dùng `LifecycleService`, tự quản lý `Lifecycle` bằng
     `LifecycleRegistry` thủ công, đảm bảo chuyển hẳn sang `STARTED`/`RESUMED`
     trước khi tạo `HandLandmarkerHelper` (hiện tại tạo helper trong
     `onCreate()`, trước khi chắc chắn lifecycle đã "STARTED").
   - Thử trì hoãn tạo `HandLandmarkerHelper` vài trăm ms sau `onCreate()`
     (qua `Handler.postDelayed`) để loại trừ hẳn khả năng race lúc khởi tạo
     graph — dù mục "Đã xác nhận" ở trên cho thấy lỗi lặp lại liên tục nên
     khả năng cao đây không phải nguyên nhân, vẫn nên loại trừ cho chắc.
4. Cân nhắc tạm thời: nếu không tìm ra nguyên nhân sớm, có thể thử đổi
   `Delegate.CPU` → thử nghiệm khác, hoặc downgrade/upgrade version
   `com.google.mediapipe:tasks-vision` (hiện `0.10.14`) xem có phải bug đã
   biết của bản này trong context Service không.

## File liên quan

- `app/src/main/java/com/untouchmove/camera/HandLandmarkerHelper.kt` — có
  try-catch quanh `detectAsync` (đã làm, xem git diff).
- `app/src/main/java/com/untouchmove/service/GestureForegroundService.kt` —
  có `buildHandLandmarkerHelper()` với self-heal (nghi ngờ đang gây hại, xem
  mục giả thuyết ở trên — cân nhắc gỡ bỏ phần recreate khi tiếp tục).
- `app/src/main/java/com/untouchmove/MainActivity.kt` — `FrontCameraPreview`
  chỉ bind khi `!running`, dùng để phát hiện `isRunning` có đang nhấp nháy.

## Trạng thái các phần khác của Phase 3 (không liên quan bug này)

- `PoseClassifier`, `GestureStateMachine`: 6 unit test pass, không đổi.
- `T_IN` đã nâng lên 0.58 — chưa kiểm chứng được trên máy thật vì bug này
  chặn đường, cần retest sau khi crash được giải quyết.
- Cơ chế bơm cử chỉ (Phase 2) vẫn hoạt động bình thường, không liên quan.
