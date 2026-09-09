package com.example.androidproject1.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A set of pages the user swipes between, with one dot per page underneath.
 *
 * For content that is *read in order and then left* — a first-run tour, a short walkthrough. Not
 * for switching between things the user comes back to: that is [AppTabs], which says what each
 * destination is instead of only how many there are.
 *
 * The dots are decoration, not a control. They are excluded from the semantics tree, because the
 * pager itself already reports the page a screen reader is on, and a row of unlabelled dots
 * announced beside it is noise. Give the caller a "next" button when a page has to be reachable
 * without a swipe.
 *
 * @param pageCount how many pages. [state] is derived from it unless one is passed in — pass one
 * when the caller has to read or drive the current page, as a "next" button does.
 * @param pageContent drawn for one page at a time; the page index is the only thing it is given.
 */
@Composable
fun AppPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(pageCount = { pageCount }),
    pageContent: @Composable (page: Int) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = state,
            // `fill = false`, so this sits inside a wrapped-height caller as well as a full-height
            // one: with room to spare the pager takes it, and with none it takes what it needs.
            modifier = Modifier.weight(1f, fill = false),
            pageContent = { page -> pageContent(page) },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.inset.lg)
                // Decoration: the pager above already announces which page this is.
                .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.inline.sm,
                Alignment.CenterHorizontally,
            ),
        ) {
            repeat(pageCount) { page -> Dot(selected = page == state.currentPage) }
        }
    }
}

@Composable
private fun Dot(selected: Boolean) {
    // Animated rather than swapped, so a dot follows the page under the user's finger instead of
    // jumping once the swipe has settled.
    val color by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.confirm.bg else AppTheme.colors.borderStrong,
        animationSpec = tween(
            durationMillis = AppTheme.motion.toggleMillis,
            easing = AppTheme.motion.toggleEasing,
        ),
        label = "dot",
    )
    // A literal: the dot's size is intrinsic to this component, not a layout measurement.
    Box(modifier = Modifier.size(DOT_SIZE).background(color, CircleShape))
}

private val DOT_SIZE = 8.dp

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppPager(pageCount = 3) { page ->
        Box(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.inset.xl),
            contentAlignment = Alignment.Center,
        ) {
            AppText(text = "Page ${page + 1}", role = TextRole.Title)
        }
    }
}
