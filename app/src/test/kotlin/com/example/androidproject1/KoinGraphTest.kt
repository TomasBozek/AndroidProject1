package com.example.androidproject1

import android.content.Context
import com.example.androidproject1.core.di.appModules
import com.example.androidproject1.feature.catalog.presentation.ProductDetailDestination
import com.example.androidproject1.feature.catalog.presentation.ProductDetailViewModel
import com.example.androidproject1.feature.catalog.presentation.ProductPickerDestination
import com.example.androidproject1.feature.catalog.presentation.ProductPickerViewModel
import com.example.androidproject1.feature.catalog.presentation.ProductsDestination
import com.example.androidproject1.feature.catalog.presentation.ProductsViewModel
import com.example.androidproject1.feature.gallery.presentation.GalleryDetailDestination
import com.example.androidproject1.feature.gallery.presentation.GalleryDetailViewModel
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
        }

        graph.verify(
            // Supplied at resolution time rather than by a module: Context by androidContext().
            extraTypes = listOf(Context::class),
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
