package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Like a dialog, a sheet is its own window, so this is the assertion that its content is reachable
 * at all — the failure the log-out flow found on a device was a dialog whose contents nothing
 * driving the screen could see.
 */
class AppSheetTest : ComponentTest() {

    @Test
    fun `the title and the content reach the sheet's window`() {
        themed {
            AppSheet(onDismiss = {}, title = "Payment method") {
                AppButton(label = "Cash", onClick = {}, modifier = Modifier.testTag(TAG))
            }
        }

        compose.onNodeWithText("Payment method").assertIsDisplayed()
        compose.onNodeWithTag(TAG).assertIsDisplayed()
    }

    @Test
    fun `content inside the sheet reaches its caller`() {
        var taps = 0
        themed {
            AppSheet(onDismiss = {}) {
                AppButton(label = "Cash", onClick = { taps++ }, modifier = Modifier.testTag(TAG))
            }
        }

        compose.onNodeWithTag(TAG).performClick()

        assertEquals(1, taps)
    }
}
