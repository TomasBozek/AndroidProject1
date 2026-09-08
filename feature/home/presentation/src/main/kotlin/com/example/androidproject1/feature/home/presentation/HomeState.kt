package com.example.androidproject1.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText

@Immutable
data class HomeState(
    // UiText rather than String: the ViewModel picks the text without a Context.
    val greeting: UiText,
) {

    companion object {

        val PREVIEW = HomeState(
            greeting = R.string.home_greeting.toUiText(),
        )
    }
}
