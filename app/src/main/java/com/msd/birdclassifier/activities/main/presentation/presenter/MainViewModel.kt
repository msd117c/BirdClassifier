package com.msd.birdclassifier.activities.main.presentation.presenter

import androidx.lifecycle.ViewModel
import com.msd.birdclassifier.activities.main.domain.usecase.GetImageAnalysisWithResultUseCase
import com.msd.birdclassifier.activities.main.presentation.ui.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

private const val BIRDS_DETECTOR_FILE = "lite-model_aiy_vision_classifier_birds_V1_3.tflite"
private const val PLANTS_DETECTOR_FILE = "lite-model_aiy_vision_classifier_plants_V1_3.tflite"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getImageAnalysisWithResultUseCase: GetImageAnalysisWithResultUseCase
) : ViewModel() {

    private val state = MutableStateFlow<MainViewState?>(null)
    fun getState(): Flow<MainViewState> = state.filterNotNull()

    fun initialize() {
        state.value = RequestingPermission
    }

    fun onPermissionResult(isGranted: Boolean) {
        state.value = if (isGranted) {
            AnalyzeCameraInput(
                imageAnalysisWithResult = getImageAnalysisWithResultUseCase(BIRDS_DETECTOR_FILE),
                currentDetectionOption = AnalyzeCameraInput.DetectionOptions.BIRDS
            )
        } else {
            PermissionDeclined(::onRetryClicked, ::onExitClicked)
        }
    }

    fun onDetectionModeChanged(detectionMode: AnalyzeCameraInput.DetectionOptions) {
        if (state.value is AnalyzeCameraInput) {
            val detectorFile = when (detectionMode) {
                AnalyzeCameraInput.DetectionOptions.BIRDS -> BIRDS_DETECTOR_FILE
                AnalyzeCameraInput.DetectionOptions.PLANTS -> PLANTS_DETECTOR_FILE
            }

            state.value = (state.value as? AnalyzeCameraInput)?.copy(
                imageAnalysisWithResult = getImageAnalysisWithResultUseCase(detectorFile),
                currentDetectionOption = detectionMode
            )
        }
    }

    private fun onRetryClicked() {
        state.value = RequestingPermission
    }

    private fun onExitClicked() {
        state.value = ExitApp
    }
}
