package com.example.androidproject1.core.ui.layout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable

/**
 * Which half of a list–detail pair a destination is.
 *
 * A screen never asks how wide the window is. It says what it *is* — the list, or the detail — and
 * `AppNavHost`'s scene strategy decides whether the two share a scene or follow one another. On a
 * phone nothing here has any effect at all, which is the point: one back stack, two layouts.
 *
 * These wrap `ListDetailSceneStrategy`'s own metadata so a feature does not import Material's
 * adaptive package directly, the same reason a feature composes `AppButton` rather than
 * Material's. It also means the day the layout comes from somewhere else, the features do not
 * change.
 *
 * ```
 * entry<ProductsDestination>(metadata = listPane(detailPlaceholder = { NothingPicked() })) { … }
 * entry<ProductDetailDestination>(metadata = detailPane()) { … }
 * ```
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun listPane(detailPlaceholder: @Composable () -> Unit): Map<String, Any> =
    ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() })

/**
 * The detail half of the pair.
 *
 * It carries no placeholder: the detail pane with nothing in it is the *list*'s problem, because
 * the list is what is on screen before anything has been picked.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun detailPane(): Map<String, Any> = ListDetailSceneStrategy.detailPane()
