package com.example.androidproject1.feature.onboarding.presentation.onboarding

import com.example.androidproject1.feature.onboarding.domain.test.FakeOnboardingRepository
import com.example.androidproject1.service.core.domain.error.UnexpectedError
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingRepository = FakeOnboardingRepository()

    private fun viewModel() = OnboardingViewModel(
        logger = FakeLogger(),
        onboardingRepository = onboardingRepository,
    )

    private val lastPage = OnboardingState.DEFAULT_PAGES.lastIndex

    @Test
    fun `renders the first page immediately`() = runTest {
        val state = viewModel().state.value

        assertEquals(0, state.data?.page)
        // A screen given an initialState should not start behind the loading overlay.
        assertNull(state.loading)
    }

    @Test
    fun `next advances a page without finishing`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(OnboardingEvent.NextClicked)

        assertEquals(1, viewModel.state.value.data?.page)
        assertFalse(onboardingRepository.seen.value)
    }

    @Test
    fun `a swipe is what moves the page, not the button alone`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(OnboardingEvent.PageChanged(lastPage))

        assertEquals(lastPage, viewModel.state.value.data?.page)
        assertEquals(true, viewModel.state.value.data?.isLastPage)
    }

    @Test
    fun `next on the last page finishes the tour`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(OnboardingEvent.PageChanged(lastPage))

        viewModel.onUiEvent(OnboardingEvent.NextClicked)

        assertTrue(onboardingRepository.seen.value)
    }

    @Test
    fun `skip finishes it from wherever it is`() = runTest {
        viewModel().onUiEvent(OnboardingEvent.SkipClicked)

        assertTrue(onboardingRepository.seen.value)
    }

    @Test
    fun `a write that fails says so rather than stranding the user on the tour`() = runTest {
        onboardingRepository.failWith = UnexpectedError(message = "disk gone")
        val viewModel = viewModel()

        viewModel.onUiEvent(OnboardingEvent.SkipClicked)

        assertFalse(onboardingRepository.seen.value)
        assertNotNull(viewModel.state.value.alert)
    }
}
