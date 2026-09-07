# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Multi-module Android app (Kotlin + Jetpack Compose), base package `com.example.androidproject1`. It is a
**template**: the structure and conventions matter more than the three sample features. Architecture is
modelled on a layered Clean/MVI setup — single activity, type-safe Compose navigation, Koin DI.

- `minSdk = 29`, `targetSdk = compileSdk = 37`, Java 11
- AGP `9.5.0-alpha04`, Kotlin `2.2.10`, Gradle `9.6`, Compose BOM `2026.02.01`
- Dependencies come from `gradle/libs.versions.toml` — never hardcode a version in a module build file.
  Compose artifacts come from the BOM without an explicit version.
- AGP 9 applies Kotlin itself; there is no `kotlin-android` plugin. New plugins must be declared in the
  root `build.gradle.kts` with `apply false` before a module can `alias(...)` them.

## Module structure

Modules are registered in `settings.gradle.kts` through `includeServiceModule` / `includeCoreModule` /
`includeFeatureModule` helpers, which fail at settings time if a module's directory or build file is
missing. Type-safe project accessors are enabled, so dependencies read `api(projects.core.ui)`.

There are three top-level groups, and the split between the first two is the important one:

```
:service:core:domain  DataResult, DomainException hierarchy, Logger (interface only)
:service:core:data    BaseRepository, DataStoreProvider, AndroidLogger
:service:core:ui      BaseViewModel, UiState, Screen(), CommandEffect, AppString, error strings

:core:ui              this app's Compose theme + the @ScreenPreview/@ComponentPreview helpers
:core:di              initKoin() + coreModule — the single Koin registration point

:app                  single activity, AppNavHost, MainViewModel, Application

:feature:auth:{domain,infrastructure,data,presentation,di}   full stack
:feature:home:{presentation,di}
:feature:settings:{presentation,di}
:feature:example:{domain,infrastructure,data,presentation,di} template for the generators
```

`service/` holds **reusable** modules — the architecture, with no knowledge of this app's features,
theme or DI graph. The intended reuse story is a directory copy: drop `service/core` into a new
project, add the three `includeServiceModule` lines, and only then create that project's own `core/`
and `feature/`. So keep it self-contained — a `service` module must never reference `:core:*`,
`:feature:*` or `:app`, and must not read `R` from anywhere but itself.

Package names under `service/` deliberately stay `com.example.androidproject1.core.*` (they are not
renamed to match the module path); only the Android **namespaces** move to
`com.example.androidproject1.service.core.*`, because two modules cannot share a namespace and
`:core:ui` keeps `...core.ui`. That is why `BaseViewModel` and `UiState` import
`com.example.androidproject1.service.core.ui.R`.

`:core:ui` re-exports `:service:core:ui` with `api(...)`, so a feature's `presentation` module still
depends on nothing but `projects.core.ui` and gets both the theme and the architecture. `Previews.kt`
lives here rather than in `service` because it references `AppTheme`.

Two rules keep `service/` portable, and both are load-bearing:

- **`:service:core:domain` stays free of `android.*`.** The `Logger` *interface* lives there;
  `AndroidLogger` lives in `:service:core:data`. Don't reintroduce a framework import into domain.
- **`:service:core:ui` sets `resourcePrefix = "core_"`,** so every string it ships is `core_*` and
  cannot silently collide with a consuming app's. New resources there must carry the prefix.

The service modules have unit tests (`src/test/kotlin`) covering `DataResult`, `BaseRepository` and
`BaseViewModel`. They run on the JVM without Robolectric — `R.string.x` is only an `Int` and
`AppString` defers resolution — so keep them that way and don't pull the Android framework in.

Layer dependency directions:

| Layer | Depends on |
|---|---|
| `domain` | `api(projects.service.core.domain)` only |
| `infrastructure` | `service:core:data` + own `domain`; holds `DefaultXRepository` and the `XDataSource` **interfaces** |
| `data` | own `domain` + own `infrastructure`; holds the `DefaultXDataSource` **implementations** |
| `presentation` | `api(projects.core.ui)` + own `domain` |
| `di` | `api(...)` of all of the above |

Note the inversion: a data source's *interface* lives in `infrastructure` and its *implementation* in
`data`, so `data` depends on `infrastructure`, not the reverse. Naming is rigid: `Foo` (interface) ↔
`DefaultFoo` (implementation) at every layer.

A feature's `presentation` module must never depend on another feature's `presentation`. Cross-feature
navigation is passed in as a lambda and wired in `AppNavHost` (see `homeDestination`). Depending on
another feature's `domain` is fine — `:feature:settings:presentation` does exactly that for `AuthService`.

`:feature:example` is compiled but unused; it is the source the generator scripts clone. Keep it working.

## Screen structure (the unit of work)

Every screen is six files in one package:

| File | Role |
|---|---|
| `XDestination.kt` | `@Serializable` route + `NavGraphBuilder.xDestination()`; gets the VM, wires `Screen()` and `CommandEffect` |
| `XScreen.kt` | Stateless `XScreen(state, onEvent)` + a private `@ScreenPreview` composable |
| `XState.kt` | `data class XState(...)` with a `companion object { val PREVIEW }` |
| `XEvent.kt` | `sealed interface XEvent : Event` — what the user did |
| `XDirection.kt` | `sealed interface XDirection` — one-off navigation intents |
| `XViewModel.kt` | `BaseViewModel<XState, XEvent, XDirection>` |

