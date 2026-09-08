package com.example.androidproject1.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The outline of content that is loading.
 *
 * It copies the shape of what is coming and it does not blink — a flashing placeholder is harder
 * to ignore than the wait it is covering.
 */
@Composable
fun AppSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: Shape = AppTheme.shapes.xs,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .alpha(alpha)
            .background(AppTheme.colors.surfaceSunken),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSkeleton()
    AppSkeleton(height = 48.dp, shape = AppTheme.shapes.lg)
}
