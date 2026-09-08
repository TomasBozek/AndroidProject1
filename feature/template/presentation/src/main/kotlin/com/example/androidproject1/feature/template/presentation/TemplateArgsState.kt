package com.example.androidproject1.feature.template.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class TemplateArgsState(
    // TODO: replace with this screen's real state.
    val templateId: String,
) {

    companion object {

        val PREVIEW = TemplateArgsState(
            templateId = "example",
        )
    }
}
