package com.example.androidproject1.feature.onboarding.presentation.onboarding

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.onboarding.domain.OnboardingRepository

/**
 * The tour, and the one write that ends it.
 *
 * It emits no navigation at all: finishing marks the flag, `MainViewModel` sees it and puts the
 * auth flow on the back stack. That is the whole point of a flow switch — a screen changes what
 * the app knows, and the root decides what that means.
 */
class OnboardingViewModel(
    logger: Logger,
    private val onboardingRepository: OnboardingRepository,
) : BaseViewModel<OnboardingState, OnboardingEvent, OnboardingNavigation>(
    initialState = OnboardingState.PREVIEW,
    logger = logger.withTag("OnboardingViewModel"),
) {

    override fun onUiEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PageChanged -> uiState.updateData { copy(page = event.page) }

            OnboardingEvent.NextClicked -> {
                val state = state.value.data ?: return
                if (state.isLastPage) finish() else uiState.updateData { copy(page = page + 1) }
            }

            OnboardingEvent.SkipClicked -> finish()
        }
    }

    private fun finish() = execute(
        // The screen is about to be replaced by whatever the session says comes next, so an
        // overlay would appear and be torn down in the same breath.
        loading = {},
        action = { onboardingRepository.markSeen() },
        onData = { logger.d { "Onboarding finished" } },
    )
}
