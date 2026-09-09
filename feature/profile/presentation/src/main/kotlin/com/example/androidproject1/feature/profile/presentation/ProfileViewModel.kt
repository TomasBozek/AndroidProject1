package com.example.androidproject1.feature.profile.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.form.Form
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.feature.profile.domain.ProfileRepository

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
            loading = {},
            action = { profileRepository.get() },
            onData = { profile ->
                uiState.updateData {
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

    override fun onUiEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NameChanged -> uiState.updateData { copy(name = name.changed(event.name)) }

            // Trimmed as it is typed, not at save. An address never has whitespace around it,
            // pasting one usually brings some, and trimming only at save would validate and store
            // something other than what the field shows.
            is ProfileEvent.EmailChanged ->
                uiState.updateData { copy(email = email.changed(event.email.trim())) }

            ProfileEvent.TakePhotoClicked -> uiState.updateData { copy(cameraOpen = true) }

            is ProfileEvent.AvatarPicked -> setAvatar(event.uri)

            // The screen has no Context, so "send them to system settings" is a command like any
            // other and `Screen()` is what carries it out.
            ProfileEvent.OpenAppSettingsClicked -> sendCommand(UiCommand.OpenAppSettings)

            ProfileEvent.SaveClicked -> save()

            ProfileEvent.NavigateUpClicked -> navigate(ProfileNavigation.NavigateUp)
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
            uiState.updateData { revealed }
            revealed.firstError?.let { showSnackbar(it) }
            return
        }

        execute(
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
        loadingMessage = R.string.profile_saving_photo.toUiText(),
        action = { profileRepository.setAvatar(sourceUri) },
        onData = { stored -> uiState.updateData { copy(avatarUri = stored, cameraOpen = false) } },
    )
}
