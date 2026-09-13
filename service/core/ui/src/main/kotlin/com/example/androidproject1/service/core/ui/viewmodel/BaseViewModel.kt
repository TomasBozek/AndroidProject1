package com.example.androidproject1.service.core.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.error.NetworkError
import com.example.androidproject1.service.core.domain.error.ServerError
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.ui.R
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.event.UiEvent
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.state.LoadingState
import com.example.androidproject1.service.core.ui.state.UiState
import com.example.androidproject1.service.core.ui.state.clearAlert
import com.example.androidproject1.service.core.ui.state.clearContent
import com.example.androidproject1.service.core.ui.state.setAlert
import com.example.androidproject1.service.core.ui.state.setContent
import com.example.androidproject1.service.core.ui.text.UiText
import com.example.androidproject1.service.core.ui.text.toUiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base class for every ViewModel, on a **State / Event / Navigation** contract: `State` is what the
 * screen renders, `Event` is what the user did, `Navigation` is a one-off navigation intent.
 *
 * Subclasses should not write try/catch or touch a loading flag — [execute] and [observe] handle
 * loading, error dialogs and cancellation.
 *
 * @param initialState the state the screen renders immediately. Pass `null` only for a screen that
 * genuinely cannot render until something is loaded — nothing is drawn while `data` is `null`, and
 * [updateData] logs rather than silently dropping an update that arrives first (D44). Ask for the
 * overlay with `loading = overlay()`; `null` no longer starts one on its own.
 *
 * A screen that takes navigation arguments declares its route key as a constructor parameter and
 * Koin passes it in — see `TemplateArgsViewModel`. There is no `SavedStateHandle` detour on
 * Navigation 3: the key is an ordinary object the back stack already holds. [savedStateHandle] is
 * not that — it is for the transient state a route key cannot carry: a half-typed search, a
 * scroll position, a partly filled form. See [saved].
 */
