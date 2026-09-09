package com.example.androidproject1.core.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Test

/** A switch takes effect immediately, so the only question is whether the tap arrives. */
class AppSwitchTest : ComponentTest() {

    @Test
    fun `the whole row is the target, not the 44 dp track`() {
        var value: Boolean? = null
        themed {
            AppSwitch(
                checked = false,
                onCheckedChange = { value = it },
                label = "Print receipt automatically",
                supporting = "Every sale",
                modifier = Modifier.testTag(TAG),
            )
        }

        compose.onNodeWithTag(TAG).performClick()

        assertEquals(true, value)
    }

    @Test
    fun `an on switch reports on and turns off`() {
        var value: Boolean? = null
        themed {
            AppSwitch(
                checked = true,
                onCheckedChange = { value = it },
                label = "Sounds",
                modifier = Modifier.testTag(TAG),
            )
        }

        compose.onNodeWithTag(TAG).assertIsOn()
        compose.onNodeWithTag(TAG).performClick()

        assertEquals(false, value)
    }

    @Test
    fun `a disabled switch emits nothing`() {
        var changes = 0
        themed {
            AppSwitch(
                checked = false,
                onCheckedChange = { changes++ },
                label = "Unavailable",
                enabled = false,
                modifier = Modifier.testTag(TAG),
            )
        }

        compose.onNodeWithTag(TAG).assertIsNotEnabled()
        compose.onNodeWithTag(TAG).performClick()

        assertEquals(0, changes)
    }
}
