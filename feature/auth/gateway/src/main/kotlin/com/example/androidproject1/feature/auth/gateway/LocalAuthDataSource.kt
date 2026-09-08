package com.example.androidproject1.feature.auth.gateway

import kotlinx.coroutines.flow.Flow

/** Declared in gateway, implemented in `:feature:auth:data` — hence data depends on gateway. */
interface LocalAuthDataSource {

    fun observeEmail(): Flow<String?>

    suspend fun setEmail(email: String?)
}
