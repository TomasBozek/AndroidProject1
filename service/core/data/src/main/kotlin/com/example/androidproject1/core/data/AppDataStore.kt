package com.example.androidproject1.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

/**
 * Single owner of the app's [DataStore] instance.
 *
 * DataStore permits exactly one active instance per file per process, so **this class must be
 * registered as a singleton** (it is, in `coreModule`) and must be the only thing that creates the
 * store. It is built through [PreferenceDataStoreFactory] rather than the `preferencesDataStore`
 * property delegate because the delegate fixes the file name at compile time, which a module meant
 * to be reused across projects should not do.
 *
 * @param name the preferences file name. Override it when one app needs more than one store, or to
 * avoid clashing with an existing file after adopting this module.
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
