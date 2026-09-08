package com.example.androidproject1.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.text.resolve
import com.example.androidproject1.core.ui.theme.AppTheme

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {
    AppScaffold(screenId = "HomeScreen") {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(text = state.greeting.resolve(), role = TextRole.Display)
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
