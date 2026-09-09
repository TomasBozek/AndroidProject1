package com.example.androidproject1.feature.settings.domain.test

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.settings.domain.ThemePreference
import com.example.androidproject1.feature.settings.domain.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ThemeRepository] for tests.
 *
 * A fixture of `:feature:settings:domain` rather than of one test source set, because the
 * preference is read in two places: the Settings screen and `MainViewModel`, which draws the whole
 * app in it.
 *
 * @property theme settable, so a test can start from a stored choice without writing one.
 * @property failWith set to make [setTheme] and the flow fail.
 */
class FakeThemeRepository(var failWith: DomainError? = null) : ThemeRepository {

    val theme = MutableStateFlow(ThemePreference.DEFAULT)

    override fun observeTheme(): Flow<Outcome<ThemePreference>> =
        theme.map { stored -> failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(stored) }

    override suspend fun setTheme(theme: ThemePreference): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        this.theme.value = theme
        return Outcome.Success(Unit)
    }
}
