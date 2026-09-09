package com.example.androidproject1.feature.home.di

import com.example.androidproject1.feature.home.presentation.home.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object HomeModule {

    val module: Module = module {
        viewModelOf(::HomeViewModel)
    }
}
