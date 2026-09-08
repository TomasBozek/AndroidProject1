# Plan

The single plan for this template. A new session should be able to read this file and continue.
`CLAUDE.md` is the rulebook, `scripts/README.md` documents the generators, `README.md` is the
human orientation. This file is the work list.

## Status

**Last updated:** 2026-09-08 (Phases 0, 1 and 2 complete; 3.1 and 3.2 landed)
**Gate at last run:** doctor 20/20 · test_scripts 37 · ktlint clean · unit tests 73 · build green
**Device pass:** emulator `medium_phone_1`, light and dark, contrast measured (3.1 / 3.4)
**Repo:** 22 Gradle modules + `build-logic` · 4 sample features + `template` · 10 scripts

| Phase | Goal | Done | Progress |
|---|---|---|---|
| 0 · Truth and small defects | Docs match code; the defects found in review are fixed | 14 / 14 | `██████████` 100% |
| 1 · Build foundation | Cheaper to build and to change; stable toolchain | 8 / 8 | `██████████` 100% |
| 2 · App shell and session | What a real app needs on day one, on Navigation 3 | 8 / 8 | `██████████` 100% |
| 3 · Design system and accessibility | A theme and components worth copying | 2 / 8 | `██░░░░░░░░` 25% |
| 4 · Testing and quality | Regression coverage that costs nothing to keep | 0 / 4 | `░░░░░░░░░░` 0% |
| 5 · Shipping baseline | Flavors, signing, crash reporting, perf | 0 / 8 | `░░░░░░░░░░` 0% |
| 6 · Data layer | Network and offline, once there is a real API | 0 / 2 | `░░░░░░░░░░` 0% |
| 7 · Backlog | Parked items, kept so they are not forgotten | 0 / 16 | `░░░░░░░░░░` 0% |
| **Total** | | **32 / 68** | `█████░░░░░` 47% |

**Now:** 3.4 (primitives: Button, TextField, Checkbox).
**Next:** 3.3 (migrate the `.dp` literals onto the new role-based scale), then 3.6, 3.7, 3.8.
**Blocked on a decision:** nothing. All ten decisions were made on 2026-09-08; see below.

### Scope

This is an Android project. The Kotlin is the work; `scripts/` exists to make the repetitive parts
of it fast, and that is all it is for. So:

- **No new scripts.** An item that would add one goes to the backlog instead. `doctor.py` and the
  generators are the set.
- **Existing scripts change only when something else forces them to** — a convention moved, a
  generated file's shape changed, a new `--graph` name. That change is part of the item that caused
  it, not an item of its own.
- Items 4.5 and 6.3 were moved to the backlog on 2026-09-08 under this rule.

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
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew ktlintCheck && ./gradlew build
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
| Route arguments | the route key itself | A constructor parameter on the ViewModel, handed over by the destination with `koinViewModel { parametersOf(key) }` |
| `AlertPayload`, `SystemEvent.AlertResult` | `state/AlertState.kt`, `event/SystemEvent.kt` | Typed confirm-then-act; see `SettingsViewModel` |
| `rememberPermissionRequest(vararg)` | `service/core/ui/.../permission/` | `status` + `request()`; re-read on resume |
| `PermissionGate(…) { }` | same | Composes content only while granted — no boolean for a caller to forget |
| `Context.openAppSettings()` | same | The one implementation; `UiCommand.OpenAppSettings` uses it too |
| `AppTheme.colors` | `core/ui/theme/Color.kt` | Semantic roles: `surfaceBase/Raised/Sunken`, `textPrimary/Secondary/Tertiary`, `confirm`/`destructive`/`info`/`warning`/`neutral` (each `bg`/`edge`/`label`/`container`/`onContainer`), `border`, `focusRing` |
| `AppTheme.typography` | `core/ui/theme/Type.kt` | Nine roles, two densities. Never a `sp` literal |
| `AppTheme.shapes` | `core/ui/theme/Shape.kt` | `none`/`xs`/`sm`/`md`/`lg`/`xl`/`sheet`/`pill`; `nested(parent, gap)` for a child's radius |
| `Modifier.keySurface(...)` | `core/ui/theme/Elevation.kt` | The edge-plus-travel press effect. Not `Modifier.shadow` — the edge has to stay sharp |
| `AppTheme.motion` | `core/ui/theme/Motion.kt` | press 70 ms, toggle 140 ms, sheet 220/180 ms, screen 260 ms |
| `AppTheme.density` | `core/ui/theme/Density.kt` | `SizeClass` + `minTouchTarget`. 48 dp on touch, 56 dp on a till |
| `AppTheme.spacing` | `core/ui/theme/Spacing.kt` | Role-based: `inset` / `stack` / `inline`, each xs…xl. Not yet used (item 3.3) |
| `MainDispatcherRule` | `testFixtures(projects.service.core.ui)` | Added by `convention.feature.presentation`; `:app` declares it itself |
| `FakeLogger` | `testFixtures(projects.service.core.domain)` | Re-exported by `:service:core:ui`'s fixtures, so one line still gets both |
| `FakeAuthService` | `testFixtures(projects.feature.auth.domain)` | The auth, settings and `MainViewModel` tests; `sessionError` fails the session flow |
| `SessionState` | `app/SessionState.kt` | `Unknown` / `SignedIn` / `SignedOut`, owned by `MainViewModel`; nothing else switches flows |
| `appModules(isDebug)` | `core/di/Koin.kt` | The one module list; `initKoin` starts it, `KoinGraphTest` verifies it |
| `coreModule(isDebug)` | `core/di/Koin.kt` | WARN-and-above logger in release |
| `execute(loadingMessage = …)` | `BaseViewModel` | Wording for the overlay; survives overlapping calls |
| `DispatcherProvider` / `DefaultDispatcherProvider` | `service/core/domain/coroutines/` | Switch at the data source, not the repository |
| `convention.*` plugins | `build-logic/src/main/kotlin/` | A module build file is a `plugins` block and its project dependencies, nothing else |
| `ProjectConfig` | same | `minSdk`, `compileSdk`, `targetSdk`, Java version, app version. One edit each |

