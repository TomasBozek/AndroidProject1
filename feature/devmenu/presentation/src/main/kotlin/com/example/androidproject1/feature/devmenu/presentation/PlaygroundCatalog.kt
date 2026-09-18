package com.example.androidproject1.feature.devmenu.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.component.AppBadge
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppProgress
import com.example.androidproject1.core.ui.component.AppSegmented
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.component.AppTag
import com.example.androidproject1.core.ui.component.AppTextField
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.ControlSize
import com.example.androidproject1.core.ui.component.TagTone

/**
 * One value a knob holds. Four shapes, one per [Knob] shape; a knob's control writes its own and
 * an entry's `render` reads it back through [KnobValues].
 */
@Immutable
sealed interface KnobValue {

    data class Bool(val value: Boolean) : KnobValue

    /** An index into the knob's options. */
    data class Choice(val index: Int) : KnobValue

    data class Text(val value: String) : KnobValue

    data class Number(val value: Int) : KnobValue
}

/**
 * One property of a component the playground lets a tester drive (D77).
 *
 * The shape is the control: a [Toggle] is a switch, a [Choice] a segmented control (a select past
 * four options), a [Text] a text field, a [Number] a stepper. `PlaygroundKnob` holds the one
 * `when` over these; nothing else in the module inspects a knob's shape, and no entry does.
 *
 * @property key what an entry's `render` asks [KnobValues] for. Unique within one entry, and the
 *   stem of the control's test id — `devMenuPlayground_<key>Switch`.
 * @property label what the control shows. Dev tooling, so a literal rather than a resource, like
 *   the gallery's names.
 */
@Immutable
sealed interface Knob {

    val key: String

    val label: String

    val default: KnobValue

    data class Toggle(
        override val key: String,
        override val label: String,
        val initial: Boolean,
    ) : Knob {

        override val default: KnobValue get() = KnobValue.Bool(initial)
    }

    data class Choice(
        override val key: String,
        override val label: String,
        val options: List<String>,
        val initial: Int = 0,
    ) : Knob {

        override val default: KnobValue get() = KnobValue.Choice(initial)
    }

    data class Text(
        override val key: String,
        override val label: String,
        val initial: String,
    ) : Knob {

        override val default: KnobValue get() = KnobValue.Text(initial)
    }

    data class Number(
        override val key: String,
        override val label: String,
        val range: IntRange,
        val initial: Int,
    ) : Knob {

        override val default: KnobValue get() = KnobValue.Number(initial)
    }
}

/**
 * What an entry's `render` reads: the current value of each knob, falling back to the knob's
 * default when the state has none yet — so a freshly picked entry renders without a first edit,
 * and a value of the wrong shape (a stale key from another entry) is the default rather than a
 * cast.
 */
@Immutable
class KnobValues(
    private val entry: PlaygroundEntry,
    private val values: Map<String, KnobValue>,
) {

    fun bool(key: String): Boolean = (value(key) as? KnobValue.Bool)?.value ?: false

    fun choice(key: String): Int = (value(key) as? KnobValue.Choice)?.index ?: 0

    fun text(key: String): String = (value(key) as? KnobValue.Text)?.value ?: ""

    fun int(key: String): Int = (value(key) as? KnobValue.Number)?.value ?: 0

    private fun value(key: String): KnobValue? = values[key] ?: entry.knobs.firstOrNull { it.key == key }?.default
}

/**
 * One component on the bench: its knobs, and how to draw it from their values.
 *
 * The demos live here rather than in `:core:ui`, the way the gallery's do: a feature composes
 * components, and `:core:ui` ships no sample code. The gallery itself is another feature's
 * `presentation`, which this one may not import — so this is a second, smaller catalog, of the
 * controls with the most states, and adding one is one `entry(...)` here and nothing anywhere else.
 */
@Immutable
data class PlaygroundEntry(
    val id: String,
    val name: String,
    val knobs: List<Knob>,
    val render: @Composable (KnobValues) -> Unit,
)

/** Every knob at its default: what the state holds when an entry is picked. */
fun PlaygroundEntry.defaults(): Map<String, KnobValue> = knobs.associate { it.key to it.default }

private fun entry(
    id: String,
    name: String,
    vararg knobs: Knob,
    render: @Composable (KnobValues) -> Unit,
) = PlaygroundEntry(id, name, knobs.toList(), render)

private fun <E : Enum<E>> names(entries: List<E>): List<String> = entries.map { it.name }

