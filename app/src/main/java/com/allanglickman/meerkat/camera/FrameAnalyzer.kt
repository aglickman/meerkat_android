package com.allanglickman.meerkat.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.allanglickman.meerkat.detection.DetectedObject
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// only one frame processed at a time, new ones get dropped until it's free
class FrameAnalyzer(
    private val scope: CoroutineScope,
    private val detect: suspend (InputImage) -> List<DetectedObject>,
    private val onResult: (detections: List<DetectedObject>, imageWidth: Int, imageHeight: Int) -> Unit,
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val imageWidth = if (isRotated) imageProxy.height else imageProxy.width
        val imageHeight = if (isRotated) imageProxy.width else imageProxy.height
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        scope.launch {
            try {
                val detections = detect(inputImage)
                onResult(detections, imageWidth, imageHeight)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Frame detection failed", e)
            } finally {
                isProcessing.set(false)
                imageProxy.close()
            }
        }
    }

    private companion object {
        const val TAG = "FrameAnalyzer"
    }
}
