package com.allanglickman.meerkat.ui

import com.allanglickman.meerkat.detection.DetectedObject

sealed interface DetectionUiState {

    data object PermissionRequired : DetectionUiState

    data class Ready(
        val detections: List<DetectedObject> = emptyList(),
        val imageWidth: Int = 3,
        val imageHeight: Int = 4,
    ) : DetectionUiState
}
