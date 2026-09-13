package com.example.androidproject1.feature.inventory.di

import androidx.room.Room
import com.example.androidproject1.feature.inventory.data.database.InventoryDatabase
import com.example.androidproject1.feature.inventory.data.repository.DefaultInventoryRepository
import com.example.androidproject1.feature.inventory.data.source.DefaultLocalInventoryDataSource
import com.example.androidproject1.feature.inventory.data.source.LocalInventoryDataSource
import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.presentation.inventory.InventoryViewModel
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailViewModel
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

object InventoryModule {

    val module: Module = module {
        viewModelOf(::InventoryViewModel)
        viewModelOf(::InventoryEditorViewModel)
        viewModelOf(::InventoryDetailViewModel)

        single {
            Room.databaseBuilder(
                androidContext(),
                InventoryDatabase::class.java,
                InventoryDatabase.NAME,
            ).addMigrations(*InventoryDatabase.MIGRATIONS)
                .build()
        }
        single { get<InventoryDatabase>().itemDao() }

        singleOf(::DefaultInventoryRepository) bind InventoryRepository::class
        single<LocalInventoryDataSource> { DefaultLocalInventoryDataSource(itemDao = get()) }
    }
}
