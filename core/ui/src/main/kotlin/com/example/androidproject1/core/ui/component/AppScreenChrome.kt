package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.service.core.ui.component.ALERT_CONFIRM_TAG
import com.example.androidproject1.service.core.ui.component.ALERT_DECLINE_TAG
import com.example.androidproject1.service.core.ui.component.ALERT_DIALOG_TAG
import com.example.androidproject1.service.core.ui.component.ScreenChrome
import com.example.androidproject1.service.core.ui.state.AlertState
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.state.LoadingState
import com.example.androidproject1.service.core.ui.text.resolve
import com.example.androidproject1.service.core.ui.text.toUiText
import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost

/**
 * This app's answer to [ScreenChrome] (D50): the four things `Screen()` draws, drawn out of the
 * design system instead of out of Material.
 *
 * Installed by `AppTheme`, so a screen gets it by being a screen. Nothing here is new UI — the
 * empty state is [AppEmptyState], the alert is [AppDialog], the spinner is [AppSpinner] — which is
 * the point: the chrome and the content now go out of date together, and a re-brand reaches the
 * loading overlay the same way it reaches everything else.
 */
object AppScreenChrome : ScreenChrome {

    @Composable
    override fun Surface(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.surfaceBase),
            content = content,
        )
    }

    @Composable
    override fun ContentMessage(state: ContentState, onAction: () -> Unit, modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxSize()
                // Shown instead of the content, which may or may not have had a scaffold of its
                // own, so it pads itself.
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            AppEmptyState(
                // ContentState.Empty carries no title by default and AppEmptyState wants one; the
                // message is the sentence that matters either way, so it moves up rather than
                // inventing a heading nobody wrote.
                title = state.title?.resolve() ?: state.message.resolve(),
                message = if (state.title != null) state.message.resolve() else "",
                actionLabel = state.actionLabel?.resolve(),
                onAction = onAction.takeIf { state.actionLabel != null },
                modifier = Modifier.wrapContentHeight(),
            )
        }
    }

    @Composable
    override fun LoadingOverlay(state: LoadingState, modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.scrim.copy(alpha = AppTheme.colors.scrimAlpha))
                // Swallow touches so the screen underneath stays uninteractive while loading.
                .pointerInput(Unit) {},
            verticalArrangement = Arrangement.spacedBy(
                AppTheme.spacing.stack.md,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppSpinner(color = AppTheme.colors.textOnScrim)
            // On the scrim, not on the surface. `textOnScrim` is the one role that does not flip
            // between the themes, because the scrim does not either.
            AppText(
                text = state.message.resolve(),
                role = TextRole.Body,
                color = AppTheme.colors.textOnScrim,
            )
        }
    }

    @Composable
    override fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier) {
        MaterialSnackbarHost(hostState = hostState, modifier = modifier) { data ->
            AppToast(
                message = data.visuals.message,
                actionLabel = data.visuals.actionLabel,
                onAction = data::performAction,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.inset.md),
            )
        }
    }

    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Composable
    override fun AlertDialog(
        state: AlertState,
        onConfirm: () -> Unit,
        onDecline: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier,
    ) {
        AppDialog(
            // A dialog is its own window, so it does not inherit the scaffold's setting — without
            // this the tags below exist in Compose's tree and are invisible to anything driving
            // the device.
            modifier = modifier
                .semantics { testTagsAsResourceId = true }
                .testTag(ALERT_DIALOG_TAG),
            title = state.title?.resolve().orEmpty(),
            message = state.message?.resolve(),
            onDismiss = { if (state.dismissible) onDismiss() },
            actions = {
                state.declineLabel?.let { label ->
                    AppButton(
                        label = label.resolve(),
                        onClick = onDecline,
                        kind = ButtonKind.Ghost,
                        modifier = Modifier.testTag(ALERT_DECLINE_TAG),
                    )
                }
                AppButton(
                    label = state.confirmLabel.resolve(),
                    onClick = onConfirm,
                    modifier = Modifier.testTag(ALERT_CONFIRM_TAG),
                )
            },
        )
    }
}

/**
 * The two chrome elements a preview can see. The alert dialog and the snackbar host draw in windows
 * of their own, so they are covered by `OverlayScreenshotTest` in this module instead —
 * `CLAUDE.md` § Testing a screen.
 */
@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.lg)) {
        Box(modifier = Modifier.height(PREVIEW_ELEMENT_HEIGHT)) {
            AppScreenChrome.ContentMessage(
                state = ContentState.Error(message = "Nothing came back. Try again.".toUiText()),
                onAction = {},
                modifier = Modifier,
            )
        }
        Box(modifier = Modifier.height(PREVIEW_ELEMENT_HEIGHT)) {
            AppScreenChrome.LoadingOverlay(
                state = LoadingState(message = "Saving the photo".toUiText()),
                modifier = Modifier,
            )
        }
    }
}

private val PREVIEW_ELEMENT_HEIGHT = 200.dp
