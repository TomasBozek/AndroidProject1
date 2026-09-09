package com.example.androidproject1.core.ui.form

import com.example.androidproject1.core.ui.text.UiText

/**
 * The rules a screen applies to a set of fields at once.
 *
 * `LoginState.canSubmit` used to be a hand-rolled boolean, which could say *whether* submit was
 * disabled but never *why*. Deriving it from the fields means the two can never disagree, and the
 * reason is already there to show.
 */
object Form {

    /** True when every field passes. What a submit button's `enabled` should read. */
    fun canSubmit(vararg fields: FieldState): Boolean = fields.all { it.isValid }

    /** The first thing wrong, in field order — for a summary line or a snackbar. */
    fun firstError(vararg fields: FieldState): UiText? =
        fields.firstNotNullOfOrNull { it.error }

    /**
     * Marks every field touched, so a submit pressed on an untouched form reveals every error at
     * once instead of one per visit.
     */
    fun touchAll(vararg fields: FieldState): List<FieldState> = fields.map { it.touch() }
}
