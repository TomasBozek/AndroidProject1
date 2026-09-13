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
import com.example.androidproject1.core.ui.component.AppImage
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailState

/**
 * The top of the detail: the picture — its placeholder state offline, which is the honest one —
 * the owner with a status dot toned by the item's condition, and a tag per tag. Its own file
 * because the screen composes it above three sections and a screen file holds only the screen.
 */
@Composable
fun ItemDetailHeader(
    item: Item,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        AppImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppTheme.density.listRowHeight * IMAGE_ROWS)
                .testTag("inventoryDetail_imageValue"),
        )
        Row(
            modifier = Modifier.padding(horizontal = AppTheme.spacing.inset.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
        ) {
            AppAvatar(
                name = item.owner,
                status = item.condition.tone(),
                statusDescription = stringResource(item.condition.labelRes()),
                modifier = Modifier.testTag("inventoryDetail_ownerValue"),
            )
            Column {
                AppText(text = item.owner, role = TextRole.Body)
                AppText(text = stringResource(R.string.inventory_detail_owner_label), role = TextRole.Secondary)
            }
        }
        if (item.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = AppTheme.spacing.inset.lg),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
            ) {
                item.tags.forEach { tag ->
                    AppTag(label = stringResource(tag.labelRes()), tone = TagTone.Info)
                }
            }
        }
    }
}

private fun ItemCondition.tone() = when (this) {
    ItemCondition.New -> TagTone.Positive
    ItemCondition.Good -> TagTone.Neutral
    ItemCondition.Worn -> TagTone.Warning
}

/** The picture is four list rows tall — a size in the density's own units, not a literal. */
private const val IMAGE_ROWS = 4

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemDetailHeader(item = InventoryDetailState.PREVIEW.item!!)
}
