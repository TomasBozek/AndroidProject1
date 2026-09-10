package com.example.androidproject1.service.core.ui.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.androidproject1.service.core.domain.Analytics
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Counts what was reported, in order, so "how many" and "which" are both assertable. */
private class RecordingAnalytics : Analytics {

    val screens = mutableListOf<String>()

    override fun screen(id: String) {
        screens += id
    }

    override fun event(name: String, params: Map<String, Any?>) = Unit
}

/**
 * A screen view is reported once per entry into composition, and never again for a recomposition.
 *
 * This is the whole risk in reporting from `AppScaffold`: the scaffold's body runs on every
 * recomposition, and a view counted per keystroke is not something anything downstream can correct
 * for afterwards. What is pinned here is the contract — one view per entry, again when the id
 * changes, none without an id, none without a provider.
 *
 * Read `ScreenViewEffect`'s own note before changing it: the mechanism that guarantees the contract
 * is not observable from here, so these tests passing is not on its own evidence that a rewrite is
 * safe.
 */
@RunWith(RobolectricTestRunner::class)
class ScreenViewTest {

    @get:Rule
    val compose = createComposeRule()

    private val analytics = RecordingAnalytics()

    @Test
    fun `entering composition reports one view`() {
        compose.setContent {
            ProvideAnalytics(analytics) { ScreenViewEffect("SettingsScreen") }
        }
        compose.waitForIdle()

        assertEquals(listOf("SettingsScreen"), analytics.screens)
    }

    @Test
    fun `recomposing does not report a second view`() {
        var counter by mutableIntStateOf(0)
        compose.setContent {
            ProvideAnalytics(analytics) {
                // Reading the state here is what makes this composable — and so the report inside
                // it — recompose when the state changes, exactly as a screen does while in use.
                @Suppress("UNUSED_EXPRESSION")
                counter
                ScreenViewEffect("SettingsScreen")
            }
        }
        compose.waitForIdle()

        repeat(3) {
            counter++
            compose.waitForIdle()
        }

        // Compose skips the call while `screenId` is unchanged, so this passes today even without
        // the effect. It is kept as the guard for the day someone gives `ScreenViewEffect` an
        // unstable parameter and the call site stops being skippable — which is exactly when a
        // plain call would start counting a view per keystroke, silently.
        assertEquals(listOf("SettingsScreen"), analytics.screens)
    }

    @Test
    fun `a new screen id reports again`() {
        // The scaffold is reused across destinations, so the id changing is a new screen being
        // shown rather than the same one recomposing.
        var screenId by mutableStateOf("SettingsScreen")
        compose.setContent {
            ProvideAnalytics(analytics) { ScreenViewEffect(screenId) }
        }
        compose.waitForIdle()

        screenId = "CartScreen"
        compose.waitForIdle()

        assertEquals(listOf("SettingsScreen", "CartScreen"), analytics.screens)
    }

    @Test
    fun `a screen without an id reports nothing`() {
        compose.setContent {
            ProvideAnalytics(analytics) { ScreenViewEffect(null) }
        }
        compose.waitForIdle()

        assertEquals(emptyList<String>(), analytics.screens)
    }

    @Test
    fun `no provider is not a crash`() {
        // The default has to be NoOp rather than an error: previews, screenshot tests and every
        // Robolectric screen test compose a scaffold with no Koin graph behind them.
        compose.setContent { ScreenViewEffect("SettingsScreen") }
        compose.waitForIdle()

        assertEquals(emptyList<String>(), analytics.screens)
    }
}
