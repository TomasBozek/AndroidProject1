package com.example.androidproject1

import com.example.androidproject1.core.network.NetworkConfig
import com.example.androidproject1.debug.DebugMenu
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import com.example.androidproject1.network.networkEngine
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
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

        // `dev` resolves this to the MockEngine over res/raw fixtures (D20); the other flavors to
        // OkHttp. Different source sets, so the wrong one is not in the build at all.
        single<HttpClientEngine> { networkEngine(androidContext()) }

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
}

/** `debugImplementation` only, so it is absent from a release build by construction. */
private const val LEAK_CANARY = "leakcanary.LeakCanary"
