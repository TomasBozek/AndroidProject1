package com.example.androidproject1

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.androidproject1.core.ui.layout.LocalSharedElementVisibility
import com.example.androidproject1.core.ui.layout.LocalSharedTransitionScope
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.core.ui.theme.SizeClass
import com.example.androidproject1.debug.DebugMenu
import com.example.androidproject1.feature.auth.presentation.login.loginDestination
import com.example.androidproject1.feature.auth.presentation.signup.signUpDestination
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.feature.cart.presentation.cart.CART_PICK_RESULT
import com.example.androidproject1.feature.cart.presentation.cart.cartDestination
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.presentation.categories.categoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productdetail.productDetailDestination
import com.example.androidproject1.feature.catalog.presentation.productpicker.ProductPickerDestination
import com.example.androidproject1.feature.catalog.presentation.productpicker.productPickerDestination
import com.example.androidproject1.feature.catalog.presentation.products.productsDestination
import com.example.androidproject1.feature.catalog.presentation.search.ProductSearchDestination
import com.example.androidproject1.feature.catalog.presentation.search.productSearchDestination
import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump
import com.example.androidproject1.feature.devmenu.presentation.devmenu.DevMenuDestination
import com.example.androidproject1.feature.devmenu.presentation.devmenu.devMenuDestination
import com.example.androidproject1.feature.devmenu.presentation.playground.devMenuPlaygroundDestination
import com.example.androidproject1.feature.gallery.presentation.gallery.GalleryDestination
import com.example.androidproject1.feature.gallery.presentation.gallery.galleryDestination
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.GalleryDetailDestination
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.galleryDetailDestination
import com.example.androidproject1.feature.home.presentation.home.homeDestination
import com.example.androidproject1.feature.inventory.presentation.inventory.InventoryDestination
import com.example.androidproject1.feature.inventory.presentation.inventory.inventoryDestination
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailDestination
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.inventoryDetailDestination
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorDestination
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.inventoryEditorDestination
import com.example.androidproject1.feature.movies.presentation.movies.MoviesDestination
import com.example.androidproject1.feature.movies.presentation.movies.moviesDestination
import com.example.androidproject1.feature.onboarding.presentation.onboarding.onboardingDestination
import com.example.androidproject1.feature.profile.presentation.profile.ProfileDestination
import com.example.androidproject1.feature.profile.presentation.profile.profileDestination
import com.example.androidproject1.feature.settings.presentation.language.settingsLanguageDestination
import com.example.androidproject1.feature.settings.presentation.permissions.SettingsPermissionsDestination
import com.example.androidproject1.feature.settings.presentation.permissions.settingsPermissionsDestination
import com.example.androidproject1.feature.settings.presentation.settings.settingsDestination
import com.example.androidproject1.feature.trips.presentation.destinationpicker.destinationPickerDestination
import com.example.androidproject1.feature.trips.presentation.tripdetail.tripDetailDestination
import com.example.androidproject1.feature.trips.presentation.trips.tripsDestination
import com.example.androidproject1.feature.trips.presentation.tripslist.TripsListDestination
import com.example.androidproject1.feature.trips.presentation.tripslist.tripsListDestination
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardDestination
import com.example.androidproject1.feature.trips.presentation.tripwizard.tripWizardDestination
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.ui.analytics.ProvideAnalytics
import com.example.androidproject1.service.core.ui.navigation.ProvideNavResultStore
import com.example.androidproject1.service.core.ui.text.toUiText
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

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
    // produced it being popped, and the analytics has to be in scope for every screen's
    // `AppScaffold` to report its view through. See core.ui.navigation.NavResultStore and
    // core.ui.analytics.ScreenViewEffect.
    ProvideAnalytics(koinInject()) {
        ProvideNavResultStore {
            AppNavContent(backStack = backStack, currentTab = currentTab, modifier = modifier)
        }
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

    // Collected once, here, rather than inside the Cart screen: the badge has to update while the
    // user is on another tab, which is the whole point of it.
    val cartRepository: CartRepository = koinInject()
    // remembered: building the flow inside composition would make a new one on every
    // recomposition, resubscribing the badge each time the bar redraws.
    val cartCount by remember(cartRepository) {
        cartRepository.observeCount().map { (it as? Outcome.Success)?.data ?: 0 }
    }.collectAsStateWithLifecycle(initialValue = 0)

    NavigationSuiteScaffold(
        // The bar sits above every screen's own AppScaffold, which is where this is switched on for
        // the content — without it here a flow cannot find a tab by id at all.
        modifier = modifier.semantics { testTagsAsResourceId = true },
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { tab ->
                item(
                    selected = tab == currentTab,
                    onClick = { backStack.selectTab(tab) },
                    modifier = Modifier.testTag(tab.testTag),
                    icon = {
                        if (tab.hasBadge && cartCount > 0) {
                            BadgedBox(badge = { Badge { Text(text = cartCount.toString()) } }) {
                                Icon(imageVector = tab.icon, contentDescription = null)
                            }
                        } else {
                            Icon(imageVector = tab.icon, contentDescription = null)
                        }
                    },
                    label = { Text(text = stringResource(tab.label)) },
                )
            }
        },
    ) {
        // A bar on a phone, a rail once there is width for one, and nothing here to decide it.
        AppNavDisplay(backStack = backStack)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    // Resolved here rather than inside the entry: `mainEntries` is a plain function, and the
    // cart needs the catalog to turn a picked id into a line it can hold.
    val catalogRepository: CatalogRepository = koinInject()

    val entries = entryProvider<NavKey> {
        onboardingEntries(backStack)
        authEntries(backStack)
        mainEntries(backStack, catalogRepository)
    }

    // Two panes where there is width for them, one where there is not. The decision is the
    // strategy's, from the window size — nothing here or in a screen asks how wide anything is,
    // and no screen behaves differently: which entries can share a scene is metadata on the two
    // catalog destinations, and every other entry falls through to the single-pane default.
    val listDetail = rememberListDetailSceneStrategy<NavKey>()

    // The product row's name and price travel to the detail (F4S1, D76): the scope is provided
    // once here, and each entry re-provides its own visibility below, so a screen only ever
    // asks `Modifier.appSharedElement(key)`. On a wider window the catalog's two panes are one
    // scene with both elements visible at once, and nothing travels — so the scope is compact-only.
    val compact = AppTheme.density.sizeClass == SizeClass.Compact
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this.takeIf { compact }) {
            AppNavDisplayContent(backStack = backStack, entries = entries, listDetail = listDetail)
        }
    }
}

