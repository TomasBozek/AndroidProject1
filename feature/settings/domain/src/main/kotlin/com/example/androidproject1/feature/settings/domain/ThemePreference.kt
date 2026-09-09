package com.example.androidproject1.feature.settings.domain

/**
 * Which palette the app draws in.
 *
 * Three cases rather than a `Boolean`, because [System] is a real choice and not the absence of
 * one: it is the app following the device, and it has to survive being stored and read back as
 * itself. A two-state switch cannot say it.
 *
 * The order is the order the segmented control draws, so a screen shows `entries` rather than
 * listing the cases again.
 */
enum class ThemePreference {

    /** Follow the device's own light/dark setting. This is what an app with no stored choice does. */
    System,

    Light,

    Dark,
    ;

    companion object {

        val DEFAULT = System
    }
}
