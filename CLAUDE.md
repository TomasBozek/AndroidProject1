# CLAUDE.md

Guidance for Claude Code (claude.ai/code) in this repository.

**This file is the rules.** Everything else is one link away from [docs/README.md](docs/README.md),
read when a task names it. A fact lives in exactly one file — write it twice and one copy is already
wrong.

**Work** is [docs/ai/PROCESS.md](docs/ai/PROCESS.md) — ids, points, lanes, the task loop — and
the plan under `docs/ai/plans/` whose header says `Status: open`. Take a task with `/task <id>`,
never by picking something that looks useful. Work you find on the way is one line in
[docs/BACKLOG.md](docs/BACKLOG.md).

## Project

Multi-module Android app (Kotlin + Compose), base package `com.example.androidproject1`, and a
**template**: the structure and conventions matter more than the five sample features. Layered
Clean/MVI — single activity, type-safe Compose navigation, Koin DI.

- `minSdk = 29`, `targetSdk = compileSdk = 37`, Java 17 — all in `build-logic`'s `ProjectConfig`.
- AGP `9.4.0`, Kotlin `2.4.20`, Gradle `9.6`, Compose BOM `2026.08.00`; Renovate keeps them current.
- Dependencies come from `gradle/libs.versions.toml` — never a version in a module build file.
- **A new dependency has to earn its place.** Google, JetBrains and androidx first; then a library
  with a large company behind it and broad adoption. Anything else needs a row in
  [docs/DECISIONS.md](docs/DECISIONS.md), and if it ships in the release build, a
  first-party alternative that was tried and found wanting. Build- and test-only tools are judged
  more leniently but still get the row; the standing exceptions are **Koin** and **Coil**. This
  applies to what is here as much as to what is added: an unused dependency is removed, not kept for
  symmetry.
- AGP 9 applies Kotlin itself; a new plugin is declared in the root `build.gradle.kts` with
  `apply false` before a module can `alias(...)` it.
- **A module build file is a `plugins` block and its project dependencies. Nothing else.**

## Convention plugins

`build-logic/` is an included build holding the `convention.*` plugins every module applies:
**library dependencies live in the plugin, project dependencies stay in the module**, so a typical
build file is `plugins { alias(libs.plugins.convention.feature.presentation) }` and two `api(...)`
lines.

| Plugin | Applies |
|---|---|
| `convention.android.library` | `com.android.library`, SDK levels, Java target, shared `lint.xml`, derived namespace. No dependencies |
| `convention.android.library.compose` | the above plus the Compose compiler, `buildFeatures.compose`, the BOM and the `compose-core` bundle |
| `convention.kotlin.jvm` | Kotlin/JVM, Java target, coroutines, JUnit, coroutines-test. Every `domain` module |
| `convention.feature.data` | android library plus coroutines, the `testing` bundle, `testFixtures(:service:core:domain)`, MockEngine and Robolectric |
| `convention.feature.di` | android library plus the Koin BOM and bundle |
| `convention.feature.presentation` | the compose library plus serialization, Koin, Navigation 3's ViewModel decorator, lifecycle, the `testing` bundle and `testFixtures(:service:core:ui)` |
| `convention.android.application` | `:app`: app identity, R8 on release, `lint.checkDependencies` |

The **namespace is derived** from the project path and `basePackage` in `gradle.properties`, so
`init_project.py` rewrites one property rather than one line per module; `applicationId` comes from
the same property. `doctor.py` fails if a module build file sets `compileSdk`, `minSdk`,
`targetSdk`, `compileOptions` or a `lint` block.

**A release is a tag, not an edit.** `git tag v1.2.0 && git push origin v1.2.0` — `versionName` is
the tag without its `v`, `versionCode` is `git rev-list --count HEAD`; any build not on a `v*` tag
is 1 / `"1.0"`.

## Module structure

