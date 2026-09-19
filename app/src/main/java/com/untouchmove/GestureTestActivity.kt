package com.untouchmove

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.untouchmove.service.ActionDispatcher
import com.untouchmove.service.UnTouchAccessibilityService
import kotlinx.coroutines.delay

/**
 * Man hinh bom thu cu chi cua ROADMAP Phase 2: chua dung camera, chi bam nut
 * de kiem tra dispatchGesture/performGlobalAction hoat dong dung tren may that.
 */
class GestureTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GestureTestScreen()
                }
            }
        }
    }
}

@Composable
private fun GestureTestScreen() {
    val context = LocalContext.current
    var connected by remember { mutableStateOf(ActionDispatcher.isConnected) }

    LaunchedEffect(Unit) {
        while (true) {
            connected = ActionDispatcher.isConnected
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (connected) "Accessibility: Da bat" else "Accessibility: Chua bat")
        Text(
            "Nut vuot/click/keo se doi 2.5s roi moi ban - nhan xong ban chuyen " +
                "ngay sang man hinh chu hoac app khac de thay tac dung. Back/Home/" +
                "Da nhiem/Thong bao ban ngay luc bam."
        )
        if (!connected) {
            Text(
                "Neu cong tac bi mo trong xam khong bam duoc: vao Cai dat > Ung " +
                    "dung > UnTouchMove > mo menu 3 cham > \"Allow restricted " +
                    "settings\" truoc (Android 13+ chan app cai ngoai Play Store)."
            )
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }) {
                Text("Mo Cai dat Accessibility")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(testButtons(context)) { (label, action) ->
                Button(onClick = action, enabled = connected) {
                    Text(label)
                }
            }
        }
    }
}

// Vuot/click/keo luon tac dong len man hinh DANG hien tai luc bam - ma chinh
// man hinh test nay khong co gi de cuon/quan sat. Tre 2.5s + toast de nguoi
// dung kip chuyen sang man hinh chu / app khac roi moi ban cu chi that ra do.
private const val PRE_FIRE_DELAY_MS = 2500L

private fun delayedAction(context: android.content.Context, action: () -> Unit): () -> Unit = {
    Toast.makeText(
        context,
        "Chuyen sang man hinh muon thu trong ${PRE_FIRE_DELAY_MS / 1000}s...",
        Toast.LENGTH_SHORT,
    ).show()
    Handler(Looper.getMainLooper()).postDelayed(action, PRE_FIRE_DELAY_MS)
}

private fun testButtons(context: android.content.Context): List<Pair<String, () -> Unit>> {
    val metrics = context.resources.displayMetrics
    fun delayed(action: () -> Unit) = delayedAction(context, action)
    return listOf(
        "Vuot len (tre 2.5s)" to delayed { ActionDispatcher.swipe(UnTouchAccessibilityService.SwipeDirection.UP) },
        "Vuot xuong (tre 2.5s)" to delayed { ActionDispatcher.swipe(UnTouchAccessibilityService.SwipeDirection.DOWN) },
        "Vuot trai (tre 2.5s)" to delayed { ActionDispatcher.swipe(UnTouchAccessibilityService.SwipeDirection.LEFT) },
        "Vuot phai (tre 2.5s)" to delayed { ActionDispatcher.swipe(UnTouchAccessibilityService.SwipeDirection.RIGHT) },
        "Click giua man hinh (tre 2.5s)" to delayed {
            ActionDispatcher.click(metrics.widthPixels / 2f, metrics.heightPixels / 2f)
        },
        "Keo thu trai->phai (tre 2.5s)" to delayed { ActionDispatcher.testDrag() },
        "Back" to { ActionDispatcher.globalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK) },
        "Home" to { ActionDispatcher.globalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME) },
        "Da nhiem" to { ActionDispatcher.globalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS) },
        "Thanh thong bao" to { ActionDispatcher.globalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) },
    )
}
