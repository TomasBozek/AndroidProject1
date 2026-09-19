package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.feature.auth.domain.test.FakeAuthService
import com.example.androidproject1.feature.devmenu.presentation.ApiSwitch
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.NotificationTester
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import com.example.androidproject1.service.core.domain.ErrorTracker
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
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
    private val apiSwitch = RecordingApiSwitch()
    private val recorded = mutableListOf<Throwable>()

    private val errorTracker = object : ErrorTracker {
        override fun recordNonFatal(throwable: Throwable, message: String?) {
            recorded += throwable
        }

        override fun log(message: String) = Unit

        override fun setUser(id: String?) = Unit
    }

    private val notificationTester = RecordingNotificationTester()

    private fun viewModel(
        switch: OfflineSwitch = offlineSwitch,
        api: ApiSwitch = apiSwitch,
        tester: NotificationTester = notificationTester,
    ) = DevMenuViewModel(
        logger = FakeLogger(),
        buildInfo = BuildInfo.PREVIEW,
        offlineSwitch = switch,
        apiSwitch = api,
        notificationTester = tester,
        authService = authService,
        errorTracker = errorTracker,
        jumps = DevMenuState.PREVIEW.jumps,
    )

    @Test
    fun `renders the build it was given immediately`() = runTest {
        val state = viewModel().state.value

        assertEquals(BuildInfo.PREVIEW, state.data?.build)
        assertEquals(DevMenuState.PREVIEW.jumps, state.data?.jumps)
        assertNull(state.loading)
    }

    @Test
    fun `a jump navigates with the jump it was given`() = runTest {
        // The jump carries its own back-stack call; the view model only hands it on.
        val viewModel = viewModel()
        val jump = DevMenuState.PREVIEW.jumps.single { it.id == "inventoryEditor" }

        viewModel.onUiEvent(DevMenuEvent.JumpClicked(jump))

        assertEquals(DevMenuNavigation.Jump(jump), viewModel.navigation.first())
    }

    @Test
    fun `shows the signed-in address, and nothing while signed out`() = runTest {
        val viewModel = viewModel()
        assertNull(viewModel.state.value.data?.session)

        authService.login("ada@example.com")

        assertEquals("ada@example.com", viewModel.state.value.data?.session)
    }

    @Test
    fun `toggling the real API writes the switch and reads it back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DevMenuEvent.RealApiToggled(true))

        assertTrue(apiSwitch.isRealApi())
        assertTrue(viewModel.state.value.data?.realApi == true)
    }

    @Test
    fun `a build without a key says so and a build with a real host has no switch`() = runTest {
        assertFalse(
            viewModel(api = RecordingApiSwitch(isKeyPresent = false)).state.value.data?.realApiKeyPresent == true,
        )

        val state = viewModel(api = ApiSwitch.Unsupported).state.value
        assertFalse(state.data?.realApiSupported == true)
        assertFalse(state.data?.realApi == true)
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
    fun `posting the test notification says it went out`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DevMenuEvent.NotificationClicked)

        assertEquals(1, notificationTester.postCount)
        assertTrue(viewModel.command.first() is UiCommand.ShowSnackbar)
    }

    @Test
    fun `a refused notification is reported rather than swallowed`() = runTest {
        // Posted-and-missed and never-posted look identical from the debug menu, and only one
        // of them is a bug worth chasing.
        val viewModel = viewModel(tester = NotificationTester.Unsupported)

        viewModel.onUiEvent(DevMenuEvent.NotificationClicked)

        assertTrue(viewModel.command.first() is UiCommand.ShowSnackbar)
    }

    @Test
    fun `the crash button reports to the tracker rather than throwing`() = runTest {
        viewModel().onUiEvent(DevMenuEvent.CrashClicked)

        assertEquals(1, recorded.size)
        assertTrue(recorded.single() is DebugMenuTestReport)
    }
}

private class RecordingNotificationTester : NotificationTester {

    var postCount = 0
        private set

    override fun post(): Boolean {
        postCount++
        return true
    }
}

private class RecordingApiSwitch(override val isKeyPresent: Boolean = true) : ApiSwitch {

    override val isSupported: Boolean = true

    private var realApi = false

    override fun isRealApi(): Boolean = realApi

    override fun setRealApi(realApi: Boolean) {
        this.realApi = realApi
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
