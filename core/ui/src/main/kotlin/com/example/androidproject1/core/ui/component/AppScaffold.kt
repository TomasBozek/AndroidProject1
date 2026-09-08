package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The shell every screen sits in: the base surface, the system insets, and an optional top bar.
 *
 * The activity draws edge to edge and `Screen()` applies no insets of its own, so this is the one
 * place that pads for them — which is why a screen should not reach for `safeDrawingPadding()`
 * itself and end up padded twice.
 *
 * Content is a `Column`; a screen that needs something else puts a `Box` or a `LazyColumn` inside
 * it. Pass [contentPadding] `false` when the content runs to the edge, as a list does.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    contentPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBase)
            .safeDrawingPadding(),
    ) {
        topBar?.invoke()
        Column(
            modifier = if (contentPadding) {
                Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.inset.xl)
            } else {
                Modifier.fillMaxSize()
            },
            content = content,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppScaffold(topBar = { AppTopBar(title = "Settings") }) {
        AppText(text = "Content sits here, padded and inside the insets.")
    }
}
