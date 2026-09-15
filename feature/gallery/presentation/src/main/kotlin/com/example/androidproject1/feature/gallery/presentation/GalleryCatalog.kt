package com.example.androidproject1.feature.gallery.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.androidproject1.core.ui.component.AppAccordion
import com.example.androidproject1.core.ui.component.AppAvatar
import com.example.androidproject1.core.ui.component.AppAvatarPhoto
import com.example.androidproject1.core.ui.component.AppBadge
import com.example.androidproject1.core.ui.component.AppBanner
import com.example.androidproject1.core.ui.component.AppBottomActionBar
import com.example.androidproject1.core.ui.component.AppBottomNav
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppConfirmDialog
import com.example.androidproject1.core.ui.component.AppDateField
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppDialog
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppFab
import com.example.androidproject1.core.ui.component.AppFieldGroup
import com.example.androidproject1.core.ui.component.AppFieldGroupRow
import com.example.androidproject1.core.ui.component.AppFormField
import com.example.androidproject1.core.ui.component.AppIconButton
import com.example.androidproject1.core.ui.component.AppImage
import com.example.androidproject1.core.ui.component.AppIndeterminateProgress
import com.example.androidproject1.core.ui.component.AppLabelledDivider
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppMenu
import com.example.androidproject1.core.ui.component.AppNavRail
import com.example.androidproject1.core.ui.component.AppPager
import com.example.androidproject1.core.ui.component.AppProgress
import com.example.androidproject1.core.ui.component.AppRadio
import com.example.androidproject1.core.ui.component.AppRadioGroup
import com.example.androidproject1.core.ui.component.AppRangeSlider
import com.example.androidproject1.core.ui.component.AppScreenChrome
import com.example.androidproject1.core.ui.component.AppScrollShadow
import com.example.androidproject1.core.ui.component.AppSearchField
import com.example.androidproject1.core.ui.component.AppSectionHeader
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppSheet
import com.example.androidproject1.core.ui.component.AppSkeleton
import com.example.androidproject1.core.ui.component.AppSlider
import com.example.androidproject1.core.ui.component.AppSpinner
import com.example.androidproject1.core.ui.component.AppStatusDot
import com.example.androidproject1.core.ui.component.AppStepProgress
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppTabs
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.AppTimeField
import com.example.androidproject1.core.ui.component.AppToast
import com.example.androidproject1.core.ui.component.AppToolbar
import com.example.androidproject1.core.ui.component.AppTooltip
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.AppVerticalDivider
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.component.ControlSize
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.component.IconButtonKind
import com.example.androidproject1.core.ui.component.MenuItem
import com.example.androidproject1.core.ui.component.NavItem
import com.example.androidproject1.core.ui.component.ScrollEdge
import com.example.androidproject1.core.ui.component.SurfaceLevel
import com.example.androidproject1.core.ui.component.TabItem
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.component.ToastTone
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.gallery.presentation.gallery.GalleryItem
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.state.LoadingState
import com.example.androidproject1.service.core.ui.text.toUiText
import java.time.LocalDate
import java.time.LocalTime
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
 * here, a screen should not be inventing it. A component added to `:core:ui` is added here in the
 * same change, and a variant the document names is a variant listed here.
 *
 * Two files in `core/ui/component` are deliberately not entries. `AppScaffold` is the screen shell
 * rather than something on a screen — every page of this gallery is already one, so a card
 * containing a scaffold would demonstrate it worse than the gallery does by existing. `ControlSize`
 * is the shared sm / md / lg scale and not a component at all; it shows up as the size variants of
 * the controls that read it.
 *
 * `AppScreenChrome` is the one entry nothing else composes: it is what `Screen()` draws around
 * every feature (D50), so rendering it here is the only way to look at it. Its alert dialog and its
 * snackbar host draw in windows of their own and are goldens in `OverlayScreenshotTest` instead.
 */
