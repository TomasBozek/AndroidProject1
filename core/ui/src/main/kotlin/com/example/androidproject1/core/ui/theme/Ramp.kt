package com.example.androidproject1.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Layer 1 — core. Raw values with no meaning attached and no light/dark modes.
 *
 * Five perceptually levelled ramps of nine to eleven steps: graphite carries surfaces and text,
 * green confirms, red cancels and deletes, blue is selection and information, amber warns. The
 * same step of any ramp matches the others in visual value, so a role can be swapped between
 * families without re-tuning the screen around it.
 *
 * **Nothing outside this package reads a ramp.** That is what `internal` is for here: a screen
 * asks for a role in [AppColors], never for `Green500`, so re-branding is a change to this file
 * and nothing else. See [AppTheme].
 */
internal object Ramp {

    // Graphite — surfaces, text, keys.
    val Gray50 = Color(0xFFF7F7F6)
    val Gray100 = Color(0xFFEEEEEC)
    val Gray200 = Color(0xFFDEDEDB)
    val Gray300 = Color(0xFFC4C4C0)
    val Gray400 = Color(0xFF9A9A95)
    val Gray500 = Color(0xFF75756F)
    val Gray600 = Color(0xFF55554F)
    val Gray700 = Color(0xFF3C3C37)
    val Gray800 = Color(0xFF292925)
    val Gray900 = Color(0xFF1A1A17)
    val Gray950 = Color(0xFF0E0E0C)

    // Green — confirm, pay, paid.
    val Green50 = Color(0xFFEDFBF2)
    val Green100 = Color(0xFFD0F4DF)
    val Green200 = Color(0xFFA2E8C1)
    val Green300 = Color(0xFF6DD69E)
    val Green400 = Color(0xFF3CBE7C)
    val Green500 = Color(0xFF1FA463)
    val Green600 = Color(0xFF17864F)
    val Green700 = Color(0xFF12693E)
    val Green800 = Color(0xFF0E4E2F)
    val Green900 = Color(0xFF0B3721)

    // Red — delete, error.
    val Red50 = Color(0xFFFFF1EF)
    val Red100 = Color(0xFFFFDCD7)
    val Red200 = Color(0xFFFFB9B0)
    val Red300 = Color(0xFFFF8E80)
    val Red400 = Color(0xFFF8604F)
    val Red500 = Color(0xFFE63A26)
    val Red600 = Color(0xFFC42C1B)
    val Red700 = Color(0xFF9C2114)
    val Red800 = Color(0xFF741A10)
    val Red900 = Color(0xFF4E140C)

    // Blue — card, selection, information.
    val Blue50 = Color(0xFFEFF5FF)
    val Blue100 = Color(0xFFDBE7FF)
    val Blue200 = Color(0xFFB8CEFF)
    val Blue300 = Color(0xFF8CADFF)
    val Blue400 = Color(0xFF5C88F7)
    val Blue500 = Color(0xFF3566E0)
    val Blue600 = Color(0xFF2650BC)
    val Blue700 = Color(0xFF1D3F96)
    val Blue800 = Color(0xFF172F6E)
    val Blue900 = Color(0xFF10204A)

    // Amber — in progress, warning.
    val Amber50 = Color(0xFFFFF8E8)
    val Amber100 = Color(0xFFFFEDC2)
    val Amber200 = Color(0xFFFFDA85)
    val Amber300 = Color(0xFFFFC44A)
    val Amber400 = Color(0xFFF7AE1E)
    val Amber500 = Color(0xFFDE9209)
    val Amber600 = Color(0xFFB87506)
    val Amber700 = Color(0xFF915B06)
    val Amber800 = Color(0xFF6B4306)
    val Amber900 = Color(0xFF472C05)

    val White = Color(0xFFFFFFFF)

    /**
     * Not a ramp step. The dark theme's raised surface sits between [Gray700] and [Gray800] —
     * a card has to lift off [Gray900] without reading as a third grey.
     */
    val DarkRaised = Color(0xFF383833)
}
