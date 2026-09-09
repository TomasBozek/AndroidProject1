package com.example.androidproject1.feature.gallery.presentation.gallerydetail

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.gallery.presentation.galleryEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have.
private const val ROBOLECTRIC_SDK = 35

/**
 * The gallery's demos have to be usable, not only visible.
 *
 * The components are stateless, so a demo that passes a constant looks right and does nothing
 * when tapped. These tests reach the control inside a variant — every variant carries the same
 * `galleryDetail_variantItem` tag, so the control is found by `hasAnyAncestor` rather than by
 * text — and assert that trying it changes it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class GalleryDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<GalleryDetailEvent>()

    private fun render(componentId: String) {
        val entry = requireNotNull(galleryEntry(componentId)) { "$componentId is not in the catalogue" }
        val state = GalleryDetailState(
            componentId = entry.id,
            name = entry.name,
            group = entry.group,
            summary = entry.summary,
        )
        compose.setContent {
            AppTheme {
                GalleryDetailScreen(state = state, onEvent = events::add)
            }
        }
    }

    private fun insideAVariant(matcher: SemanticsMatcher) =
        matcher and hasAnyAncestor(hasTestTag("galleryDetail_variantItem"))

    @Test
    fun `renders the screen and its variants`() {
        render("checkbox")

        compose.onNodeWithTag("GalleryDetailScreen").assertIsDisplayed()
        compose.onAllNodesWithTag("galleryDetail_variantItem").onFirst().assertIsDisplayed()
    }

    @Test
    fun `a checkbox demo toggles when tapped`() {
        render("checkbox")
        // The first variant is the one that starts on.
        val checkbox = compose.onAllNodes(insideAVariant(isToggleable())).onFirst()

        checkbox.assertIsOn()
        checkbox.performClick()
        checkbox.assertIsOff()
    }

    @Test
    fun `a text field demo keeps what is typed`() {
        render("textfield")
        // The first variant is the empty one.
        val field = compose.onAllNodes(insideAVariant(hasSetTextAction())).onFirst()

        field.performTextInput("Pilsner")

        field.assert(hasText("Pilsner"))
    }
}
