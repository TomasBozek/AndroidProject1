package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A hairline where a gap is not enough.
 *
 * Decorative, so it is allowed to be quiet — it draws `colors.border`, not `borderStrong`. Use it
 * sparingly: a divider between every row of a list turns the list into a table.
 */
@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.border),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppText(text = "Above")
    AppDivider()
    AppText(text = "Below")
}
