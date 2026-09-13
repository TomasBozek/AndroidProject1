package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppStepProgress
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.component.ItemBasicsStep
import com.example.androidproject1.feature.inventory.presentation.component.ItemQuantityStep
import com.example.androidproject1.feature.inventory.presentation.component.ItemReviewStep
import com.example.androidproject1.feature.inventory.presentation.component.ItemTagsStep
import com.example.androidproject1.service.core.ui.form.DiscardBackHandler

@Composable
fun InventoryEditorScreen(
    state: InventoryEditorState,
    onEvent: (InventoryEditorEvent) -> Unit,
) {
    AppScaffold(
        screenId = "InventoryEditorScreen",
        topBar = {
            AppTopBar(
                title = stringResource(
                    if (state.editing) R.string.inventory_editor_title_edit else R.string.inventory_editor_title_new,
                ),
                onNavigateUp = { onEvent(InventoryEditorEvent.NavigateUpClicked) },
                navigateUpTestTag = "inventoryEditor_upButton",
            )
        },
    ) {
        // Only the first step guards the gesture: past it, leaving pops the whole editor the way
        // the up arrow does too — see InventoryEditorViewModel.back().
        DiscardBackHandler(dirty = state.step == InventoryEditorState.STEP_BASICS && state.isDirty) {
            onEvent(InventoryEditorEvent.BackRequested)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.lg),
        ) {
            AppStepProgress(
                steps = InventoryEditorState.STEP_COUNT,
                currentStep = state.step,
                label = stringResource(
                    R.string.inventory_editor_step_label,
                    state.step + 1,
                    InventoryEditorState.STEP_COUNT,
                ),
                modifier = Modifier.testTag("inventoryEditor_stepProgress"),
            )

            when (state.step) {
                InventoryEditorState.STEP_QUANTITY -> ItemQuantityStep(state = state, onEvent = onEvent)
                InventoryEditorState.STEP_TAGS -> ItemTagsStep(state = state, onEvent = onEvent)
                InventoryEditorState.STEP_REVIEW -> ItemReviewStep(state = state)
                else -> ItemBasicsStep(state = state, onEvent = onEvent)
            }

            AppButton(
                label = stringResource(
                    if (state.step == InventoryEditorState.STEP_REVIEW) {
                        R.string.inventory_editor_save
                    } else {
                        R.string.inventory_editor_next
                    },
                ),
                onClick = { onEvent(InventoryEditorEvent.NextClicked) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventoryEditor_nextButton"),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    InventoryEditorScreen(state = InventoryEditorState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun QuantityStepPreview() = ThemedScreenPreview {
    InventoryEditorScreen(state = InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_QUANTITY)) {}
}

@ScreenPreview
@Composable
private fun TagsStepPreview() = ThemedScreenPreview {
    InventoryEditorScreen(state = InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_TAGS)) {}
}

@ScreenPreview
@Composable
private fun ReviewStepPreview() = ThemedScreenPreview {
    InventoryEditorScreen(state = InventoryEditorState.PREVIEW.copy(step = InventoryEditorState.STEP_REVIEW)) {}
}
