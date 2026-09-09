package com.example.androidproject1

import com.example.androidproject1.core.network.NetworkConfig
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
    }
}
