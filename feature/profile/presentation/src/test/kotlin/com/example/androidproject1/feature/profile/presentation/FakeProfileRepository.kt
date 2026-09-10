package com.example.androidproject1.feature.profile.presentation

import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.feature.profile.domain.ProfileRepository
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome

/**
 * In-memory [ProfileRepository].
 *
 * Here rather than in `:feature:profile:domain`'s `testFixtures`: only this module tests against
 * it. A fixtures source set exists to stop a second copy of a fake being written, not to hold the
 * first one — move it there the day something outside this module needs it.
 */
class FakeProfileRepository(
    private var profile: Profile = Profile.EMPTY,
    var failWith: DomainError? = null,
) : ProfileRepository {

    /** What `save` was last called with, so a test can assert the values and not just the call. */
    var saved: Profile? = null
        private set

    override suspend fun get(): Outcome<Profile> =
        failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(profile)

    override suspend fun save(profile: Profile): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        saved = profile
        this.profile = profile
        return Outcome.Success(Unit)
    }

    override suspend fun setAvatar(sourceUri: String): Outcome<String> {
        failWith?.let { return Outcome.Failure(it) }
        // The real one copies the bytes and hands back a URI of its own. What a caller depends on
        // is exactly that: what it gets back is not the URI it passed in.
        val stored = "file:///data/avatar/stored.jpg"
        profile = profile.copy(avatarUri = stored)
        return Outcome.Success(stored)
    }
}
