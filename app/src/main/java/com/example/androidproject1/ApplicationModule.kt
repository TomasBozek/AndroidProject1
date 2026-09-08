package com.example.androidproject1

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Bindings only the application module can provide. */
object ApplicationModule {

    val module: Module = module {
        viewModelOf(::MainViewModel)
    }
}
