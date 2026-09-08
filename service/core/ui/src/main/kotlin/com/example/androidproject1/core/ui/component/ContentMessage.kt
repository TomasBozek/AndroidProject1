package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.resolve

/**
 * Renders a [ContentState] in place of a screen's content.
 *
 * The single implementation of "couldn't load, try again" and "nothing here yet" — rendered by
 * `Screen()`, so a feature never builds its own.
 */
@Composable
fun ContentMessage(
    state: ContentState,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // Shown instead of the content, which may or may not have had a Scaffold of its own.
            .safeDrawingPadding()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.title?.let {
            Text(
                text = it.resolve(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = state.message.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        state.actionLabel?.let {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(text = it.resolve())
            }
        }
    }
}