abstract class BaseViewModel<State, Event : UiEvent, Navigation>(
    initialState: State?,
    protected val logger: Logger,
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    companion object {

        const val ALERT_ID_ERROR = "error"

        const val SNACKBAR_ID_DEFAULT = "snackbar"

        /**
         * How long a source keeps running after the last collector leaves.
         *
         * Five seconds is the figure the Android guidance settled on: longer than any
         * configuration change, shorter than a user reading something else.
         */
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }

    protected val uiState = MutableStateFlow(UiState<State?>(data = initialState))
    val state: StateFlow<UiState<State?>> = uiState.asStateFlow()

    // Buffered channels, not a MutableSharedFlow: a shared flow with no replay silently drops what
    // is emitted while nothing collects, losing a navigation issued below STARTED. receiveAsFlow()
    // is single-consumer, which is what a one-shot event wants.
    private val navigationChannel = Channel<Navigation>(Channel.BUFFERED)
    val navigation: Flow<Navigation> = navigationChannel.receiveAsFlow()

    private val commandChannel = Channel<UiCommand>(Channel.BUFFERED)
    val command: Flow<UiCommand> = commandChannel.receiveAsFlow()

    // Counted rather than flagged so the overlay from a long call is not dismissed by a short one.
    private val activeCallCount = AtomicInteger(0)

    // Set by an [ErrorDisplay.Inline] call so the retry button can re-run it, keyed by the content
    // id the failure is shown under. Keeping the lambdas here rather than in ContentState is what
    // lets the state stay a comparable data class; keying them is what lets two inline loads be in
    // flight at once — a single slot always holds the call that started last, not the one that
    // failed, so the wrong call is retried.
    private val pendingRetries = ConcurrentHashMap<String, () -> Unit>()

    // The wording of the overlay currently up, kept beside the counter. Rebuilding a bare
    // LoadingState on every change would discard the message a caller asked for as soon as a
    // second call started or finished.
    @Volatile
    private var loadingMessage: UiText? = null

    open fun onUiEvent(event: Event) = Unit

    /**
     * The default dismisses the alert and retries a failed call; override to react to a specific
     * alert or content action, delegating the rest to `super`.
     */
    open fun onSystemEvent(event: SystemEvent) {
        when (event) {
            is SystemEvent.AlertResult -> uiState.clearAlert()

            is SystemEvent.ContentAction -> {
                uiState.clearContent()
                // Re-runs the call that failed under this id, with the same arguments and handlers.
                pendingRetries.remove(event.id)?.invoke()
            }

            // Nothing sensible to do by default: whoever raised the snackbar knows what its action
            // means. Overriding without delegating to super is what silently swallows the others.
            is SystemEvent.SnackbarAction -> Unit
        }
    }

    /**
     * Updates the screen's state in place: `updateData { copy(email = email) }`.
     *
     * A `null` `data` is not a state this can update, and it used to make the call a no-op with no
     * trace — which is how a favourite flag arriving before its product was lost without anything
     * to find. It is a programming error, not a state, so it is logged (D44).
     */
    protected fun updateData(action: State.() -> State) {
        if (uiState.value.data == null) {
            logger.w { "updateData ran before the first state — the update was dropped" }
            return
        }
        uiState.update { state -> state.copy(data = state.data?.action()) }
    }

    /**
     * A property that survives process death: read from [savedStateHandle] under [key], written
     * back on every set, [default] until anything has been saved.
     *
     * `null` [savedStateHandle] — most ViewModels are not given one, since most state is either
     * derived from a call or not worth this — falls back to an ordinary field, so `saved()` is
     * always safe to call: `private var query: String by saved("query", "")`.
     */
    protected fun <T> saved(key: String, default: T): ReadWriteProperty<Any?, T> {
        val handle = savedStateHandle ?: return FieldProperty(default)
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T =
                handle.get<T>(key) ?: default

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                handle[key] = value
            }
        }
    }

    protected fun showContent(state: ContentState) = uiState.setContent(state)

    protected fun clearContent() = uiState.clearContent()

    protected fun navigate(navigation: Navigation) {
        if (navigationChannel.trySend(navigation).isFailure) {
            logger.w { "Dropped navigation $navigation — channel full or closed" }
        }
    }

    protected fun sendCommand(command: UiCommand) {
        if (commandChannel.trySend(command).isFailure) {
            logger.w { "Dropped command $command — channel full or closed" }
        }
    }

    /**
     * A message the user reads and does not act on. A snackbar with no action (D63): there is one
     * mechanism, rendered inside the screen by the chrome, so it wears the theme and a screen test
     * can see it. A message that has to outlive its screen is a different feature.
     */
    protected fun showToast(message: UiText) = showSnackbar(message)

    /**
     * Prefer this to [showToast] for anything the user might want to act on or dismiss.
     *
     * @param id comes back as [SystemEvent.SnackbarAction] when the action button is pressed.
     * Only worth naming when the screen raises more than one actionable snackbar.
     */
    protected fun showSnackbar(
        message: UiText,
        actionLabel: UiText? = null,
        withDismissAction: Boolean = false,
        id: String = SNACKBAR_ID_DEFAULT,
    ) = sendCommand(
        UiCommand.ShowSnackbar(
            id = id,
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
        ),
    )

    /**
     * Reference-counted, so the overlay stays up until the last in-flight call finishes.
     *
     * @param message wording for the overlay. The most recent one wins while calls overlap, and is
     * forgotten once the last of them finishes.
     */
    protected open fun setLoading(active: Boolean, message: UiText? = null) {
        if (active && message != null) loadingMessage = message
        val count = if (active) {
            activeCallCount.incrementAndGet()
        } else {
            activeCallCount.updateAndGet { (it - 1).coerceAtLeast(0) }
        }
        if (count == 0) loadingMessage = null
        val state = if (count > 0) loadingMessage?.let(::LoadingState) ?: LoadingState() else null
        uiState.update { it.copy(loading = state) }
    }

    /**
     * Turns the shared loading overlay on for one [execute] or [observe] call: `loading = overlay()`.
     *
     * The overlay is opt-in (D44) — two call sites in three do not want one, and a screen that
     * renders its own inline wait used to have to say `loading = {}` to switch this off. Passing
     * the wording here rather than as a parameter of its own is what stops a message being set on a
     * call that shows no overlay to put it on.
     */
    protected fun overlay(message: UiText? = null): (Boolean) -> Unit = { setLoading(it, message) }

    /**
     * Runs a one-shot domain [action], turning an [Outcome.Failure] into an alert.
     *
     * @param loading how to reflect the in-flight state. Nothing, by default: pass
     * `loading = overlay()` for the shared overlay, or the screen's own flag for an inline wait.
     * @param onError return `true` to claim an error and suppress the default presentation.
     * @param errorDisplay where a failure goes: a dialog over the screen, a retryable message in
     * place of it, or nowhere. [ErrorDisplay.Inline] is usually right for the call that loads a
     * screen, and [ErrorDisplay.Alert] for one the user triggered.
     */
    protected fun <T> execute(
        loading: (Boolean) -> Unit = {},
        scope: CoroutineScope = viewModelScope,
        onError: suspend (DomainError) -> Boolean = { false },
        alertId: String = ALERT_ID_ERROR,
        errorDisplay: ErrorDisplay = ErrorDisplay.Alert,
        action: suspend () -> Outcome<T>,
        onData: suspend (T) -> Unit,
    ): Job {
        // Captured so SystemEvent.ContentAction can re-run exactly this call. Only meaningful for
        // Inline, which is the only display mode that offers the user a retry.
        var retry: (() -> Unit)? = null
        if (errorDisplay == ErrorDisplay.Inline) {
            retry = {
                execute(
                    loading = loading,
                    scope = scope,
                    onError = onError,
                    alertId = alertId,
                    errorDisplay = errorDisplay,
                    action = action,
                    onData = onData,
                )
            }
            pendingRetries[alertId] = retry
        }

        // Nothing failed, so there is nothing to retry: drop the lambda and the closures it holds
        // rather than leaving them until the ViewModel is cleared. Removed by identity, so a later
        // call that has since claimed this id keeps its own.
        fun forgetRetry() {
            retry?.let { pendingRetries.remove(alertId, it) }
        }

        return scope.launch {
            loading(true)
            try {
                when (val outcome = action()) {
                    is Outcome.Success -> runCatching { onData(outcome.data) }
                        .onSuccess { forgetRetry() }
                        .onFailure { handleError(it, onError, alertId, errorDisplay) }

                    is Outcome.Failure -> handleError(outcome.error, onError, alertId, errorDisplay)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                handleError(e, onError, alertId, errorDisplay)
            } finally {
                loading(false)
            }
        }
    }

    /**
     * Collects a domain [flow], clearing the loading state after its first emission.
     *
     * That first emission clears the overlay whether it succeeded or failed. The `finally` is not
     * enough on its own: this is for long-lived flows, which never complete, so a failed emission
     * would otherwise leave the overlay up for good.
     *
     * Exactly one `loading(false)` runs per `loading(true)`, whichever path gets there first. That
     * balance matters because [setLoading] is reference-counted: an unconditional `finally` on top
     * of the first-emission call would decrement twice, and the spare decrement would dismiss the
     * overlay belonging to whatever [execute] happened to be in flight at the time.
     */
    protected fun <T> observe(
        flow: suspend () -> Flow<Outcome<T>>,
        whileSubscribed: Boolean = false,
        loading: (Boolean) -> Unit = {},
        scope: CoroutineScope = viewModelScope,
        onError: suspend (DomainError) -> Boolean = { false },
        alertId: String = ALERT_ID_ERROR,
        errorDisplay: ErrorDisplay = ErrorDisplay.Alert,
        onData: suspend (T) -> Unit,
    ): Job {
        // The same contract `execute` has: an Inline failure puts a retry button on screen, and
        // the button has to re-run the call that failed. Without this a screen loaded by `observe`
        // — which is every offline-first screen — shows a retry that does nothing.
        var retry: (() -> Unit)? = null
        if (errorDisplay == ErrorDisplay.Inline) {
            retry = {
                observe(
                    flow = flow,
                    whileSubscribed = whileSubscribed,
                    loading = loading,
                    scope = scope,
                    onError = onError,
                    alertId = alertId,
                    errorDisplay = errorDisplay,
                    onData = onData,
                )
            }
            pendingRetries[alertId] = retry
        }

        // Removed by identity, so a later call that has since claimed this id keeps its own.
        fun forgetRetry() {
            retry?.let { pendingRetries.remove(alertId, it) }
        }

        return scope.launch {
            val loadingOwed = AtomicBoolean(true)
            fun clearLoading() {
                if (loadingOwed.compareAndSet(true, false)) loading(false)
            }

            try {
                loading(true)
                val source = if (whileSubscribed) flow().whileStateIsCollected() else flow()
                source
                    .onEach { outcome ->
                        when (outcome) {
                            is Outcome.Success -> runCatching { onData(outcome.data) }
                                .onSuccess { forgetRetry() }
                                .onFailure { handleError(it, onError, alertId, errorDisplay) }

                            is Outcome.Failure -> handleError(outcome.error, onError, alertId, errorDisplay)
                        }

                        clearLoading()
                    }
                    .catch { handleError(it, onError, alertId, errorDisplay) }
                    .collect()
            } finally {
                clearLoading()
            }
        }
    }

    /**
     * Collects the receiver only while [state] has a subscriber, with a grace period.
     *
     * `observe {}` otherwise collects for the ViewModel's whole life, backgrounded included. That
     * is right for DataStore, which costs nothing when nothing changes, and wrong for a socket, a
     * location stream or a polling flow — those keep working, and draining the battery, behind a
     * screen nobody is looking at.
     *
     * The subscriber count is [uiState]'s own: `state` is `uiState.asStateFlow()`, a view that
     * shares its accounting, so collecting the state the screen renders is what keeps the source
     * alive. Gating on anything else — a `stateIn` of the source, say — counts the collector this
     * function creates, which lives as long as the ViewModel and so never drops.
     *
     * The grace period is why this is not simply "stop when the count hits zero": a rotation
     * destroys and recreates the collector within milliseconds, and tearing a subscription down
     * and back up across it is both wasteful and visible.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> Flow<T>.whileStateIsCollected(): Flow<T> {
        val source = this
        return uiState.subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .transformLatest { subscribed ->
                // Only the drop waits: `transformLatest` cancels this block if a subscriber
                // returns inside the grace period, so the source is never actually stopped.
                if (!subscribed) delay(SUBSCRIPTION_GRACE_MILLIS)
                emit(subscribed)
            }
            // Again, after the delay: a collector returning inside the grace period makes
            // `transformLatest` cancel the pending `false` and re-emit `true`, and without this
            // `flatMapLatest` would treat that as a new value and restart the source — which is
            // the exact teardown the grace period exists to avoid.
            .distinctUntilChanged()
            .flatMapLatest { subscribed -> if (subscribed) source else emptyFlow() }
    }

    private suspend fun handleError(
        throwable: Throwable,
        onError: suspend (DomainError) -> Boolean,
        alertId: String,
        errorDisplay: ErrorDisplay,
    ) {
        if (throwable is CancellationException) throw throwable

        val message = if (throwable is DomainError) {
            logger.d(throwable = throwable) { "Caught domain error" }
            if (onError(throwable)) return

            // A server outage is transient and not the user's problem to solve, so in Alert mode it
            // gets a snackbar rather than a dialog. Inline has nowhere quieter to put it.
            if (throwable is ServerError && errorDisplay == ErrorDisplay.Alert) {
                showToast(R.string.core_error_server_unavailable.toUiText())
                return
            }
            commonErrorMessage(throwable) ?: R.string.core_error_unexpected.toUiText()
        } else {
            logger.w(throwable = throwable) { "Unhandled exception" }
            R.string.core_error_unexpected.toUiText()
        }

        when (errorDisplay) {
            ErrorDisplay.Alert -> showErrorAlert(id = alertId, message = message)

            ErrorDisplay.Inline -> uiState.setContent(
                ContentState.Error(id = alertId, message = message),
            )

            ErrorDisplay.Silent -> Unit
        }
    }

    protected fun showUnexpectedErrorAlert(alertId: String = ALERT_ID_ERROR) {
        showErrorAlert(id = alertId, message = R.string.core_error_unexpected.toUiText())
    }

    /** Wording for the I/O failures every screen describes the same way. */
    protected fun commonErrorMessage(error: DomainError): UiText? = when (error) {
        is ServerError -> R.string.core_error_server_unavailable.toUiText()
        is NetworkError -> R.string.core_error_no_connection.toUiText()
        else -> null
    }

    // The error title lives here, not in AlertState's defaults, so an ordinary confirmation dialog
    // is not labelled "something went wrong".
    private fun showErrorAlert(id: String, message: UiText) {
        uiState.setAlert(
            id = id,
            title = R.string.core_alert_error_title.toUiText(),
            message = message,
        )
    }
}

/** Where a failure is shown. */
enum class ErrorDisplay {

    /** A dialog over the screen. Right for a call the user triggered on a screen already drawn. */
    Alert,

    /**
     * A retryable message in place of the content. Right for the call that loads a screen, where a
     * dialog would leave nothing behind it.
     */
    Inline,

    /** Nowhere — the caller handles it, or the failure genuinely does not matter. */
    Silent,
}

/** [BaseViewModel.saved] with no [SavedStateHandle] to back it: an ordinary mutable field. */
private class FieldProperty<T>(private var value: T) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
