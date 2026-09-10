package com.example.androidproject1.service.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject1.service.core.ui.state.AlertState
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.state.LoadingState
import com.example.androidproject1.service.core.ui.text.resolve
import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost

/** The dialog itself, so a flow can assert one is open without naming its copy. */
const val ALERT_DIALOG_TAG = "alert_dialog"

const val ALERT_CONFIRM_TAG = "alert_confirmButton"

const val ALERT_DECLINE_TAG = "alert_declineButton"

/**
 * The four things [Screen] draws that are not the feature's content — and the seam that lets an app
 * draw them in its own design system (D50).
 *
 * `Screen()` renders a base surface, a loading overlay, an alert dialog and empty/error content
 * centrally, so no feature reimplements a spinner or a "couldn't load" layout. That is the right
 * design and it left one thing wrong: those four were raw Material with `dp` literals in them,
 * which is the exact rule `CLAUDE.md` § Design system puts on every feature. `:service:core:ui` may
 * not name `:core:*` — it is copied out and reused — so it could not read `AppTheme` to fix it.
 *
 * This is the way out that costs nothing at either end. `:core:ui` already depends on this module
 * and every screen already composes inside `AppTheme`, so the theme installs an implementation
 * built from `App*` components and `Screen()` reads it. [DefaultScreenChrome] below is what the
 * module does on its own, so `service/` still stands up when it is copied into a project that has
 * no design system yet.
 *
 * Implementations are objects, not lambdas, and the local is `static`: the chrome does not change
 * for the life of a composition, and reading it should not subscribe anything to a snapshot.
 */
@Stable
interface ScreenChrome {

    /** The base surface every screen sits on. */
    @Composable
    fun Surface(modifier: Modifier, content: @Composable BoxScope.() -> Unit)

    /** A failure or an empty state, shown **instead of** the content. */
    @Composable
    fun ContentMessage(state: ContentState, onAction: () -> Unit, modifier: Modifier)

    /** A blocking overlay over the content. */
    @Composable
    fun LoadingOverlay(state: LoadingState, modifier: Modifier)

    /**
     * The host the snackbars raised by `UiCommand.ShowSnackbar` are drawn in.
     *
     * The [hostState] is Material's, and stays Material's: it is the queue `Screen()` drives with a
     * suspending `showSnackbar`, not something drawn. Only the row it renders is the design
     * system's business, which is what this hands over.
     */
    @Composable
    fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier)

    /**
     * A dialog over the content. Whatever draws it must carry [ALERT_DIALOG_TAG],
     * [ALERT_CONFIRM_TAG] and [ALERT_DECLINE_TAG] and must set `testTagsAsResourceId` on its own
     * window — a dialog does not inherit the scaffold's, so without it the tags exist in Compose's
     * tree and are invisible to anything driving the device.
     */
    @Composable
    fun AlertDialog(
        state: AlertState,
        onConfirm: () -> Unit,
        onDecline: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier,
    )
}

val LocalScreenChrome = staticCompositionLocalOf<ScreenChrome> { DefaultScreenChrome }

/**
 * What this module draws with nothing else present: stock Material, and the only place in the
 * repository allowed to have `dp` literals in a screen's chrome.
 *
 * It is not what this app uses — `:core:ui` installs its own — and it is not dead code either. It
 * is what makes `export_service.py` produce a directory that compiles and runs on the first day of
 * a project that has no theme yet.
 */
object DefaultScreenChrome : ScreenChrome {

    @Composable
    override fun Surface(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
        Surface(modifier = modifier) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }

    @Composable
    override fun ContentMessage(state: ContentState, onAction: () -> Unit, modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                // Shown instead of the content, which may or may not have had a Scaffold of its own.
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.title?.let {
                Text(
                    text = it.resolve(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = state.message.resolve(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            state.actionLabel?.let {
                Button(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = it.resolve())
                }
            }
        }
    }

    @Composable
    override fun LoadingOverlay(state: LoadingState, modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                // Swallow touches so the screen underneath stays uninteractive while loading.
                .pointerInput(Unit) {},
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(text = state.message.resolve())
        }
    }

    @Composable
    override fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier) {
        MaterialSnackbarHost(hostState = hostState, modifier = modifier)
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
        AlertDialog(
            // Tagged here rather than per screen: every confirm-then-act dialog in the app is this
            // one, and a flow that taps "Log out" by text hits the button behind the dialog when
            // the screen offers the same word — which is exactly what qa.2 found.
            modifier = modifier
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

    private const val SCRIM_ALPHA = 0.4f
}
