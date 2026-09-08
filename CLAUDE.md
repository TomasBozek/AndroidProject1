# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Work in progress:** [docs/PLAN.md](docs/PLAN.md) is the single plan for this template — what is
done, what is next, and the API and gotchas a cold start needs. Read it before picking up work.
Keep it current as items land; there is deliberately no second copy anywhere.

## Project

Multi-module Android app (Kotlin + Jetpack Compose), base package `com.example.androidproject1`. It is a
**template**: the structure and conventions matter more than the five sample features. Architecture is
modelled on a layered Clean/MVI setup — single activity, type-safe Compose navigation, Koin DI.

- `minSdk = 29`, `targetSdk = compileSdk = 37`, Java 17 — all in `build-logic`'s `ProjectConfig`
- AGP `9.5.0-alpha04`, Kotlin `2.2.10`, Gradle `9.6`, Compose BOM `2026.02.01`
- Dependencies come from `gradle/libs.versions.toml` — never hardcode a version in a module build file.
  Compose artifacts come from the BOM without an explicit version.
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
| `convention.kotlin.jvm` | `org.jetbrains.kotlin.jvm`, Java target, JUnit and coroutines-test. Written for the `domain` modules; nothing applies it yet (plan item 1.2) |
| `convention.feature.data` | android library plus coroutines |
| `convention.feature.di` | android library plus the Koin BOM and bundle |
| `convention.feature.presentation` | the compose library plus serialization, Koin, navigation, lifecycle, the `testing` bundle, Robolectric and `testFixtures(:service:core:ui)` |
| `convention.android.application` | `:app`: `com.android.application`, the app identity, R8 on release, `lint.checkDependencies` |

The **namespace is derived** from the project path and `basePackage` in `gradle.properties`:
`:feature:auth:presentation` becomes `<base>.feature.auth.presentation`, `:service:core:ui` becomes
`<base>.service.core.ui`, `:app` becomes `<base>`. A module that sets `namespace` itself keeps it —
nothing does today. `init_project.py` therefore rewrites one property rather than one line per
module, and `applicationId` comes from the same property.

`ProjectConfig` in `build-logic/src/main/kotlin/` holds `minSdk`, `compileSdk`, `targetSdk`, the
Java version and the app's version code and name. Changing `minSdk` is one edit.

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
                      event/UiCommand, text/UiText, util/CollectEffect, error strings

:core:ui              this app's Compose theme + the @ScreenPreview/@ComponentPreview helpers
:core:di              initKoin() + coreModule — the single Koin registration point

:app                  single activity, AppNavHost, MainViewModel, Application

