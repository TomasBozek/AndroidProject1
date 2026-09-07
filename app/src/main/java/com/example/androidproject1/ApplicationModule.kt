package com.example.androidproject1

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Bindings that only the application module can provide — platform implementations of service
 * interfaces, and the app-level ViewModel.
 */
object ApplicationModule {

    val module: Module = module {
        viewModelOf(::MainViewModel)
    }
}
