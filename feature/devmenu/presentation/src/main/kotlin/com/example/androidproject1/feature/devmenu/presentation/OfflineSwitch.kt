package com.example.androidproject1.feature.devmenu.presentation

/**
 * The fixture engine's "pretend the server is down" flag (D20), as a switch rather than a file to
 * `touch` over adb.
 *
 * Read and written rather than observed: the flag lives outside this process — `dev`'s
 * `FixtureNetwork` backs it with a file — so there is nothing to collect, only something to
 * re-read. The screen reads it when it opens and after every write.
 *
 * Only the flavor that talks to fixtures has one; the others bind [Unsupported] and the row is not
 * drawn.
 */
interface OfflineSwitch {

    val isSupported: Boolean

    fun isOffline(): Boolean

    fun setOffline(offline: Boolean)

    /** For a build that talks to a real server: there is no fixture to break. */
    object Unsupported : OfflineSwitch {

        override val isSupported: Boolean = false

        override fun isOffline(): Boolean = false

        override fun setOffline(offline: Boolean) = Unit
    }
}