**Gotchas already paid for:**

- ~~A screen with nav arguments needs Robolectric.~~ Gone with 2.8: Navigation 3 hands the route
  key to the ViewModel directly, so there is no `Bundle` to decode and every ViewModel test is a
  plain JVM test. Robolectric is out of `convention.feature.presentation` and nothing uses it —
  item 4.3 will add it back for the screen tests.
- Koin's `verify()` cannot see a `parametersOf` argument, so a screen with route arguments needs one
  line in `KoinGraphTest`'s `injectedParameters`. That is the sixth registration;
  `create_screen.py --with-args` writes it and `doctor.py` check 20 fails if it is missing.
- `rememberSceneSetupNavEntryDecorator` is internal in navigation3 1.1.7 — `NavDisplay` adds it
  itself. Pass only the saveable-state and ViewModel-store decorators.
- **`rememberNavBackStack` must be composed on the first frame.** It is a `rememberSaveable`, and
  one that first enters composition on a later frame gets nothing back from the restored state.
  Gating it on anything asynchronous — the session, a feature flag, a loaded config — throws the
  saved back stack away on every process death, silently and only on a real device. Remember it
  unconditionally (empty if need be) and gate the `NavDisplay` instead. Found in 2.1.
- `Module.mappings` is `@KoinInternalAPI`, so the route-key injections cannot be derived by walking
  the graph. They are listed.
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
- **D4 · Convention plugins in `build-logic/`?** Declined once; 24 identical build blocks and the
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
- **D11 · Adopt the KSD design system, or generate a neutral palette?** D10 chose neutral because a
  template is re-skinned per project. An existing, finished system — five levelled ramps, a nine-role
  type scale, a shape and elevation scale, all with light/dark values already decided — is better
  than anything generated from a seed, and re-skinning it is still one file (`Ramp.kt`).
  **Outcome (2026-09-08): adopt it, three layers and all.** D10 stands for the *shape* of the
  result — standard Material 3, mapped from the roles — not for inventing a palette. Items 3.1, 3.2.
- **D12 · Keep `dynamicColor`?** It was `true`, so the app took its colours from the wallpaper.
  The status roles (paid / open / void) carry meaning, and a wallpaper-derived scheme destroys them.
  **Outcome (2026-09-08): removed, not defaulted off.** A parameter nobody should pass is not an
  option worth keeping. Item 3.1.

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

- [x] **1.1 Convention plugins in `build-logic/`** (L) · 2026-09-08
  Why: 24 build files repeat the same `plugins` / `namespace` / `compileSdk` / `lint` block, and
  every presentation module repeats the same twelve dependency lines. `minSdk`, Java target and
  lint config are 24 edits. `export_service.py` ships this project's SDK levels into the next one.
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
  property instead of 24 lines. A feature presentation build file ends up as:

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
  Landed, with four differences:
  - **Java stays at 11.** Item 1.4 says "one edit once 1.1 lands", which is only true if 1.1 does
    not already make it. `ProjectConfig.JAVA_VERSION` is that one edit.
  - **`convention.feature.data` and `convention.feature.di` are applied outside `feature/` too** —
    to `:service:core:{domain,data}` and `:core:di`, whose shape is identical. Two near-duplicate
    plugins would have been worse than two slightly misleading names.
  - **The compose plugin adds the Compose BOM and bundle as `api`, not `implementation`**, because
    `:core:ui` and `:service:core:ui` re-export them on purpose and would otherwise have to repeat
    the same three lines. Item 1.5 revisits it.
  - **`convention.feature.presentation` also adds Robolectric.** `create_screen.py --with-args`
    generates a Robolectric test into any feature and edits no build file, so before this the
    generator's promise held only in the two features that happened to declare it.
  Also: the plugins apply AGP and the Kotlin plugins through the catalog
  (`libs.findPlugin("android-library")`) rather than by literal id, which is what lets
  `export_service.py --sync-versions` see them; and it exports every `convention.*` alias, not only
  the two the service modules apply. The new doctor check is 19, "no module build file repeats the
  shared Android configuration". No unit-test options were lifted into the plugin: no module set
  any, so there was nothing to share.

