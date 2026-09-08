package com.example.androidproject1.feature.settings.presentation

import android.Manifest
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.permission.DeclaredPermission
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.update

class SettingsPermissionsViewModel(
    logger: Logger,
) : BaseViewModel<SettingsPermissionsState, SettingsPermissionsEvent, SettingsPermissionsNavigation>(
    // Renders straight away: an empty list is the honest state until the screen has read the
    // platform's answer, and it arrives on the first composition.
    initialState = SettingsPermissionsState(permissions = emptyList(), canPostNotifications = false),
    logger = logger.withTag("SettingsPermissionsViewModel"),
) {

    override fun onUiEvent(event: SettingsPermissionsEvent) {
        when (event) {
            is SettingsPermissionsEvent.PermissionsRead -> show(event.permissions)

            SettingsPermissionsEvent.OpenAppSettingsClicked ->
                sendCommand(UiCommand.OpenAppSettings)

            SettingsPermissionsEvent.NavigateUpClicked ->
                navigate(SettingsPermissionsNavigation.NavigateUp)
        }
    }

    private fun show(declared: List<DeclaredPermission>) {
        val rows = declared.map {
            PermissionRow(
                name = it.name,
                label = it.name.substringAfterLast('.'),
                isGranted = it.isGranted,
            )
        }

        uiState.update {
            it.copy(
                data = SettingsPermissionsState(
                    permissions = rows,
                    canPostNotifications = rows.any { row ->
                        row.name == Manifest.permission.POST_NOTIFICATIONS && row.isGranted
                    },
                ),
            )
        }
    }
}
