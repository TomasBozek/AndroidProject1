package com.example.androidproject1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.androidproject1.feature.auth.presentation.loginDestination
import com.example.androidproject1.feature.auth.presentation.signUpDestination
import com.example.androidproject1.feature.catalog.presentation.CategoriesDestination
import com.example.androidproject1.feature.catalog.presentation.categoriesDestination
import com.example.androidproject1.feature.catalog.presentation.productDetailDestination
import com.example.androidproject1.feature.catalog.presentation.productsDestination
import com.example.androidproject1.feature.home.presentation.homeDestination
import com.example.androidproject1.feature.launch.presentation.launchDestination
import com.example.androidproject1.feature.settings.presentation.SettingsDestination
import com.example.androidproject1.feature.settings.presentation.settingsDestination

/**
 * The only place that knows about more than one feature. Cross-feature navigation is a lambda.
 *
 * Navigation 3 has no nested graphs: [backStack] is a plain list of keys and every entry is
 * registered in one provider. [authEntries] and [mainEntries] group them the way the old
 * `navigation<AuthNavGraph>` blocks did — the grouping is what `create_feature.py --graph` writes
 * into, and what makes the file readable, but nothing enforces it at runtime. Which flow the user
 * is in is decided by what MainActivity puts on the back stack.
 */
@Composable
fun AppNavHost(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
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
        entryProvider = entryProvider {
            launchDestination()
            authEntries(backStack)
            mainEntries(backStack)
        },
    )
}

/** Shown while signed out. */
private fun EntryProviderScope<NavKey>.authEntries(backStack: NavBackStack<NavKey>) {
    loginDestination(backStack = backStack)
    signUpDestination(backStack = backStack)
}

/** Shown while signed in. */
private fun EntryProviderScope<NavKey>.mainEntries(backStack: NavBackStack<NavKey>) {
    homeDestination(
        backStack = backStack,
        navigateToSettings = { backStack.add(SettingsDestination) },
        navigateToCatalog = { backStack.add(CategoriesDestination) },
    )
    settingsDestination(backStack = backStack)
    categoriesDestination(backStack = backStack)
    productsDestination(backStack = backStack)
    productDetailDestination(backStack = backStack)
}
