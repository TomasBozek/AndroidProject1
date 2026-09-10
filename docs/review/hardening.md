# Hardening sweep
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

One agent, six lenses (runtime, security, build/CI, UX and accessibility, components, seams). Ranked by the agent's value score; `niche` is 1 = every product app has it, 5 = niche.

## build-r8-mapping-artifact · Keep the R8 mapping file with every release

**effort S · value 5 · niche 1 · touches release APK: False · new dependency: none**

**What.** In .github/workflows/build.yml's release job, upload app/build/outputs/mapping/prodRelease/mapping.txt (and seeds/usage) as a workflow artifact and attach mapping.txt to the GitHub release beside the APK, named with the tag (e.g. mapping-v1.2.0.txt). Add a one-line note to the Crash reporting section of CLAUDE.md: a vendor adapter uploads the same file; until then this is the only way to read a release stack trace.

**Why standard.** A minified build's stack traces are unreadable without the mapping of that exact build; Android docs (Shrink, obfuscate, and optimize your app → Decode an obfuscated stack trace) and every crash vendor's setup guide make retaining mapping.txt per release the first step. With D8 keeping vendors out of the repo, the GitHub release is the only place the file can live.

Evidence of the gap: `/.github/workflows/build.yml:146-150 uploads only app/build/outputs/apk/prod/release/*.apk`; `/.github/workflows/build.yml:158-166 `gh release upload/create` attach the APK only`; `grep -rn 'mapping' .github build-logic → 0 hits`

## ux-textfield-keyboard-api · AppTextField: IME action, autofill content type, password toggle, multi-line

**effort M · value 5 · niche 1 · touches release APK: True · new dependency: none**

**What.** Extend core/ui/.../component/AppTextField.kt with: `imeAction: ImeAction?` + `onImeAction: (() -> Unit)?` (Next on email, Done on password that submits); `contentType: ContentType?` applied via Modifier.semantics { contentType = … } so password managers fill email/password; a trailing visibility toggle when `password = true` (tagged `<stem>_passwordToggle`, contentDescription from a core_ string); and `minLines/maxLines` instead of the hardcoded `singleLine = true`. Then use Next/Done/autofill on LoginScreen and SignUp, and add the goldens.

**Why standard.** Compose text-field guidance (developer.android.com → Text fields, Autofill in Compose) and Material 3 password-field spec: a login form advances with Next, submits with Done, is autofillable, and lets the user reveal the password. Every product login has these; without them TalkBack users and password-manager users hit friction on the very first screen. (ui.10, the field announcing its own label, is a separate open item and stays separate.)

Evidence of the gap: `/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt:56-70 signature has no imeAction/keyboardActions/contentType/maxLines parameter`; `/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt:104 `singleLine = true` hardcoded`; `/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt:106-109 PasswordVisualTransformation with no toggle`; `/feature/auth/presentation/src/main/kotlin/com/example/androidproject1/feature/auth/presentation/login/LoginScreen.kt:40-57 two fields, no Next/Done, no autofill`; `grep -rIl 'autofill\|ImeAction' core feature --include=*.kt (excluding worktrees) → no autofill hits; ImeAction only in worktree copies`

## ux-forms-scroll-under-keyboard · Forms scroll under the keyboard

**effort S · value 4 · niche 1 · touches release APK: True · new dependency: none**

**What.** Give AppScaffold a `scrollable: Boolean = false` (verticalScroll on its Column with rememberScrollState; keep the non-scrolling default for lists) and turn it on for LoginScreen, SignUp, Profile edit and the onboarding pages. Add one Roborazzi variant or a ScreenTest assertion that the submit button is reachable at fontScale 1.5 with a 360×740 viewport.

**Why standard.** Android's edge-to-edge + adjustResize guidance: content that can be covered by the IME must be scrollable, otherwise the field or the button under the keyboard is unreachable on a small phone or with large fonts. Now in Android and the Compose Forms samples wrap every form in verticalScroll.

Evidence of the gap: `/feature/auth/presentation/src/main/kotlin/com/example/androidproject1/feature/auth/presentation/login/LoginScreen.kt:31-33 Column(fillMaxSize, CenterVertically) with no verticalScroll`; `/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppScaffold.kt:38-56 Column with fillMaxSize and safeDrawingPadding, no scroll option`; `/app/src/main/AndroidManifest.xml:25 adjustResize is set, so the IME shrinks the window and clips the centred column`

## runtime-connectivity-monitor · Connectivity seam and an offline surface

**effort M · value 4 · niche 1 · touches release APK: True · new dependency: none**

**What.** Add `ConnectivityMonitor { val isOnline: Flow<Boolean> }` to :service:core:domain with a `ConnectivityManager`-backed `AndroidConnectivityMonitor` in :service:core:data (registerDefaultNetworkCallback, NET_CAPABILITY_VALIDATED, callbackFlow, distinctUntilChanged) and a fake in testFixtures. Bind it in coreModule. Surface it once, centrally: `Screen()` (or AppNavHost) collects it and shows a persistent 'No connection' banner/snackbar via the existing UiCommand path, so no feature writes offline UI. Let the dev-menu OfflineSwitch also flip the fake so the banner is demoable on the dev flavor.

