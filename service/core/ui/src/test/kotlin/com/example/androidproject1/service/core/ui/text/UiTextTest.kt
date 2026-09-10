package com.example.androidproject1.service.core.ui.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.service.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Resolved against a real resource table rather than a mocked `Resources`.
 *
 * A mock could only assert that `getQuantityString` was called with the arguments this test passed
 * it — which is a test of the test. Robolectric costs a second and asserts the thing that actually
 * goes wrong: that the quantity picks the form and the argument fills the placeholder, and that
 * they are not the same number by accident.
 */
@RunWith(RobolectricTestRunner::class)
class UiTextTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `a plural picks its form from the quantity`() {
        val one = R.plurals.core_field_min_length.toPluralUiText(quantity = 1, 1)
        val many = R.plurals.core_field_min_length.toPluralUiText(quantity = 2, 2)

        assertEquals("Must be at least 1 character.", one.resolve(context))
        assertEquals("Must be at least 2 characters.", many.resolve(context))
    }

    @Test
    fun `the quantity and the argument are separate`() {
        // The first number chooses the form, the second fills the %d. Passing one number for both
        // is the mistake toPluralUiText is named differently to prevent.
        val text = R.plurals.core_field_min_length.toPluralUiText(quantity = 2, 8)

        assertEquals("Must be at least 8 characters.", text.resolve(context))
    }

    @Test
    fun `a string resource resolves without a quantity`() {
        assertEquals("Loading…", R.string.core_loading.toUiText().resolve(context))
    }

    @Test
    fun `a literal ignores the resource table entirely`() {
        assertEquals("hello", "hello".toUiText().resolve(context))
    }
}
