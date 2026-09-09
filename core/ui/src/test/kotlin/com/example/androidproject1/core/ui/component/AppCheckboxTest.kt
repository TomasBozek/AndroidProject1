package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Three states, two of which a boolean cannot hold.
 *
 * [CheckState.Indeterminate] is the group toggle's state, and the thing to get right is what a tap
 * on it means: "some of these are on" clears the whole group rather than completing it, because a
 * person who taps a partly-filled box is undoing a selection they can see.
 *
 * The caller's tag goes on the group — box, label and the error line under them — as it does on
 * [AppTextField], so the toggle itself is reached inside it.
 */
class AppCheckboxTest : ComponentTest() {

    @Test
    fun `an off box turns on`() {
        var value: Boolean? = null
        themed {
            AppCheckbox(
                checked = CheckState.Off,
                onCheckedChange = { value = it },
                label = "Print receipt",
                modifier = Modifier.testTag(TAG),
            )
        }

        control().assertIsOff()
        control().performClick()

        assertEquals(true, value)
    }

    @Test
    fun `an indeterminate box reads as on and a tap clears it`() {
        var value: Boolean? = null
        themed {
            AppCheckbox(
                checked = CheckState.Indeterminate,
                onCheckedChange = { value = it },
                label = "All items",
                modifier = Modifier.testTag(TAG),
            )
        }

        control().assertIsOn()
        control().performClick()

        assertEquals(false, value)
    }

    @Test
    fun `a disabled box emits nothing`() {
        var changes = 0
        themed {
            AppCheckbox(
                checked = CheckState.Off,
                onCheckedChange = { changes++ },
                label = "Unavailable",
                enabled = false,
                modifier = Modifier.testTag(TAG),
            )
        }

        control().assertIsNotEnabled()
        control().performClick()

        assertEquals(0, changes)
    }

    /**
     * The same contract `AppTextField` has: red on its own is invisible to a good share of the
     * people using it, so a checkbox that is refusing says why.
     */
    @Test
    fun `an error carries its text under the row`() {
        themed {
            AppCheckbox(
                checked = CheckState.Off,
                onCheckedChange = {},
                label = "Accept the terms",
                errorText = "This has to be ticked first",
                modifier = Modifier.testTag(TAG),
            )
        }

        // The group does not merge its children, so the message is the node under it that
        // carries the words — the same shape `AppTextFieldTest` asserts an error with.
        compose.onNode(hasText("This has to be ticked first") and hasAnyAncestor(hasTestTag(TAG)))
            .assertIsDisplayed()
    }

    /** The toggle inside the group the caller tagged. */
    private fun control(): SemanticsNodeInteraction =
        compose.onNode(isToggleable() and hasAnyAncestor(hasTestTag(TAG)))
}
