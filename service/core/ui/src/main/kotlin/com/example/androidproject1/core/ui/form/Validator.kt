package com.example.androidproject1.core.ui.form

import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toPluralUiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.R

/**
 * A rule a field's value has to pass.
 *
 * Returns the message to show, or `null` when the value is fine — so a validator is a plain
 * function and a custom one needs no class. The message is a [UiText] because a ViewModel has no
 * Context, which is the same reason everything else reachable from one is.
 */
fun interface Validator {

    operator fun invoke(value: String): UiText?
}

/** Not blank. The rule every required field has, spelled once. */
fun required(message: UiText = R.string.core_field_required.toUiText()) = Validator { value ->
    message.takeIf { value.isBlank() }
}

/**
 * Looks like an address.
 *
 * Deliberately permissive: the only way to know an address is real is to send to it, and a strict
 * pattern rejects valid addresses — plus signs, new TLDs, unicode local parts. This catches the
 * typo, and the server decides the rest.
 */
fun email(message: UiText = R.string.core_field_email.toUiText()) = Validator { value ->
    message.takeIf { value.isNotBlank() && !EMAIL_SHAPE.matches(value) }
}

/** At least [length] characters. Blank passes: that is [required]'s job, not this one's. */
fun minLength(
    length: Int,
    message: UiText = R.plurals.core_field_min_length.toPluralUiText(length, length),
) = Validator { value ->
    message.takeIf { value.isNotBlank() && value.length < length }
}

/** Equal to whatever [other] returns — a confirm-password field, typically. */
fun matches(
    other: () -> String,
    message: UiText = R.string.core_field_mismatch.toUiText(),
) = Validator { value ->
    message.takeIf { value != other() }
}

private val EMAIL_SHAPE = Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")
