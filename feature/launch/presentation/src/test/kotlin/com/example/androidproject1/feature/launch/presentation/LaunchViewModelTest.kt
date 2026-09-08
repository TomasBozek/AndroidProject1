package com.example.androidproject1.feature.launch.presentation

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LaunchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `shows its screen and never navigates itself`() = runTest {
        // MainViewModel decides when the app leaves the launch screen — see LaunchNavigation.
        val state = LaunchViewModel(logger = FakeLogger()).state.value

        assertEquals(LaunchState, state.data)
        assertNull(state.loading)
        assertNull(state.alert)
    }
}