val galleryCatalog: List<GalleryEntry> = listOf(
    entry(
        "text", "AppText", "Content",
        "Text asked for by role — the role decides both size and colour.",
        "Display large" to { AppText("1 248,00", role = TextRole.DisplayLarge) },
        "Display" to { AppText("Order 12 — On its way", role = TextRole.Display) },
        "Title" to { AppText("Drinks · Hot drinks", role = TextRole.Title) },
        "Body" to { AppText("The default size for list rows and dialog copy.") },
        "Label" to { AppText("Appearance", role = TextRole.Label) },
        "Label small" to { AppText("OPEN ORDERS", role = TextRole.LabelSmall) },
        "Numeric (tabular)" to { AppText("118,00", role = TextRole.Numeric) },
        "Numeric — shrinks rather than wraps" to {
            Box(modifier = Modifier.width(AppTheme.density.listRowHeight * 2)) {
                AppText("1 234 567,00 Kč", role = TextRole.Numeric)
            }
        },
        "Secondary" to { AppText("Opened 19:24 · Jana N.", role = TextRole.Secondary) },
        "Tertiary — missing value" to { AppText("—", role = TextRole.Tertiary) },
    ),
    entry(
        "button", "AppButton", "Action",
        "Six kinds, three sizes. The press shortens the key's edge so it travels 3 dp.",
        "Confirm" to { AppButton("Pay", {}) },
        "Destructive" to { AppButton("Delete", {}, kind = ButtonKind.Destructive) },
        "Info" to { AppButton("Learn more", {}, kind = ButtonKind.Info) },
        "Neutral" to { AppButton("Later", {}, kind = ButtonKind.Neutral) },
        "Outline" to { AppButton("Back", {}, kind = ButtonKind.Outline) },
        "Ghost" to { AppButton("More options", {}, kind = ButtonKind.Ghost) },
        "Disabled — loses the body" to { AppButton("Unavailable", {}, enabled = false) },
        "Loading — label and width hold" to { AppButton("Processing", {}, loading = true) },
        "Small 40 (mouse only)" to { AppButton("Small", {}, size = ControlSize.Small) },
        "Large 64" to { AppButton("Large", {}, size = ControlSize.Large) },
    ),
    entry(
        "iconbutton",
        "AppIconButton",
        "Action",
        "An icon that does something. The description is required, for the reader and the test.",
        "Default" to { AppIconButton(Icons.Filled.Add, "Add item", {}) },
        "Confirm" to { AppIconButton(Icons.Filled.Done, "Confirm", {}, kind = IconButtonKind.Confirm) },
        "Destructive" to {
            AppIconButton(Icons.Filled.Delete, "Delete", {}, kind = IconButtonKind.Destructive)
        },
        "Square — for a row of them" to {
            AppIconButton(Icons.Filled.Add, "Add item", {}, kind = IconButtonKind.Confirm, square = true)
        },
        "Small — pointer only" to { AppIconButton(Icons.Filled.Add, "Add item", {}, size = ControlSize.Small) },
        "Large" to { AppIconButton(Icons.Filled.Add, "Add item", {}, size = ControlSize.Large) },
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
        "Unit suffix — the unit is not the value" to {
            Demo("0,420") { value, onChange ->
                AppTextField(value, onChange, label = "Weight", numeric = true, suffix = "kg")
            }
        },
        "Small — pointer only" to {
            Demo("40") { value, onChange -> AppTextField(value, onChange, size = ControlSize.Small) }
        },
        "Large" to {
            Demo("56") { value, onChange -> AppTextField(value, onChange, size = ControlSize.Large) }
        },
        "Disabled" to { AppTextField("Locked", {}, label = "Username", enabled = false) },
        "In a form — Next, and offered to a password manager" to {
            Demo("ada@example.com") { value, onChange ->
                AppTextField(
                    value,
                    onChange,
                    label = "Address",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    contentType = ContentType.Username + ContentType.EmailAddress,
                )
            }
        },
        "The last field — Done submits" to {
            Demo("hunter2") { value, onChange ->
                AppTextField(
                    value,
                    onChange,
                    label = "Password",
                    password = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {},
                    contentType = ContentType.Password,
                )
            }
        },
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
                AppSelect(listOf("Daily", "Weekly", "Monthly", "Quarterly", "Yearly"), selected, onSelect)
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
        "On" to { CheckboxDemo(CheckState.On, "Email me a summary") },
        "Off" to { CheckboxDemo(CheckState.Off, "Send a weekly digest") },
        // Tapping resolves it: a group toggle that is partly on becomes fully on.
        "Indeterminate" to { CheckboxDemo(CheckState.Indeterminate, "All items") },
        "Error — never colour alone" to {
            AppCheckbox(
                CheckState.Off,
                {},
                "Accept the terms",
                errorText = "This has to be ticked before the order can be sent",
            )
        },
        "Small — pointer only" to {
            Demo(CheckState.On) { checked, onChange ->
                AppCheckbox(
                    checked,
                    { onChange(if (it) CheckState.On else CheckState.Off) },
                    "Dense list row",
                    size = ControlSize.Small,
                )
            }
        },
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
                    AppRadio(selected == 0, { onSelect(0) }, "Light")
                    AppRadio(selected == 1, { onSelect(1) }, "Dark")
                    AppRadio(false, {}, "System", enabled = false)
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
            Demo(true) { checked, onChange -> AppSwitch(checked, onChange, "Notify me about price drops") }
        },
        "With supporting text" to {
            Demo(false) { checked, onChange ->
                AppSwitch(checked, onChange, "Sounds", supporting = "Feedback on every key press")
            }
        },
        "Small — pointer only" to {
            Demo(true) { checked, onChange ->
                AppSwitch(checked, onChange, "Dense list row", size = ControlSize.Small)
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
        "Small — pointer only" to {
            Demo(0) { selected, onSelect ->
                AppSegmented(listOf("Today", "Week"), selected, onSelect, size = ControlSize.Small)
            }
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
        "Range — a filter, not a total" to {
            Demo(0.2f..0.7f) { range, onChange ->
                AppRangeSlider(
                    range,
                    onChange,
                    label = "Price",
                    valueLabel = "${(range.start * 200).roundToInt()} — ${(range.endInclusive * 200).roundToInt()} Kc",
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
            AppFormField("Appearance", required = true) {
                Demo(0) { selected, onSelect -> AppSegmented(listOf("Light", "Dark"), selected, onSelect) }
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
        "Positive" to { AppTag("Active", tone = TagTone.Positive) },
        "Warning" to { AppTag("Pending", tone = TagTone.Warning) },
        "Negative" to { AppTag("Failed", tone = TagTone.Negative) },
        "Info" to { AppTag("New", tone = TagTone.Info) },
        "Neutral" to { AppTag("Draft") },
    ),
    entry(
        "statusdot",
        "AppStatusDot",
        "Status",
        "A dot and a word. The dot never stands alone.",
        "Positive" to { AppStatusDot("Active", tone = TagTone.Positive) },
        "Warning" to { AppStatusDot("Pending", tone = TagTone.Warning) },
        "Negative" to { AppStatusDot("Failed", tone = TagTone.Negative) },
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
        "With status — online" to {
            AppAvatar("Jana Nováková", status = TagTone.Positive, statusDescription = "Online")
        },
        "With status — away" to {
            AppAvatar(
                "Petr Svoboda",
                size = AppTheme.density.listRowHeight,
                status = TagTone.Negative,
                statusDescription = "Away",
            )
        },
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
        "Indeterminate — no share to report" to {
            AppIndeterminateProgress(label = "Checking for updates")
        },
        "Steps — a place in a process" to {
            AppStepProgress(steps = 4, currentStep = 1, label = "Step 2 of 4")
        },
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
        "Raised — the default" to {
            AppCard {
                AppText("Notifications", role = TextRole.Title)
                AppText("Turn these on to hear about orders.", role = TextRole.Secondary)
            }
        },
        "Sunken — an inert panel" to {
            AppCard(level = SurfaceLevel.Sunken) {
                AppText("Archived trip", role = TextRole.Title)
                AppText("Read only until it is restored.", role = TextRole.Secondary)
            }
        },
        "Base — grouping only" to {
            AppCard(level = SurfaceLevel.Base) {
                AppText("On the ground it is already on", role = TextRole.Secondary)
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
        "Strong — between two sections" to {
            AppText("Section")
            AppDivider(strong = true)
            AppText("Next section")
        },
        "Labelled" to { AppLabelledDivider("EARLIER TODAY") },
        "Vertical — needs a height" to {
            Row(
                modifier = Modifier.height(AppTheme.density.minTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText("Left")
                AppVerticalDivider(
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.inline.md),
                )
                AppText("Right")
            }
        },
    ),
    entry(
        "listitem",
        "AppListItem",
        "Content",
        "The base of every list. Its height is the density's row height.",
        "Clickable" to { AppListItem("Pilsner Urquell", supporting = "0,5 l", onClick = {}) },
        "Selected — the one shown beside it" to {
            AppListItem("Kofola", supporting = "0,5 l", onClick = {}, selected = true)
        },
        "With a trailing tag" to {
            AppListItem(
                "Camera",
                supporting = "android.permission.CAMERA",
                trailing = { AppTag("Granted", tone = TagTone.Positive) },
            )
        },
        "With a leading avatar" to {
            AppListItem(
                "Cordless drill",
                supporting = "Jana Nováková",
                onClick = {},
                leading = { AppAvatar("Jana Nováková") },
                trailing = { AppTag("Good") },
            )
        },
    ),
    entry(
        "descriptionlist",
        "AppDescriptionList",
        "Content",
        "Label left, value right. A missing value is a dash, never a blank or a zero.",
        "An order's detail" to {
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
        "Success" to { AppToast("Order placed", tone = ToastTone.Success) },
        "With an action" to { AppToast("Item moved to your wishlist", actionLabel = "Undo", onAction = {}) },
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
                AppTabs(listOf(TabItem("All", badge = 4), TabItem("Unread"), TabItem("Archived")), selected, onSelect)
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
        "pager",
        "AppPager",
        "Navigation",
        "Pages read in order and then left — a first-run tour. Dots say how many, not what.",
        "Three pages" to {
            AppPager(pageCount = 3) { page ->
                AppText("Page ${page + 1} of 3", role = TextRole.Title)
            }
        },
        "Two pages" to {
            AppPager(pageCount = 2) { page ->
                AppText(if (page == 0) "Swipe left" else "And back again", role = TextRole.Title)
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
                AppCheckbox(CheckState.Indeterminate, {}, "All", inverse = true)
                AppIconButton(Icons.Filled.Delete, "Delete", {}, kind = IconButtonKind.Inverse)
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
    entry(
        "datefield",
        "AppDateField",
        "Form",
        "A date from the platform's picker. Never typed — 01/02 is January here and February there.",
        "With a value" to {
            Demo<LocalDate?>(LocalDate.of(2026, 9, 9)) { value, onChange ->
                AppDateField(value, { onChange(it) }, label = "Delivery")
            }
        },
        "Empty" to {
            Demo<LocalDate?>(null) { value, onChange ->
                AppDateField(value, { onChange(it) }, label = "Closed until")
            }
        },
        "Time" to {
            Demo<LocalTime?>(LocalTime.of(19, 24)) { value, onChange ->
                AppTimeField(value, { onChange(it) }, label = "Opened")
            }
        },
        "Disabled" to { AppTimeField(null, {}, label = "Last order", enabled = false) },
    ),
    entry(
        "fieldgroup",
        "AppFieldGroup",
        "Form",
        "Several fields that are one answer, in one frame — an address, a card.",
        "An address" to {
            AppFieldGroup(label = "Delivery address", helperText = "Where the order goes") {
                AppFieldGroupRow {
                    Demo("Nádražní 12") { v, on -> AppTextField(v, on, frame = false) }
                }
                AppFieldGroupRow {
                    Demo("Praha 5") { v, on -> AppTextField(v, on, frame = false) }
                }
                AppFieldGroupRow(last = true) {
                    Demo("150 00") { v, on -> AppTextField(v, on, frame = false, numeric = true) }
                }
            }
        },
        "In error — the group carries it, not each row" to {
            AppFieldGroup(label = "Card", errorText = "This card has expired") {
                AppFieldGroupRow { AppText("4242 4242 4242 4242", role = TextRole.Numeric) }
                AppFieldGroupRow(last = true) { AppText("01 / 24", role = TextRole.Numeric) }
            }
        },
    ),
    entry(
        "scrollshadow",
        "AppScrollShadow",
        "Content",
        "The cue that a list carries on past the fold. Present only while there is more.",
        "Both edges of a short list" to {
            val scroll = rememberScrollState()
            // Two rows tall, so there is always more of the list than there is room for it.
            Box(modifier = Modifier.height(AppTheme.density.listRowHeight * 2)) {
                Column(modifier = Modifier.verticalScroll(scroll)) {
                    repeat(8) { AppListItem("Row ${it + 1}") }
                }
                AppScrollShadow(scroll, ScrollEdge.Top, Modifier.align(Alignment.TopCenter))
                AppScrollShadow(scroll, ScrollEdge.Bottom, Modifier.align(Alignment.BottomCenter))
            }
        },
    ),
    entry(
        "menu",
        "AppMenu",
        "Overlay",
        "Actions on the thing you opened it from. Destructive items sort to the bottom.",
        "Opens from its anchor" to {
            var expanded by remember { mutableStateOf(false) }
            AppMenu(
                expanded = expanded,
                onDismiss = { expanded = false },
                items = listOf(
                    MenuItem("Rename") { expanded = false },
                    MenuItem("Duplicate") { expanded = false },
                    MenuItem("Delete", destructive = true) { expanded = false },
                ),
                anchor = {
                    AppButton("Actions", { expanded = true }, kind = ButtonKind.Outline)
                },
            )
        },
    ),
    entry(
        "sheet",
        "AppSheet",
        "Overlay",
        "The default way to open a detail on a phone. The handle is always drawn.",
        "Opens over the screen" to {
            var open by remember { mutableStateOf(false) }
            AppButton("Open the sheet", { open = true })
            if (open) {
                AppSheet(onDismiss = { open = false }, title = "Appearance") {
                    AppText("Everything inside a sheet is in a window of its own.", role = TextRole.Secondary)
                    AppButton("Close", { open = false }, kind = ButtonKind.Neutral)
                }
            }
        },
    ),
    entry(
        "dialog",
        "AppDialog",
        "Overlay",
        "The top layer. The confirming button carries a verb — \"Delete trip\", never \"OK\".",
        "Dialog with actions" to {
            var open by remember { mutableStateOf(false) }
            AppButton("Open the dialog", { open = true })
            if (open) {
                AppDialog(
                    title = "Delete this trip?",
                    message = "Its itinerary and notes go with it.",
                    onDismiss = { open = false },
                    actions = {
                        AppButton("Cancel", { open = false }, kind = ButtonKind.Ghost)
                        AppButton("Delete trip", { open = false }, kind = ButtonKind.Destructive)
                    },
                )
            }
        },
        "Confirm dialog — the gap is deliberate" to {
            var open by remember { mutableStateOf(false) }
            AppButton("Open the confirm dialog", { open = true }, kind = ButtonKind.Destructive)
            if (open) {
                AppConfirmDialog(
                    title = "Delete this trip?",
                    message = "Its itinerary and notes go with it. This cannot be undone.",
                    confirmLabel = "Delete trip",
                    onConfirm = { open = false },
                    onDismiss = { open = false },
                )
            }
        },
    ),
    entry(
        "screenChrome",
        "AppScreenChrome",
        "Feedback",
        "What Screen() draws around every feature: the base surface, the loading overlay and the " +
            "empty and error states. A feature never composes these — it puts a ContentState or a " +
            "LoadingState in its UiState and this renders it.",
        "Error, with a retry" to {
            Box(modifier = Modifier.height(AppTheme.density.listRowHeight * CHROME_DEMO_ROWS)) {
                AppScreenChrome.ContentMessage(
                    state = ContentState.Error(message = "Nothing came back. Try again.".toUiText()),
                    onAction = {},
                    modifier = Modifier,
                )
            }
        },
        "Empty" to {
            Box(modifier = Modifier.height(AppTheme.density.listRowHeight * CHROME_DEMO_ROWS)) {
                AppScreenChrome.ContentMessage(
                    state = ContentState.Empty(message = "No orders on this table yet.".toUiText()),
                    onAction = {},
                    modifier = Modifier,
                )
            }
        },
        "Loading overlay" to {
            Box(modifier = Modifier.height(AppTheme.density.listRowHeight * CHROME_DEMO_ROWS)) {
                AppScreenChrome.LoadingOverlay(state = LoadingState(), modifier = Modifier)
            }
        },
        "Loading overlay, worded" to {
            Box(modifier = Modifier.height(AppTheme.density.listRowHeight * CHROME_DEMO_ROWS)) {
                AppScreenChrome.LoadingOverlay(
                    state = LoadingState(message = "Saving the photo".toUiText()),
                    modifier = Modifier,
                )
            }
        },
    ),
    entry(
        "banner",
        "AppBanner",
        "Status",
        "One line above the screen, in the warning role, for a fact that stays true until it stops: " +
            "the device is offline. Not a toast — nothing happened, something is so.",
        "Offline" to { AppBanner(text = "You're offline") },
    ),
    // create_component.py appends a starter entry here; doctor.py fails on a component with none.
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

/** Tall enough that an overlay reads as an overlay rather than as a strip. */
private const val CHROME_DEMO_ROWS = 3

fun galleryEntry(id: String): GalleryEntry? = galleryCatalog.firstOrNull { it.id == id }
