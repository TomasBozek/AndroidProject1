package com.example.androidproject1.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.state.AlertState
import com.example.androidproject1.core.ui.text.resolve

@Composable
fun StateAlertDialog(
    state: AlertState,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { if (state.dismissible) onDismiss() },
        title = state.title?.let { { Text(text = it.resolve()) } },
        text = state.message?.let { { Text(text = it.resolve()) } },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = state.confirmLabel.resolve())
            }
        },
        dismissButton = state.declineLabel?.let { label ->
            {
                TextButton(onClick = onDecline) {
                    Text(text = label.resolve())
                }
            }
        },
    )
}
