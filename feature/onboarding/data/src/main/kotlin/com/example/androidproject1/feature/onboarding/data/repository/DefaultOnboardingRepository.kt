package com.example.androidproject1.feature.onboarding.data.repository

import com.example.androidproject1.feature.onboarding.data.source.LocalOnboardingDataSource
import com.example.androidproject1.feature.onboarding.domain.OnboardingRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultOnboardingRepository(
    logger: Logger,
    private val localOnboardingDataSource: LocalOnboardingDataSource,
) : OnboardingRepository, BaseRepository(logger = logger.withTag("DefaultOnboardingRepository")) {

    // `retries`, because the collector is `MainViewModel` and lives as long as the process: an
    // `observe` failure is terminal, so one transient read error would strand the app on the
    // splash screen. Same reason `DefaultAuthRepository.observeSession()` passes it.
    override fun observeSeen(): Flow<Outcome<Boolean>> =
        observe(source = localOnboardingDataSource.observeSeen(), retries = RETRIES)

    override suspend fun markSeen(): Outcome<Unit> = execute {
        localOnboardingDataSource.setSeen(true)
    }

    private companion object {

        const val RETRIES = 3L
    }
}
