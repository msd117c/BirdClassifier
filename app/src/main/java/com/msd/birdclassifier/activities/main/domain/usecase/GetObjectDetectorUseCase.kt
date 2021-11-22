package com.msd.birdclassifier.activities.main.domain.usecase

import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import javax.inject.Inject

class GetObjectDetectorUseCase @Inject constructor() {

    operator fun invoke(modelName: String): ObjectDetector {
        val localModel = LocalModel.Builder()
            .setAssetFilePath(modelName)
            //.setAssetFilePath("lite-model_aiy_vision_classifier_birds_V1_3.tflite")
            //.setAssetFilePath("lite-model_aiy_vision_classifier_plants_V1_3.tflite")
            .build()

        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()

        return ObjectDetection.getClient(customObjectDetectorOptions)
    }
}