package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.core.domain.ErrorTracker
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DevMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authService = FakeAuthService()
    private val offlineSwitch = RecordingOfflineSwitch()
    private val recorded = mutableListOf<Throwable>()

    private val errorTracker = object : ErrorTracker {
        override fun recordNonFatal(throwable: Throwable, message: String?) {
            recorded += throwable
        }

        override fun log(message: String) = Unit

        override fun setUser(id: String?) = Unit
    }

    private fun viewModel(switch: OfflineSwitch = offlineSwitch) = DevMenuViewModel(
        logger = FakeLogger(),
        buildInfo = BuildInfo.PREVIEW,
        offlineSwitch = switch,
        authService = authService,
        errorTracker = errorTracker,
    )

    @Test
    fun `renders the build it was given immediately`() = runTest {
        val state = viewModel().state.value

        assertEquals(BuildInfo.PREVIEW, state.data?.build)
        assertNull(state.loading)
    }

    @Test
    fun `shows the signed-in address, and nothing while signed out`() = runTest {
        val viewModel = viewModel()
        assertNull(viewModel.state.value.data?.session)

        authService.login("ada@example.com")

        assertEquals("ada@example.com", viewModel.state.value.data?.session)
    }

    @Test
    fun `toggling offline writes the switch and reads it back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DevMenuEvent.OfflineToggled(true))

        assertTrue(offlineSwitch.isOffline())
        assertTrue(viewModel.state.value.data?.offline == true)
    }

    @Test
    fun `a switch that refuses the write leaves the state as it is`() = runTest {
        // The flag lives outside the process, so the screen reports what it read back rather than
        // what it asked for.
        val viewModel = viewModel(switch = OfflineSwitch.Unsupported)

        viewModel.onUiEvent(DevMenuEvent.OfflineToggled(true))

        assertFalse(viewModel.state.value.data?.offline == true)
    }

    @Test
    fun `the crash button reports to the tracker rather than throwing`() = runTest {
        viewModel().onUiEvent(DevMenuEvent.CrashClicked)

        assertEquals(1, recorded.size)
        assertTrue(recorded.single() is DebugMenuTestReport)
    }
}

private class RecordingOfflineSwitch : OfflineSwitch {

    private var offline = false

    override val isSupported: Boolean = true

    override fun isOffline(): Boolean = offline

    override fun setOffline(offline: Boolean) {
        this.offline = offline
    }
}
