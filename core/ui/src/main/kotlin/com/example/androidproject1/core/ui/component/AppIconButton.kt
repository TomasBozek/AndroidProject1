package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * What an icon button does, in the same words [AppButton] uses.
 *
 * [Plain] is the default and carries no fill — it is the one that belongs in a top bar, where a
 * filled circle would compete with the title. The other two are for an icon that is the action
 * itself, with no label beside it to say which.
 */
/** [Inverse] is [Plain] on a dark wash — the actions on an [AppToolbar]. */
enum class IconButtonKind { Plain, Confirm, Destructive, Inverse }

/**
 * An icon that does something.
 *
 * [contentDescription] is not optional: an icon with no label has to say what it is, to a screen
 * reader and to a test, and it is the same string for both. [size] moves the target, which is what
 * a finger has to find; the glyph inside it stays 24 dp.
 *
 * [square] is for an icon button in a row of them — a toolbar, a keypad — where a run of circles
 * reads as a row of holes. On its own an icon button is a pill.
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    kind: IconButtonKind = IconButtonKind.Plain,
    size: ControlSize = ControlSize.Medium,
    square: Boolean = false,
) {
    val colors = AppTheme.colors
    val family = when (kind) {
        IconButtonKind.Plain, IconButtonKind.Inverse -> null
        IconButtonKind.Confirm -> colors.confirm
        IconButtonKind.Destructive -> colors.destructive
    }
    val shape = if (square) AppTheme.shapes.md else AppTheme.shapes.pill
    val tint = when {
        !enabled -> colors.textDisabled
        family != null -> family.label
        kind == IconButtonKind.Inverse -> colors.textOnInverse
        else -> colors.textPrimary
    }
    Box(
        modifier = modifier
            .size(size.height)
            .clip(shape)
            .then(
                // A disabled filled button keeps its shape and loses its colour, the same way a
                // disabled `AppButton` flattens rather than only fading its label.
                if (family != null) {
                    Modifier.background(if (enabled) family.bg else colors.surfaceSunken)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(AppTheme.icons.md),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = {},
    )
    AppIconButton(
        icon = Icons.Filled.Done,
        contentDescription = "Confirm",
        onClick = {},
        kind = IconButtonKind.Confirm,
    )
    AppIconButton(
        icon = Icons.Filled.Delete,
        contentDescription = "Delete",
        onClick = {},
        kind = IconButtonKind.Destructive,
        square = true,
    )
    AppIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back, small",
        onClick = {},
        size = ControlSize.Small,
    )
    AppIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back, large",
        onClick = {},
        size = ControlSize.Large,
    )
    AppIconButton(
        icon = Icons.Filled.Done,
        contentDescription = "Unavailable",
        onClick = {},
        kind = IconButtonKind.Confirm,
        enabled = false,
    )
}
