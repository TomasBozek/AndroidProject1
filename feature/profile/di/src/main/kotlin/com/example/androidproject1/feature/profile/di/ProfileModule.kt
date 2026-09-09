package com.example.androidproject1.feature.profile.di

import com.example.androidproject1.feature.profile.data.repository.DefaultProfileRepository
import com.example.androidproject1.feature.profile.data.source.AvatarDataSource
import com.example.androidproject1.feature.profile.data.source.DefaultAvatarDataSource
import com.example.androidproject1.feature.profile.data.source.DefaultLocalProfileDataSource
import com.example.androidproject1.feature.profile.data.source.LocalProfileDataSource
import com.example.androidproject1.feature.profile.domain.ProfileRepository
import com.example.androidproject1.feature.profile.presentation.profile.ProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object ProfileModule {

    val module: Module = module {
        viewModelOf(::ProfileViewModel)

        singleOf(::DefaultProfileRepository) bind ProfileRepository::class
        singleOf(::DefaultLocalProfileDataSource) bind LocalProfileDataSource::class
        singleOf(::DefaultAvatarDataSource) bind AvatarDataSource::class
    }
}
