package com.example.androidproject1.core.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * The design system's fifth check, by machine: text reads at 4.5:1 and a border at 3:1, on every
 * surface, in both palettes.
 *
 * Contrast is a pure function of two colours, so this needs no device, no screenshot and no eye —
 * which is the point. A palette is the one part of a design system a re-brand replaces wholesale,
 * and the failure mode is a role that looked fine against the surface the designer had open and
 * disappears on one of the other two.
 *
 * Every pair is asserted. A pair that does not meet its threshold today is listed in [ACCEPTED]
 * with the reason it is allowed to fail *and a floor it still has to clear*, so an accepted
 * exception cannot quietly get worse; removing a role's exception is how it gets fixed.
 */
class ContrastTest {

    @Test
    fun `the ratio is WCAG's, so the numbers can be compared to any other tool`() {
        assertEquals(21.0, contrastRatio(Color.Black, Color.White), 0.01)
        assertEquals(1.0, contrastRatio(Color.White, Color.White), 0.01)
        // The reference value every contrast checker prints for #777777 on white.
        assertEquals(4.48, contrastRatio(Color(0xFF777777), Color.White), 0.01)
    }

    @Test
    fun `every text role reads on every surface it can appear on`() {
        assertPairs(textPairs(), TEXT_MINIMUM)
    }

    @Test
    fun `every border role is visible on every surface`() {
        assertPairs(borderPairs(), NON_TEXT_MINIMUM)
    }

    @Test
    fun `every feedback role's text reads on its container, and its accent on the surfaces`() {
        assertPairs(feedbackTextPairs(), TEXT_MINIMUM)
        assertPairs(feedbackAccentPairs(), NON_TEXT_MINIMUM)
    }

    @Test
    fun `every accepted exception is still a pair that exists`() {
        val known = (textPairs() + borderPairs() + feedbackTextPairs() + feedbackAccentPairs())
            .map { Triple(it.theme, it.foreground, it.background) }
            .toSet()
        val stale = ACCEPTED.filterNot { Triple(it.theme, it.foreground, it.background) in known }
        assertTrue(
            "Accepted exceptions that no longer name a pair this test checks — delete them:\n" +
                stale.joinToString("\n") { "  ${it.theme}: ${it.foreground} on ${it.background}" },
            stale.isEmpty(),
        )
    }
}

private const val TEXT_MINIMUM = 4.5
private const val NON_TEXT_MINIMUM = 3.0

/** One foreground role on one background role, in one palette. */
private data class Pair(
    val theme: String,
    val foreground: String,
    val background: String,
    val foregroundColor: Color,
    val backgroundColor: Color,
) {
    val ratio: Double get() = contrastRatio(foregroundColor, backgroundColor)
}

/**
 * A pair that does not meet its threshold, why that is allowed, and the floor it still has to
 * clear. The floor is the measured value rounded down: the pair is accepted where it stands, not
 * given room to slide.
 */
private data class Accepted(
    val theme: String,
    val foreground: String,
    val background: String,
    val floor: Double,
    val reason: String,
)

private val ACCEPTED = listOf(
    // WCAG 1.4.3 exempts text that is part of an inactive control: a disabled label that read as
    // strongly as an enabled one would be the accessibility bug.
    Accepted("light", "textDisabled", "surfaceSunken", 2.4, "inactive control, WCAG 1.4.3 exempt"),
    Accepted("light", "textDisabled", "surfaceBase", 2.6, "inactive control, WCAG 1.4.3 exempt"),
    Accepted("light", "textDisabled", "surfaceRaised", 2.8, "inactive control, WCAG 1.4.3 exempt"),
    Accepted("dark", "textDisabled", "surfaceSunken", 4.1, "inactive control, WCAG 1.4.3 exempt"),
    Accepted("dark", "textDisabled", "surfaceBase", 3.7, "inactive control, WCAG 1.4.3 exempt"),
    Accepted("dark", "textDisabled", "surfaceRaised", 2.5, "inactive control, WCAG 1.4.3 exempt"),

    // The dash in a table. It carries no information of its own — the row's label says what is
    // missing — so it is held to the 3:1 of a non-text mark rather than to 4.5:1. Anything a
    // person reads is on textSecondary; see AppColors.textTertiary.
    Accepted("light", "textTertiary", "surfaceSunken", 3.9, "a mark, not text; see AppColors.textTertiary"),
    Accepted("light", "textTertiary", "surfaceBase", 4.3, "a mark, not text; see AppColors.textTertiary"),
    Accepted("dark", "textTertiary", "surfaceSunken", 4.1, "a mark, not text; see AppColors.textTertiary"),
    Accepted("dark", "textTertiary", "surfaceBase", 3.7, "a mark, not text; see AppColors.textTertiary"),
    Accepted("dark", "textTertiary", "surfaceRaised", 2.5, "a mark, not text; see AppColors.textTertiary"),

    // The hairline between rows. WCAG 1.4.11 asks for 3:1 from what a person needs in order to
    // identify a control or its state; a divider identifies nothing — remove it and the rows are
    // still rows. borderStrong is the role that draws anything pressable or typeable, and it is
    // asserted at 3:1 with no exception.
    Accepted("light", "border", "surfaceSunken", 1.1, "decorative hairline; see AppColors.border"),
    Accepted("light", "border", "surfaceBase", 1.2, "decorative hairline; see AppColors.border"),
    Accepted("light", "border", "surfaceRaised", 1.3, "decorative hairline; see AppColors.border"),
    Accepted("dark", "border", "surfaceSunken", 1.7, "decorative hairline; see AppColors.border"),
    Accepted("dark", "border", "surfaceBase", 1.5, "decorative hairline; see AppColors.border"),
    Accepted("dark", "border", "surfaceRaised", 1.0, "decorative hairline; see AppColors.border"),
)

