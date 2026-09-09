package com.example.androidproject1.feature.onboarding.data.source

import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:onboarding:data` names it. What the rest of the app depends on is the repository
 * interface in `domain`.
 */
interface LocalOnboardingDataSource {

    fun observeSeen(): Flow<Boolean>

    suspend fun setSeen(seen: Boolean)
}
