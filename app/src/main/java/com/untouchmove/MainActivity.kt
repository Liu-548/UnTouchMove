package com.untouchmove

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

/**
 * Man hinh duy nhat cua app (SPEC muc 7). Phase 0 chi can hien preview
 * camera truoc de xac nhan CameraX da noi day duoc.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CameraPermissionGate()
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionGate() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            FrontCameraPreview()
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { context.startActivity(Intent(context, DebugActivity::class.java)) }) {
                    Text("Man hinh Debug (Phase 1)")
                }
                Button(onClick = { context.startActivity(Intent(context, GestureTestActivity::class.java)) }) {
                    Text("Bom thu cu chi (Phase 2)")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = stringResourceCameraRationale())
        }
    }
}

@Composable
private fun stringResourceCameraRationale(): String =
    androidx.compose.ui.res.stringResource(id = R.string.camera_permission_rationale)

@Composable
private fun FrontCameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}
