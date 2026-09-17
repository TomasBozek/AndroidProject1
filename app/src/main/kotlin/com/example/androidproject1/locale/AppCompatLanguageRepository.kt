package com.example.androidproject1.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.androidproject1.feature.settings.domain.AppLanguage
import com.example.androidproject1.feature.settings.domain.LanguageRepository
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * [LanguageRepository] over the platform's per-app language setting, through AppCompat (D75).
 *
 * AppCompat is the store: on API 33+ it delegates to the system's `LocaleManager`, below that it
 * persists the choice itself (`autoStoreLocales` in the manifest) and applies it in
 * `AppCompatActivity.attachBaseContext`, before the first frame. Nothing here is written to
 * DataStore, and the flow is a `StateFlow` seeded from what AppCompat holds and re-read after
 * every set — the platform has no callback for it, and the only writer is this class.
 *
 * In `:app` rather than `:feature:settings:data` because `AppCompatDelegate` is an activity-stack
 * class, and the one module that already depends on that stack is this one.
 */
class AppCompatLanguageRepository(
    private val dispatcherProvider: DispatcherProvider,
) : LanguageRepository {

    private val language = MutableStateFlow(read())

    override fun observeLanguage(): Flow<Outcome<AppLanguage>> = language.map { Outcome.Success(it) }

    // The main thread is where AppCompat wants this called: below API 33 it recreates the
    // activities that are up, and above it the framework does the same on its own terms.
    override suspend fun setLanguage(language: AppLanguage): Outcome<Unit> = withContext(dispatcherProvider.main) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        this@AppCompatLanguageRepository.language.value = read()
        Outcome.Success(Unit)
    }

    /** The first stored locale's language, or `""` for "follow the device" — an empty list. */
    private fun read(): AppLanguage = AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales()[0]?.language ?: "")
}
