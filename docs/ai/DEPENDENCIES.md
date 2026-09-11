# Dependencies

Every version lives in [../../gradle/libs.versions.toml](../../gradle/libs.versions.toml) and
nowhere else — not in a module build file, not in this document. What follows is which library does
which job, and which convention plugin puts it on a module's classpath.

A new dependency has to earn its place: Google, JetBrains and androidx first, then a library with a
large company behind it and broad adoption. Anything else gets a row in
[../DECISIONS.md](../DECISIONS.md), and if it reaches the release build, a first-party alternative that
was tried and found wanting. Build- and test-only tools are judged more leniently but still get the
row. This applies to what is here as much as to what is added: an unused dependency is removed, not
kept for symmetry.

## Ships in the app

| Library | Job | Applied by |
|---|---|---|
| Compose BOM, runtime, UI, foundation, Material 3, icons | the whole UI | `convention.android.library.compose` |
| Material 3 adaptive navigation suite | bar on a phone, rail on a tablet | `:core:ui` |
| Navigation 3 runtime and UI, the lifecycle ViewModel decorator | the back stack and its entries | `:service:core:ui` |
| Lifecycle runtime, ViewModel, saved state, runtime-compose | view models and lifecycle-aware collection | `:service:core:ui`, `convention.feature.presentation` |
| Activity Compose, core-ktx, splashscreen | the single activity and its splash | `convention.android.application` |
| Koin (BOM, core, android, androidx-compose) | the object graph | `convention.feature.di`, `convention.feature.presentation` |
| Coroutines core | everything asynchronous | `convention.kotlin.jvm`, `convention.feature.data` |
| kotlinx.serialization JSON | route keys and network payloads | `convention.feature.presentation`, `convention.service.network` |
| DataStore preferences | key-value storage | `:service:core:data` |
| Room (runtime, ktx, compiler via KSP) | the three databases | `convention.android.room` |
| Ktor client (core, auth, logging, content negotiation, JSON, OkHttp) | HTTP | `convention.service.network` |
| Coil (compose, okhttp) | remote images | `convention.core.ui`, `:core:ui`'s only consumer |
| LeakCanary | leak reports, debug build only | `convention.android.application` |

## Build and test only

| Library | Job | Applied by |
|---|---|---|
| Android Gradle Plugin, Kotlin, Compose compiler, KSP, Room plugin | the build itself | the convention plugins, `compileOnly` |
| ktlint Gradle plugin | formatting; the rule set is `.editorconfig` | the root build file, on every module |
| Kover | coverage, a signal and never a gate | the root build file |
| JUnit, coroutines-test, Turbine | unit tests | `convention.kotlin.jvm`, the `testing` bundle |
| Robolectric | screen tests as ordinary unit tests | `AndroidConventions`, `convention.feature.data` |
| Roborazzi and its preview-scanner support | screenshot goldens | `convention.android.library.compose` |
| ComposablePreviewScanner | finds the `@Preview` functions the goldens are recorded from | `convention.android.library.compose` |
| Ktor MockEngine | the `dev` flavor's fixtures, and data-layer tests | `convention.feature.data`, `convention.service.network` |
| OkHttp MockWebServer | tests the real engine's on-disk HTTP cache against a real request/response cycle, rather than reimplementing OkHttp's cache logic with a fake | `:app`'s own test source set |
| Koin test | `KoinGraphTest` | `convention.android.application` |
| androidx test core | Robolectric's `ApplicationScenario`/`ApplicationProvider` | `convention.feature.data` |
| gitleaks, Maestro | secret scanning and end-to-end flows, in CI only | `.github/workflows/build.yml` |

## Updating

Renovate watches the catalog and opens one pull request per group — androidx, Kotlin, Koin and the
Android Gradle Plugin are grouped, and pre-releases are skipped. That pull request runs T2 like any
other. A bump that fails is not pinned back silently: it gets a line in
[../BACKLOG.md](../BACKLOG.md) saying what broke, and the pull request is closed.

Two version facts are load-bearing and live outside the catalog: the Gradle daemon's JDK, pinned in
`gradle/gradle-daemon-jvm.properties` and provisioned through foojay, and the SDK levels in
`build-logic`'s `ProjectConfig`. Both are named in `ai/CODEBASE.md` § Known constraints, because both
have broken a tool before.
