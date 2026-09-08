package com.example.androidproject1.feature.gallery.presentation

import app.cash.turbine.test
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class GalleryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The route key the destination hands in. A plain object on Navigation 3 — no Bundle, and so
    // no Robolectric.
    private fun viewModel(componentId: String = "button") = GalleryDetailViewModel(
        logger = FakeLogger(),
        args = GalleryDetailDestination(componentId = componentId),
    )

    @Test
    fun `reads its component from the route`() = runTest {
        val state = viewModel().state.value

        assertEquals("button", state.data?.componentId)
        assertEquals("AppButton", state.data?.name)
        assertEquals("Action", state.data?.group)
    }

    @Test
    fun `an id that is not in the catalogue renders nothing`() = runTest {
        // The catalogue is the source of truth. A route key naming something that is not in it
        // leaves the screen with no state rather than an empty shell claiming the component exists.
        val state = viewModel(componentId = "not-a-component").state.value

        assertNull(state.data)
    }

    @Test
    fun `up navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.navigation.test {
            viewModel.onUiEvent(GalleryDetailEvent.NavigateUpClicked)

            assertEquals(GalleryDetailNavigation.Up, awaitItem())
        }
    }
}
