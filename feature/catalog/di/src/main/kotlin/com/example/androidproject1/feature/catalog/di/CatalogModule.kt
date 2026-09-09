package com.example.androidproject1.feature.catalog.di

import androidx.room.Room
import com.example.androidproject1.feature.catalog.data.database.CatalogDatabase
import com.example.androidproject1.feature.catalog.data.repository.DefaultCatalogRepository
import com.example.androidproject1.feature.catalog.data.repository.DefaultFavouritesRepository
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalFavouritesDataSource
import com.example.androidproject1.feature.catalog.data.source.DefaultRemoteCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.LocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.LocalFavouritesDataSource
import com.example.androidproject1.feature.catalog.data.source.RemoteCatalogDataSource
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.catalog.presentation.categories.CategoriesViewModel
import com.example.androidproject1.feature.catalog.presentation.productdetail.ProductDetailViewModel
import com.example.androidproject1.feature.catalog.presentation.productpicker.ProductPickerViewModel
import com.example.androidproject1.feature.catalog.presentation.products.ProductsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object CatalogModule {

    val module: Module = module {
        viewModelOf(::CategoriesViewModel)
        viewModelOf(::ProductsViewModel)
        viewModelOf(::ProductDetailViewModel)
        viewModelOf(::ProductPickerViewModel)

        single {
            Room.databaseBuilder(
                androidContext(),
                CatalogDatabase::class.java,
                CatalogDatabase.NAME,
            ).build()
        }
        single { get<CatalogDatabase>().catalogDao() }
        single { get<CatalogDatabase>().favouritesDao() }

        singleOf(::DefaultCatalogRepository) bind CatalogRepository::class
        singleOf(::DefaultFavouritesRepository) bind FavouritesRepository::class
        singleOf(::DefaultLocalCatalogDataSource) bind LocalCatalogDataSource::class
        singleOf(::DefaultRemoteCatalogDataSource) bind RemoteCatalogDataSource::class
        single<LocalFavouritesDataSource> { DefaultLocalFavouritesDataSource(favouritesDao = get()) }
    }
}
