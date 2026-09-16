package com.allanglickman.meerkat.detection

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetector
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ObjectDetectorRepository @Inject constructor(
    private val detector: ObjectDetector,
) {
    suspend fun detect(image: InputImage): List<DetectedObject> {
        return detector.process(image).await().map { it.toDetectedObject() }
    }

    private fun com.google.mlkit.vision.objects.DetectedObject.toDetectedObject(): DetectedObject {
        val topLabel = labels.maxByOrNull { it.confidence }
        return DetectedObject(
            trackingId = trackingId,
            label = topLabel?.text ?: "Object",
            confidence = topLabel?.confidence ?: 0f,
            boundingBox = boundingBox,
        )
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    addOnCanceledListener { continuation.cancel() }
}
