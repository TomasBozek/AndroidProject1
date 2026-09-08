package com.example.androidproject1.feature.settings.di

import com.example.androidproject1.feature.settings.presentation.SettingsPermissionsViewModel
import com.example.androidproject1.feature.settings.presentation.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object SettingsModule {

    val module: Module = module {
        viewModelOf(::SettingsViewModel)
        viewModelOf(::SettingsPermissionsViewModel)
    }
}