:feature:auth:{domain,data,presentation,di}       full stack; owns the session
:feature:catalog:{domain,data,presentation,di}    full stack; three screens, one with args
:feature:home:{presentation,di}                   screen only
:feature:launch:{presentation,di}                 screen only; the splash held by MainViewModel
:feature:settings:{presentation,di}               screen only; reads :feature:auth:domain
:feature:template:{domain,data,presentation,di}   what the generators clone
```

`service/` holds **reusable** modules — the architecture, with no knowledge of this app's features,
theme or DI graph. Reuse is by directory copy: drop `service/core` into a new project, add the three
`includeServiceModule` lines, then create that project's own `core/` and `feature/`. So keep it
self-contained — never reference `:core:*`, `:feature:*` or `:app`, and never read `R` from elsewhere.

Package names under `service/` stay `com.example.androidproject1.core.*`; only the Android
**namespaces** move to `...service.core.*`, because two modules cannot share a namespace and
`:core:ui` keeps `...core.ui`. Hence the `...service.core.ui.R` imports in `:service:core:ui`.

`:core:ui` re-exports `:service:core:ui` with `api(...)`, so a feature's `presentation` module still
depends on nothing but `projects.core.ui` and gets both the theme and the architecture. `Previews.kt`
lives here rather than in `service` because it references `AppTheme`.

Two rules keep `service/` portable, and both are load-bearing:

- **`:service:core:domain` stays free of `android.*`.** The `Logger` *interface* lives there;
  `AndroidLogger` lives in `:service:core:data`. Don't reintroduce a framework import into domain.
- **`:service:core:ui` sets `resourcePrefix = "core_"`,** so every string it ships is `core_*` and
  cannot silently collide with a consuming app's. New resources there must carry the prefix.

The service modules have JVM unit tests (`src/test/kotlin`) covering `Outcome`, `BaseRepository` and
`BaseViewModel`. They need no Robolectric — `R.string.x` is only an `Int` and `UiText` defers
resolution — so keep it that way and don't pull the framework in.

Layer dependency directions:

| Layer | Depends on |
|---|---|
| `domain` | `api(projects.service.core.domain)` only |
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
navigation is passed in as a lambda and wired in `AppNavHost` (see `homeDestination`). Depending on
another feature's `domain` is fine — `:feature:settings:presentation` does exactly that for `AuthService`.

`:feature:template` is compiled but unused; it is the source the generator scripts clone. Keep it working.

## Screen structure (the unit of work)

Every screen is seven files — six in one package, plus its test in the matching test package:

| File | Role |
|---|---|
| `XDestination.kt` | `@Serializable` route + `NavGraphBuilder.xDestination()`; gets the VM and wires `Screen()`, passing `onNavigation` |
| `XScreen.kt` | Stateless `XScreen(state, onEvent)` + a private `@ScreenPreview` composable |
| `XState.kt` | `data class XState(...)` with a `companion object { val PREVIEW }` |
| `XEvent.kt` | `sealed interface XEvent : UiEvent` — what the user did |
| `XNavigation.kt` | `sealed interface XNavigation` — one-off navigation intents |
| `XViewModel.kt` | `BaseViewModel<XState, XEvent, XNavigation>` |
| `XViewModelTest.kt` | in `src/test/kotlin`; uses `MainDispatcherRule` + `FakeLogger` from `testFixtures(projects.service.core.ui)` |

`XState.PREVIEW` is required — it is the preview fixture and usually the value passed as
`initialState`.

## MVI conventions

- ViewModels expose `state: StateFlow<UiState<State?>>`; the UI sends events via `onUiEvent(event)`.
- **`BaseViewModel` takes `initialState` first.** Pass the state the screen renders straight away and
  it starts with no overlay; pass `null` only when the screen cannot render until something loads,
  which also starts the overlay. Never write `init { uiState.update { ... loading = null } }` — one
  forgotten line there strands a screen behind a permanent spinner.
- `UiState(data, loading, alert)` is an envelope. **Loading overlays and alert dialogs are rendered
  centrally by `Screen()`** — never reimplement them in a feature screen.
- `Screen()` is the only place that calls `collectAsStateWithLifecycle` and the only interpreter of
  `UiCommand` (toast, back, browser, app settings). A feature screen only ever receives a non-null state.
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
- `MainViewModel` is the single owner of session state and the only thing that switches nav graphs.
  Screens change the session and let it react — do not navigate between the auth and main graphs directly.
- Strings reachable from a ViewModel are `UiText` (`R.string.x.toUiText()`), so no Context is needed;
  strings used only in a composable use `stringResource(...)`. Each feature's `presentation` module owns
  its `res/values/strings.xml` — no hardcoded literals. See `HomeState.greeting`.
- **A screen without a `Scaffold` pads itself with `.safeDrawingPadding()`.** The activity is edge to
  edge and `Screen()` applies no insets, so `Scaffold`-based screens are not padded twice.

## DI (Koin)

Each feature has `object XModule { val module = module { ... } }` in its `di` module, using
`viewModelOf(::XViewModel)` and `singleOf(::DefaultX) bind X::class`. Modules are registered in
`core/di/.../Koin.kt`, and `:core:di` must have an `api(projects.feature.x.di)` dependency for that to
compile. Both edits are made automatically by `create_feature.py`.

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

1. Replace the placeholder `val counter: Int` in `UserProfileState` and its `PREVIEW` with the real state.
   `PREVIEW` is not optional — it is the preview fixture and normally the `initialState`.
2. Write `UserProfileScreen.kt`. Strings used only in the composable go through `stringResource(...)`;
   strings a ViewModel needs go into the state as `UiText` (`R.string.x.toUiText()`). Both live in
   the feature's own `res/values/strings.xml`, prefixed `user_profile_`.
3. Add cases to `UserProfileEvent`, handle them in `UserProfileViewModel.onUiEvent`, and emit
   `UserProfileNavigation` to move on. The `CollectEffect` block in `UserProfileDestination.kt` is
   generated empty — that is where a navigation intent turns into a `navController` call.

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
demonstrates the argument-carrying route: an `@Serializable data class` route, a ViewModel taking a
`SavedStateHandle` and reading `navArgs<XDestination>()`, and a Robolectric-annotated test (route
decoding goes through an `android.os.Bundle`). Bare `--with-args` gives one `id: String`. Supported
types are `String`, `Int`, `Long`, `Boolean`, `Float`, `Double`.

Do not hand-convert a `data object` route into a `data class` — that is what produced a screen
loading from a `LaunchedEffect` instead of its `SavedStateHandle`.

Note that `create_feature.py` deliberately skips the `TemplateArgs*` files: a new feature starts
with one screen, and copying the second would leave an unregistered destination behind.

Writes the six-file unit, adds `user_profile_detail_*` strings to the feature's `strings.xml`, registers
the ViewModel in the feature's Koin module and the destination in `AppNavHost.kt`. Then follow steps
1–3 above. For a screen that takes an argument, turn its `@Serializable data object XDestination` into
a `data class` and read it from the nav entry.


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

### Navigating to another feature

Not scripted, and deliberately: a `presentation` module must never depend on another feature's
`presentation` (`doctor.py` fails if it does). Add a lambda parameter to the destination function and
wire it in `AppNavHost.kt`, the way `homeDestination` reaches settings:

```kotlin
userProfileDestination(
    navController = navController,
    navigateToSettings = { navController.navigate(SettingsDestination) },
)
```

Switching between the auth and main graphs is different again: change the session and let
`MainViewModel` react. Do not navigate across graphs from a screen.

### Removing things

`delete_feature.py` removes a whole feature and all five registrations. There is no script for a single
screen: delete its six files, its `user_profile_detail_*` strings, the `viewModelOf(::XViewModel)` line
and the two `AppNavHost.kt` lines. Run `doctor.py` afterwards — it catches every one of those if you
miss it.

### Changing what gets generated

`feature/template` **is** the template — `create_feature.py` and `create_screen.py` clone it verbatim
and only rewrite names. To change the shape of every future feature or screen, edit `feature/template`,
not the scripts. It is included in `settings.gradle.kts` precisely so `./gradlew build` keeps it
compiling; keep it that way.

### Before you call the work done

```bash
python3 scripts/doctor.py && ./gradlew build
```

Add `python3 scripts/test_scripts.py` if you touched anything under `scripts/`.

## Scaffolding scripts

Prefer these over copying files by hand; they also perform the registration steps that are easy to
forget. Every one of them supports `--dry-run`. `scripts/_common.py` holds the shared naming rules,
paths and the idempotent file-editing helpers — put anything used by two scripts there.
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
generate are stripped from the generated build files. `--graph main|auth|none` picks the nav graph.

```bash
python3 scripts/create_screen.py userprofile UserProfileList --sub overview
```

Clones the six-file screen unit into an existing feature, renames the template's `template_*` strings to
`user_profile_list_*` and merges them into the feature's `strings.xml`, registers the ViewModel in the
feature's Koin module and the destination in `AppNavHost.kt`. With `--sub` the generated files get an
explicit `import ...presentation.R`, because `R` lives in the module's namespace package and a
sub-package is no longer part of it.

```bash
python3 scripts/create_component.py PrimaryButton
python3 scripts/create_component.py ProductCard --feature catalog --state
```

Writes a Compose component with a `modifier` parameter and a `@ComponentPreview`. With no
`--feature` it lands in `:core:ui`, where every feature can reach it; `--feature` keeps it to one.
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
Android-free domain layer, the `core_` resource prefix, the six-file screen unit, `XState.PREVIEW`, an
`init` block that clears `loading`, cross-feature `presentation` dependencies, a repository importing a
data source implementation, module registration in `settings.gradle.kts`, ViewModel/Koin/AppNavHost
registration, the module tree above matching the `feature/` directories on disk, a module build file
repeating the shared Android configuration, and hardcoded dependency coordinates.
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
./gradlew :app:assembleDebug
```

```bash
./gradlew installDebug
```

```bash
./gradlew test
```

```bash
./gradlew lint
```

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py
```

There is no detekt/ktlint: detekt 1.23 embeds a Kotlin compiler that cannot read the JDK 25 the daemon
is pinned to, and running it on a separate toolchain fights AGP 9's plugin ordering. Formatting comes
from `.editorconfig`; revisit when detekt 2.x is stable.

Build a single module, e.g. `./gradlew :feature:auth:presentation:assembleDebug` or
`./gradlew :service:core:ui:assembleDebug`.

## Known constraints

- `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon to JDK 25 (auto-provisioned via foojay).
  A machine without it and without network access will fail before configuring; lower `toolchainVersion`
  if that happens.
- AGP is a pre-release (`9.5.0-alpha04`), so the `compileSdk { version = release(37) }` and
  `optimization { enable = false }` block DSL is AGP-9-only and will not work on AGP 8.x.
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
