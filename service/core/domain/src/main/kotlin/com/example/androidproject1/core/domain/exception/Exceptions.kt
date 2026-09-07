package com.example.androidproject1.core.domain.exception

/** The device could not reach the server at all. */
class NetworkErrorException(
    override val cause: Throwable? = null,
) : DomainException()

/** The server was reached but responded with a 5xx. */
class ServerErrorException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainException()

/** Credentials are missing or no longer valid. */
class UnauthorizedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainException()

/** The request was rejected as malformed. */
class BadRequestException(
    override val message: String? = null,
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainException()

/** The requested resource does not exist. */
class NotFoundException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DomainException()

/** Login failed for a reason the user can act on. */
class AuthenticationException(
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainException()

/** Anything unexpected — a bug, rather than a condition worth modelling. */
class InternalErrorException(
    override val message: String? = null,
    override val displayMessage: String? = null,
    override val cause: Throwable? = null,
) : DomainException()
