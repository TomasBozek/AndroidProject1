package com.example.androidproject1.core.ui.screenshot

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppDateField
import com.example.androidproject1.core.ui.component.AppDialog
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTimeField
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime

private const val ONE_FRAME_MILLIS = 16L

/** The tag on the field, so the tap that opens the overlay is not a tap on a string. */
private const val FIELD = "overlay_underTest"

/**
 * A golden per overlay, on the device shapes an overlay actually breaks on.
 *
 * `PreviewScreenshotTest` cannot see any of this. A dialog, a sheet and a menu each draw in a
 * window of their own, and `captureRoboImage` on a preview captures the composable — so every
 * overlay in the design system was invisible to a suite of 349 images. That is not a hypothetical:
 * it is why the date picker shipped clipped on a 360 dp phone and pushed its buttons off the
 * bottom in landscape, with every golden green.
 *
 * `captureScreenRoboImage` captures the screen rather than a composable, which is what pulls the
 * dialog window in.
 *
 * **The qualifiers are the test.** A picker looks fine at 400x900; the two shapes that break it are
 * the narrowest phone still supported and a phone turned on its side, so those are what each
 * overlay is recorded at. A golden here that is merely *pretty* has not been read: look for the
 * right-hand weekday column, and for both buttons.
 *
 * ```
 * ./gradlew :core:ui:recordRoborazziDebug   # write them
 * ./gradlew :core:ui:verifyRoborazziDebug   # check them
 * ```
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun `the date picker on the narrowest phone`() {
        openDateField()
        capture("overlay_datePicker_narrowPhone")
    }

    @Test
    @Config(qualifiers = "w720dp-h360dp-xhdpi")
    fun `the date picker in landscape`() {
        openDateField()
        capture("overlay_datePicker_landscape")
    }

    @Test
    @Config(qualifiers = "w720dp-h360dp-xhdpi")
    fun `the time picker in landscape`() {
        openTimeField()
        capture("overlay_timePicker_landscape")
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun `a dialog with more content than fits keeps its actions on screen`() {
        compose.setContent {
            AppTheme {
                AppDialog(
                    title = "Terms of collection",
                    onDismiss = {},
                    content = {
                        repeat(12) { paragraph ->
                            AppText(
                                text = "Paragraph ${paragraph + 1}. Something long enough that " +
                                    "twelve of them cannot fit on a short screen.",
                                role = TextRole.Body,
                            )
                        }
                    },
                    actions = { AppButton(label = "Accept", onClick = {}) },
                )
            }
        }
        capture("overlay_dialog_longContent")
    }

    private fun openDateField() {
        compose.setContent {
            AppTheme {
                AppDateField(
                    value = LocalDate.of(2026, 9, 9),
                    onValueChange = {},
                    label = "Delivery",
                    modifier = Modifier.testTag(FIELD),
                )
            }
        }
        openTheOverlay()
    }

    private fun openTimeField() {
        compose.setContent {
            AppTheme {
                AppTimeField(
                    value = LocalTime.of(19, 24),
                    onValueChange = {},
                    label = "Opened",
                    modifier = Modifier.testTag(FIELD),
                )
            }
        }
        openTheOverlay()
    }

    /**
     * A caller's `modifier` tags the whole field, so the tap goes to the clickable row inside it —
     * the pattern every component test here uses, and the reason none of them find anything by the
     * string it happens to be showing.
     */
    private fun openTheOverlay() {
        compose.onNode(hasClickAction() and hasAnyAncestor(hasTestTag(FIELD))).performClick()
    }

    private fun capture(name: String) {
        // Both, and in this order. `waitForIdle` is what applies the tap — without it the window
        // the tap opens does not exist yet and the capture is of the closed field, which is a
        // green golden asserting nothing. The frame after it is what draws the window's contents.
        // A `Dialog` has no enter animation of its own, so this settles rather than ticking on.
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(ONE_FRAME_MILLIS)
        captureScreenRoboImage(filePath = "src/test/screenshots/$name.png")
    }
}
