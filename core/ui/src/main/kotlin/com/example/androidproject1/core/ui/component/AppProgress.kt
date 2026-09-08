package com.example.androidproject1.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Determinate progress — use it whenever the share is actually known.
 *
 * Past ten seconds [label] stops being optional: a bar with no words does not say what is being
 * waited for, only that something is.
 */
@Composable
fun AppProgress(
    fraction: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(AppTheme.motion.toggleMillis, easing = AppTheme.motion.toggleEasing),
        label = "progress",
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) AppText(text = label, role = TextRole.Label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(AppTheme.shapes.pill)
                .background(AppTheme.colors.surfaceSunken),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(6.dp)
                    .clip(AppTheme.shapes.pill)
                    .background(AppTheme.colors.confirm.bg),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppProgress(fraction = 0.35f)
    AppProgress(fraction = 0.8f, label = "Uploading receipts")
}
