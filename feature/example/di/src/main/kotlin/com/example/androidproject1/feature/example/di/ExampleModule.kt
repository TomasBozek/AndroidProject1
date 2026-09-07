package com.example.androidproject1.feature.example.di

import com.example.androidproject1.feature.example.presentation.ExampleViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object ExampleModule {

    val module: Module = module {
        viewModelOf(::ExampleViewModel)
    }
}
