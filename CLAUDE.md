# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Work in progress:** [docs/PLAN.md](docs/PLAN.md) is the board — what is open, what needs a
decision, and how the work splits into tracks that can run in parallel worktrees — and
[docs/PLAN-DETAIL.md](docs/PLAN-DETAIL.md) holds each open item's Why / Done / Verify, the
working rules and the reasoning behind decisions. Read the board before picking up work and keep
both current as items land; each fact lives in exactly one of them. The API table and the gotchas
a cold start needs are in this file, under *API you build on* and *Known constraints*.

## Project

Multi-module Android app (Kotlin + Jetpack Compose), base package `com.example.androidproject1`. It is a
**template**: the structure and conventions matter more than the five sample features. Architecture is
modelled on a layered Clean/MVI setup — single activity, type-safe Compose navigation, Koin DI.

- `minSdk = 29`, `targetSdk = compileSdk = 37`, Java 17 — all in `build-logic`'s `ProjectConfig`
- AGP `9.4.0`, Kotlin `2.4.20`, Gradle `9.6`, Compose BOM `2026.08.00`. Renovate keeps them
  current — `renovate.json` groups androidx, kotlin, koin and AGP, and skips pre-releases.
- Dependencies come from `gradle/libs.versions.toml` — never hardcode a version in a module build file.
  Compose artifacts come from the BOM without an explicit version.
- **A new dependency has to earn its place.** Prefer Google, JetBrains and androidx; then a
  library with a large company behind it and broad adoption. Anything else — a single maintainer,
  a small vendor — needs a line in the Decisions table of [docs/PLAN.md](docs/PLAN.md), and if it
  ships in the release APK, a first-party alternative that was actually tried and found wanting.
  Build- and test-only tools are judged more leniently because they never reach production, but
  they still get the line. The standing exceptions are **Koin**, which is 23 files deep and whose
  replacement is a plan of its own, and **Coil**, because no first-party image loader exists.
  Applies to what is already here as much as to what is added: an unused dependency is removed,
  not kept for symmetry.
- AGP 9 applies Kotlin itself; there is no `kotlin-android` plugin. New plugins must be declared in the
  root `build.gradle.kts` with `apply false` before a module can `alias(...)` them.
- **A module build file is a `plugins` block and its project dependencies. Nothing else.** SDK levels,
  Java target, lint, the namespace and every shared library dependency live in the convention plugins
  in `build-logic/` — see below.

## Convention plugins

`build-logic/` is an included build (`includeBuild` in `settings.gradle.kts`'s `pluginManagement`,
not a module) holding the `convention.*` plugins every module applies. The rule is **library
dependencies live in the plugin; project dependencies stay in the module**, so a typical build file
is six lines:

```kotlin
plugins { alias(libs.plugins.convention.feature.presentation) }

dependencies {
    api(projects.core.ui)
    api(projects.feature.auth.domain)
}
```

| Plugin | Applies |
|---|---|
| `convention.android.library` | `com.android.library`, the SDK levels, Java target, the shared `lint.xml`, the derived namespace. No dependencies |
| `convention.android.library.compose` | the above plus the Compose compiler plugin, `buildFeatures.compose`, the BOM and the `compose-core` bundle |
| `convention.kotlin.jvm` | `org.jetbrains.kotlin.jvm`, Java target, coroutines, JUnit and coroutines-test. Every `domain` module |
| `convention.feature.data` | android library plus coroutines, and what testing one takes: the `testing` bundle, `testFixtures(:service:core:domain)`, MockEngine, and Robolectric plus `androidx-test-core` — a DataStore wants a `Context` exactly as a DAO does |
| `convention.feature.di` | android library plus the Koin BOM and bundle |
| `convention.feature.presentation` | the compose library plus serialization, Koin, Navigation 3's ViewModel decorator, lifecycle, the `testing` bundle and `testFixtures(:service:core:ui)` |
| `convention.android.application` | `:app`: `com.android.application`, the app identity, R8 on release, `lint.checkDependencies` |

The **namespace is derived** from the project path and `basePackage` in `gradle.properties`:
`:feature:auth:presentation` becomes `<base>.feature.auth.presentation`, `:service:core:ui` becomes
`<base>.service.core.ui`, `:app` becomes `<base>`. A module that sets `namespace` itself keeps it —
nothing does today. `init_project.py` therefore rewrites one property rather than one line per
module, and `applicationId` comes from the same property.

`ProjectConfig` in `build-logic/src/main/kotlin/` holds `minSdk`, `compileSdk`, `targetSdk`, the
Java version and the app's fallback version. Changing `minSdk` is one edit.

**A release is a tag, not an edit.** `git tag v1.2.0 && git push origin v1.2.0` — `versionName`
becomes `1.2.0` (the tag without its `v`) and `versionCode` becomes `git rev-list --count HEAD`,
which only goes up and needs nothing stored anywhere. Any build that is not on a `v*` tag is
1 / `"1.0"`: a number that moved on every commit would make two debug APKs indistinguishable from
the outside. The release job then attaches the signed `prodRelease` APK to a GitHub release whose
notes are generated from the commits since the previous tag — which is the other reason a commit
is titled `<id> <title>`. Its checkout is `fetch-depth: 0`, because `rev-list --count` on the
default shallow clone is 1.

`service/` modules apply the same plugins, so `export_service.py` copies `build-logic/` along with
them and prints the `includeBuild` line. `doctor.py` fails if a module build file sets `compileSdk`,
`minSdk`, `targetSdk`, `compileOptions` or a `lint` block; `resourcePrefix` and `testFixtures` are
exempt, because `:service:core:ui` genuinely owns both.

## Module structure

Modules are registered in `settings.gradle.kts` through `includeServiceModule` / `includeCoreModule` /
`includeFeatureModule` helpers, which fail at settings time if a module's directory or build file is
missing. Type-safe project accessors are enabled, so dependencies read `api(projects.core.ui)`.

There are three top-level groups, and the split between the first two is the important one:

```
:service:core:domain  result/Outcome, error/DomainError hierarchy, Logger (interface only)
:service:core:data    BaseRepository, DataStoreProvider, AndroidLogger
:service:core:ui      viewmodel/BaseViewModel, state/UiState, component/Screen(), event/UiEvent,
                      event/UiCommand, text/UiText, format/Formats, util/CollectEffect,
                      error strings

:core:ui              this app's Compose theme + the @ScreenPreview/@ComponentPreview helpers
:core:di              initKoin() + coreModule — the single Koin registration point

:app                  single activity, AppNavHost, MainViewModel, SessionState,
                      TopLevelDestination (the tabs), Application

:feature:auth:{domain,data,presentation,di}       full stack; owns the session
:feature:cart:{domain,data,presentation,di}       full stack
:feature:catalog:{domain,data,presentation,di}    full stack; three screens, one with args; a tab
:feature:gallery:{presentation,di}                screen only
:feature:home:{presentation,di}                   screen only; a tab
:feature:profile:{domain,data,presentation,di}    full stack
:feature:settings:{presentation,di}               screen only; a tab; reads :feature:auth:domain
:feature:template:{domain,data,presentation,di}   what the generators clone
```

