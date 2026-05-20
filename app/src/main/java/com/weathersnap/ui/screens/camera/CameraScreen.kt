package com.weathersnap.ui.screens.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                onImageCaptured = onImageCaptured,
                onClose = onClose,
                context = context
            )
        } else {
            // Show permission denied message
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required to capture photos.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onImageCaptured: (String) -> Unit,
    onClose: () -> Unit,
    context: Context
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    imageCapture = capture

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // UI overlay on top of camera preview
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Camera",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Text("Close", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Capture button at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val outputFile = File(context.filesDir, "raw_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                        imageCapture?.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onImageCaptured(outputFile.absolutePath)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Capture", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
