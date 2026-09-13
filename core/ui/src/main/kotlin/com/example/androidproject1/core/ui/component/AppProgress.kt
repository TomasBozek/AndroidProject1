package com.example.androidproject1.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
 * waited for, only that something is. When the share is *not* known use [AppIndeterminateProgress],
 * and do not fake one: a bar that crawls to 90 % and waits is worse than one that never claimed a
 * number.
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

/**
 * Progress with no share to report.
 *
 * The bar sweeps rather than filling, because there is nothing to fill towards. Prefer
 * [AppSpinner] inline and this at the top of a region that is being replaced.
 */
@Composable
fun AppIndeterminateProgress(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "indeterminateProgress")
    val start by transition.animateFloat(
        initialValue = -SWEEP,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "indeterminateProgressStart",
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) AppText(text = label, role = TextRole.Label)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(AppTheme.shapes.pill)
                .background(AppTheme.colors.surfaceSunken),
        ) {
            val width = maxWidth
            Box(
                modifier = Modifier
                    .offset(x = width * start)
                    .width(width * SWEEP)
                    .height(6.dp)
                    .clip(AppTheme.shapes.pill)
                    .background(AppTheme.colors.confirm.bg),
            )
        }
    }
}

/** How much of the track the sweeping bar covers. */
private const val SWEEP = 0.35f

/**
 * Progress through a fixed number of named steps.
 *
 * A count of steps is what a person can hold — "2 of 4" is a place in a process, where 50 % is only
 * a number. Use it for a flow someone is walking through; use [AppProgress] for work being done to
 * them.
 */
@Composable
fun AppStepProgress(
    steps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) AppText(text = label, role = TextRole.Label)
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.xs)) {
            repeat(steps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(AppTheme.shapes.pill)
                        .background(
                            // Done and current read the same: what the bar says is how far along
                            // this is, not which step is being worked on — the content says that.
                            if (index <= currentStep) {
                                AppTheme.colors.confirm.bg
                            } else {
                                AppTheme.colors.surfaceSunken
                            },
                        ),
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppProgress(fraction = 0.35f)
    AppProgress(fraction = 0.8f, label = "Uploading receipts")
    AppIndeterminateProgress(label = "Checking for updates")
    AppStepProgress(steps = 4, currentStep = 1, label = "Step 2 of 4")
}
