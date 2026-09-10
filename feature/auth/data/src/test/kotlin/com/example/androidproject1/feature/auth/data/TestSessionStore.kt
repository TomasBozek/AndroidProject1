package com.example.androidproject1.feature.auth.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.auth.data.source.DefaultLocalAuthDataSource
import com.example.androidproject1.service.core.data.EncryptedDataStoreProvider
import com.example.androidproject1.service.core.data.crypto.AesGcmAead
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.domain.test.TestDispatchers
import java.io.File
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** AES-256, as `KeystoreAead` asks the Keystore for. Its own constant is module-internal. */
private const val KEY_BITS = 256

/** DataStore permits one live instance per file per process, so no two stores share a name. */
private var storeCount = 0

internal fun newAesKey(): SecretKey =
    KeyGenerator.getInstance("AES").apply { init(KEY_BITS) }.generateKey()

/**
 * The production encrypted store, on a plain JVM key.
 *
 * `KeystoreAead` cannot run under Robolectric — there is no `AndroidKeyStore` provider — but where
 * the key comes from is the one thing the data source does not depend on. Everything else here is
 * the real thing: the real serializer, the real AES-GCM, a real file. Faking the store instead
 * would leave the part worth testing — that a session survives being written to disk and read back
 * by a *different* instance — asserted by nothing.
 *
 * @param key defaulted, so a test that wants to read a file written by another installation's key
 * simply passes a second one.
 */
internal class TestSessionStore(key: SecretKey = newAesKey()) {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val name = "session-${storeCount++}.bin"

    /** Where the bytes actually land, so a test can look at them. */
    val file: File = File(context.filesDir, "datastore/$name")

    val logger = FakeLogger()

    val provider = EncryptedDataStoreProvider(
        context = context,
        aead = AesGcmAead { key },
        name = name,
    )

    fun dataSource() = DefaultLocalAuthDataSource(
        sessionStoreProvider = provider,
        dispatcherProvider = TestDispatchers,
        logger = logger,
    )
}