@Composable
private fun AppNavDisplayContent(
    backStack: NavBackStack<NavKey>,
    entries: (NavKey) -> NavEntry<NavKey>,
    listDetail: SceneStrategy<NavKey>,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetail),
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
        entryProvider = { key -> entries(key).withTabRootTransitions(key).withSharedElementVisibility(key) },
    )
}

/**
 * Re-provides navigation3's entry scope as `:core:ui`'s nullable local, so a screen can read it
 * without knowing navigation3 — and without the throw its own local raises outside an entry.
 */
private fun NavEntry<NavKey>.withSharedElementVisibility(key: NavKey): NavEntry<NavKey> =
    NavEntry(
        key = key,
        contentKey = contentKey,
        metadata = metadata,
        content = {
            CompositionLocalProvider(LocalSharedElementVisibility provides LocalNavAnimatedContentScope.current) {
                Content()
            }
        },
    )

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

/**
 * Shown until the first-run tour has been finished, whether or not a session is stored.
 *
 * A third flow beside [authEntries] and [mainEntries], and like them it is a grouping rather
 * than a graph: which flow the user is in is decided by what `MainActivity` puts on the stack.
 */
private fun EntryProviderScope<NavKey>.onboardingEntries(backStack: NavBackStack<NavKey>) {
    onboardingDestination(backStack = backStack)
}

/** Shown while signed out. */
private fun EntryProviderScope<NavKey>.authEntries(backStack: NavBackStack<NavKey>) {
    loginDestination(backStack = backStack)
    signUpDestination(backStack = backStack)
}

/** Shown while signed in, grouped by the tab whose stack the screen is pushed onto. */
private fun EntryProviderScope<NavKey>.mainEntries(
    backStack: NavBackStack<NavKey>,
    catalogRepository: CatalogRepository,
) {
    homeEntries(backStack)
    catalogEntries(backStack)
    settingsEntries(backStack)
    cartDestination(
        backStack = backStack,
        // The only place that knows both features. The cart says "pick a product"; what that
        // means is decided here, which is what keeps the two presentation modules apart.
        onPickProduct = { backStack.add(ProductPickerDestination(resultKey = CART_PICK_RESULT)) },
        onProductPicked = { productId ->
            // The lookup is the catalog's business, so it happens here rather than in the cart.
            (catalogRepository.getProduct(productId) as? Outcome.Success)?.data?.let { product ->
                CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1,
                )
            }
        },
    )
    productPickerDestination(backStack = backStack)
    tripsEntries(backStack)
    inventoryEntries(backStack)
}

