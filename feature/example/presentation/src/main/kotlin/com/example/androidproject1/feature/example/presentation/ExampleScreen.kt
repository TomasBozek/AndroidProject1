package com.example.androidproject1.feature.example.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview

@Composable
fun ExampleScreen(
    state: ExampleState,
    onEvent: (ExampleEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // The activity is edge to edge, so a screen without a Scaffold pads itself.
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.example_title),
            textAlign = TextAlign.Center,
        )
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    ExampleScreen(
        state = ExampleState.PREVIEW,
    ) {}
}
