package com.example.androidproject1.core.ui.component

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidproject1.core.ui.CommonEvent
import com.example.androidproject1.core.ui.CommonUiCommand
import com.example.androidproject1.core.ui.Event
import com.example.androidproject1.core.ui.util.CommandEffect
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel

/**
 * Wraps a feature's stateless screen composable.
 *
 * This is the single place in the app where state is collected and where [CommonUiCommand]s are
 * interpreted, and it renders the loading overlay and alert dialog centrally. The feature's own
 * [screen] lambda therefore only ever receives a non-null state.
 */
@Composable
fun <State, E : Event, Direction> Screen(
    viewModel: BaseViewModel<State, E, Direction>,
    isTransparent: Boolean = false,
    screen: @Composable (State, (E) -> Unit) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    ScreenWrapper(
        isTransparent = isTransparent,
        modifier = Modifier.fillMaxSize(),
    ) {
        uiState.data?.let { screen(it, viewModel::onUiEvent) }

        uiState.alert?.let { alert ->
            AppAlertDialog(
                state = alert,
                onPrimaryClick = {
                    viewModel.onCommonEvent(
                        CommonEvent.AlertDialogAction.PrimaryClicked(alert.id, alert.data),
                    )
                },
                onSecondaryClick = {
                    viewModel.onCommonEvent(
                        CommonEvent.AlertDialogAction.SecondaryClicked(alert.id, alert.data),
                    )
                },
                onDismiss = {
                    viewModel.onCommonEvent(
                        CommonEvent.AlertDialogAction.Dismissed(alert.id, alert.data),
                    )
                },
            )
        }

        uiState.loading?.let { ModalLoadingOverlay(state = it) }
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activity = LocalActivity.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    CommandEffect(commandFlow = viewModel.commonUiCommand) { command ->
        when (command) {
            is CommonUiCommand.ShowToast ->
                Toast.makeText(context, command.message.getString(context), Toast.LENGTH_SHORT).show()

            is CommonUiCommand.PressBack -> backDispatcher?.onBackPressed()

            is CommonUiCommand.CloseApp -> activity?.finishAndRemoveTask()

            is CommonUiCommand.OpenUri -> when (command) {
                is CommonUiCommand.OpenUri.ExternalBrowser -> uriHandler.openUri(command.uri)

                is CommonUiCommand.OpenUri.AppSettings -> context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }
        }
    }
}

@Composable
private fun ScreenWrapper(
    isTransparent: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (isTransparent) {
        Box(modifier = modifier) { content() }
    } else {
        Surface(modifier = modifier) { content() }
    }
}
