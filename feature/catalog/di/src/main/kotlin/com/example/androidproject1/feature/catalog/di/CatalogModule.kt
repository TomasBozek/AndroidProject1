package com.example.androidproject1.feature.catalog.di

import com.example.androidproject1.feature.catalog.data.DefaultLocalCatalogDataSource
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.gateway.DefaultCatalogRepository
import com.example.androidproject1.feature.catalog.gateway.LocalCatalogDataSource
import com.example.androidproject1.feature.catalog.presentation.CategoriesViewModel
import com.example.androidproject1.feature.catalog.presentation.ProductDetailViewModel
import com.example.androidproject1.feature.catalog.presentation.ProductsViewModel
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

        singleOf(::DefaultCatalogRepository) bind CatalogRepository::class
        singleOf(::DefaultLocalCatalogDataSource) bind LocalCatalogDataSource::class
    }
}
