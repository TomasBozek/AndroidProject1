package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.ErrorTracker
import com.example.androidproject1.core.domain.Logger

/**
 * A [Logger] that also reports what it logs to an [ErrorTracker].
 *
 * A decorator rather than a parameter on `BaseViewModel` and `BaseRepository`: the four places
 * worth reporting already log with a `Throwable`, so wrapping the binding turns all four into
 * reports without adding an infrastructure argument to every ViewModel constructor in the app and
 * to every one the generators will write.
 *
 * Only calls carrying a throwable are recorded. A `w` with no exception is a note to a developer
 * reading logcat, not a defect.
 */
class TrackingLogger(
    private val delegate: Logger,
    private val tracker: ErrorTracker,
    private val tag: String? = null,
) : Logger {

    override fun withTag(tag: String): Logger =
        TrackingLogger(delegate.withTag(tag), tracker, tag)

    override fun d(throwable: Throwable?, message: () -> String) {
        delegate.d(throwable, message)
        // Breadcrumb only: a debug line is context for a later report, never a report itself.
        if (throwable == null) tracker.log(qualify(message()))
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        delegate.w(throwable, message)
        if (throwable != null) tracker.recordNonFatal(throwable, qualify(message()))
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        delegate.e(throwable, message)
        if (throwable != null) tracker.recordNonFatal(throwable, qualify(message()))
    }

    private fun qualify(message: String): String =
        if (tag == null) message else "$tag: $message"
}
