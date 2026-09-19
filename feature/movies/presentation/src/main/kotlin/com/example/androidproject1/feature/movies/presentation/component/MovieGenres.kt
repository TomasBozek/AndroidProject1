package com.example.androidproject1.feature.movies.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A movie's genres as a row of tags that wraps: TMDB lists up to eight, and eight do not fit on
 * one line of a phone. Draws nothing for an empty list, so the detail above it closes up.
 */
@Composable
fun MovieGenres(
    genres: List<String>,
    modifier: Modifier = Modifier,
) {
    if (genres.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        genres.forEach { genre -> AppTag(label = genre) }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    MovieGenres(genres = listOf("Action", "Adventure", "Animation", "Comedy", "Crime", "Drama", "Family", "Fantasy"))
}
