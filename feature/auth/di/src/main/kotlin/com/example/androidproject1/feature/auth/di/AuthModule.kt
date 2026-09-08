package com.example.androidproject1.feature.auth.di

import com.example.androidproject1.feature.auth.data.repository.DefaultAuthRepository
import com.example.androidproject1.feature.auth.data.source.DefaultLocalAuthDataSource
import com.example.androidproject1.feature.auth.data.source.LocalAuthDataSource
import com.example.androidproject1.feature.auth.domain.AuthRepository
import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.auth.domain.DefaultAuthService
import com.example.androidproject1.feature.auth.presentation.LoginViewModel
import com.example.androidproject1.feature.auth.presentation.SignUpViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object AuthModule {

    val module: Module = module {
        viewModelOf(::LoginViewModel)
        viewModelOf(::SignUpViewModel)

        singleOf(::DefaultAuthService) bind AuthService::class
        singleOf(::DefaultAuthRepository) bind AuthRepository::class
        singleOf(::DefaultLocalAuthDataSource) bind LocalAuthDataSource::class
    }
}
