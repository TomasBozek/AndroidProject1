package com.example.androidproject1.core.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/** The two promises `AppButton`'s KDoc makes that a screenshot cannot check. */
class AppButtonTest : ComponentTest() {

    @Test
    fun `a tap reaches the caller`() {
        var taps = 0
        themed { AppButton(label = "Pay", onClick = { taps++ }, modifier = Modifier.testTag(TAG)) }

        compose.onNodeWithTag(TAG).performClick()

        assertEquals(1, taps)
    }

    @Test
    fun `a disabled button emits nothing`() {
        var taps = 0
        themed {
            AppButton(label = "Pay", onClick = { taps++ }, enabled = false, modifier = Modifier.testTag(TAG))
        }

        compose.onNodeWithTag(TAG).assertIsNotEnabled()
        compose.onNodeWithTag(TAG).performClick()

        assertEquals(0, taps)
    }

    @Test
    fun `a loading button emits nothing and keeps its label`() {
        var taps = 0
        themed {
            AppButton(label = "Pay", onClick = { taps++ }, loading = true, modifier = Modifier.testTag(TAG))
        }

        compose.onNodeWithTag(TAG).performClick()

        assertEquals(0, taps)
        compose.onNodeWithTag(TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * The one a person notices: a button that grows when it starts working moves out from under
     * the finger that is already on its way down to press it again.
     */
    @Test
    fun `loading keeps the width`() {
        var loading by mutableStateOf(false)
        themed {
            AppButton(label = "Pay", onClick = {}, loading = loading, modifier = Modifier.testTag(TAG))
        }
        val resting = with(compose.onNodeWithTag(TAG).getUnclippedBoundsInRoot()) { right - left }

        loading = true
        compose.waitForIdle()

        val working = with(compose.onNodeWithTag(TAG).getUnclippedBoundsInRoot()) { right - left }
        assertEquals(resting.value.toDouble(), working.value.toDouble(), 0.5)
    }
}
