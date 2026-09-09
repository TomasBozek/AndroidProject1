package com.example.androidproject1.core.ui.form

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.text.UiText

/**
 * One field of a form.
 *
 * [touched] is why this is a type rather than a `String`: a form must not shout at someone who has
 * not typed yet. The value is validated from the first keystroke, but [error] stays null until the
 * field has been touched — so "This field is required" appears when they clear it, not when the
 * screen opens.
 */
@Immutable
data class FieldState(
    val value: String = "",
    val touched: Boolean = false,
    private val validators: List<Validator> = emptyList(),
) {

    /** The first rule this value breaks, once the user has had a chance to satisfy it. */
    val error: UiText? get() = if (touched) firstFailure else null

    /** Whether the value is acceptable, regardless of whether the error is being shown yet. */
    val isValid: Boolean get() = firstFailure == null

    private val firstFailure: UiText? get() = validators.firstNotNullOfOrNull { it(value) }

    /** What a `ValueChanged` event does. Typing marks the field touched. */
    fun changed(value: String): FieldState = copy(value = value, touched = true)

    /** Marks the field touched without changing it — for a submit that has to reveal every error. */
    fun touch(): FieldState = if (touched) this else copy(touched = true)

    companion object {

        fun of(vararg validators: Validator, value: String = "") =
            FieldState(value = value, validators = validators.toList())
    }
}
