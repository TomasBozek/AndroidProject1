package com.example.androidproject1.core.ui.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.resolve
import com.example.androidproject1.service.core.ui.R

/**
 * What to tell the user about a permission they have not granted.
 *
 * Plain data, so it can be built anywhere — including a ViewModel. The UI it turns into is
 * [PermissionGate]'s `denied` slot, which is where a composable belongs.
 *
 * @property settingsLabel shown instead of [actionLabel] once asking again would do nothing.
 */
@Immutable
data class PermissionRationale(
    val title: UiText,
    val message: UiText,
    val actionLabel: UiText,
    val settingsLabel: UiText,
)

/**
 * Composes [content] only while [permissions] are granted, and the rationale otherwise.
 *
 * The point of the type: the caller cannot forget the ungranted case, because there is nothing to
 * forget — `content` simply does not run. A `granted: Boolean` returned from a helper leaves an
 * `if (!granted) return` for every caller to remember, and one of them will not.
 *
 * @param denied replaces the default rationale UI. It receives the request, so it can call
 * `request()` or read `status` for itself.
 */
@Composable
fun PermissionGate(
    vararg permissions: String,
    rationale: PermissionRationale,
    modifier: Modifier = Modifier,
    denied: @Composable (PermissionRequest) -> Unit = { request ->
        PermissionRationaleContent(
            rationale = rationale,
            request = request,
            modifier = modifier,
        )
    },
    content: @Composable () -> Unit,
) {
    val request = rememberPermissionRequest(permissions = permissions)

    if (request.status is PermissionStatus.Granted) content() else denied(request)
}

/**
 * The default rationale: a title, a message, and the one button that can still move things on —
 * asking again while that is possible, and system settings once it is not.
 */
@Composable
fun PermissionRationaleContent(
    rationale: PermissionRationale,
    request: PermissionRequest,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val status = request.status
    val canAskAgain = status !is PermissionStatus.Denied || status.canAskAgain

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = rationale.title.resolve(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = rationale.message.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Button(
            onClick = { if (canAskAgain) request.request() else context.openAppSettings() },
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag(PERMISSION_ACTION_TAG),
        ) {
            val label = if (canAskAgain) rationale.actionLabel else rationale.settingsLabel
            Text(text = label.resolve())
        }
    }
}

/**
 * The one button the rationale has, so a test or a flow can tap it without naming its copy — which
 * changes with `canAskAgain` and with the language.
 */
const val PERMISSION_ACTION_TAG = "permission_actionButton"

/** The wording every permission needs, so only the specific part is written per call site. */
fun permissionRationale(title: UiText, message: UiText): PermissionRationale = PermissionRationale(
    title = title,
    message = message,
    actionLabel = UiText.Resource(R.string.core_permission_allow),
    settingsLabel = UiText.Resource(R.string.core_permission_open_settings),
)
