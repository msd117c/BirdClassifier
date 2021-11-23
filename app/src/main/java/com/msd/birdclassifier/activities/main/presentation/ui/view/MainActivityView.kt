package com.msd.birdclassifier.activities.main.presentation.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.msd.birdclassifier.activities.main.presentation.ui.*

@Composable
fun MainActivityView(
    state: MainViewState,
    onDetectionModeListener: (AnalyzeCameraInput.DetectionOptions) -> Unit
) {
    when (state) {
        is PermissionDeclined -> PermissionDeclinedView(state)
        is AnalyzeCameraInput -> CameraPreview(state, onDetectionModeListener)
        is RequestingPermission -> RequestingPermissionView()
        is ExitApp -> (LocalContext.current as MainActivity).finishAffinity()
    }
}