**Why standard.** Now in Android's `NetworkMonitor` + `ConnectivityManagerNetworkMonitor` with the 'You are not connected to the internet' snackbar in NiaApp is the reference shape; Google's offline-first guide expects apps to expose connectivity state rather than infer it from the next failed call.

Evidence of the gap: `grep -rIl ConnectivityManager --include=*.kt (excluding worktrees) → 0`; `/service/core/domain/src/main/kotlin/com/example/androidproject1/core/domain/error/DomainErrors.kt:4 NetworkError exists only as a post-failure error`; `/feature/devmenu/presentation/src/main/kotlin/com/example/androidproject1/feature/devmenu/presentation/OfflineSwitch.kt fakes the MockEngine only; nothing observes real connectivity`; `/gradle/libs.versions.toml has no connectivity-related entry (none needed; framework API)`

## ux-pull-to-refresh · AppPullToRefresh and a Refresh event on the catalog lists

**effort M · value 4 · niche 1 · touches release APK: True · new dependency: none**

**What.** Add `AppPullToRefresh(isRefreshing, onRefresh, content)` to :core:ui via create_component.py, wrapping Material 3's stable PullToRefreshBox with the theme's colours and a @ComponentPreview; add it to GalleryCatalog. Add `Refresh` to CategoriesEvent/ProductsEvent, driven by `execute(loading = {})` so the indicator, not the overlay, shows; tag the list `<stem>_List` and extend one Maestro flow with a swipe-down.

**Why standard.** Material 3 pull-to-refresh is the platform gesture for 'get me the latest' on any list backed by a network; every catalog/feed app ships it and Now in Android's feed does too. The template's catalog is offline-first with a stale-snackbar but gives the user no way to ask for fresh data.

Evidence of the gap: `grep -rIn -i 'refresh' feature/*/presentation/src/main --include=*Event.kt → 0 hits`; `/core/ui component inventory (grep '^fun App' core/ui/.../component/*.kt) has no pull-to-refresh component`; `/feature/catalog/presentation/.../products/ProductsViewModel.kt:57-60 keepStaleContent shows a snackbar but offers no retry gesture`

## build-dependency-graph-alerts · Submit the Gradle dependency graph so Dependabot alerts cover the build

**effort S · value 4 · niche 1 · touches release APK: False · new dependency: none**

**What.** In build.yml's build job (push to main only), set `dependency-graph: generate-and-submit` on gradle/actions/setup-gradle@v4 and enable Dependabot alerts in the repo settings. No Renovate involvement (D26 stays parked): this is vulnerability alerts on what is already resolved, not version bumps. Optionally add `dependency-review-action` on pull_request, which reads the same graph.

**Why standard.** GitHub's supply-chain guidance and the gradle/actions README recommend dependency submission as the free way to get CVE alerts for Gradle builds; NiA and most Google samples run it. It is free on every plan, needs no plugin in the build and no file in the repo — cheaper than verification-metadata.xml, which is the other standard answer and costs maintenance on every bump.

Evidence of the gap: `/.github/workflows/build.yml:67 `uses: gradle/actions/setup-gradle@v4` with no dependency-graph option`; `ls gradle/verification-metadata.xml → absent`; `grep -rn 'dependency-review\|dependency-graph' .github → 0`

## security-network-security-config · network_security_config with debug-overrides

**effort S · value 3 · niche 1 · touches release APK: True · new dependency: none**

**What.** Add app/src/main/res/xml/network_security_config.xml referenced from the manifest's <application android:networkSecurityConfig>: base-config cleartextTrafficPermitted=false (states the API 28+ default explicitly), and <debug-overrides> trusting user-installed CAs so a debug build can be inspected with Charles/Proxyman/mitmproxy. Leave a commented <pin-set> stub pointing at Q8 so pinning lands in the same file later. Lint's already-enabled security checks will validate it.

**Why standard.** Android docs (Network security configuration) name this file as the place for cleartext policy, trust anchors and pins; the debug-overrides block is what every product team uses to debug traffic on a debug build without weakening release. Certificate pinning itself stays behind Q8.

Evidence of the gap: `grep -rIl network_security_config --include=*.xml --include=*.kt (excluding worktrees) → 0`; `/app/src/main/AndroidManifest.xml:9-17 <application> has no android:networkSecurityConfig`; `ls app/src/main/res/xml → backup_rules.xml, data_extraction_rules.xml only`

## runtime-strictmode-debug · StrictMode in debug builds

**effort S · value 3 · niche 1 · touches release APK: False · new dependency: none**

**What.** In App.onCreate under `if (BuildConfig.DEBUG)`, set ThreadPolicy (detectDiskReads/Writes, detectNetwork, penaltyLog) and VmPolicy (detectLeakedClosableObjects, detectActivityLeaks, detectLeakedSqlLiteObjects, penaltyLog). Log-only penalties so it informs rather than blocks; a doc comment saying a main-thread DataStore read is the thing it is there to catch. Keep it beside LeakCanary as the second debug-only watchdog.

