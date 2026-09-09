package com.example.androidproject1.feature.template.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole

/**
 * The screen's headline.
 *
 * A placeholder, and also the shape every feature-local composable takes: anything that is not the
 * screen itself lives in this module's `component/` package, one to a file, with a
 * `@ComponentPreview` so it can be looked at on its own. A screen file holds the screen and its
 * previews and nothing else — `doctor.py` fails on a composable that stays behind.
 *
 * One `component/` per feature rather than one per screen: a second screen reaching for this needs
 * no move. The day a second *feature* wants it, it goes to `:core:ui` with
 * `create_component.py` instead — never copied.
 */
@Composable
fun TemplateHeadline(
    text: String,
    modifier: Modifier = Modifier,
) {
    AppText(
        text = text,
        role = TextRole.Title,
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    TemplateHeadline(text = "Template")
}
