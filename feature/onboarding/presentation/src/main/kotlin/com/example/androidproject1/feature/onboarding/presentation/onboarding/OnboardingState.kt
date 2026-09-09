package com.example.androidproject1.feature.onboarding.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.feature.onboarding.presentation.R

/**
 * @property pages what the tour says, in order. In the state rather than in the screen because the
 * ViewModel decides when the last page has been reached, and it cannot do that from a layout.
 * @property page the page currently in view. The pager reports its own scrolling back as an event,
 * so this stays the single answer to "where are we" — the "Next" button and the label both read it.
 */
@Immutable
data class OnboardingState(
    val pages: List<OnboardingPage>,
    val page: Int,
) {

    val isLastPage: Boolean get() = page == pages.lastIndex

    companion object {

        /** The tour itself. Here rather than in the ViewModel so the previews show the real copy. */
        val DEFAULT_PAGES = listOf(
            OnboardingPage(
                title = R.string.onboarding_page_browse_title.toUiText(),
                body = R.string.onboarding_page_browse_body.toUiText(),
            ),
            OnboardingPage(
                title = R.string.onboarding_page_cart_title.toUiText(),
                body = R.string.onboarding_page_cart_body.toUiText(),
            ),
            OnboardingPage(
                title = R.string.onboarding_page_account_title.toUiText(),
                body = R.string.onboarding_page_account_body.toUiText(),
            ),
        )

        // Declared after the pages, because a companion object initialises in source order and
        // this reads one of them.
        val PREVIEW = OnboardingState(page = 0, pages = DEFAULT_PAGES)
    }
}

/** One page of the tour. `UiText`, so the ViewModel can name a string without holding a Context. */
@Immutable
data class OnboardingPage(
    val title: UiText,
    val body: UiText,
)

/** The states this screen is drawn in: the first page, a middle one, and the last. */
class OnboardingStatePreviews : PreviewParameterProvider<OnboardingState> {

    override val values = sequenceOf(
        OnboardingState.PREVIEW,
        OnboardingState.PREVIEW.copy(page = 1),
        OnboardingState.PREVIEW.copy(page = OnboardingState.DEFAULT_PAGES.lastIndex),
    )
}
