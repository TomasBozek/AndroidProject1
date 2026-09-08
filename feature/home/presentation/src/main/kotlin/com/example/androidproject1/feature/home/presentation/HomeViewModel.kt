package com.example.androidproject1.feature.home.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

class HomeViewModel(
    logger: Logger,
) : BaseViewModel<HomeState, HomeEvent, HomeNavigation>(
    initialState = HomeState(greeting = R.string.home_greeting.toUiText()),
    logger = logger.withTag("HomeViewModel"),
)
