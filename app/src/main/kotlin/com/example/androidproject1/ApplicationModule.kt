package com.example.androidproject1

import com.example.androidproject1.debug.DebugMenu
import com.example.androidproject1.debug.TestNotification
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.NotificationTester
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import com.example.androidproject1.feature.movies.domain.TmdbConfig
import com.example.androidproject1.feature.settings.domain.LanguageRepository
import com.example.androidproject1.locale.AppCompatLanguageRepository
import com.example.androidproject1.network.connectivityMonitor
import com.example.androidproject1.network.networkEngine
import com.example.androidproject1.service.network.ConnectivityMonitor
import com.example.androidproject1.service.network.NetworkConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Bindings only the application module can provide. */
object ApplicationModule {

    val module: Module = module {
        viewModelOf(::MainViewModel)

        // BuildConfig lives here, so the config is built here: :core:di assembles the client but
        // must not know which flavor it is in.
        single {
            NetworkConfig(
                baseUrl = BuildConfig.BASE_URL,
                // Headers and bodies carry the bearer token and whatever the user typed.
                logBodies = BuildConfig.DEBUG,
            )
        }

        // The second host (D80): the constants from ProjectConfig through BuildConfig, and the key
        // from local.properties — `""` on a clone without one, which is a fixtures-only build.
        single {
            TmdbConfig(
                apiBaseUrl = BuildConfig.TMDB_API_BASE_URL,
                imageBaseUrl = BuildConfig.TMDB_IMAGE_BASE_URL,
                apiKey = BuildConfig.TMDB_API_KEY,
            )
        }

        // `dev` resolves this to the MockEngine over res/raw fixtures (D20); the other flavors to
        // OkHttp. Different source sets, so the wrong one is not in the build at all.
        single<HttpClientEngine> { networkEngine(androidContext(), get<NetworkConfig>().httpCacheSizeBytes) }

        // Whether a request has a route at all is the platform's to say — and on `dev` the fixture
        // switch's, since the engine is in-process and airplane mode means nothing to it. Per
        // flavor source set, beside the engine (D68).
        single<ConnectivityMonitor> { connectivityMonitor(androidContext(), get()) }

        // The per-app language, stored by AppCompat and read by the Settings feature (D75). Here
        // for the same reason as the monitor: the class it wraps belongs to the activity stack.
        singleOf(::AppCompatLanguageRepository) bind LanguageRepository::class

        // Only where there is a debug menu to show them. A binding is a reference, and a `prod`
        // build that named `BuildInfo` would keep it; `DebugMenu.ENABLED` is a const, so the
        // branch folds away there. `KoinGraphTest` skips the same block for the same reason, so
        // the graph it verifies is still the graph that starts.
        if (DebugMenu.ENABLED) debugMenuBindings()
    }
}

/** What the debug menu reads: this build's identity, and the fixture engine's offline switch. */
private fun Module.debugMenuBindings() {
    single {
        BuildInfo(
            applicationId = BuildConfig.APPLICATION_ID,
            flavor = BuildConfig.FLAVOR,
            buildType = BuildConfig.BUILD_TYPE,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            baseUrl = BuildConfig.BASE_URL,
            // LeakCanary installs itself and nothing in :app references it, so asking the
            // classloader is the only honest way to say whether this build has it.
            leakDetection = runCatching { Class.forName(LEAK_CANARY) }.isSuccess,
        )
    }
    single<OfflineSwitch> { DebugMenu.offlineSwitch(androidContext()) }
    single<NotificationTester> { TestNotification(androidContext()) }
}

/** `debugImplementation` only, so it is absent from a release build by construction. */
private const val LEAK_CANARY = "leakcanary.LeakCanary"
