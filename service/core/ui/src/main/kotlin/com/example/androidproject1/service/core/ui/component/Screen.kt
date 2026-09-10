package com.example.androidproject1.service.core.ui.component

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.event.UiEvent
import com.example.androidproject1.service.core.ui.permission.openAppSettings
import com.example.androidproject1.service.core.ui.util.CollectEffect
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * Wraps a feature's stateless screen composable.
 *
 * The single place that collects state, interprets [UiCommand]s, renders the loading overlay, the
 * alert dialog and empty/error content, and delivers navigation intents. The [content] lambda
 * therefore only ever receives a non-null state, and a destination never writes a collector.
 *
 * @param onNavigation turns this screen's navigation intents into back-stack calls. Collected
 * only while the UI is at least STARTED; an intent emitted below that is buffered and delivered on
 * resume rather than dropped.
 */
@Composable
fun <State, Event : UiEvent, Navigation : Any> Screen(
    viewModel: BaseViewModel<State, Event, Navigation>,
    onNavigation: (Navigation) -> Unit = {},
    content: @Composable (State, (Event) -> Unit) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    ScreenSurface(modifier = Modifier.fillMaxSize()) {
        // A failure or empty state stands in for the content rather than covering it: there is
        // nothing behind it worth showing.
        val contentState = uiState.content
        if (contentState != null) {
            ContentMessage(
                state = contentState,
                onAction = { viewModel.onSystemEvent(SystemEvent.ContentAction(contentState.id)) },
            )
        } else {
            uiState.data?.let { content(it, viewModel::onUiEvent) }
        }

        uiState.alert?.let { alert ->
            StateAlertDialog(
                state = alert,
                onConfirm = {
                    viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(alert.id, alert.payload))
                },
                onDecline = {
                    viewModel.onSystemEvent(SystemEvent.AlertResult.Declined(alert.id, alert.payload))
                },
                onDismiss = {
                    viewModel.onSystemEvent(SystemEvent.AlertResult.Dismissed(alert.id, alert.payload))
                },
            )
        }

        uiState.loading?.let { LoadingOverlay(state = it) }

        // The activity is edge to edge and `Screen()` applies no insets to its content, so the
        // host pads itself — without this the snackbar sits under the gesture-navigation bar.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding(),
        )
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activity = LocalActivity.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    CollectEffect(flow = viewModel.navigation, action = onNavigation)

    CollectEffect(flow = viewModel.command) { command ->
        when (command) {
            is UiCommand.ShowToast ->
                Toast.makeText(context, command.message.resolve(context), Toast.LENGTH_SHORT).show()

            is UiCommand.ShowSnackbar -> snackbarScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = command.message.resolve(context),
                    actionLabel = command.actionLabel?.resolve(context),
                    withDismissAction = command.withDismissAction,
                    duration = if (command.actionLabel == null) {
                        SnackbarDuration.Short
                    } else {
                        SnackbarDuration.Long
                    },
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onSystemEvent(SystemEvent.SnackbarAction(command.id))
                }
            }

            is UiCommand.NavigateBack -> backDispatcher?.onBackPressed()

            is UiCommand.CloseApp -> activity?.finishAndRemoveTask()

            is UiCommand.OpenBrowser -> uriHandler.openUri(command.url)

            is UiCommand.OpenAppSettings -> context.openAppSettings()
        }
    }
}

// Always a Box inside, so the snackbar host has a BoxScope to align itself in.
@Composable
private fun ScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}
