package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppBottomNav
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppMenu
import com.example.androidproject1.core.ui.component.AppNavRail
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.MenuItem
import com.example.androidproject1.core.ui.component.NavItem
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.core.ui.theme.SizeClass
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.component.ItemDetailHeader
import com.example.androidproject1.feature.inventory.presentation.component.ItemHistorySection
import com.example.androidproject1.feature.inventory.presentation.component.ItemOverviewSection
import com.example.androidproject1.feature.inventory.presentation.component.historyEntries

@Composable
fun InventoryDetailScreen(
    state: InventoryDetailState,
    onEvent: (InventoryDetailEvent) -> Unit,
) {
    val item = state.item
    AppScaffold(
        screenId = "InventoryDetailScreen",
        topBar = {
            AppTopBar(
                title = item?.name ?: stringResource(R.string.inventory_detail_title),
                onNavigateUp = { onEvent(InventoryDetailEvent.NavigateUpClicked) },
                navigateUpTestTag = "inventoryDetail_upButton",
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    AppMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        items = listOf(
                            MenuItem(
                                label = stringResource(R.string.inventory_detail_edit),
                                testTag = "inventoryDetail_editItem",
                            ) {
                                menuExpanded = false
                                onEvent(InventoryDetailEvent.EditClicked)
                            },
                            MenuItem(
                                label = stringResource(R.string.inventory_detail_delete_confirm),
                                destructive = true,
                                testTag = "inventoryDetail_deleteItem",
                            ) {
                                menuExpanded = false
                                onEvent(InventoryDetailEvent.DeleteClicked)
                            },
                        ),
                        anchor = {
                            AppIconButton(
                                icon = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.inventory_detail_menu),
                                onClick = { menuExpanded = true },
                                modifier = Modifier.testTag("inventoryDetail_menuButton"),
                            )
                        },
                    )
                },
            )
        },
        contentPadding = false,
    ) {
        if (item == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.inset.xl)
                    .testTag("inventoryDetail_skeleton"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
            ) {
                repeat(SKELETON_ROWS) { AppSkeleton(height = AppTheme.density.listRowHeight) }
            }
            return@AppScaffold
        }

        // A badge on any section with something in it — the count of what a tap would show.
        val sections = listOf(
            NavItem(stringResource(R.string.inventory_detail_section_overview), Icons.AutoMirrored.Filled.List),
            NavItem(
                stringResource(R.string.inventory_detail_section_notes),
                Icons.Filled.Edit,
                badge = if (item.notes.isBlank()) 0 else 1,
            ),
            NavItem(
                stringResource(R.string.inventory_detail_section_history),
                Icons.Filled.DateRange,
                badge = item.historyEntries().size,
            ),
        )
        val content: @Composable (Modifier) -> Unit = { modifier ->
            Column(
                modifier = modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.lg),
            ) {
                ItemDetailHeader(item = item)
                when (state.selectedSection) {
                    InventoryDetailState.SECTION_NOTES -> AppText(
                        text = item.notes.ifBlank { stringResource(R.string.inventory_detail_notes_empty) },
                        role = TextRole.Body,
                        modifier = Modifier
                            .padding(horizontal = AppTheme.spacing.inset.lg)
                            .testTag("inventoryDetail_notesValue"),
                    )

                    InventoryDetailState.SECTION_HISTORY -> ItemHistorySection(item = item)
                    else -> ItemOverviewSection(item = item)
                }
            }
        }

        // The first screen that reads the width class itself: a compact width switches sections
        // with a bottom bar, anything wider with a rail — the same items, only where they sit.
        if (AppTheme.density.sizeClass == SizeClass.Compact) {
            Column(modifier = Modifier.fillMaxSize()) {
                content(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                AppBottomNav(
                    items = sections,
                    selectedIndex = state.selectedSection,
                    onSelect = { onEvent(InventoryDetailEvent.SectionSelected(it)) },
                    modifier = Modifier.testTag("inventoryDetail_sectionTab"),
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavRail(
                    items = sections,
                    selectedIndex = state.selectedSection,
                    onSelect = { onEvent(InventoryDetailEvent.SectionSelected(it)) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("inventoryDetail_sectionTab"),
                )
                Box(modifier = Modifier.weight(1f)) {
                    content(Modifier.fillMaxSize())
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 5

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    InventoryDetailScreen(state = InventoryDetailState.PREVIEW) {}
}

@ScreenPreview
@Composable
private fun HistoryPreview() = ThemedScreenPreview {
    InventoryDetailScreen(
        state = InventoryDetailState.PREVIEW.copy(selectedSection = InventoryDetailState.SECTION_HISTORY),
    ) {
    }
}

@ScreenPreview
@Composable
private fun LoadingPreview() = ThemedScreenPreview {
    InventoryDetailScreen(state = InventoryDetailState.PREVIEW.copy(item = null)) {}
}

/** The one screen that responds to width, so it carries the width it responds at (see ScreenPreview). */
@Preview(name = "Tablet", showBackground = true, device = "spec:width=840dp,height=1000dp,dpi=240")
@Composable
private fun TabletPreview() = ThemedScreenPreview {
    InventoryDetailScreen(state = InventoryDetailState.PREVIEW) {}
}
