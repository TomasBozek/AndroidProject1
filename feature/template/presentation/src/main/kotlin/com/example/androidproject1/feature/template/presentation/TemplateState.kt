package com.example.androidproject1.feature.template.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class TemplateState(
    // TODO: replace with this screen's real state.
    val counter: Int,
) {

    companion object {

        val PREVIEW = TemplateState(
            counter = 1,
        )
    }
}
