package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The floor and the ceiling, which are the two values a stepper is ever wrong about.
 *
 * The two keys carry no tag of their own — the caller tags the group — so they are reached by
 * position within it, which is also the order a screen reader announces them in.
 */
class AppStepperTest : ComponentTest() {

    @Test
    fun `a tap moves the value by one`() {
        var value: Int? = null
        themed { AppStepper(value = 4, onValueChange = { value = it }, modifier = Modifier.testTag(TAG)) }

        increment().performClick()
        assertEquals(5, value)

        decrement().performClick()
        assertEquals(3, value)
    }

    @Test
    fun `the floor disables the decrement`() {
        var changes = 0
        themed {
            AppStepper(value = 0, onValueChange = { changes++ }, min = 0, modifier = Modifier.testTag(TAG))
        }

        decrement().assertIsNotEnabled()
        decrement().performClick()

        assertEquals(0, changes)
    }

    @Test
    fun `the ceiling disables the increment`() {
        var changes = 0
        themed {
            AppStepper(
                value = 9,
                onValueChange = { changes++ },
                max = 9,
                modifier = Modifier.testTag(TAG),
            )
        }

        increment().assertIsNotEnabled()
        increment().performClick()

        assertEquals(0, changes)
    }

    private fun decrement(): SemanticsNodeInteraction = key(0)

    private fun increment(): SemanticsNodeInteraction = key(1)

    private fun key(index: Int) =
        compose.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(TAG)))[index]
}
