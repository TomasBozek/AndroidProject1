package com.example.androidproject1.feature.cart.di

import androidx.room.Room
import com.example.androidproject1.feature.cart.data.database.CartDatabase
import com.example.androidproject1.feature.cart.data.repository.DefaultCartRepository
import com.example.androidproject1.feature.cart.data.source.DefaultLocalCartDataSource
import com.example.androidproject1.feature.cart.data.source.LocalCartDataSource
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.feature.cart.presentation.cart.CartViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object CartModule {

    val module: Module = module {
        viewModelOf(::CartViewModel)

        // Its own database, not the catalog\'s: feat.5 replaces the catalog table on every
        // refresh, and the cart must survive the shop reorganising itself.
        single {
            Room.databaseBuilder(androidContext(), CartDatabase::class.java, CartDatabase.NAME)
                .build()
        }
        single { get<CartDatabase>().cartDao() }

        singleOf(::DefaultCartRepository) bind CartRepository::class
        // Spelled out: the data source has a defaulted clock for tests, which reflection would
        // try to resolve from the graph.
        single<LocalCartDataSource> { DefaultLocalCartDataSource(cartDao = get()) }
    }
}
