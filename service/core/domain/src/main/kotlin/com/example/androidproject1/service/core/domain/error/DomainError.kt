package com.example.androidproject1.service.core.domain.error

/**
 * Base class of everything that can go wrong inside the domain.
 *
 * @property message debug text, never shown to the user. What the user sees is decided in the UI
 * layer from the error's *type* — `BaseViewModel.handleError` maps each one to a `core_error_*`
 * string — never from text carried up from below, which could not be localized.
 */
abstract class DomainError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception()
