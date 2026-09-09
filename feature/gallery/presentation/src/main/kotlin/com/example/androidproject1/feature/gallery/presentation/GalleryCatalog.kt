package com.example.androidproject1.feature.gallery.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.component.AppAccordion
import com.example.androidproject1.core.ui.component.AppAvatar
import com.example.androidproject1.core.ui.component.AppAvatarPhoto
import com.example.androidproject1.core.ui.component.AppBadge
import com.example.androidproject1.core.ui.component.AppBottomActionBar
import com.example.androidproject1.core.ui.component.AppBottomNav
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppFab
import com.example.androidproject1.core.ui.component.AppFormField
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppImage
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppNavRail
import com.example.androidproject1.core.ui.component.AppProgress
import com.example.androidproject1.core.ui.component.AppRadio
import com.example.androidproject1.core.ui.component.AppRadioGroup
import com.example.androidproject1.core.ui.component.AppSearchField
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppSlider
import com.example.androidproject1.core.ui.component.AppSpinner
import com.example.androidproject1.core.ui.component.AppStatusDot
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppTabs
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.AppToast
import com.example.androidproject1.core.ui.component.AppToolbar
import com.example.androidproject1.core.ui.component.AppTooltip
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.ButtonSize
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.component.NavItem
import com.example.androidproject1.core.ui.component.TabItem
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.component.ToastTone
import com.example.androidproject1.core.ui.theme.AppTheme
import kotlin.math.roundToInt

/** One rendered state of a component, with the label that says which state it is. */
@Immutable
data class GalleryVariant(val label: String, val content: @Composable () -> Unit)

/**
 * One component in the gallery.
 *
 * The demos live here rather than in `:core:ui` on purpose: the gallery is a feature, and a feature
 * composes components. Keeping them here means `:core:ui` ships no sample code.
 */
@Immutable
data class GalleryEntry(
    val id: String,
    val name: String,
    val group: String,
    val summary: String,
    val variants: List<GalleryVariant>,
)

private fun entry(
    id: String,
    name: String,
    group: String,
    summary: String,
    vararg variants: Pair<String, @Composable () -> Unit>,
) = GalleryEntry(id, name, group, summary, variants.map { GalleryVariant(it.first, it.second) })

/**
 * Every component in `:core:ui`, with the states worth looking at.
 *
 * This is the running counterpart to the design system's component document: if something is not
 * here, a screen should not be inventing it.
 */
