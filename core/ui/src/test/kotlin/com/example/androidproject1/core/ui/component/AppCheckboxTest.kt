package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Three states, two of which a boolean cannot hold.
 *
 * [CheckState.Indeterminate] is the group toggle's state, and the thing to get right is what a tap
 * on it means: "some of these are on" clears the whole group rather than completing it, because a
 * person who taps a partly-filled box is undoing a selection they can see.
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

        compose.onNodeWithTag(TAG).assertIsOff()
        compose.onNodeWithTag(TAG).performClick()

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

        compose.onNodeWithTag(TAG).assertIsOn()
        compose.onNodeWithTag(TAG).performClick()

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

        compose.onNodeWithTag(TAG).assertIsNotEnabled()
        compose.onNodeWithTag(TAG).performClick()

        assertEquals(0, changes)
    }
}
