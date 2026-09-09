package com.example.androidproject1.feature.profile.domain

/**
 * What this app knows about the person using it.
 *
 * A plain domain type: [avatarUri] is a `file://` string rather than anything Android names,
 * because this is a Kotlin/JVM module and `android.net.Uri` is not on its classpath — which is the
 * constraint that keeps the picture-handling in `data` where it belongs.
 *
 * @property avatarUri null until a picture has been chosen. The file it points at is owned by this
 * app, so it survives a restart; the picker's own URI would not.
 */
data class Profile(
    val name: String,
    val email: String,
    val avatarUri: String?,
) {

    companion object {

        /** Nothing saved yet — what a first run reads. */
        val EMPTY = Profile(name = "", email = "", avatarUri = null)
    }
}
