package com.example.androidproject1.feature.settings.presentation.language

import com.example.androidproject1.feature.settings.domain.AppLanguage
import com.example.androidproject1.feature.settings.domain.test.FakeLanguageRepository
import com.example.androidproject1.service.core.domain.error.UnexpectedError
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** What the state becomes: the ring follows the store, and a tap writes to it. */
class SettingsLanguageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val languageRepository = FakeLanguageRepository()

    private fun viewModel() = SettingsLanguageViewModel(
        logger = FakeLogger(),
        languageRepository = languageRepository,
    )

    @Test
    fun `renders at once, in the stored language`() = runTest {
        languageRepository.language.value = AppLanguage.Czech

        val state = viewModel().state.value

        assertEquals(AppLanguage.Czech, state.data?.selected)
        // The screen has a state to draw from the first frame, so no overlay.
        assertNull(state.loading)
        assertNull(state.alert)
    }

    @Test
    fun `picking a language stores it and shows what came back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsLanguageEvent.LanguageSelected(AppLanguage.English))

        assertEquals(AppLanguage.English, languageRepository.language.value)
        assertEquals(AppLanguage.English, viewModel.state.value.data?.selected)
    }

    @Test
    fun `a write the store refuses leaves the ring where it was, and says so`() = runTest {
        // No optimistic update: the state follows the flow, not the tap.
        val viewModel = viewModel()
        languageRepository.failWith = UnexpectedError(message = "no locale service")

        viewModel.onUiEvent(SettingsLanguageEvent.LanguageSelected(AppLanguage.Czech))

        assertEquals(AppLanguage.System, viewModel.state.value.data?.selected)
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun `up is a navigation intent`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsLanguageEvent.NavigateUpClicked)

        assertEquals(SettingsLanguageNavigation.NavigateUp, viewModel.navigation.first())
    }
}
