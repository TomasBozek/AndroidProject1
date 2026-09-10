package com.example.androidproject1.feature.onboarding.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 *
 * One flag, observed rather than read once, because it is what decides the flow the app is in:
 * `MainViewModel` combines it with the session, and the onboarding screen finishes by writing it
 * and letting that combination move the app on. A read would leave the tour on screen.
 */
interface OnboardingRepository {

    /** `false` until the tour has been finished once — a first run is not a failure. */
    fun observeSeen(): Flow<Outcome<Boolean>>

    suspend fun markSeen(): Outcome<Unit>
}