- [x] **1.2 Domain modules are plain Kotlin JVM** (M) · 2026-09-08
  Why: `:service:core:domain` and every `feature/*/domain` are Android libraries that by rule
  contain no `android.*`. As `kotlin("jvm")` modules the rule is enforced by the compiler, there
  is no manifest, AAR or lint pass per module, and the build is faster.
  Done: `org.jetbrains.kotlin.jvm` in the catalog and root; the five domain modules converted;
  `doctor.py`'s Android-free check kept as a second line; generators and template updated.
  Landed: four domain modules, not five — `home`, `settings` and `launch` are screen-only, and the
  fifth was `gateway`, which 1.8 removed. `convention.kotlin.jvm` gained `api(kotlinx-coroutines-core)`
  (a domain repository returns `Flow`), so `:service:core:domain`'s build file is now the plugins
  block and nothing else. It also needed Kotlin's `jvmTarget` set: an Android module inherits it
  from `compileOptions` and merely warns, a Kotlin/JVM module inherits nothing and fails against
  the JDK 25 daemon — `configureKotlinJvmTarget()` is now shared by both plugins. The generators
  needed no change: they clone `feature/template`, whose build files are the new shape.

- [x] **1.3 Toolchain: stable AGP, fresh dependencies, Renovate** (M) · 2026-09-08
  Why: AGP is an alpha. coroutines 1.9.0, Koin 4.0.4, serialization 1.7.3, lifecycle 2.9.1 and
  navigation 2.9.0 lag Compose BOM 2026.02 and Kotlin 2.2.10. A template should start current.
  Done: latest stable AGP 9.x; every catalog entry on the latest stable compatible with the BOM;
  `renovate.json` grouping androidx, kotlin and koin with the version catalog manager enabled;
  build green; lint's version checks stay informational.
  Landed: AGP `9.5.0-alpha04` to `9.4.0`, Kotlin `2.2.10` to `2.4.20`, Compose BOM `2026.02.01` to
  `2026.08.00`, coroutines to `1.11.0`, serialization to `1.11.0`, Koin to `4.2.2`, lifecycle to
  `2.11.0`, navigation to `2.10.0`, activity-compose to `1.13.0`, core-ktx to `1.19.0`, datastore to
  `1.2.1`, turbine to `1.2.1`, mockk to `1.14.11`. Robolectric and JUnit were already on the latest
  stable — 4.17 is still in beta. AGP gets its own Renovate group rather than sharing androidx's,
  because it is the one that breaks the build DSL. The bump surfaced one deprecation: Koin 4.2 sets
  the Compose context up in `startKoin()`, so `MainActivity`'s `KoinContext { }` wrapper is gone.
  Nothing else needed a source change.

- [x] **1.4 Java target 11 to 17** (S) · 2026-09-08
  Why: the daemon runs JDK 25, AGP 9 requires 17 to run, and 17 is the current baseline. One edit
  once 1.1 lands.
  Done: `compileOptions` and Kotlin `jvmTarget` at 17 in the convention plugin; build green.
  The one edit is `ProjectConfig.JAVA_VERSION` in `build-logic/src/main/kotlin/ProjectConfig.kt`.
  Landed: it was one edit, plus three lines making Kotlin's `jvmTarget` explicit rather than
  inherited from `compileOptions` — the two drifting apart is a warning most builds never surface.
  Verified on the emitted bytecode: class file major version 61.

- [x] **1.5 `api` vs `implementation` hygiene** (S) · 2026-09-08
  Why: 43 `api(` lines in feature build files, most of them in `di` modules re-exporting layers,
  and `:app` reaches `AppTheme` and `Screen()` only because `:core:di` has `api(projects.core.ui)`.
  Done: `:app` depends on `core.ui` directly; `di` modules use `implementation` except where a
  type is exposed; the Dependency Analysis Gradle plugin runs as `./gradlew buildHealth` (advisory,
  not failing) with its findings fixed once.
  Landed: `:app` declares `:core:ui` and `:feature:auth:domain` rather than reaching them through
  `:core:di`. A feature's `di` module keeps `api` for its `presentation` only — the destinations are
  the surface `:app` builds the nav graph from — and uses `implementation` for `domain` and `data`.
  `convention.feature.di` makes Koin `api`, since the `XModule.module` a `di` module exists to
  publish is a Koin `Module`.
  What was **not** taken from the report, deliberately: it advises moving every
  `:feature:*:presentation` into `:app`'s own dependencies and dropping `api` from `:core:di`. That
  is a truer graph but a sixth registration point per feature, for no build-avoidance — `:app`
  would depend on those modules either way. `:core:di` is the aggregation point on purpose. It also
  advises replacing the bundle-and-BOM declarations with one line per transitively-used artifact in
  every module, which would undo the version catalog's bundles. `CLAUDE.md` records both categories
  as expected output so the next reader does not "fix" them.
  The plugin has to be applied to the subprojects explicitly (root-only application produced no
  reports), and warns that 9.4.0 is past the AGP range it is tested against.

