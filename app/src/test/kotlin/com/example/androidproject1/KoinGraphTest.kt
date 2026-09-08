package com.example.androidproject1

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.di.appModules
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Asserts that every dependency every registered class asks for is actually provided.
 *
 * Worth more here than in most projects: the graph spans 27 modules and `create_feature.py`
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
            // Supplied at resolution time rather than by a module: Context by androidContext(),
            // SavedStateHandle by the ViewModel owner.
            extraTypes = listOf(
                Context::class,
                SavedStateHandle::class,
            ),
        )
    }
}
