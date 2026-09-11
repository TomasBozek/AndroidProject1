# service/ — the reusable half

Four modules that know nothing about this app. Reuse is by directory copy, so they never reference
`:core:*`, `:feature:*` or `:app`, and never read `R` from elsewhere. Directory, package and Android
namespace agree — `service/core/ui` is `…service.core.ui` in all three — so an import names the
module a symbol came from, and `:core:ui` and `:service:core:ui` no longer share a package (D49).

## `:service:core:domain`

A **Kotlin/JVM** module, so `android.*` is not on its classpath and the compiler is what enforces
the boundary. Do not make it an Android library to reach a framework class — move the class instead.

| Holds | What it is |
|---|---|
| `result/Outcome` | success or failure with a `DomainError`; the return type of every repository call |
| `error/DomainError` | `Network`, `Server`, `Unauthorized`, `BadRequest`, `NotFound`, `Credentials`, `Unexpected` |
| `Logger` | the interface only; the Android implementation lives one module up |
| `ErrorTracker` | the crash seam. `LoggingErrorTracker` is bound by default; the repo carries no vendor SDK |
| `Analytics` | `screen(id)` and `event(name, params)`. `LoggingAnalytics` is bound by default |
| `coroutines/DispatcherProvider` | switch at the data source, never at the repository |
| `crypto/Aead` and `AesGcmAead` | the encryption logic, JVM-tested |

`testFixtures` here carry `FakeLogger` and `TestDispatchers`.

## `:service:core:data`

| Holds | What it is |
|---|---|
| `BaseRepository` | `execute`, `observe` and `cached()`; turns a throw into an `Outcome` and takes a retry count for a flow whose collector outlives a failure |
| `DataStoreProvider` | one provider per module, so two modules never open the same file |
| `EncryptedDataStoreProvider` | the same, through `Aead`; the session uses it |
| `crypto/KeystoreAead` | fifteen lines fetching the key. Robolectric ships no `AndroidKeyStore`, which is why the logic is in `AesGcmAead` and only the fetch is here |
| `AndroidLogger` | the `Logger` implementation |
| `TrackingLogger` | decorates whichever logger is bound and forwards anything logged with a `Throwable` to the `ErrorTracker`, so no call site changes |

## `:service:core:ui`

`resourcePrefix = "core_"`, so it cannot collide with a consuming app's resources; lint's
`ResourceName` is an error.

| Holds | What it is |
|---|---|
| `viewmodel/BaseViewModel` | `execute {}` and `observe(flow = …) {}`; turns a failure into an alert or an inline retry, rethrows cancellation. The overlay is opt-in — `loading = overlay()` (D44). Never `try`/`catch` in a view model. `saved(key, default)` is a property that survives process death through the optional `SavedStateHandle` constructor parameter — for a half-typed search or a scroll position, never for a route argument, which Navigation 3's back stack already carries |
| `state/UiState` | the `(data, loading, alert)` envelope |
| `state/ContentState` | the error and empty states, rendered instead of content |
| `component/Screen()` | the only collector in the app and the only interpreter of `UiCommand` |
| `component/ScreenChrome` | the seam that lets an app draw `Screen()`'s surface, overlay, alert, empty state and snackbar host in its own design system (D50). `LocalScreenChrome` carries it; `DefaultScreenChrome` is stock Material, which is what this module renders when it is copied into a project that has no theme yet |
| `event/UiEvent`, `UiCommand`, `SystemEvent` | what the user did, what the shell should do, what came back |
| `navigation/NavResultStore` | a value handed from one screen back to another, consumed once |
| `permission/` | `rememberPermissionRequest`, `PermissionStatus`, `PermissionGate`, `rememberDeclaredPermissions`. The one package here whose tests need Robolectric |
| `form/DiscardBackHandler` | the back gesture on a form with unsaved input: `PredictiveBackHandler`, so a cancelled swipe asks nothing, and `discardAlert()` for the one wording every form uses |
| `text/UiText` | a string a view model can hold without a context |
| `format/Formats` and `LocalFormats` | money, weight, quantity, percent, time, date, duration, all from one locale. A screen never formats a number itself |
| `analytics/ScreenViewEffect` | the screen view `AppScaffold` sends automatically |

`testFixtures` carry `MainDispatcherRule` and re-export `:service:core:domain`'s, so one
`testFixtures(projects.service.core.ui)` line brings all three.

## `:service:network`

Flat rather than layered: it is one port to the outside world, so there is no domain/data split to
make.

`HttpClientFactory` takes its engine as a parameter, so the `dev` flavor can hand it `MockEngine`
and `:app` the OkHttp one without this module knowing either. Its retry policy makes up to
`NetworkConfig.retries` further attempts on a 5xx or a transport failure, with exponential backoff
and jitter, **on idempotent methods only** — never a POST. Do not add a retry loop in a data source.
`HttpErrorMapper` turns a status into a `DomainError`.

The on-disk HTTP cache is not here: it is a property of the engine, not of the client wrapping it,
so `:app`'s `cachedOkHttpEngine` (`src/main`, shared by `prod` and `staging`) builds it from
`NetworkConfig.httpCacheSizeBytes` and the platform's own cache directory. `dev`'s fixture engine
ignores it — a fixture is not worth caching.

## Taking it to another project

`python3 scripts/export_service.py --to <dir> --package <pkg>` copies these four modules and
`build-logic/`, rewrites the base package and prints the `settings.gradle.kts` block to paste. The
two rules above — no reference upward, and the resource prefix — are what make that work; both are
checked by `doctor.py`.
