package com.msd.birdclassifier.activities.main.presentation.presenter

import androidx.lifecycle.ViewModel
import com.msd.birdclassifier.activities.main.domain.usecase.GetImageAnalysisWithResultUseCase
import com.msd.birdclassifier.activities.main.presentation.ui.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

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
            val defaultDetectionOption = AnalyzeCameraInput.DetectionOptions.BIRDS

            AnalyzeCameraInput(
                imageAnalysisWithResult = getImageAnalysisWithResultUseCase(defaultDetectionOption.fileName),
                currentDetectionOption = defaultDetectionOption
            )
        } else {
            PermissionDeclined(::onRetryClicked, ::onExitClicked)
        }
    }

    fun onDetectionModeChanged(detectionMode: AnalyzeCameraInput.DetectionOptions) {
        if (state.value is AnalyzeCameraInput) {
            state.value = (state.value as? AnalyzeCameraInput)?.copy(
                imageAnalysisWithResult = getImageAnalysisWithResultUseCase(detectionMode.fileName),
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
