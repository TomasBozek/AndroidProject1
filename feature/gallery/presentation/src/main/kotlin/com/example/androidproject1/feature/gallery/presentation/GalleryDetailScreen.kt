package com.example.androidproject1.feature.gallery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * One component, in every state worth seeing.
 *
 * Each variant is labelled with the state it shows, so this doubles as the check that a component
 * still has all of them: a missing state here is a missing state in the component.
 */
@Composable
fun GalleryDetailScreen(
    state: GalleryDetailState,
    onEvent: (GalleryDetailEvent) -> Unit,
) {
    val entry = galleryEntry(state.componentId)
    AppScaffold(
        topBar = {
            AppTopBar(
                title = state.name,
                onNavigateUp = { onEvent(GalleryDetailEvent.NavigateUpClicked) },
            )
        },
        contentPadding = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                AppTheme.spacing.inset.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            item(key = "summary") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
                ) {
                    AppText(text = state.group.uppercase(), role = TextRole.LabelSmall)
                    AppText(text = state.summary, role = TextRole.BodyLarge)
                }
            }
            entry?.variants?.forEach { variant ->
                item(key = variant.label) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
                    ) {
                        AppText(text = variant.label, role = TextRole.LabelSmall)
                        // The demo ground is the sunken surface, which is what the system says a
                        // key or a list sits on. It also has to be: a neutral button is the raised
                        // surface itself, so on a card it would be white on white and invisible.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppTheme.shapes.lg)
                                .background(AppTheme.colors.surfaceSunken)
                                .padding(AppTheme.spacing.inset.lg),
                            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
                        ) {
                            variant.content()
                        }
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    GalleryDetailScreen(
        state = GalleryDetailState.PREVIEW,
    ) {}
}
