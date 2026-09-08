package com.example.androidproject1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import com.example.androidproject1.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(
            ApplicationModule.module,
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

    companion object {

        const val CHANNEL_ID_GENERAL = "general"
    }
}
