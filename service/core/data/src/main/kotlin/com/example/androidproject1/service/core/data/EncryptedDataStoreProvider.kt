package com.example.androidproject1.service.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.example.androidproject1.service.core.data.crypto.EncryptedStringSerializer
import com.example.androidproject1.service.core.domain.crypto.Aead
import java.io.File

/**
 * A store for one value that should not be readable from a copy of the app's data directory.
 *
 * Separate from [DataStoreProvider] on purpose. Encrypting everything would put an AES operation
 * in front of every preference read, and most preferences are a theme choice or a sort order —
 * worth nothing to an attacker and worth a cold start to the user. This is for the few values
 * where the opposite is true.
 *
 * @param name the file; one provider per value, since each store owns its own file. Defaulted so
 *   the common case — one encrypted store — needs no argument and Koin can build it from the
 *   graph rather than from a lambda that `verify` cannot see into.
 */
class EncryptedDataStoreProvider(
    private val context: Context,
    private val aead: Aead,
    private val name: String = DEFAULT_NAME,
) {

    val dataStore: DataStore<String> by lazy {
        DataStoreFactory.create(
            serializer = EncryptedStringSerializer(aead),
            // A value that cannot be decrypted is gone, not fatal. See the serializer.
            corruptionHandler = ReplaceFileCorruptionHandler { "" },
            produceFile = { File(context.filesDir, "datastore/$name") },
        )
    }

    companion object {

        const val DEFAULT_NAME = "secure.bin"
    }
}
