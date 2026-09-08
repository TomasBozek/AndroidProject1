package com.example.androidproject1.feature.launch.di

import com.example.androidproject1.feature.launch.presentation.LaunchViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object LaunchModule {

    val module: Module = module {
        viewModelOf(::LaunchViewModel)
    }
}
