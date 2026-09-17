package com.example.androidproject1.feature.settings.presentation.language

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.settings.domain.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What is on screen, and what a tap does. **Everything is found by `testTag`, never by text** —
 * this screen's labels are the one place copy is deliberately not translated the same way twice.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsLanguageScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<SettingsLanguageEvent>()

    private fun render(state: SettingsLanguageState) {
        compose.setContent {
            AppTheme {
                SettingsLanguageScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders one ring per language, the stored one filled`() {
        render(SettingsLanguageState(selected = AppLanguage.Czech))

        compose.onNodeWithTag("SettingsLanguageScreen").assertIsDisplayed()
        compose.onNodeWithTag("settingsLanguage_optionsGroup").assertIsDisplayed()
        compose.onNodeWithTag("settingsLanguage_systemItem").assertIsNotSelected()
        compose.onNodeWithTag("settingsLanguage_enItem").assertIsNotSelected()
        compose.onNodeWithTag("settingsLanguage_csItem").assertIsSelected()
    }

    @Test
    fun `tapping a ring reports the language`() {
        render(SettingsLanguageState.PREVIEW)

        compose.onNodeWithTag("settingsLanguage_csItem").performClick()

        assertEquals(listOf(SettingsLanguageEvent.LanguageSelected(AppLanguage.Czech)), events)
    }

    @Test
    fun `up reports itself`() {
        render(SettingsLanguageState.PREVIEW)

        compose.onNodeWithTag("settingsLanguage_upButton").performClick()

        assertEquals(listOf(SettingsLanguageEvent.NavigateUpClicked), events)
    }
}
