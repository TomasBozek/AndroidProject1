# Architecture

The reasoning behind the rules, and the recipes too long to be a table. [CLAUDE.md](../CLAUDE.md)
is the rules themselves and is what a session reads every turn; this is what you open when you are
about to do one of these things for the first time. The generators document themselves in
[scripts/README.md](../scripts/README.md) and in each `--help`.

## Recipes, in full

### A new feature

```bash
python3 scripts/create_feature.py userProfile
```

Clones all five layers and registers them everywhere. Then, in order:

1. Replace the placeholder `val title: String` / `val counter: Int` in `UserProfileState`, its
   `PREVIEW` and the three states in `UserProfileStatePreviews` with the real ones. Keep all three:
   empty is where layouts collapse and long text is where they overflow, and both are goldens.
   `PREVIEW` is not optional — it is the preview fixture and normally the `initialState`.
2. Write `UserProfileScreen.kt`. Strings used only in the composable go through `stringResource(...)`;
   strings a ViewModel needs go into the state as `UiText` (`R.string.x.toUiText()`). Both live in
   the feature's own `res/values/strings.xml`, prefixed `user_profile_`.
3. Add cases to `UserProfileEvent`, handle them in `UserProfileViewModel.onUiEvent`, and emit
   `UserProfileNavigation` to move on. The `CollectEffect` block in `UserProfileDestination.kt` is
   generated empty — that is where a navigation intent turns into a back-stack call.

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
demonstrates the argument-carrying route: an `@Serializable data class` route key, a ViewModel
taking that key as a constructor parameter, a destination handing it over with
`koinViewModel { parametersOf(key) }`, and a plain JVM test. Bare `--with-args` gives one
`id: String`. Supported types are `String`, `Int`, `Long`, `Boolean`, `Float`, `Double`.

It also adds the key to `KoinGraphTest`'s `injectedParameters`: Koin's `verify()` cannot see a
`parametersOf` argument and would call the route key a missing definition. That is the sixth
registration, and `doctor.py` fails if it is missing.

Do not hand-convert a `data object` route into a `data class` — that is what produced a screen
loading from a `LaunchedEffect` instead of from the key it was handed.

Note that `create_feature.py` deliberately skips the `TemplateArgs*` files: a new feature starts
with one screen, and copying the second would leave an unregistered destination behind.

Writes the six-file unit into `presentation/userprofiledetail/` — a directory named after the
screen — plus its two tests, plus the one feature-local component the generated screen composes
in `presentation/component/`. Adds `user_profile_detail_*` strings to the feature's
`strings.xml`, registers the ViewModel in the feature's Koin module and the destination in
`AppNavHost.kt`. Then follow steps 1–3 above. `--sub` names that directory instead of deriving
it, which is what a long screen name wants: `--sub search` rather than `catalogsearch`.

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

### Changing a Room schema

A module with a database applies `convention.android.room` beside `convention.feature.data`, which
commits the exported schemas under the module's `schemas/` and puts them on the unit test's assets.

**A `version` bump ships its migration and its test in the same commit.** Bump `@Database`'s
`version`, add the `Migration` to the database's own `MIGRATIONS` array — the builder in the
feature's Koin module and `XDatabaseMigrationTest` both read that one list — and commit the schema
JSON Room exports on the next compile. Nothing else to write: the test walks every committed
version up to the compiled one, so it fails on the bump alone and passes once the migration exists.

Never reach for `fallbackToDestructiveMigration`. It compiles, the tests go quiet, and what it
means is that the next update empties the user's cart.

### Navigating to another feature

Not scripted, and deliberately: a `presentation` module must never depend on another feature's
`presentation` (`doctor.py` fails if it does). Add a lambda parameter to the destination function and
wire it in `AppNavHost.kt`, where the parameter is the only thing either module knows about the other:

```kotlin
userProfileDestination(
    backStack = backStack,
    navigateToSettings = { backStack.add(SettingsDestination) },
)
```

Switching between the auth and main flows is different again: change the session and let
`MainViewModel` react. Do not replace the back stack from a screen.

### Getting a value back from another screen

"Pick something on screen B, hand it back to screen A." Navigation 3 has no `previousBackStackEntry`
and a back stack of plain keys has nowhere to hang a value, so this is
`service/core/ui/.../navigation/NavResultStore.kt` and never a `SavedStateHandle`.

- The requester registers a callback: `NavResultEffect<String>(KEY) { id -> onEvent(...) }`.
  It fires **once** per result — the value is consumed, so coming back later does not replay a
  selection the user already made.
- The responder gets a setter: `val setNavResult = rememberNavResultSender(key)`, calls
  `setNavResult(value)` and then pops. The key arrives as a route argument, so one picker can
  serve several callers and knows nothing about any of them.
