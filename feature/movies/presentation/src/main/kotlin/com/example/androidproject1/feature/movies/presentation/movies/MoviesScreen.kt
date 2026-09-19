package com.example.androidproject1.feature.movies.presentation.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.movies.presentation.R
import com.example.androidproject1.feature.movies.presentation.component.MoviesHeadline

@Composable
fun MoviesScreen(
    state: MoviesState,
    onEvent: (MoviesEvent) -> Unit,
) {
    // A screen composes components from :core:ui and nothing else — no Material widget, no colour,
    // no size. AppScaffold is the shell: base surface, system insets, an optional top bar.
    AppScaffold(screenId = "MoviesScreen") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Every element a test or a screen reader has to reach carries a tag, named
            // `<screenStem>_<element>` from the closed vocabulary in CLAUDE.md. One id serves the
            // test, the screen reader and the design registry, so there is one to keep in sync.
            //
            // A composable that is not the screen itself lives in this module's `component/`,
            // one to a file with its own preview — never inline here. See MoviesHeadline.
            MoviesHeadline(
                text = state.title.ifEmpty { stringResource(R.string.movies_title) },
                modifier = Modifier.testTag("movies_titleValue"),
            )
            AppText(
                text = stringResource(R.string.movies_counter, state.counter),
                modifier = Modifier.testTag("movies_counterValue"),
            )
            AppButton(
                label = stringResource(R.string.movies_increment),
                onClick = { onEvent(MoviesEvent.IncrementClicked) },
                modifier = Modifier.testTag("movies_incrementButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(MoviesStatePreviews::class) state: MoviesState,
) = ThemedScreenPreview {
    MoviesScreen(
        state = state,
    ) {}
}
