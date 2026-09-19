package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump

/** One-off navigation intents, turned into back-stack calls in DevMenuDestination. */
sealed interface DevMenuNavigation {

    data object NavigateUp : DevMenuNavigation

    /** This feature's own, so the destination pushes it directly. */
    data object Playground : DevMenuNavigation

    /** A screen from the jump list — another feature's, so the jump carries the call itself. */
    data class Jump(val jump: DevMenuJump) : DevMenuNavigation
}
