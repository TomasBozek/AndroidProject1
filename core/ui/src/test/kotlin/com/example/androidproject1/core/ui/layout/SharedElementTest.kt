package com.example.androidproject1.core.ui.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The path every preview, test and gallery page takes (D76): no host has provided a scope, so the
 * modifier is a no-op and the element draws as if it were not there. navigation3's own local
 * would throw here; this one must not.
 */
@RunWith(RobolectricTestRunner::class)
class SharedElementTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `outside any transition the element simply draws`() {
        compose.setContent {
            AppTheme {
                AppText(
                    text = "Pilsner Urquell",
                    modifier = Modifier
                        .testTag(TAG)
                        .appSharedElement("product/1/name"),
                )
            }
        }

        compose.onNodeWithTag(TAG).assertIsDisplayed()
    }

    private companion object {

        const val TAG = "component_underTest"
    }
}
