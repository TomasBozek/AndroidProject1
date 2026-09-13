package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailState
import com.example.androidproject1.service.core.ui.format.LocalFormats
import java.time.LocalDate

/**
 * The detail's third section: what happened to the item, derived from what it records — nothing
 * here is stored as history. [historyEntries] is the one place that decides what counts, so the
 * badge on the section and the rows in it cannot disagree.
 */
@Composable
fun ItemHistorySection(
    item: Item,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    Column(modifier = modifier.testTag("inventoryDetail_historyList")) {
        item.historyEntries().forEach { entry ->
            when (entry) {
                is HistoryEntry.Acquired -> AppListItem(
                    headline = stringResource(R.string.inventory_detail_history_acquired),
                    supporting = formats.date(entry.on),
                )

                HistoryEntry.Insured -> AppListItem(
                    headline = stringResource(R.string.inventory_detail_history_insured),
                    supporting = stringResource(R.string.inventory_detail_history_insured_supporting),
                )
            }
        }
    }
}

sealed interface HistoryEntry {

    data class Acquired(val on: LocalDate) : HistoryEntry

    data object Insured : HistoryEntry
}

/** The entries in the order they happened; the section's badge is this list's size. */
fun Item.historyEntries(): List<HistoryEntry> = buildList {
    acquiredOn?.let { add(HistoryEntry.Acquired(it)) }
    if (insured) add(HistoryEntry.Insured)
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemHistorySection(item = InventoryDetailState.PREVIEW.item!!)
}
