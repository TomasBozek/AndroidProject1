package com.example.androidproject1.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects a hot one-shot [commandFlow] only while the UI is at least in [lifecycleState].
 *
 * Because the underlying flows have no replay, a command emitted while below [lifecycleState] is
 * dropped — pass a lower state (e.g. [Lifecycle.State.CREATED]) when that matters.
 */
@Composable
fun <T : Any> CommandEffect(
    commandFlow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(commandFlow) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            commandFlow.collect(collector)
        }
    }
}
