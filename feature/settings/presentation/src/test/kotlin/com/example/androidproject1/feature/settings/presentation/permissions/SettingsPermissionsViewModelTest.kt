package com.example.androidproject1.feature.settings.presentation.permissions

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.permission.DeclaredPermission
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsPermissionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = SettingsPermissionsViewModel(logger = FakeLogger())

    @Test
    fun `renders an empty list immediately, with no loading overlay`() = runTest {
        val state = viewModel().state.value

        assertEquals(emptyList<PermissionRow>(), state.data?.permissions)
        assertNull(state.loading)
    }

    @Test
    fun `what the screen reads becomes rows labelled by their last segment`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(
            SettingsPermissionsEvent.PermissionsRead(
                listOf(
                    DeclaredPermission("android.permission.INTERNET", isGranted = true),
                    DeclaredPermission("android.permission.POST_NOTIFICATIONS", isGranted = false),
                ),
            ),
        )

        val rows = viewModel.state.value.data?.permissions.orEmpty()
        assertEquals(listOf("INTERNET", "POST_NOTIFICATIONS"), rows.map { it.label })
        assertEquals(listOf(true, false), rows.map { it.isGranted })
    }

    @Test
    fun `notifications count as available only when that permission is granted`() = runTest {
        val viewModel = viewModel()
        val notifications = "android.permission.POST_NOTIFICATIONS"

        viewModel.onUiEvent(
            SettingsPermissionsEvent.PermissionsRead(
                listOf(DeclaredPermission(notifications, isGranted = false)),
            ),
        )
        assertFalse(viewModel.state.value.data!!.canPostNotifications)

        viewModel.onUiEvent(
            SettingsPermissionsEvent.PermissionsRead(
                listOf(DeclaredPermission(notifications, isGranted = true)),
            ),
        )
        assertTrue(viewModel.state.value.data!!.canPostNotifications)
    }

    @Test
    fun `the settings button asks the host to open system settings`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsPermissionsEvent.OpenAppSettingsClicked)

        assertEquals(UiCommand.OpenAppSettings, viewModel.command.first())
    }

    @Test
    fun `up navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(SettingsPermissionsEvent.NavigateUpClicked)

        assertEquals(SettingsPermissionsNavigation.NavigateUp, viewModel.navigation.first())
    }
}
