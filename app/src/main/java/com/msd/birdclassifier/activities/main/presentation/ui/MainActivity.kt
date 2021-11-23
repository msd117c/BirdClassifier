package com.msd.birdclassifier.activities.main.presentation.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.msd.birdclassifier.activities.main.presentation.presenter.MainViewModel
import com.msd.birdclassifier.activities.main.presentation.ui.view.MainActivityView
import com.msd.birdclassifier.ui.theme.BirdClassifierTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val activityResultLauncher by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            viewModel::onPermissionResult
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.getState().collect { state ->
                setContentView(state)
                if (state == RequestingPermission) {
                    activityResultLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.initialize()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun setContentView(state: MainViewState) {
        setContent {
            BirdClassifierTheme {
                MainActivityView(
                    state = state,
                    onDetectionModeListener = viewModel::onDetectionModeChanged
                )
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.hide(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            with(window.decorView) {
                systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
        }
    }
}
