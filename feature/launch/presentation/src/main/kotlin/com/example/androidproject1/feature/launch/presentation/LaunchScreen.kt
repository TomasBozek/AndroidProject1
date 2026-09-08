package com.example.androidproject1.feature.launch.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview

@Composable
fun LaunchScreen(
    state: LaunchState,
    onEvent: (LaunchEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Edge to edge: a screen without a Scaffold pads itself.
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    LaunchScreen(
        state = LaunchState.PREVIEW,
    ) {}
}
