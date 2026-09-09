package com.example.androidproject1.feature.auth.data.repository

import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.feature.auth.data.TestSessionStore
import com.example.androidproject1.feature.auth.data.source.LocalAuthDataSource
import com.example.androidproject1.feature.auth.data.source.StoredSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** Robolectric ships an SDK image per API level and has none for this project's targetSdk. */
private const val ROBOLECTRIC_SDK = 35

private const val ADA = "ada@example.com"

/**
 * Sign-in end to end, over the real encrypted store.
 *
 * The repository is four lines of its own, so testing it against a fake source would assert that
 * this test's own map lookup works. What is worth an assertion is the pair: that signing in leaves
 * something the next reader — `MainViewModel`, on the next cold start — actually finds.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class DefaultAuthRepositoryTest {

    private lateinit var store: TestSessionStore

    @Before
    fun setUp() {
        store = TestSessionStore()
    }

    private fun repository(
        source: LocalAuthDataSource = store.dataSource(),
        newSessionId: (() -> String)? = null,
    ) = if (newSessionId == null) {
        // The real generator, so the id test below is about what ships rather than about a
        // lambda this test just handed it.
        DefaultAuthRepository(logger = FakeLogger(), localAuthDataSource = source)
    } else {
        DefaultAuthRepository(FakeLogger(), source, newSessionId)
    }

    private suspend fun session() =
        (repository().observeSession().first() as Outcome.Success).data

    @Test
    fun `signing in leaves a session the next reader finds`() = runTest {
        repository(newSessionId = { "session-1" }).login(ADA)

        assertEquals("session-1", session()?.id)
        assertEquals(ADA, session()?.email)
    }

    @Test
    fun `nobody is signed in until somebody signs in`() = runTest {
        assertNull(session())
    }

    @Test
    fun `signing out clears the session`() = runTest {
        val repository = repository()
        repository.login(ADA)

        repository.logout()

        assertNull(session())
    }

    @Test
    fun `skipping the form signs in as a guest rather than as nobody`() = runTest {
        // A blank address must not read as signed out, or the app bounces straight back to login.
        repository().login("   ")

        assertEquals("guest@example.com", session()?.email)
    }

    @Test
    fun `the session id says nothing about the address`() = runTest {
        // The id is what reaches the crash reporter, so this is the privacy property rather than
        // a detail: a value derived from the address is the address to anyone holding a list.
        repository().login(ADA)
        val first = session()?.id.orEmpty()

        repository().login(ADA)
        val second = session()?.id.orEmpty()

        // Not a hash of the address either: the same address twice gives two ids, which no
        // function of the address alone can do.
        assertNotEquals(first, second)
        assertFalse(first.contains(ADA))
    }

    @Test
    fun `a read failure is reported rather than thrown`() = runTest {
        val outcome = repository(source = FlakyLocalAuthDataSource(readFailures = Int.MAX_VALUE))
            .observeSession()
            .first()

        assertTrue(outcome is Outcome.Failure)
    }

    @Test
    fun `a transient read failure does not end the session flow`() = runTest {
        // MainViewModel collects this for the whole process lifetime. Without the retries the
        // repository asks for, one unlucky read would sign the user out until they restarted.
        val source = FlakyLocalAuthDataSource(readFailures = 2)
        val repository = repository(source = source, newSessionId = { "session-1" })
        repository.login(ADA)

        val outcome = repository.observeSession().first()

        assertEquals("session-1", (outcome as Outcome.Success).data?.id)
    }

    @Test
    fun `a write failure is reported rather than thrown`() = runTest {
        val repository = repository(source = FlakyLocalAuthDataSource(failWrites = true))

        assertTrue(repository.login(ADA) is Outcome.Failure)
        assertTrue(repository.logout() is Outcome.Failure)
    }
}

/**
 * A source that fails a fixed number of times before working.
 *
 * A source that always fails cannot tell a retried flow from an abandoned one, which is the whole
 * question `retries` exists to answer.
 */
private class FlakyLocalAuthDataSource(
    private val readFailures: Int = 0,
    private val failWrites: Boolean = false,
) : LocalAuthDataSource {

    private val stored = MutableStateFlow<StoredSession?>(null)

    private var reads = 0

    override fun observeSession(): Flow<StoredSession?> = flow {
        if (reads++ < readFailures) throw IOException("the disk is busy")
        emitAll(stored)
    }

    override suspend fun setSession(session: StoredSession?) {
        if (failWrites) throw IOException("the disk is full")
        stored.value = session
    }
}
