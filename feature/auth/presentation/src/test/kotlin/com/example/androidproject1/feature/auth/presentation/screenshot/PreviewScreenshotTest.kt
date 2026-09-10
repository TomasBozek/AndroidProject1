package com.example.androidproject1.feature.auth.presentation.screenshot

import com.example.androidproject1.service.core.ui.screenshot.PreviewScreenshotSpec
import com.example.androidproject1.service.core.ui.screenshot.previewsIn
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * A golden per preview in this module. Everything but the package tree is [PreviewScreenshotSpec],
 * which is where the reasoning and the record/verify commands are.
 *
 * One per module (D35) because a module's test sees only its own classpath.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewScreenshotTest(
    preview: ComposablePreview<AndroidPreviewInfo>,
) : PreviewScreenshotSpec(preview) {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> = previewsIn(
            "com.example.androidproject1.feature.auth.presentation",
        )
    }
}
