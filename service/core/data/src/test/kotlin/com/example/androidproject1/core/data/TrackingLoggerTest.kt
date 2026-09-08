package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.ErrorTracker
import com.example.androidproject1.core.domain.test.FakeLogger
import org.junit.Assert.assertEquals
import org.junit.Test

private class RecordingTracker : ErrorTracker {
    val nonFatals = mutableListOf<Pair<Throwable, String?>>()
    val breadcrumbs = mutableListOf<String>()
    var lastUser: String? = "unset"

    override fun recordNonFatal(throwable: Throwable, message: String?) {
        nonFatals += throwable to message
    }

    override fun log(message: String) {
        breadcrumbs += message
    }

    override fun setUser(id: String?) {
        lastUser = id
    }
}

class TrackingLoggerTest {

    private val tracker = RecordingTracker()
    private val logger = TrackingLogger(FakeLogger(), tracker)

    @Test
    fun `a warning with a throwable is reported`() {
        val boom = IllegalStateException("boom")

        logger.w(throwable = boom) { "Repository call failed" }

        assertEquals(listOf(boom to "Repository call failed"), tracker.nonFatals)
    }

    @Test
    fun `an error with a throwable is reported`() {
        val boom = IllegalStateException("boom")

        logger.e(throwable = boom) { "Unhandled" }

        assertEquals(listOf(boom to "Unhandled"), tracker.nonFatals)
    }

    @Test
    fun `a warning with no throwable is not a defect`() {
        // A `w` with no exception is a note to whoever is reading logcat. Reporting those would
        // bury the real ones.
        logger.w { "Dropped navigation — channel full" }

        assertEquals(emptyList<Pair<Throwable, String?>>(), tracker.nonFatals)
    }

    @Test
    fun `a debug line is a breadcrumb`() {
        logger.d { "Signing in" }

        assertEquals(listOf("Signing in"), tracker.breadcrumbs)
        assertEquals(emptyList<Pair<Throwable, String?>>(), tracker.nonFatals)
    }

    @Test
    fun `the tag travels with the report`() {
        val boom = IllegalStateException("boom")

        logger.withTag("LoginViewModel").w(throwable = boom) { "Unhandled exception" }

        assertEquals(listOf(boom to "LoginViewModel: Unhandled exception"), tracker.nonFatals)
    }

    @Test
    fun `everything still reaches the underlying logger`() {
        val delegate = FakeLogger()

        TrackingLogger(delegate, tracker).w(throwable = IllegalStateException()) { "still logged" }

        assertEquals(1, delegate.warnings.size)
    }
}
