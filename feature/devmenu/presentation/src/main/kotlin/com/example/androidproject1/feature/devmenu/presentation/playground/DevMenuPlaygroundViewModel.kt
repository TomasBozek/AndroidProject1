package com.example.androidproject1.feature.devmenu.presentation.playground

import com.example.androidproject1.feature.devmenu.presentation.defaults
import com.example.androidproject1.feature.devmenu.presentation.playgroundEntry
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class DevMenuPlaygroundViewModel(
    logger: Logger,
) : BaseViewModel<DevMenuPlaygroundState, DevMenuPlaygroundEvent, DevMenuPlaygroundNavigation>(
    initialState = DevMenuPlaygroundState.PREVIEW,
    logger = logger.withTag("DevMenuPlaygroundViewModel"),
) {

    override fun onUiEvent(event: DevMenuPlaygroundEvent) {
        when (event) {
            // The knobs reset with the entry: a value left over from another entry's knob of the
            // same key would be a bench that remembers the wrong thing.
            is DevMenuPlaygroundEvent.ComponentPicked -> playgroundEntry(event.entryId)?.let { entry ->
                updateData { copy(entryId = entry.id, values = entry.defaults()) }
            }

            is DevMenuPlaygroundEvent.KnobChanged ->
                updateData { copy(values = values + (event.key to event.value)) }

            DevMenuPlaygroundEvent.NavigateUpClicked -> navigate(DevMenuPlaygroundNavigation.NavigateUp)
        }
    }
}
