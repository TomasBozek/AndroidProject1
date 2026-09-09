package com.example.androidproject1.feature.profile.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.content.FileProvider
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppAvatarPhoto
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.permission.PermissionGate
import com.example.androidproject1.core.ui.permission.PermissionRationale
import com.example.androidproject1.core.ui.permission.PermissionRequest
import com.example.androidproject1.core.ui.permission.PermissionStatus
import com.example.androidproject1.core.ui.permission.permissionRationale
import com.example.androidproject1.core.ui.text.resolve
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.theme.AppTheme
import java.io.File

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

            AppTextField(
                value = state.name.value,
                errorText = state.name.error?.resolve(),
                onValueChange = { onEvent(ProfileEvent.NameChanged(it)) },
                label = stringResource(R.string.profile_name),
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

/**
 * The two ways to get a picture, in the order they cost the user something.
 *
 * The Photo Picker asks for no permission at all — the system shows the gallery, the user chooses
 * one picture, and this app is handed that one and nothing else. The camera needs a permission,
 * so it is the second button and it opens the gated section below rather than the camera itself.
 */
@Composable
private fun PictureButtons(onEvent: (ProfileEvent) -> Unit, modifier: Modifier = Modifier) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { onEvent(ProfileEvent.AvatarPicked(it.toString())) } }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        AppButton(
            label = stringResource(R.string.profile_choose_photo),
            onClick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            kind = ButtonKind.Outline,
            modifier = Modifier
                .weight(1f)
                .testTag("profile_choosePhotoButton"),
        )

        AppButton(
            label = stringResource(R.string.profile_take_photo),
            onClick = { onEvent(ProfileEvent.TakePhotoClicked) },
            kind = ButtonKind.Outline,
            modifier = Modifier
                .weight(1f)
                .testTag("profile_takePhotoButton"),
        )
    }
}

/**
 * Everything that needs the camera, and nothing that does not.
 *
 * The gate's value is the type: `content` is not composed unless the permission is held, so there
 * is no ungranted branch for this screen to forget. The Photo Picker above is outside it and keeps
 * working however this ends — which is the point of offering both.
 */
@Composable
private fun CameraSection(onEvent: (ProfileEvent) -> Unit, modifier: Modifier = Modifier) {
    val rationale = permissionRationale(
        title = R.string.profile_camera_rationale_title.toUiText(),
        message = R.string.profile_camera_rationale_message.toUiText(),
    )

    PermissionGate(
        Manifest.permission.CAMERA,
        rationale = rationale,
        modifier = modifier,
        // The default rationale is drawn from Material directly, because `:service:core:ui` cannot
        // depend on this app's design system. A feature can do better, so it does.
        denied = { request ->
            CameraRationale(
                rationale = rationale,
                request = request,
                onEvent = onEvent,
                modifier = modifier,
            )
        },
    ) {
        CaptureButton(
            onCaptured = { onEvent(ProfileEvent.AvatarPicked(it)) },
            modifier = modifier,
        )
    }
}

@Composable
private fun CameraRationale(
    rationale: PermissionRationale,
    request: PermissionRequest,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = request.status
    val canAskAgain = status !is PermissionStatus.Denied || status.canAskAgain

    AppCard(modifier = modifier.testTag("profile_cameraRationaleTile")) {
        AppText(text = rationale.title.resolve(), role = TextRole.Title)
        AppText(text = rationale.message.resolve(), role = TextRole.Secondary)

        // Once the user has denied permanently the system dialog never appears again, so "Allow"
        // would be a button that does nothing. That is the whole reason PermissionStatus has four
        // cases rather than being a boolean.
        AppButton(
            label = (if (canAskAgain) rationale.actionLabel else rationale.settingsLabel).resolve(),
            onClick = {
                if (canAskAgain) request.request() else onEvent(ProfileEvent.OpenAppSettingsClicked)
            },
            kind = ButtonKind.Outline,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_cameraPermissionButton"),
        )
    }
}

@Composable
private fun CaptureButton(onCaptured: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Saveable, not remembered: the camera is a different app, and this one is a prime candidate
    // for being killed while it is in front. Without this the picture comes back to a screen that
    // has forgotten where it asked for it to be put.
    var target by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(contract = CaptureToUri) { saved ->
        target?.takeIf { saved }?.let(onCaptured)
    }

    AppButton(
        label = stringResource(R.string.profile_capture),
        onClick = {
            val uri = context.newCaptureUri().toString()
            target = uri
            camera.launch(Uri.parse(uri))
        },
        modifier = modifier.testTag("profile_captureButton"),
    )
}

/**
 * `TakePicture`, plus the grant the camera app needs for the file it is handed.
 *
 * The stock contract puts the URI in `EXTRA_OUTPUT` and flags nothing, so another app cannot open
 * it — and the symptom is a zero-byte file rather than an error, which is the kind of thing that
 * reaches a release. `createIntent` is open for exactly this.
 */
private object CaptureToUri : ActivityResultContracts.TakePicture() {

    override fun createIntent(context: Context, input: Uri): Intent =
        super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

/**
 * A file in this app's cache that the camera may write into.
 *
 * A camera app cannot be handed a `File`; it needs a `content://` URI it has been granted access
 * to, which is what the `FileProvider` in this module's `AndroidManifest.xml` publishes. The cache
 * is the right home for it — what is worth keeping is the copy the data layer makes.
 */
private fun Context.newCaptureUri(): Uri {
    val directory = File(cacheDir, CAPTURE_DIRECTORY).apply { mkdirs() }
    val file = File(directory, "capture-${System.currentTimeMillis()}.jpg")
    // Matches the authority in AndroidManifest.xml, where it is written as ${applicationId} — so
    // the three flavors, whose ids differ by a suffix, each get their own.
    return FileProvider.getUriForFile(this, "$packageName.profile.captures", file)
}

private const val CAPTURE_DIRECTORY = "captures"

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
