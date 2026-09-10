package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The field's contract is that an error is never colour alone — red on its own is invisible to a
 * good share of the people using it, and the API is shaped so that the error state and the message
 * are the same argument.
 */
class AppTextFieldTest : ComponentTest() {

    @Test
    fun `an error always carries text`() {
        themed {
            AppTextField(
                value = "abc",
                onValueChange = {},
                label = "Code",
                errorText = "No product with this code",
                modifier = Modifier.testTag(TAG),
            )
        }

        compose.onNode(hasText("No product with this code") and hasAnyAncestor(hasTestTag(TAG)))
            .assertIsDisplayed()
    }

    @Test
    fun `an error replaces the helper text rather than sitting beside it`() {
        themed {
            AppTextField(
                value = "abc",
                onValueChange = {},
                helperText = "From the catalogue",
                errorText = "No product with this code",
                modifier = Modifier.testTag(TAG),
            )
        }

        compose.onNodeWithText("No product with this code").assertIsDisplayed()
        compose.onAllNodesWithText("From the catalogue").assertCountEquals(0)
    }

    @Test
    fun `typing reaches the caller`() {
        var typed = ""
        themed {
            AppTextField(value = "", onValueChange = { typed = it }, modifier = Modifier.testTag(TAG))
        }

        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(TAG))).performTextInput("12")

        assertEquals("12", typed)
    }

    @Test
    fun `a disabled field takes no input`() {
        themed {
            AppTextField(
                value = "Locked",
                onValueChange = {},
                enabled = false,
                modifier = Modifier.testTag(TAG),
            )
        }

        // A disabled field carries no text-input action at all — there is nothing to type into,
        // rather than something that refuses what is typed.
        compose.onAllNodes(hasSetTextAction() and hasAnyAncestor(hasTestTag(TAG))).assertCountEquals(0)
    }

    @Test
    fun `the label is on the input, not only beside it`() {
        themed {
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Email",
                modifier = Modifier.testTag(TAG),
            )
        }

        // The node that takes text is the one a screen reader lands on, so the name has to be
        // there — a sibling Text above it is a different node and announces nothing.
        compose.onNode(
            hasSetTextAction() and hasAnyAncestor(hasTestTag(TAG)) and hasText("Email"),
        ).assertIsDisplayed()
    }
}
