package com.example.androidproject1.core.di

import android.util.Log
import com.example.androidproject1.core.data.AndroidLogger
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.feature.auth.di.AuthModule
import com.example.androidproject1.feature.catalog.di.CatalogModule
import com.example.androidproject1.feature.home.di.HomeModule
import com.example.androidproject1.feature.launch.di.LaunchModule
import com.example.androidproject1.feature.settings.di.SettingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Cross-cutting dependencies available to every module.
 *
 * @param isDebug drives what reaches logcat. `:service:core:data` cannot read the app's
 * `BuildConfig`, so the app decides here and a release build stays quiet.
 */
fun coreModule(isDebug: Boolean): Module = module {
    val minLogLevel = if (isDebug) Log.DEBUG else Log.WARN

    factory<Logger> { AndroidLogger(minLevel = minLogLevel) }
    factory<Logger>(named(TAGGED_LOGGER)) { (tag: String) -> AndroidLogger(tag, minLogLevel) }

    single { DispatcherProvider() }
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
    isDebug: Boolean = false,
    platformActions: KoinApplication.() -> Unit = {},
) {
    startKoin {
        platformActions()
        modules(
            *appModules,
            coreModule(isDebug),
            AuthModule.module,
            HomeModule.module,
            SettingsModule.module,
            LaunchModule.module,
            CatalogModule.module,
        )
    }
}
