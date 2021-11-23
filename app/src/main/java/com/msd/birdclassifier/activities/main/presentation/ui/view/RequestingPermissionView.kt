package com.msd.birdclassifier.activities.main.presentation.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.msd.birdclassifier.ui.theme.BirdClassifierTheme

@Composable
fun RequestingPermissionView() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {}
}

@Preview
@Composable
fun RequestingPermissionPreview() {
    BirdClassifierTheme {
        RequestingPermissionView()
    }
}
