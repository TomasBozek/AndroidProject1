package com.example.androidproject1.feature.settings.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** The tag is what the platform stores, so reading one back has to land on a case or the default. */
class AppLanguageTest {

    @Test
    fun `a shipped tag reads back as its language`() {
        assertEquals(AppLanguage.Czech, AppLanguage.fromTag("cs"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en"))
    }

    @Test
    fun `an empty list is the device's own language`() {
        assertEquals(AppLanguage.System, AppLanguage.fromTag(""))
    }

    @Test
    fun `a locale this build does not ship is the default, not a crash`() {
        assertEquals(AppLanguage.DEFAULT, AppLanguage.fromTag("de"))
        assertEquals(AppLanguage.DEFAULT, AppLanguage.fromTag(null))
    }
}