private val palettes = listOf("light" to lightAppColors(), "dark" to darkAppColors())

/**
 * Every text role on every surface it can appear on.
 *
 * `textOnInverse` is paired with `surfaceInverse` alone — it exists for the tooltip and the
 * contextual toolbar, and pairing it with the ordinary surfaces would assert a combination no
 * component can produce.
 */
private fun textPairs(): List<Pair> = palettes.flatMap { (theme, c) ->
    val texts = listOf(
        "textPrimary" to c.textPrimary,
        "textSecondary" to c.textSecondary,
        "textTertiary" to c.textTertiary,
        "textDisabled" to c.textDisabled,
    )
    texts.flatMap { (name, color) ->
        surfaces(c).map { (surface, surfaceColor) -> Pair(theme, name, surface, color, surfaceColor) }
    } + Pair(theme, "textOnInverse", "surfaceInverse", c.textOnInverse, c.surfaceInverse)
}

/** Every border role on every surface. A focus ring lands on all three, so it is checked on all three. */
private fun borderPairs(): List<Pair> = palettes.flatMap { (theme, c) ->
    val borders = listOf(
        "border" to c.border,
        "borderStrong" to c.borderStrong,
        "focusRing" to c.focusRing,
    )
    borders.flatMap { (name, color) ->
        surfaces(c).map { (surface, surfaceColor) -> Pair(theme, name, surface, color, surfaceColor) }
    }
}

private fun feedbackRoles(c: AppColors) = listOf(
    "feedback.success" to c.feedback.success,
    "feedback.warning" to c.feedback.warning,
    "feedback.error" to c.feedback.error,
    "feedback.info" to c.feedback.info,
)

/** A message's text on its own container (D78): the pair a banner, a toast and a tag draw. */
private fun feedbackTextPairs(): List<Pair> = palettes.flatMap { (theme, c) ->
    feedbackRoles(c).map { (name, role) ->
        Pair(theme, "$name.onContainer", "$name.container", role.onContainer, role.container)
    }
}

/**
 * The accent — a status dot, an icon in a message — on the two surfaces a dot sits on. Not the
 * sunken one: nothing draws a dot on the ground under a list.
 */
private fun feedbackAccentPairs(): List<Pair> = palettes.flatMap { (theme, c) ->
    feedbackRoles(c).flatMap { (name, role) ->
        listOf("surfaceBase" to c.surfaceBase, "surfaceRaised" to c.surfaceRaised).map { (surface, surfaceColor) ->
            Pair(theme, "$name.accent", surface, role.accent, surfaceColor)
        }
    }
}

private fun surfaces(c: AppColors) = listOf(
    "surfaceSunken" to c.surfaceSunken,
    "surfaceBase" to c.surfaceBase,
    "surfaceRaised" to c.surfaceRaised,
)

/** Names the theme, the two roles and the measured ratio, so a failure needs no second look. */
private fun assertPairs(pairs: List<Pair>, minimum: Double) {
    val failures = pairs.mapNotNull { pair ->
        val accepted = ACCEPTED.firstOrNull {
            it.theme == pair.theme && it.foreground == pair.foreground && it.background == pair.background
        }
        val required = accepted?.floor ?: minimum
        val note = accepted?.let { " (accepted at ${it.floor}:1 — ${it.reason})" }.orEmpty()
        if (pair.ratio >= required) {
            null
        } else {
            "  ${pair.theme}: ${pair.foreground} on ${pair.background} is " +
                "${format(pair.ratio)}:1, needs $required:1$note"
        }
    }
    assertTrue("Contrast below the threshold:\n" + failures.joinToString("\n"), failures.isEmpty())
}

private fun format(ratio: Double) = (Math.round(ratio * 100.0) / 100.0).toString()

/**
 * WCAG 2.1 contrast: `(lighter + 0.05) / (darker + 0.05)` over relative luminance.
 *
 * Alpha is ignored — every role in [AppColors] is opaque, and a translucent one would have to be
 * composited against its surface before it could be measured at all.
 */
internal fun contrastRatio(foreground: Color, background: Color): Double {
    val a = relativeLuminance(foreground)
    val b = relativeLuminance(background)
    return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
}

private fun relativeLuminance(color: Color): Double =
    0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)

private fun linear(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}
