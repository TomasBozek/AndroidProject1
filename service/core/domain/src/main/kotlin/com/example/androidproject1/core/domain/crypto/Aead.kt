package com.example.androidproject1.core.domain.crypto

/**
 * Authenticated encryption: the ciphertext cannot be read *or altered* without detection.
 *
 * Authentication is the half that is easy to leave out and expensive to leave out. Encryption
 * alone stops someone reading the session; it does not stop them flipping bits in it. [decrypt]
 * throws rather than returning something plausible.
 *
 * Free of `android.*` so a data source can be tested against a fake. The real implementation is
 * `KeystoreAead` in `:service:core:data`, which keeps the key in hardware where the OS will not
 * hand it back.
 */
interface Aead {

    fun encrypt(plaintext: ByteArray): ByteArray

    /** @throws GeneralSecurityException-shaped failure if the data was altered or the key is gone. */
    fun decrypt(ciphertext: ByteArray): ByteArray

    companion object {

        /** Stores plaintext. For tests, and for a store whose contents are not worth protecting. */
        val None: Aead = object : Aead {
            override fun encrypt(plaintext: ByteArray) = plaintext

            override fun decrypt(ciphertext: ByteArray) = ciphertext
        }
    }
}
