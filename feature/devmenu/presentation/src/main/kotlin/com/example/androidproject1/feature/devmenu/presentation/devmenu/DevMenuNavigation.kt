package com.example.androidproject1.feature.devmenu.presentation.devmenu

/** One-off navigation intents, turned into back-stack calls in DevMenuDestination. */
sealed interface DevMenuNavigation {

    data object NavigateUp : DevMenuNavigation

    /** The component gallery. Another feature, so the destination takes a lambda for it. */
    data object Components : DevMenuNavigation
}
