package com.example.androidproject1.feature.settings.presentation.settings

import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.Session
import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.test.FakeThemeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authService = FakeAuthService()
    private val themeRepository = FakeThemeRepository()

    private fun viewModel() = SettingsViewModel(
        logger = FakeLogger(),
        authService = authService,
        themeRepository = themeRepository,
    )

    @Test
    fun `shows the signed-in email`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals("ada@example.com", viewModel().state.value.data?.email)
    }

    @Test
    fun `the debug menu entry is off until the destination says otherwise`() = runTest {
        val viewModel = viewModel()

        assertEquals(false, viewModel.state.value.data?.debugMenuEnabled)

        viewModel.onUiEvent(SettingsEvent.DebugMenuAvailable(true))

        assertEquals(true, viewModel.state.value.data?.debugMenuEnabled)
    }

    @Test
    fun `a session change does not forget the debug menu entry`() = runTest {
        // The regression a rebuilt SettingsState causes: the entry appears and then vanishes on
        // the next emission of the session.
        val viewModel = viewModel()
        viewModel.onUiEvent(SettingsEvent.DebugMenuAvailable(true))

        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals(true, viewModel.state.value.data?.debugMenuEnabled)
    }

    @Test
    fun `the stored theme is what the control shows`() = runTest {
        themeRepository.theme.value = ThemePreference.Dark

        assertEquals(ThemePreference.Dark, viewModel().state.value.data?.theme)
    }

    @Test
    fun `picking a theme stores it and shows what came back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsEvent.ThemeSelected(ThemePreference.Light))

        assertEquals(ThemePreference.Light, themeRepository.theme.value)
        assertEquals(ThemePreference.Light, viewModel.state.value.data?.theme)
    }

    @Test
    fun `a write the store refuses leaves the control where it was`() = runTest {
        // No optimistic update: the state follows the flow, not the tap.
        val viewModel = viewModel()
        themeRepository.failWith = UnexpectedError(message = "disk gone")

        viewModel.onUiEvent(SettingsEvent.ThemeSelected(ThemePreference.Dark))

        assertEquals(ThemePreference.System, viewModel.state.value.data?.theme)
    }

    @Test
    fun `logging out asks first and does nothing until confirmed`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsEvent.LogoutClicked)

        assertNotNull(viewModel.state.value.alert)
        assertTrue(authService.logoutCount == 0)
    }

    @Test
    fun `declining the confirmation dismisses it and keeps the session`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(SettingsEvent.LogoutClicked)
        val alertId = viewModel.state.value.alert!!.id

        viewModel.onSystemEvent(SystemEvent.AlertResult.Declined(alertId))

        assertNull(viewModel.state.value.alert)
        assertEquals(0, authService.logoutCount)
    }

    @Test
    fun `confirming the alert logs out`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(SettingsEvent.LogoutClicked)
        val alertId = viewModel.state.value.alert!!.id

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(alertId))

        assertNull(viewModel.state.value.alert)
        assertEquals(1, authService.logoutCount)
    }
}
