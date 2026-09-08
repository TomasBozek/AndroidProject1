package com.example.androidproject1.core.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.androidproject1.core.domain.crypto.Aead
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * [AesGcmAead] with the key held in the Android Keystore.
 *
 * The key never leaves the Keystore — on a device with a secure element it never exists in the
 * app's memory at all — so a copy of the app's data directory is not enough to read anything. That
 * is the property `security-crypto` used to provide, and it is deprecated, which is why this is
 * about thirty lines rather than a dependency.
 *
 * Everything except the key lookup is in [AesGcmAead] and tested there. This part cannot be:
 * Robolectric ships no `AndroidKeyStore` provider, so it is exercised on a device.
 */
fun KeystoreAead(alias: String): Aead = AesGcmAead { keystoreKey(alias) }

/** Created on first use and reused after. Generating it eagerly would slow every cold start. */
private fun keystoreKey(alias: String): SecretKey {
    val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
    (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

    return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER).apply {
        init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AesGcmAead.KEY_BITS)
                // Deliberately not `setUserAuthenticationRequired`: this protects a session token
                // at rest, and a lock-screen prompt to read it would fire on every cold start. A
                // value that needs the user present is a different key.
                .build(),
        )
    }.generateKey()
}

private const val PROVIDER = "AndroidKeyStore"
