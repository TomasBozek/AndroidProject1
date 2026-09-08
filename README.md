# AndroidProject1

A multi-module Android template — Kotlin, Jetpack Compose, Koin, single activity, type-safe
navigation. It exists to be copied: the structure and the conventions are the product, and the
five sample features are there to demonstrate them.

If you are an agent working in this repo, read [CLAUDE.md](CLAUDE.md) instead — it is the rulebook.
This file is the orientation. [docs/PLAN.md](docs/PLAN.md) is what is being worked on next.

## Start a project from it

```bash
python3 scripts/doctor.py   # 17 checks; should pass on a clean tree
./gradlew build
```

Then make it yours — one command, on a clean tree:

```bash
python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker"
```

It rewrites the base package across every source file, moves the package directories in all 35
source sets, and renames the Gradle project, the Android theme, the launcher label and
`scripts/_common.py` so the other generators keep working. Add `--dry-run` to see the plan first.
It refuses to run on a dirty working tree, so `git checkout .` stays an escape hatch.

## How it is laid out

```
build-logic/                    the convention.* plugins — an included build, not a module
service/core/{domain,data,ui}   the reusable architecture — knows nothing about this app
core/{ui,di}                    this app's theme and its single Koin registration point
app                             one activity, AppNavHost, MainViewModel
feature/<name>/{domain,data,presentation,di}
```

The split between `service/` and `core/` is the one that matters. `service/` is portable: drop the
directory into a new project, add three `includeServiceModule` lines, and you have the architecture
without any of this app's identity. `scripts/export_service.py` does that copy, brings `build-logic/`
along and rewrites the package for you.

A module build file is a `plugins` block and its project dependencies, and nothing else:

```kotlin
plugins { alias(libs.plugins.convention.feature.presentation) }

dependencies {
    api(projects.core.ui)
    api(projects.feature.auth.domain)
}
```

SDK levels, the Java target, lint and every shared library dependency live in `build-logic/`. So does
the namespace, derived from the project path and `basePackage` in `gradle.properties` — which is why
`init_project.py` renames one property rather than one line per module.

Layer direction, enforced by `doctor.py`:

| Layer | May depend on |
|---|---|
| `domain` | `service:core:domain` only — a plain Kotlin/JVM module, so `android.*` will not compile |
| `data` | `service:core:data` + own `domain` — holds `DefaultXRepository` and both halves of the data source |
| `presentation` | `core:ui` + own `domain` |
| `di` | all of the above |

`data` is split into two packages: `repository` holds `DefaultXRepository`, `source` holds the
`XDataSource` interface and its `DefaultXDataSource` implementation. The repository depends on the
interface and never on the implementation, which is what lets you swap a network source for a cache
without touching anything above it — and `doctor.py` fails if it does.

## A screen is seven files

`XDestination`, `XScreen`, `XState`, `XEvent`, `XNavigation`, `XViewModel`, `XViewModelTest`. Generate
them; do not write them by hand, because five registrations (`settings.gradle.kts`,
`core/di/build.gradle.kts`, `Koin.kt`, `AppNavHost.kt` and the module tree in `CLAUDE.md`) are easy
to forget and `doctor.py` will fail on every one you miss.

`create_screen.py` and `create_feature.py` generate all seven, and `doctor.py` fails if one is
missing. `MainDispatcherRule` and `FakeLogger` come from `testFixtures(projects.service.core.ui)`,
so no module writes its own.

One wrinkle worth knowing before you add a screen with navigation arguments: `toRoute()` decodes
through an `android.os.Bundle`, so its test needs Robolectric —
`@RunWith(RobolectricTestRunner::class)` and `@Config(sdk = [34])`, as
`ProductsViewModelTest` does. Screens without arguments stay on the plain JVM. Note the version
floor: Robolectric 4.14 cannot read this build's JDK 25 bytecode and 4.16 can.