:service:network        HttpClientFactory (engine is a parameter), HttpErrorMapper,
                       TokenStore/TokenRefresher + SingleFlightTokenRefresher

`service/` holds **reusable** modules — the architecture, with no knowledge of this app's features,
theme or DI graph. Reuse is by directory copy: drop `service/core` into a new project, add the three
`includeServiceModule` lines, then create that project's own `core/` and `feature/`. So keep it
self-contained — never reference `:core:*`, `:feature:*` or `:app`, and never read `R` from elsewhere.

Package names under `service/` stay `com.example.androidproject1.core.*`; only the Android
**namespaces** move to `...service.core.*`, because two modules cannot share a namespace and
`:core:ui` keeps `...core.ui`. Hence the `...service.core.ui.R` imports in `:service:core:ui`.

`:core:ui` re-exports `:service:core:ui` with `api(...)`, so a feature's `presentation` module still
depends on nothing but `projects.core.ui` and gets the theme, the architecture and Navigation 3. `Previews.kt`
lives here rather than in `service` because it references `AppTheme`.

Two rules keep `service/` portable, and both are load-bearing:

- **`:service:core:domain` stays free of `android.*`.** The `Logger` *interface* lives there;
  `AndroidLogger` lives in `:service:core:data`. It is a plain Kotlin/JVM module, so the compiler
  enforces this and `doctor.py` is only the second line of defence. Don't make it an Android
  library to get around a framework import — move the class into `:service:core:data` instead.
- **`:service:core:ui` sets `resourcePrefix = "core_"`,** so every string it ships is `core_*` and
  cannot silently collide with a consuming app's. New resources there must carry the prefix.
  `:core:ui` sets `app_` for the same reason. Lint's `ResourceName` check is an error, so an
  unprefixed resource in either stops the build.

Test fixtures live with the type they fake: `FakeLogger` and `TestDispatchers` in `testFixtures` of
`:service:core:domain` alongside `Logger` and `DispatcherProvider`, `MainDispatcherRule` in
`:service:core:ui`'s, `FakeAuthService` in `:feature:auth:domain`'s. `:service:core:ui` re-exports the
first two with `testFixturesApi`, so a screen test still needs one `testFixtures(...)` line. Never
write a second copy of a fake — move the first.

The service modules have JVM unit tests (`src/test/kotlin`) covering `Outcome`, `BaseRepository` and
`BaseViewModel`. They need no Robolectric — `R.string.x` is only an `Int` and `UiText` defers
resolution — so keep it that way and don't reach for the framework to test logic. The exception is
`:service:core:ui`'s `permission/` package, which is a wrapper over `PackageManager` and the
activity-result contract: there is no logic there to test without a shadowed framework, and
`convention.android.library.compose` already puts Robolectric on that module's test classpath.

Layer dependency directions:

| Layer | Depends on |
|---|---|
| `domain` | `api(projects.service.core.domain)` only. A **Kotlin/JVM** module — no manifest, no AAR, no lint pass, and `android.*` is not on its classpath |
| `data` | `service:core:data` + own `domain`; holds `DefaultXRepository` and both halves of the data source |
| `presentation` | `api(projects.core.ui)` + own `domain` |
| `di` | `api(...)` of all of the above |

`data` is the port to the outside world, split into two packages: `repository` holds
`DefaultXRepository`, which adapts what the feature needs to the domain, and `source` holds the
`XDataSource` interface *and* its `DefaultXDataSource` implementation. The interface sits beside its
implementation because nothing above `data` names it — what the rest of the app depends on is the
repository interface in `domain`. A `DefaultXRepository` therefore imports `XDataSource` and never
`DefaultXDataSource`; `doctor.py` fails if it does. Naming is rigid at every layer: `Foo`
(interface) ↔ `DefaultFoo` (implementation).

A feature's `presentation` module must never depend on another feature's `presentation`. Cross-feature
navigation is passed in as a lambda and wired in `AppNavHost` — the sample app has none left to point
at, because the three cross-feature jumps it had are now tabs. Depending on another feature's `domain`
is fine — `:feature:settings:presentation` does exactly that for `AuthService`.

`:feature:template` is compiled but unused; it is the source the generator scripts clone. Keep it working.

## Design system

`:core:ui` holds the design system, imported from the KSD system in Claude Design. It is three
layers, and the split is what makes a re-brand one file rather than a sweep:

| Layer | Where | Rule |
|---|---|---|
| 1 · core | `theme/Ramp.kt`, `Scale` in `theme/Spacing.kt` | Raw ramps and the 4 dp dimension scale. **`internal`** — no screen can name a step |
| 2 · semantic | `theme/{Color,Type,Shape,Elevation,Motion,Density,Spacing}.kt` | Roles. The only layer that differs light ⇄ dark |
| 3 · component | `component/*.kt` | Binds a role to an element and its states |

Read layer 2 through `AppTheme`: `AppTheme.colors`, `.typography`, `.shapes`, `.elevation`,
`.motion`, `.density`, `.spacing`, `.icons` — the last being three icon sizes and only three,
`sm` 18 in a row or a field, `md` 24 for a control, `lg` 32 where the icon is the thing being
looked at. A touch target is `AppTheme.density.minTouchTarget` and a different question. `toColorScheme()` / `toTypography()` / `toShapes()` also populate
Material's own theme, so `service/core/ui`'s `Screen()` — which cannot depend on `:core:ui` without
losing its portability — picks up the system for free.

**A feature composes components; it never draws.** A `presentation` module imports from
`core.ui.component` and `core.ui.theme` and from nothing else in Compose's widget set:

- no `androidx.compose.material3.*` — every widget a screen needs has an `App*` counterpart
- no `.dp` or `.sp` literal — ask `AppTheme.spacing` for a role, `AppTheme.typography` for a role
- no `Color(...)` and no `MaterialTheme.colorScheme` — ask `AppTheme.colors` for a role
- no `NumberFormat`, `DecimalFormat` or `DateTimeFormatter` — ask `LocalFormats.current` for a
  role: `money`, `moneyShort`, `weight`, `quantity`, `percent`, `time`, `date`, `duration`

`AppScaffold` is the screen shell: base surface, system insets, an optional `AppTopBar`. A screen
does not call `safeDrawingPadding()` itself; that is what got screens padded twice.

