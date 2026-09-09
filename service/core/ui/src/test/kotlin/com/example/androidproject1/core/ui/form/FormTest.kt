package com.example.androidproject1.core.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Plain JVM: a validator returns a `UiText`, and a `UiText` holding a resource id is only an Int
 * until something resolves it. That is what keeps these tests off Robolectric.
 */
class FormTest {

    @Test
    fun `required rejects blank and whitespace`() {
        val rule = required()

        assertNotNull(rule(""))
        assertNotNull(rule("   "))
        assertNull(rule("a"))
    }

    @Test
    fun `email accepts an ordinary address`() {
        val rule = email()

        assertNull(rule("ada@example.com"))
        assertNull(rule("ada+tag@sub.example.co.uk"))
    }

    @Test
    fun `email rejects the typo it exists to catch`() {
        val rule = email()

        assertNotNull(rule("ada.example.com"))
        assertNotNull(rule("ada@example"))
        assertNotNull(rule("@example.com"))
    }

    @Test
    fun `email leaves blank to required`() {
        // Two rules on one field would otherwise both fire and the user sees the wrong one first.
        assertNull(email()(""))
    }

    @Test
    fun `minLength counts characters and ignores blank`() {
        val rule = minLength(8)

        assertNotNull(rule("short"))
        assertNull(rule("longenough"))
        assertNull(rule(""))
    }

    @Test
    fun `matches compares against the other field as it is now`() {
        var other = "hunter2"
        val rule = matches({ other })

        assertNull(rule("hunter2"))
        other = "changed"
        assertNotNull(rule("hunter2"))
    }

    @Test
    fun `an untouched field shows no error even when it is invalid`() {
        val field = FieldState.of(required())

        // The rule fails, but shouting before the user has typed is the behaviour this prevents.
        assertFalse(field.isValid)
        assertNull(field.error)
    }

    @Test
    fun `typing marks a field touched, so the error appears`() {
        val field = FieldState.of(required()).changed("a").changed("")

        assertNotNull(field.error)
    }

    @Test
    fun `touch reveals the error without changing the value`() {
        val field = FieldState.of(required(), value = "").touch()

        assertEquals("", field.value)
        assertNotNull(field.error)
    }

    @Test
    fun `the first failing rule wins`() {
        val field = FieldState.of(required(), email(), value = "").touch()

        // Blank is both "required" and not an email; the user should be told the useful one.
        assertEquals(required()(""), field.error)
    }

    @Test
    fun `canSubmit is false while any field is invalid`() {
        val good = FieldState.of(required(), value = "a")
        val bad = FieldState.of(required(), value = "")

        assertTrue(Form.canSubmit(good))
        assertFalse(Form.canSubmit(good, bad))
    }

    @Test
    fun `canSubmit ignores whether the errors are being shown`() {
        // An untouched invalid field still blocks submit — it just does so quietly.
        assertFalse(Form.canSubmit(FieldState.of(required())))
    }

    @Test
    fun `touchAll reveals every error at once`() {
        val fields = Form.touchAll(FieldState.of(required()), FieldState.of(required()))

        assertTrue(fields.all { it.error != null })
    }

    @Test
    fun `firstError reports in field order`() {
        val fields = arrayOf(
            FieldState.of(required(), value = "fine").touch(),
            FieldState.of(email(), value = "nope").touch(),
        )

        assertEquals(email()("nope"), Form.firstError(*fields))
    }
}
