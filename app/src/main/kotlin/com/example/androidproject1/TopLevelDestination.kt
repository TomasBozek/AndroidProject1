package com.example.androidproject1

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.cart.presentation.cart.CartDestination
import com.example.androidproject1.feature.catalog.presentation.categories.CategoriesDestination
import com.example.androidproject1.feature.home.presentation.home.HomeDestination
import com.example.androidproject1.feature.settings.presentation.settings.SettingsDestination
import com.example.androidproject1.feature.trips.presentation.trips.TripsDestination

/**
 * A tab of the bottom bar: a top-level destination, and the root of its own back stack.
 *
 * Lives in `:app` for the same reason `AppNavHost` does — it is the one place allowed to know about
 * more than one feature.
 *
 * @property label a string resource in `:app`'s own `strings.xml`, not the feature's: what a screen
 * calls itself and what the tab that leads to it calls itself are not the same decision.
 */
enum class TopLevelDestination(
    val key: NavKey,
    @StringRes val label: Int,
    val icon: ImageVector,
    /**
     * What a flow and a test find this tab by — `CLAUDE.md` § Test identifiers, "find by id, never
     * by text". Carried on the entry rather than written at the call site, so a tab cannot be added
     * without one; `doctor.py` fails on a tab whose tag is not `tabs_<name>Tab`.
     */
    val testTag: String,
    /**
     * Whether this tab shows a count on its icon.
     *
     * A boolean rather than the flow itself: an enum entry is a constant, and the flow comes from
     * Koin — which the enum cannot reach and should not know about. `AppNavHost` supplies it.
     */
    val hasBadge: Boolean = false,
) {

    Home(HomeDestination, R.string.tab_home, Icons.Filled.Home, testTag = "tabs_homeTab"),
    Catalog(CategoriesDestination, R.string.tab_catalog, Icons.AutoMirrored.Filled.List, testTag = "tabs_catalogTab"),
    Cart(CartDestination, R.string.tab_cart, Icons.Filled.ShoppingCart, testTag = "tabs_cartTab", hasBadge = true),
    Trips(TripsDestination, R.string.tab_trips, Icons.Filled.DateRange, testTag = "tabs_tripsTab"),
    Settings(SettingsDestination, R.string.tab_settings, Icons.Filled.Settings, testTag = "tabs_settingsTab"),
    ;

    companion object {

        fun of(key: NavKey): TopLevelDestination? = entries.firstOrNull { it.key == key }
    }
}

/**
 * The tab on screen, or `null` when the back stack is not in the tabbed main flow.
 *
 * The tabs do not each hold a list of their own. The back stack **is** their concatenation, in the
 * order the tabs were last visited, and a tab's key is the only thing that starts a segment — so
 * the tab on screen is the last tab key on the stack, and everything after it is that tab's own
 * history. One flat list is what makes per-tab history survive process death for nothing: it is the
 * list `rememberNavBackStack` already saves, and pushing and popping still work unchanged, because
 * they act on the segment that happens to be last.
 */
val NavBackStack<NavKey>.currentTab: TopLevelDestination?
    get() = asReversed().firstNotNullOfOrNull { TopLevelDestination.of(it) }

/**
 * Brings [tab] to the front, keeping the screens the user left on it.
 *
 * A tab not visited yet is appended as a new segment; the tab already on screen is left alone.
 */
fun NavBackStack<NavKey>.selectTab(tab: TopLevelDestination) {
    val start = indexOf(tab.key)
    if (start < 0) {
        add(tab.key)
        return
    }

    val end = segmentEnd(start)
    if (end == size) return

    val segment = subList(start, end).toList()
    subList(start, end).clear()
    addAll(segment)
}

/** The index one past the last key belonging to the segment starting at [start]. */
private fun NavBackStack<NavKey>.segmentEnd(start: Int): Int =
    (start + 1 until size).firstOrNull { TopLevelDestination.of(this[it]) != null } ?: size
