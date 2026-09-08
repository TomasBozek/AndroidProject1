package com.example.androidproject1.feature.template.presentation

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * The seventh file of a screen. Generated with the other six so that a screen starts testable
 * rather than becoming testable later.
 *
 * Replace the assertions below with this screen's real behaviour: an event produces the state the
 * user should see, and a navigation intent is emitted where one is expected.
 */
class TemplateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = TemplateViewModel(logger = FakeLogger())

    @Test
    fun `renders its initial state immediately`() = runTest {
        val state = viewModel().state.value

        assertEquals(TemplateState.PREVIEW, state.data)
        // A screen given an initialState should not start behind the loading overlay.
        assertNull(state.loading)
    }

    @Test
    fun `starts with no alert and no error content`() = runTest {
        val state = viewModel().state.value

        assertNull(state.alert)
        assertNull(state.content)
    }
}
