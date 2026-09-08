package com.example.androidproject1.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

/**
 * Single owner of the app's [DataStore]. DataStore allows one active instance per file per process,
 * so this **must** be a singleton (it is, in `coreModule`) and the only thing that creates a store.
 * The factory is used rather than the `preferencesDataStore` delegate because the delegate fixes
 * the file name at compile time, which a reusable module should not do.
 *
 * @param name the preferences file name; override it for a second store or to avoid a clash.
 */
class DataStoreProvider(
    private val context: Context,
    private val name: String = DEFAULT_NAME,
) {

    val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(name) }
    }

    companion object {

        const val DEFAULT_NAME = "app_preferences"
    }
}
