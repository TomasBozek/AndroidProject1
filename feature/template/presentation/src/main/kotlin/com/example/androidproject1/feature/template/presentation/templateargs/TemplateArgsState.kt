package com.example.androidproject1.feature.template.presentation.templateargs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Immutable
data class TemplateArgsState(
    // The value carried in by the route key. Replace with what this screen renders.
    val templateId: String,
) {

    companion object {

        val PREVIEW = TemplateArgsState(
            templateId = "example",
        )
    }
}

/** See `TemplateStatePreviews` — the same three states, for the argument-carrying screen. */
class TemplateArgsStatePreviews : PreviewParameterProvider<TemplateArgsState> {

    override val values = sequenceOf(
        TemplateArgsState.PREVIEW,
        TemplateArgsState.PREVIEW.copy(templateId = ""),
        TemplateArgsState.PREVIEW.copy(
            templateId = "an-identifier-long-enough-to-wrap-onto-a-second-and-then-a-third-line",
        ),
    )
}
