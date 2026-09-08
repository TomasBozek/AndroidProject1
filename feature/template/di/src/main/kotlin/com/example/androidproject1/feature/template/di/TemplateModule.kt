package com.example.androidproject1.feature.template.di

import com.example.androidproject1.feature.template.presentation.TemplateArgsViewModel
import com.example.androidproject1.feature.template.presentation.TemplateViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object TemplateModule {

    val module: Module = module {
        viewModelOf(::TemplateViewModel)
        viewModelOf(::TemplateArgsViewModel)
    }
}
