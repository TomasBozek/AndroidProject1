package com.example.androidproject1.feature.profile.data.source

import com.example.androidproject1.feature.profile.domain.Profile

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:profile:data` names it. What the rest of the app depends on is the repository
 * interface in `domain`.
 */
interface LocalProfileDataSource {

    /** [Profile.EMPTY] when nothing has been saved yet — a first run is not a failure. */
    suspend fun read(): Profile

    /** Name and e-mail only; the avatar has its own write because it is saved on its own. */
    suspend fun writeDetails(name: String, email: String)

    suspend fun writeAvatarUri(uri: String)
}
