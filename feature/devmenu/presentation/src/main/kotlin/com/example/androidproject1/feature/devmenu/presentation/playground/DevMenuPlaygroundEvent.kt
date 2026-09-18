package com.example.androidproject1.feature.devmenu.presentation.playground

import com.example.androidproject1.feature.devmenu.presentation.KnobValue
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface DevMenuPlaygroundEvent : UiEvent {

    /** Another entry on the bench; its knobs start at their defaults. */
    data class ComponentPicked(val entryId: String) : DevMenuPlaygroundEvent

    /** One knob turned. */
    data class KnobChanged(val key: String, val value: KnobValue) : DevMenuPlaygroundEvent

    data object NavigateUpClicked : DevMenuPlaygroundEvent
}