- [x] **1.6 Re-test detekt 2.x and ktlint on JDK 25** (S) · 2026-09-08
  Why: Plan 1's I3. The earlier failure was version-specific (Robolectric 4.16 works where 4.14
  did not). If the current release reads JDK 25 bytecode, we get formatting and a few rules for
  free.
  Done: outcome recorded in `CLAUDE.md` and the root `build.gradle.kts` comment. If it works, a
  small rule set plus `detekt` in CI. If not, ktlint CLI as its own CI step on its own JDK.
  Landed: **detekt still fails, ktlint works as a Gradle plugin** — better than the fallback, so no
  separate CI step and no second JDK. detekt 1.23.8 (the current release; 2.x is `2.0.0-alpha`)
  refuses `--jvm-target 25`, and pinning that to 17 only moves the failure to its embedded
  compiler choking on the JDK's version string. ktlint-gradle 14.2.0 runs on the JDK 25 daemon.
  The rule set is `.editorconfig` — no second config file — on `intellij_idea` style rather than
  `ktlint_official`, with four rules disabled: `class-signature`, `function-signature` and
  `parameter-list-spacing` all read a multi-line parameter list as if it were on one line (572
  violations became 20 once they were off), and `function-naming` cannot know a `@Composable` is
  PascalCase or that a test name is a backtick-quoted sentence. The remaining 20 —
  `statement-wrapping`, `import-ordering`, one unused import — were fixed by `ktlintFormat`.
  `ktlintCheck` is a CI step ahead of the build, and part of the gate. Applying the two new
  plugins with `subprojects { }` made Gradle materialise a build directory for `:feature` and the
  other path-only projects, which `doctor.py` then read as a feature named `build`; the block now
  skips a project with no build file, and `feature_names()` ignores `build` either way.

- [x] **1.7 Shared test fixtures instead of copies** (S) · 2026-09-08
  Why: `FakeLogger` exists three times (`service/core/data` tests, `service/core/ui` fixtures,
  inline in `BaseViewModelTest`). `FakeAuthService` exists twice (`RecordingAuthService` in
  settings tests). The code promises "plan item F6" that Plan 1 never listed.
  Done: `FakeLogger` in `testFixtures` of `:service:core:domain` (where `Logger` lives);
  `:service:core:ui` fixtures keep `MainDispatcherRule`; `FakeAuthService` in `testFixtures` of
  `:feature:auth:domain`, used by auth and settings tests; the copies deleted.
  Landed: `:service:core:ui` takes the domain fixtures with `testFixturesApi`, so a screen test
  still needs the one `testFixtures(projects.service.core.ui)` line the convention plugin adds and
  no module gained a second. The merged `FakeAuthService` is the union of the two it replaces — the
  auth copy's `failWith` and `loggedInEmails`, the settings copy's settable `session` and
  `logoutCount`. Both `domain` modules apply `java-test-fixtures`; on a Kotlin/JVM module that is
  the plugin rather than AGP's `testFixtures { enable = true }`.
  The stale "plan item F6" promise in the auth fixture's KDoc is gone with it.

- [x] **1.8 Merge `gateway` into `data`** (M) · 2026-09-08
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
  Landed: the new check is check 18, "no repository imports a data source implementation" — it
  reads the imports of any `*Repository.kt` under `feature/*/data`, so it also covers a repository
  written by hand rather than generated. `export_service.py`'s `SERVICE_LAYER_ORDER` and
  `_common.py:FEATURE_TREE_COLUMN` (57 to 50, the tree entries being shorter) went with it. The
  repo is 24 Gradle modules and 35 source sets, down from 27 and 38.

## Phase 2 · App shell and session

Goal: the template demonstrates what every real app needs in week one: a proper splash, a session
switch without hacks, bottom navigation, transitions, permissions, all on Navigation 3.

Order: **2.8 first** (or together with 2.1 and 2.2, since both touch the navigation host). 2.3
and 2.4 are built on Navigation 3; do not build tab graphs on Navigation 2 and migrate them later.
2.5, 2.6 and 2.7 are independent.

