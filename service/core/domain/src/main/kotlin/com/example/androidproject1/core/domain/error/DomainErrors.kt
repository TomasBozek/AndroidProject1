package com.example.androidproject1.core.domain.error

/** The device could not reach the server at all. */
class NetworkError(
    override val cause: Throwable? = null,
) : DomainError()

/** The server was reached but answered with a 5xx. */
class ServerError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainError()

/** Credentials are missing or no longer valid. */
class UnauthorizedError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainError()

/** The request was rejected as malformed. */
class BadRequestError(
    override val message: String? = null,
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainError()

/** The requested resource does not exist. */
class NotFoundError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainError()

/** Sign-in failed for a reason the user can act on. */
class CredentialsError(
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainError()

/** Anything unforeseen — a bug rather than a condition worth modelling. */
class UnexpectedError(
    override val message: String? = null,
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainError()
