package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.example.androidproject1.core.ui.analytics.ScreenViewEffect
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
 *
 * @param screenId the screen's own test identifier, which is its name — `"SettingsScreen"`. It is
 *   published as a resource id, so `assertVisible: id: "SettingsScreen"` is the universal check
 *   that a flow is where it meant to be, and it costs a screen one argument. Elements inside use
 *   `Modifier.testTag("settings_permissionsButton")`: the screen's name in camelCase without
 *   `Screen`, an underscore, then the element. See CLAUDE.md.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    screenId: String? = null,
    topBar: @Composable (() -> Unit)? = null,
    contentPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    ScreenViewEffect(screenId)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBase)
            // Publishes every testTag below as a resource id, so one Maestro flow addresses the
            // same ids on Android that Playwright addresses as data-testid on the web.
            .semantics { testTagsAsResourceId = true }
            .then(if (screenId != null) Modifier.testTag(screenId) else Modifier)
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
    AppScaffold(screenId = "SettingsScreen", topBar = { AppTopBar(title = "Settings") }) {
        AppText(text = "Content sits here, padded and inside the insets.")
    }
}
