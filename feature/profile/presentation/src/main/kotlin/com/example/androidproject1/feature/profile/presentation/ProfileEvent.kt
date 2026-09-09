package com.example.androidproject1.feature.profile.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface ProfileEvent : UiEvent {

    data class NameChanged(val name: String) : ProfileEvent

    data class EmailChanged(val email: String) : ProfileEvent

    /** Pressed whether or not the form is valid — see [ProfileViewModel]. */
    data object SaveClicked : ProfileEvent

    /** Opens the camera half of the picture section, which is what the permission gate guards. */
    data object TakePhotoClicked : ProfileEvent

    /**
     * A picture arrived, from the Photo Picker or from the camera.
     *
     * One event for both, because what happens next is the same either way: the bytes at [uri]
     * belong to somebody else and have to be copied into storage this app owns.
     */
    data class AvatarPicked(val uri: String) : ProfileEvent

    /** Raised by the rationale once the system will not show the permission dialog again. */
    data object OpenAppSettingsClicked : ProfileEvent

    data object NavigateUpClicked : ProfileEvent
}
