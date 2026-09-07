package com.example.androidproject1.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.exception.DomainException
import com.example.androidproject1.core.domain.exception.NetworkErrorException
import com.example.androidproject1.core.domain.exception.ServerErrorException
import com.example.androidproject1.core.ui.AppString
import com.example.androidproject1.core.ui.CommonEvent
import com.example.androidproject1.core.ui.CommonUiCommand
import com.example.androidproject1.core.ui.Event
import com.example.androidproject1.core.ui.state.ModalLoadingState
import com.example.androidproject1.core.ui.state.UiState
import com.example.androidproject1.core.ui.state.clearAlert
import com.example.androidproject1.core.ui.state.isLoading
import com.example.androidproject1.core.ui.state.setAlert
import com.example.androidproject1.core.ui.toText
import com.example.androidproject1.service.core.ui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base class for every ViewModel in the app.
 *
 * The contract is **State / Event / Direction**:
 * - `State` is the data the screen renders, wrapped in a [UiState] envelope.
 * - `E` is what the user did, sent in via [onUiEvent].
 * - `Direction` is a one-off navigation intent, observed by the screen's destination function.
 *
 * Subclasses should normally not write try/catch or touch a loading flag — [domainCall] handles
 * loading, error dialogs and cancellation for them.
 *
 * @param initialState the state the screen renders immediately. Pass `null` only for a screen that
 * genuinely cannot render until something is loaded — [Screen][com.example.androidproject1.core.ui.component.Screen]
 * draws nothing while `data` is `null`, so a `null` initial state also starts the loading overlay.
 */
