package com.untouchmove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.untouchmove.ui.UnTouchTheme

/**
 * Màn hình Hướng dẫn sử dụng (ROADMAP Phase 7 "màn hình hướng dẫn cử chỉ").
 * Tóm tắt lại toàn bộ cử chỉ trong SPEC.md bằng ngôn ngữ người dùng cuối
 * (CLAUDE.md mục 2), kèm hình minh hoạ đơn giản (HandPoseIcon) và bảng màu
 * icon trạng thái đã dùng trong OverlayRenderer/CursorView.
 */
class GuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnTouchTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GuideScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun GuideScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("‹ Quay lại")
            }
            Text("Hướng dẫn sử dụng", style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Đưa tay vào trước camera trước, giữ đúng một trong các tư thế " +
                    "dưới đây khoảng nửa giây để kích hoạt. Xoè cả 5 ngón bất kỳ " +
                    "lúc nào để về trạng thái nghỉ.",
            )

            GestureCard(
                iconColor = Color(0xFF4CAF50), // xanh lá, khớp OverlayRenderer M1_VERTICAL
                title = "Vuốt 2 ngón - Lên / Xuống",
                poses = listOf(HandPose(thumbOut = false, fingersUp = listOf(true, true, false, false))),
                description = "Đưa ngón trỏ và ngón giữa thẳng lên, 2 ngón còn lại gập " +
                    "vào lòng bàn tay. Vẫy tay lên hoặc xuống để cuộn trang.",
            )

            GestureCard(
                iconColor = Color(0xFFFFEB3B), // vàng, khớp M1_HORIZONTAL
                title = "Vuốt 3 ngón - Trái / Phải",
                poses = listOf(HandPose(thumbOut = false, fingersUp = listOf(true, true, true, false))),
                description = "Đưa thêm ngón áp út thẳng lên (trỏ, giữa, áp út đều " +
                    "thẳng), chỉ ngón út gập lại. Vẫy tay sang trái hoặc phải để " +
                    "chuyển bài / quay lại.",
            )

            GestureCard(
                iconColor = Color(0xFF00E5FF), // cyan, khớp M2
                title = "Con trỏ - di chuyển, bấm, giữ kéo",
                poses = listOf(HandPose(thumbOut = true, fingersUp = listOf(true, true, false, false))),
                description = "Đưa ngón trỏ, ngón giữa thẳng lên VÀ xoè ngón cái ra " +
                    "(khác tư thế vuốt ở trên). Di chuyển cả bàn tay hoặc chỉ hướng " +
                    "ngón trỏ (chọn ở Cài đặt) để lái con trỏ.\n\n" +
                    "• Tách nhanh đầu ngón trỏ và ngón giữa rồi khép lại ngay = bấm (click).\n" +
                    "• Tách và GIỮ lâu hơn (con trỏ đổi màu đỏ) rồi di chuyển tay = " +
                    "kéo; khép 2 ngón lại để thả ra.\n" +
                    "• Xoè cả 5 ngón để tắt con trỏ.",
            )

            GestureCard(
                iconColor = Color(0xFF2962FF), // xanh dương, khớp M5_CLOSED
                title = "4 ngón KHÉP - Back / Đa nhiệm",
                poses = listOf(HandPose(thumbOut = false, fingersUp = listOf(true, true, true, true), spread = false)),
                description = "Đưa cả 4 ngón (trỏ, giữa, áp út, út) thẳng lên VÀ khép " +
                    "sát nhau, ngón cái gập vào. Vẫy sang trái = Back (quay lại), " +
                    "vẫy sang phải = Đa nhiệm. Xoè 4 ngón ra để thoát.",
            )

            GestureCard(
                iconColor = Color(0xFFE040FB), // tím/magenta, khớp M5_SPREAD
                title = "4 ngón TÁCH - Home / Thanh thông báo",
                poses = listOf(HandPose(thumbOut = false, fingersUp = listOf(true, true, true, true), spread = true)),
                description = "Giống tư thế trên nhưng xoè 4 ngón hơi rời nhau. Vẫy " +
                    "sang trái = Home (về màn hình chính), vẫy sang phải = mở " +
                    "thanh thông báo. Khép 4 ngón lại để thoát.",
            )

            ColorLegendCard()
            TroubleshootingCard()
        }
    }
}

