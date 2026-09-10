package com.example.androidproject1.feature.catalog.presentation.productpicker

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.component.Screen
import com.example.androidproject1.core.ui.navigation.rememberNavResultSender
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * @property resultKey where to put the chosen product. Carried in the route rather than held in a
 * ViewModel, so it survives process death with the back stack entry it belongs to.
 */
@Serializable
data class ProductPickerDestination(val resultKey: String) : NavKey

fun EntryProviderScope<NavKey>.productPickerDestination(backStack: NavBackStack<NavKey>) {
    entry<ProductPickerDestination> { key ->
        val viewModel: ProductPickerViewModel = koinViewModel { parametersOf(key) }
        val setNavResult = rememberNavResultSender(key.resultKey)

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is ProductPickerNavigation.Picked -> {
                        // Set, then pop: the store lives above the entries, so the value outlives
                        // this screen leaving the back stack.
                        setNavResult(navigation.productId)
                        backStack.removeLastOrNull()
                    }

                    // No result set, so the requester sees nothing arrive.
                    ProductPickerNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            ProductPickerScreen(state = state, onEvent = onEvent)
        }
    }
}