private fun EntryProviderScope<NavKey>.homeEntries(backStack: NavBackStack<NavKey>) {
    homeDestination(
        backStack = backStack,
        // Inventory has no tab (D59); the card on Home is its door, and this is the one place that
        // knows both features.
        navigateToInventory = { backStack.add(InventoryDestination) },
        navigateToMovies = { backStack.add(MoviesDestination) },
    )
    moviesDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.catalogEntries(backStack: NavBackStack<NavKey>) {
    categoriesDestination(backStack = backStack)
    productsDestination(backStack = backStack)
    productDetailDestination(backStack = backStack)
    productSearchDestination(backStack = backStack)
}

/** Not a tab: Inventory's door is the card on Home (D59), so its entries sit beside the tabs'. */
private fun EntryProviderScope<NavKey>.inventoryEntries(backStack: NavBackStack<NavKey>) {
    inventoryDestination(backStack = backStack)
    inventoryEditorDestination(backStack = backStack)
    inventoryDetailDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.tripsEntries(backStack: NavBackStack<NavKey>) {
    tripsDestination(backStack = backStack)
    tripsListDestination(backStack = backStack)
    tripWizardDestination(backStack = backStack)
    tripDetailDestination(backStack = backStack)
    destinationPickerDestination(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.settingsEntries(backStack: NavBackStack<NavKey>) {
    settingsDestination(
        backStack = backStack,
        navigateToProfile = { backStack.add(ProfileDestination) },
        // Null in `prod`, where DebugMenu.ENABLED is a `const false`: the branch folds away at
        // compile time, so nothing outside it names a debug-menu class and Settings draws no
        // entry. Passing the lambda *is* how the screen knows this build has one.
        navigateToDebugMenu = if (DebugMenu.ENABLED) {
            { backStack.add(DevMenuDestination) }
        } else {
            null
        },
    )
    settingsPermissionsDestination(backStack = backStack)
    profileDestination(backStack = backStack)
    if (DebugMenu.ENABLED) debugEntries(backStack)
    settingsLanguageDestination(backStack = backStack)
}

/**
 * The screens a tester gets and a customer does not (D16).
 *
 * Registered behind `DebugMenu.ENABLED`, which is `const` per flavor source set — so in a `prod`
 * build this call is dead code, R8 removes it, and every class it reaches goes with it: the debug
 * menu and the whole component gallery.
 */
private fun EntryProviderScope<NavKey>.debugEntries(backStack: NavBackStack<NavKey>) {
    devMenuDestination(
        backStack = backStack,
        jumps = devMenuJumps(backStack),
    )
    galleryDestination(backStack = backStack)
    galleryDetailDestination(backStack = backStack)
    // The component playground (F4U2): dev and staging only, like the gallery.
    devMenuPlaygroundDestination(backStack = backStack)
}

/**
 * Where the debug menu jumps: the screens a tester otherwise reaches by signing in, tapping a
 * tab, walking a list and entering a wizard. Built here because this is the only file that knows
 * every destination, and pushed onto whatever stack the menu is on, so the up arrow comes back.
 *
 * The label is the screen's own id — the word a flow's `assertVisible` uses — so it is not copy.
 * A route that takes an argument gets a value the fixtures serve on every flavor: the seeded
 * inventory, the gallery catalogue. Never a blank one, which would open an empty screen and prove
 * nothing. Tab roots are one tap away already and are left out.
 */
private fun devMenuJumps(backStack: NavBackStack<NavKey>): List<DevMenuJump> = listOf(
    jump(backStack, "gallery", "GalleryScreen", GalleryDestination),
    jump(backStack, "galleryDetail", "GalleryDetailScreen", GalleryDetailDestination(componentId = "button")),
    jump(backStack, "tripsList", "TripsListScreen", TripsListDestination),
    jump(backStack, "tripWizard", "TripWizardScreen", TripWizardDestination),
    jump(backStack, "productSearch", "ProductSearchScreen", ProductSearchDestination),
    jump(backStack, "profile", "ProfileScreen", ProfileDestination),
    jump(backStack, "settingsPermissions", "SettingsPermissionsScreen", SettingsPermissionsDestination),
    jump(backStack, "inventoryDetail", "InventoryDetailScreen", InventoryDetailDestination(itemId = "drill")),
    jump(backStack, "inventoryEditor", "InventoryEditorScreen", InventoryEditorDestination(itemId = "drill")),
    jump(backStack, "movies", "MoviesScreen", MoviesDestination),
)

private fun jump(
    backStack: NavBackStack<NavKey>,
    id: String,
    screenId: String,
    destination: NavKey,
) = DevMenuJump(
    id = id,
    label = screenId.toUiText(),
    navigate = { backStack.add(destination) },
)
