package com.example.androidproject1.core.data.crypto

import androidx.datastore.core.CorruptionException
import com.example.androidproject1.core.domain.crypto.Aead
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** A stand-in that is obviously not encryption, so a test failure is about the serializer. */
private object ReversingAead : Aead {
    override fun encrypt(plaintext: ByteArray) = plaintext.reversedArray()

    override fun decrypt(ciphertext: ByteArray) = ciphertext.reversedArray()
}

private object FailingAead : Aead {
    override fun encrypt(plaintext: ByteArray) = plaintext

    override fun decrypt(ciphertext: ByteArray): ByteArray = error("key is gone")
}

class EncryptedStringSerializerTest {

    private val serializer = EncryptedStringSerializer(ReversingAead)

    private suspend fun roundTrip(value: String): String {
        val out = ByteArrayOutputStream()
        serializer.writeTo(value, out)
        return serializer.readFrom(ByteArrayInputStream(out.toByteArray()))
    }

    @Test
    fun `a value survives a round trip`() = runTest {
        assertEquals("ada@example.com", roundTrip("ada@example.com"))
    }

    @Test
    fun `the empty string survives a round trip`() = runTest {
        // Signed out is a real value, not an absent one.
        assertEquals("", roundTrip(""))
    }

    @Test
    fun `what is written is not the plaintext`() = runTest {
        val out = ByteArrayOutputStream()
        serializer.writeTo("ada@example.com", out)

        assertEquals("moc.elpmaxe@ada", out.toByteArray().decodeToString())
    }

    @Test
    fun `an empty file reads as signed out`() = runTest {
        assertEquals("", serializer.readFrom(ByteArrayInputStream(ByteArray(0))))
    }

    @Test
    fun `a value that cannot be decrypted is corruption, not a crash`() = runTest {
        // The Keystore key is gone — a cleared lock screen, a restore to a new device, or
        // tampering. DataStore's corruption handler resets the file; the user is signed out.
        val failing = EncryptedStringSerializer(FailingAead)

        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking {
                failing.readFrom(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
            }
        }
    }
}