- [x] **2.1 SplashScreen API replaces the `launch` feature and the 2 s hold** (M) · 2026-09-08 · D2 decided: yes
  Why: `LaunchScreen` is a spinner, `MainViewModel` delays two seconds so it is visible, and
  `MainActivity` composes an invisible `Screen()` over the nav host just to receive the graph
  switch. `androidx.core.splashscreen` with `setKeepOnScreenCondition { session is Unknown }`
  does the same job with no delay and no extra feature.
  Done: `feature/launch` deleted via `delete_feature.py`; the navigation host is composed only
  once the session is known, starting in the matching flow (auth or main); a later session change
  replaces the whole back stack; `Screen.isTransparent` removed; `themes.xml` uses
  `Theme.SplashScreen`.
  Landed. `androidx.core:core-splashscreen` **1.2.0**, the current stable. The manifest names
  `Theme.AndroidProject1.Starting` on the activity and `postSplashScreenTheme` points back at the
  real theme, so the launcher window is the splash and `installSplashScreen()` swaps it.
  One difference from the Done line, and it is the reason this item was worth running on a device:
  - **The back stack is remembered on the first frame and starts empty**, rather than the whole
    host being composed only once the session is known. `rememberNavBackStack` is a
    `rememberSaveable`; composing it behind the session dropped the saved back stack on every
    process death — see the new gotcha above. So it is always remembered, the session fills it,
    and `AppNavHost` is composed once it is non-empty. Same "nothing before the flow is known"
    behaviour, without the loss.
  Verified on an emulator (API 37): cold start signed in lands on Home with no spinner and no hold;
  cold start signed out lands on Sign in; Log out replaces the whole stack with Sign in; back walks
  Croissant → Bakery → Categories → Home; and killing the process four screens deep and relaunching
  comes back on the product detail. This last one **fails** on the version of this item that
  composed the host conditionally, and passes on the one that shipped — the regression was found
  and fixed here, not shipped. Predictive back's animation was not exercised.

- [x] **2.2 `MainViewModel` is a plain `ViewModel` with a `SessionState`** (S) · 2026-09-08 · D2 decided: yes
  Why: it is not a screen. Today it subclasses `BaseViewModel` with a state nothing renders and a
  `sessionKnown` flag to work around that.
  Done: `sealed interface SessionState { Unknown; SignedIn; SignedOut }` exposed as `StateFlow`;
  a read failure after retries maps to `SignedOut` and a WARN log instead of a modal over nothing;
  `MainNavigation`, `MainEvent`, `MainState` deleted; `KoinGraphTest` green.
  Landed. Done ahead of 2.1 because the splash's keep-on-screen condition is this item's
  `SessionState.Unknown`. Two things beyond the Done line, both needed for it to be true:
  - **`MainActivity` applies the session by asserting an invariant**, not by remembering whether it
    has already navigated: the flow the user is in *is* the first key on the back stack, so a
    `LaunchedEffect` replaces the stack when the session's root key and `backStack.first()` disagree
    and does nothing when they match. The old `sessionKnown` flag had no equivalent, and the
    obvious replacement — switch on the first non-`Unknown` value — would discard a back stack
    restored after process death. Still true of the launch screen this commit keeps; 2.1 removes it.
  - **`MainViewModelTest`, and the `testFixtures(...)` lines in `app/build.gradle.kts` it needs.**
    `convention.android.application` is not `convention.feature.presentation`, so `:app` had no
    `MainDispatcherRule`. `FakeAuthService` grew a `sessionError` property — kept apart from
    `failWith`, so a test of a failing sign-in still reads a session — since nothing could make the
    session flow fail before.

- [x] **2.3 Bottom navigation with nested graphs** (M) · 2026-09-08
  Why: Plan 1's D1. The first thing a real project adds and the one thing the template does not
  show. `NavigationSuiteScaffold` gives a rail on tablets for free.
  Done: Home, Catalog and Settings as top-level destinations on Navigation 3 (after 2.8); each tab
  keeps its own back stack and survives process death; `create_feature.py --graph <tab>` registers
  the entry under that tab; `doctor.py` still finds every destination; the `homeDestination`
  lambdas for settings and catalog are removed.
  Landed. `material3-adaptive-navigation-suite`, versioned by the Compose BOM already applied.
  **How the per-tab back stacks work, because it is the decision worth knowing:** the tabs do not
  each own a list. The back stack *is* their concatenation, in the order the tabs were last visited,
  and a tab's key is the only thing that starts a segment — so `currentTab` is the last tab key on
  the stack and `selectTab` moves that tab's segment to the end. Push and pop are untouched, backing
  out of a tab root lands on the tab underneath it, and process-death survival is free: it is the one
  list `rememberNavBackStack` already saves, so there is no `Saver` for a map of lists to write or to
  get wrong. The alternative — a `TopLevelBackStack` holding a map, as the Nav3 recipes do — needs
  that `Saver` and buys nothing here.
  Two consequences beyond the Done line:
  - **Settings lost its up arrow**, with `SettingsEvent.NavigateUpClicked` and
    `SettingsNavigation.NavigateUp`. A tab root has nothing to go up to.
  - **`--graph` gained the three tabs** (`home`, `catalog`, `settings`) beside `main` and `auth`, and
    `mainEntries()` became three per-tab blocks. `main` still means the signed-in flow as a whole and
    is still the default, so nothing generated before changes shape.
  Also: the sample app now has **no cross-feature navigation lambda left** — the three jumps it had
  are tabs — so `CLAUDE.md`'s recipe for one no longer points at `homeDestination`. `HomeScreen` is
  the greeting alone, and `HomeEvent` / `HomeNavigation` are empty interfaces.
  Verified on an emulator (API 37): the bar shows only in the signed-in flow; a tab switch keeps the
  screens left on the other tab (Catalog three deep, away to Settings, back to Catalog, still on the
  product detail); process death four screens deep restores both the stack and the tab; backing out
  of the Catalog root lands on Settings, the tab visited before it; logging out replaces everything
  with the bar-less sign-in screen.

