package com.example.androidproject1.feature.home.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.inspectionMode
import com.github.takahirom.roborazzi.manualAdvance
import com.github.takahirom.roborazzi.toRoborazziComposeOptions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

private const val ROBOLECTRIC_SDK = 35

// One frame at 60 Hz. The clock is advanced by hand rather than left to run to idle, because a
// screen holding a CircularProgressIndicator never reaches idle — the capture would wait forever.
// One frame is enough for the first composition and layout, and it is the same frame every time,
// which is what a golden needs.
private const val ONE_FRAME_MILLIS = 16L

/**
 * A golden image per `@ScreenPreview` and `@ComponentPreview` in this module.
 *
 * **Scanned, not listed.** The previews already exist and already name the cases worth looking at
 * — light, dark and 1.5× font for a component, five device shapes for a screen — so a second list
 * of them here would be two descriptions of the same thing, kept in step by hand. Add a preview
 * and its goldens appear; delete one and its goldens are left for `git status` to point at.
 *
 * One copy per `presentation` module and one in `:core:ui` (D35): a module's test sees only its
 * own classpath, so this is the only way a screen and a component are both covered.
 *
 * ```
 * ./gradlew recordRoborazziDebug   # write the goldens
 * ./gradlew verifyRoborazziDebug   # check them — this is what CI runs
 * ```
 *
 * **Look at what you record.** A golden nobody opened is a test that passes forever; a blank or
 * clipped image asserts the blankness just as firmly as a correct one asserts the layout.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have — the same pin every screen test carries.
// The qualifier is the device a preview gets when it names none — `@ComponentPreview` never
// does. Robolectric's own default is 320x470dp, which is shorter than a phone and clips a
// component preview that stacks its variants; this is an ordinary phone instead.
@Config(sdk = [ROBOLECTRIC_SDK], qualifiers = "w400dp-h900dp-xhdpi")
// Robolectric's legacy renderer draws nothing but a stub; NATIVE is what makes the pixels real.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PreviewScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun preview() {
        // `preview.captureRoboImage`, not the plain one: this overload reads the @Preview's own
        // device, uiMode and fontScale, which is what makes "Dark" dark rather than a second copy
        // of the light image. Those options are what `manualAdvance` is added *to* — a fresh
        // `RoborazziComposeOptions { }` would replace them, and the five variants of a screen
        // would come out as five identical files.
        preview.captureRoboImage(
            filePath = "src/test/screenshots/${AndroidPreviewScreenshotIdBuilder(preview).build()}.png",
            roborazziComposeOptions = preview.toRoborazziComposeOptions()
                .builder()
                .manualAdvance(compose, ONE_FRAME_MILLIS)
                // The same mode Android Studio renders a preview in, and the reason `AppImage` is
                // a golden at all: outside it, Coil starts a real request that has neither
                // finished nor failed by the time the frame is taken, so the image is a skeleton
                // on one run and an error icon on the next. In inspection mode nothing is
                // fetched.
                .inspectionMode(true)
                .build(),
        )
    }

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                // The previews are `private`, as a preview should be — nothing calls one. The
                // scanner reads them anyway once asked; this call is what ui.2 was missing.
                .scanPackageTrees("com.example.androidproject1.feature.home.presentation")
                .includePrivatePreviews()
                .getPreviews()
    }
}
