package com.example.androidproject1.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** Which end of a scrolling region the shadow sits at. */
enum class ScrollEdge { Top, Bottom }

/**
 * The cue that a list carries on past the edge of the screen.
 *
 * On a desktop a scrollbar says this; on glass there is nothing, and a list that happens to end
 * flush with the fold is indistinguishable from one that has more below it. The shadow appears
 * only while there is something to scroll to in that direction, so it is never a decoration — if
 * it is there, there is more.
 *
 * It belongs inside a `Box` with the scrolling content, aligned to the edge it names: the content
 * scrolls under it rather than the shadow moving.
 *
 * ```
 * Box {
 *     LazyColumn(state = listState) { … }
 *     AppScrollShadow(listState, ScrollEdge.Bottom, Modifier.align(Alignment.BottomCenter))
 * }
 * ```
 */
@Composable
fun AppScrollShadow(
    state: ScrollableState,
    edge: ScrollEdge,
    modifier: Modifier = Modifier,
) {
    val more = when (edge) {
        ScrollEdge.Top -> state.canScrollBackward
        ScrollEdge.Bottom -> state.canScrollForward
    }
    // Faded rather than switched: a shadow that blinks on at the first pixel of scroll reads as a
    // rendering fault, and the fade is short enough not to lag the finger.
    val visible by animateFloatAsState(
        targetValue = if (more) 1f else 0f,
        label = "scrollShadow",
    )
    val scrim = AppTheme.colors.scrim.copy(alpha = AppTheme.colors.scrimAlpha)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HEIGHT)
            .alpha(visible)
            .background(
                Brush.verticalGradient(
                    when (edge) {
                        ScrollEdge.Top -> listOf(scrim, Color.Transparent)
                        ScrollEdge.Bottom -> listOf(Color.Transparent, scrim)
                    },
                ),
            ),
    )
}

/** Tall enough to read as depth, short enough not to dim the last row. */
private val HEIGHT = 12.dp

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    val scroll = rememberScrollState()
    Box(modifier = Modifier.height(96.dp)) {
        Column(modifier = Modifier.verticalScroll(scroll)) {
            repeat(6) { AppListItem(headline = "Row ${it + 1}") }
        }
        AppScrollShadow(scroll, ScrollEdge.Top, Modifier.align(Alignment.TopCenter))
        AppScrollShadow(scroll, ScrollEdge.Bottom, Modifier.align(Alignment.BottomCenter))
    }
}