If a screen needs something the set does not have, **add it to `:core:ui` with
`create_component.py`** and give it a `@ComponentPreview` — do not draw it in the feature. Every
component is browsable in the running app under Settings → Components, which is
`:feature:gallery`; add the new one to `GalleryCatalog.kt` in the same change.

### Testing a screen

Two tests per screen, and they answer different questions:

| | Asks | Runs as |
|---|---|---|
| `XViewModelTest` | what the state becomes, and what navigation is emitted | plain JVM test |
| `XScreenTest` | what is on screen, and what a tap does | Robolectric unit test |

The second is the half a ViewModel test cannot reach. `LoginScreenTest` is the pattern: it renders
the stateless screen with a fixed state, collects the events it emits, and finds everything by
`testTag` — never by text, because copy gets reworded and translated. It runs under Robolectric as
an ordinary unit test, so `./gradlew test` covers it and CI needs no emulator.

Two things it has to say out loud, both of which cost a comment in the file:

- **`@Config(sdk = …)` is pinned.** Robolectric ships an SDK image per API level and has none for
  this project's `targetSdk`; the test asks for the newest it does have.
- **A compound component is tagged on its group.** A caller's `modifier` goes to the outermost
  element, so `Modifier.testTag("login_emailField")` on an `AppTextField` tags the label, input and
  supporting line together. Assertions about the group use the tag directly; a test that types
  reaches the input inside it with `hasSetTextAction() and hasAnyAncestor(hasTestTag(…))`.

`convention.feature.presentation` carries the dependencies, so a new screen's test needs no
build-file edit.

### Test identifiers

One id serves the screen reader, the test and the design registry, so there is one to keep in sync
rather than three:

- **A screen** is `<Domain><Purpose>Screen` and carries its own name — `AppScaffold(screenId =
  "SettingsScreen")`. `AppScaffold` publishes tags as resource ids, so `assertVisible: id:
  "SettingsScreen"` is the universal check that a flow is where it meant to be.
- **An element** is `<screenStem>_<element>`: the screen's name in camelCase without `Screen`, an
  underscore, then the element from a closed vocabulary — `Button`, `Field`, `Switch`, `Checkbox`,
  `List`, `Item`, `Tile`, `Key`, `Dialog`, `Sheet`, `Tab`, `Badge`, `Value`. So
  `Modifier.testTag("settings_permissionsButton")`.
- **Find by id, never by text.** Copy changes and gets translated; a test that finds a button by
  its label fails on a wording fix. Text is an assertion about content, not a way to reach a thing.
- An icon with no visible label carries the same string as its `contentDescription` — which is why
  accessibility here is a by-product of being testable rather than separate work.

Elevation is not `Modifier.shadow`. A pressable surface uses `Modifier.keySurface(color, edge,
shape, pressed)`: a hard bottom edge in the family's own colour that shortens on press, so the key
travels 3 dp. A blurred shadow makes it a floating card instead of a pressed one.

## Screen structure (the unit of work)

Every screen is eight files — six in **a package of its own**, plus its two tests in the matching
test package. The package is named after the screen, flat lowercase, and a feature gets one even
when it has a single screen, so every feature reads the same and a second screen never forces a
move:

```
feature/catalog/presentation/src/main/kotlin/<base>/feature/catalog/presentation/
    categories/     CategoriesDestination.kt, CategoriesScreen.kt, …
    products/       ProductsDestination.kt, …
    productdetail/
    productpicker/
    component/      one file per composable that is not a screen, each with a @ComponentPreview
```

| File | Role |
|---|---|
| `XDestination.kt` | `@Serializable` route key (a `NavKey`) + `EntryProviderScope<NavKey>.xDestination()`; gets the VM and wires `Screen()`, passing `onNavigation` |
| `XScreen.kt` | Stateless `XScreen(state, onEvent)` + its `@ScreenPreview` composables — **and nothing else**. Any other composable goes to the feature's `component/` |
| `XState.kt` | `data class XState(...)` with a `companion object { val PREVIEW }` |
| `XEvent.kt` | `sealed interface XEvent : UiEvent` — what the user did |
| `XNavigation.kt` | `sealed interface XNavigation` — one-off navigation intents |
| `XViewModel.kt` | `BaseViewModel<XState, XEvent, XNavigation>` |
| `XViewModelTest.kt` | in `src/test/kotlin`; uses `MainDispatcherRule` + `FakeLogger`, both of which arrive with `testFixtures(projects.service.core.ui)` — the convention plugin already adds it |
| `XScreenTest.kt` | in `src/test/kotlin`; renders the stateless screen with a fixed state, finds by `testTag` and asserts the event a tap emits. Robolectric, so `./gradlew test` covers it |

`XState.PREVIEW` is required — it is the preview fixture and usually the value passed as
`initialState`.

Two `doctor.py` checks hold the shape: a screen's directory holds that screen's six files and
nothing else, and a screen file holds the screen and its previews and no other composable. So a
composable a screen grows has exactly one home — **`presentation/component/`, one file each with
a `@ComponentPreview`**, written with `create_component.py --feature <name>`. One `component/`
per feature rather than one per screen: a screen-private composable and a feature-shared one
would otherwise need two homes, and the day one is used from a second screen it would have to
move. A component a *second feature* wants goes to `:core:ui`, also through
`create_component.py` — never copied.

## MVI conventions

- ViewModels expose `state: StateFlow<UiState<State?>>`; the UI sends events via `onUiEvent(event)`.
- **`BaseViewModel` takes `initialState` first.** Pass the state the screen renders straight away and
  it starts with no overlay; pass `null` only when the screen cannot render until something loads,
  which also starts the overlay. Never write `init { uiState.update { ... loading = null } }` — one
  forgotten line there strands a screen behind a permanent spinner.
- `UiState(data, loading, alert)` is an envelope. **Loading overlays and alert dialogs are rendered
  centrally by `Screen()`** — never reimplement them in a feature screen.
- `Screen()` is the only place that calls `collectAsStateWithLifecycle` and the only interpreter of
  `UiCommand` (toast, snackbar, back, browser, app settings). A feature screen only ever receives a
  non-null state. Every command is plain data: a snackbar's action button comes back as
  `SystemEvent.SnackbarAction(id)`, handled in `onSystemEvent` beside the alert results, not as a
  lambda the command carried.
- **Use `execute {}` (one-shot) and `observe(flow = …) {}` (flows) rather than try/catch.** They drive
  the loading state, convert `Outcome.Failure` into an alert, and rethrow cancellation. Pass
  `loading = {}` when a screen renders its own inline loading. Overlapping calls are reference-counted.
