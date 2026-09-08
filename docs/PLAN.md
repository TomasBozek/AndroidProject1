# Plan

The single plan for this template. A new session should be able to read this file and continue.
`CLAUDE.md` is the rulebook, `scripts/README.md` documents the generators, `README.md` is the
human orientation. This file is the work list.

## Status

**Last updated:** 2026-09-08 (Phase 0 complete)
**Gate at last run:** doctor 17/17 · test_scripts 37 · unit tests 61 · build green
**Repo:** 27 Gradle modules · 5 sample features + `template` · 10 scripts

| Phase | Goal | Done | Progress |
|---|---|---|---|
| 0 · Truth and small defects | Docs match code; the defects found in review are fixed | 14 / 14 | `██████████` 100% |
| 1 · Build foundation | Cheaper to build and to change; stable toolchain | 0 / 8 | `░░░░░░░░░░` 0% |
| 2 · App shell and session | What a real app needs on day one, on Navigation 3 | 0 / 8 | `░░░░░░░░░░` 0% |
| 3 · Design system and accessibility | A theme and components worth copying | 0 / 7 | `░░░░░░░░░░` 0% |
| 4 · Testing and quality | Regression coverage that costs nothing to keep | 0 / 5 | `░░░░░░░░░░` 0% |
| 5 · Shipping baseline | Flavors, signing, crash reporting, perf | 0 / 8 | `░░░░░░░░░░` 0% |
| 6 · Data layer | Network and offline, once there is a real API | 0 / 3 | `░░░░░░░░░░` 0% |
| 7 · Backlog | Parked items, kept so they are not forgotten | 0 / 14 | `░░░░░░░░░░` 0% |
| **Total** | | **14 / 67** | `██░░░░░░░░` 21% |

**Now:** nothing in flight.
**Next:** 1.8 (merge `gateway` into `data`), then 1.1 (convention plugins).
**Blocked on a decision:** nothing. All ten decisions were made on 2026-09-08; see below.

### How to keep this file current

1. Starting an item: mark it `[~]`, add its ID to **Now**.
2. Landing an item: run the gate below, mark it `[x] (YYYY-MM-DD)`, remove it from **Now**,
   update the phase row and the total. Refresh the bar: one `█` per 10%.
3. Dropping an item: mark it `[-]` and add one line saying why. Never delete a line. A dropped
   item is removed from its phase total.
4. Adding an item: next free number in its phase. Update the counts.
5. If a decision (D-section) is made, write the outcome and date next to it.
6. If what shipped differs from the item's **Done** line, add a **Landed:** line under it saying
   how. Do not rewrite the original; the difference is the record.
7. Grow an item only when its **Done** line cannot be true without the extra work, and say so
   under **Landed**. Anything else becomes a new numbered item, not part of this one.
8. Commit each landed item on `main` as one commit, message `<id> <title>` (for example
   `0.2 Snackbar sits under the system bar`), with this file's update in the same commit. Start
   an item on a clean tree with the gate green.

