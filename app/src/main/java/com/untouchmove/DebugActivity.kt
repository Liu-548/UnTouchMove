package com.untouchmove

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.untouchmove.camera.HandLandmarkerHelper
import com.untouchmove.gesture.FeatureExtractor
import com.untouchmove.gesture.Features
import com.untouchmove.gesture.Point3D
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

private const val RECORD_DURATION_MS = 5_000L
private const val CSV_HEADER = "timestamp_ms,r_b,r_c,r_d,r_e,g_bc,g_cd,g_de,t,confidence"

/**
 * Man hinh do dac cua ROADMAP Phase 1: preview + skeleton + bang so lieu song
 * (r_b, r_c, r_d, r_e, g_bc, g_cd, g_de, t, fps, do tin cay) + nut ghi CSV.
 * Khong dung may trang thai that, chi phuc vu do dac de chon nguong o SPEC muc 6.
 */
class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DebugScreen()
                }
            }
        }
    }
}

@Composable
private fun DebugScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var features by remember { mutableStateOf<Features?>(null) }
    var confidence by remember { mutableStateOf(0f) }
    var fps by remember { mutableStateOf(0f) }
    var skeletonPoints by remember { mutableStateOf<List<Point3D>>(emptyList()) }
    var lastFrameAtMs by remember { mutableStateOf(0L) }

    val csvRows = remember { mutableListOf<String>() }
    var isRecording by remember { mutableStateOf(false) }
    var recordStopAtMs by remember { mutableStateOf(0L) }
    var lastSavedFile by remember { mutableStateOf<File?>(null) }

    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(
            context = context,
            onResult = { frame, normalized, _ ->
                mainHandler.post {
                    val extracted = runCatching { FeatureExtractor.extract(frame.worldLandmarks) }.getOrNull()
                    features = extracted
                    confidence = frame.confidence
                    skeletonPoints = normalized

                    if (lastFrameAtMs != 0L) {
                        val deltaMs = (frame.timestampMs - lastFrameAtMs).coerceAtLeast(1)
                        fps = fps * 0.8f + (1000f / deltaMs) * 0.2f
                    }
                    lastFrameAtMs = frame.timestampMs

                    if (isRecording && extracted != null) {
                        csvRows.add(csvRow(frame.timestampMs, extracted, frame.confidence))
                        if (System.currentTimeMillis() >= recordStopAtMs) {
                            lastSavedFile = writeCsv(context, csvRows)
                            csvRows.clear()
                            isRecording = false
                        }
                    }
                }
            },
            onError = { /* TODO(untouch): hien loi ra UI neu can, tam bo qua cho don gian o Phase 1 */ },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            handLandmarkerHelper.close()
            cameraExecutor.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    handLandmarkerHelper.detectAsync(imageProxy)
                                }
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageAnalysis,
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSkeleton(skeletonPoints)
            }
        }

        StatsPanel(
            features = features,
            confidence = confidence,
            fps = fps,
            isRecording = isRecording,
            lastSavedFile = lastSavedFile,
            onRecordClick = {
                csvRows.clear()
                csvRows.add(CSV_HEADER)
                recordStopAtMs = System.currentTimeMillis() + RECORD_DURATION_MS
                isRecording = true
            },
            onShareClick = { file -> shareCsv(context, file) },
        )
    }
}

@Composable
private fun StatsPanel(
    features: Features?,
    confidence: Float,
    fps: Float,
    isRecording: Boolean,
    lastSavedFile: File?,
    onRecordClick: () -> Unit,
    onShareClick: (File) -> Unit,
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text("fps: ${"%.1f".format(fps)}   do tin cay: ${"%.2f".format(confidence)}")
        if (features != null) {
            Text(
                "r_b=${fmt(features.rB)}  r_c=${fmt(features.rC)}  " +
                    "r_d=${fmt(features.rD)}  r_e=${fmt(features.rE)}"
            )
            Text(
                "g_bc=${fmt(features.gBC)}  g_cd=${fmt(features.gCD)}  " +
                    "g_de=${fmt(features.gDE)}  t=${fmt(features.t)}"
            )
        } else {
            Text("Khong thay tay")
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Button(onClick = onRecordClick, enabled = !isRecording) {
                Text(if (isRecording) "Dang ghi..." else "Ghi 5 giay")
            }
            if (lastSavedFile != null) {
                Button(
                    onClick = { onShareClick(lastSavedFile) },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text("Chia se file moi nhat")
                }
            }
        }
    }
}

private fun fmt(value: Float) = "%.2f".format(value)

private fun csvRow(timestampMs: Long, f: Features, confidence: Float): String =
    "$timestampMs,${f.rB},${f.rC},${f.rD},${f.rE},${f.gBC},${f.gCD},${f.gDE},${f.t},$confidence"

private fun writeCsv(context: Context, rows: List<String>): File {
    val dir = File(context.filesDir, "logs").apply { mkdirs() }
    val name = "log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())}.csv"
    val file = File(dir, name)
    file.writeText(rows.joinToString("\n"))
    return file
}

private fun shareCsv(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Chia se file CSV"))
}

// Cac cap landmark noi lien nhau de ve skeleton (theo quy uoc MediaPipe Hand)
private val HAND_CONNECTIONS = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4, // ngon cai
    0 to 5, 5 to 6, 6 to 7, 7 to 8, // ngon tro
    0 to 9, 9 to 10, 10 to 11, 11 to 12, // ngon giua
    0 to 13, 13 to 14, 14 to 15, 15 to 16, // ngon ap ut
    0 to 17, 17 to 18, 18 to 19, 19 to 20, // ngon ut
    5 to 9, 9 to 13, 13 to 17, // long ban tay
)

// ponytail: anh xa truc tiep x*width, y*height, khong bu scale-type cua
// PreviewView nen skeleton co the lech vai px so voi preview that. Du de xac
// nhan tay co dang duoc track, so lieu chinh xac nam o bang ben duoi.
private fun DrawScope.drawSkeleton(points: List<Point3D>) {
    if (points.size < 21) return
    fun offset(index: Int) = Offset(points[index].x * size.width, points[index].y * size.height)
    HAND_CONNECTIONS.forEach { (a, b) ->
        drawLine(color = Color(0xFF00E676), start = offset(a), end = offset(b), strokeWidth = 4f)
    }
    points.indices.forEach { i ->
        drawCircle(color = Color(0xFFFFEB3B), radius = 6f, center = offset(i))
    }
}
