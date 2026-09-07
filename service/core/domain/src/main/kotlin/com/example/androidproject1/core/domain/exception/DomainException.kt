package com.example.androidproject1.core.domain.exception

/**
 * Base class of all domain errors.
 *
 * @property displayMessage Localized message presentable to the user.
 * @property message Debug message, not presentable to the user.
 * @property cause Cause of this throwable.
 */
abstract class DomainException(
    open val displayMessage: String? = null,
    override val message: String? = displayMessage,
    override val cause: Throwable? = null,
) : Exception()