**Why standard.** Android docs (StrictMode) and the App Startup / performance guidance recommend enabling StrictMode in debug to catch main-thread I/O and leaked resources early; it is a few lines that has no cost in release and catches the class of bug a 498-test JVM suite never sees.

Evidence of the gap: `/app/src/main/kotlin/com/example/androidproject1/App.kt:15-30 onCreate wires Koin and the channel only`; `grep -rIl StrictMode --include=*.kt (excluding worktrees) → 0`

## runtime-app-lifecycle-seam · Foreground/background seam over ProcessLifecycleOwner, with a session re-check on return

**effort M · value 3 · niche 2 · touches release APK: True · new dependency: androidx.lifecycle:lifecycle-process (version from the existing lifecycle ref)**

**What.** Add `AppLifecycle { val isForeground: StateFlow<Boolean> }` in :service:core:domain, an implementation in :service:core:data built on ProcessLifecycleOwner (androidx.lifecycle:lifecycle-process, already in the BOM family), bound in coreModule, with a fake in testFixtures. First consumers, so it is not a seam without a use: MainViewModel re-validates the session when the app returns to the foreground after N minutes (the session-timeout policy, a constant in ApplicationModule), and Analytics receives an `app_foreground` event. Later consumers: pausing polling, refreshing the catalog on return.

**Why standard.** Google's app-architecture guidance and the lifecycle library's ProcessLifecycleOwner are the standard way to know the app-level foreground state; a session-timeout-on-return policy is how banking/retail apps decide when a stale token must be re-checked. Having it as a seam keeps features from registering their own ActivityLifecycleCallbacks.

Evidence of the gap: `grep -rIl ProcessLifecycleOwner --include=*.kt --include=*.toml (excluding worktrees) → 0`; `/gradle/libs.versions.toml has no lifecycle-process entry (grep 'lifecycle-process' → 0)`; `/app/src/main/kotlin/com/example/androidproject1/MainViewModel.kt:82-84 observes session, onboarding and theme; nothing reacts to foregrounding`; `grep -rn 'expires\|expiry\|lastActive' feature/auth/domain service/network → 0`

## services-feature-flags-seam · FeatureFlags seam with local defaults and debug-menu overrides

**effort M · value 3 · niche 2 · touches release APK: True · new dependency: none**

**What.** Add `FeatureFlags { fun isEnabled(flag: Flag): Boolean; fun observe(flag): Flow<Boolean> }` and an enum/sealed `Flag(key, default)` in :service:core:domain; `LocalFeatureFlags` in :service:core:data reads DataStore overrides over compile-time defaults; the dev menu gets a switch per flag (already the pattern for OfflineSwitch). A vendor's remote config becomes one adapter bound in :app, exactly as ErrorTracker/Analytics. This promotes the backlog line 'Logger backend and remote config' with a concrete vendor-free shape; use the first real flag (e.g. the onboarding tour) as the proof.

**Why standard.** Feature flags are how product apps ship dark and roll back without a release; the seam-plus-logging-default pattern is the one this repo already uses for crash reporting and analytics (D8). Firebase Remote Config, LaunchDarkly and Statsig all fit behind the same two methods.

Evidence of the gap: `grep -rIl 'FeatureFlag\|RemoteConfig' --include=*.kt (excluding worktrees) → 0`; `grep -rIn 'BuildConfig.DEBUG\|DebugMenu.ENABLED' feature app core (main sources) → 13 hits: build-time constants are the only toggle mechanism`; `/docs/PLAN-DETAIL.md backlog line 'Logger backend and remote config' has no shape`

## services-push-seam · Push seam that routes a payload through the existing deep-link parser

**effort M · value 3 · niche 2 · touches release APK: True · new dependency: none**

**What.** Add `PushTokenSource { suspend fun currentToken(): String? }` and `PushMessageRouter` (data payload → deep-link URI string → DeepLinks.parse) to :service:core:domain, with `NoOpPushTokenSource` bound by default and a `PushPayloadTest`. `TestNotification` in :app (already there for the debug menu) becomes the one place that builds a notification from a routed payload with a PendingIntent carrying the `<applicationId>://` URI, so the tap lands in MainActivity.onNewIntent like any deep link. An FCM adapter later is the service class plus one binding in :app.

**Why standard.** Push is month-one for most product apps, and Android's notification guidance is that a tap navigates via an intent the app already understands. Keeping the vendor out (D8/D27) but owning payload→destination in a tested JVM function is the same seam pattern as ErrorTracker, and it reuses DeepLinks.kt rather than growing a second router.

Evidence of the gap: `grep -rIl 'Push\|Firebase\|Messaging' --include=*.kt (excluding worktrees) → 0 main-source hits`; `/app/src/main/kotlin/com/example/androidproject1/DeepLinks.kt (40 lines) parses product links only from the launch intent`; `/app/src/main/kotlin/com/example/androidproject1/debug/TestNotification.kt posts a fixed test notification with no routed content`

