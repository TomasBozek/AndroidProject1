package com.example.androidproject1.feature.settings.domain

/**
 * Which language the app's strings are shown in.
 *
 * One case per locale the app ships — `values` and `values-cs` — plus [System], which is a real
 * choice and not the absence of one: the app following the device. The order is the order the
 * picker draws, so a screen shows `entries` rather than listing the cases again.
 *
 * @property tag the BCP 47 tag the platform stores, or `""` for [System]: "follow the device" is
 *   an empty locale list to `AppCompatDelegate`, and the empty string is how that reads back.
 */
enum class AppLanguage(val tag: String) {

    /** Follow the device's own language setting. What an app with no stored choice does. */
    System(""),

    English("en"),

    Czech("cs"),
    ;

    companion object {

        val DEFAULT = System

        /** An unrecognised or absent tag is a first run, or a locale this build does not ship. */
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: DEFAULT
    }
}
