package com.msd.birdclassifier.activities.main.presentation.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.msd.birdclassifier.activities.main.presentation.presenter.MainViewModel
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

    private fun setContentView(state: MainViewState) {
        setContent {
            BirdClassifierTheme {
                MainActivityView(state = state)
            }
        }
    }
}