- [x] **2.4 Shared enter/exit transitions** (S) · 2026-09-08
  Why: Plan 1's D4. Default cross-fade looks unfinished; one shared spec on the host is cheap.
  Done: one shared `transitionSpec` / `popTransitionSpec` on the `NavDisplay` (after 2.8); tab
  switches fade, pushes slide; predictive back animates.
  Landed. `slideIntoContainer` / `slideOutOfContainer` rather than raw offsets, so the direction
  follows the layout direction and RTL is right for free.
  **Tab switches fade through entry metadata, not through the host's spec**, and that is the part
  worth remembering. `NavDisplay` resolves `NavEntry.metadata` against the screen *arriving* on a
  push and the one *leaving* on a pop — exactly the rule this needs, and one the host's spec cannot
  express, because from inside it a pop to `CategoriesDestination` and a switch to the Catalog tab
  look identical. So a tab root carries fade specs and everything else slides. `AppNavHost` attaches
  them by wrapping the entry the provider returns, so no feature knows it is a tab.
  `NavEntry.key` is private and `defaultContentKey` is internal, so a spec cannot recover the
  `NavKey` behind a `Scene` — the wrap has the key and the metadata route does not need it.
  Predictive back is left at the library's default (fade plus scale-out): it is the platform's own
  gesture and should look like it does everywhere else.
  Verified on an emulator (API 37) with `animator_duration_scale 10` to catch mid-transition frames:
  a push through the catalog shows the outgoing screen offset horizontally, and a tab switch shows
  Home cross-fading in over the product detail with no offset at all. The predictive-back gesture
  itself was not exercised.

- [x] **2.5 Permissions, redesigned** (L) · 2026-09-08
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
  Landed. Three differences, all deliberate:
  - **`PartiallyGranted`, not `Granted.Partial`.** As a subtype, `status is Granted` would be true
    for a partial grant — which is the same class of footgun the item exists to remove. A caller
    happy with a subset now has to name the case.
  - **The optional slot is on `PermissionGate`, not on `PermissionRationale`.** A data class holding
    a composable lambda is neither comparable nor stable; the rationale stays plain data (so a
    ViewModel could build one) and `PermissionGate(denied = …)` is where a composable belongs.
  - **The Permissions screen reads the platform in composition and hands it to the ViewModel as an
    event** (`PermissionsRead`). Permission state lives outside the app and changes while it is
    backgrounded, so there is nothing a ViewModel could observe — only something the UI can re-read
    on resume. That is also why item **7.13** stays parked: it is the alternative, not a gap.
  `rememberDeclaredPermissions()` reads the *merged* manifest, which is worth more than a hand-written
  list — the emulator shows a `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` that no source file here
  mentions. `PackageInfoFlags.of` is API 33 and `minSdk` is 29, so `declaredPermissions()` carries
  both spellings until that floor moves.
  `PermissionGate` and `PermissionRationale` ship unused: nothing in the sample app has content worth
  gating. They are API for the app built on this, like `UiText.Plural`.
  Verified on an emulator (API 37), four passes, with `dumpsys package` read after each to confirm
  the platform agreed: **grant** → chip flips to Granted and the prompt disappears; **deny once**
  (`USER_SET`) → "Allow" is still offered; **deny again** (`USER_FIXED`) → "Allow" is gone and only
  "Open system settings" remains; and **granted from outside while backgrounded** → the chip is
  right again on resume, which is the `LifecycleResumeEffect` doing its job.

