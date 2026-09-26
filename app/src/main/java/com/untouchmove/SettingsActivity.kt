package com.untouchmove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import com.untouchmove.ui.ScreenSurface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.untouchmove.data.SettingsRepository
import com.untouchmove.gesture.GestureThresholds
import com.untouchmove.ui.UnTouchTheme
import kotlinx.coroutines.launch

/**
 * Man hinh Cai dat do nhay (ARCHITECTURE muc 1 "Luu cai dat | DataStore
 * Preferences"). Moi thao tac 1 thanh truot rieng (yeu cau nguoi dung
 * 2026-09-20), doi la ap dung ngay cho GestureForegroundService dang chay
 * (xem SettingsRepository + GestureForegroundService.observeSettings),
 * khong can khoi dong lai app.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnTouchTheme {
                ScreenSurface() {
                    SettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

// "Nguong": gia tri CANG THAP thi CANG nhay (SWIPE_VEL_MIN_*, HAND_DETECTION_CONFIDENCE).
private fun sensitivityToVelocity(sensitivity: Float, min: Float, max: Float): Float =
    max - sensitivity * (max - min)

private fun velocityToSensitivity(velocity: Float, min: Float, max: Float): Float =
    ((max - velocity) / (max - min)).coerceIn(0f, 1f)

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    var sensUp by remember { mutableFloatStateOf(0.5f) }
    var sensDown by remember { mutableFloatStateOf(0.5f) }
    var sensLeft by remember { mutableFloatStateOf(0.5f) }
    var sensRight by remember { mutableFloatStateOf(0.5f) }
    var sensHandDetection by remember { mutableFloatStateOf(0.5f) }
    var cooldownMs by remember { mutableFloatStateOf(GestureThresholds.DEFAULT_SWIPE_COOLDOWN_MS.toFloat()) }
    // O nhap so tu do (yeu cau nguoi dung 2026-09-20: can nhan he so con tro
    // len nhieu lan, tu tinh toan lay) - khong dung thanh truot 0..1 nhu cac
    // muc khac vi khong co gioi han tren co dinh nao hop ly.
    var cursorGainTranslationText by remember { mutableStateOf(GestureThresholds.DEFAULT_CURSOR_GAIN_TRANSLATION.toString()) }
    var cursorGainPointingText by remember { mutableStateOf(GestureThresholds.DEFAULT_CURSOR_GAIN_POINTING.toString()) }
    var pointingMode by remember { mutableStateOf(false) }
    var sensGOpen by remember { mutableFloatStateOf(0f) }
    var sensSysVelMin by remember { mutableFloatStateOf(0f) }
    var enableM1Vertical by remember { mutableStateOf(true) }
    var enableM1Horizontal by remember { mutableStateOf(true) }
    var enableM2 by remember { mutableStateOf(true) }
    var enableM5 by remember { mutableStateOf(true) }
    var enableM6 by remember { mutableStateOf(true) }
    var enableScreenOffReopen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            repository.velUp.collect {
                sensUp = velocityToSensitivity(it, GestureThresholds.MIN_SWIPE_VEL_MIN_UP, GestureThresholds.MAX_SWIPE_VEL_MIN_UP)
            }
        }
        launch {
            repository.velDown.collect {
                sensDown = velocityToSensitivity(it, GestureThresholds.MIN_SWIPE_VEL_MIN_DOWN, GestureThresholds.MAX_SWIPE_VEL_MIN_DOWN)
            }
        }
        launch {
            repository.velLeft.collect {
                sensLeft = velocityToSensitivity(it, GestureThresholds.MIN_SWIPE_VEL_MIN_LEFT, GestureThresholds.MAX_SWIPE_VEL_MIN_LEFT)
            }
        }
        launch {
            repository.velRight.collect {
                sensRight = velocityToSensitivity(it, GestureThresholds.MIN_SWIPE_VEL_MIN_RIGHT, GestureThresholds.MAX_SWIPE_VEL_MIN_RIGHT)
            }
        }
        launch {
            repository.handDetectionConfidence.collect {
                sensHandDetection = velocityToSensitivity(
                    it,
                    GestureThresholds.MIN_HAND_DETECTION_CONFIDENCE,
                    GestureThresholds.MAX_HAND_DETECTION_CONFIDENCE,
                )
            }
        }
        launch {
            repository.swipeCooldownMs.collect { cooldownMs = it.toFloat() }
        }
        launch {
            repository.cursorGainTranslation.collect { cursorGainTranslationText = it.toString() }
        }
        launch {
            repository.cursorGainPointing.collect { cursorGainPointingText = it.toString() }
        }
        launch {
            repository.cursorPointingMode.collect { pointingMode = it }
        }
        launch {
            repository.gOpen.collect {
                sensGOpen = velocityToSensitivity(it, GestureThresholds.MIN_G_OPEN, GestureThresholds.MAX_G_OPEN)
            }
        }
        launch {
            repository.sysVelMin.collect {
                sensSysVelMin = velocityToSensitivity(it, GestureThresholds.MIN_SYS_VEL_MIN, GestureThresholds.MAX_SYS_VEL_MIN)
            }
        }
        launch { repository.enableM1Vertical.collect { enableM1Vertical = it } }
        launch { repository.enableM1Horizontal.collect { enableM1Horizontal = it } }
        launch { repository.enableM2Cursor.collect { enableM2 = it } }
        launch { repository.enableM5System.collect { enableM5 = it } }
        launch { repository.enableM6ScreenOff.collect { enableM6 = it } }
        launch { repository.enableScreenOffReopenGesture.collect { enableScreenOffReopen = it } }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Quay lại") }
            Text("Cài đặt độ nhạy", style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsSection(
                title = "Bật/tắt từng cử chỉ",
                description = "Tắt cử chỉ nào thì tay làm đúng tư thế đó cũng không kích hoạt gì.",
            ) {
                GestureToggleRow(
                    label = "Lướt 2 ngón (Lên/Xuống)",
                    checked = enableM1Vertical,
                    onCheckedChange = { checked ->
                        enableM1Vertical = checked
                        GestureThresholds.ENABLE_M1_VERTICAL = checked
                        scope.launch { repository.setEnableM1Vertical(checked) }
                    },
                )
                GestureToggleRow(
                    label = "Lướt 3 ngón (Trái/Phải)",
                    checked = enableM1Horizontal,
                    onCheckedChange = { checked ->
                        enableM1Horizontal = checked
                        GestureThresholds.ENABLE_M1_HORIZONTAL = checked
                        scope.launch { repository.setEnableM1Horizontal(checked) }
                    },
                )
                GestureToggleRow(
                    label = "Con trỏ ảo (click/giữ)",
                    checked = enableM2,
                    onCheckedChange = { checked ->
                        enableM2 = checked
                        GestureThresholds.ENABLE_M2_CURSOR = checked
                        scope.launch { repository.setEnableM2Cursor(checked) }
                    },
                )
                GestureToggleRow(
                    label = "Cử chỉ hệ thống 4 ngón",
                    checked = enableM5,
                    onCheckedChange = { checked ->
                        enableM5 = checked
                        GestureThresholds.ENABLE_M5_SYSTEM = checked
                        scope.launch { repository.setEnableM5System(checked) }
                    },
                )
                GestureToggleRow(
                    label = "Xoè 5 ngón rồi nắm tay = tắt màn hình",
                    checked = enableM6,
                    onCheckedChange = { checked ->
                        enableM6 = checked
                        GestureThresholds.ENABLE_M6_SCREEN_OFF = checked
                        scope.launch { repository.setEnableM6ScreenOff(checked) }
                    },
                )
            }

            SettingsSection(
                title = "Tắt màn hình bằng cử chỉ (thử nghiệm)",
                description = "Xoè cả 5 ngón, giữ yên 1 giây (icon chuyển màu cam), " +
                    "rồi nắm tay lại để tắt màn hình. Chỉ tắt màn hình, không tắt " +
                    "app/camera/thông báo. Không hoạt động khi đang ở chế độ con trỏ.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cho phép mở lại màn hình bằng cử chỉ")
                        Text(
                            "TẮT (mặc định): khoá màn hình thật như bấm nút nguồn — " +
                                "muốn mở lại phải bấm nguồn/vân tay như bình thường. " +
                                "BẬT: không khoá/tắt gì thật cả, chỉ phủ 1 lớp màn đen che " +
                                "kín màn hình (camera vẫn chạy bình thường suốt lúc đó) — " +
                                "nắm tay giữ ít nhất 0,5 giây rồi xoè 5 ngón ra để gỡ lớp " +
                                "màn đen, xem lại màn hình bình thường.",
                        )
                    }
                    Switch(
                        checked = enableScreenOffReopen,
                        onCheckedChange = { checked ->
                            enableScreenOffReopen = checked
                            GestureThresholds.ENABLE_SCREEN_OFF_REOPEN_GESTURE = checked
                            scope.launch { repository.setEnableScreenOffReopenGesture(checked) }
                        },
                    )
                }
            }

            SettingsSection(
                title = "Độ nhạy vuốt 2 ngón (Lên / Xuống)",
                description = "Kéo sang phải = nhạy hơn (chỉ cần vẫy nhẹ là nhận).",
            ) {
                SensitivitySlider(
                    label = "Vuốt Lên",
                    sensitivity = sensUp,
                    onChange = { value ->
                        sensUp = value
                        val v = sensitivityToVelocity(value, GestureThresholds.MIN_SWIPE_VEL_MIN_UP, GestureThresholds.MAX_SWIPE_VEL_MIN_UP)
                        GestureThresholds.SWIPE_VEL_MIN_UP = v
                        scope.launch { repository.setVelUp(v) }
                    },
                )
                SensitivitySlider(
                    label = "Vuốt Xuống",
                    sensitivity = sensDown,
                    onChange = { value ->
                        sensDown = value
                        val v =
                            sensitivityToVelocity(value, GestureThresholds.MIN_SWIPE_VEL_MIN_DOWN, GestureThresholds.MAX_SWIPE_VEL_MIN_DOWN)
                        GestureThresholds.SWIPE_VEL_MIN_DOWN = v
                        scope.launch { repository.setVelDown(v) }
                    },
                )
            }

            SettingsSection(
                title = "Độ nhạy vuốt 3 ngón (Trái / Phải)",
                description = "Kéo sang phải = nhạy hơn (chỉ cần vẫy nhẹ là nhận).",
            ) {
                SensitivitySlider(
                    label = "Vuốt Trái",
                    sensitivity = sensLeft,
                    onChange = { value ->
                        sensLeft = value
                        val v =
                            sensitivityToVelocity(value, GestureThresholds.MIN_SWIPE_VEL_MIN_LEFT, GestureThresholds.MAX_SWIPE_VEL_MIN_LEFT)
                        GestureThresholds.SWIPE_VEL_MIN_LEFT = v
                        scope.launch { repository.setVelLeft(v) }
                    },
                )
                SensitivitySlider(
                    label = "Vuốt Phải",
                    sensitivity = sensRight,
                    onChange = { value ->
                        sensRight = value
                        val v = sensitivityToVelocity(
                            value,
                            GestureThresholds.MIN_SWIPE_VEL_MIN_RIGHT,
                            GestureThresholds.MAX_SWIPE_VEL_MIN_RIGHT,
                        )
                        GestureThresholds.SWIPE_VEL_MIN_RIGHT = v
                        scope.launch { repository.setVelRight(v) }
                    },
                )
            }

            SettingsSection(
                title = "Độ nhạy bắt tay / ngón tay",
                description = "Kéo sang phải = dễ nhận tay hơn, nhưng cũng dễ nhận nhầm hơn. " +
                    "Đổi cái này sẽ giật hình 1 chút vì phải khởi động lại camera.",
            ) {
                SensitivitySlider(
                    label = "Độ nhạy bắt tay",
                    sensitivity = sensHandDetection,
                    onChange = { value -> sensHandDetection = value }, // chi cap nhat UI, ap dung khi tha tay (ben duoi)
                    onChangeFinished = {
                        val v = sensitivityToVelocity(
                            sensHandDetection,
                            GestureThresholds.MIN_HAND_DETECTION_CONFIDENCE,
                            GestureThresholds.MAX_HAND_DETECTION_CONFIDENCE,
                        )
                        scope.launch { repository.setHandDetectionConfidence(v) }
                    },
                )
            }

            SettingsSection(title = "Giãn cách giữa 2 lần thao tác") {
                Text("Thời gian tối thiểu phải chờ sau 1 lần vuốt mới vuốt tiếp được: ${cooldownMs.toInt()}ms")
                Slider(
                    value = cooldownMs,
                    onValueChange = { cooldownMs = it },
                    valueRange = GestureThresholds.MIN_SWIPE_COOLDOWN_MS.toFloat()..GestureThresholds.MAX_SWIPE_COOLDOWN_MS.toFloat(),
                    onValueChangeFinished = {
                        val v = cooldownMs.toLong()
                        GestureThresholds.SWIPE_COOLDOWN_MS = v
                        scope.launch { repository.setSwipeCooldownMs(v) }
                    },
                )
            }

            SettingsSection(title = "Con trỏ (M2)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (pointingMode) "Chế độ: Theo hướng ngón trỏ" else "Chế độ: Di chuyển theo tay")
                        Text(
                            if (pointingMode) {
                                "Con trỏ chỉ theo hướng ngón trỏ đang chỉ (như tia laser), không cần di chuyển cả bàn tay."
                            } else {
                                "Con trỏ di chuyển theo chuyển động của bàn tay (như chuột)."
                            },
                        )
                    }
                    Switch(
                        checked = pointingMode,
                        onCheckedChange = { checked ->
                            pointingMode = checked
                            GestureThresholds.CURSOR_POINTING_MODE = checked
                            scope.launch { repository.setCursorPointingMode(checked) }
                        },
                    )
                }

                Text(
                    "Kéo sang phải = dễ tách 2 ngón hơn (xoè ít hơn vẫn được tính là " +
                        "tách) - giúp tay không phải xoè quá rộng đến mức để rơi khỏi " +
                        "khung hình camera lúc đang click/giữ.",
                )
                SensitivitySlider(
                    label = "Độ nhạy tách ngón (click/giữ, M2)",
                    sensitivity = sensGOpen,
                    onChange = { value -> sensGOpen = value },
                    onChangeFinished = {
                        val v = sensitivityToVelocity(sensGOpen, GestureThresholds.MIN_G_OPEN, GestureThresholds.MAX_G_OPEN)
                        GestureThresholds.G_OPEN = v
                        scope.launch { repository.setGOpen(v) }
                    },
                )

                Text("Số dp con trỏ chạy trên màn hình cho mỗi đơn vị chuyển động/lệch hướng. Số càng lớn = con trỏ chạy càng nhanh/xa.")
                Text("Mỗi chế độ có độ nhạy riêng vì đơn vị chuyển động khác hẳn nhau.")
                OutlinedTextField(
                    value = cursorGainTranslationText,
                    onValueChange = { text ->
                        cursorGainTranslationText = text
                        val v = text.toFloatOrNull()
                        if (v != null && v > 0f) {
                            GestureThresholds.CURSOR_GAIN_TRANSLATION = v
                            scope.launch { repository.setCursorGainTranslation(v) }
                        }
                    },
                    label = { Text("Hệ số con trỏ (di chuyển theo tay)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = cursorGainPointingText,
                    onValueChange = { text ->
                        cursorGainPointingText = text
                        val v = text.toFloatOrNull()
                        if (v != null && v > 0f) {
                            GestureThresholds.CURSOR_GAIN_POINTING = v
                            scope.launch { repository.setCursorGainPointing(v) }
                        }
                    },
                    label = { Text("Hệ số con trỏ (theo hướng ngón trỏ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }

            SettingsSection(
                title = "Cử chỉ hệ thống (M5, 4 ngón)",
                description = "Kéo sang phải = nhạy hơn (chỉ cần vẫy nhẹ là nhận Back/Đa nhiệm/Home/Thanh thông báo).",
            ) {
                SensitivitySlider(
                    label = "Độ nhạy vẫy 4 ngón",
                    sensitivity = sensSysVelMin,
                    onChange = { value ->
                        sensSysVelMin = value
                        val v = sensitivityToVelocity(value, GestureThresholds.MIN_SYS_VEL_MIN, GestureThresholds.MAX_SYS_VEL_MIN)
                        GestureThresholds.SYS_VEL_MIN = v
                        scope.launch { repository.setSysVelMin(v) }
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, description: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun GestureToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SensitivitySlider(
    label: String,
    sensitivity: Float,
    onChange: (Float) -> Unit,
    onChangeFinished: (() -> Unit)? = null,
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Slider(
            value = sensitivity,
            onValueChange = onChange,
            valueRange = 0f..1f,
            onValueChangeFinished = onChangeFinished,
        )
    }
}
