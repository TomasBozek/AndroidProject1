package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The select is an anchor plus an [AppMenu], so the thing worth asserting is the round trip: the
 * options are not on screen until it is opened, and choosing one both reports the index and puts
 * them away again.
 *
 * The options open in a popup, which is a window of its own and therefore no descendant of the
 * tagged group. So the anchor is reached through the tag and an option through its position in
 * the popup — the menu being open or shut is `isPopup()`, not a string that happens to be visible.
 */
class AppSelectTest : ComponentTest() {

    private val options = listOf("Cash", "Card", "Voucher")

    @Test
    fun `the selected option is what the anchor shows`() {
        themed {
            AppSelect(
                options = options,
                selectedIndex = 1,
                onSelect = {},
                modifier = Modifier.testTag(TAG),
            )
        }

        anchor().assertTextEquals("Card")
        compose.onAllNodes(isPopup()).assertCountEquals(0)
    }

    @Test
    fun `the placeholder stands in when nothing is selected`() {
        themed {
            AppSelect(
                options = options,
                selectedIndex = -1,
                onSelect = {},
                placeholder = "Choose a price list",
                modifier = Modifier.testTag(TAG),
            )
        }

        anchor().assertTextEquals("Choose a price list")
    }

    @Test
    fun `choosing an option emits its index and closes the menu`() {
        var selected: Int? = null
        themed {
            AppSelect(
                options = options,
                selectedIndex = 0,
                onSelect = { selected = it },
                modifier = Modifier.testTag(TAG),
            )
        }

        anchor().performClick()
        option(2).assertTextEquals("Voucher")
        option(2).performClick()

        assertEquals(2, selected)
        compose.onAllNodes(isPopup()).assertCountEquals(0)
    }

    @Test
    fun `a disabled select does not open`() {
        themed {
            AppSelect(
                options = options,
                selectedIndex = 0,
                onSelect = {},
                enabled = false,
                modifier = Modifier.testTag(TAG),
            )
        }

        // A disabled anchor carries no click action at all, so the tap is aimed at the group the
        // caller tagged — which is where a finger would land — rather than at an action that is
        // not there to be performed.
        compose.onNodeWithTag(TAG).performClick()

        compose.onAllNodes(isPopup()).assertCountEquals(0)
    }

    /** The one clickable node inside the tagged group; the options are not in it. */
    private fun anchor(): SemanticsNodeInteraction =
        compose.onNode(hasClickAction() and hasAnyAncestor(hasTestTag(TAG)))

    private fun option(index: Int): SemanticsNodeInteraction =
        compose.onAllNodes(hasClickAction() and hasAnyAncestor(isPopup()))[index]
}
