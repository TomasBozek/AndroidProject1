package com.example.androidproject1.core.domain

/**
 * Where a defect goes to be counted.
 *
 * Free of `android.*` and of any vendor, like everything else in this module. **The repo carries no
 * crash-reporting SDK and no vendor config file** — a template that shipped one would make every
 * project that starts from it either use that vendor or unpick it first. What it carries is the
 * seam.
 *
 * Nothing calls this directly. `TrackingLogger` in `:service:core:data` decorates whatever `Logger`
 * is bound and forwards anything logged with a `Throwable`, so the paths that already report a
 * problem — `BaseViewModel.handleError`, `BaseRepository`'s failure paths — report here too
 * without a signature changing anywhere.
 */
interface ErrorTracker {

    /** Something went wrong and was handled. The app carried on. */
    fun recordNonFatal(throwable: Throwable, message: String? = null)

    /**
     * Breadcrumbs. A crash report is only as useful as what led up to it, and the last few things
     * the app did are usually the whole story.
     */
    fun log(message: String)

    /**
     * Who this is, for grouping reports — an opaque id, never an email or a name. Pass `null` on
     * sign-out so the next person's reports are not attributed to the last one.
     */
    fun setUser(id: String?)

    companion object {

        /** Records nothing. The default in tests, and in a build with no tracker configured. */
        val NoOp: ErrorTracker = object : ErrorTracker {
            override fun recordNonFatal(throwable: Throwable, message: String?) = Unit

            override fun log(message: String) = Unit

            override fun setUser(id: String?) = Unit
        }
    }
}

/**
 * The default binding: reports go to the log and nowhere else.
 *
 * That is a real implementation rather than a placeholder — in a debug build it is what you want,
 * and in a project that never adds a vendor it stays correct. Swapping in Crashlytics or Sentry is
 * a different binding in `:app`; see CLAUDE.md.
 */
class LoggingErrorTracker(logger: Logger) : ErrorTracker {

    private val logger = logger.withTag("ErrorTracker")

    override fun recordNonFatal(throwable: Throwable, message: String?) {
        logger.e(throwable = throwable) { message ?: "Non-fatal" }
    }

    override fun log(message: String) {
        logger.d { message }
    }

    override fun setUser(id: String?) {
        logger.d { "User is now ${id ?: "signed out"}" }
    }
}
