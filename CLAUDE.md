# CLAUDE.md

Guidance for Claude Code (claude.ai/code) in this repository.

**This file is the rules.** Everything else is one link away from [docs/README.md](docs/README.md),
read when a task names it. A fact lives in exactly one file — write it twice and one copy is already
wrong.

**Work** is [docs/ai/PROCESS.md](docs/ai/PROCESS.md) — one agent, ids, points, lanes, the task
loop — and the plan under `docs/ai/plans/` whose header says `Status: open`. Take a task with
`/task <id>`, never by picking something that looks useful. Work you find on the way is one line in
[docs/BACKLOG.md](docs/BACKLOG.md).

## Project

Multi-module Android app (Kotlin + Compose), base package `com.example.androidproject1`, and a
**template**: the structure and conventions matter more than the sample features. Layered
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

## Modules

`build-logic/` is an included build holding the `convention.*` plugins every module applies:
**library dependencies live in the plugin, project dependencies stay in the module.** The namespace
is derived from the project path and `basePackage` in `gradle.properties`, so `init_project.py`
rewrites one property rather than one line per module.

**The module tree, the layer table and the convention-plugin table are
[docs/ai/CODEBASE.md](docs/ai/CODEBASE.md)** — open it before adding a module, a layer or a plugin.
Five rules bite before you get there:

- **`:service:core:domain` stays free of `android.*`.** A Kotlin/JVM module, so the compiler
  enforces it — move the class rather than making it an Android library.
- **`service/` never references `:core:*`, `:feature:*` or `:app`**, and never reads `R` from
  elsewhere: it is reused by directory copy. Directory, package and namespace agree (D49).
- **A feature's `presentation` never depends on another feature's `presentation`** — cross-feature
  navigation is a lambda wired in `AppNavHost`. Another feature's `domain` is fine.
- **`data` splits in two**: `repository` holds `DefaultXRepository`, `source` holds the
  `XDataSource` interface *and* its `DefaultXDataSource`. A repository imports the interface.
- **`:feature:template` is what the generators clone** — keep it working.

**A release is a tag, not an edit.** `git tag v1.2.0 && git push origin v1.2.0` — `versionName` is
the tag without its `v`, `versionCode` is `git rev-list --count HEAD`; any build not on a `v*` tag
is 1 / `"1.0"`.

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
`create_component.py`** and give it a `@ComponentPreview`; the generator writes its gallery entry
and `doctor.py` fails without one (D52). Elevation is not `Modifier.shadow`: a pressable surface
uses `Modifier.keySurface(…)`, a hard bottom edge that shortens on press.

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
  `Tile`, `Key`, `Dialog`, `Sheet`, `Tab`, `Badge`, `Value`, `Card`, `Empty`, `Skeleton`,
  `Progress`, `Group` — eighteen words, a kind of element and never the component that draws it
  (D60). So `"settings_permissionsButton"`; a stepper in a form is `"cart_quantityField"`.
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
- **`BaseViewModel` takes `initialState` first.** Pass the state the screen renders straight away;
  pass `null` only when it genuinely cannot draw until something loads. `null` raises no overlay of
  its own (D44) — the call that is waiting asks for one.
- **Write state with `updateData { copy(...) }`**, the protected member: it logs an update that
  lands while `data` is still `null` rather than dropping it in silence.
- `UiState(data, loading, alert)` is an envelope. **Loading overlays and alert dialogs are rendered
  centrally by `Screen()`** — never reimplement them in a feature screen. What they look like is
  `AppScreenChrome`, installed by `AppTheme` (D50).
- `Screen()` is the only place that calls `collectAsStateWithLifecycle` and the only interpreter of
  `UiCommand`. A feature screen only ever receives a non-null state. Every command is plain data: a
  snackbar's action comes back as `SystemEvent.SnackbarAction(id)`, handled in `onSystemEvent`.
- **Use `execute {}` (one-shot) and `observe(flow = …) {}` (flows) rather than try/catch.** They
  turn `Outcome.Failure` into an alert and rethrow cancellation. **The overlay is opt-in**: pass
  `loading = overlay()`, or `overlay(message)` to word it. Overlapping calls are reference-counted.
