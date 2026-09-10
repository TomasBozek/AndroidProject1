package com.example.androidproject1.feature.template.presentation.templateargs

import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class TemplateArgsViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: TemplateArgsDestination,
) : BaseViewModel<TemplateArgsState, TemplateArgsEvent, TemplateArgsNavigation>(
    // The route key is already enough to draw with, so the screen starts from a real state. Pass
    // `null` only when it genuinely cannot render until something loads — and then ask for the wait
    // on the call that is waiting, with `loading = overlay()` (D44).
    initialState = TemplateArgsState(templateId = args.templateId),
    logger = logger.withTag("TemplateArgsViewModel"),
) {

    init {
        // TODO: load whatever the argument stands for — usually
        //  execute(loading = overlay(), errorDisplay = Inline, action = { … }, onData = { … }).
        logger.d { "Opened ${args.templateId}" }
    }
}