- `navigation` and `command` are buffered channels, not shared flows, so a one-shot emitted while
  nothing collects arrives on resume rather than being dropped. Single-consumer by design.
- `AlertState.title` has no default; `BaseViewModel`'s error paths supply the error title themselves,
  so an ordinary confirmation dialog is not labelled "something went wrong".
- Alert results arrive at `onSystemEvent` as `SystemEvent.AlertResult.*` tagged with the alert's
  `id`; delegate anything you don't handle to `super`. See `SettingsViewModel` for the confirm-then-act
  pattern.
- `MainViewModel` is the single owner of session state and the only thing that switches flows. It is
  a **plain `ViewModel`**, not a `BaseViewModel` — it owns no screen — and exposes
  `sessionState: StateFlow<SessionState>` (`Unknown` / `SignedIn` / `SignedOut`). `MainActivity`
  keeps the system splash screen up while it is `Unknown` and composes the nav host once it is not.
  Screens change the session and let it react — do not navigate between the auth and main flows
  directly.
- Strings reachable from a ViewModel are `UiText` (`R.string.x.toUiText()`), so no Context is needed;
  a quantity string is `R.plurals.x.toPluralUiText(count, count)` — named differently on purpose, so
  `toUiText(count)` cannot silently resolve a string resource as a plural. The first `count` picks
  the form, the second fills the `%d`;
  strings used only in a composable use `stringResource(...)`. Each feature's `presentation` module owns
  its `res/values/strings.xml` — no hardcoded literals. See `HomeState.greeting`.
- **A feature's resource is named after the screen that shows it** — `login_title`,
  `product_detail_add_to_cart` — or after the feature when two of its screens share one
  (`catalog_stale`). `doctor.py` checks it, reading the prefixes off the `*Screen.kt` files rather
  than a list, so a generated screen needs no edit. Not AGP's `resourcePrefix`, which allows one
  prefix per module: `:feature:auth:presentation` legitimately ships both `login_` and `sign_up_`,
  and a path-derived prefix could never match `user_profile_title` in a directory called
  `userprofile`.
- **A screen without a `Scaffold` pads itself with `.safeDrawingPadding()`.** The activity is edge to
  edge and `Screen()` applies no insets, so `Scaffold`-based screens are not padded twice.

## API you build on (do not reinvent)

Plan 2's reference table, kept here because the plan holds only open work.

| Thing | Where | Note |
|---|---|---|
| `execute {}` / `observe(flow = …) {}` | `service/core/ui/.../BaseViewModel.kt` | Never try/catch in a ViewModel. `loadingMessage` words the overlay and survives overlapping calls |
| `ErrorDisplay.{Alert,Inline,Silent}` | same | `Inline` remembers the failed call per content id; the retry re-runs that one and forgets it on success |
| `ContentState.{Error,Empty}` | `service/core/ui/.../state/ContentState.kt` | Rendered by `Screen()` instead of content; a screen with two of them gives each its own `id` |
| `AlertPayload`, `SystemEvent.AlertResult` | `state/AlertState.kt`, `event/SystemEvent.kt` | Typed confirm-then-act; see `SettingsViewModel` |
| `UiCommand` | `event/UiCommand.kt` | Toast, snackbar (action comes back as `SystemEvent.SnackbarAction(id)`), back, close, browser, app settings |
| `Formats` / `LocalFormats` | `service/core/ui/.../format/` | `money`, `moneyShort`, `weight`, `quantity`, `percent`, `time`, `date`, `duration`, all from one `Locale`. A screen never formats a number itself; `doctor.py` fails on a `NumberFormat` in a feature. Tabular figures stay `TextRole.Numeric` |
| `DispatcherProvider` / `DefaultDispatcherProvider` | `service/core/domain/coroutines/` | Switch at the data source, not the repository |
| `Aead` / `AesGcmAead` / `KeystoreAead` | `service/core/domain/crypto/`, `service/core/data/crypto/` | `EncryptedDataStoreProvider` stores the session with it; the logic is in `AesGcmAead` and JVM-tested, the Keystore fetch is fifteen lines |
| `ErrorTracker` / `TrackingLogger` | `service/core/domain/`, `service/core/data/` | See Crash reporting above |
| `Analytics` / `LocalAnalytics` / `ScreenViewEffect` | `service/core/domain/`, `service/core/ui/analytics/` | `AppScaffold` sends the screen view; see Analytics below |
| `HttpClientFactory` retry policy | `service/network/.../HttpClientFactory.kt` | Up to `NetworkConfig.retries` more tries on a 5xx or a transport failure, exponential backoff with jitter. **Idempotent methods only** — never a POST; do not add a retry loop in a data source |
| `SessionState` | `app/SessionState.kt` | `Unknown` / `SignedIn` / `SignedOut`, owned by `MainViewModel`; nothing else switches flows |
| `appModules(isDebug)` / `coreModule(isDebug)` | `core/di/Koin.kt` | The one module list; `initKoin` starts it, `KoinGraphTest` verifies it. WARN-and-above logging in release |
| `MainDispatcherRule`, `FakeLogger`, `TestDispatchers`, `FakeAuthService` | `testFixtures` of `:service:core:ui`, `:service:core:domain` (both middle two), `:feature:auth:domain` | One `testFixtures(projects.service.core.ui)` line brings the first three; the convention plugin adds it, and `convention.feature.data` takes `:service:core:domain`'s directly |
| `ProjectConfig`, `convention.*` | `build-logic/src/main/kotlin/` | SDK levels, Java target, flavors. One edit each; the version comes from the tag |

## DI (Koin)

Each feature has `object XModule { val module = module { ... } }` in its `di` module, using
`viewModelOf(::XViewModel)` and `singleOf(::DefaultX) bind X::class`. Modules are registered in
`core/di/.../Koin.kt`, and `:core:di` must have an `api(projects.feature.x.di)` dependency for that to
compile. Both edits are made automatically by `create_feature.py`.

## Crash reporting

`ErrorTracker` in `:service:core:domain` is the seam; `LoggingErrorTracker` is bound by default and
reports to the log and nowhere else. **The repo carries no vendor SDK and no vendor config file** —
one would make every project that starts from this template either use that vendor or unpick it
first.

Nothing calls the tracker directly. `TrackingLogger` decorates whatever `Logger` is bound and
forwards anything logged with a `Throwable`, so the paths that already report a problem —
`BaseViewModel.handleError`, `BaseRepository`'s failure paths — report to it with no signature
changing anywhere. A `w` with no exception stays a note to whoever is reading logcat; reporting
those would bury the real ones.

To swap in a vendor, add the dependency to `:app` and override the one binding there:

