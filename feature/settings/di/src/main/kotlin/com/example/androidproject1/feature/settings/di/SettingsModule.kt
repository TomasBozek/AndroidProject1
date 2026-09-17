package com.example.androidproject1.feature.settings.di

import com.example.androidproject1.feature.settings.data.repository.DefaultThemeRepository
import com.example.androidproject1.feature.settings.data.source.DefaultLocalThemeDataSource
import com.example.androidproject1.feature.settings.data.source.LocalThemeDataSource
import com.example.androidproject1.feature.settings.domain.ThemeRepository
import com.example.androidproject1.feature.settings.presentation.language.SettingsLanguageViewModel
import com.example.androidproject1.feature.settings.presentation.permissions.SettingsPermissionsViewModel
import com.example.androidproject1.feature.settings.presentation.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object SettingsModule {

    val module: Module = module {
        viewModelOf(::SettingsViewModel)
        viewModelOf(::SettingsPermissionsViewModel)
        viewModelOf(::SettingsLanguageViewModel)

        singleOf(::DefaultThemeRepository) bind ThemeRepository::class
        singleOf(::DefaultLocalThemeDataSource) bind LocalThemeDataSource::class
    }
}
