package com.example.androidproject1.feature.auth.data.source

import kotlinx.coroutines.flow.Flow

/** Internal to the data layer: nothing above `:feature:auth:data` names it. */
interface LocalAuthDataSource {

    fun observeEmail(): Flow<String?>

    suspend fun setEmail(email: String?)
}
