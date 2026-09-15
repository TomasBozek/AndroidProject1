# core/ and app/ — this app's own

Three modules that are specifically this app: its look, its object graph and its shell. What they
build on is [SERVICES.md](SERVICES.md).

## `:core:ui`

`resourcePrefix = "app_"`. Re-exports `:service:core:ui` with `api(...)`, so a feature's
`presentation` module depends on nothing but `projects.core.ui`.

- The theme — three layers, the roles, the components. See [DESIGN-SYSTEM.md](DESIGN-SYSTEM.md).
- `@ScreenPreview` and `@ComponentPreview`, which live here rather than in `service/` because they
  reference `AppTheme`.
- `OverlayScreenshotTest`, which covers what a preview cannot see.
- The adaptive navigation suite, so the tab bar becomes a rail when there is width for one.

## `:core:di`

One file, `Koin.kt`, holding `initKoin()` and `coreModule(isDebug)` — the single registration point
for the whole graph. Every feature's `di` module is an `api(...)` dependency here, which is what
makes `appModules(isDebug)` the one list.

`KoinGraphTest` in `:app` verifies that list. Koin's `verify()` cannot see a `parametersOf`
argument, so a route key a screen takes as a parameter is listed by hand in `injectedParameters`;
`create_screen.py --with-args` writes that line and `doctor.py` fails if it is missing. What
`verify()` also cannot see is a Kotlin default: it treats a defaulted constructor parameter as
satisfied, while Koin's `*Of` builders resolve every parameter through `get()` and never read the
default. `Clock` is bound here beside `DispatcherProvider` for that reason — ambient system state a
test has to be able to fix, named by the constructors that need it rather than defaulted in them.
`check_koin_constructor_defaults` is what keeps that true.

Logging is WARN and above in a release build.

## `:app`

| Holds | What it is |
|---|---|
| `MainActivity` | the single activity, edge to edge, with the splash screen. Remembers the back stack unconditionally on the first frame and gates the display instead. Draws `AppBanner` above the display while `MainViewModel.online` is false, spending the status-bar inset there and consuming it for the screens |
| `AppNavHost` | every destination, in three groups: onboarding, auth and main. The only place a cross-feature navigation lambda is wired, and the place `ProvideNavResultStore` and `ProvideAnalytics` wrap everything below |
| `MainViewModel` | a plain `ViewModel`, the owner of `SessionState` and the only thing that switches flows; exposes `online` for the banner |
| `SessionState` | `Unknown`, `Onboarding`, `SignedIn`, `SignedOut` |
| `TopLevelDestination` | the five tabs — Home, Catalog, Cart, Trips, Settings: route key, label, icon and test id each. The id is `tabs_<name>Tab`, carried on the entry so a tab cannot be added without one. Five is Material's ceiling for a bottom bar, so a sixth is a decision, not an entry (D59) |
| `App` | the `Application`: `initKoin`, the notification channel |
| `DebugMenu` | the flag that decides whether the debug entries are registered at all |
| `debug/installDebugTooling` | per build type: `src/debug` installs StrictMode on `penaltyLog()` and a manifest overlay that names `res/xml/network_security_config.xml`, trusting user certificates; `src/release` is a no-op and carries no config |
| `network/CachedOkHttpEngine` | the real engine's on-disk HTTP cache, sized from `NetworkConfig`. In `src/main`, not `src/prod`/`src/staging` where the engine itself lives, because `:app`'s unit tests run for `devDebug` only and this is the one part of the real engine worth a JVM test |
| `network/AndroidConnectivityMonitor` | `ConnectivityMonitor` over the platform's default-network callback, registered only while collected. In `src/main` for the same reason; `connectivityMonitor()` in each flavor source set picks it, and `dev`'s returns `FixtureNetwork`, so the banner there follows the offline switch (D68) |

`:app` is also where a vendor is swapped in. Add the dependency here and override the one binding in
`:app`'s own Koin module, which is loaded after `coreModule` and therefore wins — that is the whole
procedure for `ErrorTracker` and for `Analytics`.

## What is registered where

Adding a feature or a screen means five or six edits, and every one of them is made by a generator
and checked by `doctor.py`:

| File | Gains |
|---|---|
| `settings.gradle.kts` | the module include |
| `core/di/build.gradle.kts` | `api(projects.feature.x.di)` |
| `core/di/…/Koin.kt` | the feature's module in the list |
| `app/…/AppNavHost.kt` | the destination |
| `app/…/KoinGraphTest.kt` | the route key, when the screen takes arguments |
| `docs/ai/CODEBASE.md` | the module tree row |
