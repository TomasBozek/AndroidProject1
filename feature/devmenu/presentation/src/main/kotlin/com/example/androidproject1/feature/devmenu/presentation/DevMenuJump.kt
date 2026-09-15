package com.example.androidproject1.feature.devmenu.presentation

import androidx.compose.runtime.Immutable
import com.example.androidproject1.service.core.ui.text.UiText

/**
 * One row of the debug menu's jump list: a screen a tester opens in one tap instead of walking
 * the app to it.
 *
 * Built in `AppNavHost` — the only file that knows every destination, since a feature's
 * presentation may not name another's — and handed to the destination as a list, so a new target
 * is one line there and nothing in this module changes. A route that takes an argument is given a
 * value the fixtures serve, never a blank one.
 *
 * @property id the camelCase stem the row's test tag is built from: `tripWizard` becomes
 *   `devMenu_tripWizardItem`.
 * @property label what the row shows — the screen's own id, the word a flow's `assertVisible`
 *   uses, so it is an identifier rather than copy.
 * @property navigate pushes the destination onto the back stack.
 */
@Immutable
data class DevMenuJump(
    val id: String,
    val label: UiText,
    val navigate: () -> Unit,
)