- `ProvideNavResultStore` wraps the `NavDisplay` in `AppNavHost` — above the entries, because the
  result has to outlive the responder being popped. It is a `rememberSaveable`, so it survives
  process death with the back stack.
- A value must be something a `Bundle` can hold: a primitive, `String`, `Parcelable` or
  `Serializable`, the same constraint a route argument has.

### Permissions

`service/core/ui/.../permission/` owns the whole story; no feature writes permission code of its own.

- **`rememberPermissionRequest(vararg permissions)`** wraps `rememberLauncherForActivityResult` and
  returns a `PermissionRequest` with a `status` and a `request()`. `status` is re-read on every
  resume, because the user can change a permission in system settings and come back.
- **`PermissionStatus`** is `NotRequested` / `Granted` / `PartiallyGranted` / `Denied(canAskAgain)`.
  Four cases, not a boolean, because each ungranted one needs different UI — and once
  `canAskAgain` is false the system dialog never appears again, so the only way forward is
  `Context.openAppSettings()`.
- **`PermissionGate(permission, rationale = …) { content }`** composes `content` only while the
  permission is held. Prefer it to checking a boolean: there is no ungranted case for a caller to
  forget, because `content` simply does not run.
- **`rememberDeclaredPermissions()`** lists what the merged manifest asks for, read from
  `PackageManager` — so a permission a library contributed shows up too. Also re-read on resume.

**Permission state is read in composition, not in a ViewModel.** It lives outside the app and
changes while the app is backgrounded, so there is nothing to observe — only something to re-read.
A screen that needs it in its state hands it over as an event, the way `SettingsPermissionsScreen`
does with `PermissionsRead`. A ViewModel-readable version is item 7.13, parked until a ViewModel has
to make a decision on one.

Ask for a permission where the user came looking for it, not on first launch. `POST_NOTIFICATIONS`
is requested from the Permissions screen, and its channel is created in `App.onCreate`.

### The tabs

