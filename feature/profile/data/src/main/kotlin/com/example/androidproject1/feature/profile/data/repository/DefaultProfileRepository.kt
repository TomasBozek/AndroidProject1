package com.example.androidproject1.feature.profile.data.repository

import com.example.androidproject1.core.data.BaseRepository
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.profile.data.source.AvatarDataSource
import com.example.androidproject1.feature.profile.data.source.LocalProfileDataSource
import com.example.androidproject1.feature.profile.domain.Profile
import com.example.androidproject1.feature.profile.domain.ProfileRepository

class DefaultProfileRepository(
    logger: Logger,
    private val localProfileDataSource: LocalProfileDataSource,
    private val avatarDataSource: AvatarDataSource,
) : ProfileRepository, BaseRepository(logger = logger.withTag("DefaultProfileRepository")) {

    override suspend fun get(): Outcome<Profile> = execute { localProfileDataSource.read() }

    override suspend fun save(profile: Profile): Outcome<Unit> = execute {
        localProfileDataSource.writeDetails(name = profile.name, email = profile.email)
    }

    override suspend fun setAvatar(sourceUri: String): Outcome<String> = execute {
        // Stored before it is recorded: a preference pointing at a file that was never written is
        // worse than no preference at all.
        val stored = avatarDataSource.store(sourceUri)
        localProfileDataSource.writeAvatarUri(stored)
        stored
    }
}
