package com.example.androidproject1.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The brand face is bundled and actually loads.
 *
 * A preview that silently falls back to the platform face looks fine and is wrong, and the failure
 * only shows up when someone compares a screenshot to a design. Rendering under Robolectric with
 * no network is the same situation a preview and a golden are in.
 */
@RunWith(RobolectricTestRunner::class)
class AppFontFamilyTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `the app does not fall back to the platform face`() {
        assertNotEquals(FontFamily.Default, AppFontFamily)
    }

    @Test
    fun `text renders with the bundled face and no network`() {
        compose.setContent {
            AppTheme {
                Text(
                    text = "Sphinx of black quartz",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("sample"),
                )
            }
        }

        // Resolving a missing or malformed font resource throws while composing, so reaching this
        // assertion is the check.
        compose.onNodeWithTag("sample").assertIsDisplayed()
    }
}
