package com.example.androidproject1

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.androidproject1.core.ui.navigation.ProvideNavResultStore
import com.example.androidproject1.feature.auth.presentation.loginDestination
import com.example.androidproject1.feature.auth.presentation.signUpDestination
import com.example.androidproject1.feature.catalog.presentation.categoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productDetailDestination
import com.example.androidproject1.feature.catalog.presentation.productsDestination
import com.example.androidproject1.feature.gallery.presentation.GalleryDestination
import com.example.androidproject1.feature.gallery.presentation.galleryDestination
import com.example.androidproject1.feature.gallery.presentation.galleryDetailDestination
import com.example.androidproject1.feature.home.presentation.homeDestination
import com.example.androidproject1.feature.settings.presentation.settingsDestination
import com.example.androidproject1.feature.settings.presentation.settingsPermissionsDestination

/**
 * The only place that knows about more than one feature. Cross-feature navigation is a lambda.
 *
 * Navigation 3 has no nested graphs: [backStack] is a plain list of keys and every entry is
 * registered in one provider. [authEntries] and [mainEntries] — and inside the latter, one block
 * per tab — group them the way the old `navigation<AuthNavGraph>` blocks did. The grouping is what
 * `create_feature.py --graph` writes into, and what makes the file readable, but nothing enforces
 * it at runtime: any entry can appear on any stack. Which flow the user is in is decided by what
 * MainActivity puts on the back stack, and which tab by [currentTab].
 */
@Composable
fun AppNavHost(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val currentTab = backStack.currentTab

    // Around the display, not inside an entry: a nav result has to outlive the screen that
    // produced it being popped. See core.ui.navigation.NavResultStore.
    ProvideNavResultStore {
        AppNavContent(backStack = backStack, currentTab = currentTab, modifier = modifier)
    }
}

@Composable
private fun AppNavContent(
    backStack: NavBackStack<NavKey>,
    currentTab: TopLevelDestination?,
    modifier: Modifier = Modifier,
) {
    // The auth flow has no tabs, so it is not wrapped: an empty navigation suite would still
    // reserve the bar's height.
    if (currentTab == null) {
        AppNavDisplay(backStack = backStack, modifier = modifier)
        return
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { tab ->
                item(
                    selected = tab == currentTab,
                    onClick = { backStack.selectTab(tab) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    label = { Text(text = stringResource(tab.label)) },
                )
            }
        },
    ) {
        // A bar on a phone, a rail once there is width for one, and nothing here to decide it.
        AppNavDisplay(backStack = backStack)
    }
}

@Composable
private fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val entries = entryProvider<NavKey> {
        authEntries(backStack)
        mainEntries(backStack)
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        // NavDisplay adds its own scene-setup decorator; these two are the ones a screen needs.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Gives every entry its own ViewModelStore, which is what koinViewModel() resolves
            // against — without it two screens would share one ViewModel.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        // Going deeper slides; the library's default is a cross-fade, which reads as unfinished.
        transitionSpec = {
            slideIntoContainer(SlideDirection.Start, SLIDE) togetherWith
                slideOutOfContainer(SlideDirection.Start, SLIDE)
        },
        popTransitionSpec = {
            slideIntoContainer(SlideDirection.End, SLIDE) togetherWith
                slideOutOfContainer(SlideDirection.End, SLIDE)
        },
        // predictivePopTransitionSpec is left at the library's default: it is the platform's own
        // back gesture, and a screen that scales away under the user's finger is what that looks
        // like everywhere else on the device.
        entryProvider = { key -> entries(key).withTabRootTransitions(key) },
    )
}

/**
 * Makes a tab root fade instead of slide.
 *
 * Attached to the entry rather than decided in the host's spec because `NavDisplay` resolves entry
 * metadata against the screen *arriving* on a push and the one *leaving* on a pop — which is exactly
 * the rule wanted here and not one the host's spec can express. Arriving at a tab root is a tab
 * switch, so it fades; arriving at `CategoriesDestination` by popping `ProductsDestination` off is
 * not, and the leaving screen has no metadata, so it slides back.
 */
private fun NavEntry<NavKey>.withTabRootTransitions(key: NavKey): NavEntry<NavKey> =
    if (TopLevelDestination.of(key) == null) {
        this
    } else {
        NavEntry(
            key = key,
            contentKey = contentKey,
            metadata = metadata + TAB_ROOT_TRANSITIONS,
            content = { Content() },
        )
    }

private val SLIDE = tween<IntOffset>(durationMillis = 300)

private val FADE = tween<Float>(durationMillis = 200)

private val TAB_ROOT_TRANSITIONS: Map<String, Any> =
    NavDisplay.transitionSpec { fadeIn(FADE) togetherWith fadeOut(FADE) } +
        NavDisplay.popTransitionSpec { fadeIn(FADE) togetherWith fadeOut(FADE) }

/** Shown while signed out. */
private fun EntryProviderScope<NavKey>.authEntries(backStack: NavBackStack<NavKey>) {
    loginDestination(backStack = backStack)
    signUpDestination(backStack = backStack)
}

/** Shown while signed in, grouped by the tab whose stack the screen is pushed onto. */
private fun EntryProviderScope<NavKey>.mainEntries(backStack: NavBackStack<NavKey>) {
    homeEntries(backStack)
    catalogEntries(backStack)
    settingsEntries(backStack)
    galleryDetailDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.homeEntries(backStack: NavBackStack<NavKey>) {
    homeDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.catalogEntries(backStack: NavBackStack<NavKey>) {
    categoriesDestination(backStack = backStack)
    productsDestination(backStack = backStack)
    productDetailDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.settingsEntries(backStack: NavBackStack<NavKey>) {
    settingsDestination(
        backStack = backStack,
        navigateToComponents = { backStack.add(GalleryDestination) },
    )
    settingsPermissionsDestination(backStack = backStack)
    galleryDestination(backStack = backStack)
}
