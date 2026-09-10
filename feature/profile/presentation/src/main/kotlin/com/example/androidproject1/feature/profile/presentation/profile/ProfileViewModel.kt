package com.example.androidproject1.feature.profile.presentation.profile

import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.feature.profile.domain.ProfileRepository
import com.example.androidproject1.feature.profile.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.form.Form
import com.example.androidproject1.service.core.ui.form.discardAlert
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class ProfileViewModel(
    logger: Logger,
    private val profileRepository: ProfileRepository,
) : BaseViewModel<ProfileState, ProfileEvent, ProfileNavigation>(
    // An empty form is a state the screen can draw, so it renders straight away and fills in when
    // the stored profile arrives. `null` here would put a spinner over a form for the length of
    // one preferences read.
    initialState = ProfileState.EMPTY,
    logger = logger.withTag("ProfileViewModel"),
) {

    init {
        execute(
            // No overlay for the same reason: it would only flash.
            action = { profileRepository.get() },
            onData = { profile ->
                updateData {
                    // Whoever is typing wins. The stored profile is where the form starts, not an
                    // update to apply over someone's hands.
                    if (name.touched || email.touched) {
                        this
                    } else {
                        ProfileState.of(profile).copy(cameraOpen = cameraOpen)
                    }
                }
            },
        )
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.AlertResult.Confirmed && event.id == ALERT_ID_DISCARD) {
            uiState.clearAlert()
            navigate(ProfileNavigation.NavigateUp)
            return
        }
        super.onSystemEvent(event)
    }

    override fun onUiEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NameChanged -> updateData { copy(name = name.changed(event.name)) }

            // Trimmed as it is typed, not at save. An address never has whitespace around it,
            // pasting one usually brings some, and trimming only at save would validate and store
            // something other than what the field shows.
            is ProfileEvent.EmailChanged ->
                updateData { copy(email = email.changed(event.email.trim())) }

            ProfileEvent.TakePhotoClicked -> updateData { copy(cameraOpen = true) }

            is ProfileEvent.AvatarPicked -> setAvatar(event.uri)

            // The screen has no Context, so "send them to system settings" is a command like any
            // other and `Screen()` is what carries it out.
            ProfileEvent.OpenAppSettingsClicked -> sendCommand(UiCommand.OpenAppSettings)

            ProfileEvent.SaveClicked -> save()

            ProfileEvent.NavigateUpClicked -> navigate(ProfileNavigation.NavigateUp)

            ProfileEvent.BackRequested -> uiState.setAlert(discardAlert())
        }
    }

    /**
     * Save, or say why not.
     *
     * The button stays enabled on an invalid form on purpose. A disabled submit is the thing
     * `core.5` was written against: it can say *whether* the form is submittable and never *why*,
     * so someone with a two-letter minimum they have not met is left pressing a dead button.
     * Pressing it here touches every field, which is what reveals the errors, and the first one
     * is repeated in a snackbar for anyone who is looking at the button rather than the field.
     */
    private fun save() {
        val state = uiState.value.data ?: return
        if (!state.canSubmit) {
            val (name, email) = Form.touchAll(state.name, state.email)
            val revealed = state.copy(name = name, email = email)
            updateData { revealed }
            revealed.firstError?.let { showSnackbar(it) }
            return
        }

        execute(
            loading = overlay(),
            action = {
                profileRepository.save(
                    Profile(
                        // A name keeps its spaces while it is being typed — "Jana " is halfway
                        // through "Jana Nováková" — so it is trimmed here instead.
                        name = state.name.value.trim(),
                        email = state.email.value,
                        avatarUri = state.avatarUri,
                    ),
                )
            },
            onData = { showSnackbar(R.string.profile_saved.toUiText()) },
        )
    }

    /**
     * Copying can take a moment for a photograph off a modern sensor, so this one does get the
     * overlay — with wording, because "Loading" over a screen the user just took a picture on says
     * nothing.
     */
    private fun setAvatar(sourceUri: String) = execute(
        loading = overlay(R.string.profile_saving_photo.toUiText()),
        action = { profileRepository.setAvatar(sourceUri) },
        onData = { stored -> updateData { copy(avatarUri = stored, cameraOpen = false) } },
    )
}
