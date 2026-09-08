package com.example.androidproject1.feature.template.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.ui.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric because [androidx.navigation.toRoute] decodes through an
 * `android.os.Bundle`, which a plain JVM test does not have. Only screens that take navigation
 * arguments need this; the rest stay on the JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplateArgsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // What the framework restores into the ViewModel after process death.
    private fun savedState() = SavedStateHandle(mapOf("templateId" to "example"))

    private fun viewModel() = TemplateArgsViewModel(
        logger = FakeLogger(),
        savedStateHandle = savedState(),
    )

    @Test
    fun `reads its argument from the route`() = runTest {
        val state = viewModel().state.value

        assertEquals("example", state.data?.templateId)
    }
}
