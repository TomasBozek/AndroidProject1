package com.example.androidproject1.feature.onboarding.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppPager
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.onboarding.presentation.R
import com.example.androidproject1.feature.onboarding.presentation.component.OnboardingPageContent

/**
 * The first-run tour: three pages, a "next" that becomes "get started", and a way out of it.
 *
 * The pager and the state are kept in step in both directions — a swipe is reported as an event,
 * and a page the ViewModel chose is scrolled to. Neither drives the other on its own: the button
 * has to be able to move the pager, and the pager has to be able to tell the ViewModel where the
 * user's finger left it.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { state.pages.size })

    LaunchedEffect(pagerState.currentPage) {
        onEvent(OnboardingEvent.PageChanged(pagerState.currentPage))
    }
    LaunchedEffect(state.page) {
        // Guarded, or the two effects chase each other: this one only acts on a page the pager is
        // not already on, which is exactly the case where the button moved it.
        if (pagerState.currentPage != state.page) pagerState.animateScrollToPage(state.page)
    }

    AppScaffold(screenId = "OnboardingScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppPager(
                pageCount = state.pages.size,
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboarding_pageList"),
            ) { page ->
                OnboardingPageContent(page = state.pages[page])
            }

            AppButton(
                label = stringResource(
                    if (state.isLastPage) R.string.onboarding_start else R.string.onboarding_next,
                ),
                onClick = { onEvent(OnboardingEvent.NextClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_nextButton"),
            )
            AppButton(
                label = stringResource(R.string.onboarding_skip),
                onClick = { onEvent(OnboardingEvent.SkipClicked) },
                kind = ButtonKind.Ghost,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_skipButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(OnboardingStatePreviews::class) state: OnboardingState,
) = ThemedScreenPreview {
    OnboardingScreen(
        state = state,
    ) {}
}
