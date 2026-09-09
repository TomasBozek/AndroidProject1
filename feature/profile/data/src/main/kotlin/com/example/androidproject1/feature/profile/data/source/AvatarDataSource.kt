package com.example.androidproject1.feature.profile.data.source

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:profile:data` names it.
 *
 * The port for the one thing a profile owns that is not a string in a preferences file — a
 * picture, which arrives as a URI somebody else owns and has to become a file this app does.
 */
interface AvatarDataSource {

    /**
     * Copies the bytes at [sourceUri] into this app's own storage and returns the `file://` URI
     * they now live at. The name is new every time, so nothing serves the previous picture from a
     * cache keyed on it.
     */
    suspend fun store(sourceUri: String): String
}