Modules are registered in `settings.gradle.kts` through `includeServiceModule` / `includeCoreModule` /
`includeFeatureModule`, which fail at settings time if a directory or build file is missing.

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
:feature:devmenu:{presentation,di}                screen only; dev and staging only (D16)
:feature:gallery:{presentation,di}                screen only; reached from the debug menu
:feature:home:{presentation,di}                   screen only; a tab
:feature:onboarding:{domain,data,presentation,di} full stack; the first-run flow, behind a stored flag
:feature:profile:{domain,data,presentation,di}    full stack
:feature:settings:{domain,data,presentation,di}   full stack; a tab; reads :feature:auth:domain
:feature:template:{domain,data,presentation,di}   what the generators clone
```

`service/` holds **reusable** modules — the architecture, with no knowledge of this app's features,
theme or DI graph. Reuse is by directory copy, so keep it self-contained: never reference `:core:*`,
`:feature:*` or `:app`, and never read `R` from elsewhere. Packages under `service/` stay
`...core.*` while the Android **namespaces** move to `...service.core.*`, because two modules cannot
share one. `:core:ui` re-exports `:service:core:ui` with `api(...)`, so a feature's `presentation`
depends on nothing but `projects.core.ui`. Two rules keep it portable, and both are load-bearing:

- **`:service:core:domain` stays free of `android.*`.** The `Logger` *interface* lives there,
  `AndroidLogger` in `:service:core:data`. It is a Kotlin/JVM module, so the compiler enforces it —
  don't make it an Android library to get around a framework import, move the class instead.
- **`:service:core:ui` sets `resourcePrefix = "core_"`** and `:core:ui` sets `app_`, so neither can
  collide with a consuming app's resources. Lint's `ResourceName` is an error.

Fixtures live with the type they fake — `FakeLogger` and `TestDispatchers` in `testFixtures` of
`:service:core:domain`, `MainDispatcherRule` in `:service:core:ui`'s. Never write a second copy of a
fake; move the first.

| Layer | Depends on |
|---|---|
| `domain` | `api(projects.service.core.domain)` only. A **Kotlin/JVM** module — no manifest, no AAR, no lint pass, and `android.*` is not on its classpath |
| `data` | `service:core:data` + own `domain`; holds `DefaultXRepository` and both halves of the data source |
| `presentation` | `api(projects.core.ui)` + own `domain` |
| `di` | `api(...)` of all of the above |

`data` splits in two: `repository` holds `DefaultXRepository`, `source` holds the `XDataSource`
interface *and* its `DefaultXDataSource`, because nothing above `data` names it — so a repository
imports `XDataSource` and never `DefaultXDataSource`, and `doctor.py` fails if it does. Naming is
rigid at every layer: `Foo` ↔ `DefaultFoo`. A feature's `presentation` must never depend on another
feature's `presentation` (cross-feature navigation is a lambda wired in `AppNavHost`); another
feature's `domain` is fine. `:feature:template` is what the generators clone — keep it working.

## Design system

`:core:ui` holds the design system, imported from the KSD system in Claude Design. Three layers, and
the split is what makes a re-brand one file rather than a sweep:

**1 · core** (`theme/Ramp.kt`, `Scale` in `theme/Spacing.kt`) is raw ramps and the 4 dp scale, and
is **`internal`** so no screen can name a step. **2 · semantic** (`theme/{Color,Type,Shape,…}.kt`)
is roles, and the only layer that differs light ⇄ dark. **3 · component** (`component/*.kt`) binds a
role to an element and its states.

Read layer 2 through `AppTheme`: `.colors`, `.typography`, `.shapes`, `.elevation`, `.motion`,
`.density`, `.spacing`, `.icons` — the last being three icon sizes and only three (`sm` 18 in a row,
`md` 24 for a control, `lg` 32 where the icon is the thing being looked at). A touch target is
`AppTheme.density.minTouchTarget` and a different question.

**A feature composes components; it never draws.** A `presentation` module imports from
`core.ui.component` and `core.ui.theme` and nothing else in Compose's widget set:

- no `androidx.compose.material3.*` — every widget a screen needs has an `App*` counterpart
- no `.dp` or `.sp` literal — ask `AppTheme.spacing` and `AppTheme.typography` for a role
- no `Color(...)` and no `MaterialTheme.colorScheme` — ask `AppTheme.colors` for a role
- no `NumberFormat`, `DecimalFormat` or `DateTimeFormatter` — ask `LocalFormats.current` for a role

`AppScaffold` is the screen shell: base surface, system insets, an optional `AppTopBar`. A screen
does not call `safeDrawingPadding()` itself — that is what got screens padded twice — but a screen
with no scaffold at all does, because the activity is edge to edge and `Screen()` applies no insets.

If a screen needs something the set does not have, **add it to `:core:ui` with
`create_component.py`**, give it a `@ComponentPreview`, and add it to `GalleryCatalog.kt` in the
same change. Elevation is not `Modifier.shadow`: a pressable surface uses `Modifier.keySurface(…)`,
a hard bottom edge that shortens on press.

### Testing a screen

Two tests per screen: `XViewModelTest` (plain JVM) says what the state becomes and what navigation
is emitted; `XScreenTest` (Robolectric, so `./gradlew test` covers it) says what is on screen and
what a tap does. `LoginScreenTest` is the pattern, and Robolectric's API level is pinned once in
`build-logic/robolectric/robolectric.properties`. `PreviewScreenshotTest` records a golden per
preview, and `OverlayScreenshotTest` in `:core:ui` covers what previews cannot see, because a
dialog, sheet, menu or picker draws in a window of its own — 349 goldens were green while the date
picker was clipped, so add an overlay and add a case there. `recordRoborazziDebug` writes the
goldens, `verifyRoborazziDebug` checks them, and **a golden nobody looked at is a test that passes
forever** — open what `record` wrote before committing it. The rest is in
[docs/ai/TESTING.md](docs/ai/TESTING.md).

### Test identifiers

One id serves the screen reader, the test and the design registry, so there is one to keep in sync
rather than three:

- **A screen** is `<Domain><Purpose>Screen` and carries its own name — `AppScaffold(screenId =
  "SettingsScreen")`, published as a resource id, so `assertVisible: id: "SettingsScreen"` is the
  universal check that a flow is where it meant to be.
- **An element** is `<screenStem>_<element>`: the screen's name in camelCase without `Screen`, then
  the element from a closed vocabulary — `Button`, `Field`, `Switch`, `Checkbox`, `List`, `Item`,
  `Tile`, `Key`, `Dialog`, `Sheet`, `Tab`, `Badge`, `Value`. So `"settings_permissionsButton"`.
- **Find by id, never by text.** Copy changes and gets translated; a test that finds a button by its
  label fails on a wording fix. An icon with no visible label carries the same string as its
  `contentDescription`, which is why accessibility here is a by-product of being testable.

## Screen structure (the unit of work)

Every screen is eight files — six in **a package of its own**, named after the screen and flat
lowercase, plus its two tests in the matching test package. A feature gets one even when it has a
single screen, so every feature reads the same and a second screen never forces a move.

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

`XState.PREVIEW` is required — it is the preview fixture and usually the `initialState`.

Two `doctor.py` checks hold the shape: a screen's directory holds that screen's six files and
nothing else, and a screen file holds the screen and its previews and no other composable. So a
composable a screen grows has exactly one home — **`presentation/component/`, one file each with a
`@ComponentPreview`**, written with `create_component.py --feature <name>`. One a *second feature*
wants goes to `:core:ui`, the same way — never copied.

## MVI conventions

- ViewModels expose `state: StateFlow<UiState<State?>>`; the UI sends events via `onUiEvent(event)`.
- **`BaseViewModel` takes `initialState` first.** Pass the state the screen renders straight away
  and it starts with no overlay; pass `null` only when the screen cannot render until something
  loads. Never write `init { uiState.update { ... loading = null } }` — one forgotten line there
  strands a screen behind a permanent spinner.
- `UiState(data, loading, alert)` is an envelope. **Loading overlays and alert dialogs are rendered
  centrally by `Screen()`** — never reimplement them in a feature screen.
- `Screen()` is the only place that calls `collectAsStateWithLifecycle` and the only interpreter of
  `UiCommand`. A feature screen only ever receives a non-null state. Every command is plain data: a
  snackbar's action comes back as `SystemEvent.SnackbarAction(id)`, handled in `onSystemEvent`.
- **Use `execute {}` (one-shot) and `observe(flow = …) {}` (flows) rather than try/catch.** They
  drive the loading state, turn `Outcome.Failure` into an alert and rethrow cancellation; pass
  `loading = {}` for a screen with its own inline loading. Overlapping calls are reference-counted.
- `navigation` and `command` are buffered channels, not shared flows, so a one-shot emitted while
  nothing collects arrives on resume rather than being dropped. Single-consumer by design.
- `AlertState.title` has no default, so an ordinary confirmation is not labelled "something went
  wrong". Alert results arrive at `onSystemEvent` as `SystemEvent.AlertResult.*` tagged with the
  alert's `id`; delegate what you don't handle to `super`. See `SettingsViewModel`.
- `MainViewModel` is the single owner of session state and the only thing that switches flows — a
  **plain `ViewModel`**, exposing `sessionState: StateFlow<SessionState>` (`Unknown` / `Onboarding`
  / `SignedIn` / `SignedOut`). Screens change the session and let it react; never navigate between
  the auth and main flows directly.
- Strings reachable from a ViewModel are `UiText` (`R.string.x.toUiText()`); a quantity string is
  `R.plurals.x.toPluralUiText(count, count)`, named differently on purpose. Strings used only in a
  composable use `stringResource(...)`, and each feature owns its `res/values/strings.xml`.
- **A resource is named after the screen that shows it** — `login_title` — or after the feature when
  two screens share one; `doctor.py` reads the prefixes off the `*Screen.kt` files. **Every string
  ships in every locale**, and `doctor.py` fails on a module missing one; Czech's four plural forms
  are in [docs/ai/RECIPES.md](docs/ai/RECIPES.md).

## API you build on (do not reinvent)

Plan 2's reference table, kept here because the plan holds only open work.

| Thing | Where | Note |
|---|---|---|
| `execute {}` / `observe(flow = …) {}` | `service/core/ui/.../BaseViewModel.kt` | Never try/catch in a ViewModel. `loadingMessage` words the overlay and survives overlapping calls |
| `ErrorDisplay.{Alert,Inline,Silent}` | same | `Inline` remembers the failed call per content id; the retry re-runs that one and forgets it on success |
| `ContentState.{Error,Empty}` | `service/core/ui/.../state/ContentState.kt` | Rendered by `Screen()` instead of content; a screen with two of them gives each its own `id` |
| `AlertPayload`, `SystemEvent.AlertResult` | `state/AlertState.kt`, `event/SystemEvent.kt` | Typed confirm-then-act. `SettingsViewModel` shows the confirm-then-act pattern but carries no payload; nothing does yet |
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

## Recipes

Start here for any new code. **Do not create these files by hand.** The scripts perform the five
registrations a manual copy silently skips — `settings.gradle.kts`, `:core:di`'s build file,
`Koin.kt`, `AppNavHost.kt` and the module tree above — and `doctor.py` fails on the ones you forget.
Extend a script rather than working around it.

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
| to reuse `service/` in another project | `python3 scripts/export_service.py --to <dir> --package <pkg>` |

Pass the name in any case; what comes out is fixed — directories and packages flat lowercase,
classes PascalCase, functions camelCase, resources snake_case. `--dry-run` shows the plan first, the
follow-up steps are in [docs/ai/RECIPES.md](docs/ai/RECIPES.md), and the same workflows are
slash commands in `.claude/commands/`.

**Do not add a script.** The Kotlin is the work; the ten are the set. Change one when something else
forces you to and treat that as part of the change that caused it; anything that would be a new tool
goes to [docs/BACKLOG.md](docs/BACKLOG.md).

## Checks

Five tiers. **You run T0 and T1; CI runs T2, T3 and T4.** Never `./gradlew build` — it assembles
every variant and runs R8 three times.

| | When | Run |
|---|---|---|
| **T0** | once or twice while working — `/check` | `python3 scripts/doctor.py && ./gradlew ktlintCheck`, then the touched module's own `test` (~45 s) |
| **T1** | once, after `git rebase origin/main`, before the pull request — `/check pr` | doctor · `ktlintCheck` · `:app:assembleDevDebug` · `test` for every module whose `src/main` changed · plus the three conditionals below (2–6 min) |
| **T2** | every non-draft pull request | conventions always; the build only when the diff is not documentation-only; goldens only when a UI path moved |
| **T3** | every push to `main` | T2 with nothing skipped, plus coverage |
| **T4** | a `v*` tag, and weekly | the release build, the generator compile, the end-to-end flows |

T1's conditionals, decided from `git diff --name-only origin/main...HEAD`:

- a path under `*/presentation/src/main`, `core/ui` or `service/core/ui` → add
  `verifyRoborazziDebug` **to the same `./gradlew` invocation as `test`**, never a second one:
  a task runs at most once per invocation, so one pass captures and compares, and two passes
  run every Robolectric test twice
- a path under `scripts/`, `feature/template/` or `.claude/commands/` → `python3 scripts/test_scripts.py`
- a path under `build-logic/`, `gradle/`, `service/` or `core/` → the whole `./gradlew test`

Do not wait for CI. Open the pull request, start the next task, and check `gh pr checks` between
tasks.

## Working a task

- `/task <id>` takes the first `[ ]` line in your lane; the branch is `<id>-<slug>`.
- A `Decide first` line is settled before the code, as a row in `docs/DECISIONS.md`.
- Touch only the shared files your lane owns. Otherwise stop and take the next task.
- A fact you changed moves to its one doc in the same commit — `docs/ai/PROCESS.md` § Which doc
  changes when.
- **One commit** per task, titled `<id> <title>`, carrying the code, the docs and the board line
  flipped to `[x]` with `· est → act`.
- Open the pull request with the template; `gh pr merge --rebase --delete-branch` once it is green.

## Commands

```bash
./gradlew :app:assembleDevDebug      # one variant
./gradlew installDevDebug            # onto a device
./gradlew test                       # every module's unit tests, one variant each
./gradlew lint
./gradlew ktlintCheck                # ktlintFormat fixes what it can
./gradlew koverHtmlReport            # coverage: a signal, never a gate
```

Build a single module, e.g. `./gradlew :feature:auth:presentation:assembleDebug`.

Three flavors on one `environment` dimension — `dev`, `staging`, `prod` — so a build is `devDebug`
or `prodRelease` and `assembleDebug` alone no longer names a variant. `dev` and `staging` carry an
application-id suffix and their own launcher label, so all three install side by side.
`BuildConfig.BASE_URL` differs per flavor and a screen never writes a URL literal; the flavors are
defined once, in `ProjectConfig.Flavor`. Compose stability reports, coverage and the ktlint rule set
are in [docs/ai/TESTING.md](docs/ai/TESTING.md).

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
- `variant.enableUnitTest` does not compile on AGP 9.4.0, even though `LibraryVariantBuilder` and
  `ApplicationVariantBuilder` both list `HasUnitTestBuilder` among their supertypes and neither
  redeclares the property. Assign through a `HasUnitTestBuilder`-typed local instead; the convention
  plugins do, and the local is load-bearing rather than style.
