package com.example.androidproject1.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.example.androidproject1.core.ui.state.AlertState
import com.example.androidproject1.core.ui.text.resolve

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun StateAlertDialog(
    state: AlertState,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        // Tagged here rather than per screen: every confirm-then-act dialog in the app is this
        // one, and a flow that taps "Log out" by text hits the button behind the dialog when the
        // screen offers the same word — which is exactly what qa.2 found.
        modifier = modifier
            // A dialog is its own window, so it does not inherit the scaffold's setting — without
            // this the tags below exist in Compose's tree and are invisible to anything driving
            // the device.
            .semantics { testTagsAsResourceId = true }
            .testTag(ALERT_DIALOG_TAG),
        onDismissRequest = { if (state.dismissible) onDismiss() },
        title = state.title?.let { { Text(text = it.resolve()) } },
        text = state.message?.let { { Text(text = it.resolve()) } },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(ALERT_CONFIRM_TAG)) {
                Text(text = state.confirmLabel.resolve())
            }
        },
        dismissButton = state.declineLabel?.let { label ->
            {
                TextButton(onClick = onDecline, modifier = Modifier.testTag(ALERT_DECLINE_TAG)) {
                    Text(text = label.resolve())
                }
            }
        },
    )
}

/** The dialog itself, so a flow can assert one is open without naming its copy. */
const val ALERT_DIALOG_TAG = "alert_dialog"

const val ALERT_CONFIRM_TAG = "alert_confirmButton"

const val ALERT_DECLINE_TAG = "alert_declineButton"
