package com.example.androidproject1.feature.profile.domain

import com.example.androidproject1.core.domain.result.Outcome

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 *
 * Read once rather than observed: this backs a form, and a flow that re-emits while someone is
 * typing overwrites what they typed. Nothing else in the app changes a profile, so there is
 * nothing to observe.
 */
interface ProfileRepository {

    suspend fun get(): Outcome<Profile>

    suspend fun save(profile: Profile): Outcome<Unit>

    /**
     * Copies the picture at [sourceUri] into storage this app owns and records it as the avatar.
     *
     * Both routes end here — the Photo Picker's grant lasts only as long as the process, and the
     * camera writes to a cache file that is not ours to keep. Returns the stored `file://` URI,
     * which is new on every call so an image loader cannot serve the previous picture from cache.
     */
    suspend fun setAvatar(sourceUri: String): Outcome<String>
}
