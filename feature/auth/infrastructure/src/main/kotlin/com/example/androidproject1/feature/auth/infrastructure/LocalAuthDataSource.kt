package com.example.androidproject1.feature.auth.infrastructure

import kotlinx.coroutines.flow.Flow

/**
 * Declared in infrastructure, implemented in `:feature:auth:data`. This inversion is what keeps
 * the data layer depending on infrastructure rather than the other way round.
 */
interface LocalAuthDataSource {

    fun observeEmail(): Flow<String?>

    suspend fun setEmail(email: String?)
}
