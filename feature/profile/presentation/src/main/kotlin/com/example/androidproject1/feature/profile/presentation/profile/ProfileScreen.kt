package com.example.androidproject1.feature.profile.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppAvatarPhoto
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.profile.presentation.R
import com.example.androidproject1.feature.profile.presentation.component.CameraSection
import com.example.androidproject1.feature.profile.presentation.component.PictureButtons
import com.example.androidproject1.service.core.ui.form.DiscardBackHandler
import com.example.androidproject1.service.core.ui.text.resolve

@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
) {
    AppScaffold(
        screenId = "ProfileScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.profile_title),
                onNavigateUp = { onEvent(ProfileEvent.NavigateUpClicked) },
                navigateUpTestTag = "profile_upButton",
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // The error lines under two fields plus an open camera section is more than a
                // short phone has, and a form that cannot reach its own submit button is a form
                // nobody can use.
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppAvatarPhoto(
                name = state.name.value.ifBlank { stringResource(R.string.profile_avatar_unnamed) },
                photo = state.avatarUri,
                contentDescription = stringResource(R.string.profile_avatar),
                modifier = Modifier.testTag("profile_avatarValue"),
            )

            PictureButtons(onEvent = onEvent, modifier = Modifier.fillMaxWidth())

            if (state.cameraOpen) {
                CameraSection(onEvent = onEvent, modifier = Modifier.fillMaxWidth())
            }

            DiscardBackHandler(dirty = state.isDirty) { onEvent(ProfileEvent.BackRequested) }

            AppTextField(
                value = state.name.value,
                errorText = state.name.error?.resolve(),
                onValueChange = { onEvent(ProfileEvent.NameChanged(it)) },
                label = stringResource(R.string.profile_name),
                imeAction = ImeAction.Next,
                contentType = ContentType.PersonFullName,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_nameField"),
            )

            AppTextField(
                value = state.email.value,
                errorText = state.email.error?.resolve(),
                onValueChange = { onEvent(ProfileEvent.EmailChanged(it)) },
                label = stringResource(R.string.profile_email),
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(ProfileEvent.SaveClicked) },
                contentType = ContentType.EmailAddress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_emailField"),
            )

            // Enabled whether or not the form is valid: pressing it is what reveals the errors.
            // See ProfileViewModel.save.
            AppButton(
                label = stringResource(R.string.profile_save),
                onClick = { onEvent(ProfileEvent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_saveButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(ProfileStatePreviews::class) state: ProfileState,
) = ThemedScreenPreview {
    ProfileScreen(
        state = state,
    ) {}
}

/** The camera half open, which is where the permission rationale is drawn. */
@ScreenPreview
@Composable
private fun CameraPreview() = ThemedScreenPreview {
    ProfileScreen(
        state = ProfileState.PREVIEW.copy(cameraOpen = true),
    ) {}
}
