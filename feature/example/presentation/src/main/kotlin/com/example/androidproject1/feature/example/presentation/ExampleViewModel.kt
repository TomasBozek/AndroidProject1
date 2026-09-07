package com.example.androidproject1.feature.example.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

class ExampleViewModel(
    logger: Logger,
) : BaseViewModel<ExampleState, ExampleEvent, ExampleDirection>(
    initialState = ExampleState.PREVIEW,
    logger = logger.withTag("ExampleViewModel"),
) {

    // Pass `initialState = null` instead when the screen cannot render until something is loaded;
    // the loading overlay then shows until the ViewModel puts a state in.
}
