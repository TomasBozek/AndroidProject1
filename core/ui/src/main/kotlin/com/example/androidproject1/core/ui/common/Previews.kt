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
import com.example.androidproject1.core.ui.theme.SizeClass

/**
 * Renders a full screen across a representative set of device sizes.
 *
 * The large-font variant is not decoration: the system scale goes to 200 %, and a layout that only
 * works at 100 % is a layout that breaks for a large share of the people using it. Catching it in
 * the preview is cheaper than catching it in a report.
 *
 * The tablet variant is 800 dp wide, which is the [SizeClass.Regular] side of the 720 dp
 * breakpoint: regular typography and a 56 dp touch target. Components are not previewed at that
 * width one by one — the gallery is a screen, so `@ScreenPreview` renders all of them there.
 */
@Preview(name = "Phone", showBackground = true, device = "id:pixel_7")
@Preview(name = "Narrow phone", showBackground = true, widthDp = 360, heightDp = 740)
@Preview(name = "Tablet", showBackground = true, device = "spec:width=800dp,height=1280dp,dpi=240")
@Preview(name = "Dark", showBackground = true, uiMode = 0x21)
@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
annotation class ScreenPreview

/** Renders a single component: light, dark, and at the font scale that breaks layouts. */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = 0x21)
@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
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
