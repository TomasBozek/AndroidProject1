package com.example.androidproject1.feature.home.presentation

import com.example.androidproject1.core.ui.AppString
import com.example.androidproject1.core.ui.toText

data class HomeState(
    // An AppString rather than a String: the ViewModel picks the text without needing a Context,
    // and the screen resolves it at composition. This is the pattern for any state a ViewModel sets.
    val greeting: AppString,
) {

    companion object {

        val PREVIEW = HomeState(
            greeting = R.string.home_greeting.toText(),
        )
    }
}
