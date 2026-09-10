package com.example.androidproject1.service.core.data.crypto

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.androidproject1.service.core.domain.crypto.Aead
import java.io.InputStream
import java.io.OutputStream

/**
 * A DataStore of one encrypted string.
 *
 * The empty string is the default, which is what an absent file and a signed-out session both look
 * like — so there is no separate "is there a file" case for a caller to forget.
 *
 * A file that cannot be decrypted is treated as corrupt and reset rather than crashing the app.
 * That is the right call for a session: it happens when the Keystore key is gone — the user
 * cleared the lock screen, restored the app to a new device, or the data was tampered with — and
 * in every one of those cases the honest answer is that there is no session any more.
 */
class EncryptedStringSerializer(
    private val aead: Aead,
    private val onCorruption: (Throwable) -> Unit = {},
) : Serializer<String> {

    override val defaultValue: String = ""

    override suspend fun readFrom(input: InputStream): String {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            aead.decrypt(bytes).decodeToString()
        } catch (e: Exception) {
            onCorruption(e)
            throw CorruptionException("Could not decrypt; the stored value is being reset", e)
        }
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.write(aead.encrypt(t.encodeToByteArray()))
    }
}
