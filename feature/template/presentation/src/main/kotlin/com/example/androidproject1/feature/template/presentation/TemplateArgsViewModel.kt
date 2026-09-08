package com.example.androidproject1.feature.template.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.update

class TemplateArgsViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: TemplateArgsDestination,
) : BaseViewModel<TemplateArgsState, TemplateArgsEvent, TemplateArgsNavigation>(
    // Null while the argument is being turned into something renderable. Pass a real state
    // instead when the screen can draw before that work finishes.
    initialState = null,
    logger = logger.withTag("TemplateArgsViewModel"),
) {

    init {
        // TODO: replace with the real load — usually execute(errorDisplay = Inline) { … }.
        uiState.update { it.copy(data = TemplateArgsState(templateId = args.templateId)) }
    }
}
