package com.example.androidproject1.feature.gallery.presentation.gallery

import app.cash.turbine.test
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.gallery.presentation.galleryItems
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GalleryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = GalleryViewModel(logger = FakeLogger())

    @Test
    fun `renders the catalogue immediately`() = runTest {
        val state = viewModel().state.value

        assertEquals(galleryItems(), state.data?.items)
        // The catalogue is a compile-time constant, so nothing is loading.
        assertNull(state.loading)
    }

    @Test
    fun `groups the catalogue for the list`() = runTest {
        val groups = viewModel().state.value.data!!.groups

        assertTrue(groups.isNotEmpty())
        assertEquals(galleryItems().size, groups.sumOf { it.second.size })
    }

    @Test
    fun `clicking a component navigates to it`() = runTest {
        val viewModel = viewModel()

        viewModel.navigation.test {
            viewModel.onUiEvent(GalleryEvent.ComponentClicked("button"))

            assertEquals(GalleryNavigation.ToComponent("button"), awaitItem())
        }
    }
}
