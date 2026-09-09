package com.example.androidproject1.feature.catalog.presentation.categories

import com.example.androidproject1.core.ui.event.UiEvent
import com.example.androidproject1.feature.catalog.domain.Category

sealed interface CategoriesEvent : UiEvent {

    data class CategoryClicked(val category: Category) : CategoriesEvent
}
