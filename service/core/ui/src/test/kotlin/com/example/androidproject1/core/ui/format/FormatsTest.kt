package com.example.androidproject1.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Two locales that disagree about everything: Czech groups with a space, puts the decimal comma in
 * and the currency symbol after; English does none of those. A role is only worth having if it is
 * right in both, so both are asserted rather than one.
 *
 * **Spaces are normalised before comparison.** CLDR writes a Czech group separator as a
 * non-breaking space and has changed which one over JDK versions, so asserting the exact code
 * point would make this a test of the JDK. What matters is the digits, the separators and the
 * order.
 */
private val CZECH = Locale.forLanguageTag("cs-CZ")
private val ENGLISH = Locale.US

/** Every flavour of space CLDR uses as a group separator, flattened to one. */
private fun String.normalised(): String = replace(' ', ' ').replace(' ', ' ')

class FormatsTest {

    private val cs = Formats(CZECH)
    private val en = Formats(ENGLISH)

    @Test
    fun `money writes the locale's own form`() {
        assertEquals("1 248,50 Kč", cs.money(124850).normalised())
        assertEquals("$1,248.50", en.money(124850))
    }

    @Test
    fun `money at zero is a price, not an empty string`() {
        // Free is a price. A screen that special-cases zero ends up with two spellings of it.
        assertEquals("0,00 Kč", cs.money(0).normalised())
        assertEquals("$0.00", en.money(0))
    }

    @Test
    fun `a negative amount keeps its minus sign`() {
        // A refund line is negative, and a locale that writes debts in brackets would hide it.
        assertTrue(cs.money(-2400).normalised(), cs.money(-2400).normalised().startsWith("-24,00"))
        assertEquals("-$24.00", en.money(-2400))
    }

    @Test
    fun `moneyShort drops the fraction and rounds half up`() {
        assertEquals("1 249 Kč", cs.moneyShort(124850).normalised())
        assertEquals("$1,249", en.moneyShort(124850))
        assertEquals("$1,248", en.moneyShort(124849))
    }

    @Test
    fun `weight is written in kilograms to the gram`() {
        assertEquals("0,420 kg", cs.weight(420).normalised())
        assertEquals("0.420 kg", en.weight(420))
        assertEquals("1.500 kg", en.weight(1500))
    }

    @Test
    fun `quantity is grouped the way the locale groups`() {
        assertEquals("1 248", cs.quantity(1248).normalised())
        assertEquals("1,248", en.quantity(1248))
        assertEquals("0", en.quantity(0))
    }

    @Test
    fun `percent takes a fraction, never an already multiplied number`() {
        assertEquals("21 %", cs.percent(0.21).normalised())
        assertEquals("21%", en.percent(0.21))
        assertEquals("0%", en.percent(0.0))
    }

    @Test
    fun `time and date follow the locale`() {
        val time = LocalTime.of(14, 5)
        assertEquals("14:05", cs.time(time).normalised())
        assertEquals("2:05 PM", en.time(time).normalised())

        val date = LocalDate.of(2026, 9, 9)
        assertTrue(cs.date(date), cs.date(date).contains("2026"))
        assertEquals("Sep 9, 2026", en.date(date))
    }

    @Test
    fun `duration shows the largest two units`() {
        assertEquals("2 h 10 m", en.duration(2.hours + 10.minutes))
        assertEquals("45 m 0 s", en.duration(45.minutes))
        assertEquals("30 s", en.duration(30.seconds))
    }

    @Test
    fun `a duration of nothing still reads as a duration`() {
        assertEquals("0 m 0 s", en.duration(0.seconds))
    }

    @Test
    fun `a negative duration keeps its sign`() {
        assertEquals("-2 h 10 m", en.duration(-(2.hours + 10.minutes)))
    }

    @Test
    fun `the default locale is the device's`() {
        assertEquals(Locale.getDefault(), Formats().locale)
    }
}
