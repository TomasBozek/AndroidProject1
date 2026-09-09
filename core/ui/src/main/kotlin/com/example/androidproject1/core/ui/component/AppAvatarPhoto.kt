package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Someone's picture, falling back to [AppAvatar]'s initials while there is none.
 *
 * The pair is one component rather than an `if` in every screen that shows a person: "photo if we
 * have one, initials otherwise" is the rule, and a screen that writes it itself is a screen that
 * has to be found again when the fallback changes. It is also the one place that decides how large
 * a person is drawn, so a feature never writes a dimension for it.
 *
 * @param photo anything [AppImage] can load — a `file://` string, a URL, a resource. Null draws
 * the initials.
 */
@Composable
fun AppAvatarPhoto(
    name: String,
    modifier: Modifier = Modifier,
    photo: Any? = null,
    contentDescription: String? = null,
    size: Dp = 96.dp,
) {
    if (photo == null) {
        AppAvatar(name = name, modifier = modifier, size = size)
    } else {
        AppImage(
            model = photo,
            contentDescription = contentDescription,
            shape = AppTheme.shapes.pill,
            modifier = modifier.size(size),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // No network in a preview, so the loaded state is the one that cannot be shown here; the two
    // that can are the fallback and the failure AppImage draws in its place.
    AppAvatarPhoto(name = "Jana Nováková")
    AppAvatarPhoto(name = "Jana Nováková", photo = "https://example.invalid/avatar.jpg")
}
