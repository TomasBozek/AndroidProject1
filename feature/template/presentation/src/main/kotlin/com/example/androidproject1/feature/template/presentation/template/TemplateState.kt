package com.example.androidproject1.feature.template.presentation.template

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Immutable
data class TemplateState(
    // TODO: replace with this screen's real state.
    val title: String,
    val counter: Int,
) {

    companion object {

        val PREVIEW = TemplateState(
            title = "Template",
            counter = 1,
        )
    }
}

/**
 * The states this screen is drawn in — for the preview, and so for 4.1's goldens.
 *
 * Three, because one preview only ever shows the state you were thinking about. Empty is where
 * layouts collapse and long text is where they overflow, and both are cheaper to see here than in
 * a report. Replace these with the states this screen actually has; keep the count.
 */
class TemplateStatePreviews : PreviewParameterProvider<TemplateState> {

    override val values = sequenceOf(
        TemplateState.PREVIEW,
        TemplateState.PREVIEW.copy(title = "", counter = 0),
        TemplateState.PREVIEW.copy(
            title = "A title long enough to wrap onto a second line and then onto a third",
            counter = 9_999,
        ),
    )
}
