package com.example.androidproject1.feature.home.presentation

import app.cash.turbine.test
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = HomeViewModel(logger = FakeLogger())

    @Test
    fun `renders immediately, with no loading overlay`() = runTest {
        val state = viewModel().state.value

        assertEquals(HomeState.PREVIEW, state.data)
        assertNull(state.loading)
    }

    @Test
    fun `each button emits its navigation intent`() = runTest {
        val viewModel = viewModel()

        viewModel.navigation.test {
            viewModel.onUiEvent(HomeEvent.SettingsClicked)
            assertEquals(HomeNavigation.Settings, awaitItem())

            viewModel.onUiEvent(HomeEvent.BrowseCatalogClicked)
            assertEquals(HomeNavigation.Catalog, awaitItem())
        }
    }
}