`XState.PREVIEW` is required — it is the preview fixture and usually the value passed as
`initialState`.

## MVI conventions

- ViewModels expose `state: StateFlow<UiState<State?>>`; the UI sends events via `onUiEvent(event)`.
- **`BaseViewModel` takes `initialState` as its first constructor argument.** Pass the state the
  screen renders straight away and it starts with no loading overlay; pass `null` only when the
  screen genuinely cannot render until something loads, which also starts the overlay. Do not
  reintroduce an `init { uiState.update { ... loading = null } }` block — that pattern is what made a
  forgotten line strand a screen behind a permanent spinner.
- `UiState(data, loading, alert)` is an envelope. **Loading overlays and alert dialogs are rendered
  centrally by `Screen()`** — never reimplement them in a feature screen.
- `Screen()` is the only place that calls `collectAsStateWithLifecycle` and the only interpreter of
  `CommonUiCommand` (toast, back, open URI). A feature screen only ever receives a non-null state.
- **Use `domainCall {}` rather than try/catch.** It runs the call, drives the loading state, converts
  `DataResult.Error` into an alert, and rethrows cancellation. Pass `loading = {}` to suppress the modal
  spinner when a screen renders its own inline loading. Overlapping calls are reference-counted, so the
  overlay stays up until the last one finishes.
- `direction` and `commonUiCommand` are buffered channels, not shared flows, so a one-shot event
  emitted while nothing is collecting is delivered on resume rather than dropped. They are
  single-consumer by design — one collector per screen.
- `AlertState.title` has no default; `BaseViewModel`'s error paths supply the error title themselves,
  so an ordinary confirmation dialog is not labelled "something went wrong".
- Alert results arrive at `onCommonEvent` as `CommonEvent.AlertDialogAction.*` tagged with the alert's
  `id`; delegate anything you don't handle to `super`. See `SettingsViewModel` for the confirm-then-act
  pattern.
- `MainViewModel` is the single owner of session state and the only thing that switches nav graphs.
  Screens change the session and let it react — do not navigate between the auth and main graphs directly.
- Strings reachable from a ViewModel use `AppString` (`R.string.x.toText()`), so no Context is needed;
  strings used only inside a composable use `stringResource(...)`. Each feature's `presentation` module
  owns its `res/values/strings.xml` — no hardcoded literals, see `HomeState.greeting` for the
  `AppString`-in-state pattern.
- **A screen without a `Scaffold` pads itself with `.safeDrawingPadding()`.** The activity is edge to
  edge and `Screen()` deliberately applies no insets, so that `Scaffold`-based screens are not padded
  twice.

## DI (Koin)

Each feature has `object XModule { val module = module { ... } }` in its `di` module, using
`viewModelOf(::XViewModel)` and `singleOf(::DefaultX) bind X::class`. Modules are registered in
`core/di/.../Koin.kt`, and `:core:di` must have an `api(projects.feature.x.di)` dependency for that to
compile. Both edits are made automatically by `create_feature.py`.

## Scaffolding scripts

Prefer these over copying files by hand; they also perform the registration steps that are easy to forget.

```bash
python3 scripts/create_feature.py chatRoom --layers domain,presentation,di
```

Clones `feature/example`, rewrites names, registers the modules in `settings.gradle.kts`, adds the
`:core:di` dependency and the Koin module entry. Dependencies on layers you did not generate are stripped
from the generated build files. Supports `--dry-run` and `--force`.

```bash
python3 scripts/create_screen.py chatroom ChatRoomList --sub overview
```

Clones the six-file screen unit into an existing feature, registers the ViewModel in the feature's Koin
module, and prints the line to add to `AppNavHost.kt`.

```bash
python3 scripts/export_service.py --to ~/Projects/android/MyNewApp --package com.acme.myapp
```

Copies `service/` into another project, rewriting `com.example.androidproject1` in sources, directory
layout and namespaces, then prints the `includeServiceModule` block to paste into the target's
`settings.gradle.kts`. This is the other half of the reuse story — `service/` is portable by
construction, but its package still has to be changed. Supports `--dry-run`, `--force` and `--modules`.

Renaming caveat: the base package is `com.example.androidproject1`, which itself contains the word
`example`. `create_feature.py` / `create_screen.py` therefore use narrow, targeted replacements
(`feature.example`, `feature/example`, `Example`, `example` followed by an uppercase letter) rather than
a blanket rename. Preserve that if you edit them — see `scripts/_common.py:rewrite_source`.
`export_service.py` is the deliberate exception: moving the whole base package is the point there, and
`service/` contains no other occurrence of the word.

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
- There are no convention plugins: every library module repeats its own `plugins`/`namespace`/`compileSdk`
  block. This is deliberate but costly — changing `minSdk` means editing every module, and a `service/`
  module copied into another project carries this one's SDK levels with it. If the module count grows,
  move this into an included `build-logic` build; the build files are kept identical in shape so that
  migration stays a find/replace.
- `flowRepositoryCall`'s error is terminal — a `Flow` that has thrown can only be resubscribed, not
  resumed. A flow whose collector outlives the failure (session state, say) must pass `retries`, or one
  transient I/O error stops it emitting for as long as the collector lives. See
  `DefaultAuthRepository.observeSession()`.