`TopLevelDestination` in `:app` is the bottom bar: one entry per tab, each naming a feature's route
key, its label (a string in `:app`'s own `strings.xml`) and its icon. `NavigationSuiteScaffold`
renders it as a bar on a phone and a rail once there is width for one. The auth flow has no tabs, so
`AppNavHost` composes the display without the scaffold there.

**The tabs do not each own a list.** The back stack *is* their concatenation, in the order the tabs
were last visited, and a tab's key is the only thing that starts a segment — so `currentTab` is the
last tab key on the stack, `selectTab` moves that tab's segment to the end, and push and pop are
unchanged because they act on the segment that happens to be last. One flat list is what makes
per-tab history survive process death with no custom `Saver`: it is the list `rememberNavBackStack`
already saves. Back out of a tab's root and the previously visited tab is underneath it, which is
what Android expects.

Adding a tab is one entry in `TopLevelDestination`, one `xEntries()` block in `AppNavHost.kt`, one
label string, and its `--graph` name in `scripts/_common.py`'s `NAV_GRAPHS`. A tab root carries no up
arrow: the bar is what leaves it.

### Removing things

`delete_feature.py` removes a whole feature and all five registrations. There is no script for a single
screen: delete its directory, its `user_profile_detail_*` strings, the `viewModelOf(::XViewModel)` line
and the two `AppNavHost.kt` lines. Run `doctor.py` afterwards — it catches every one of those if you
miss it.

### Changing what gets generated

`feature/template` **is** the template — `create_feature.py` and `create_screen.py` clone it verbatim
and only rewrite names. To change the shape of every future feature or screen, edit `feature/template`,
not the scripts. It is included in `settings.gradle.kts` precisely so `./gradlew build` keeps it
compiling; keep it that way.

## Testing a screen

Two tests per screen, and they answer different questions:

| | Asks | Runs as |
|---|---|---|
| `XViewModelTest` | what the state becomes, and what navigation is emitted | plain JVM test |
| `XScreenTest` | what is on screen, and what a tap does | Robolectric unit test |

The second is the half a ViewModel test cannot reach. `LoginScreenTest` is the pattern: it renders
the stateless screen with a fixed state, collects the events it emits, and finds everything by
`testTag` — never by text, because copy gets reworded and translated. It runs under Robolectric as
an ordinary unit test, so `./gradlew test` covers it and CI needs no emulator.

Two things it has to say out loud, both of which cost a comment in the file:

- **`@Config(sdk = …)` is pinned.** Robolectric ships an SDK image per API level and has none for
  this project's `targetSdk`; the test asks for the newest it does have.
- **A compound component is tagged on its group.** A caller's `modifier` goes to the outermost
  element, so `Modifier.testTag("login_emailField")` on an `AppTextField` tags the label, input and
  supporting line together. Assertions about the group use the tag directly; a test that types
  reaches the input inside it with `hasSetTextAction() and hasAnyAncestor(hasTestTag(…))`.

`convention.feature.presentation` carries the dependencies, so a new screen's test needs no
build-file edit.

## Screenshots

A third test, but one per **module** rather than one per screen: `PreviewScreenshotTest` records a
golden image for every `@ScreenPreview` and `@ComponentPreview` on its own classpath and fails when
one of them changes. It is in every `presentation` module and in `:core:ui`, and
`create_feature.py` clones `feature/template`'s copy, so a generated feature is covered the day it
is generated.

```bash
./gradlew recordRoborazziDebug   # write the goldens after a deliberate change
./gradlew verifyRoborazziDebug   # check them; the CI build job runs this
```

**The previews are the list.** Nothing is registered anywhere: adding a component with a
`@ComponentPreview` adds three goldens, adding a screen adds five, and deleting either leaves its
images for `git status` to point at. The images live in each module's `src/test/screenshots/` and
are committed — which is the one thing to be careful about, because **a golden nobody looked at is
a test that passes forever**. Open what `record` wrote before committing it; a blank or clipped
image asserts the blankness just as firmly as a correct one asserts the layout.

Two details the test file explains and that a new one must keep:

- **The clock is advanced by hand, one frame.** A screen holding a `CircularProgressIndicator`
  never reaches idle, and a capture that waits for idle waits forever.
- **`manualAdvance` is added to the preview's own options, not to fresh ones.** Those options carry
  the `@Preview`'s device, `uiMode` and `fontScale`; replace them and a screen's five variants come
  out as five identical files that assert nothing.

**An overlay needs a golden of its own**, and that is the one thing the previews are not the list
for. A dialog, a sheet, a menu and a picker each draw in a window of their own, and a capture of a
preview captures the composable — so all 349 goldens were green while the date picker was clipped
on a 360 dp phone. `OverlayScreenshotTest` in `:core:ui` is the answer: it opens the overlay and
uses `captureScreenRoboImage`, which captures the screen and so takes the window with it. Add an
overlay component and add a case there, recorded at the shapes an overlay actually breaks on —
the narrowest phone and a phone in landscape, not the comfortable 400×900 the previews use.

An ordinary `./gradlew test` leaves Roborazzi switched off, so the class costs the build nothing —
which is also why CI needs the separate `verifyRoborazziDebug` step.

## The design system's edges

`toColorScheme()` / `toTypography()` / `toShapes()` also populate Material's own theme, so
`service/core/ui`'s `Screen()` — which cannot depend on `:core:ui` without losing its portability —
picks the system up for free. `Previews.kt` lives in `:core:ui` rather than in `service/` for the
same reason in reverse: it references `AppTheme`.

`:service:core:ui`'s `permission/` package is the one part of `service/` whose tests need
Robolectric. It is a wrapper over `PackageManager` and the activity-result contract, so there is no
logic there to test without a shadowed framework, and `convention.android.library.compose` already
puts Robolectric on that module's test classpath. Everything else under `service/` is JVM-tested —
`R.string.x` is only an `Int` and `UiText` defers resolution, so keep it that way rather than
reaching for the framework to test logic.

`:service:core:ui` re-exports `FakeLogger` and `TestDispatchers` with `testFixturesApi`, so a screen
test needs one `testFixtures(projects.service.core.ui)` line and the convention plugin already adds
it.

## Crash reporting

`ErrorTracker` in `:service:core:domain` is the seam; `LoggingErrorTracker` is bound by default and
reports to the log and nowhere else. **The repo carries no vendor SDK and no vendor config file** —
one would make every project that starts from this template either use that vendor or unpick it
first.

Nothing calls the tracker directly. `TrackingLogger` decorates whatever `Logger` is bound and
forwards anything logged with a `Throwable`, so the paths that already report a problem —
`BaseViewModel.handleError`, `BaseRepository`'s failure paths — report to it with no signature
changing anywhere. A `w` with no exception stays a note to whoever is reading logcat; reporting
those would bury the real ones.

To swap in a vendor, add the dependency to `:app` and override the one binding there:

```kotlin
// app/src/main/kotlin/.../CrashlyticsErrorTracker.kt
class CrashlyticsErrorTracker : ErrorTracker {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordNonFatal(throwable: Throwable, message: String?) {
        message?.let(crashlytics::log)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) = crashlytics.log(message)

    override fun setUser(id: String?) = crashlytics.setUserId(id.orEmpty())
}

// in :app's own Koin module, which is loaded after coreModule and so wins
single<ErrorTracker> { CrashlyticsErrorTracker() }
```

`setUser` takes an opaque id — never an email, never a name, because a crash report is not the
place to put either. **Nothing calls it yet**: the sample `AuthService` exposes only a boolean, so
there is no id to pass. Call it from wherever the session becomes known once the session carries
one, and call it with `null` on sign-out so the next person's reports are not attributed to the
last one.

## Analytics

`Analytics` in `:service:core:domain` is the seam — `screen(id)` and `event(name, params)` —
and `LoggingAnalytics` is bound by default, reporting to the log and nowhere else. As with crash
reporting, **the repo carries no vendor SDK and no vendor config file**.

**The screen view is automatic.** `AppScaffold` calls `ScreenViewEffect(screenId)`, so a screen
that passes `screenId` is measured and there is no per-screen call to forget; `doctor.py` fails on
a screen that composes a scaffold without one. It is a `LaunchedEffect`, so a recomposition does
not count a second view — read the note on `ScreenViewEffect` before changing that, because
`ScreenViewTest` cannot observe the difference.

Everything else is a deliberate call at the point it happens. Read it from composition with
`LocalAnalytics.current`, which defaults to `Analytics.NoOp` so a preview or a Robolectric test
needs no graph behind it. Keep `params` few and low-cardinality: a parameter that can take a user
id or a free-text field turns one event into millions and is unusable in every vendor's console.

To swap in a vendor, add the dependency to `:app` and override the one binding there, exactly as
with `ErrorTracker`:

```kotlin
// in :app's own Koin module, which is loaded after coreModule and so wins
single<Analytics> { FirebaseAnalyticsAdapter(androidContext()) }
```

`AppNavHost` wraps everything below it in `ProvideAnalytics(koinInject())`, beside
`ProvideNavResultStore` and for the same reason — above the entries, so it outlives any one
screen. That one call is what makes the binding reach composition: without it `LocalAnalytics`
falls back to `Analytics.NoOp` and every screen view is silently dropped, with nothing anywhere
turning red. `AppNavHostAnalyticsTest` in `:app` is what holds it, by composing the real nav host
over a small Koin graph and asserting the view arrives.

## Translations

**Every string ships in every locale.** A `res/values/strings.xml` has a `res/values-cs/strings.xml`
beside it declaring the same names, and `doctor.py` fails on a module missing one — precisely the
case lint's `MissingTranslation` cannot see, because a module with no `values-cs` at all looks to
lint like a module that ships one language. `create_feature.py` clones both from `feature/template`
and `create_screen.py` merges a new screen's strings into both, so a generated feature starts
bilingual.

Czech has four CLDR plural forms against English's two — `one` / `few` / `many` / `other` — and
`doctor.py` requires all four; `many` is the decimal form („1,5 znaku") and the one a hand-written
translation forgets. A `%s` is filled at runtime with a phrase in a fixed form, so a translated
sentence must not put it where the language needs a different case: see `cart_checkout_message`,
worded around a count phrase that is always nominative. The locale list is `TRANSLATED_LOCALES` in
`scripts/_common.py` — a second language is one entry there and a directory per module.

## Build reports

`./gradlew assembleDevDebug -PcomposeMetrics` writes the Compose compiler's stability reports under
`build/compose-reports/<module>/`. Off by default because they cost a compiler pass on every module;
on when you want to know whether a state the code calls `@Immutable` is one the compiler agrees
about. `<module>-classes.txt` is the file to read: every `XState` should say `stable class`, with no
`runtime` or `unstable` member. The one exception is `UiState`, which is generic — its stability is
its type argument's, which is what `Parameter(Data)` on its `<runtime stability>` line means.

What the compiler cannot work out for itself is stated in `build-logic/compose-stability.conf`:
read-only collections, and the domain models, whose modules are Kotlin/JVM and so never see the
Compose compiler at all — it treats every class from one as unstable rather than unknown. A line
there is a promise, so do not add one to quiet a report that is right. Compose plugin options are
not task inputs, so a report needs `--rerun-tasks` (or a clean) to be regenerated.

`./gradlew koverHtmlReport` is coverage, and deliberately not a gate: there is no threshold, because
a number that has to be met gets met by tests written for the number. It is a signal — which module
the tests avoid. CI uploads it as an artifact. Previews and generated classes are filtered out.

ktlint's rule set lives in `.editorconfig`, not a second config file: `intellij_idea` style rather
than `ktlint_official`, with `class-signature`, `function-signature` and `parameter-list-spacing`
off (all three read a multi-line parameter list as if it were on one line, so every constructor in
the repo would be a violation) and `function-naming` off (a `@Composable` is PascalCase and a test
name is a backtick-quoted sentence).

**There is still no detekt.** Re-tested 2026-09-08 on 1.23.8, the current release: its embedded
Kotlin compiler rejects the JDK 25 the daemon is pinned to — it refuses `--jvm-target 25`, and once
that is pinned to 17 it fails on the JDK's version string instead. detekt 2.x is `2.0.0-alpha`.
Revisit when 2.x is stable.