@Composable
private fun ColorLegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Màu icon ở góc màn hình nghĩa là gì", style = MaterialTheme.typography.titleMedium)
            LegendRow(Color(0xFF9E9E9E), "Xám - đang giữ yên để kích hoạt")
            LegendRow(Color(0xFF4CAF50), "Xanh lá - đang ở chế độ vuốt 2 ngón")
            LegendRow(Color(0xFFFFEB3B), "Vàng - đang ở chế độ vuốt 3 ngón")
            LegendRow(Color(0xFF00E5FF), "Cyan - đang ở chế độ con trỏ")
            LegendRow(Color(0xFF2962FF), "Xanh dương - đang ở chế độ 4 ngón KHÉP")
            LegendRow(Color(0xFFE040FB), "Tím - đang ở chế độ 4 ngón TÁCH")
            Text(
                "Riêng CHẤM TRÒN con trỏ (khác icon góc màn hình) đổi màu: xanh " +
                    "cyan = đang di chuyển, vàng = vừa tách chờ xem có phải bấm " +
                    "không, đỏ = đang giữ/kéo thật, xám = vừa bị buông tay tự động " +
                    "do giữ quá lâu.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LegendRow(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(16.dp)) { drawCircle(color = color) }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TroubleshootingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nếu không dùng được", style = MaterialTheme.typography.titleMedium)
            Text(
                "• Mỗi lần cài lại/cập nhật app, Android thường TỰ TẮT quyền Trợ " +
                    "năng - vào Cài đặt máy > Trợ năng > bật lại UnTouchMove.\n" +
                    "• Android 13 trở lên chặn bật Trợ năng cho app cài ngoài Play " +
                    "Store - mở menu 3 chấm trong Cài đặt > Ứng dụng > UnTouchMove, " +
                    "chọn \"Cho phép cài đặt hạn chế\" trước.\n" +
                    "• Máy hay tự tắt ứng dụng chạy nền (Realme UI, Oppo...) - vào " +
                    "Cài đặt pin, tắt tối ưu hoá pin cho UnTouchMove.\n" +
                    "• Khó tách 2 ngón để bấm/giữ mà không bị mất tay khỏi khung " +
                    "hình - vào Cài đặt độ nhạy, tăng \"Độ nhạy tách ngón\".\n" +
                    "• Vẫy 4 ngón khó kích hoạt - vào Cài đặt độ nhạy, tăng \"Độ " +
                    "nhạy vẫy 4 ngón\".",
            )
        }
    }
}

private data class HandPose(val thumbOut: Boolean, val fingersUp: List<Boolean>, val spread: Boolean = false)

@Composable
private fun GestureCard(iconColor: Color, title: String, poses: List<HandPose>, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconColor.copy(alpha = 0.10f)),
            ) {
                poses.forEach { pose ->
                    HandPoseIcon(
                        thumbOut = pose.thumbOut,
                        fingersUp = pose.fingersUp,
                        spread = pose.spread,
                        tint = iconColor,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Mau trung tinh cho ngon/ngon cai GAP lai (khong dung, khong phu thuoc mau
// rieng cua tung the) - giup phan biet ro rang voi ngon DUNG (to mau iconColor).
private val FingerDownColor = Color(0xFF9E9E9E)

/**
 * Hinh minh hoa mot ban tay nhin thang: long ban tay hinh chu nhat bo goc,
 * 4 "vien thuoc" (rounded rect) cho 4 ngon chinh (tro/giua/ap ut/ut) toa ra
 * tu mep tren long ban tay, va 1 vien cho ngon cai o canh ben. Ngon DUNG ve
 * dai, to mau; ngon GAP ve ngan, mau xam trung tinh - de phan biet hon nhieu
 * so voi ban dau tien (duong ke mong, kho nhin ra ban tay that).
 *
 * "spread": xoe rong goc giua cac ngon chinh (dung phan biet tu the M5 KHEP
 * vs TACH); rotate() quay tung ngon quanh diem goc cua no (mep tren long ban
 * tay) de tao hieu ung xoe/khep tu nhien.
 */
@Composable
private fun HandPoseIcon(thumbOut: Boolean, fingersUp: List<Boolean>, spread: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(96.dp)) {
        val w = size.width
        val h = size.height

        val palmWidth = w * 0.5f
        val palmHeight = h * 0.4f
        val palmTop = h * 0.56f
        val palmLeft = (w - palmWidth) / 2f
        drawRoundRect(
            color = tint.copy(alpha = 0.22f),
            topLeft = Offset(palmLeft, palmTop),
            size = Size(palmWidth, palmHeight),
            cornerRadius = CornerRadius(palmWidth * 0.28f),
        )

        val fingerWidth = w * 0.11f
        val fingerUpLength = h * 0.46f
        val fingerDownLength = h * 0.10f
        val count = fingersUp.size
        val span = if (spread) palmWidth * 1.15f else palmWidth * 0.66f
        val spanStart = (w - span) / 2f
        val maxSpreadDeg = if (spread) 26f else 6f

        fingersUp.forEachIndexed { i, up ->
            val t = if (count == 1) 0.5f else i / (count - 1).toFloat()
            val baseX = spanStart + span * t
            val angleDeg = (t - 0.5f) * 2f * maxSpreadDeg
            val length = if (up) fingerUpLength else fingerDownLength
            val color = if (up) tint else FingerDownColor
            rotate(degrees = angleDeg, pivot = Offset(baseX, palmTop)) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(baseX - fingerWidth / 2f, palmTop - length),
                    size = Size(fingerWidth, length),
                    cornerRadius = CornerRadius(fingerWidth / 2f),
                )
            }
        }

        // Ngon cai: goc o canh trai long ban tay, xoe (thumbOut) thi dai va
        // choia ra xa hon, gap (khong xoe) thi ngan va sat vao long ban tay.
        val thumbWidth = w * 0.13f
        val thumbBase = Offset(palmLeft + palmWidth * 0.08f, palmTop + palmHeight * 0.32f)
        val thumbLength = if (thumbOut) h * 0.34f else h * 0.16f
        val thumbAngle = if (thumbOut) -50f else -12f
        val thumbColor = if (thumbOut) tint else FingerDownColor
        rotate(degrees = thumbAngle, pivot = thumbBase) {
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(thumbBase.x - thumbWidth / 2f, thumbBase.y - thumbLength),
                size = Size(thumbWidth, thumbLength),
                cornerRadius = CornerRadius(thumbWidth / 2f),
            )
        }
    }
}
