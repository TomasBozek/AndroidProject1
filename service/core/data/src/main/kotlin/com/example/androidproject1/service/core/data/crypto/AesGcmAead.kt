package com.example.androidproject1.service.core.data.crypto

import com.example.androidproject1.service.core.domain.crypto.Aead
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM. Where the key comes from is [key]'s problem.
 *
 * Split from [KeystoreAead] so the scheme is testable: the Android Keystore has no implementation
 * under Robolectric, and everything worth getting wrong here — IV handling, tamper detection,
 * length checks — is in this class rather than in the fifteen lines that fetch the key.
 *
 * The IV is random per encryption and prepended to the ciphertext. GCM with a reused IV leaks the
 * key, so it is never derived or stored: [encrypt] lets the provider generate one and reads it back
 * out.
 */
class AesGcmAead(private val key: () -> SecretKey) : Aead {

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(plaintext)
    }

    override fun decrypt(ciphertext: ByteArray): ByteArray {
        require(ciphertext.size > IV_BYTES) { "Ciphertext is too short to contain an IV" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(),
            GCMParameterSpec(TAG_BITS, ciphertext, 0, IV_BYTES),
        )
        return cipher.doFinal(ciphertext, IV_BYTES, ciphertext.size - IV_BYTES)
    }

    internal companion object {

        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val KEY_BITS = 256
    }
}
