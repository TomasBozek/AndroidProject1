package com.example.androidproject1.feature.profile.presentation.profile

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.service.core.ui.form.FieldState
import com.example.androidproject1.service.core.ui.form.Form
import com.example.androidproject1.service.core.ui.form.email
import com.example.androidproject1.service.core.ui.form.minLength
import com.example.androidproject1.service.core.ui.form.required
import com.example.androidproject1.service.core.ui.text.UiText

/**
 * @property cameraOpen whether the camera half of the picture section is showing. Note what this
 * is *not*: whether the camera permission is held. That lives outside the app and changes while
 * the app is backgrounded, so it is read in composition by `PermissionGate` and never kept here.
 */
@Immutable
data class ProfileState(
    val name: FieldState = nameField(),
    val email: FieldState = emailField(),
    val avatarUri: String? = null,
    val cameraOpen: Boolean = false,
) {

    /** Derived, so "is submit allowed" and "why is it not" cannot disagree. */
    val canSubmit: Boolean get() = Form.canSubmit(name, email)

    /** The first rule broken, for the one line that says what to fix. */
    val firstError: UiText? get() = Form.firstError(name, email)

    companion object {

        val PREVIEW = ProfileState(
            name = nameField("Jana Nováková"),
            email = emailField("jana@example.com"),
        )

        /** A first run: nothing typed, so nothing is wrong yet and nothing is shouted about. */
        val EMPTY = ProfileState()

        /** What the screen looks like after Save was pressed on a form that is not valid. */
        val PREVIEW_INVALID = ProfileState(
            name = nameField("J").touch(),
            email = emailField("jana@").touch(),
        )

        fun of(profile: Profile) = ProfileState(
            name = nameField(profile.name),
            email = emailField(profile.email),
            avatarUri = profile.avatarUri,
        )
    }
}

/**
 * The states this screen is drawn in — for the preview, and so for `ui.2`'s goldens.
 *
 * Three, because one preview only ever shows the state you were thinking about. Empty is where
 * layouts collapse; the invalid one is where the error lines appear and push everything down.
 */
class ProfileStatePreviews : PreviewParameterProvider<ProfileState> {

    override val values = sequenceOf(
        ProfileState.PREVIEW,
        ProfileState.EMPTY,
        ProfileState.PREVIEW_INVALID,
    )
}

/**
 * The rules, spelled once. A field carries its own validators, so the screen that renders it and
 * the ViewModel that reads it are looking at the same ones.
 */
private fun nameField(value: String = "") =
    FieldState.of(required(), minLength(MIN_NAME_LENGTH), value = value)

private fun emailField(value: String = "") = FieldState.of(required(), email(), value = value)

private const val MIN_NAME_LENGTH = 2
