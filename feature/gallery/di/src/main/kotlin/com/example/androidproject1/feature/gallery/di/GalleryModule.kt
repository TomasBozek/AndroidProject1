package com.example.androidproject1.feature.gallery.di

import com.example.androidproject1.feature.gallery.presentation.gallery.GalleryViewModel
import com.example.androidproject1.feature.gallery.presentation.gallerydetail.GalleryDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object GalleryModule {

    val module: Module = module {
        viewModelOf(::GalleryViewModel)
        viewModelOf(::GalleryDetailViewModel)
    }
}
