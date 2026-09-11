package com.example.androidproject1

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.di.appModules
import com.example.androidproject1.core.di.debugMenuModules
import com.example.androidproject1.debug.DebugMenu
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailDestination
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailViewModel
import com.example.androidproject1.feature.catalog.presentation.productpicker.ProductPickerDestination
import com.example.androidproject1.feature.catalog.presentation.productpicker.ProductPickerViewModel
import com.example.androidproject1.feature.catalog.presentation.products.ProductsDestination
import com.example.androidproject1.feature.catalog.presentation.products.ProductsViewModel
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.GalleryDetailDestination
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.GalleryDetailViewModel
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

/**
 * Asserts that every dependency every registered class asks for is actually provided.
 *
 * Worth more here than in most projects: the graph spans 24 modules and `create_feature.py`
 * assembles part of it by editing `Koin.kt` and `core/di/build.gradle.kts`. A missing binding is
 * otherwise a crash at app launch, which is the slowest possible way to find out.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinGraphTest {

    @Test
    fun `every dependency in the graph can be resolved`() {
        // One module including all the others: `verify` follows `includes`, so this checks the
        // graph as it is actually assembled rather than each module in isolation — where every
        // cross-module dependency would look missing. `appModules` is the same list `initKoin`
        // starts, so there is nothing here to keep in step by hand.
        val graph = module {
            includes(ApplicationModule.module)
            includes(appModules(isDebug = true))
            // The debug menu and the gallery are registered by :app, behind a const that is false
            // in `prod` — so this verifies them in exactly the builds that start them.
            if (DebugMenu.ENABLED) includes(debugMenuModules())
        }

        graph.verify(
            // Supplied at resolution time rather than by a module: Context by androidContext(),
            // SavedStateHandle by Koin's own ViewModel factory — see BaseViewModel.saved().
            extraTypes = listOf(Context::class, SavedStateHandle::class),
            // A screen that takes navigation arguments gets its route key from the destination
            // through `parametersOf(key)`, so the graph does not provide it. One line per such
            // screen; `doctor.py` fails if one is missing, and `create_screen.py --with-args`
            // writes it.
            injections = injectedParameters(
                definition<ProductPickerViewModel>(ProductPickerDestination::class),
                definition<GalleryDetailViewModel>(GalleryDetailDestination::class),
                definition<ProductsViewModel>(ProductsDestination::class),
                definition<ProductDetailViewModel>(ProductDetailDestination::class),
            ),
        )
    }
}