The gate, in the order CI runs it:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew build
```

## Done before this plan

Plan 1 (September 2026) delivered 33 items. In short: the four P0 defects (double loading
decrement in `observe()`, session carried by auto-backup, R8 off, no dispatcher switching), nav
arguments via `SavedStateHandle`, inline error/empty/retry states, `Screen()` owning navigation,
snackbar command, typed alert payloads, the Modifier convention, the spacing scale, the testing
foundation (`MainDispatcherRule`, `KoinGraphTest`, a test per screen), CI and `doctor.py` gates,
and the generator suite (`init_project`, `create_feature`, `create_screen --with-args`,
`create_component`, `create_datasource`, `delete_feature`, `export_service`, `install_hooks`,
six slash commands). See git history for the details.

## API you build on (do not reinvent)

| Thing | Where | Note |
|---|---|---|
| `execute {}` / `observe(flow = …) {}` | `service/core/ui/.../BaseViewModel.kt` | Never try/catch in a ViewModel |
| `ErrorDisplay.{Alert,Inline,Silent}` | same | `Inline` remembers the failed call **per content id**; retry re-runs that one |
| `ContentState.{Error,Empty}` | `service/core/ui/.../state/ContentState.kt` | Rendered by `Screen()` instead of content |
| `Screen(onNavigation = …)` | `service/core/ui/.../component/Screen.kt` | Destinations write no collector |
| `navArgs<T>()` | `BaseViewModel` | Route args from `SavedStateHandle`. Goes away in 2.8: on Navigation 3 the route key is a plain ViewModel constructor parameter |
| `AlertPayload`, `SystemEvent.AlertResult` | `state/AlertState.kt`, `event/SystemEvent.kt` | Typed confirm-then-act; see `SettingsViewModel` |
| `AppTheme.spacing` | `core/ui/theme/Spacing.kt` | Defined; not yet used (item 3.3) |
| `MainDispatcherRule`, `FakeLogger` | `testFixtures(projects.service.core.ui)` | Moves in item 1.7 |
| `appModules(isDebug)` | `core/di/Koin.kt` | The one module list; `initKoin` starts it, `KoinGraphTest` verifies it |
| `coreModule(isDebug)` | `core/di/Koin.kt` | WARN-and-above logger in release |
| `execute(loadingMessage = …)` | `BaseViewModel` | Wording for the overlay; survives overlapping calls |
| `DispatcherProvider` / `DefaultDispatcherProvider` | `service/core/domain/coroutines/` | Switch at the data source, not the repository |

**Gotchas already paid for:**

- A screen with nav arguments needs Robolectric 4.16 (`@RunWith(RobolectricTestRunner::class)`,
  `@Config(sdk = [34])`). 4.14 cannot read JDK 25 bytecode. Screens without arguments stay on the
  plain JVM. This gotcha disappears with 2.8: Navigation 3 hands the route key to the ViewModel
  directly, so there is no `Bundle` to decode and every ViewModel test is a plain JVM test again.
- `testFixtures { enable = true }` works on AGP 9.
- `lint { checkDependencies }` belongs to `:app` only.
- `feature/template` holds two screens (`Template*`, `TemplateArgs*`). `create_feature.py` skips
  the args set on purpose.
- The Modifier check in `doctor.py` exempts extensions, value-returning composables and
  `*Theme`/`*Provider`/`*Screen`/`*Preview`. Extend the list rather than fight it.

## Decisions

All ten were made by Tomáš on 2026-09-08. Kept here so the reasoning stays with the plan.

- **D1 · Keep `gateway` as its own module?** It existed so the data layer had a module to depend
  on for the data-source interface. That contract is not what other modules use: they depend on
  the repository interface, which lives in `domain`. The data-source interface is internal to the
  data layer and can sit beside its implementation in a separate package.
  **Outcome (2026-09-08): merge `gateway` into `data`.** Item 1.8. Layers become
  `domain` / `data` / `presentation` / `di`.
- **D2 · Replace the `launch` feature and the 2 s hold with the SplashScreen API?** A splash should
  last as long as startup work does, not two seconds; and it removes two modules and the
  `Screen() { _, _ -> }` overlay in `MainActivity`.
  **Outcome (2026-09-08): replace.** Items 2.1 and 2.2.
- **D3 · `UiState.loading` default to `null`?** Today `UiState(data = x)` shows a loading overlay
  unless told otherwise; `BaseViewModel` is the only caller and always passes it explicitly.
  **Outcome (2026-09-08): flip.** Item 0.9.
- **D4 · Convention plugins in `build-logic/`?** Declined once; 27 identical build blocks and the
  export story changed the maths.
  **Outcome (2026-09-08): yes, and the plugins own the common dependencies too**, not only the
  Android config: a module's build file keeps only its project dependencies. `service/` modules
  use the same plugins; `export_service.py` copies `build-logic/` along with `service/` and
  prints the `includeBuild` line. Item 1.1.
- **D5 · Move from AGP `9.5.0-alpha04` to the latest stable 9.x?** The
  `compileSdk { version = release(37) }` DSL is AGP 9 DSL, so a stable 9.x should carry it.
  **Outcome (2026-09-08): yes.** Item 1.3.
- **D6 · Navigation 3: migrate or hold?** `Screen(onNavigation)` and typed routes map directly onto
  a Navigation 3 back stack, and the tab graphs in Phase 2 should not be built twice.
  **Outcome (2026-09-08): migrate, not spike.** Item 2.8 replaces the 6.4 spike, and 2.3 / 2.4
  are built on it.
- **D7 · Network and database stack?** Ktor uses the kotlinx-serialization already here and needs
  no annotation processing, matching the Koin choice; Room is the standard Android database.
  **Outcome (2026-09-08): Ktor client + Room.** Items 6.1, 6.2.
- **D8 · Crash reporting vendor?** A template should not carry a vendor SDK or its config file.
  **Outcome (2026-09-08): interface plus a logging no-op; vendor recipe in `CLAUDE.md`.** Item 5.3.
- **D9 · Screenshot-testing tool?** The `@ScreenPreview` functions already exist, so the tool that
  renders them directly needs no new test code.
  **Outcome (2026-09-08): Compose Preview Screenshot Testing.** Item 4.1.
- **D10 · Material 3 Expressive?** The template is re-skinned per project; neutral wins.
  **Outcome (2026-09-08): standard Material 3.** Item 3.1.

---

## Phase 0 · Truth and small defects

Goal: the docs describe the code that exists, and the defects found in the September review are
gone. Everything here is small and independent. Do these first; they make every later phase
easier to verify.

- [x] **0.1 Sync CLAUDE.md, README.md and this file with the code** (S) · 2026-09-08
  Why: they say "three sample features"; there are five. The module tree in `CLAUDE.md` omits
  `launch` and `catalog`. Module count is 27, not 26. A wrong rulebook teaches wrong rules.
  Done: tree and counts correct; new `doctor.py` check 17 fails if a `feature/*` directory is not
  named in `CLAUDE.md`'s module tree.
  Landed: the check also compares the **layers** in the tree with the directories on disk, so the
  tree is now a fifth registration — `create_feature.py` and `delete_feature.py` maintain it
  (`_common.py:register_in_feature_tree`), and the round-trip test watches `CLAUDE.md` too. Source
  sets are 38, not 39, after 0.7 removed the empty `app/src/androidTest`.

- [x] **0.2 Snackbar sits under the system bar** (S) · 2026-09-08
  Why: `Screen()` aligns its `SnackbarHost` to the bottom with no insets and the activity is edge
  to edge, so on gesture navigation the snackbar overlaps the bar.
  Done: host padded with `WindowInsets.safeDrawing`; checked on a gesture-nav device or preview.

- [x] **0.3 Remove the unused `TAGGED_LOGGER` factory** (S) · 2026-09-08
  Why: registered in `coreModule`, consumed nowhere. Loggers get their tag via `withTag()`.
  Done: qualifier and constant gone; `KoinGraphTest.extraTypes` trimmed to what `verify()` still
  needs (re-check `String`/`Int`; they may come from `DataStoreProvider` and `AndroidLogger`).
  Landed: both `String` and `Int` came from the tagged logger — `extraTypes` is now `Context` and
  `SavedStateHandle` only.

- [x] **0.4 One source of truth for the Koin module list** (S) · 2026-09-08
  Why: `initKoin()` and `KoinGraphTest` each list every feature module, with a comment asking you
  to keep them in step. That is a drift waiting to happen.
  Done: `core/di` exposes `fun appModules(isDebug: Boolean): List<Module>`; both use it;
  `create_feature.py` and `delete_feature.py` edit that list; `test_scripts.py` still passes.

- [x] **0.5 Generators agree on destination function names** (S) · 2026-09-08
  Why: `create_feature.py` emits `userprofileDestination`, `create_screen.py` emits
  `userProfileDetailDestination`. `CLAUDE.md` documents the difference instead of fixing it.
  Done: both emit camelCase (`rewrite_source` gains a `camel` argument for identifiers, keeps
  `flat` for packages and paths); the note in `CLAUDE.md` is deleted; a test covers it.

- [x] **0.6 Generated data sources switch to IO** (S) · 2026-09-08
  Why: `DefaultLocalAuthDataSource` documents the rule "every data source that touches disk or the
  network switches to `dispatcherProvider.io`". The `create_datasource.py` template does not.
  Done: generated implementation takes `DispatcherProvider` and uses `flowOn`/`withContext`; the
  Koin binding still resolves; `test_scripts.py` asserts the import is present.

- [x] **0.7 Drop the unused `androidTest` dependencies in `:app`** (S) · 2026-09-08
  Why: espresso, `androidx.test.ext:junit` and the Compose test manifest are declared; there is no
  `androidTest` source set anywhere. Item 4.3 adds UI tests as Robolectric unit tests instead.
  Done: dependencies removed; `./gradlew :app:assembleDebug` green.
  Landed: the empty `app/src/androidTest/java` directory went too, and with it the catalog's now
  orphaned `androidx-junit`, `androidx-espresso-core` and `junitVersion` entries. `ui-test-junit4`
  and `ui-test-manifest` stay — item 4.3 needs them.

- [x] **0.8 `:app` sources move from `src/main/java` to `src/main/kotlin`** (S) · 2026-09-08
  Why: it is the only module still on `java/`. `_common.py:APP_NAV_HOST_FILE` hardcodes it.
  Done: directory moved; `_common.py`, `init_project.py` source roots and `test_scripts.py`
  updated; doctor and scripts green.

- [x] **0.9 `UiState.loading` defaults to `null`** (S) · 2026-09-08
  Why: see D3.
  Done: default flipped; `BaseViewModel` unchanged in behaviour; `BaseViewModelTest` still green.

- [x] **0.10 `DispatcherProvider` becomes interface + `DefaultDispatcherProvider`** (S) · 2026-09-08
  Why: the naming rule "Foo / DefaultFoo at every layer" is called rigid in `CLAUDE.md`; this is
  the one type that breaks it (an `open class`).
  Done: interface in `service/core/domain`, default in the same module, Koin binding updated,
  tests use a one-line fake.
  Landed: no test substituted one yet — the only consumer, `DefaultLocalAuthDataSource`, has no
  unit test — so the one-line fake is documented in the interface's KDoc rather than written.

- [x] **0.11 Catalog sample uses the framework it demonstrates** (S) · 2026-09-08
  Why: `ProductDetail` renders "not found" as inline text while `Products` uses
  `ContentState.Empty`. `Product.price` is a `Double`; `asPrice()` hardcodes `$`.
  Done: a missing product shows `ContentState.Empty` with a "Go back" action; price is minor units
  (`Long`) formatted with `NumberFormat.getCurrencyInstance()`; tests updated.
  Landed: with the not-found case moved into `ContentState`, `ProductDetailState.product` is no
  longer nullable and the screen has one branch instead of two. The empty state carries its own
  content id (`ProductDetailViewModel.CONTENT_NOT_FOUND`) so `onSystemEvent` can tell "go back"
  from a retry of a failed load, and answers it with `UiCommand.NavigateBack`.

- [x] **0.12 Retry works for more than one inline load at a time** (S) · 2026-09-08
  Why: `pendingRetry` is a single lambda slot. Two `ErrorDisplay.Inline` calls in flight and only
  the last is retryable; the first retry button silently does nothing useful.
  Done: retries keyed by content id (`Map<String, () -> Unit>`); `SystemEvent.ContentAction(id)`
  picks its own; a `BaseViewModelTest` case covers two concurrent failures.
  Landed: a `ConcurrentHashMap`, plus a second case for an action whose id has no registered call.

- [x] **0.13 Loading message survives the loading counter** (S) · 2026-09-08
  Why: Plan 1's N1. `isLoading`'s setter builds a fresh `LoadingState()`, so a custom message
  passed by a caller is discarded. `updateData` reads oddly for what it does.
  Done: `execute(loading = LoadingState(message))` style API (or `loadingMessage: UiText?`); the
  counter keeps the most recent message; `updateData` renamed or documented; tests cover it.
  Landed: `loadingMessage: UiText?` on `execute`/`observe` and `setLoading(active, message)`. The
  `MutableStateFlow<UiState<*>>.isLoading` extension is **deleted** — its setter was the bug, and
  `setLoading` is the only thing that should write the overlay. `updateData` now takes
  `Data.() -> Data` rather than `Data.(Data) -> Data`, which handed the same value twice.

- [x] **0.14 Forget a retry once its call succeeds** (S) · 2026-09-08
  Why: 0.12 registers the retry lambda when an inline call starts and removes it only when the
  retry button is pressed. After a success the lambda, and the closures it captures, stay in the
  map until the ViewModel is cleared. Bounded and harmless, but a leak is a leak.
  Done: the entry is removed on success as well; a `BaseViewModelTest` case shows that a content
  action arriving after a successful load re-runs nothing.
  Landed: removed by identity (`ConcurrentHashMap.remove(key, value)`), so a later call that has
  since claimed the same content id keeps its own retry.

## Phase 1 · Build foundation

Goal: the build is cheaper to run and to change, the toolchain is stable, and the module count
stops being a tax.

Order: **1.8 first**, so the convention plugins in 1.1 are written for four layers and there are
three fewer modules to convert. Then 1.1, then the rest in any order.

- [ ] **1.1 Convention plugins in `build-logic/`** (L) · D4 decided: yes, config and dependencies
  Why: 27 build files repeat the same `plugins` / `namespace` / `compileSdk` / `lint` block, and
  every presentation module repeats the same twelve dependency lines. `minSdk`, Java target and
  lint config are 27 edits. `export_service.py` ships this project's SDK levels into the next one.
  Done: an included build `build-logic/` (`includeBuild` in `settings.gradle.kts`, `kotlin-dsl`,
  plugins aliased in the version catalog with `version = "unspecified"`), plugin ids prefixed
  `convention.` so `init_project.py` never has to rename them. The rule is **library
  dependencies live in the plugin, project dependencies stay in the module.**
  - `convention.android.library`: `com.android.library`, compileSdk / minSdk, Java 17, the shared
    `lint.xml`, unit-test options. Nothing else.
  - `convention.android.library.compose`: the above plus the Compose compiler plugin,
    `buildFeatures.compose`, the Compose BOM and bundle, tooling on debug.
  - `convention.kotlin.jvm`: `org.jetbrains.kotlin.jvm`, Java 17, JUnit and coroutines-test. For
    every `domain` module (item 1.2).
  - `convention.feature.presentation`: the compose library plus the serialization plugin, Koin BOM
    and bundle, navigation, lifecycle, the `testing` bundle and `testFixtures(:service:core:ui)`.
  - `convention.feature.data` and `convention.feature.di`: android library plus coroutines, and
    plus Koin, respectively.
  - `convention.android.application`: `:app`.
  The namespace is derived from the project path and one `basePackage` property in
  `gradle.properties` (`:feature:auth:presentation` becomes `<base>.feature.auth.presentation`,
  `:service:core:ui` becomes `<base>.service.core.ui`); an explicit `namespace` in a module still
  wins, and `resourcePrefix = "core_"` stays where it is. `init_project.py` then changes one
  property instead of 27 lines. A feature presentation build file ends up as:

  ```kotlin
  plugins { alias(libs.plugins.convention.feature.presentation) }

  dependencies {
      api(projects.core.ui)
      api(projects.feature.auth.domain)
  }
  ```

  `service/` modules use the same plugins; `export_service.py` copies `build-logic/` and prints
  the `includeBuild` line. New `doctor.py` check: no module build file sets `compileSdk`,
  `minSdk`, `compileOptions` or a `lint` block. `feature/template` and every generator emit the
  new shape; `module_namespace()` in `_common.py` falls back to the derived value. Gate green.

- [ ] **1.2 Domain modules are plain Kotlin JVM** (M)
  Why: `:service:core:domain` and every `feature/*/domain` are Android libraries that by rule
  contain no `android.*`. As `kotlin("jvm")` modules the rule is enforced by the compiler, there
  is no manifest, AAR or lint pass per module, and the build is faster.
  Done: `org.jetbrains.kotlin.jvm` in the catalog and root; the five domain modules converted;
  `doctor.py`'s Android-free check kept as a second line; generators and template updated.

- [ ] **1.3 Toolchain: stable AGP, fresh dependencies, Renovate** (M) · D5 decided: yes
  Why: AGP is an alpha. coroutines 1.9.0, Koin 4.0.4, serialization 1.7.3, lifecycle 2.9.1 and
  navigation 2.9.0 lag Compose BOM 2026.02 and Kotlin 2.2.10. A template should start current.
  Done: latest stable AGP 9.x; every catalog entry on the latest stable compatible with the BOM;
  `renovate.json` grouping androidx, kotlin and koin with the version catalog manager enabled;
  build green; lint's version checks stay informational.

- [ ] **1.4 Java target 11 to 17** (S)
  Why: the daemon runs JDK 25, AGP 9 requires 17 to run, and 17 is the current baseline. One edit
  once 1.1 lands.
  Done: `compileOptions` and Kotlin `jvmTarget` at 17 in the convention plugin; build green.

- [ ] **1.5 `api` vs `implementation` hygiene** (S)
  Why: 43 `api(` lines in feature build files, most of them in `di` modules re-exporting layers,
  and `:app` reaches `AppTheme` and `Screen()` only because `:core:di` has `api(projects.core.ui)`.
  Done: `:app` depends on `core.ui` directly; `di` modules use `implementation` except where a
  type is exposed; the Dependency Analysis Gradle plugin runs as `./gradlew buildHealth` (advisory,
  not failing) with its findings fixed once.

- [ ] **1.6 Re-test detekt 2.x and ktlint on JDK 25** (S)
  Why: Plan 1's I3. The earlier failure was version-specific (Robolectric 4.16 works where 4.14
  did not). If the current release reads JDK 25 bytecode, we get formatting and a few rules for
  free.
  Done: outcome recorded in `CLAUDE.md` and the root `build.gradle.kts` comment. If it works, a
  small rule set plus `detekt` in CI. If not, ktlint CLI as its own CI step on its own JDK.

- [ ] **1.7 Shared test fixtures instead of copies** (S)
  Why: `FakeLogger` exists three times (`service/core/data` tests, `service/core/ui` fixtures,
  inline in `BaseViewModelTest`). `FakeAuthService` exists twice (`RecordingAuthService` in
  settings tests). The code promises "plan item F6" that Plan 1 never listed.
  Done: `FakeLogger` in `testFixtures` of `:service:core:domain` (where `Logger` lives);
  `:service:core:ui` fixtures keep `MainDispatcherRule`; `FakeAuthService` in `testFixtures` of
  `:feature:auth:domain`, used by auth and settings tests; the copies deleted.

- [ ] **1.8 Merge `gateway` into `data`** (M) · D1 decided: merge
  Why: a feature is five modules, and the rule "interface in `gateway`, implementation in `data`"
  is the one the docs admit is most often got backwards. The contract other modules depend on is
  the repository interface in `domain`; the data-source interface is internal to the data layer.
  Done: `feature/{auth,catalog,template}/gateway` folded into their `data` modules, with
  `DefaultXRepository` and the `XDataSource` interface in package `...data.repository` /
  `...data.source` and `DefaultXDataSource` in `...data.source`; `ModuleSuffix.Gateway` removed
  from `settings.gradle.kts`; `doctor.py` layer table is `domain` / `data` / `presentation` /
  `di`, plus a new check that a `Default*Repository` never imports a `Default*DataSource`;
  `_common.py:ALL_LAYERS`, `create_feature.py`, `create_datasource.py`, `delete_feature.py`,
  `test_scripts.py`, `CLAUDE.md`, `README.md`, `scripts/README.md` and the `new-datasource` slash
  command all say four layers; gate green.

## Phase 2 · App shell and session

Goal: the template demonstrates what every real app needs in week one: a proper splash, a session
switch without hacks, bottom navigation, transitions, permissions, all on Navigation 3.

Order: **2.8 first** (or together with 2.1 and 2.2, since both touch the navigation host). 2.3
and 2.4 are built on Navigation 3; do not build tab graphs on Navigation 2 and migrate them later.
2.5, 2.6 and 2.7 are independent.

- [ ] **2.1 SplashScreen API replaces the `launch` feature and the 2 s hold** (M) · D2 decided: yes
  Why: `LaunchScreen` is a spinner, `MainViewModel` delays two seconds so it is visible, and
  `MainActivity` composes an invisible `Screen()` over the nav host just to receive the graph
  switch. `androidx.core.splashscreen` with `setKeepOnScreenCondition { session is Unknown }`
  does the same job with no delay and no extra feature.
  Done: `feature/launch` deleted via `delete_feature.py`; the navigation host is composed only
  once the session is known, starting in the matching flow (auth or main); a later session change
  replaces the whole back stack; `Screen.isTransparent` removed; `themes.xml` uses
  `Theme.SplashScreen`.

- [ ] **2.2 `MainViewModel` is a plain `ViewModel` with a `SessionState`** (S) · D2 decided: yes
  Why: it is not a screen. Today it subclasses `BaseViewModel` with a state nothing renders and a
  `sessionKnown` flag to work around that.
  Done: `sealed interface SessionState { Unknown; SignedIn; SignedOut }` exposed as `StateFlow`;
  a read failure after retries maps to `SignedOut` and a WARN log instead of a modal over nothing;
  `MainNavigation`, `MainEvent`, `MainState` deleted; `KoinGraphTest` green.

- [ ] **2.3 Bottom navigation with nested graphs** (M)
  Why: Plan 1's D1. The first thing a real project adds and the one thing the template does not
  show. `NavigationSuiteScaffold` gives a rail on tablets for free.
  Done: Home, Catalog and Settings as top-level destinations on Navigation 3 (after 2.8); each tab
  keeps its own back stack and survives process death; `create_feature.py --graph <tab>` registers
  the entry under that tab; `doctor.py` still finds every destination; the `homeDestination`
  lambdas for settings and catalog are removed.

- [ ] **2.4 Shared enter/exit transitions** (S)
  Why: Plan 1's D4. Default cross-fade looks unfinished; one shared spec on the host is cheap.
  Done: one shared `transitionSpec` / `popTransitionSpec` on the `NavDisplay` (after 2.8); tab
  switches fade, pushes slide; predictive back animates.

- [ ] **2.5 Permissions, redesigned** (L)
  Why: Plan 1's E1 to E5, and the part of the original brief with nothing built yet. The
  original's `PermissionBox` named the implementation not the job, left an `if (!granted) return`
  footgun to the caller, and pulled Accompanist into `service/`.
  Done: package `service/core/ui/.../permission/` with `PermissionStatus` (`Granted`,
  `Granted.Partial`, `Denied(canAskAgain)`, `NotRequested`), `rememberPermissionRequest(vararg)`
  on `rememberLauncherForActivityResult`, `PermissionGate(permission, rationale) { content }`
  composing content only when granted, `PermissionRationale` as a data class with an optional
  slot. The "open app settings" intent extracted from `Screen()` and reused, not copied.
  `POST_NOTIFICATIONS` in the manifest with a channel created on first launch and requested at a
  sensible moment. A Permissions screen in `feature/settings` (full seven-file unit) listing what
  the manifest declares, read from `PackageManager`, with status chips. Verified on device in
  all three states: grant, deny once, deny permanently.

- [ ] **2.6 Snackbar action is a `SystemEvent`, not a lambda in a command** (S)
  Why: `UiCommand.ShowSnackbar.onAction` is a function inside a data class, the one command that is
  not plain data. Alerts already solve this with `AlertResult(id)`.
  Done: `ShowSnackbar(id, message, actionLabel, …)` and `SystemEvent.SnackbarAction(id)` handled
  in `onSystemEvent`; `Screen()` routes it; one test.

- [ ] **2.7 `UiText.Plural`** (S)
  Why: quantity strings are a week-one need and `UiText` cannot express them today.
  Done: `UiText.Plural(id, quantity, args)` resolving through `getQuantityString`; a `toUiText`
  overload; one test with a `Resources` fake or Robolectric.

- [ ] **2.8 Migrate to Navigation 3** (L) · D6 decided: migrate
  Why: `Screen(onNavigation)` plus typed `@Serializable` routes already look like a Navigation 3
  back stack of keys. Navigation 3 also removes the `SavedStateHandle` + `toRoute()` detour: the
  route key is handed to the screen and its ViewModel directly, so argument-carrying screens lose
  the Robolectric requirement and every ViewModel test is a plain JVM test.
  First step: check the current stable `androidx.navigation3` version and its Koin integration
  before writing code; this plan was written without that knowledge.
  Done: `AppNavHost` is a `NavDisplay` over `rememberNavBackStack(...)` with the saved-state and
  ViewModel-store entry decorators; each `XDestination.kt` keeps its `@Serializable` route (now a
  `NavKey`) and exposes an `EntryProviderBuilder` extension instead of a `NavGraphBuilder` one;
  `Screen(onNavigation)` lambdas call the back stack instead of a `navController`; the
  `TemplateArgs*` set passes the key into the ViewModel through Koin `parametersOf`, `navArgs<T>()`
  is deleted from `BaseViewModel` and the two Robolectric tests become JVM tests;
  `create_feature.py`, `create_screen.py`, `delete_feature.py` and the `doctor.py` destination
  check target the new registration shape; `UiCommand.NavigateBack` still works through the
  back-pressed dispatcher; process death restores the back stack; `CLAUDE.md` and
  `scripts/README.md` updated; gate green.

## Phase 3 · Design system and accessibility

Goal: the theme is something a project keeps rather than replaces on day one, and the sample
screens are built from shared components. Order matters: 3.1 before 3.4, 3.2 before 3.4.

- [ ] **3.1 A real colour scheme** (M)
  Why: Plan 1's G2. `Color.kt` is the wizard's purple with three roles; `dynamicColor = true`
  means the app never looks like itself.
  Done: full M3 role set (primary/secondary/tertiary, surface, error, outline) in light and dark
  from one seed, neutral by design; `AppTheme(dynamicColor = false)` default; every
  `@ScreenPreview` checked in dark. Standard Material 3, not Expressive (D10).

- [ ] **3.2 Full type scale** (S)
  Why: `Type.kt` defines `bodyLarge` only; everything else falls back to Material defaults.
  Done: the M3 scale defined explicitly with `FontFamily.Default`, so a brand font is one edit.

- [ ] **3.3 Spacing scale actually used** (S)
  Why: Plan 1's G1-finish. `AppTheme.spacing` exists; 28 `.dp` literals remain in screens.
  Done: literals migrated; `doctor.py` check 18 flags a bare `.dp` literal in a feature screen
  (allow-list for `1.dp` dividers and hairlines).

- [ ] **3.4 Component set** (M)
  Why: Plan 1's G4. Sample screens hand-roll buttons, text fields and headers.
  Done: `AppButton`, `AppTextField` (with error state), `AppTopBar`, `AppListItem`, `EmptyState`
  (wrapping `ContentMessage`), `Skeleton`, each generated with `create_component.py`, each with
  a `@ComponentPreview`; sample screens use them.

- [ ] **3.5 `AppImage` over Coil 3** (S)
  Why: Plan 1's G5. Features should not import an image library directly.
  Done: one composable in `core/ui` with placeholder and error states; Coil is `implementation`
  in `core/ui` only; `doctor.py` flags `coil` imports in features.

- [ ] **3.6 Accessibility pass** (M)
  Why: Plan 1's G7. Lint's `ContentDescription` and `ClickableViewAccessibility` are warnings.
  Done: every icon has a description or is marked decorative; 48 dp touch targets; a `testTag`
  convention documented and used by 4.3; a font-scale 1.5 variant in `@ScreenPreview`; the two
  lint rules raised to `error`.

- [ ] **3.7 `@Immutable` on every `XState`** (S)
  Why: Plan 1's C5. States hold `List<Product>`. Strong skipping covers most of it since Kotlin
  2.0.20, but the annotation documents intent and lets the compiler skip more.
  Done: template and `create_component.py --state` emit `@Immutable`; `doctor.py` check 19 flags
  an `XState` without it. `kotlinx-collections-immutable` only if 4.1's screenshots or a profiler
  show a need.

## Phase 4 · Testing and quality

Goal: regressions are caught by things that already exist (previews, generators), not by
new manual effort. Do 4.2 before 4.1 so goldens are recorded once.

- [ ] **4.1 Compose Preview Screenshot Testing** (M)
  Why: Plan 1's H3 and D9. Ten screens already carry `@ScreenPreview`; goldens are nearly free.
  Done: plugin applied to presentation modules through the convention plugin;
  `./gradlew updateDebugScreenshotTest` records, `validateDebugScreenshotTest` runs in CI;
  deliberately break one padding value and confirm it fails; goldens committed.

- [ ] **4.2 Preview variants in the template** (S)
  Why: Plan 1's C7. One preview per screen shows one state.
  Done: a `PreviewParameterProvider` for loaded / empty / long-text in `feature/template`, cloned by
  the generators, so every new screen ships three goldens.

- [ ] **4.3 One Compose UI test as the pattern** (S)
  Why: Plan 1's H4. Nothing shows how to test a screen's behaviour end to end.
  Done: `LoginScreenTest` under Robolectric using `ui-test-junit4` and the `testTag`s from 3.6,
  running as a unit test so CI needs no emulator; documented in `CLAUDE.md`.

- [ ] **4.4 Coverage report** (S)
  Why: not a gate, a signal. Cheap to add and it shows which module the tests avoid.
  Done: Kover aggregated report uploaded as a CI artifact; no threshold.

- [ ] **4.5 Generator output compiles** (M)
  Why: `test_scripts.py` checks text, not compilation, and the docs admit it. A template change
  that breaks generated code is found by the next user, not by CI.
  Done: an opt-in test (`--with-gradle`) that runs `create_feature.py` in the temp copy and then
  `./gradlew :feature:x:presentation:compileDebugKotlin`; run in CI only.

## Phase 5 · Shipping baseline

Goal: a project started from this template can ship without adding infrastructure first.

- [ ] **5.1 Flavors dev / staging / prod** (S)
  Why: Plan 1's B3. Base URL and app-id suffix per environment.
  Done: three flavors, `BuildConfig.BASE_URL`, distinct launcher label per flavor.

- [ ] **5.2 Release signing from `keystore.properties`** (S)
  Why: Plan 1's B4. The file is already gitignored; nothing reads it.
  Done: `signingConfigs.release` reads it when present, falls back to debug when absent so CI
  still assembles.

- [ ] **5.3 `:service:errortracker`** (M) · D8 decided: no vendor SDK in the repo
  Why: Plan 1's J1. Four hook points already exist in `BaseRepository` and `BaseViewModel`. A
  template should not carry a vendor SDK or its config file.
  Done: `ErrorTracker` interface in `service/`, a `LoggingErrorTracker` bound by default;
  `handleError` and the repository's `logger.w` paths report through it; a recipe in `CLAUDE.md`
  shows the few lines that swap in Crashlytics or Sentry inside `:app`. No vendor dependency.

- [ ] **5.4 LeakCanary and Chucker on debug** (S)
  Why: Plan 1's J4.
  Done: both on `debugImplementation`; Chucker wired once 6.1 gives it a client.

- [ ] **5.5 gitleaks in CI** (S)
  Why: Plan 1's M3.
  Done: a job on push and pull request; a baseline file if needed.

- [ ] **5.6 Baseline profile and macrobenchmark** (M)
  Why: startup time is a shipping concern and the module is boilerplate a template should carry.
  Done: `:baselineprofile` module generating the profile for the main flow; a startup benchmark;
  profile committed to `:app`.

- [ ] **5.7 Release build in CI** (S)
  Why: R8 is on but only `build` runs; a keep-rule regression shows up at release time.
  Done: `assembleRelease` and `lintRelease` on tags and on a weekly schedule.

- [ ] **5.8 Session stored encrypted** (M)
  Why: Plan 1's M2. The session is plain text in Preferences DataStore. `security-crypto` is
  deprecated, so the answer is not that.
  Done: Keystore-backed AEAD (Tink or a small Keystore wrapper) as a DataStore `Serializer`;
  `LocalAuthDataSource` unchanged; one Robolectric test for round-trip.

## Phase 6 · Data layer

Goal: network and offline, held until there is a real API to point them at. Do not build these
against mock data.

- [ ] **6.1 `:service:network`** (L)
  Why: Plan 1's F2 and D7. Ktor client, status-to-`DomainError` mapping, auth header,
  single-flight token refresh.
  Done: module in `service/` with `api(projects.service.core.domain)` only; a `NetworkError` /
  `ServerError` / `UnauthorizedError` mapping table with tests; `export_service.py` picks it up
  automatically.

- [ ] **6.2 Room and offline-first** (L)
  Why: Plan 1's F3 and D7 (Room). The original had `repositoryCall(remote, local, updateLocal)`
  emitting cache then remote.
  Done: `BaseRepository.observe(local, remote, updateLocal)`; one feature (catalog) converted;
  tests for cache hit, cache miss, remote failure with stale cache.

- [ ] **6.3 `create_datasource.py --remote`** (S)
  Why: once 6.1 exists, the generator should produce the network-backed variant, not only
  DataStore.
  Done: flag emits a Ktor-backed `DefaultRemoteXDataSource`; tested in `test_scripts.py`.

- [-] **6.4 Navigation 3 spike** (M)
  Dropped 2026-09-08: D6 was decided without a spike. The migration is item 2.8.

## Phase 7 · Backlog

Parked. Not scheduled, kept so they are not lost. Promote by moving to a phase and renumbering.

- [ ] **7.1 Deep links** (M) · Plan 1's D2.
- [ ] **7.2 Navigation results (screen returns a value)** (M) · Plan 1's D3.
- [ ] **7.3 Feature-owned nav graphs** (M) · Plan 1's D5.
- [ ] **7.4 Analytics with automatic screen tracking from `Screen()`** (M) · Plan 1's J.
- [ ] **7.5 Logger backend, remote config, debug menu** (M) · Plan 1's J.
- [ ] **7.6 `doctor.py --fix` for the mechanical checks** (M) · Plan 1's L3.
- [ ] **7.7 `create_service.py`** (S) · Plan 1's L4.
- [ ] **7.8 `check_strings.py` (unused and missing strings)** (S) · Plan 1's L5.
- [ ] **7.9 Localisation pipeline** (M) · Plan 1's L7.
- [ ] **7.10 `:core:designsystem` split and adaptive layouts** (L) · Plan 1's G3, G6.
- [ ] **7.11 ADRs and a release process doc** (S) · Plan 1's M4, M5.
- [ ] **7.12 Licence** (S) · Plan 1's N6/N8. The repo has no LICENSE file since the init commit.
- [ ] **7.13 ViewModel-readable permission state** (S) · Plan 1's E6. Build when a feature needs it.
- [ ] **7.14 Undecoded Plan 1 codes: C8, C9, C10, H7, I6, K3, K4** (?)
  Why: Plan 1 lists these by code only and the audit they refer to is not in the repo. Nobody can
  act on them.
  Done: either the descriptions are recovered and each becomes a real item, or this line is marked
  `[-]` with "audit not available".

## Verification

Every item ends green on the gate. Phase-specific checks:

- **Phase 0 and 1 (generators, build files):** `python3 scripts/test_scripts.py`, then generate a
  throwaway feature end to end, build it, `delete_feature.py`, and confirm the tree is byte-identical.
- **Phase 2 (splash, session, navigation, permissions):** cannot be unit-tested alone.
  `./gradlew :app:installDebug`, cold start signed out and signed in, log out from settings, and
  for permissions all three states (grant, deny once, deny permanently). After 2.8: predictive back
  on every screen, and "Don't keep activities" on with a deep back stack to confirm it restores.
- **Phase 3 (theme, spacing):** every `@ScreenPreview` in light and dark; after 3.3 the new
  `doctor.py` check passes on the tree.
- **Phase 4 (screenshots):** record, then break one padding value and confirm the validate task
  fails, then restore.
- **Doctor count:** 17 today (0.1 added the CLAUDE.md tree check). Phases 1 to 3 take it to about
  20 (no direct `compileSdk`, bare `.dp`, `XState` without `@Immutable`). Update the counts in
  `CLAUDE.md`, `README.md` and `scripts/README.md` when they change.
