package com.example.androidproject1.feature.home.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.toText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

class HomeViewModel(
    logger: Logger,
) : BaseViewModel<HomeState, HomeEvent, HomeDirection>(
    initialState = HomeState(greeting = R.string.home_greeting.toText()),
    logger = logger.withTag("HomeViewModel"),
) {

    override fun onUiEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.SettingsClicked -> navigate(HomeDirection.Settings)
        }
    }
}
