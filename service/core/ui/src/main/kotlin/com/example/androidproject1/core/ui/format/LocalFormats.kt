package com.example.androidproject1.core.ui.format

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

/**
 * The [Formats] a screen reads: `LocalFormats.current.money(product.price)`.
 *
 * A composition local rather than a parameter threaded through every composable, for the same
 * reason the theme is one: a price four levels deep in a list row should not need its own
 * argument. The default is the device's locale, so nothing has to provide it for the app to be
 * correct — a provider is for the cases that differ from the device, and for a test that wants a
 * fixed locale:
 *
 * ```kotlin
 * CompositionLocalProvider(LocalFormats provides Formats(Locale.forLanguageTag("cs-CZ"))) { … }
 * ```
 *
 * `compositionLocalOf` rather than `staticCompositionLocalOf`: a locale change is rare but it must
 * recompose the things that read it, and there are few enough of them that tracking is cheap.
 */
val LocalFormats = compositionLocalOf { Formats(Locale.getDefault()) }
