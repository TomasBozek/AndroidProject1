package com.example.androidproject1

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The parser takes a string rather than an `android.net.Uri`, so this is a plain JVM test with
 * nothing to stub. The scheme itself is `MainActivity`'s to check.
 */
class DeepLinksTest {

    @Test
    fun `a product link names the product`() {
        assertEquals(
            DeepLink.Product(productId = "croissant"),
            DeepLinks.parse("androidproject1://product/croissant"),
        )
    }

    @Test
    fun `a trailing slash is not a second path segment`() {
        assertEquals(
            DeepLink.Product(productId = "croissant"),
            DeepLinks.parse("androidproject1://product/croissant/"),
        )
    }

    @Test
    fun `a host the app does not serve is not a link`() {
        assertNull(DeepLinks.parse("androidproject1://order/1"))
    }

    @Test
    fun `a product link with no product is not a link`() {
        assertNull(DeepLinks.parse("androidproject1://product"))
        assertNull(DeepLinks.parse("androidproject1://product/"))
    }

    @Test
    fun `a deeper path is not a link, rather than one with the first segment taken`() {
        assertNull(DeepLinks.parse("androidproject1://product/croissant/reviews"))
    }

    @Test
    fun `nothing at all is not a link`() {
        assertNull(DeepLinks.parse(null))
        assertNull(DeepLinks.parse(""))
    }
}
