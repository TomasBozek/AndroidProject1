package com.example.androidproject1.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects a one-shot [flow] only while the UI is at least in [minActiveState].
 *
 * The flows this is used with are backed by buffered channels, so an emission made while the UI is
 * below [minActiveState] is held and delivered on resume rather than dropped.
 */
@Composable
fun <T : Any> CollectEffect(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(flow) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            flow.collect(action)
        }
    }
}
