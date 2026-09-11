package com.example.androidproject1.feature.profile.data.repository

import com.example.androidproject1.feature.profile.data.source.LocalProfileDataSource
import com.example.androidproject1.feature.profile.data.test.FakeAvatarDataSource
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.test.FakeLogger
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No Room, no DataStore, no [android.content.Context]: [FakeAvatarDataSource] and an in-memory
 * [LocalProfileDataSource] are enough to test the one thing this repository does that is not
 * straight delegation — that `setAvatar` writes in order, and what a failed copy leaves behind.
 */
class DefaultProfileRepositoryTest {

    private val local = InMemoryLocalProfileDataSource()
    private val avatar = FakeAvatarDataSource()
    private val repository = DefaultProfileRepository(
        logger = FakeLogger(),
        localProfileDataSource = local,
        avatarDataSource = avatar,
    )

    @Test
    fun `a stored avatar is recorded against the profile`() = runTest {
        val outcome = repository.setAvatar("content://picker/1")

        val stored = (outcome as Outcome.Success).data
        assertEquals("content://picker/1", avatar.stored)
        assertEquals(stored, local.read().avatarUri)
    }

    @Test
    fun `a copy that fails leaves nothing recorded`() = runTest {
        avatar.failWith = IllegalStateException("no longer readable")

        val outcome = repository.setAvatar("content://picker/1")

        assertTrue(outcome is Outcome.Failure)
        assertNull(local.read().avatarUri)
    }

    private class InMemoryLocalProfileDataSource : LocalProfileDataSource {

        private var profile = Profile.EMPTY

        override suspend fun read(): Profile = profile

        override suspend fun writeDetails(name: String, email: String) {
            profile = profile.copy(name = name, email = email)
        }

        override suspend fun writeAvatarUri(uri: String) {
            profile = profile.copy(avatarUri = uri)
        }
    }
}
