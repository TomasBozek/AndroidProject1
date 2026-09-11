# Codebase

The module tree and the convention plugins: which modules exist, what each one is allowed to depend
on, and which `convention.*` plugin supplies its libraries. Below them, the API that is already
built — the table to read before writing something that exists.

The rules that govern all of it are [../../CLAUDE.md](../../CLAUDE.md); what the modules *contain*,
rather than how they are wired, is [reference/](reference/): `CORE.md`, `DOMAIN.md`, `FEATURES.md`,
`SERVICES.md`, `DESIGN-SYSTEM.md`.

## Convention plugins

`build-logic/` is an included build holding the `convention.*` plugins every module applies:
**library dependencies live in the plugin, project dependencies stay in the module**, so a typical
build file is `plugins { alias(libs.plugins.convention.feature.presentation) }` and two `api(...)`
lines.

| Plugin | Applies |
|---|---|
| `convention.android.library` | `com.android.library`, SDK levels, Java target, shared `lint.xml`, derived namespace. No dependencies |
| `convention.android.library.compose` | the above plus the Compose compiler, `buildFeatures.compose`, the BOM and the `compose-core` bundle |
| `convention.core.ui` | the compose library plus Coil. `:core:ui`'s own plugin, and Coil's one consumer — no other Compose module gets it |
| `convention.kotlin.jvm` | Kotlin/JVM, Java target, coroutines, JUnit, coroutines-test. Every `domain` module |
| `convention.feature.data` | android library plus coroutines, the `testing` bundle, `testFixtures(:service:core:domain)`, MockEngine and Robolectric |
| `convention.feature.di` | android library plus the Koin BOM and bundle |
| `convention.feature.presentation` | the compose library plus serialization, Koin, Navigation 3's ViewModel decorator, lifecycle, the `testing` bundle and `testFixtures(:service:core:ui)` |
| `convention.android.library.testfixtures` | turns on AGP's `src/testFixtures/` for the module — stacked on top, for the rare module that ships a `Fake*` for another module's tests |
| `convention.android.application` | `:app`: app identity, R8 on release, `lint.checkDependencies` |

The **namespace is derived** from the project path and `basePackage` in `gradle.properties`, so
`init_project.py` rewrites one property rather than one line per module; `applicationId` comes from
the same property. `doctor.py` fails if a module build file sets `compileSdk`, `minSdk`,
`targetSdk`, `compileOptions` or a `lint` block.

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
:feature:trips:{domain,data,presentation,di}      full stack
:feature:template:{domain,data,presentation,di}   what the generators clone
```

`service/` holds **reusable** modules — the architecture, with no knowledge of this app's features,
theme or DI graph. Reuse is by directory copy, so keep it self-contained: never reference `:core:*`,
`:feature:*` or `:app`, and never read `R` from elsewhere. **Directory, package and namespace agree**
— `service/core/ui` is `...service.core.ui` in all three (D49), so an import says which module a
symbol came from and no package is split across two. `:core:ui` re-exports `:service:core:ui` with
`api(...)`, so a feature's `presentation` depends on nothing but `projects.core.ui`. Two rules keep
it portable, and both are load-bearing:

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

## API you build on (do not reinvent)

Read this before writing something that exists. A plan holds only open work, so it cannot.

| Thing | Where | Note |
|---|---|---|
| `execute {}` / `observe(flow = …) {}` | `service/core/ui/.../BaseViewModel.kt` | Never try/catch in a ViewModel. The overlay is opt-in: `loading = overlay()`, or `overlay(message)` to word it, and the wording survives overlapping calls |
| `updateData { copy(…) }` | same | The protected member, not the state extension: it logs an update that lands before the first state rather than dropping it |
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

## Known constraints

What the build and its tooling refuse, and why. The two runtime ones that silently produce a wrong
result are `CLAUDE.md` § Known constraints.

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
