package com.example.androidproject1.feature.movies.di

import androidx.room.Room
import com.example.androidproject1.feature.movies.data.database.MoviesDatabase
import com.example.androidproject1.feature.movies.data.repository.DefaultMoviesRepository
import com.example.androidproject1.feature.movies.data.source.DefaultLocalMoviesDataSource
import com.example.androidproject1.feature.movies.data.source.DefaultRemoteMoviesDataSource
import com.example.androidproject1.feature.movies.data.source.LocalMoviesDataSource
import com.example.androidproject1.feature.movies.data.source.RemoteMoviesDataSource
import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.feature.movies.presentation.movies.MoviesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object MoviesModule {

    val module: Module = module {
        viewModelOf(::MoviesViewModel)

        single {
            Room.databaseBuilder(
                androidContext(),
                MoviesDatabase::class.java,
                MoviesDatabase.NAME,
            ).addMigrations(*MoviesDatabase.MIGRATIONS)
                .build()
        }
        single { get<MoviesDatabase>().moviesDao() }

        singleOf(::DefaultMoviesRepository) bind MoviesRepository::class
        // `TmdbConfig` is bound by :app, which owns BuildConfig (D80).
        singleOf(::DefaultRemoteMoviesDataSource) bind RemoteMoviesDataSource::class
        // Spelled out, like the catalog's: the data source has a defaulted clock for the page
        // markers, and reflection would try to resolve the `() -> Long` from the graph.
        single<LocalMoviesDataSource> { DefaultLocalMoviesDataSource(moviesDao = get()) }
    }
}