val galleryCatalog: List<GalleryEntry> = listOf(
    entry(
        "text", "AppText", "Content",
        "Text asked for by role — the role decides both size and colour.",
        "Display large" to { AppText("1 248,00", role = TextRole.DisplayLarge) },
        "Display" to { AppText("Order 12 — Terrace", role = TextRole.Display) },
        "Title" to { AppText("Drinks · Hot drinks", role = TextRole.Title) },
        "Body" to { AppText("The default size for list rows and dialog copy.") },
        "Label" to { AppText("Payment method", role = TextRole.Label) },
        "Label small" to { AppText("OPEN ORDERS", role = TextRole.LabelSmall) },
        "Numeric (tabular)" to { AppText("118,00", role = TextRole.Numeric) },
        "Secondary" to { AppText("Opened 19:24 · Jana N.", role = TextRole.Secondary) },
        "Tertiary — missing value" to { AppText("—", role = TextRole.Tertiary) },
    ),
    entry(
        "button", "AppButton", "Action",
        "Six kinds, three sizes. The press shortens the key's edge so it travels 3 dp.",
        "Confirm" to { AppButton("Pay", {}) },
        "Destructive" to { AppButton("Void", {}, kind = ButtonKind.Destructive) },
        "Info" to { AppButton("Card", {}, kind = ButtonKind.Info) },
        "Neutral" to { AppButton("Cash", {}, kind = ButtonKind.Neutral) },
        "Outline" to { AppButton("Back", {}, kind = ButtonKind.Outline) },
        "Ghost" to { AppButton("More options", {}, kind = ButtonKind.Ghost) },
        "Disabled — loses the body" to { AppButton("Unavailable", {}, enabled = false) },
        "Loading — label and width hold" to { AppButton("Processing", {}, loading = true) },
        "Small 40 (mouse only)" to { AppButton("Small", {}, size = ButtonSize.Small) },
        "Large 64" to { AppButton("Large", {}, size = ButtonSize.Large) },
    ),
    entry(
        "iconbutton",
        "AppIconButton",
        "Action",
        "An icon that does something. The description is required, for the reader and the test.",
        "Default" to { AppIconButton(Icons.Filled.Add, "Add item", {}) },
        "Disabled" to { AppIconButton(Icons.Filled.Add, "Add item", {}, enabled = false) },
    ),
    entry(
        "fab",
        "AppFab",
        "Action",
        "One per screen, for the thing the screen exists to do. Travels like every other key.",
        "Icon only" to { AppFab(Icons.Filled.Add, "New order", {}) },
        "Extended" to { AppFab(Icons.Filled.Add, "New order", {}, label = "New order") },
    ),
    entry(
        "textfield", "AppTextField", "Form",
        "A sunken field. An error always carries text — colour alone is invisible to too many.",
        "Empty with placeholder" to {
            Demo("") { value, onChange ->
                AppTextField(value, onChange, label = "Search", placeholder = "Product name")
            }
        },
        "Filled with helper" to {
            Demo("Pilsner") { value, onChange ->
                AppTextField(value, onChange, label = "Item", helperText = "From the catalogue")
            }
        },
        "Numeric — tabular figures" to {
            Demo("12") { value, onChange ->
                AppTextField(value, onChange, label = "Quantity", numeric = true)
            }
        },
        "Password" to {
            Demo("hunter2") { value, onChange ->
                AppTextField(value, onChange, label = "PIN", password = true)
            }
        },
        "Error" to {
            Demo("abc") { value, onChange ->
                AppTextField(value, onChange, label = "Code", errorText = "No product with this code")
            }
        },
        "Disabled" to { AppTextField("Locked", {}, label = "Till", enabled = false) },
    ),
    entry(
        "searchfield",
        "AppSearchField",
        "Form",
        "Glass on the left, clearing on the right — the most common thing to undo.",
        "Empty" to {
            Demo("") { value, onChange ->
                AppSearchField(value, onChange, placeholder = "Search products")
            }
        },
        "With a query" to { Demo("Pilsner") { value, onChange -> AppSearchField(value, onChange) } },
    ),
    entry(
        "select",
        "AppSelect",
        "Form",
        "One choice from more than a handful. Below five, prefer radio or segmented.",
        "Selected" to {
            Demo(1) { selected, onSelect ->
                AppSelect(listOf("Cash", "Card", "Voucher", "Invoice", "Split"), selected, onSelect)
            }
        },
        "Nothing chosen" to {
            Demo(-1) { selected, onSelect ->
                AppSelect(listOf("A", "B"), selected, onSelect, placeholder = "Choose a price list")
            }
        },
    ),
    entry(
        "checkbox",
        "AppCheckbox",
        "Form",
        "The whole row is the target. Indeterminate belongs to a group toggle and nowhere else.",
        "On" to { CheckboxDemo(CheckState.On, "Print receipt") },
        "Off" to { CheckboxDemo(CheckState.Off, "Email receipt") },
        // Tapping resolves it: a group toggle that is partly on becomes fully on.
        "Indeterminate" to { CheckboxDemo(CheckState.Indeterminate, "All items") },
        "Disabled" to { AppCheckbox(CheckState.Off, {}, "Unavailable", enabled = false) },
    ),
    entry(
        "radio",
        "AppRadio",
        "Form",
        "Exactly one of a group. Above five options this is the wrong control.",
        "A group" to {
            Demo(0) { selected, onSelect ->
                AppRadioGroup {
                    AppRadio(selected == 0, { onSelect(0) }, "Cash")
                    AppRadio(selected == 1, { onSelect(1) }, "Card")
                    AppRadio(false, {}, "Voucher", enabled = false)
                }
            }
        },
    ),
    entry(
        "switch",
        "AppSwitch",
        "Form",
        "An immediate change with no confirmation — so never for a destructive choice.",
        "On" to {
            Demo(true) { checked, onChange -> AppSwitch(checked, onChange, "Print receipt automatically") }
        },
        "With supporting text" to {
            Demo(false) { checked, onChange ->
                AppSwitch(checked, onChange, "Sounds", supporting = "Feedback on every key press")
            }
        },
        "Disabled" to { AppSwitch(false, {}, "Unavailable", enabled = false) },
    ),
    entry(
        "segmented",
        "AppSegmented",
        "Form",
        "Two to four short options that switch immediately.",
        "Three options" to {
            Demo(0) { selected, onSelect -> AppSegmented(listOf("Today", "Week", "Month"), selected, onSelect) }
        },
        "Two options" to {
            Demo(1) { selected, onSelect -> AppSegmented(listOf("Percent", "Amount"), selected, onSelect) }
        },
    ),
    entry(
        "stepper",
        "AppStepper",
        "Form",
        "Quantity, one at a time. Fixed width, so the row does not shift from 9 to 10.",
        "One" to { Demo(1) { value, onChange -> AppStepper(value, onChange) } },
        "Twelve" to { Demo(12) { value, onChange -> AppStepper(value, onChange) } },
        "At the floor" to { Demo(0) { value, onChange -> AppStepper(value, onChange) } },
    ),
    entry(
        "slider",
        "AppSlider",
        "Form",
        "Continuous values only. Never a price — nearly right is wrong.",
        "With a value label" to {
            Demo(0.4f) { value, onChange ->
                AppSlider(
                    value,
                    onChange,
                    label = "Brightness",
                    valueLabel = "${(value * 100).roundToInt()} %",
                )
            }
        },
    ),
    entry(
        "formfield",
        "AppFormField",
        "Form",
        "Label above, help or error below. A required field says the word.",
        "Required" to {
            AppFormField("Payment method", required = true) {
                Demo(0) { selected, onSelect -> AppSegmented(listOf("Cash", "Card"), selected, onSelect) }
            }
        },
        "Error" to {
            AppFormField("Discount", errorText = "Above the limit for this role") {
                Demo(2) { selected, onSelect ->
                    AppSegmented(listOf("10 %", "20 %", "50 %"), selected, onSelect)
                }
            }
        },
    ),
    entry(
        "tag", "AppTag", "Status",
        "A state you cannot press. The label always carries the meaning, not just the colour.",
        "Paid" to { AppTag("Paid", tone = TagTone.Paid) },
        "Open" to { AppTag("Open", tone = TagTone.Open) },
        "Void" to { AppTag("Void", tone = TagTone.Void) },
        "Info" to { AppTag("Card", tone = TagTone.Info) },
        "Neutral" to { AppTag("No VAT") },
    ),
    entry(
        "statusdot",
        "AppStatusDot",
        "Status",
        "A dot and a word. The dot never stands alone.",
        "Paid" to { AppStatusDot("Paid", tone = TagTone.Paid) },
        "Open" to { AppStatusDot("Open", tone = TagTone.Open) },
        "Void" to { AppStatusDot("Void", tone = TagTone.Void) },
    ),
    entry(
        "badge",
        "AppBadge",
        "Status",
        "A count over an icon or in a tab. Above 99 it becomes 99+.",
        "Small" to { AppBadge(3) },
        "Two digits" to { AppBadge(42) },
        "Over the cap" to { AppBadge(128) },
    ),
    entry(
        "avatar",
        "AppAvatar",
        "Status",
        "Initials, not a photograph. The colour is derived from the id, so it needs no storage.",
        "Two names" to { AppAvatar("Jana Nováková") },
        "Another person" to { AppAvatar("Petr Svoboda") },
        "One name" to { AppAvatar("Root") },
    ),
    entry(
        "avatarPhoto",
        "AppAvatarPhoto",
        "Status",
        "The photo if there is one, the initials if there is not. One rule, in one place.",
        "No photo yet" to { AppAvatarPhoto("Jana Nováková") },
        "Photo unavailable" to {
            AppAvatarPhoto("Jana Nováková", photo = "https://example.invalid/avatar.jpg")
        },
    ),
    entry(
        "spinner",
        "AppSpinner",
        "Status",
        "Waiting for under two seconds. Longer than that needs a skeleton.",
        "Default" to { AppSpinner() },
    ),
    entry(
        "progress",
        "AppProgress",
        "Status",
        "Determinate progress. Past ten seconds the label stops being optional.",
        "Bare" to { AppProgress(0.35f) },
        "Labelled" to { AppProgress(0.8f, label = "Uploading receipts") },
    ),
    entry(
        "skeleton",
        "AppSkeleton",
        "Status",
        "The outline of what is loading. It copies the target's shape and does not blink.",
        "Text line" to { AppSkeleton() },
        "A list row" to { AppSkeleton(height = AppTheme.density.listRowHeight) },
    ),
    entry(
        "image",
        "AppImage",
        "Content",
        "A remote image with its two real states. The one place that knows Coil exists.",
        "Failed to load" to {
            AppImage(
                model = "https://example.invalid/product.jpg",
                contentDescription = null,
                // A thumbnail the height of a list row, which is where one usually sits.
                modifier = Modifier.size(AppTheme.density.listRowHeight),
            )
        },
    ),
    entry(
        "card",
        "AppCard",
        "Content",
        "A raised surface. Three levels exist and no more — the fourth is a dialog.",
        "Default" to {
            AppCard {
                AppText("Notifications", role = TextRole.Title)
                AppText("Turn these on to hear about orders.", role = TextRole.Secondary)
            }
        },
    ),
    entry(
        "divider",
        "AppDivider",
        "Content",
        "A hairline where a gap is not enough. Decorative, so it is allowed to be quiet.",
        "Between two lines" to {
            AppText("Above")
            AppDivider()
            AppText("Below")
        },
    ),
    entry(
        "listitem",
        "AppListItem",
        "Content",
        "The base of every list. Its height is the density's row height.",
        "Clickable" to { AppListItem("Pilsner Urquell", supporting = "0,5 l", onClick = {}) },
        "With a trailing tag" to {
            AppListItem(
                "Camera",
                supporting = "android.permission.CAMERA",
                trailing = { AppTag("Granted", tone = TagTone.Paid) },
            )
        },
    ),
    entry(
        "descriptionlist",
        "AppDescriptionList",
        "Content",
        "Label left, value right. A missing value is a dash, never a blank or a zero.",
        "A receipt detail" to {
            AppDescriptionList(
                listOf(
                    DescriptionRow("Opened", "19:24"),
                    DescriptionRow("Server", "Jana N."),
                    DescriptionRow("Total", "1 248,00", numeric = true),
                    DescriptionRow("Tip", null),
                ),
            )
        },
    ),
    entry(
        "sectionheader",
        "AppSectionHeader",
        "Content",
        "A heading inside a screen, with at most one action.",
        "Plain" to { AppSectionHeader("OPEN ORDERS") },
        "With an action" to { AppSectionHeader("DEVICES", actionLabel = "Add", onAction = {}) },
    ),
    entry(
        "emptystate",
        "AppEmptyState",
        "Content",
        "Why it is empty, and the first step out. Never just \"nothing here\".",
        "With an action" to {
            AppEmptyState(
                "No open orders",
                "Orders you start will show up here until they are paid.",
                actionLabel = "New order",
                onAction = {},
            )
        },
    ),
    entry(
        "accordion",
        "AppAccordion",
        "Content",
        "Collapsible content — for settings, never for an operational screen.",
        "Expanded" to {
            Demo(true) { expanded, onChange ->
                AppAccordion("Advanced", expanded = expanded, onToggle = { onChange(!expanded) }) {
                    AppText("Settings most people never open.", role = TextRole.Secondary)
                }
            }
        },
        "Collapsed" to {
            Demo(false) { expanded, onChange ->
                AppAccordion("Diagnostics", expanded = expanded, onToggle = { onChange(!expanded) }) {
                    AppText("Logs, versions, the device id.", role = TextRole.Secondary)
                }
            }
        },
    ),
    entry(
        "toast",
        "AppToast",
        "Overlay",
        "Something that already happened. Above every layer; only an error waits to be read.",
        "Success" to { AppToast("Order sent to the kitchen", tone = ToastTone.Success) },
        "With an action" to { AppToast("Item moved to order 14", actionLabel = "Undo", onAction = {}) },
        "Error" to { AppToast("Printer not responding", tone = ToastTone.Error) },
    ),
    entry(
        "tooltip",
        "AppTooltip",
        "Overlay",
        "A label for an icon that has none. Never anything you cannot continue without.",
        "Default" to { AppTooltip("Split the bill") },
    ),
    entry(
        "topbar",
        "AppTopBar",
        "Navigation",
        "Where I am, and at most three things I can do. A tab root carries no up arrow.",
        "Tab root" to { AppTopBar("Settings") },
        "With up" to { AppTopBar("Permissions", onNavigateUp = {}) },
    ),
    entry(
        "tabs",
        "AppTabs",
        "Navigation",
        "Switching content inside a screen. Takes a longer label and a count than segmented.",
        "With a badge" to {
            Demo(0) { selected, onSelect ->
                AppTabs(listOf(TabItem("Open", badge = 4), TabItem("Paid"), TabItem("Void")), selected, onSelect)
            }
        },
    ),
    entry(
        "bottomnav",
        "AppBottomNav",
        "Navigation",
        "At most four destinations. The selected one always shows its label.",
        "Three destinations" to {
            Demo(0) { selected, onSelect ->
                AppBottomNav(
                    listOf(
                        NavItem("Home", Icons.Filled.Home),
                        NavItem("Orders", Icons.AutoMirrored.Filled.List, badge = 3),
                        NavItem("Settings", Icons.Filled.Settings),
                    ),
                    selected,
                    onSelect,
                )
            }
        },
    ),
    entry(
        "navrail",
        "AppNavRail",
        "Navigation",
        "The same destinations, down the side, for the wider classes.",
        "Two destinations" to {
            Demo(1) { selected, onSelect ->
                AppNavRail(
                    listOf(
                        NavItem("Home", Icons.Filled.Home),
                        NavItem("Orders", Icons.AutoMirrored.Filled.List, badge = 3),
                    ),
                    selected,
                    onSelect,
                )
            }
        },
    ),
    entry(
        "toolbar",
        "AppToolbar",
        "Navigation",
        "Appears when something is selected. Inverted, because it is a different mode.",
        "Three selected" to {
            AppToolbar("3 selected") {
                AppText("Move", role = TextRole.Label)
            }
        },
    ),
    entry(
        "bottomactionbar",
        "AppBottomActionBar",
        "Navigation",
        "A screen's one main action, anchored. Total left, action right and wider.",
        "With a total" to {
            AppBottomActionBar(label = "TOTAL", value = "1 248,00", actionLabel = "Pay", onAction = {})
        },
        "Action only" to { AppBottomActionBar(actionLabel = "Continue", onAction = {}) },
    ),
)

/**
 * The screen's half of a stateless component, for a demo.
 *
 * Every component here takes its value and hands changes back — a screen holds the state. A demo
 * that passes a constant and an empty callback is therefore something to look at and not to try:
 * tapping it changes nothing. This holds the value for it, so a checkbox in the gallery toggles the
 * way it does on a real screen. Only the disabled variants stay constant, which is their point.
 */
@Composable
private fun <T> Demo(
    initial: T,
    content: @Composable (value: T, onChange: (T) -> Unit) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    content(value) { value = it }
}

@Composable
private fun CheckboxDemo(initial: CheckState, label: String) = Demo(initial) { checked, onChange ->
    AppCheckbox(checked, { onChange(if (it) CheckState.On else CheckState.Off) }, label)
}

/** Everything the gallery list needs, without the composables. */
fun galleryItems(): List<GalleryItem> = galleryCatalog.map {
    GalleryItem(id = it.id, name = it.name, group = it.group, summary = it.summary)
}

fun galleryEntry(id: String): GalleryEntry? = galleryCatalog.firstOrNull { it.id == id }