```kotlin
// app/src/main/kotlin/.../CrashlyticsErrorTracker.kt
class CrashlyticsErrorTracker : ErrorTracker {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordNonFatal(throwable: Throwable, message: String?) {
        message?.let(crashlytics::log)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) = crashlytics.log(message)

    override fun setUser(id: String?) = crashlytics.setUserId(id.orEmpty())
}

// in :app's own Koin module, which is loaded after coreModule and so wins
single<ErrorTracker> { CrashlyticsErrorTracker() }
```

`setUser` takes an opaque id — never an email, never a name, because a crash report is not the
place to put either. **Nothing calls it yet**: the sample `AuthService` exposes only a boolean, so
there is no id to pass. Call it from wherever the session becomes known once the session carries
one, and call it with `null` on sign-out so the next person's reports are not attributed to the
last one.

## Analytics

`Analytics` in `:service:core:domain` is the seam — `screen(id)` and `event(name, params)` —
and `LoggingAnalytics` is bound by default, reporting to the log and nowhere else. As with crash
reporting, **the repo carries no vendor SDK and no vendor config file**.

**The screen view is automatic.** `AppScaffold` calls `ScreenViewEffect(screenId)`, so a screen
that passes `screenId` is measured and there is no per-screen call to forget; `doctor.py` fails on
a screen that composes a scaffold without one. It is a `LaunchedEffect`, so a recomposition does
not count a second view — read the note on `ScreenViewEffect` before changing that, because
`ScreenViewTest` cannot observe the difference.

Everything else is a deliberate call at the point it happens. Read it from composition with
`LocalAnalytics.current`, which defaults to `Analytics.NoOp` so a preview or a Robolectric test
needs no graph behind it. Keep `params` few and low-cardinality: a parameter that can take a user
id or a free-text field turns one event into millions and is unusable in every vendor's console.

To swap in a vendor, add the dependency to `:app` and override the one binding there, exactly as
with `ErrorTracker`:

```kotlin
// in :app's own Koin module, which is loaded after coreModule and so wins
single<Analytics> { FirebaseAnalyticsAdapter(androidContext()) }
```

**One wiring line is still open.** Nothing calls `ProvideAnalytics` yet, so the bound `Analytics`
does not reach composition and `LocalAnalytics` stays `NoOp` at runtime. Wrap the `NavDisplay` in
`AppNavHost` the way `ProvideNavResultStore` already is — `ProvideAnalytics(koinInject()) { … }` —
and every screen starts reporting with no further change.

## Recipes

Start here for any new code. **Do not create these files by hand.** The scripts perform the five
registrations a manual copy silently skips — `settings.gradle.kts`, `:core:di`'s build file, `Koin.kt`,
`AppNavHost.kt` and the module tree above — and `doctor.py` fails on the ones you forget. If a script cannot do what you
need, extend it rather than working around it; `scripts/test_scripts.py` covers them.

| I need | Command |
|---|---|
| to turn this template into a real project | `python3 scripts/init_project.py --package com.acme.app --name "My App"` |
| a new feature, full stack | `python3 scripts/create_feature.py userProfile` |
| a new screen-only feature | `python3 scripts/create_feature.py userProfile --layers presentation,di` |
| another screen in an existing feature | `python3 scripts/create_screen.py userprofile UserProfileDetail` |
| a screen that takes route arguments | `python3 scripts/create_screen.py userprofile UserProfileDetail --with-args 'userId:String'` |
| a shared Compose component | `python3 scripts/create_component.py PrimaryButton` |
| a component only one feature needs | `python3 scripts/create_component.py ProductCard --feature catalog` |
| a data source, optionally with its repository | `python3 scripts/create_datasource.py userprofile LocalUserProfile --repository` |
| to undo a generated feature | `python3 scripts/delete_feature.py userProfile` |
| to check the conventions still hold | `python3 scripts/doctor.py` |
| to make a missed registration fail at commit time | `python3 scripts/install_hooks.py` |
| to reuse `service/` in another project | `python3 scripts/export_service.py --to <dir> --package <pkg> --sync-versions` |

Pass the name in any case — `userProfile`, `user-profile`, `UserProfile` all work. What comes out is fixed:
directory and package flat lowercase (`userprofile`), classes PascalCase (`UserProfileViewModel`), functions
camelCase (`userProfileDestination`), string resources snake_case (`user_profile_title`). Add
`--dry-run` to any generator to see the plan before it writes.

### A new feature

```bash
python3 scripts/create_feature.py userProfile
```

Clones all five layers and registers them everywhere. Then, in order:

1. Replace the placeholder `val title: String` / `val counter: Int` in `UserProfileState`, its
   `PREVIEW` and the three states in `UserProfileStatePreviews` with the real ones. Keep all three:
   empty is where layouts collapse and long text is where they overflow, and both are goldens.
   `PREVIEW` is not optional — it is the preview fixture and normally the `initialState`.
2. Write `UserProfileScreen.kt`. Strings used only in the composable go through `stringResource(...)`;
   strings a ViewModel needs go into the state as `UiText` (`R.string.x.toUiText()`). Both live in
   the feature's own `res/values/strings.xml`, prefixed `user_profile_`.
3. Add cases to `UserProfileEvent`, handle them in `UserProfileViewModel.onUiEvent`, and emit
   `UserProfileNavigation` to move on. The `CollectEffect` block in `UserProfileDestination.kt` is
   generated empty — that is where a navigation intent turns into a back-stack call.

Use `--layers presentation,di` when the screen has no data of its own; the generated build files drop
the dependencies on layers you skipped. You can add a layer later with
`--layers domain --force` (`--force` only overwrites the layers you name).

### A new screen in an existing feature

```bash
python3 scripts/create_screen.py userprofile UserProfileDetail
python3 scripts/create_screen.py userprofile UserProfileDetail --sub detail
python3 scripts/create_screen.py userprofile UserProfileDetail --with-args 'userId:String,tab:Int'
```

`--with-args` clones a second screen template, `feature/template`'s `TemplateArgs*` set, which
demonstrates the argument-carrying route: an `@Serializable data class` route key, a ViewModel
taking that key as a constructor parameter, a destination handing it over with
`koinViewModel { parametersOf(key) }`, and a plain JVM test. Bare `--with-args` gives one
`id: String`. Supported types are `String`, `Int`, `Long`, `Boolean`, `Float`, `Double`.

It also adds the key to `KoinGraphTest`'s `injectedParameters`: Koin's `verify()` cannot see a
`parametersOf` argument and would call the route key a missing definition. That is the sixth
registration, and `doctor.py` fails if it is missing.

Do not hand-convert a `data object` route into a `data class` — that is what produced a screen
loading from a `LaunchedEffect` instead of from the key it was handed.

