package com.untouchmove.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Mot bo mau + bo goc bo tron duy nhat cho toan app (thay vi moi man hinh tu
// goi MaterialTheme rieng) - de giao dien nhat quan giua Man hinh chinh/Cai
// dat/Huong dan. Yeu cau nguoi dung 2026-09-20 "van toi gian nhung co chieu
// sau va dep hon" - dung ĐẦY ĐỦ bang mau tonal cua Material3 (container/
// outline/surfaceVariant rieng biet ro rang, khong chi primary/secondary
// nhu ban dau) de Card/Button co do tuong phan va do "noi" (elevation) ro
// hon, khong can them thu vien ngoai nao.
private val Teal = Color(0xFF00695C)
private val TealDark = Color(0xFF004D40)
private val TealLight = Color(0xFF4DB6AC)
private val TealContainerLight = Color(0xFFB2DFDB)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = TealDark,
    secondary = TealLight,
    onSecondary = Color.White,
    background = Color(0xFFF0F3F2),
    onBackground = Color(0xFF191C1C),
    surface = Color.White,
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFE3ECEA),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFFBEC9C6),
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF00382E),
    primaryContainer = TealDark,
    onPrimaryContainer = TealContainerLight,
    secondary = Teal,
    onSecondary = Color.White,
    background = Color(0xFF0E1413),
    onBackground = Color(0xFFDDE4E2),
    surface = Color(0xFF191F1E),
    onSurface = Color(0xFFDDE4E2),
    surfaceVariant = Color(0xFF232B29),
    onSurfaceVariant = Color(0xFFC0CBC8),
    outline = Color(0xFF556360),
)

// Bo goc bo tron LON hon mac dinh Material3 (12dp) - 20dp cho Card/o nhap,
// 28dp cho nut bam - tao cam giac "mem", co chieu sau hon la vuong vuc phang.
private val UnTouchShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun UnTouchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = UnTouchShapes,
        content = content,
    )
}
