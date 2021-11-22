package com.msd.birdclassifier.activities.main.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.MutableState
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
            image.image?.let {
                objectDetector.process(InputImage.fromMediaImage(it, rotationDegrees))
                    .addOnFailureListener { onError(result, image) }
                    .addOnSuccessListener { results ->
                        if (results.isEmpty()) result.value = null

                        for (detectedObject in results) {
                            detectedObject.labels.sortByDescending { label -> label.confidence }
                            val boundingBox = detectedObject.boundingBox
                            if (detectedObject.labels.isNotEmpty() && detectedObject.labels[0].text != NO_LABEL) {
                                result.value = AnalyzeCameraInput.ObjectDetectionBox(
                                    boundingBox,
                                    detectedObject.labels[0].text.takeIf { text -> text != NO_LABEL },
                                    image.width.toFloat(),
                                    image.height.toFloat()
                                )
                            }
                        }
                    }.addOnCompleteListener { image.close() }
                    .addOnCanceledListener { onError(result, image) }
            } ?: onError(result, image)
        })

        return AnalyzeCameraInput.ImageAnalysisWithResult(imageAnalysis, result)
    }

    private fun onError(result: MutableState<AnalyzeCameraInput.ObjectDetectionBox?>, image: ImageProxy) {
        result.value = null
        image.close()
    }
}
