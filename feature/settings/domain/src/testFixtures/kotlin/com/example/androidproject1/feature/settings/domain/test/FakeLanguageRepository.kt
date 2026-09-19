package com.example.androidproject1.feature.settings.domain.test

import com.example.androidproject1.feature.settings.domain.AppLanguage
import com.example.androidproject1.feature.settings.domain.LanguageRepository
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [LanguageRepository] for tests, the same shape as [FakeThemeRepository].
 *
 * @property language settable, so a test can start from a stored choice without writing one.
 * @property failWith set to make [setLanguage] and the flow fail.
 */
class FakeLanguageRepository(var failWith: DomainError? = null) : LanguageRepository {

    val language = MutableStateFlow(AppLanguage.DEFAULT)

    override fun observeLanguage(): Flow<Outcome<AppLanguage>> =
        language.map { stored -> failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(stored) }

    override suspend fun setLanguage(language: AppLanguage): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        this.language.value = language
        return Outcome.Success(Unit)
    }
}
