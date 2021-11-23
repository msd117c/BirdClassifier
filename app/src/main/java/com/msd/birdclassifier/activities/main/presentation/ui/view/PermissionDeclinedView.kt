package com.msd.birdclassifier.activities.main.presentation.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.msd.birdclassifier.R
import com.msd.birdclassifier.activities.main.presentation.ui.PermissionDeclined
import com.msd.birdclassifier.ui.theme.BirdClassifierTheme

@Composable
fun PermissionDeclinedView(state: PermissionDeclined) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = LocalContext.current.getString(R.string.permission_needed))
        Button(onClick = { state.onRetryClicked() }) {
            Text(text = LocalContext.current.getString(R.string.retry))
        }
        Button(onClick = { state.onExitClicked() }) {
            Text(text = LocalContext.current.getString(R.string.exit))
        }
    }
}

@Preview
@Composable
fun PermissionDeclinedPreview() {
    BirdClassifierTheme {
        PermissionDeclinedView(PermissionDeclined({}, {}))
    }
}
