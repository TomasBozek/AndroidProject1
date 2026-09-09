package com.example.androidproject1.feature.auth.data.source

import com.example.androidproject1.feature.auth.data.TestSessionStore
import com.example.androidproject1.feature.auth.data.newAesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric ships an SDK image per API level and has none for this project's targetSdk. */
private const val ROBOLECTRIC_SDK = 35

private val ADA = StoredSession(id = "1c2f", email = "ada@example.com")

/** The data source's own field separator, spelled out so the malformed cases below are readable. */
private const val SEPARATOR = "\u001F"

/**
 * The session, against the real encrypted store rather than a fake `DataStore`.
 *
 * The three cases the class promises all read as signed out — an absent file, an undecryptable one
 * and one written in a format this build does not know — are only real if the file is real, so
 * every test here goes through AES-GCM and the disk.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class DefaultLocalAuthDataSourceTest {

    private lateinit var store: TestSessionStore
    private lateinit var dataSource: DefaultLocalAuthDataSource

    @Before
    fun setUp() {
        store = TestSessionStore()
        dataSource = store.dataSource()
    }

    @Test
    fun `a store that was never written reads as signed out`() = runTest {
        assertFalse(store.file.exists())

        assertNull(dataSource.observeSession().first())
    }

    @Test
    fun `a session survives being written and read back`() = runTest {
        dataSource.setSession(ADA)

        // A second data source over the same file: the first one holding it in memory would make
        // this pass without anything having been persisted.
        assertEquals(ADA, store.dataSource().observeSession().first())
    }

    @Test
    fun `signing out clears the stored session`() = runTest {
        dataSource.setSession(ADA)

        dataSource.setSession(null)

        assertNull(dataSource.observeSession().first())
    }

    @Test
    fun `the address is not readable in the file`() = runTest {
        dataSource.setSession(ADA)

        // The whole reason the session is in an encrypted store: a copy of the data directory
        // must not say who was signed in.
        val bytes = store.file.readBytes()
        assertTrue(bytes.isNotEmpty())
        assertFalse(bytes.decodeToString().contains("ada@example.com"))
        assertFalse(bytes.decodeToString().contains("1c2f"))
    }

    @Test
    fun `a session another installation's key wrote reads as signed out`() = runTest {
        dataSource.setSession(ADA)
        // What restoring a backup to a new device does: the file arrives, the Keystore key does
        // not. Nothing here may throw — the honest answer is that there is no session any more.
        val restored = TestSessionStore(key = newAesKey())
        restored.file.parentFile?.mkdirs()
        store.file.copyTo(restored.file, overwrite = true)

        assertNull(restored.dataSource().observeSession().first())
    }

    @Test
    fun `a session in a format this build does not know reads as signed out`() = runTest {
        // Written through the store, so it is a properly encrypted value of an older shape — the
        // upgrade case, not the corruption one. Version 0, and one field too few.
        store.provider.dataStore.updateData { listOf("0", ADA.email).joinToString(SEPARATOR) }

        assertNull(dataSource.observeSession().first())
        assertTrue(store.logger.warnings.isNotEmpty())
    }

    @Test
    fun `a session with a separator in the address reads as signed out rather than truncated`() =
        runTest {
            // Not reachable through setSession today, but the decoder is what stands between a
            // malformed file and a Session carrying half an address.
            store.provider.dataStore.updateData {
                listOf("1", ADA.id, "ada", "example.com").joinToString(SEPARATOR)
            }

            assertNull(dataSource.observeSession().first())
        }
}
