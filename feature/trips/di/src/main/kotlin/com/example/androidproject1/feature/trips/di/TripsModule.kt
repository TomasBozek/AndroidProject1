package com.example.androidproject1.feature.trips.di

import androidx.room.Room
import com.example.androidproject1.feature.trips.data.database.TripsDatabase
import com.example.androidproject1.feature.trips.data.repository.DefaultDestinationsRepository
import com.example.androidproject1.feature.trips.data.repository.DefaultTripsRepository
import com.example.androidproject1.feature.trips.data.source.DefaultLocalDestinationsDataSource
import com.example.androidproject1.feature.trips.data.source.DefaultLocalTripsDataSource
import com.example.androidproject1.feature.trips.data.source.LocalDestinationsDataSource
import com.example.androidproject1.feature.trips.data.source.LocalTripsDataSource
import com.example.androidproject1.feature.trips.domain.DestinationsRepository
import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.feature.trips.presentation.destinationpicker.DestinationPickerViewModel
import com.example.androidproject1.feature.trips.presentation.tripdetail.TripDetailViewModel
import com.example.androidproject1.feature.trips.presentation.trips.TripsViewModel
import com.example.androidproject1.feature.trips.presentation.tripslist.TripsListViewModel
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object TripsModule {

    val module: Module = module {
        viewModelOf(::TripsViewModel)
        viewModelOf(::TripsListViewModel)
        viewModelOf(::TripWizardViewModel)
        viewModelOf(::TripDetailViewModel)
        viewModelOf(::DestinationPickerViewModel)

        single {
            Room.databaseBuilder(
                androidContext(),
                TripsDatabase::class.java,
                TripsDatabase.NAME,
            ).addMigrations(*TripsDatabase.MIGRATIONS)
                .build()
        }
        single { get<TripsDatabase>().tripDao() }
        single { get<TripsDatabase>().destinationDao() }

        singleOf(::DefaultTripsRepository) bind TripsRepository::class
        singleOf(::DefaultDestinationsRepository) bind DestinationsRepository::class
        single<LocalTripsDataSource> { DefaultLocalTripsDataSource(tripDao = get()) }
        single<LocalDestinationsDataSource> { DefaultLocalDestinationsDataSource(destinationDao = get()) }
    }
}
