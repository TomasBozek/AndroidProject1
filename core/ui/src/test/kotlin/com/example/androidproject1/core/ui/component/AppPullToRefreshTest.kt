package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import org.junit.Assert.assertEquals
import org.junit.Test

/** The gesture is the whole point: a pull calls back once, and the state is readable without eyes. */
class AppPullToRefreshTest : ComponentTest() {

    @Test
    fun `a pull calls onRefresh once`() {
        var refreshes = 0
        themed {
            AppPullToRefresh(
                refreshing = false,
                onRefresh = { refreshes++ },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TAG),
            ) {
                // Scrollable, as the real content is: the pull arrives through nested scrolling,
                // and a column that cannot scroll has nothing to hand up.
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    repeat(3) { AppListItem("Row $it") }
                }
            }
        }

        compose.onNodeWithTag(TAG).performTouchInput { swipeDown() }
        compose.waitForIdle()

        assertEquals(1, refreshes)
    }

    @Test
    fun `the content is drawn either way`() {
        themed {
            AppPullToRefresh(refreshing = true, onRefresh = {}, modifier = Modifier.testTag(TAG)) {
                AppListItem("Row", modifier = Modifier.testTag(CONTENT))
            }
        }

        compose.onNodeWithTag(CONTENT).assertIsDisplayed()
    }

    @Test
    fun `the state is on the box, for a screen reader and for a test`() {
        themed {
            AppPullToRefresh(refreshing = true, onRefresh = {}, modifier = Modifier.testTag(TAG)) {
                AppListItem("Row")
            }
        }

        compose.onNodeWithTag(TAG).assert(hasStateDescription(PULL_TO_REFRESH_REFRESHING))
    }

    @Test
    fun `an idle box says so`() {
        themed {
            AppPullToRefresh(refreshing = false, onRefresh = {}, modifier = Modifier.testTag(TAG)) {
                AppListItem("Row")
            }
        }

        compose.onNodeWithTag(TAG).assert(hasStateDescription(PULL_TO_REFRESH_IDLE))
    }

    private companion object {

        const val CONTENT = "component_content"
    }
}
