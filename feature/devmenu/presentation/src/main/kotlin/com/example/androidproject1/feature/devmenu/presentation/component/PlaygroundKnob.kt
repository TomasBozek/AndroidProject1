package com.example.androidproject1.feature.devmenu.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.devmenu.presentation.Knob
import com.example.androidproject1.feature.devmenu.presentation.KnobValue

/**
 * The control for one [Knob], chosen by the knob's shape and nothing else (D77): a toggle is a
 * switch, a choice a segmented control — a select once there are more than four options, which
 * is where segments stop fitting — a text a field, a number a stepper. This is the one `when`
 * over [Knob] in the module.
 *
 * The test id is built from the knob's key — `devMenuPlayground_<key>Switch`, `…Tab`, `…Field` —
 * so a test drives a knob by what it is called, never by its label.
 */
@Composable
fun PlaygroundKnob(
    knob: Knob,
    value: KnobValue,
    onChange: (KnobValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (knob) {
        is Knob.Toggle -> AppSwitch(
            checked = (value as? KnobValue.Bool)?.value ?: knob.initial,
            onCheckedChange = { onChange(KnobValue.Bool(it)) },
            label = knob.label,
            modifier = modifier.testTag("devMenuPlayground_${knob.key}Switch"),
        )

        is Knob.Choice -> Labelled(label = knob.label, modifier = modifier) {
            val selected = (value as? KnobValue.Choice)?.index ?: knob.initial
            if (knob.options.size <= SEGMENT_CEILING) {
                AppSegmented(
                    options = knob.options,
                    selectedIndex = selected,
                    onSelect = { onChange(KnobValue.Choice(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("devMenuPlayground_${knob.key}Tab"),
                )
            } else {
                AppSelect(
                    options = knob.options,
                    selectedIndex = selected,
                    onSelect = { onChange(KnobValue.Choice(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("devMenuPlayground_${knob.key}Tab"),
                )
            }
        }

        is Knob.Text -> AppTextField(
            value = (value as? KnobValue.Text)?.value ?: knob.initial,
            onValueChange = { onChange(KnobValue.Text(it)) },
            label = knob.label,
            modifier = modifier
                .fillMaxWidth()
                .testTag("devMenuPlayground_${knob.key}Field"),
        )

        is Knob.Number -> Labelled(label = knob.label, modifier = modifier) {
            AppStepper(
                value = (value as? KnobValue.Number)?.value ?: knob.initial,
                onValueChange = { onChange(KnobValue.Number(it)) },
                min = knob.range.first,
                max = knob.range.last,
                modifier = Modifier.testTag("devMenuPlayground_${knob.key}Field"),
            )
        }
    }
}

/** A small label over a control that has no label slot of its own. */
@Composable
private fun Labelled(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        AppText(text = label, role = TextRole.LabelSmall)
        content()
    }
}

/** Four segments is what a phone's width holds; past that the choice is a select. */
private const val SEGMENT_CEILING = 4

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md)) {
        PlaygroundKnob(Knob.Toggle("enabled", "Enabled", true), KnobValue.Bool(true), {})
        PlaygroundKnob(Knob.Choice("size", "Size", listOf("Small", "Medium", "Large"), 1), KnobValue.Choice(1), {})
        PlaygroundKnob(Knob.Text("label", "Label", "Pay"), KnobValue.Text("Pay"), {})
        PlaygroundKnob(Knob.Number("count", "Count", 0..9, 3), KnobValue.Number(3), {})
    }
}
