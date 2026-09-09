package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.ActionColors
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.core.ui.theme.keySurface

/**
 * Which action this is, not what colour it should be.
 *
 * A screen never picks a colour for a button; it says what pressing it does, and the theme decides.
 * That is why re-branding does not touch a single screen.
 */
enum class ButtonKind { Confirm, Destructive, Info, Neutral, Outline, Ghost }

/**
 * How large, which also decides the radius: the shape scale distinguishes a key from a surface.
 *
 * [Small] is 40 dp and exists only where there is a mouse — on a touch screen the range starts at
 * [Medium], so the 48 dp minimum target holds.
 */
enum class ButtonSize(val height: Dp, internal val shape: Shape, val horizontalPadding: Dp) {
    Small(40.dp, RoundedCornerShape(8.dp), 12.dp),
    Medium(52.dp, RoundedCornerShape(10.dp), 16.dp),
    Large(64.dp, RoundedCornerShape(12.dp), 20.dp),
}

/**
 * The system's button: a pressed key rather than a floating card.
 *
 * The body is a hard bottom edge in the family's own colour, and pressing shortens it so the
 * button travels down — see `Modifier.keySurface`. Four solid kinds plus [ButtonKind.Outline] and
 * [ButtonKind.Ghost] for the secondary rung.
 *
 * The label never disappears while [loading] and the width does not change, so a button never
 * jumps under a finger already on its way down — the spinner is drawn over the label rather than
 * beside it, which is what holds the width. [enabled] `false` flattens the body: losing the
 * elevation is what carries "this cannot be pressed", not the text colour alone.
 */
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.Confirm,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = AppTheme.colors
    val family: ActionColors = when (kind) {
        ButtonKind.Confirm -> colors.confirm
        ButtonKind.Destructive -> colors.destructive
        ButtonKind.Info -> colors.info
        ButtonKind.Neutral, ButtonKind.Outline, ButtonKind.Ghost -> colors.neutral
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val flat = !enabled || kind == ButtonKind.Outline || kind == ButtonKind.Ghost

    val labelColor = when {
        !enabled -> colors.textDisabled
        kind == ButtonKind.Outline -> colors.textPrimary
        kind == ButtonKind.Ghost -> colors.confirm.bg
        else -> family.label
    }

    val surface = when {
        kind == ButtonKind.Ghost -> Modifier
        flat -> Modifier.border(
            BorderStroke(1.dp, if (enabled) colors.borderStrong else colors.border),
            size.shape,
        )
        else -> Modifier.keySurface(
            color = family.bg,
            edge = family.edge,
            shape = size.shape,
            pressed = pressed,
        )
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = size.height)
            .then(surface)
            .clickable(
                enabled = enabled && !loading,
                role = Role.Button,
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = size.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        // The spinner is drawn *over* the label, not beside it. Beside it the button grows by the
        // spinner and the gap at the moment it starts working, which moves it out from under a
        // finger already on its way down; here the label alone measures the button. It dims rather
        // than going away, so what the button does is still readable while it does it.
        CompositionLocalProvider(LocalContentColor provides labelColor) {
            Text(
                text = label,
                style = when (size) {
                    ButtonSize.Small -> AppTheme.typography.labelMd
                    ButtonSize.Medium -> AppTheme.typography.bodyLg
                    ButtonSize.Large -> AppTheme.typography.titleMd
                },
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(if (loading) LOADING_LABEL_ALPHA else 1f),
            )
        }
        if (loading) {
            // `matchParentSize` keeps the spinner out of the Box's own measurement, so a short
            // label cannot be widened by it either.
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = labelColor,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

/** Enough for the label to stay readable behind the spinner, little enough to read as busy. */
private const val LOADING_LABEL_ALPHA = 0.35f

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppButton(label = "Pay", onClick = {})
    AppButton(label = "Void", onClick = {}, kind = ButtonKind.Destructive)
    AppButton(label = "Card", onClick = {}, kind = ButtonKind.Info)
    AppButton(label = "Back", onClick = {}, kind = ButtonKind.Outline)
    AppButton(label = "More options", onClick = {}, kind = ButtonKind.Ghost)
    AppButton(label = "Unavailable", onClick = {}, enabled = false)
    AppButton(label = "Processing", onClick = {}, loading = true)
    AppButton(label = "Large", onClick = {}, size = ButtonSize.Large)
}
