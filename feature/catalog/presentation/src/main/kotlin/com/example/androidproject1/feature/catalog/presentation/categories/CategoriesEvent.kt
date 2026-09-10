package com.example.androidproject1.feature.catalog.presentation.categories

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface CategoriesEvent : UiEvent {

    data class CategoryClicked(val category: Category) : CategoriesEvent

    data object SearchClicked : CategoriesEvent
}
