package com.allanglickman.meerkat.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allanglickman.meerkat.R
import com.allanglickman.meerkat.camera.FrameAnalyzer
import com.allanglickman.meerkat.detection.DetectedObject
import com.allanglickman.meerkat.ui.components.BoundingBoxOverlay
import com.google.mlkit.vision.common.InputImage

@Composable
fun DetectionRoute(viewModel: DetectionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetectionScreen(
        uiState = uiState,
        onPermissionGranted = viewModel::onPermissionGranted,
        onPermissionDenied = viewModel::onPermissionDenied,
        onFrameDetect = viewModel::detect,
        onFrameResult = viewModel::onDetections,
    )
}

@Composable
fun DetectionScreen(
    uiState: DetectionUiState,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onFrameDetect: suspend (InputImage) -> List<DetectedObject>,
    onFrameResult: (List<DetectedObject>, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) onPermissionGranted() else onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        if (hasCameraPermission) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (hasCameraPermission) {
            val imageWidth = (uiState as? DetectionUiState.Ready)?.imageWidth ?: 3
            val imageHeight = (uiState as? DetectionUiState.Ready)?.imageHeight ?: 4
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageWidth.toFloat() / imageHeight.toFloat())
            ) {
                CameraPreview(
                    onFrameDetect = onFrameDetect,
                    onFrameResult = onFrameResult,
                    modifier = Modifier.fillMaxSize(),
                )
                if (uiState is DetectionUiState.Ready) {
                    BoundingBoxOverlay(
                        detections = uiState.detections,
                        imageWidth = uiState.imageWidth,
                        imageHeight = uiState.imageHeight,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            PermissionRationale(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CameraPreview(
    onFrameDetect: suspend (InputImage) -> List<DetectedObject>,
    onFrameResult: (List<DetectedObject>, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }
                    val analyzer = FrameAnalyzer(
                        scope = scope,
                        detect = onFrameDetect,
                        onResult = onFrameResult,
                    )
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(ContextCompat.getMainExecutor(ctx), analyzer) }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                },
                ContextCompat.getMainExecutor(ctx),
            )
            previewView
        },
    )
}

@Composable
private fun PermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.camera_permission_rationale))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(text = stringResource(R.string.grant_camera_permission))
        }
    }
}
