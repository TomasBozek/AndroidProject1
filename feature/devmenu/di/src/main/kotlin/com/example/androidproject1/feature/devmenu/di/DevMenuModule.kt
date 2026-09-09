package com.example.androidproject1.feature.devmenu.di

import com.example.androidproject1.feature.devmenu.presentation.devmenu.DevMenuViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * `BuildInfo` and `OfflineSwitch` are bound by `:app` — only the application module can read
 * `BuildConfig`, and only its `dev` source set has a fixture engine to switch off.
 */
object DevMenuModule {

    val module: Module = module {
        viewModelOf(::DevMenuViewModel)
    }
}
