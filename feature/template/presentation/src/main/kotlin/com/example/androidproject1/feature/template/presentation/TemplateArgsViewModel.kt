package com.example.androidproject1.feature.template.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.update

class TemplateArgsViewModel(
    logger: Logger,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<TemplateArgsState, TemplateArgsEvent, TemplateArgsNavigation>(
    // Null while the argument is being turned into something renderable. Pass a real state
    // instead when the screen can draw before that work finishes.
    initialState = null,
    logger = logger.withTag("TemplateArgsViewModel"),
    savedStateHandle = savedStateHandle,
) {

    // Decoded from the route. Available here in init, and restored after process death.
    private val args = navArgs<TemplateArgsDestination>()

    init {
        // TODO: replace with the real load — usually execute(errorDisplay = Inline) { … }.
        uiState.update { it.copy(data = TemplateArgsState(templateId = args.templateId)) }
    }
}
