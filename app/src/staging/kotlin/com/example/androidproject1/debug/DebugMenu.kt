package com.example.androidproject1.debug

import android.content.Context
import com.example.androidproject1.feature.devmenu.presentation.ApiSwitch
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch

/** See the `dev` source set's copy for what this is and why it is a `const`. */
object DebugMenu {

    const val ENABLED = true

    // A real server, so there is no fixture to break: the row is not drawn.
    fun offlineSwitch(context: Context): OfflineSwitch = OfflineSwitch.Unsupported

    // The real host already: there is nothing to switch to.
    fun apiSwitch(context: Context): ApiSwitch = ApiSwitch.Unsupported
}
