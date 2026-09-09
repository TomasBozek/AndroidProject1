package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A dialog draws in its own window, which is exactly why it is worth a test: a preview cannot show
 * it, and everything inside it is out of reach of the screen's own tags until something asserts
 * that it is there.
 *
 * `AppConfirmDialog` draws its own two buttons, so a caller has no tag to put on them; they are
 * reached by position in the dialog window — dismiss first, confirm second, which is the order the
 * component composes them in and the order a screen reader reads them.
 */
class AppDialogTest : ComponentTest() {

    private val dismissAction get() = action(0)

    private val confirmAction get() = action(1)

    @Test
    fun `the title, the message and the actions all reach the window`() {
        themed {
            AppDialog(
                title = "Void this order?",
                message = "The items go back to stock.",
                onDismiss = {},
                actions = {
                    AppButton(
                        label = "Void order",
                        onClick = {},
                        kind = ButtonKind.Destructive,
                        modifier = Modifier.testTag(TAG),
                    )
                },
            )
        }

        // Text here is the assertion, not the way in: what a dialog is for is the words in it.
        compose.onNode(hasText("Void this order?") and hasAnyAncestor(isDialog())).assertIsDisplayed()
        compose.onNode(hasText("The items go back to stock.") and hasAnyAncestor(isDialog()))
            .assertIsDisplayed()
        compose.onNodeWithTag(TAG).assertIsDisplayed()
    }

    @Test
    fun `an action reaches its caller from inside the dialog window`() {
        var confirmed = 0
        themed {
            AppDialog(
                title = "Void this order?",
                onDismiss = {},
                actions = {
                    AppButton(
                        label = "Void order",
                        onClick = { confirmed++ },
                        kind = ButtonKind.Destructive,
                        modifier = Modifier.testTag(TAG),
                    )
                },
            )
        }

        compose.onNodeWithTag(TAG).performClick()

        assertEquals(1, confirmed)
    }

    /**
     * The one that would go unnoticed: the two callbacks wired to the wrong buttons still gives a
     * dialog that dismisses and confirms, so each is checked against the label it carries.
     */
    @Test
    fun `a confirm dialog gives the destructive verb to confirm and cancel to dismiss`() {
        var confirmed = 0
        var dismissed = 0
        themed {
            AppConfirmDialog(
                title = "Void this order?",
                confirmLabel = "Void order",
                onConfirm = { confirmed++ },
                onDismiss = { dismissed++ },
            )
        }

        confirmAction.assertTextEquals("Void order")
        confirmAction.performClick()
        assertEquals(1, confirmed)

        dismissAction.assertTextEquals("Cancel")
        dismissAction.performClick()
        assertEquals(1, dismissed)
    }

    private fun action(index: Int): SemanticsNodeInteraction =
        compose.onAllNodes(hasClickAction() and hasAnyAncestor(isDialog()))[index]
}
