package com.example.androidproject1

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.auth.presentation.login.LoginDestination
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import com.example.androidproject1.feature.gallery.presentation.gallery.GalleryDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * The back-stack arithmetic every tab switch depends on.
 *
 * The tabs do not each hold a list. The stack **is** their concatenation in the order they were
 * last visited, a tab's key is the only thing that starts a segment, and everything after one
 * belongs to it. That is what makes per-tab history survive process death for nothing — it is the
 * list `rememberNavBackStack` already saves — and it is why these are plain functions on a list
 * with nothing to mock.
 */
private fun stackOf(vararg keys: NavKey) = NavBackStack(*keys)

private val product = ProductDetailDestination(productId = "coffee")

class TopLevelDestinationTest {

    @Test
    fun `every tab has a key of its own`() {
        // indexOf finds the first match, so two tabs sharing a key would make one unreachable.
        val keys = TopLevelDestination.entries.map { it.key }

        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `a key that is not a tab is not mistaken for one`() {
        assertNull(TopLevelDestination.of(product))
        assertNull(TopLevelDestination.of(LoginDestination))
    }

    // ---- currentTab -------------------------------------------------------------------------

    @Test
    fun `the auth flow has no tab`() {
        // The bottom bar is not composed there at all, so `null` is the answer AppNavHost needs.
        assertNull(stackOf(LoginDestination).currentTab)
    }

    @Test
    fun `an empty stack has no tab`() {
        assertNull(stackOf().currentTab)
    }

    @Test
    fun `the tab on screen is the last tab key on the stack`() {
        val stack = stackOf(
            TopLevelDestination.Home.key,
            TopLevelDestination.Catalog.key,
        )

        assertEquals(TopLevelDestination.Catalog, stack.currentTab)
    }

    @Test
    fun `a screen pushed onto a tab does not change which tab it is`() {
        val stack = stackOf(TopLevelDestination.Catalog.key, product)

        assertEquals(TopLevelDestination.Catalog, stack.currentTab)
    }

    @Test
    fun `a screen that is not a tab root never becomes the current tab`() {
        // GalleryDestination is reached from Settings and is not a tab of its own.
        val stack = stackOf(TopLevelDestination.Settings.key, GalleryDestination)

        assertEquals(TopLevelDestination.Settings, stack.currentTab)
    }

    // ---- selectTab --------------------------------------------------------------------------

    @Test
    fun `an unvisited tab is appended as a new segment`() {
        val stack = stackOf(TopLevelDestination.Home.key)

        stack.selectTab(TopLevelDestination.Catalog)

        assertEquals(listOf(TopLevelDestination.Home.key, TopLevelDestination.Catalog.key), stack.toList())
        assertEquals(TopLevelDestination.Catalog, stack.currentTab)
    }

    @Test
    fun `the tab already on screen is left alone`() {
        val stack = stackOf(TopLevelDestination.Home.key, TopLevelDestination.Catalog.key, product)
        val before = stack.toList()

        stack.selectTab(TopLevelDestination.Catalog)

        // Not "moved to the end and happens to look the same": tapping the current tab must not
        // disturb the screen the user is on.
        assertEquals(before, stack.toList())
    }

    @Test
    fun `a visited tab moves with the screens the user left on it`() {
        val stack = stackOf(
            TopLevelDestination.Home.key,
            TopLevelDestination.Catalog.key,
            product,
            TopLevelDestination.Cart.key,
        )

        stack.selectTab(TopLevelDestination.Catalog)

        assertEquals(
            listOf(
                TopLevelDestination.Home.key,
                TopLevelDestination.Cart.key,
                TopLevelDestination.Catalog.key,
                product,
            ),
            stack.toList(),
        )
        assertEquals(TopLevelDestination.Catalog, stack.currentTab)
    }

    @Test
    fun `a segment ends at the next tab key, not at the end of the stack`() {
        // The boundary: Home's segment is Home and its one screen, and stops before Catalog.
        val stack = stackOf(
            TopLevelDestination.Home.key,
            GalleryDestination,
            TopLevelDestination.Catalog.key,
            product,
        )

        stack.selectTab(TopLevelDestination.Home)

        assertEquals(
            listOf(
                TopLevelDestination.Catalog.key,
                product,
                TopLevelDestination.Home.key,
                GalleryDestination,
            ),
            stack.toList(),
        )
    }

    @Test
    fun `switching away and back returns to the screen the tab was left on`() {
        val stack = stackOf(TopLevelDestination.Catalog.key, product)

        stack.selectTab(TopLevelDestination.Cart)
        stack.selectTab(TopLevelDestination.Catalog)

        assertSame(product, stack.last())
    }

    @Test
    fun `backing out of a tab root reveals the tab visited before it`() {
        // What Android expects of back, and what the flat list gives for nothing.
        val stack = stackOf(TopLevelDestination.Home.key)
        stack.selectTab(TopLevelDestination.Cart)

        stack.removeAt(stack.lastIndex)

        assertEquals(TopLevelDestination.Home, stack.currentTab)
    }

    @Test
    fun `selecting every tab in turn leaves one segment each`() {
        val stack = stackOf(TopLevelDestination.Home.key)

        TopLevelDestination.entries.forEach(stack::selectTab)

        assertEquals(TopLevelDestination.entries.map { it.key }, stack.toList())
    }
}
