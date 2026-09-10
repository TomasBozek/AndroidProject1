package com.example.androidproject1.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What a screen is handed at each size class, and what a pointer changes.
 *
 * This is the half of the size-class work a preview cannot state: a preview shows that a tablet
 * looks different, and this says in what — which typography, which touch target, which row height.
 */
@RunWith(RobolectricTestRunner::class)
class AppDensityTest {

    @get:Rule
    val compose = createComposeRule()

    private var sizeClass by mutableStateOf(SizeClass.Compact)
    private var pointer by mutableStateOf(false)
    private var seen: Tokens? = null
    private var composed = false

    @Test
    fun `the breakpoints are 720 and 1280`() {
        assertEquals(SizeClass.Compact, sizeClassFor(719))
        assertEquals(SizeClass.Regular, sizeClassFor(720))
        assertEquals(SizeClass.Regular, sizeClassFor(1279))
        assertEquals(SizeClass.Expanded, sizeClassFor(1280))
    }

    @Test
    fun `a phone gets the compact scale and a 48 dp target`() {
        val tokens = themeFor(SizeClass.Compact)

        assertEquals(48.dp, tokens.density.minTouchTarget)
        assertEquals(56.dp, tokens.density.listRowHeight)
        assertEquals(compactTypography(), tokens.typography)
    }

    @Test
    fun `a tablet gets the regular scale and a 56 dp target`() {
        val tokens = themeFor(SizeClass.Regular)

        assertEquals(56.dp, tokens.density.minTouchTarget)
        assertEquals(regularTypography(), tokens.typography)
        assertTrue(
            "Regular typography has to be larger than compact, or the size class buys nothing",
            tokens.typography.bodyMd.fontSize.value > compactTypography().bodyMd.fontSize.value,
        )
    }

    @Test
    fun `a pointer gets 40 dp targets and 48 dp rows whatever the width`() {
        for (entry in SizeClass.entries) {
            val tokens = themeFor(entry, pointer = true)

            assertEquals("$entry with a pointer", 40.dp, tokens.density.minTouchTarget)
            assertEquals("$entry with a pointer", 48.dp, tokens.density.listRowHeight)
            assertTrue("$entry with a pointer", tokens.density.pointer)
        }
    }

    @Test
    fun `a pointer changes the target, not the typography`() {
        val touch = themeFor(SizeClass.Regular).typography
        val mouse = themeFor(SizeClass.Regular, pointer = true).typography

        assertEquals(touch, mouse)
    }

    private class Tokens(val density: AppDensity, val typography: AppTypography)

    /**
     * Composes the theme once and reads back what a screen inside it would see. The inputs are
     * state, so each call recomposes rather than calling `setContent` a second time.
     */
    private fun themeFor(sizeClass: SizeClass, pointer: Boolean = false): Tokens {
        if (!composed) {
            compose.setContent {
                AppTheme(sizeClass = this.sizeClass, pointer = this.pointer) {
                    seen = Tokens(AppTheme.density, AppTheme.typography)
                }
            }
            composed = true
        }
        this.sizeClass = sizeClass
        this.pointer = pointer
        compose.waitForIdle()
        return checkNotNull(seen)
    }
}