- `navigation` and `command` are buffered channels, not shared flows, so a one-shot emitted while
  nothing collects arrives on resume rather than being dropped. Single-consumer by design.
- **A form with unsaved input guards back**: `DiscardBackHandler(dirty) { … }` in the screen,
  `setAlert(discardAlert())` in the view model, `ALERT_ID_DISCARD` in `onSystemEvent`.
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

The table is [docs/ai/CODEBASE.md](docs/ai/CODEBASE.md) § API you build on: `execute`/`observe`,
`ErrorDisplay`, `ContentState`, `AlertPayload`, `UiCommand`, `Formats`, `DispatcherProvider`,
`Aead`, `ErrorTracker`, `Analytics`, the retry policy, `SessionState`, `appModules`, the fixtures
and `ProjectConfig`. Read it before writing something that is already there.

## DI (Koin)

Each feature has `object XModule { val module = module { ... } }` in its `di` module, using
`viewModelOf(::XViewModel)` and `singleOf(::DefaultX) bind X::class`. Modules are registered in
`core/di/.../Koin.kt`, and `:core:di` must have an `api(projects.feature.x.di)` dependency for that to
compile. Both edits are made automatically by `create_feature.py`.

## Recipes

Start here for any new code. **Do not create these files by hand.** The scripts perform the five
registrations a manual copy silently skips — `settings.gradle.kts`, `:core:di`'s build file,
`Koin.kt`, `AppNavHost.kt` and the module tree in `docs/ai/CODEBASE.md` — and `doctor.py` fails on
the ones you forget.
Extend a script rather than working around it.

| I need | Command |
|---|---|
| to turn this template into a real project | `python3 scripts/init_project.py --package com.acme.app --name "My App" --author "Acme"` |
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
| **T4** | a `v*` tag, and weekly | the release build, the generator compile, the end-to-end flows on `devDebug` and a launch of `prodRelease` |

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

- **One agent works a release**, lane by lane. A second or third only when the plan's `Agents:`
  line says so, and no subagents or workflow scripts inside a session unless the owner asks (D61).
- `/task <id>` takes the first `[ ]` line in your lane; the branch is the lane's,
  `<letter><lane>-<slug>`, and every task of the lane is one commit on it.
- A `Decide first` line is settled before the code, as a row in `docs/DECISIONS.md`.
- With more than one agent, touch only the files your lane owns. Alone, there is nothing to
  arbitrate.
- A fact you changed moves to its one doc in the same commit — `docs/ai/PROCESS.md` § Which doc
  changes when.
- **One commit** per task, titled `<id> <title>`, carrying the code, the docs and the board line
  flipped to `[x]` with `· est → act`.
- One pull request per lane, opened with the template on its first task;
  `gh pr merge --rebase --delete-branch` once the lane is done and green, so `main` stays one
  commit per task (D17).

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

- `observe`'s error is terminal — a `Flow` that has thrown can only be resubscribed, not
  resumed. A flow whose collector outlives the failure (session state, say) must pass `retries`, or one
  transient I/O error stops it emitting for as long as the collector lives. See
  `DefaultAuthRepository.observeSession()`.
- **`rememberNavBackStack` must be composed on the first frame.** It is a `rememberSaveable`, and
  one that first enters composition on a later frame gets nothing back from the restored state.
  Gating it on anything asynchronous — the session, a flag, a loaded config — throws the saved
  back stack away on every process death, silently and only on a real device. Remember it
  unconditionally (empty if need be) and gate the `NavDisplay` instead. `MainActivity` shows it.

The build- and tooling-shaped ones — the JDK 25 daemon pin, the AGP-9-only DSL, the `compileOnly`
plugin declarations, `configureAndroid`'s property getters, Koin's `verify()` and `parametersOf`,
Robolectric's missing `AndroidKeyStore` and its JDK 25 bytecode support, and
`variant.enableUnitTest` — are [docs/ai/CODEBASE.md](docs/ai/CODEBASE.md) § Known constraints.
Read it before changing anything under `build-logic/`.
