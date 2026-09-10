package com.example.androidproject1.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Renders a full screen in the three shapes that differ.
 *
 * The large-font variant is not decoration: the system scale goes to 200 %, and a layout that only
 * works at 100 % is a layout that breaks for a large share of the people using it. Catching it in
 * the preview is cheaper than catching it in a report.
 *
 * **Three, not five.** A narrow phone (360 dp against the pixel_7's 411) and an 800 dp tablet were
 * previewed too, and their goldens were the phone's again: no screen here has an adaptive layout,
 * so those two variants were 140 images asserting a third and fourth time what the phone already
 * said. A screen that *does* respond to width should carry its own extra `@Preview` naming the
 * width it responds at — which says something, where a blanket tablet variant on every screen
 * said nothing.
 */
@Preview(name = "Phone", showBackground = true, device = "id:pixel_7")
@Preview(name = "Dark", showBackground = true, uiMode = 0x21)
@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
annotation class ScreenPreview

/**
 * Renders a single component light and dark.
 *
 * No large-font variant, for [ScreenPreview]'s reason in reverse: the gallery is a screen, so its
 * `@ScreenPreview` already renders every component at 1.5×. A third image per component here would
 * say what one already says.
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = 0x21)
annotation class ComponentPreview

@Composable
fun ThemedScreenPreview(content: @Composable () -> Unit) {
    AppTheme {
        Surface { content() }
    }
}

@Composable
fun ThemedComponentPreview(content: @Composable ColumnScope.() -> Unit) {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}
