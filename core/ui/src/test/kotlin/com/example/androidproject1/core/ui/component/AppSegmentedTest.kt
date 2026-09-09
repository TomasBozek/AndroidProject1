package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Two to four options that switch immediately. Selection is state the caller owns, so the control
 * reports which one is selected and emits the index of the one that was pressed — it never moves
 * the selection itself.
 *
 * The options carry no tag of their own — the caller tags the group — so each is reached by its
 * position within it, which is also the order a screen reader announces them in.
 */
class AppSegmentedTest : ComponentTest() {

    private val options = listOf("Today", "Week", "Month")

    @Test
    fun `the selected option is marked and the others are not`() {
        themed {
            AppSegmented(
                options = options,
                selectedIndex = 0,
                onSelect = {},
                modifier = Modifier.testTag(TAG),
            )
        }

        option(0).assertIsSelected()
        option(0).assertTextEquals("Today")
        option(1).assertIsNotSelected()
    }

    @Test
    fun `a tap emits the index of the option pressed`() {
        var selected: Int? = null
        themed {
            AppSegmented(
                options = options,
                selectedIndex = 0,
                onSelect = { selected = it },
                modifier = Modifier.testTag(TAG),
            )
        }

        option(2).performClick()

        assertEquals(2, selected)
    }

    private fun option(index: Int): SemanticsNodeInteraction =
        compose.onAllNodes(isSelectable() and hasAnyAncestor(hasTestTag(TAG)))[index]
}
