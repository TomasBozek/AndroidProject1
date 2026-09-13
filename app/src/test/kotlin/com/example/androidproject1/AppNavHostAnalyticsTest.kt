package com.example.androidproject1

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.feature.cart.domain.test.FakeCartRepository
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import com.example.androidproject1.feature.catalog.domain.test.FakeFavouritesRepository
import com.example.androidproject1.feature.home.presentation.home.HomeDestination
import com.example.androidproject1.feature.home.presentation.home.HomeViewModel
import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.test.FakeInventoryRepository
import com.example.androidproject1.service.core.domain.Analytics
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.test.FakeLogger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * That the bound [Analytics] actually reaches composition.
 *
 * `core.6` built the seam and `ScreenViewTest` in `:service:core:ui` pins what `AppScaffold` does
 * with it — but neither can see whether anything ever *provides* one. Without the
 * `ProvideAnalytics` call in [AppNavHost] every screen view in the running app goes to
 * `Analytics.NoOp` and nothing anywhere is red. This composes the real nav host over a real Koin
 * graph and asserts the view arrives, which is the only place that gap is visible.
 */
@RunWith(RobolectricTestRunner::class)
// The plain Application, not this project's `App`: its `onCreate` starts the real Koin graph,
// and this test wants a small one it can put a recording Analytics into.
@Config(application = Application::class)
class AppNavHostAnalyticsTest {

    @get:Rule
    val compose = createComposeRule()

    private val screens = mutableListOf<String>()

    private val analytics = object : Analytics {
        override fun screen(id: String) {
            screens += id
        }

        override fun event(name: String, params: Map<String, Any?>) = Unit
    }

    /**
     * Only what the nav host itself resolves, plus the one screen this renders: the graph as a
     * whole is `KoinGraphTest`'s job, and starting it here would drag in Room and DataStore to
     * prove something about a composition local.
     */
    @Suppress("unused")
    private val graph = module {
        single<Analytics> { analytics }
        single<Logger> { FakeLogger() }
        single<CatalogRepository> { FakeCatalogRepository() }
        single<CartRepository> { FakeCartRepository() }
        single<FavouritesRepository> { FakeFavouritesRepository() }
        single<InventoryRepository> { FakeInventoryRepository() }
        viewModelOf(::HomeViewModel)
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun `a screen composed by the nav host reports its view`() {
        startKoin { modules(graph) }

        compose.setContent {
            AppTheme {
                // Home rather than the gallery: the gallery is registered only behind
                // `DebugMenu.ENABLED`, so this would not resolve on the `prod` flavor.
                AppNavHost(backStack = NavBackStack<NavKey>(HomeDestination))
            }
        }
        compose.waitForIdle()

        assertEquals(listOf("HomeScreen"), screens)
    }
}
