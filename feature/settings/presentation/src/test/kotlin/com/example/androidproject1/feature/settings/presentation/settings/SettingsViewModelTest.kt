package com.example.androidproject1.feature.settings.presentation.settings

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.Session
import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
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

    private fun viewModel() = SettingsViewModel(logger = FakeLogger(), authService = authService)

    @Test
    fun `shows the signed-in email`() = runTest {
        authService.session.value = Session(id = "session-1", email = "ada@example.com")

        assertEquals("ada@example.com", viewModel().state.value.data?.email)
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
