package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Which of the three grounds a panel sits on.
 *
 * Three and no more, so a card does not stack on a card. The fourth level is a dialog, and that is
 * a different component.
 */
enum class SurfaceLevel { Sunken, Base, Raised }

/**
 * A surface for grouped content.
 *
 * [Raised][SurfaceLevel.Raised] is the default and the one to reach for: content lifted off the
 * screen's own ground. [Sunken][SurfaceLevel.Sunken] is the well a form or a read-only block sits
 * in — the same ground [AppTextField] uses, which is what makes an inert panel read as inert.
 * [Base][SurfaceLevel.Base] is a card that only groups: it takes the padding and the corner and
 * leaves the colour alone, for a card on a ground that is already right.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    level: SurfaceLevel = SurfaceLevel.Raised,
    content: @Composable ColumnScope.() -> Unit,
) {
    val background = when (level) {
        SurfaceLevel.Sunken -> AppTheme.colors.surfaceSunken
        SurfaceLevel.Base -> AppTheme.colors.surfaceBase
        SurfaceLevel.Raised -> AppTheme.colors.surfaceRaised
    }
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.xl)
            .background(background)
            .padding(AppTheme.spacing.inset.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppCard {
        AppText(text = "Notifications", role = TextRole.Title)
        AppText(text = "Turn these on to hear about orders while the app is closed.", role = TextRole.Secondary)
    }
    AppCard(level = SurfaceLevel.Sunken) {
        AppText(text = "Archived trip", role = TextRole.Title)
        AppText(text = "Read only until it is restored.", role = TextRole.Secondary)
    }
    AppCard(level = SurfaceLevel.Base) {
        AppText(text = "Grouped, on the ground it is already on", role = TextRole.Secondary)
    }
}
