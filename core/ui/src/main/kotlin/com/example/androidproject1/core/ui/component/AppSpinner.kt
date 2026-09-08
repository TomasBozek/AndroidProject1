package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Waiting, for less than two seconds.
 *
 * Anything longer needs [AppSkeleton] — a spinner that runs for five seconds reads as a hang, and
 * says nothing about what is coming.
 */
@Composable
fun AppSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AppTheme.colors.confirm.bg,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 2.dp,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSpinner()
    AppSpinner(size = 40.dp)
}
