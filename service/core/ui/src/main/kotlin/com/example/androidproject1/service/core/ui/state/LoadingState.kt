package com.example.androidproject1.service.core.ui.state

import com.example.androidproject1.service.core.ui.R
import com.example.androidproject1.service.core.ui.text.UiText
import com.example.androidproject1.service.core.ui.text.toUiText

/**
 * Wording for the loading overlay. Set it through `BaseViewModel.setLoading` or the
 * `loadingMessage` parameter of `execute`/`observe` — those keep the message across the
 * reference-counted overlay rather than rebuilding a default one on every change.
 */
data class LoadingState(
    val message: UiText = R.string.core_loading.toUiText(),
)
