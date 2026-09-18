package com.example.androidproject1.core.ui.layout

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The scope a shared element travels in, provided once by the host around the whole display —
 * `null` everywhere else, which is what a preview, a test and the gallery see (D76).
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * The entering or leaving entry's visibility, re-provided by the host inside each entry.
 * navigation3 has a local for the same thing and it throws when read outside an entry; this one
 * is `null` there instead, so a screen can be composed anywhere.
 */
val LocalSharedElementVisibility = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks this element as the one with [key] on the screen it is leaving or arriving at, so the
 * two are drawn as one thing moving between them. A no-op when either local is absent: the
 * element then draws exactly as it would without the modifier, which is why the goldens of a
 * screen that uses it do not change.
 *
 * The bounds move over [AppTheme.motion]'s screen duration, the same token the push and the pop
 * slide with, so the two animations end together.
 */
@Composable
fun Modifier.appSharedElement(key: String): Modifier {
    val transition = LocalSharedTransitionScope.current ?: return this
    val visibility = LocalSharedElementVisibility.current ?: return this
    val screenMillis = AppTheme.motion.screenMillis
    return with(transition) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibility,
            boundsTransform = { _, _ -> tween(durationMillis = screenMillis) },
        )
    }
}
