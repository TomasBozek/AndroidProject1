package com.example.androidproject1.feature.profile.presentation.component

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.feature.profile.presentation.R
import com.example.androidproject1.feature.profile.presentation.profile.ProfileEvent
import com.example.androidproject1.service.core.ui.permission.PermissionGate
import com.example.androidproject1.service.core.ui.permission.permissionRationale
import com.example.androidproject1.service.core.ui.text.toUiText

/**
 * Everything that needs the camera, and nothing that does not.
 *
 * The gate's value is the type: `content` is not composed unless the permission is held, so there
 * is no ungranted branch for this screen to forget. The Photo Picker above is outside it and keeps
 * working however this ends — which is the point of offering both.
 */
@Composable
fun CameraSection(onEvent: (ProfileEvent) -> Unit, modifier: Modifier = Modifier) {
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

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    CameraSection(onEvent = {})
}
