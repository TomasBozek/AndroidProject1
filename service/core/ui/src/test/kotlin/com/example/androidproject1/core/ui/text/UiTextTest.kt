package com.example.androidproject1.core.ui.text

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class UiTextTest {

    private val resources = mockk<Resources>()
    private val context = mockk<Context> { every { resources } returns this@UiTextTest.resources }

    @Test
    fun `a plural resolves through getQuantityString`() {
        every { resources.getQuantityString(ITEMS, 2, *anyVararg()) } returns "2 items"

        val text = ITEMS.toPluralUiText(quantity = 2, 2)

        assertEquals("2 items", text.resolve(context))
        // The quantity picks the form; showing it is what the argument is for.
        verify { resources.getQuantityString(ITEMS, 2, 2) }
    }

    @Test
    fun `a plural with no arguments still passes its quantity`() {
        // Always the vararg overload, with an empty array — the two-argument one is never called.
        every { resources.getQuantityString(ITEMS, 1, *anyVararg()) } returns "one item"

        assertEquals("one item", ITEMS.toPluralUiText(quantity = 1).resolve(context))
    }

    @Test
    fun `a literal ignores the context entirely`() {
        assertEquals("hello", "hello".toUiText().resolve(mockk()))
    }

    private companion object {

        /** Any id will do: nothing here reads a real resource table. */
        const val ITEMS = 42
    }
}
