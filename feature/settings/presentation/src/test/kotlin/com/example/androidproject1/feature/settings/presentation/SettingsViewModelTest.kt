package com.example.androidproject1.feature.settings.presentation

import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.auth.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

    private val authService = RecordingAuthService()

    private fun viewModel() = SettingsViewModel(logger = FakeLogger(), authService = authService)

    @Test
    fun `shows the signed-in email`() = runTest {
        authService.session.value = Session(email = "ada@example.com")

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

    private class RecordingAuthService : AuthService {

        val session = MutableStateFlow<Session?>(null)
        var logoutCount = 0

        override fun observeSession(): Flow<Outcome<Session?>> = session.map { Outcome.Success(it) }

        override fun isLoggedIn(): Flow<Outcome<Boolean>> = session.map { Outcome.Success(it != null) }

        override suspend fun login(email: String): Outcome<Unit> {
            session.value = Session(email = email)
            return Outcome.Success(Unit)
        }

        override suspend fun logout(): Outcome<Unit> {
            logoutCount++
            session.value = null
            return Outcome.Success(Unit)
        }
    }
}
