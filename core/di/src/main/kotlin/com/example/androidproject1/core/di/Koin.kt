package com.example.androidproject1.core.di

import android.util.Log
import com.example.androidproject1.core.data.AndroidLogger
import com.example.androidproject1.core.data.DataStoreProvider
import com.example.androidproject1.core.data.TrackingLogger
import com.example.androidproject1.core.domain.ErrorTracker
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.LoggingErrorTracker
import com.example.androidproject1.core.domain.coroutines.DefaultDispatcherProvider
import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.feature.auth.di.AuthModule
import com.example.androidproject1.feature.catalog.di.CatalogModule
import com.example.androidproject1.feature.gallery.di.GalleryModule
import com.example.androidproject1.feature.home.di.HomeModule
import com.example.androidproject1.feature.settings.di.SettingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Cross-cutting dependencies available to every module.
 *
 * @param isDebug drives what reaches logcat. `:service:core:data` cannot read the app's
 * `BuildConfig`, so the app decides here and a release build stays quiet.
 */
fun coreModule(isDebug: Boolean): Module = module {
    val minLogLevel = if (isDebug) Log.DEBUG else Log.WARN

    // A tag comes from `logger.withTag(...)` at the point of use, so one binding is enough.
    // Where defects are counted. The default reports to the log and nowhere else, which is
    // correct in debug and correct in a project that never adds a vendor. To swap in Crashlytics
    // or Sentry, override this one binding in :app — see CLAUDE.md. No vendor SDK is in the repo.
    single<ErrorTracker> { LoggingErrorTracker(AndroidLogger(minLevel = minLogLevel)) }

    // Every Logger in the app is decorated, so anything logged with a Throwable is reported
    // without BaseViewModel or BaseRepository taking an extra constructor argument.
    factory<Logger> { TrackingLogger(AndroidLogger(minLevel = minLogLevel), get()) }

    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    single { DataStoreProvider(androidContext()) }
}

/**
 * Every module this app's graph is assembled from, in registration order.
 *
 * One list, so that the graph `KoinGraphTest` verifies is the graph `initKoin` starts: two lists
 * kept in step by a comment is a drift waiting to happen. `scripts/create_feature.py` edits it, so
 * keep it formatted one module per line.
 */
fun appModules(isDebug: Boolean): List<Module> = listOf(
    coreModule(isDebug),
    AuthModule.module,
    HomeModule.module,
    SettingsModule.module,
    CatalogModule.module,
    GalleryModule.module,
)

/**
 * The single Koin registration point.
 *
 * @param platformModules bindings only the application module can provide, such as `MainViewModel`.
 */
fun initKoin(
    vararg platformModules: Module,
    isDebug: Boolean = false,
    platformActions: KoinApplication.() -> Unit = {},
) {
    startKoin {
        platformActions()
        modules(*platformModules, *appModules(isDebug).toTypedArray())
    }
}
