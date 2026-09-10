package com.example.androidproject1.feature.template.presentation.template

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface TemplateEvent : UiEvent {

    /**
     * The worked example: the screen reports what the user did, the ViewModel decides what it
     * means. Replace it with this screen's real events — but keep at least one, because
     * `TemplateScreenTest` asserts that a tap arrives here, and a screen that reports nothing has
     * no behaviour to test.
     */
    data object IncrementClicked : TemplateEvent
}
