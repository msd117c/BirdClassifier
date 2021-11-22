package com.msd.birdclassifier.activities.main.presentation.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Rect
import android.view.ScaleGestureDetector
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.msd.birdclassifier.ui.theme.BirdClassifierTheme
import kotlin.math.max
import kotlin.math.min

@Composable
fun MainActivityView(state: MainViewState) {
    when (state) {
        is PermissionDeclined -> PermissionDeclinedView(state)
        is AnalyzeCameraInput -> CameraPreview(state)
        is RequestingPermission -> Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {}
        is ExitApp -> (LocalContext.current as MainActivity).finishAffinity()
    }
}

@Composable
fun PermissionDeclinedView(state: PermissionDeclined) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Permissions needed")
        Button(onClick = { state.onRetryClicked() }) {
            Text(text = "Retry")
        }
        Button(onClick = { state.onExitClicked() }) {
            Text(text = "Exit")
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreview(state: AnalyzeCameraInput) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.unbindAll()
                val cameraX = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    state.imageAnalysisWithResult.imageAnalysis,
                    preview
                )

                val cameraControl = cameraX.cameraControl
                val cameraInfo = cameraX.cameraInfo

                val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val scale: Float = (cameraInfo.zoomState.value?.zoomRatio ?: 0f).times(detector.scaleFactor)
                        cameraControl.setZoomRatio(scale)
                        return true
                    }
                }

                val scaleGestureDetector = ScaleGestureDetector(context, listener)

                previewView.setOnTouchListener { _, event ->
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize(),
    )
    state.imageAnalysisWithResult.objectDetectionBoxState.value?.let { objectDetectionBox ->
        DrawFocusRect(objectDetectionBox = objectDetectionBox)
    }
}

@Composable
fun DrawFocusRect(objectDetectionBox: AnalyzeCameraInput.ObjectDetectionBox) {
    val context = LocalContext.current
    val realWidth: Float
    val realHeight: Float
    if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        realWidth = min(objectDetectionBox.imageWidth, objectDetectionBox.imageHeight)
        realHeight = max(objectDetectionBox.imageWidth, objectDetectionBox.imageHeight)
    } else {
        realWidth = max(objectDetectionBox.imageWidth, objectDetectionBox.imageHeight)
        realHeight = min(objectDetectionBox.imageWidth, objectDetectionBox.imageHeight)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier,
            onDraw = {
                val widthScale = maxWidth.toPx().div(realWidth)
                val heightScale = maxHeight.toPx().div(realHeight)

                drawRect(
                    color = Color.Red,
                    topLeft = Offset(
                        objectDetectionBox.box.left.times(widthScale),
                        objectDetectionBox.box.top.times(heightScale)
                    ),
                    size = Size(
                        objectDetectionBox.box.width().times(widthScale),
                        objectDetectionBox.box.height().times(heightScale)
                    ),
                    style = Stroke(5f)
                )

                objectDetectionBox.label?.let { label ->
                    drawIntoCanvas { canvas ->
                        val paint = Paint().also {
                            it.textAlign = Paint.Align.CENTER
                            it.textSize = 64f
                            it.color = android.graphics.Color.RED
                        }

                        val bounds = Rect()
                        paint.getTextBounds(
                            label,
                            0,
                            label.length,
                            bounds
                        )
                        val topMargin = bounds.height().div(3)

                        val height = bounds.height() + topMargin

                        with(objectDetectionBox.box) {
                            val x = left.times(widthScale) + width().times(widthScale).div(2)
                            val y = bottom.times(heightScale) + height

                            canvas.nativeCanvas.drawText(label, x, y, paint)
                        }
                    }
                }
            }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DefaultPreview() {
    BirdClassifierTheme {
        DrawFocusRect(
            AnalyzeCameraInput.ObjectDetectionBox(
                Rect(229, 181, 403, 299),
                "Hello",
                480f,
                640f
            )
        )
    }
}