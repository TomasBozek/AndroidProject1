package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tabs carry a count, and a count of zero draws nothing rather than a zero.
 *
 * Like [AppSegmentedTest], a tab is reached by its position inside the tagged group; the badge is
 * then read off that tab's own merged text rather than searched for across the tree.
 */
class AppTabsTest : ComponentTest() {

    @Test
    fun `a tap emits the index of the tab pressed`() {
        var selected: Int? = null
        themed {
            AppTabs(
                tabs = listOf(TabItem("Open", badge = 4), TabItem("Paid"), TabItem("Void")),
                selectedIndex = 0,
                onSelect = { selected = it },
                modifier = Modifier.testTag(TAG),
            )
        }

        tab(2).performClick()

        assertEquals(2, selected)
    }

    @Test
    fun `the selected tab reports itself as selected`() {
        themed {
            AppTabs(
                tabs = listOf(TabItem("Open"), TabItem("Paid")),
                selectedIndex = 1,
                onSelect = {},
                modifier = Modifier.testTag(TAG),
            )
        }

        tab(1).assertIsSelected()
        tab(1).assertTextContains("Paid")
    }

    @Test
    fun `a badge above zero is shown and a badge of zero is not`() {
        themed {
            AppTabs(
                tabs = listOf(TabItem("Open", badge = 4), TabItem("Paid", badge = 0)),
                selectedIndex = 0,
                onSelect = {},
                modifier = Modifier.testTag(TAG),
            )
        }

        // The tab merges its label and its badge, so what the tab says is the whole of what it
        // draws: the first carries the count, the second carries nothing but its label.
        tab(0).assertTextContains("4")
        tab(1).assertTextEquals("Paid")
    }

    private fun tab(index: Int): SemanticsNodeInteraction =
        compose.onAllNodes(isSelectable() and hasAnyAncestor(hasTestTag(TAG)))[index]
}
