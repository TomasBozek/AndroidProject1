package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppAvatar
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppFormField
import com.example.androidproject1.core.ui.component.AppImage
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorEvent
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorState

/**
 * The editor's third step: the tags under a master checkbox, the owner beside their avatar, a
 * picture and the notes. The master shows [CheckState.Indeterminate] while some tags are ticked,
 * which is the one state a plain checkbox cannot say.
 */
@Composable
fun ItemTagsStep(
    state: InventoryEditorState,
    onEvent: (InventoryEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        AppFormField(
            label = stringResource(R.string.inventory_editor_tags_label),
            modifier = Modifier.testTag("inventoryEditor_tagsGroup"),
        ) {
            AppCheckbox(
                checked = state.allTagsState,
                onCheckedChange = { onEvent(InventoryEditorEvent.AllTagsChanged(it)) },
                label = stringResource(R.string.inventory_editor_all_tags_label),
                modifier = Modifier.testTag("inventoryEditor_allTagsCheckbox"),
            )
            ItemTag.entries.forEach { tag ->
                AppCheckbox(
                    checked = if (tag in state.tags) CheckState.On else CheckState.Off,
                    onCheckedChange = { onEvent(InventoryEditorEvent.TagChanged(tag, it)) },
                    label = stringResource(tag.labelRes()),
                    modifier = Modifier.padding(start = AppTheme.spacing.inset.lg),
                )
            }
        }

        val owners = InventoryEditorState.OWNERS
        AppFormField(
            label = stringResource(R.string.inventory_editor_owner_label),
            modifier = Modifier.testTag("inventoryEditor_ownerField"),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
            ) {
                AppAvatar(name = state.owner)
                AppSelect(
                    options = owners,
                    selectedIndex = owners.indexOf(state.owner).coerceAtLeast(0),
                    onSelect = { onEvent(InventoryEditorEvent.OwnerSelected(owners[it])) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AppTextField(
            value = state.imageUrl,
            onValueChange = { onEvent(InventoryEditorEvent.ImageUrlChanged(it)) },
            label = stringResource(R.string.inventory_editor_image_label),
            helperText = stringResource(R.string.inventory_editor_image_helper),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventoryEditor_imageField"),
        )
        if (state.imageUrl.isNotBlank()) {
            AppImage(
                model = state.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppTheme.density.listRowHeight * IMAGE_ROWS),
            )
        }

        AppTextField(
            value = state.notes,
            onValueChange = { onEvent(InventoryEditorEvent.NotesChanged(it)) },
            label = stringResource(R.string.inventory_editor_notes_label),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventoryEditor_notesField"),
        )
    }
}

/** The preview is three list rows tall — a size in the density's own units, not a literal. */
private const val IMAGE_ROWS = 3

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemTagsStep(state = InventoryEditorState.PREVIEW, onEvent = {})
}