Note that `create_feature.py` deliberately skips the `TemplateArgs*` files: a new feature starts
with one screen, and copying the second would leave an unregistered destination behind.

Writes the six-file unit into `presentation/userprofiledetail/` — a directory named after the
screen — plus its two tests, plus the one feature-local component the generated screen composes
in `presentation/component/`. Adds `user_profile_detail_*` strings to the feature's
`strings.xml`, registers the ViewModel in the feature's Koin module and the destination in
`AppNavHost.kt`. Then follow steps 1–3 above. `--sub` names that directory instead of deriving
it, which is what a long screen name wants: `--sub search` rather than `catalogsearch`.


### A new data source

```bash
python3 scripts/create_datasource.py userprofile LocalUserProfile --repository
```

Writes the interface and its `Default…` implementation into `data.source`, the repository pair into
`domain` + `data.repository`, and the Koin bindings. Then:

1. Replace the placeholder `observeValue` / `setValue` with the real operations — in the interface and
   the implementation, and in the repository pair if you generated one.
2. The generated implementation is DataStore-backed so that it compiles and runs; swap it for the real
   source (network client, DAO) and keep the interface where it is. `DefaultXRepository` depends on
   `XDataSource` and never on `DefaultXDataSource` — that is what the swap depends on.
3. Repository methods return `Outcome` and go through `execute` / `observe`. Pass
   `retries` when the collector outlives a failure, as `DefaultAuthRepository.observeSession()` does.
4. Call it from a ViewModel with `execute {}`, never try/catch.

### Changing a Room schema

A module with a database applies `convention.android.room` beside `convention.feature.data`, which
commits the exported schemas under the module's `schemas/` and puts them on the unit test's assets.

**A `version` bump ships its migration and its test in the same commit.** Bump `@Database`'s
`version`, add the `Migration` to the database's own `MIGRATIONS` array — the builder in the
feature's Koin module and `XDatabaseMigrationTest` both read that one list — and commit the schema
JSON Room exports on the next compile. Nothing else to write: the test walks every committed
version up to the compiled one, so it fails on the bump alone and passes once the migration exists.

Never reach for `fallbackToDestructiveMigration`. It compiles, the tests go quiet, and what it
means is that the next update empties the user's cart.

### Navigating to another feature

Not scripted, and deliberately: a `presentation` module must never depend on another feature's
`presentation` (`doctor.py` fails if it does). Add a lambda parameter to the destination function and
wire it in `AppNavHost.kt`, where the parameter is the only thing either module knows about the other:

```kotlin
userProfileDestination(
    backStack = backStack,
    navigateToSettings = { backStack.add(SettingsDestination) },
)
```

Switching between the auth and main flows is different again: change the session and let
`MainViewModel` react. Do not replace the back stack from a screen.

### Getting a value back from another screen

"Pick something on screen B, hand it back to screen A." Navigation 3 has no `previousBackStackEntry`
and a back stack of plain keys has nowhere to hang a value, so this is
`service/core/ui/.../navigation/NavResultStore.kt` and never a `SavedStateHandle`.

- The requester registers a callback: `NavResultEffect<String>(KEY) { id -> onEvent(...) }`.
  It fires **once** per result — the value is consumed, so coming back later does not replay a
  selection the user already made.
- The responder gets a setter: `val setNavResult = rememberNavResultSender(key)`, calls
  `setNavResult(value)` and then pops. The key arrives as a route argument, so one picker can
  serve several callers and knows nothing about any of them.
- `ProvideNavResultStore` wraps the `NavDisplay` in `AppNavHost` — above the entries, because the
  result has to outlive the responder being popped. It is a `rememberSaveable`, so it survives
  process death with the back stack.
- A value must be something a `Bundle` can hold: a primitive, `String`, `Parcelable` or
  `Serializable`, the same constraint a route argument has.

### Permissions

`service/core/ui/.../permission/` owns the whole story; no feature writes permission code of its own.

- **`rememberPermissionRequest(vararg permissions)`** wraps `rememberLauncherForActivityResult` and
  returns a `PermissionRequest` with a `status` and a `request()`. `status` is re-read on every
  resume, because the user can change a permission in system settings and come back.
- **`PermissionStatus`** is `NotRequested` / `Granted` / `PartiallyGranted` / `Denied(canAskAgain)`.
  Four cases, not a boolean, because each ungranted one needs different UI — and once
  `canAskAgain` is false the system dialog never appears again, so the only way forward is
  `Context.openAppSettings()`.
- **`PermissionGate(permission, rationale = …) { content }`** composes `content` only while the
  permission is held. Prefer it to checking a boolean: there is no ungranted case for a caller to
  forget, because `content` simply does not run.
- **`rememberDeclaredPermissions()`** lists what the merged manifest asks for, read from
  `PackageManager` — so a permission a library contributed shows up too. Also re-read on resume.

**Permission state is read in composition, not in a ViewModel.** It lives outside the app and
changes while the app is backgrounded, so there is nothing to observe — only something to re-read.
A screen that needs it in its state hands it over as an event, the way `SettingsPermissionsScreen`
does with `PermissionsRead`. A ViewModel-readable version is item 7.13, parked until a ViewModel has
to make a decision on one.

Ask for a permission where the user came looking for it, not on first launch. `POST_NOTIFICATIONS`
is requested from the Permissions screen, and its channel is created in `App.onCreate`.

### The tabs

