package com.example.androidproject1.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.getComposableString
import com.example.androidproject1.core.ui.common.ThemedScreenPreview

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // The activity is edge to edge, so a screen without a Scaffold pads itself.
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.greeting.getComposableString(),
            style = MaterialTheme.typography.headlineMedium,
        )

        Button(onClick = { onEvent(HomeEvent.SettingsClicked) }) {
            Text(text = stringResource(R.string.home_settings))
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    HomeScreen(
        state = HomeState.PREVIEW,
    ) {}
}
