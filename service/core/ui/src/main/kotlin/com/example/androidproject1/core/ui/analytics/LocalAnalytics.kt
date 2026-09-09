package com.example.androidproject1.core.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.androidproject1.core.domain.Analytics

/**
 * The [Analytics] a composable reports to.
 *
 * Unlike `LocalNavResultStore`, the absence of a provider is not an error: it defaults to
 * [Analytics.NoOp]. A screen has to render in a preview, in a screenshot test and in a Robolectric
 * test that has no Koin graph at all, and none of those should have to install an analytics to draw
 * a button. A build that measures nothing therefore pays nothing, and a missing provider costs
 * measurements rather than a crash — the right way round for something no user can see.
 */
val LocalAnalytics = staticCompositionLocalOf { Analytics.NoOp }

/**
 * Installs [analytics] for everything composed inside.
 *
 * Goes around the nav display, above the entries, so it outlives any one screen — the same place
 * and for the same reason as `ProvideNavResultStore`.
 */
@Composable
fun ProvideAnalytics(analytics: Analytics, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAnalytics provides analytics, content = content)
}

/**
 * Reports one screen view per entry into composition.
 *
 * A [LaunchedEffect] and not a plain call, for two reasons. Composition can be re-run and thrown
 * away, so a report sent from inside it counts Compose's bookkeeping rather than the user; and
 * `AppScaffold`'s body runs on every recomposition — a state change, a keystroke, a theme switch —
 * so a plain call would count a view per keystroke the moment this call site stops being skippable.
 * Keyed on [screenId], so a scaffold reused across destinations reports the new one and a scaffold
 * that merely recomposed reports nothing.
 *
 * Worth knowing before rewriting this: neither reason is currently *observable* in a test. Compose
 * skips the call while [screenId] is unchanged, and the JUnit4 compose rule runs effects on an
 * unconfined dispatcher, so `ScreenViewTest` passes with a plain call too. It guards the contract
 * rather than proving the mechanism — do not read its green as permission to inline this.
 *
 * A `null` [screenId] reports nothing: a screen that has not opted into an id has no name to file
 * the view under, and inventing one would put a wrong name in the funnel rather than no name.
 */
@Composable
fun ScreenViewEffect(screenId: String?) {
    val analytics = LocalAnalytics.current
    LaunchedEffect(screenId, analytics) {
        if (screenId != null) analytics.screen(screenId)
    }
}
