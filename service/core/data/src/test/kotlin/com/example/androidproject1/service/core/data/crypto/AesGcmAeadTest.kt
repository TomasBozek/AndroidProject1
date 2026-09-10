package com.example.androidproject1.service.core.data.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.KeyGenerator

/**
 * The encryption scheme, on a plain JVM key.
 *
 * `KeystoreAead` is this class with the key coming from the Android Keystore, which has no
 * implementation under Robolectric — so the split is what makes everything worth testing testable.
 */
class AesGcmAeadTest {

    private val key = KeyGenerator.getInstance("AES")
        .apply { init(AesGcmAead.KEY_BITS) }
        .generateKey()
    private val aead = AesGcmAead { key }

    @Test
    fun `a value survives a round trip`() {
        val plaintext = "ada@example.com".encodeToByteArray()

        assertArrayEquals(plaintext, aead.decrypt(aead.encrypt(plaintext)))
    }

    @Test
    fun `the ciphertext does not contain the plaintext`() {
        val ciphertext = aead.encrypt("ada@example.com".encodeToByteArray())

        assertFalse(ciphertext.decodeToString().contains("ada@example.com"))
    }

    @Test
    fun `the same value encrypts differently every time`() {
        // A fresh IV per encryption. GCM with a reused IV leaks the key, so this is the property
        // the whole scheme rests on rather than a nicety.
        val plaintext = "ada@example.com".encodeToByteArray()

        assertNotEquals(aead.encrypt(plaintext).toList(), aead.encrypt(plaintext).toList())
    }

    @Test
    fun `altered ciphertext is rejected rather than decrypted`() {
        val ciphertext = aead.encrypt("ada@example.com".encodeToByteArray())
        ciphertext[ciphertext.lastIndex] = (ciphertext.last() + 1).toByte()

        // What the "authenticated" in AEAD buys: a flipped bit fails loudly instead of producing a
        // different, plausible session.
        assertThrows(Exception::class.java) { aead.decrypt(ciphertext) }
    }

    @Test
    fun `a different key cannot read it`() {
        val other = AesGcmAead {
            KeyGenerator.getInstance("AES").apply { init(AesGcmAead.KEY_BITS) }.generateKey()
        }

        assertThrows(Exception::class.java) {
            other.decrypt(aead.encrypt("ada@example.com".encodeToByteArray()))
        }
    }

    @Test
    fun `something too short to hold an IV is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { aead.decrypt(byteArrayOf(1, 2, 3)) }
    }
}
