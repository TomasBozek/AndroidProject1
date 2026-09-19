package com.example.androidproject1.feature.devmenu.presentation

/**
 * The fixture engine's "talk to the real TMDB" flag (D81), as a switch rather than a file to
 * `touch` over adb.
 *
 * Read and written rather than observed, like [OfflineSwitch]: the flag lives outside this
 * process — `dev`'s `FixtureNetwork` backs it with a file — so there is nothing to collect, only
 * something to re-read. Without a key the switch is drawn disabled with a hint, because a flag
 * nobody can act on is a lie.
 *
 * Only the flavor that talks to fixtures has one; the others bind [Unsupported] and the row is not
 * drawn — they talk to the real host already.
 */
interface ApiSwitch {

    val isSupported: Boolean

    /** `tmdb.apiKey` was in `local.properties` when this build was made. */
    val isKeyPresent: Boolean

    fun isRealApi(): Boolean

    fun setRealApi(realApi: Boolean)

    /** For a build that talks to a real server: there is nothing to switch. */
    object Unsupported : ApiSwitch {

        override val isSupported: Boolean = false

        override val isKeyPresent: Boolean = false

        override fun isRealApi(): Boolean = false

        override fun setRealApi(realApi: Boolean) = Unit
    }
}
