package com.example.androidproject1.feature.launch.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

/**
 * Nothing to do: MainActivity always starts here, and MainViewModel is the one that decides when
 * the session is ready and swaps this destination for the auth or main graph. This is also the
 * future home of any real startup work (remote config, cache warm-up) this screen stands in for.
 */
class LaunchViewModel(
    logger: Logger,
) : BaseViewModel<LaunchState, LaunchEvent, LaunchNavigation>(
    initialState = LaunchState,
    logger = logger.withTag("LaunchViewModel"),
)
