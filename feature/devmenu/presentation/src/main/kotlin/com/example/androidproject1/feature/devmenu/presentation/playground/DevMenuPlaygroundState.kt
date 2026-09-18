package com.example.androidproject1.feature.devmenu.presentation.playground

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.devmenu.presentation.KnobValue
import com.example.androidproject1.feature.devmenu.presentation.defaults
import com.example.androidproject1.feature.devmenu.presentation.playgroundCatalog
import com.example.androidproject1.feature.devmenu.presentation.playgroundEntry

/**
 * Which entry is on the bench and what its knobs hold. Nothing here names a component: the
 * screen looks the entry up by id and draws its controls from its knobs (D77).
 */
@Immutable
data class DevMenuPlaygroundState(
    val entryId: String,
    val values: Map<String, KnobValue>,
) {

    companion object {

        /** The first entry with its knobs at their defaults — also the ViewModel's initial state. */
        val PREVIEW = playgroundCatalog.first().let { DevMenuPlaygroundState(it.id, it.defaults()) }
    }
}

/** The first entry, a second entry, and the first with every knob turned — three shapes of bench. */
class DevMenuPlaygroundStatePreviews : PreviewParameterProvider<DevMenuPlaygroundState> {

    override val values = sequenceOf(
        DevMenuPlaygroundState.PREVIEW,
        playgroundEntry("switch")!!.let { DevMenuPlaygroundState(it.id, it.defaults()) },
        DevMenuPlaygroundState.PREVIEW.copy(
            values = mapOf(
                "label" to KnobValue.Text("Delete everything"),
                "kind" to KnobValue.Choice(1),
                "size" to KnobValue.Choice(2),
                "enabled" to KnobValue.Bool(false),
                "loading" to KnobValue.Bool(true),
            ),
        ),
    )
}
