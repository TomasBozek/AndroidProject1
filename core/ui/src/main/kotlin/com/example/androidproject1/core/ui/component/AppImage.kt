package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A remote image, with the two states a remote image always has.
 *
 * **This is the only place the app knows an image library exists.** A feature that imports Coil
 * directly fails `doctor.py`: swapping the loader should be a change to this file, and a screen
 * that reaches past it is a screen that has to be found and rewritten when that happens.
 *
 * Loading draws an [AppSkeleton] in the image's own shape rather than a spinner, so the layout
 * does not move when the bytes arrive. A failure draws a mark rather than collapsing to nothing:
 * an empty box says the design is broken, a mark says the picture is missing.
 */
@Composable
fun AppImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.lg,
    contentScale: ContentScale = ContentScale.Crop,
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.clip(shape),
    ) {
        when (painter.state.collectAsState().value) {
            is AsyncImagePainter.State.Loading -> AppSkeleton(shape = shape)
            is AsyncImagePainter.State.Error -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(AppTheme.colors.surfaceSunken),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = AppTheme.colors.textTertiary,
                    modifier = Modifier.size(24.dp),
                )
            }

            else -> SubcomposeAsyncImageContent()
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // No network in a preview, so this is the error state — which is the one worth seeing.
    AppImage(
        model = "https://example.invalid/product.jpg",
        contentDescription = null,
        modifier = Modifier.size(96.dp),
    )
}
