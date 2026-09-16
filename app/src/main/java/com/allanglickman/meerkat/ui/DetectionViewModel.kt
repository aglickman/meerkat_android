package com.allanglickman.meerkat.ui

import androidx.lifecycle.ViewModel
import com.allanglickman.meerkat.detection.DetectedObject
import com.allanglickman.meerkat.detection.ObjectDetectorRepository
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val repository: ObjectDetectorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetectionUiState>(DetectionUiState.PermissionRequired)
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    suspend fun detect(image: InputImage): List<DetectedObject> = repository.detect(image)

    fun onPermissionGranted() {
        if (_uiState.value == DetectionUiState.PermissionRequired) {
            _uiState.value = DetectionUiState.Ready()
        }
    }

    fun onPermissionDenied() {
        _uiState.value = DetectionUiState.PermissionRequired
    }

    fun onDetections(detections: List<DetectedObject>, imageWidth: Int, imageHeight: Int) {
        _uiState.value = DetectionUiState.Ready(detections, imageWidth, imageHeight)
    }
}
