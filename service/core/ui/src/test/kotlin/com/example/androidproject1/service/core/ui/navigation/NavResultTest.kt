package com.example.androidproject1.service.core.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val RESULT_KEY = "picked_product"

/**
 * A push and a pop, and the value that crosses between them.
 *
 * The two screens are swapped by a boolean rather than driven through a real `NavDisplay`: what is
 * under test is that the store outlives the responder leaving composition, and a boolean destroys
 * it just as thoroughly as a pop does — without making this a Navigation 3 test.
 */
@RunWith(RobolectricTestRunner::class)
class NavResultTest {

    @get:Rule
    val compose = createComposeRule()

    private val received = mutableListOf<String>()

    @Composable
    private fun Requester() {
        NavResultEffect<String>(RESULT_KEY) { received.add(it) }
        Text(text = "requester", modifier = Modifier.testTag("requester"))
    }

    @Composable
    private fun Responder(onPicked: () -> Unit) {
        val setNavResult = rememberNavResultSender(RESULT_KEY)
        Text(
            text = "responder",
            modifier = Modifier
                .testTag("pick")
                .clickable {
                    setNavResult("croissant")
                    onPicked()
                },
        )
    }

    private fun renderFlow() {
        compose.setContent {
            ProvideNavResultStore {
                var onResponder by remember { mutableStateOf(false) }
                if (onResponder) {
                    Responder(onPicked = { onResponder = false })
                } else {
                    Requester()
                    Text(
                        text = "push",
                        modifier = Modifier
                            .testTag("push")
                            .clickable { onResponder = true },
                    )
                }
            }
        }
    }

    @Test
    fun `a value picked on the responder reaches the requester after the pop`() {
        renderFlow()

        compose.onNodeWithTag("push").performClick()
        compose.onNodeWithTag("pick").performClick()
        // The delivery is a LaunchedEffect, and the rule only auto-syncs before its own actions.
        compose.waitForIdle()

        // The responder has left composition; the store is above it, so the value survived.
        assertEquals(listOf("croissant"), received)
    }

    @Test
    fun `a result is delivered once, not again on every recomposition`() {
        renderFlow()

        compose.onNodeWithTag("push").performClick()
        compose.onNodeWithTag("pick").performClick()
        // There and back again, picking a second time: two selections, two deliveries — and not
        // three, which is what a result that is read but never consumed would produce.
        compose.onNodeWithTag("push").performClick()
        compose.onNodeWithTag("pick").performClick()
        compose.waitForIdle()

        assertEquals(listOf("croissant", "croissant"), received)
    }

    @Test
    fun `nothing is delivered when the responder sets nothing`() {
        renderFlow()

        compose.onNodeWithTag("requester").assertIsDisplayed()
        compose.waitForIdle()

        assertEquals(emptyList<String>(), received)
    }

    @Test
    fun `the store survives being saved and restored`() {
        // What process death does to it: the map is saved, and a new store is built from it.
        val store = NavResultStore()
        store.set(RESULT_KEY, "croissant")

        val restored = with(NavResultStore.Saver) {
            val saved = requireNotNull(SaverScope { true }.save(store))
            requireNotNull(restore(saved))
        }

        assertEquals("croissant", restored.consume(RESULT_KEY))
        // Consumed, so a second read finds nothing.
        assertEquals(null, restored.consume(RESULT_KEY))
    }
}
