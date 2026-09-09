package com.example.androidproject1.debug

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.androidproject1.App
import com.example.androidproject1.R
import com.example.androidproject1.feature.devmenu.presentation.NotificationTester

/**
 * The debug menu's "post a test notification", carrying a product deep link.
 *
 * In `:app` rather than in the feature because only this module knows the app's own URL scheme and
 * which Activity receives it — see `DeepLinks`. `:app` binds it into the debug menu's graph the
 * same way it binds `BuildInfo` and `OfflineSwitch`.
 *
 * In `main` rather than in the flavor source sets `DebugMenu` lives in: there is nothing per-flavor
 * about it, and `ApplicationModule` only reaches it from inside the branch that folds away in
 * `prod`, so R8 drops it there with everything else the debug menu names.
 */
class TestNotification(private val context: Context) : NotificationTester {

    override fun post(): Boolean {
        val manager = NotificationManagerCompat.from(context)
        // Both are refusals with the same outcome: the permission was never granted, or the
        // channel was turned off in system settings afterwards.
        if (!manager.areNotificationsEnabled()) return false

        val notification = NotificationCompat.Builder(context, App.CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.debug_notification_title))
            .setContentText(context.getString(R.string.debug_notification_text))
            .setContentIntent(deepLinkIntent())
            .setAutoCancel(true)
            .build()

        return runCatching { manager.notify(NOTIFICATION_ID, notification) }.isSuccess
    }

    /**
     * The same `VIEW` intent an external app would send, aimed at this package.
     *
     * Explicit about the package rather than left open, so the tap cannot be answered by a chooser
     * or by another app that has registered the scheme — and so it behaves identically to
     * `adb shell am start -d …`, which is what the deep link was verified with.
     */
    private fun deepLinkIntent(): PendingIntent {
        val scheme = context.getString(R.string.deep_link_scheme)
        val intent = Intent(Intent.ACTION_VIEW, "$scheme://product/$PRODUCT_ID".toUri())
            .setPackage(context.packageName)

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            // IMMUTABLE because nothing may rewrite the link on the way through; UPDATE_CURRENT so
            // posting twice replaces the pending intent rather than reusing a stale one.
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {

        const val NOTIFICATION_ID = 1

        const val REQUEST_CODE = 0

        /** A product the `dev` fixtures actually have, so the tap lands on a real screen. */
        const val PRODUCT_ID = "croissant"
    }
}
