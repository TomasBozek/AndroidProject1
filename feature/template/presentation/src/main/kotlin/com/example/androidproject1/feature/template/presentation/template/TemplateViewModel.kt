package com.example.androidproject1.feature.template.presentation.template

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.updateData
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

class TemplateViewModel(
    logger: Logger,
) : BaseViewModel<TemplateState, TemplateEvent, TemplateNavigation>(
    // Pass null instead when the screen cannot render until something is loaded; the loading
    // overlay then stays up until the ViewModel puts a state in.
    initialState = TemplateState.PREVIEW,
    logger = logger.withTag("TemplateViewModel"),
) {

    // What the user did arrives here; emit a TemplateNavigation to move on. Anything that can
    // fail goes through execute {} or observe(flow = …) {} — never try/catch.
    override fun onUiEvent(event: TemplateEvent) = when (event) {
        TemplateEvent.IncrementClicked -> uiState.updateData { copy(counter = counter + 1) }
    }
}
