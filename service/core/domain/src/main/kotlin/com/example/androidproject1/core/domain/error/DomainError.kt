package com.example.androidproject1.core.domain.error

/**
 * Base class of everything that can go wrong inside the domain.
 *
 * @property displayMessage localized text that may be shown to the user.
 * @property message debug text, never shown to the user.
 */
abstract class DomainError(
    open val displayMessage: String? = null,
    override val message: String? = displayMessage,
    override val cause: Throwable? = null,
) : Exception()
