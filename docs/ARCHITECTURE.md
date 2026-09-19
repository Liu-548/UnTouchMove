# ARCHITECTURE — UnTouchMove

## 1. Công nghệ đã chốt

| Hạng mục | Lựa chọn |
|---|---|
| Ngôn ngữ | Kotlin |
| Build | Gradle Kotlin DSL, AGP mới nhất ổn định, JDK 17 |
| minSdk / targetSdk | 29 / 36 |
| Camera | CameraX (`ImageAnalysis`) |
| Nhận diện tay | MediaPipe Tasks Vision — `HandLandmarker` (model `.task` đóng gói trong `assets/`) |
| Bơm cử chỉ | `AccessibilityService.dispatchGesture` + `performGlobalAction` |
| Overlay | `TYPE_ACCESSIBILITY_OVERLAY` (không dùng SYSTEM_ALERT_WINDOW) |
| UI | Jetpack Compose, một màn hình |
| Lưu cài đặt | DataStore Preferences |
| Bật nhanh | `TileService` (Quick Settings) |

`minSdk 29` vì: `dispatchGesture` cần API 24, `continueStroke` cần API 26,
`TYPE_ACCESSIBILITY_OVERLAY` cần API 22, và cả hai máy mục tiêu đều cao hơn 29.

## 2. Luồng dữ liệu

```
CameraX ImageAnalysis (front, ~20fps)
   │
   ▼
HandLandmarker (LIVE_STREAM, delegate GPU nếu có)
   │  world landmarks + confidence
   ▼
FeatureExtractor        → r, g, t, P, S
   │
   ▼
PoseClassifier          → ngón nào dựng/gập, khép/tách (có hysteresis + vote)
   │
   ▼
GestureStateMachine     → IDLE / ARMING / M1 / M2 (+M3, M4) / M5
   │  phát ra GestureAction
   ▼
ActionDispatcher        → gửi tới AccessibilityService
   │
   ▼
UnTouchAccessibilityService
   ├── dispatchGesture (swipe, click, drag)
   ├── performGlobalAction (back/home/recents/notifications)
   └── OverlayRenderer (icon trạng thái + con trỏ)
```

`GestureStateMachine` là **thuần Kotlin, không phụ thuộc Android** — đây là điều
kiện để unit test được mà không cần thiết bị. Nó nhận vào một `HandFrame`
(landmarks + timestamp) và trả ra `GestureAction?`.

## 3. Cấu trúc module / package

```
app/src/main/java/com/untouchmove/
├── MainActivity.kt                 Compose UI, một màn hình
├── DebugActivity.kt                màn hình đo đạc (Phase 1)
├── LauncherProxyActivity.kt        Activity trong suốt, dùng để khởi động service
├── tile/
│   └── UnTouchTileService.kt
├── camera/
│   ├── CameraSource.kt             CameraX binding
│   └── HandLandmarkerHelper.kt     MediaPipe wrapper
├── gesture/
│   ├── HandFrame.kt                data class thuần
│   ├── FeatureExtractor.kt         r, g, t, P, S
│   ├── PoseClassifier.kt           dựng/gập, khép/tách, hysteresis, vote
│   ├── GestureStateMachine.kt      máy trạng thái (thuần Kotlin)
│   ├── GestureAction.kt            sealed class các hành động
│   ├── GestureThresholds.kt        TOÀN BỘ hằng số ngưỡng ở đây
│   └── OneEuroFilter.kt            lọc rung con trỏ
├── service/
│   ├── GestureForegroundService.kt giữ camera sống, thông báo thường trú
│   └── UnTouchAccessibilityService.kt  bơm cử chỉ + overlay
├── overlay/
│   ├── OverlayRenderer.kt
│   └── CursorView.kt
└── data/
    └── SettingsRepository.kt       DataStore
```

## 4. Vòng đời và cách khởi động (quan trọng)

Từ Android 14, **không được tạo foreground service loại `camera` khi app đang ở
nền**. Vì vậy:

- Bật từ trong app: MainActivity đang hiển thị → khởi động service trực tiếp, hợp lệ.
- Bật từ Quick Settings Tile: Tile **không** tự khởi động service, mà mở
  `LauncherProxyActivity` (Activity trong suốt, không animation). Activity này
  khởi động service rồi `finish()` ngay.
  - Ghi chú: cách Tile được miễn trừ hay không còn tùy phiên bản và ROM, nên
    phải test trên cả GT Neo 2 và GT 8 Pro. Nếu proxy activity gây nháy màn hình,
    thử `TileService.startActivityAndCollapse`.
- Tắt: từ nút trong app, từ Tile, hoặc từ action trên thông báo thường trú.

Manifest cần: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CAMERA`, `CAMERA`,
và `android:foregroundServiceType="camera"`. **Không** khai báo `INTERNET`.

Khi màn hình tắt hoặc khóa: dừng phân tích khung hình, nhả camera.

## 5. Hai service, tại sao

- `GestureForegroundService`: giữ camera và pipeline sống, có thông báo thường
  trú. Người dùng bật/tắt được.
- `UnTouchAccessibilityService`: do hệ thống quản lý, người dùng bật thủ công
  trong Cài đặt, không tự bật được. Chỉ nó mới bơm được cử chỉ.

Hai bên liên lạc qua một singleton nhẹ (`ActionDispatcher` giữ tham chiếu yếu tới
accessibility service). Không dùng broadcast, không dùng AIDL — thừa.

Nếu accessibility service chưa bật, app vẫn chạy được ở chế độ chỉ hiển thị (dùng
cho màn hình Debug), nhưng không bơm cử chỉ, và UI phải báo rõ.

## 6. Hiệu năng

- Độ phân giải phân tích: 480p hoặc thấp hơn, đủ cho tracking tay ở khoảng cách
  30–60cm. Không cần nét.
- Nhịp mục tiêu: ~20fps trên GT Neo 2. Nếu đạt được 30fps thì tốt cho việc phát
  hiện tách/khép nhanh, nhưng ưu tiên **ổn định nhiệt** hơn là fps cao.
- Chỉ theo dõi **một tay** (`numHands = 1`), chọn tay thuận trong cài đặt.
- Camera trước bị lật gương: nhãn trái/phải của MediaPipe cũng bị đảo. Xử lý tập
  trung ở một chỗ duy nhất trong `HandLandmarkerHelper`, không rải rác.

## 7. Bảo mật và riêng tư

Không quyền mạng, không lưu ảnh, model chạy hoàn toàn trên máy. Ghi rõ điều này
trong README và trong app. Đây là điểm bán hàng chính của một app xin quyền
camera + accessibility — hai quyền nhạy cảm nhất trên Android.