- [x] **2.6 Snackbar action is a `SystemEvent`, not a lambda in a command** (S) · 2026-09-08
  Why: `UiCommand.ShowSnackbar.onAction` is a function inside a data class, the one command that is
  not plain data. Alerts already solve this with `AlertResult(id)`.
  Done: `ShowSnackbar(id, message, actionLabel, …)` and `SystemEvent.SnackbarAction(id)` handled
  in `onSystemEvent`; `Screen()` routes it; one test.
  Landed as written. `showSnackbar` takes `id` last with a `SNACKBAR_ID_DEFAULT`, so the common
  call — a snackbar with no action — is unchanged. `BaseViewModel.onSystemEvent` ignores the event
  by default: only the screen that raised the snackbar knows what its action means. Two tests, not
  one: the second is that an id nobody handles is ignored rather than mistaken for an alert result.
  Nothing in the app raises an actionable snackbar yet, so this is API only.

- [x] **2.7 `UiText.Plural`** (S) · 2026-09-08
  Why: quantity strings are a week-one need and `UiText` cannot express them today.
  Done: `UiText.Plural(id, quantity, args)` resolving through `getQuantityString`; a `toUiText`
  overload; one test with a `Resources` fake or Robolectric.
  Landed with one difference, and it matters: **the builder is `toPluralUiText`, not another
  `toUiText` overload.** As an overload, `R.string.x.toUiText(count)` would bind to the plural one —
  a non-vararg parameter beats a vararg — and a string resource would be read as a plural at
  runtime, silently, in code that compiled yesterday. The name is longer and the trap is gone.
  Tests use a mockk `Resources` rather than Robolectric, so `:service:core:ui` stays free of it
  (see the Robolectric gotcha above). Three of them, including that the quantity is not also a
  format argument — `getQuantityString` requires it twice and that surprises everyone once.

- [x] **2.8 Migrate to Navigation 3** (L) · 2026-09-08
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
  Landed. The versions the item asked to check first: **navigation3 1.1.7** (stable; 1.2.0 is beta)
  and **lifecycle-viewmodel-navigation3 2.11.0**, matching the lifecycle version already here.
  Koin's own `koin-compose-navigation3` was **not** used: it registers destinations in the Koin
  graph, which would move the one place that knows about more than one feature out of `AppNavHost`.
  Plain `koinViewModel()` resolves against the entry's ViewModelStore, and `parametersOf(key)`
  passes the route key.
  Differences from the Done line:
  - **`AuthNavGraph` and `MainNavGraph` are deleted, not converted.** Navigation 3 has no nested
    graphs. `AppNavHost` groups the entries into `authEntries()` and `mainEntries()`, which is what
    `--graph` now writes into, and `MainActivity` switches flows by replacing the back stack.
  - **A sixth registration appeared**, and could not be avoided: see the `KoinGraphTest` gotcha
    above. `doctor.py` check 20 covers it.
  - **Robolectric came back out of `convention.feature.presentation`**, reversing part of 1.1's
    Landed note — the reason it was there was the `Bundle` decoding this item removes.
  - Navigation 2 is gone from the catalog with it: `navigation-compose`, `navigationCompose` and
    `koin-androidx-compose-navigation` (and so from the `koin-android` bundle).
  Not verified on a device: process-death restoration and predictive back are asserted by
  `rememberNavBackStack`'s contract, not by a test here. Worth a manual pass with 2.1.
  Since done: 2.1's device pass confirms process-death restoration four screens deep. Predictive
  back is still unexercised.

## Phase 3 · Design system and accessibility

Goal: the theme is something a project keeps rather than replaces on day one, and the sample
screens are built from shared components. Order matters: 3.1 before 3.4, 3.2 before 3.4.

- [x] **3.1 A real colour scheme** (M) (2026-09-08)
  Why: Plan 1's G2. `Color.kt` is the wizard's purple with three roles; `dynamicColor = true`
  means the app never looks like itself.
  Done: full M3 role set (primary/secondary/tertiary, surface, error, outline) in light and dark
  from one seed, neutral by design; `AppTheme(dynamicColor = false)` default; every
  `@ScreenPreview` checked in dark. Standard Material 3, not Expressive (D10).
  **Landed:** not generated from a seed. The palette is imported from the KSD design system
  (`claude.ai/design`, project `KotlinProject1`), whose foundations document already defines five
  perceptually levelled ramps and a semantic role table with exact light/dark values. `Ramp.kt`
  holds the ramps as layer 1 and is `internal`, so no screen can name a ramp step; `AppColors` in
  `Color.kt` is layer 2 and the only layer that differs between the themes. `dynamicColor` is gone
  rather than defaulted off — see D12. M3's scheme is derived from the roles by
  `toColorScheme()`, so stock Material components fit without a wrapper.

- [x] **3.2 Full type scale** (S) (2026-09-08)
  Why: `Type.kt` defines `bodyLarge` only; everything else falls back to Material defaults.
  Done: the M3 scale defined explicitly with `FontFamily.Default`, so a brand font is one edit.
  **Landed:** nine roles rather than M3's fifteen slots — `displayXl` … `numericMd`, from the same
  design system — in two densities, `compactTypography()` and `regularTypography()` (the compact
  scale × 1.15). `toTypography()` maps the nine onto Material's fifteen. `AppFontFamily` is the
  one edit for a brand face; the system's own face is Source Sans 3, not bundled (item 3.8).
  Monetary and numeric roles carry `tnum`.

