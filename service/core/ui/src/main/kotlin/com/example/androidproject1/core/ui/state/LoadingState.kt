package com.example.androidproject1.core.ui.state

import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class LoadingState(
    val message: UiText = R.string.core_loading.toUiText(),
)

var <Data> MutableStateFlow<UiState<Data>>.isLoading: Boolean
    get() = value.loading != null
    set(value) = update { it.copy(loading = if (value) LoadingState() else null) }
