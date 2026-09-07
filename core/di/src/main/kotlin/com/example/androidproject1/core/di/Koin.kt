package com.example.androidproject1.core.di

import com.example.androidproject1.core.data.AndroidLogger
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.feature.auth.di.AuthModule
import com.example.androidproject1.feature.home.di.HomeModule
import com.example.androidproject1.feature.settings.di.SettingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Cross-cutting dependencies available to every module.
 */
val coreModule: Module = module {
    factory<Logger> { AndroidLogger() }
    factory<Logger>(named(TAGGED_LOGGER)) { (tag: String) -> AndroidLogger(tag) }

    single { DataStoreProvider(androidContext()) }
}

/** Qualifier for a [Logger] that takes its tag as a parameter. */
const val TAGGED_LOGGER = "tagged"

/**
 * The single Koin registration point.
 *
 * `scripts/create_feature.py` edits the module list below, so keep the call formatted one module
 * per line.
 */
fun initKoin(
    vararg appModules: Module,
    platformActions: KoinApplication.() -> Unit = {},
) {
    startKoin {
        platformActions()
        modules(
            *appModules,
            coreModule,
            AuthModule.module,
            HomeModule.module,
            SettingsModule.module,
        )
    }
}
