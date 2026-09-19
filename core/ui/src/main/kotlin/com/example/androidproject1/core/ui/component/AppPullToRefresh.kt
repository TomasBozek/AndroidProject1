package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A scrolling region that can be pulled down to reload what it shows.
 *
 * The gesture is the list's, not a button's: a person who wants fresh data on a phone pulls, and a
 * list that ignores the pull reads as broken. The screen owns the two halves — `refreshing`, which
 * is whatever its view model says about the call in flight, and `onRefresh`, the event that starts
 * it — so the indicator stops exactly when the data lands and never on a timer.
 *
 * Wrap the scrolling content and nothing else: the box fills whatever it is given, and the
 * indicator draws over the top edge of the content in the brand accent on a raised surface.
 *
 * ```
 * AppPullToRefresh(refreshing = state.refreshing, onRefresh = { onEvent(Refresh) }) {
 *     LazyColumn { … }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefresh(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier.semantics {
            // The gesture has no visible label, so the state is what a screen reader has to go on.
            stateDescription = if (refreshing) PULL_TO_REFRESH_REFRESHING else PULL_TO_REFRESH_IDLE
        },
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = refreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = AppTheme.colors.surfaceRaised,
                color = AppTheme.colors.confirm.bg,
            )
        },
        content = content,
    )
}

/** What the box's `stateDescription` says; a test reads it, and so does a screen reader. */
const val PULL_TO_REFRESH_REFRESHING: String = "refreshing"
const val PULL_TO_REFRESH_IDLE: String = "idle"

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // Refreshing, so the indicator is in the frame: an idle box looks like the list it wraps.
    AppPullToRefresh(
        refreshing = true,
        onRefresh = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.density.listRowHeight * 4),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            repeat(6) { AppListItem("Row ${it + 1}") }
        }
    }
}
