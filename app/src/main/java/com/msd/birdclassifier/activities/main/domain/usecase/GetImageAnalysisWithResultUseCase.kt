package com.msd.birdclassifier.activities.main.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.msd.birdclassifier.activities.main.presentation.ui.AnalyzeCameraInput
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val NO_LABEL = "None"

class GetImageAnalysisWithResultUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getObjectDetectorUseCase: GetObjectDetectorUseCase
) {

    @SuppressLint("UnsafeOptInUsageError")
    operator fun invoke(modelName: String): AnalyzeCameraInput.ImageAnalysisWithResult {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .build()
        val result = mutableStateOf<AnalyzeCameraInput.ObjectDetectionBox?>(null)

        val executor = ContextCompat.getMainExecutor(context)
        val objectDetector = getObjectDetectorUseCase(modelName)


        imageAnalysis.setAnalyzer(executor, { image ->
            val rotationDegrees = image.imageInfo.rotationDegrees
            // insert your code here.
            if (image.image != null) {
                val inputImage = InputImage.fromMediaImage(image.image, rotationDegrees)
                objectDetector
                    .process(inputImage)
                    .addOnFailureListener {
                        result.value = null
                        image.close()
                    }
                    .addOnSuccessListener { results ->
                        if (results.isEmpty()) result.value = null
                        for (detectedObject in results) {
                            detectedObject.labels.sortByDescending { it.confidence }
                            val boundingBox = detectedObject.boundingBox
                            if (detectedObject.labels.isNotEmpty() && detectedObject.labels[0].text != NO_LABEL) {
                                result.value = AnalyzeCameraInput.ObjectDetectionBox(
                                    boundingBox,
                                    detectedObject.labels[0].text.takeIf { it != NO_LABEL },
                                    image.width.toFloat(),
                                    image.height.toFloat()
                                )
                            }
                        }
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
                    .addOnCanceledListener {
                        result.value = null
                        image.close()
                    }
            } else {
                result.value = null
                image.close()
            }
        })

        return AnalyzeCameraInput.ImageAnalysisWithResult(imageAnalysis, result)
    }
}