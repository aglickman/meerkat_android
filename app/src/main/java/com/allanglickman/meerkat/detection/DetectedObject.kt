package com.allanglickman.meerkat.detection

import android.graphics.Rect

data class DetectedObject(
    val trackingId: Int?,
    val label: String,
    val confidence: Float,
    val boundingBox: Rect,
)