/**
 * The bench. An entry's own callbacks are empty on purpose: the stage is a display, and its
 * state is the knobs' — a switch on the stage is turned by the `checked` knob under it, which is
 * what a bench is for (D77).
 */
val playgroundCatalog: List<PlaygroundEntry> = listOf(
    entry(
        "button",
        "AppButton",
        Knob.Text("label", "Label", "Pay"),
        Knob.Choice("kind", "Kind", names(ButtonKind.entries)),
        Knob.Choice("size", "Size", names(ControlSize.entries), initial = ControlSize.Medium.ordinal),
        Knob.Toggle("enabled", "Enabled", true),
        Knob.Toggle("loading", "Loading", false),
    ) { v ->
        AppButton(
            label = v.text("label"),
            onClick = {},
            kind = ButtonKind.entries[v.choice("kind")],
            size = ControlSize.entries[v.choice("size")],
            enabled = v.bool("enabled"),
            loading = v.bool("loading"),
        )
    },
    entry(
        "switch",
        "AppSwitch",
        Knob.Text("label", "Label", "Print receipt automatically"),
        Knob.Text("supporting", "Supporting", "Every sale"),
        Knob.Toggle("checked", "Checked", true),
        Knob.Toggle("enabled", "Enabled", true),
        Knob.Choice("size", "Size", names(ControlSize.entries), initial = ControlSize.Medium.ordinal),
    ) { v ->
        AppSwitch(
            checked = v.bool("checked"),
            onCheckedChange = {},
            label = v.text("label"),
            supporting = v.text("supporting").ifEmpty { null },
            enabled = v.bool("enabled"),
            size = ControlSize.entries[v.choice("size")],
        )
    },
    entry(
        "textfield",
        "AppTextField",
        Knob.Text("value", "Value", "ada@example.com"),
        Knob.Text("label", "Label", "Email"),
        Knob.Text("helper", "Helper", "Where the receipt goes"),
        Knob.Text("error", "Error", ""),
        Knob.Toggle("enabled", "Enabled", true),
    ) { v ->
        AppTextField(
            value = v.text("value"),
            onValueChange = {},
            label = v.text("label").ifEmpty { null },
            helperText = v.text("helper").ifEmpty { null },
            errorText = v.text("error").ifEmpty { null },
            enabled = v.bool("enabled"),
        )
    },
    entry(
        "tag",
        "AppTag",
        Knob.Text("label", "Label", "Active"),
        Knob.Choice("tone", "Tone", names(TagTone.entries), initial = TagTone.Positive.ordinal),
    ) { v ->
        AppTag(label = v.text("label"), tone = TagTone.entries[v.choice("tone")])
    },
    entry(
        "stepper",
        "AppStepper",
        Knob.Number("value", "Value", 0..STEPPER_CEILING, initial = 2),
        Knob.Number("min", "Min", 0..STEPPER_CEILING, initial = 0),
        Knob.Number("max", "Max", 0..STEPPER_CEILING, initial = STEPPER_CEILING),
    ) { v ->
        AppStepper(value = v.int("value"), onValueChange = {}, min = v.int("min"), max = v.int("max"))
    },
    entry(
        "segmented",
        "AppSegmented",
        Knob.Choice("selected", "Selected", listOf("Day", "Week", "Month")),
        Knob.Choice("size", "Size", names(ControlSize.entries), initial = ControlSize.Medium.ordinal),
    ) { v ->
        AppSegmented(
            options = listOf("Day", "Week", "Month"),
            selectedIndex = v.choice("selected"),
            onSelect = {},
            size = ControlSize.entries[v.choice("size")],
        )
    },
    entry(
        "badge",
        "AppBadge",
        Knob.Number("count", "Count", 0..BADGE_CEILING, initial = 3),
    ) { v ->
        AppBadge(count = v.int("count"))
    },
    entry(
        "progress",
        "AppProgress",
        Knob.Number("percent", "Percent", 0..PERCENT, initial = 40),
        Knob.Text("label", "Label", "Syncing"),
    ) { v ->
        AppProgress(fraction = v.int("percent") / PERCENT.toFloat(), label = v.text("label").ifEmpty { null })
    },
)

fun playgroundEntry(id: String): PlaygroundEntry? = playgroundCatalog.firstOrNull { it.id == id }

/** Wide enough to see a stepper stop at its floor and its ceiling, small enough to reach both. */
private const val STEPPER_CEILING = 9

/** Past the two-digit badge, so the "99+" case is one tap away. */
private const val BADGE_CEILING = 120

private const val PERCENT = 100