| I need | Command |
|---|---|
| a feature, full stack | `python3 scripts/create_feature.py userProfile` |
| a screen-only feature | `python3 scripts/create_feature.py userProfile --layers presentation,di` |
| another screen | `python3 scripts/create_screen.py userprofile UserProfileDetail` |
| a screen with route arguments | `python3 scripts/create_screen.py userprofile Detail --with-args 'id:String'` |
| a shared UI component | `python3 scripts/create_component.py PrimaryButton` |
| a data source | `python3 scripts/create_datasource.py userprofile LocalUserProfile --repository` |
| to undo a feature | `python3 scripts/delete_feature.py userProfile` |
| to check conventions | `python3 scripts/doctor.py` |

See [scripts/README.md](scripts/README.md) for the full set, or `--help` on any of them.

In Claude Code these are also slash commands — `/new-feature`, `/new-screen`, `/new-component`,
`/new-datasource`, `/check`, `/rename-project` — which carry the follow-up steps with them.

Install the pre-commit hook once per clone, so a missed registration fails in seconds rather than
in CI:

```bash
python3 scripts/install_hooks.py
```

## The parts worth knowing before you write a ViewModel

- `BaseViewModel(initialState, logger, savedStateHandle)`. Pass the state the screen can render
  immediately; pass `null` only when it genuinely cannot render until something loads, which also
  starts the loading overlay.
- **Never write try/catch.** `execute {}` for one-shot calls, `observe(flow = …) {}` for flows. Both
  drive the loading state, turn an `Outcome.Failure` into an alert, and re-throw cancellation.
- **Loading overlays, alert dialogs and empty/error states are rendered by `Screen()`**, centrally.
  So are toasts, snackbars, back presses, navigation intents and the route to app settings. A
  feature screen never sees a `Context`, and a destination never writes a collector.
- **Pick where a failure goes.** `execute(errorDisplay = ErrorDisplay.Inline)` for the call that
  loads a screen — it shows a retryable message in place of the content, and the retry button
  re-runs the failed call for you. The default, `ErrorDisplay.Alert`, is right for a call the user
  triggered on a screen that is already drawn.
- Navigation arguments arrive through `navArgs<XDestination>()`, decoded from the route. They are
  available in `init` and survive process death.
- Strings a ViewModel needs are `UiText` (`R.string.x.toUiText()`); strings only a composable needs
  use `stringResource(...)`. No literals — lint fails on `HardcodedText`.

## Where this architecture comes from

The core of it — `BaseViewModel`, `UiState`, `Screen()`, `BaseRepository`, the `Outcome` type and
the six-file screen convention — grew out of a production Android client I worked on, and the debt
is worth stating plainly rather than leaving implicit.

What is different here, and why:

| Change | Reason |
|---|---|
| `service/` vs `core/` split, and `export_service.py` | The original had no portability story; reuse meant copying files and fixing imports by hand. |
| Python generators + `doctor.py` (17 checks) | The original had two bash scripts and no verification. Conventions that nothing checks decay. |
| Buffered channels for navigation and commands | The original used `MutableSharedFlow`, which silently drops anything emitted while the UI is below `STARTED`. |
| `initialState` as the first constructor parameter | The original always started from `UiState(data = null)`, so every screen began behind a spinner whether it needed to or not. |
| Reference-counted loading | The original's boolean let a short call dismiss a long call's overlay. |
| `observe(retries = …)` | A flow that throws is terminal. Without retries, one transient read error kills a session flow for the process lifetime. |
| `AlertPayload` instead of `Map<String, Any>` | Confirm handlers should not cast out of a map. |
| `AlertState.title` with no default | The original defaulted every dialog title to the error title, so confirmations read "something went wrong". |
| No `KoinComponent` in the base class | Everything is constructor-injected, so a ViewModel's dependencies are visible in its signature. |

The general shape — layered modules, MVI, a `Result` wrapper, DI, type-safe navigation — is
standard Android practice and is documented in Google's architecture guide and Now in Android.

## Commands

```bash
./gradlew build            # compile, lint and test everything
./gradlew :app:installDebug
./gradlew test
python3 scripts/doctor.py && python3 scripts/test_scripts.py
```

CI runs `doctor.py` and `test_scripts.py` before the Gradle build, so a convention violation fails
fast.