abstract class BaseViewModel<State, E : Event, Direction>(
    initialState: State?,
    protected val logger: Logger,
) : ViewModel() {

    companion object {

        const val ALERT_ID_DOMAIN_ERROR = "domain_error"
    }

    protected val uiState = MutableStateFlow(
        UiState<State?>(
            data = initialState,
            // Having no state to render is itself the thing being waited for.
            loading = if (initialState == null) ModalLoadingState() else null,
        ),
    )
    val state: StateFlow<UiState<State?>> = uiState.asStateFlow()

    // Buffered channels rather than a MutableSharedFlow: a shared flow with no replay and no buffer
    // silently drops emissions made while nothing is collecting, which loses a navigation issued
    // while the screen is below STARTED. receiveAsFlow() is single-consumer, which is exactly right
    // for a one-shot event — each is delivered once, to whichever collector is current.
    private val directionChannel = Channel<Direction>(Channel.BUFFERED)
    val direction: Flow<Direction> = directionChannel.receiveAsFlow()

    private val commonUiCommandChannel = Channel<CommonUiCommand>(Channel.BUFFERED)
    val commonUiCommand: Flow<CommonUiCommand> = commonUiCommandChannel.receiveAsFlow()

    /**
     * Number of [domainCall]s currently in flight. Counted rather than flagged so that the overlay
     * from a long call is not dismissed by a short one finishing first.
     */
    private val activeLoadingCount = AtomicInteger(0)

    /** Handles an event raised by this screen. */
    open fun onUiEvent(event: E) = Unit

    /**
     * Handles a framework-level event. The default clears the alert; override to react to a
     * specific alert's buttons, and call `super` for the ones you don't handle.
     */
    open fun onCommonEvent(event: CommonEvent) {
        when (event) {
            is CommonEvent.AlertDialogAction -> uiState.clearAlert()
        }
    }

    protected fun navigate(direction: Direction) {
        if (directionChannel.trySend(direction).isFailure) {
            logger.w { "Dropped direction $direction — channel full or closed" }
        }
    }

    protected fun sendCommand(command: CommonUiCommand) {
        if (commonUiCommandChannel.trySend(command).isFailure) {
            logger.w { "Dropped command $command — channel full or closed" }
        }
    }

    protected fun showToast(message: AppString) = sendCommand(CommonUiCommand.ShowToast(message))

    /**
     * Reflects an in-flight [domainCall] in the modal loading overlay. Nested and overlapping calls
     * are reference-counted, so the overlay stays up until the last one finishes.
     */
    protected open fun setLoading(active: Boolean) {
        val count = if (active) {
            activeLoadingCount.incrementAndGet()
        } else {
            activeLoadingCount.updateAndGet { (it - 1).coerceAtLeast(0) }
        }
        uiState.isLoading = count > 0
    }

    /**
     * Runs a one-shot domain [action], showing a loading overlay while it runs and converting any
     * [DataResult.Error] into an alert.
     *
     * @param loading how to reflect the in-flight state; defaults to the modal loading overlay.
     * Pass `{}` when the screen renders its own inline loading.
     * @param errorHandler return `true` to claim an error and suppress the default alert.
     */
    protected fun <T> domainCall(
        loading: (Boolean) -> Unit = { setLoading(it) },
        scope: CoroutineScope = viewModelScope,
        errorHandler: suspend (DomainException) -> Boolean = { false },
        errorDialogId: String = ALERT_ID_DOMAIN_ERROR,
        action: suspend () -> DataResult<T>,
        handleData: suspend (T) -> Unit,
    ): Job = scope.launch {
        loading(true)
        try {
            when (val result = action()) {
                is DataResult.Success -> runCatching { handleData(result.data) }
                    .onFailure { handleException(it, errorHandler, errorDialogId) }

                is DataResult.Error -> handleException(result.exception, errorHandler, errorDialogId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            handleException(e, errorHandler, errorDialogId)
        } finally {
            loading(false)
        }
    }

    /**
     * Collects a domain [flow], clearing the loading state after the first emission and converting
     * any [DataResult.Error] into an alert.
     *
     * The loading state is cleared on the first emission whether it succeeded or failed. The
     * `finally` below is not enough on its own: this overload exists for long-lived flows, which
     * never complete, so an error emission would otherwise leave the overlay up for good.
     */
    protected fun <T> domainCall(
        flow: suspend () -> Flow<DataResult<T>>,
        loading: (Boolean) -> Unit = { setLoading(it) },
        scope: CoroutineScope = viewModelScope,
        errorHandler: suspend (DomainException) -> Boolean = { false },
        errorDialogId: String = ALERT_ID_DOMAIN_ERROR,
        handleData: suspend (T) -> Unit,
    ): Job = scope.launch {
        try {
            loading(true)
            var firstEmission = true
            flow()
                .onEach { result ->
                    when (result) {
                        is DataResult.Success -> runCatching { handleData(result.data) }
                            .onFailure { handleException(it, errorHandler, errorDialogId) }

                        is DataResult.Error ->
                            handleException(result.exception, errorHandler, errorDialogId)
                    }

                    if (firstEmission) {
                        firstEmission = false
                        loading(false)
                    }
                }
                .catch { handleException(it, errorHandler, errorDialogId) }
                .collect()
        } finally {
            loading(false)
        }
    }

    private suspend fun handleException(
        throwable: Throwable,
        errorHandler: suspend (DomainException) -> Boolean,
        errorDialogId: String,
    ) {
        if (throwable is CancellationException) throw throwable

        if (throwable is DomainException) {
            logger.d(throwable = throwable) { "Caught domain exception" }
            if (errorHandler(throwable) || handleIoDomainException(throwable, errorDialogId)) {
                return
            }
            setErrorAlert(
                id = errorDialogId,
                message = throwable.displayMessage?.toText()
                    ?: R.string.core_general_error_try_again_description.toText(),
            )
        } else {
            logger.w(throwable = throwable) { "Unhandled exception" }
            showInternalErrorDialog(errorDialogId)
        }
    }

    protected fun showInternalErrorDialog(errorDialogId: String = ALERT_ID_DOMAIN_ERROR) {
        setErrorAlert(
            id = errorDialogId,
            message = R.string.core_general_error_try_again_description.toText(),
        )
    }

    /** Handles the I/O errors every screen treats the same way. Returns `true` if consumed. */
    protected fun handleIoDomainException(
        exception: DomainException,
        errorDialogId: String,
    ): Boolean {
        when (exception) {
            is ServerErrorException -> showToast(R.string.core_error_server.toText())

            is NetworkErrorException -> setErrorAlert(
                id = errorDialogId,
                message = R.string.core_error_network_message.toText(),
            )

            else -> return false
        }
        return true
    }

    /**
     * Shows an alert titled as an error. The error title lives here rather than in
     * [AlertState][com.example.androidproject1.core.ui.state.AlertState]'s defaults so that an
     * ordinary confirmation dialog is not labelled "something went wrong".
     */
    private fun setErrorAlert(id: String, message: AppString) {
        uiState.setAlert(
            id = id,
            title = R.string.core_general_error_title.toText(),
            message = message,
        )
    }
}
