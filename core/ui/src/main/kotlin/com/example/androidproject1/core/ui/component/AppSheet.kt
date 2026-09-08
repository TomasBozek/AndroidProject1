package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The default way to open a detail or a choice on a phone.
 *
 * The handle is always drawn, even when the sheet cannot be dragged — it is what says "this came
 * from below and goes back down". Only one overlay at a time: a sheet over a sheet replaces the
 * first rather than stacking, and back closes both.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = AppTheme.colors.surfaceRaised,
        scrimColor = AppTheme.colors.scrim.copy(alpha = AppTheme.colors.scrimAlpha),
        shape = AppTheme.shapes.sheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.spacing.inset.md),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(AppTheme.shapes.pill)
                        .background(AppTheme.colors.borderStrong),
                )
            }
        },
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = AppTheme.spacing.inset.xl,
                    end = AppTheme.spacing.inset.xl,
                    bottom = AppTheme.spacing.inset.xl,
                ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            if (title != null) AppText(text = title, role = TextRole.TitleLarge)
            content()
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // A sheet draws in its own window; this shows the content it would carry.
    AppText(text = "Payment method", role = TextRole.TitleLarge)
    AppRadio(selected = true, onSelect = {}, label = "Cash")
    AppRadio(selected = false, onSelect = {}, label = "Card")
}
