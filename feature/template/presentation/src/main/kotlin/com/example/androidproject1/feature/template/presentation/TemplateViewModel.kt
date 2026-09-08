package com.example.androidproject1.feature.template.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

class TemplateViewModel(
    logger: Logger,
) : BaseViewModel<TemplateState, TemplateEvent, TemplateNavigation>(
    // Pass null instead when the screen cannot render until something is loaded; the loading
    // overlay then stays up until the ViewModel puts a state in.
    initialState = TemplateState.PREVIEW,
    logger = logger.withTag("TemplateViewModel"),
)
