package com.example.androidproject1.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.androidproject1.core.ui.getComposableString
import com.example.androidproject1.core.ui.state.AlertState

@Composable
fun AppAlertDialog(
    state: AlertState,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (state.dismissible) onDismiss() },
        title = state.title?.let { { Text(text = it.getComposableString()) } },
        text = state.message?.let { { Text(text = it.getComposableString()) } },
        confirmButton = {
            TextButton(onClick = onPrimaryClick) {
                Text(text = state.primaryButton.getComposableString())
            }
        },
        dismissButton = state.secondaryButton?.let { secondary ->
            {
                TextButton(onClick = onSecondaryClick) {
                    Text(text = secondary.getComposableString())
                }
            }
        },
    )
}