- [ ] **3.3 Spacing scale actually used** (S)
  Why: Plan 1's G1-finish. `AppTheme.spacing` exists; 28 `.dp` literals remain in screens.
  Done: literals migrated; `doctor.py` check 18 flags a bare `.dp` literal in a feature screen
  (allow-list for `1.dp` dividers and hairlines).

- [~] **3.4 Component set** (M)
  Why: Plan 1's G4. Sample screens hand-roll buttons, text fields and headers.
  Done: `AppButton`, `AppTextField` (with error state), `AppTopBar`, `AppListItem`, `EmptyState`
  (wrapping `ContentMessage`), `Skeleton`, each generated with `create_component.py`, each with
  a `@ComponentPreview`; sample screens use them.
  **In flight (2026-09-08):** the three the design system calls primitives are in —
  `AppButton` (six kinds × three sizes, the edge-and-travel press, loading that does not change
  the width), `AppTextField` (sunken ground, four states, an error that always carries text) and
  `AppCheckbox` (row-sized target, indeterminate only for a group toggle). All three take their
  colours as *roles*, never as values, so a re-brand does not touch them.
  `SettingsScreen` is the first to use them, which is also how the set was verified on a device.
  Still open: `AppTopBar`, `AppListItem`, `EmptyState`, `Skeleton`, and the remaining sample
  screens. That last part overlaps 3.3 and should land with it.
  **Contrast, measured on an emulator (2026-09-08):** base `#F7F7F6` / `#1A1A17` as specified;
  outline border 4.32:1 light and 3.76:1 dark, label 16.27:1 in both. `borderStrong` had to move
  to `Gray500` on both sides to clear the 3:1 the system requires of an element border — a step of
  the surface does not.
  **Known, for 3.6:** Material's own `OutlinedButton` draws its border from `outlineVariant`, which
  is mapped to the quiet divider hairline (1.57:1) — correct for a divider, too weak for a button.
  Any sample screen still on a stock `OutlinedButton` under-contrasts until it moves to `AppButton`.
  `textTertiary` is `Gray500` in both themes, the value the source system gives it: 4.32:1 light
  and 3.76:1 dark, just under the 4.5:1 body-text threshold. It is used for placeholders and
  missing values. 3.6 decides whether to keep the source value or raise it.

- [ ] **3.5 `AppImage` over Coil 3** (S)
  Why: Plan 1's G5. Features should not import an image library directly.
  Done: one composable in `core/ui` with placeholder and error states; Coil is `implementation`
  in `core/ui` only; `doctor.py` flags `coil` imports in features.

- [ ] **3.6 Accessibility pass** (M)
  Why: Plan 1's G7. Lint's `ContentDescription` and `ClickableViewAccessibility` are warnings.
  Done: every icon has a description or is marked decorative; 48 dp touch targets; a `testTag`
  convention documented and used by 4.3; a font-scale 1.5 variant in `@ScreenPreview`; the two
  lint rules raised to `error`.

- [ ] **3.8 Bundle the brand face** (S)
  Why: 3.2 defines the scale against `FontFamily.Default`. The design system's own face is Source
  Sans 3, chosen for a high x-height and a `1` distinguishable from `l` on a tilted tablet.
  Done: the four weights the scale asks for (400/600/700/800) in `core/ui/src/main/res/font/`,
  `AppFontFamily` pointing at them, APK size delta recorded here. Downloadable fonts considered
  and rejected or taken, with the reason.

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

- [-] **4.5 Generator output compiles** (M)
  Why: `test_scripts.py` checks text, not compilation, and the docs admit it. A template change
  that breaks generated code is found by the next user, not by CI.
  Done: an opt-in test (`--with-gradle`) that runs `create_feature.py` in the temp copy and then
  `./gradlew :feature:x:presentation:compileDebugKotlin`; run in CI only.
  Moved to the backlog 2026-09-08 as 7.15: Python tooling is not where the effort goes (see the
  note under Scope below).

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

- [-] **6.3 `create_datasource.py --remote`** (S)
  Why: once 6.1 exists, the generator should produce the network-backed variant, not only
  DataStore.
  Done: flag emits a Ktor-backed `DefaultRemoteXDataSource`; tested in `test_scripts.py`.
  Moved to the backlog 2026-09-08 as 7.16, for the same reason as 4.5.

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
- [ ] **7.15 Generator output compiles** (M) · was 4.5. An opt-in `test_scripts.py --with-gradle`
  that runs `create_feature.py` in the temp copy and compiles the result.
- [ ] **7.16 `create_datasource.py --remote`** (S) · was 6.3. Needs 6.1 first either way.
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
