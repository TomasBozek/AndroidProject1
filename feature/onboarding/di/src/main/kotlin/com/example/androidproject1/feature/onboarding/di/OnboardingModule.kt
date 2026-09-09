package com.example.androidproject1.feature.onboarding.di

import com.example.androidproject1.feature.onboarding.data.repository.DefaultOnboardingRepository
import com.example.androidproject1.feature.onboarding.data.source.DefaultLocalOnboardingDataSource
import com.example.androidproject1.feature.onboarding.data.source.LocalOnboardingDataSource
import com.example.androidproject1.feature.onboarding.domain.OnboardingRepository
import com.example.androidproject1.feature.onboarding.presentation.onboarding.OnboardingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object OnboardingModule {

    val module: Module = module {
        viewModelOf(::OnboardingViewModel)

        singleOf(::DefaultOnboardingRepository) bind OnboardingRepository::class
        singleOf(::DefaultLocalOnboardingDataSource) bind LocalOnboardingDataSource::class
    }
}
