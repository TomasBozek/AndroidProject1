package com.example.androidproject1.feature.template.presentation.templateargs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The same shape as `TemplateScreenTest`, for the screen that takes a route argument.
 *
 * What it adds is the assertion that the argument reached the screen at all — the failure mode
 * this variant exists to prevent is a screen that loads from a `LaunchedEffect` instead of from
 * the key it was handed.
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class TemplateArgsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TemplateArgsEvent>()

    private fun render(state: TemplateArgsState) {
        compose.setContent {
            AppTheme {
                TemplateArgsScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the argument it was handed`() {
        render(TemplateArgsState.PREVIEW)

        compose.onNodeWithTag("templateArgs_idValue").assertIsDisplayed()
    }

    @Test
    fun `renders an empty argument without collapsing`() {
        render(TemplateArgsState.PREVIEW.copy(templateId = ""))

        compose.onNodeWithTag("templateArgs_idValue").assertIsDisplayed()
    }
}
