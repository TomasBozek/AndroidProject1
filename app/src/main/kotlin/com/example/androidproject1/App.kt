package com.example.androidproject1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import com.example.androidproject1.core.di.debugMenuModules
import com.example.androidproject1.core.di.initKoin
import com.example.androidproject1.debug.DebugMenu
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(
            ApplicationModule.module,
            // Registered from here rather than from `appModules`, so a `prod` build never names
            // the debug menu or the gallery and R8 can drop both — see `debugMenuModules`.
            *debugModules().toTypedArray(),
            isDebug = BuildConfig.DEBUG,
        ) {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@App)
        }

        createNotificationChannel()
    }

    /**
     * Channels are what the user sees in system settings, and creating one is idempotent — so it
     * belongs here rather than at the first notification, where a race or an early crash would
     * leave the app with a permission and nowhere to post.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            getString(R.string.notification_channel_general),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    /** `DebugMenu.ENABLED` is a `const` per flavor, so in `prod` this folds to an empty list. */
    private fun debugModules() = if (DebugMenu.ENABLED) debugMenuModules() else emptyList()

    companion object {

        const val CHANNEL_ID_GENERAL = "general"
    }
}
