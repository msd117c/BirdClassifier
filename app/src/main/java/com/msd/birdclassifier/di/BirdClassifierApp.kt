package com.msd.birdclassifier.di

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BirdClassifierApp : Application(), CameraXConfig.Provider {

    override fun getCameraXConfig(): CameraXConfig = Camera2Config.defaultConfig()
}