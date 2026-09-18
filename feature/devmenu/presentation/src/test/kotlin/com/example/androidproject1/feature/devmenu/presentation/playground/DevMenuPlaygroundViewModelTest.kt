package com.example.androidproject1.feature.devmenu.presentation.playground

import com.example.androidproject1.feature.devmenu.presentation.KnobValue
import com.example.androidproject1.feature.devmenu.presentation.defaults
import com.example.androidproject1.feature.devmenu.presentation.playgroundCatalog
import com.example.androidproject1.feature.devmenu.presentation.playgroundEntry
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** What the state becomes: an entry picked resets its knobs, a knob turned writes one key. */
class DevMenuPlaygroundViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = DevMenuPlaygroundViewModel(logger = FakeLogger())

    @Test
    fun `opens on the first entry with its knobs at their defaults`() = runTest {
        val state = viewModel().state.value

        val first = playgroundCatalog.first()
        assertEquals(first.id, state.data?.entryId)
        assertEquals(first.defaults(), state.data?.values)
        assertNull(state.loading)
    }

    @Test
    fun `picking an entry resets the knobs to that entry's defaults`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(DevMenuPlaygroundEvent.KnobChanged("label", KnobValue.Text("Delete")))

        viewModel.onUiEvent(DevMenuPlaygroundEvent.ComponentPicked("switch"))

        val switch = playgroundEntry("switch")!!
        assertEquals("switch", viewModel.state.value.data?.entryId)
        assertEquals(switch.defaults(), viewModel.state.value.data?.values)
    }

    @Test
    fun `turning a knob writes that key and leaves the rest`() = runTest {
        val viewModel = viewModel()
        val before = viewModel.state.value.data!!.values

        viewModel.onUiEvent(DevMenuPlaygroundEvent.KnobChanged("enabled", KnobValue.Bool(false)))

        assertEquals(before + ("enabled" to KnobValue.Bool(false)), viewModel.state.value.data?.values)
    }

    @Test
    fun `an entry the catalog does not have changes nothing`() = runTest {
        val viewModel = viewModel()
        val before = viewModel.state.value.data

        viewModel.onUiEvent(DevMenuPlaygroundEvent.ComponentPicked("teleporter"))

        assertEquals(before, viewModel.state.value.data)
    }

    @Test
    fun `up is a navigation intent`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DevMenuPlaygroundEvent.NavigateUpClicked)

        assertEquals(DevMenuPlaygroundNavigation.NavigateUp, viewModel.navigation.first())
    }
}
