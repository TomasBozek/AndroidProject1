package com.example.androidproject1.debug

import android.content.Context
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import com.example.androidproject1.network.FixtureNetwork

/**
 * Whether this build has a debug menu, and what its switches are wired to.
 *
 * One of these per flavor source set, the same split `networkEngine` uses (D16). [ENABLED] is a
 * `const`, so `prod`'s `false` folds the branch in `AppNavHost` away at compile time and R8 drops
 * the screens behind it — the gallery's component catalogue included.
 */
object DebugMenu {

    const val ENABLED = true

    fun offlineSwitch(context: Context): OfflineSwitch = FixtureOfflineSwitch(context)
}

/**
 * The `dev` flavor talks to fixtures, and [FixtureNetwork] is what makes them fail on demand. It
 * is backed by a file so it survives a process death, which is also why the state is read back
 * rather than remembered.
 */
private class FixtureOfflineSwitch(private val context: Context) : OfflineSwitch {

    override val isSupported: Boolean = true

    override fun isOffline(): Boolean = FixtureNetwork.failing

    override fun setOffline(offline: Boolean) = FixtureNetwork.setFailing(context, offline)
}
