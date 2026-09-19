package com.example.androidproject1.feature.devmenu.presentation.devmenu

import com.example.androidproject1.feature.auth.domain.AuthService
import com.example.androidproject1.feature.devmenu.presentation.ApiSwitch
import com.example.androidproject1.feature.devmenu.presentation.BuildInfo
import com.example.androidproject1.feature.devmenu.presentation.DevMenuJump
import com.example.androidproject1.feature.devmenu.presentation.NotificationTester
import com.example.androidproject1.feature.devmenu.presentation.OfflineSwitch
import com.example.androidproject1.feature.devmenu.presentation.R
import com.example.androidproject1.service.core.domain.ErrorTracker
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

/**
 * @param buildInfo bound by `:app`, which is the only module allowed to read `BuildConfig`.
 * @param offlineSwitch the fixture engine's flag, or `OfflineSwitch.Unsupported` in a build that
 * talks to a real server.
 * @param apiSwitch the same engine's other flag (D81), or `ApiSwitch.Unsupported`.
 * @param jumps built by `AppNavHost` and passed through the destination with `parametersOf`,
 * the way a route key is, so the state carries it from `init`.
 */
class DevMenuViewModel(
    logger: Logger,
    buildInfo: BuildInfo,
    private val offlineSwitch: OfflineSwitch,
    private val apiSwitch: ApiSwitch,
    private val notificationTester: NotificationTester,
    private val authService: AuthService,
    private val errorTracker: ErrorTracker,
    jumps: List<DevMenuJump>,
) : BaseViewModel<DevMenuState, DevMenuEvent, DevMenuNavigation>(
    initialState = DevMenuState(
        build = buildInfo,
        session = null,
        offlineSupported = offlineSwitch.isSupported,
        offline = offlineSwitch.isOffline(),
        realApiSupported = apiSwitch.isSupported,
        realApiKeyPresent = apiSwitch.isKeyPresent,
        realApi = apiSwitch.isRealApi(),
        jumps = jumps,
    ),
    logger = logger.withTag("DevMenuViewModel"),
) {

    init {
        observe(
            flow = { authService.observeSession() },
        ) { session ->
            updateData { copy(session = session?.email) }
        }
    }

    override fun onUiEvent(event: DevMenuEvent) {
        when (event) {
            is DevMenuEvent.OfflineToggled -> {
                offlineSwitch.setOffline(event.offline)
                // Read back rather than assumed: the flag lives outside this process, so a write
                // that did not take should not leave the screen claiming it did.
                updateData { copy(offline = offlineSwitch.isOffline()) }
            }

            is DevMenuEvent.RealApiToggled -> {
                apiSwitch.setRealApi(event.realApi)
                updateData { copy(realApi = apiSwitch.isRealApi()) }
            }

            DevMenuEvent.CrashClicked -> {
                // A handled exception, not a thrown one: what is being checked is that reports
                // reach the tracker this build binds, and killing the app proves nothing extra.
                errorTracker.recordNonFatal(
                    DebugMenuTestReport(),
                    "Recorded from the debug menu",
                )
                showToast(R.string.dev_menu_crash_recorded.toUiText())
            }

            DevMenuEvent.NotificationClicked -> {
                // A toast either way: a notification that was never posted and one that was
                // posted and missed look identical from here, and only one is a bug.
                val posted = notificationTester.post()
                showToast(
                    if (posted) {
                        R.string.dev_menu_notification_posted.toUiText()
                    } else {
                        R.string.dev_menu_notification_blocked.toUiText()
                    },
                )
            }

            is DevMenuEvent.JumpClicked -> navigate(DevMenuNavigation.Jump(event.jump))

            DevMenuEvent.PlaygroundClicked -> navigate(DevMenuNavigation.Playground)
            DevMenuEvent.NavigateUpClicked -> navigate(DevMenuNavigation.NavigateUp)
        }
    }
}

/** Its own type so the report is recognisable in whatever console the build reports to. */
class DebugMenuTestReport : RuntimeException("Test report from the debug menu")