`TopLevelDestination` in `:app` is the bottom bar: one entry per tab, each naming a feature's route
key, its label (a string in `:app`'s own `strings.xml`) and its icon. `NavigationSuiteScaffold`
renders it as a bar on a phone and a rail once there is width for one. The auth flow has no tabs, so
`AppNavHost` composes the display without the scaffold there.

**The tabs do not each own a list.** The back stack *is* their concatenation, in the order the tabs
were last visited, and a tab's key is the only thing that starts a segment — so `currentTab` is the
last tab key on the stack, `selectTab` moves that tab's segment to the end, and push and pop are
unchanged because they act on the segment that happens to be last. One flat list is what makes
per-tab history survive process death with no custom `Saver`: it is the list `rememberNavBackStack`
already saves. Back out of a tab's root and the previously visited tab is underneath it, which is
what Android expects.

Adding a tab is one entry in `TopLevelDestination`, one `xEntries()` block in `AppNavHost.kt`, one
label string, and its `--graph` name in `scripts/_common.py`'s `NAV_GRAPHS`. A tab root carries no up
arrow: the bar is what leaves it.

### Removing things

`delete_feature.py` removes a whole feature and all five registrations. There is no script for a single
screen: delete its directory, its `user_profile_detail_*` strings, the `viewModelOf(::XViewModel)` line
and the two `AppNavHost.kt` lines. Run `doctor.py` afterwards — it catches every one of those if you
miss it.

### Changing what gets generated

`feature/template` **is** the template — `create_feature.py` and `create_screen.py` clone it verbatim
and only rewrite names. To change the shape of every future feature or screen, edit `feature/template`,
not the scripts. It is included in `settings.gradle.kts` precisely so `./gradlew build` keeps it
compiling; keep it that way.

### Before you call the work done

```bash
python3 scripts/doctor.py && ./gradlew ktlintCheck && ./gradlew build
```

Add `python3 scripts/test_scripts.py` if you touched anything under `scripts/`.

## Scaffolding scripts

Prefer these over copying files by hand; they also perform the registration steps that are easy to
forget. Every one of them supports `--dry-run`. `scripts/_common.py` holds the shared naming rules,
paths and the idempotent file-editing helpers — put anything used by two scripts there.

**Do not add a script.** This is an Android project: the Kotlin is the work, and `scripts/` exists
only to make the repetitive parts of it fast. The ten below are the set. Change one when something
else forces you to — a convention moved, a generated file's shape changed, a new `--graph` name —
and treat that as part of the change that caused it. Anything that would be a new tool goes to the
backlog in [docs/PLAN-DETAIL.md](docs/PLAN-DETAIL.md) instead.
`scripts/README.md` documents them at the point of use, and every script's `--help` carries worked
examples.

```bash
python3 scripts/init_project.py --package com.acme.app --name "My App" [--dry-run]
```

Run once on a fresh clone, before writing anything of your own. Rewrites the base package in every
source file (both the dotted and slash-separated forms — `scripts/test_scripts.py` holds the latter),
moves the package directory in all 35 source sets, and renames `rootProject.name`, the Android theme,
the launcher label, `basePackage` in `gradle.properties` and `_common.py:BASE_PACKAGE`. Every module's
namespace and the `applicationId` follow from that one property, so there is nothing per-module left
to rename. Refuses to run on a dirty tree without `--force`.

```bash
python3 scripts/create_feature.py userProfile --layers domain,presentation,di
```

Clones `feature/template`, rewrites names, and performs **all five** registrations: the modules in
`settings.gradle.kts`, `api(projects.feature.userprofile.di)` in `core/di/build.gradle.kts`, the Koin
module entry in `Koin.kt`, the destination in `AppNavHost.kt`, and the line in the module tree above. Dependencies on layers you did not
generate are stripped from the generated build files. `--graph` picks the entry block in
`AppNavHost.kt`: `main` for the signed-in flow, `home` / `catalog` / `settings` for one tab of it,
`auth` for the signed-out flow, `none` to skip the registration.

```bash
python3 scripts/create_screen.py userprofile UserProfileList --sub overview
```

Clones the six-file screen unit into a directory of its own inside an existing feature — named
after the screen unless `--sub` names it — along with its two tests and the one feature-local
component the generated screen composes. Renames the template's `template_*` strings to
`user_profile_list_*` and merges them into the feature's `strings.xml`, registers the ViewModel in the
feature's Koin module and the destination in `AppNavHost.kt`. Every generated file gets an explicit
`import ...presentation.R`, because `R` lives in the module's namespace package and no generated
file sits in it any more.

```bash
python3 scripts/create_component.py PrimaryButton
python3 scripts/create_component.py ProductCard --feature catalog --state
```

Writes a Compose component with a `modifier` parameter and a `@ComponentPreview`. With no
`--feature` it lands in `:core:ui`, where every feature can reach it; `--feature` keeps it to one, in that
module's `component/` package.
`--state` adds an `@Immutable` `XState` data class with the `PREVIEW` fixture `doctor.py` requires.
A component needs no registration, which is why this script edits nothing outside the file it
writes. Templates live in the script rather than in `feature/template` — there is no component
there to clone, and adding a fake one would ship a placeholder component in the app.

```bash
python3 scripts/create_datasource.py userprofile LocalUserProfile --repository
```

Writes the `XDataSource` interface and `DefaultXDataSource` into `data.source`, and the Koin
`singleOf(...) bind ...::class` line that a hand-written copy forgets. `--repository [NAME]` also
generates `XRepository` in `domain` and `DefaultXRepository` in `data.repository`; the name defaults
to the data source's without its `Local`/`Remote`/`Cached`/`InMemory` qualifier, matching
`LocalAuthDataSource` ↔ `AuthRepository`.

```bash
python3 scripts/delete_feature.py userProfile
```

The inverse of `create_feature.py`: deletes `feature/<name>/` and undoes the same five registrations,
then greps for references it could not remove safely (a cross-feature navigation lambda, typically) and
prints them. It refuses to delete `feature/template` without `--force`.

```bash
python3 scripts/doctor.py
```

Greps for the conventions in this file that no compiler enforces: `service/` portability, the
Android-free domain layer, the `core_` resource prefix, the six-file screen unit and the directory
it lives in, a screen file holding nothing but the screen, `XState.PREVIEW`, an
`init` block that clears `loading`, cross-feature `presentation` dependencies, a repository importing a
data source implementation, module registration in `settings.gradle.kts`, ViewModel/Koin/AppNavHost
registration, the module tree above matching the `feature/` directories on disk, a module build file
repeating the shared Android configuration, hardcoded dependency coordinates, and every `id:` a
Maestro flow drives existing in the code as a `testTag` or a `screenId` — the reverse, a tag no flow
uses, is printed as a note rather than failed.
Exits non-zero, so it can gate CI; `--list` prints the checks. Since there is no detekt/ktlint here,
this is the only automated defence these rules have.

```bash
python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --package com.acme.myapp --sync-versions
```

Copies `service/` and `build-logic/` into another project, rewriting `com.example.androidproject1` in
sources, directory layout and namespaces, then prints the `includeBuild` and `includeServiceModule`
blocks to paste into the target's `settings.gradle.kts` and the `basePackage` line for its
`gradle.properties`. The module list is read off disk, so adding `service/network` needs no edit here.
`--sync-versions` resolves every `libs.*` accessor in the copied build files *and in the convention
plugins* (which is where most of the dependencies now are, spelled `libs.findBundle("compose-core")`)
— version refs and bundle members included — and merges those entries into the target's
`gradle/libs.versions.toml`, creating it if needed; an alias the target already defines differently is left alone and reported. Also supports
`--force` and `--modules`.

```bash
python3 scripts/install_hooks.py
```

Installs a `pre-commit` hook running `doctor.py` — quiet on success, and it prints the full report
and aborts the commit on failure. `test_scripts.py` runs too, but only when the commit touches
`scripts/`, since it takes ~20s. Hooks are not version controlled, so each clone runs this once;
`--uninstall` removes it, and it refuses to overwrite a hook it did not write.

The same workflows are exposed as Claude Code slash commands in `.claude/commands/`:
`/new-feature`, `/new-screen`, `/new-component`, `/new-datasource`, `/check`, `/rename-project`.
Each carries the follow-up steps, so the conventions arrive with the command rather than having to
be looked up. There is deliberately no accompanying skill — `CLAUDE.md` is already loaded for every
session in this project, so a skill restating it would be duplication to maintain, not context to
gain.

```bash
python3 scripts/test_scripts.py
```

Smoke tests for all of the above, on `unittest` so there is nothing to install. Each test copies the
repo into a temp directory and runs the scripts there as subprocesses; the delete-feature test asserts
the five registration files come back byte-identical. They check generated text, not that it compiles —
`./gradlew build` is still the real gate.

Renaming caveat: `create_feature.py` / `create_screen.py` use targeted replacements
(`feature.template`, `feature/template`, `Template`, `template` followed by an uppercase letter, and
`template_` followed by a resource name) rather than a blanket rename, so the word appearing in a
comment or a string survives. Preserve that if you edit them — see `scripts/_common.py:rewrite_source`
and `rewrite_resource_names`. `export_service.py` is the deliberate exception: moving the whole base
package is the point there.

## Commands

Run from the repo root with the Gradle wrapper.

```bash
./gradlew build
```

```bash
./gradlew :app:assembleDevDebug
```

```bash
./gradlew installDevDebug
```

Three flavors on one `environment` dimension — `dev`, `staging`, `prod` — so a build is
`devDebug`, `prodRelease` and so on, and `assembleDebug` alone no longer names a variant. `dev`
and `staging` carry an application-id suffix and their own launcher label, so all three install
side by side; the usual reason a tester cannot reproduce something is that they only have one of
them. `BuildConfig.BASE_URL` differs per flavor and a screen never writes a URL literal.

The flavors are defined once, in `ProjectConfig.Flavor` — adding a fourth is one enum entry. The
launcher label is composed from `appName` in `gradle.properties`, which `init_project.py` rewrites
alongside `basePackage`.

```bash
./gradlew test
```

```bash
./gradlew lint
```

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py
```

```bash
./gradlew koverHtmlReport
```

Coverage, and deliberately not a gate: there is no threshold, because a number that has to be met
gets met by tests written for the number. It is a signal — which module the tests avoid. CI
uploads it as an artifact. Previews and generated classes are filtered out; they say nothing about
where the tests are thin.

```bash
./gradlew assembleDevDebug -PcomposeMetrics
```

The Compose compiler's stability reports, under `build/compose-reports/<module>/`. Off by default
because they cost a compiler pass on every module; on when you want to know whether a state the
code calls `@Immutable` is one the compiler agrees about. `<module>-classes.txt` is the file to
read: every `XState` should say `stable class`, with no `runtime` or `unstable` member. The one
exception is `UiState`, which is generic — its stability is its type argument's, which is what
`Parameter(Data)` on its `<runtime stability>` line means.

What the compiler cannot work out for itself is stated in `build-logic/compose-stability.conf`:
read-only collections, and the domain models, whose modules are Kotlin/JVM and so never see the
Compose compiler at all — it treats every class from one as unstable rather than unknown. A line
there is a promise, so do not add one to quiet a report that is right. Compose plugin options are
not task inputs, so a report needs `--rerun-tasks` (or a clean) to be regenerated.

```bash
./gradlew ktlintCheck
```

`ktlintFormat` fixes what it can. The rule set lives in `.editorconfig`, not a second config file:
`intellij_idea` style rather than `ktlint_official`, with `class-signature`, `function-signature`
and `parameter-list-spacing` off (all three read a multi-line parameter list as if it were on one
line, so every constructor in the repo would be a violation) and `function-naming` off (a
`@Composable` is PascalCase and a test name is a backtick-quoted sentence).

**There is still no detekt.** Re-tested 2026-09-08 on 1.23.8, the current release: its embedded
Kotlin compiler rejects the JDK 25 the daemon is pinned to — it refuses `--jvm-target 25`, and once
that is pinned to 17 it fails on the JDK's version string instead. detekt 2.x is `2.0.0-alpha`.
Revisit when 2.x is stable.

Build a single module, e.g. `./gradlew :feature:auth:presentation:assembleDebug` or
`./gradlew :service:core:ui:assembleDebug`.

## Known constraints

- `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 25 (auto-provisioned via foojay).
  A machine without it and without network access will fail before configuring; lower `toolchainVersion`
  if that happens.
- The `compileSdk { version = release(37) }` and `optimization { enable = … }` block DSL is AGP-9
  only and will not work on AGP 8.x. It lives in `build-logic`, so there is one place to change.
- The convention plugins declare AGP and the Kotlin plugins as `compileOnly`, so they compile against
  the DSL but do not put it on the consuming build's classpath. That is why the root
  `build.gradle.kts` still needs `alias(libs.plugins.android.library) apply false` and friends —
  deleting those breaks every module with an unresolved plugin id.
- `configureAndroid` is written against `CommonExtension`'s property getters (`extension.lint.…`)
  rather than its `lint { }` block, because in AGP 9 the action forms are declared separately on
  `LibraryExtension` and `ApplicationExtension`, not on the interface they share.
- `observe`'s error is terminal — a `Flow` that has thrown can only be resubscribed, not
  resumed. A flow whose collector outlives the failure (session state, say) must pass `retries`, or one
  transient I/O error stops it emitting for as long as the collector lives. See
  `DefaultAuthRepository.observeSession()`.
- **`rememberNavBackStack` must be composed on the first frame.** It is a `rememberSaveable`, and
  one that first enters composition on a later frame gets nothing back from the restored state.
  Gating it on anything asynchronous — the session, a flag, a loaded config — throws the saved
  back stack away on every process death, silently and only on a real device. Remember it
  unconditionally (empty if need be) and gate the `NavDisplay` instead. `MainActivity` shows it.
- Koin's `verify()` cannot see a `parametersOf` argument, and `Module.mappings` is internal API,
  so the route keys a screen takes are listed by hand in `KoinGraphTest`'s `injectedParameters`.
  `create_screen.py --with-args` writes the line; `doctor.py` fails if it is missing.
- Robolectric ships no `AndroidKeyStore` provider, so `KeystoreAead` cannot run under it. That is
  why the class is split: everything worth getting wrong is in `AesGcmAead` and JVM-tested.
- Robolectric 4.16 reads JDK 25 bytecode; 4.14 did not. A tool that fails here with a class-file
  version error may only need its current release — try that before concluding it cannot work.
