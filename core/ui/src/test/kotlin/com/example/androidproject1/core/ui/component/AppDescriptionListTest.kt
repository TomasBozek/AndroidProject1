package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.height
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.GraphicsMode

/**
 * F4X1: at a large font scale the label used to get whatever the value left over, and "Application
 * id" came out one letter per line. The value is the column that wraps, never the label.
 *
 * The list is composed in a row too narrow for both texts, beside each text composed alone at the
 * same width as its reference. The label and the value are the first two children of the tagged
 * column — the row between them has no semantics of its own — which is how a part a caller cannot
 * tag is reached (see [ComponentTest]).
 */
// Native graphics, or text is measured a pixel per glyph and nothing here can wrap. The screenshot
// tests set the same; the other component tests assert taps and never a width.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppDescriptionListTest : ComponentTest() {

    @Test
    fun `a long value wraps and the label keeps its shape`() {
        themed {
            // The golden that showed the bug is the 1.5× font-scale one: five row heights at that
            // scale is a narrow phone, and "Application id" beside the application id is wider.
            val density = LocalDensity.current
            val large = Density(density = density.density, fontScale = LARGE_FONT_SCALE)
            val narrow = AppTheme.density.listRowHeight * 5
            CompositionLocalProvider(LocalDensity provides large) {
                // Scrollable, so every height below is the text's own and not what the window
                // had left for it.
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.width(narrow)) {
                        AppDescriptionList(
                            rows = listOf(DescriptionRow(LABEL, VALUE)),
                            modifier = Modifier.testTag(TAG),
                        )
                    }
                    Box(modifier = Modifier.width(narrow)) {
                        AppText(text = LABEL, role = TextRole.Secondary, modifier = Modifier.testTag(LABEL_ALONE))
                    }
                    // Unconstrained, so the reference is the value on one line.
                    AppText(text = VALUE, modifier = Modifier.testTag(VALUE_ALONE))
                }
            }
        }

        val cells = compose.onNodeWithTag(TAG, useUnmergedTree = true).onChildren()
        val label = cells[0].getUnclippedBoundsInRoot()
        val value = cells[1].getUnclippedBoundsInRoot()
        val labelAlone = compose.onNodeWithTag(LABEL_ALONE).getUnclippedBoundsInRoot()
        val valueAlone = compose.onNodeWithTag(VALUE_ALONE).getUnclippedBoundsInRoot()

        // Two lines at most — "Application" over "id" — never a letter per line. Fewer than three
        // rather than at most two, because two lines are a dp taller than twice one.
        assertTrue(
            "label is ${label.height} tall, alone it is ${labelAlone.height}",
            label.height < labelAlone.height * 3,
        )
        // The value is the one that gave: it stands taller than it does with the row to itself.
        assertTrue(
            "value is ${value.height} tall, alone it is ${valueAlone.height}",
            value.height > valueAlone.height,
        )
    }

    private companion object {

        /** The scale the `Large_font` previews record at. */
        const val LARGE_FONT_SCALE = 1.5f
        const val LABEL = "Application id"
        const val VALUE = "com.example.androidproject1.dev"
        const val LABEL_ALONE = "component_labelAlone"
        const val VALUE_ALONE = "component_valueAlone"
    }
}
