package com.example.androidproject1.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.ButtonSize
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.component.TagTone
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Goldens for the design system.
 *
 * These live in `screenshotTest` because the plugin only renders previews it finds here — the
 * `@ComponentPreview`s in `main` are for the IDE. That is a duplication, and it is the reason this
 * file groups components into a handful of sheets rather than mirroring all 41 one for one: a
 * sheet catches a change to the theme, which is what actually breaks a set of components at once.
 *
 * `./gradlew updateDebugScreenshotTest` records, `validateDebugScreenshotTest` checks.
 */
private const val LIGHT = "spec:width=420dp,height=900dp"

@Composable
private fun Sheet(content: @Composable () -> Unit) = ThemedComponentPreview {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
    ) { content() }
}

@Preview(name = "Buttons", device = LIGHT, showBackground = true)
@Preview(name = "Buttons dark", device = LIGHT, showBackground = true, uiMode = 0x21)
@Composable
fun ButtonSheet() = Sheet {
    AppButton("Pay", {})
    AppButton("Void", {}, kind = ButtonKind.Destructive)
    AppButton("Card", {}, kind = ButtonKind.Info)
    AppButton("Cash", {}, kind = ButtonKind.Neutral)
    AppButton("Back", {}, kind = ButtonKind.Outline)
    AppButton("More options", {}, kind = ButtonKind.Ghost)
    AppButton("Unavailable", {}, enabled = false)
    AppButton("Processing", {}, loading = true)
    AppButton("Small", {}, size = ButtonSize.Small)
    AppButton("Large", {}, size = ButtonSize.Large)
}

@Preview(name = "Form", device = LIGHT, showBackground = true)
@Preview(name = "Form dark", device = LIGHT, showBackground = true, uiMode = 0x21)
@Composable
fun FormSheet() = Sheet {
    AppTextField("", {}, label = "Search", placeholder = "Product name")
    AppTextField("12", {}, label = "Quantity", numeric = true)
    AppTextField("abc", {}, label = "Code", errorText = "No product with this code")
    AppCheckbox(CheckState.On, {}, "Print receipt")
    AppCheckbox(CheckState.Indeterminate, {}, "All items")
    AppSwitch(true, {}, "Print automatically")
    AppSegmented(listOf("Today", "Week", "Month"), 0, {})
}

@Preview(name = "Type and status", device = LIGHT, showBackground = true)
@Preview(name = "Type and status dark", device = LIGHT, showBackground = true, uiMode = 0x21)
@Composable
fun TypeSheet() = Sheet {
    AppText("1 248,00", role = TextRole.DisplayLarge)
    AppText("Order 12 — Terrace", role = TextRole.Display)
    AppText("Drinks · Hot drinks", role = TextRole.Title)
    AppText("List rows and dialog copy.", role = TextRole.Body)
    AppText("OPEN ORDERS", role = TextRole.LabelSmall)
    AppText("118,00", role = TextRole.Numeric)
    AppTag("Paid", tone = TagTone.Paid)
    AppTag("Open", tone = TagTone.Open)
    AppTag("Void", tone = TagTone.Void)
    AppListItem("Pilsner Urquell", supporting = "0,5 l", onClick = {})
}

/** The scale that breaks layouts, on the sheet most likely to break. */
@Preview(name = "Form at 1.5x", device = LIGHT, showBackground = true, fontScale = 1.5f)
@Composable
fun FormSheetLargeFont() = FormSheet()
