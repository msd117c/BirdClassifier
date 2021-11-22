package com.msd.birdclassifier.activities.main.presentation.ui

import android.graphics.Rect
import androidx.annotation.StringRes
import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.msd.birdclassifier.R

sealed class MainViewState

object RequestingPermission : MainViewState()
data class PermissionDeclined(
    val onRetryClicked: () -> Unit,
    val onExitClicked: () -> Unit
) : MainViewState()

data class AnalyzeCameraInput(
    val imageAnalysisWithResult: ImageAnalysisWithResult,
    val currentDetectionOption: DetectionOptions
) : MainViewState() {

    data class ImageAnalysisWithResult(
        val imageAnalysis: ImageAnalysis,
        val objectDetectionBoxState: MutableState<ObjectDetectionBox?> = mutableStateOf(null)
    )

    data class ObjectDetectionBox(
        val box: Rect,
        val label: String?,
        val imageWidth: Float,
        val imageHeight: Float
    )

    enum class DetectionOptions(@StringRes val label: Int) {
        BIRDS(R.string.birds_detection),
        PLANTS(R.string.plants_detection)
    }
}

object ExitApp : MainViewState()
