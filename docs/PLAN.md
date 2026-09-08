# Plan

Plan 3. The single work list for this template: what is open, who may work on it in parallel,
and what needs a decision. `CLAUDE.md` is the rulebook, `README.md` the orientation,
`scripts/README.md` the generators. History lives in git, not here — Plan 2 in full is
`git show 3dde6e2:docs/PLAN.md`, and every landed item is one commit named `<id> <title>`.

## Status

**Updated:** 2026-09-09 · **Gate:** doctor 23/23 · test_scripts 43 · ktlint clean · build green
**Coverage:** 38 % lines — architecture and ViewModels tested; data layers 0 %, components 10 %
**Repo:** 25 modules + `build-logic` · 41 components · 5 sample features + `template` · 10 scripts

| Track | Owns | Done | Progress |
|---|---|---|---|
| **core** · the reusable architecture | `service/`, `core/di`, `build-logic/` | 0 / 6 | `░░░░░░░░░░` 0 % |
| **ui** · design system and adaptive | `core/ui`, `feature/gallery` | 0 / 5 | `░░░░░░░░░░` 0 % |
| **app** · shell and sample features | `app/`, `feature/*` | 0 / 13 | `░░░░░░░░░░` 0 % |
| **quality** · tests, CI, release | `.github/`, `.maestro/`, `scripts/`, `feature/template`, `docs/` | 0 / 9 | `░░░░░░░░░░` 0 % |
| **Total** | | **0 / 33** | `░░░░░░░░░░` 0 % |

**Start now, one worktree each:** `core.1` · `ui.1` · `feat.1` · `qa.3`. None of the four waits
on a question, and none touches another's files.
**Waiting on you:** Q6 — the licence, now askable because D19 settled the repo as a template —
and Q7, strike or swap a sample feature. Q3 and Q4 shape backlog items only. No track waits.

Legend · size `S` under an hour, `M` half a day, `L` a day or more · risk `stable` known-good
libraries, `plugin` adds a Gradle plugin (check its AGP range before writing code), `alpha`
pre-release library, `device` needs an emulator or hardware, `decision` waits on a Q or D.

## Questions

No default; the dependent items wait, everything else proceeds.

- **Q3 · Locales.** English only, or English and Czech? And is the spreadsheet-driven string
  pipeline the reference project has wanted here — written fresh, not ported?
- **Q4 · Theme from tokens, or the hand port?** The KSD project plans to generate `Tokens.kt`
  from its DTCG token files with Style Dictionary. That keeps design and code in step at the cost
  of Node tooling in an Android repo; the hand port is one file and already done.
- **Q5 · Two facts left.** Has the Renovate GitHub app been installed on the repo? `git ls-remote
  origin` shows only `main`, so it has never opened a PR. And can `main` get required status
  checks (`qa.9`)? Both need the GitHub UI or `gh`, which is not installed here. The device half
  is answered — D21.
- **Q6 · Licence.** Apache-2.0 or MIT — D19 made this a template, so it needs one. The repo has
  had no LICENSE file since the first commit; `qa.1` is one file and a README line once you pick.
- **Q7 · The four sample features.** Favourites, cart, profile, search — each exists to prove one
  capability the architecture has and no sample uses (see the app track). Strike or swap any.

## Decisions

A decision with a default is taken when its item starts; say so before then to change it.
D1–D12 are Plan 2's, made 2026-09-08, and stand.

| | Decision | Outcome |
|---|---|---|
| D1 | `gateway` module | Merged into `data` |
| D2 | Splash | SplashScreen API, no minimum hold |
| D3 | `UiState.loading` | Defaults to `null` |
| D4 | Convention plugins | Yes, and they own the shared dependencies |
| D5 | AGP | Stable 9.x |
| D6 | Navigation 3 | Migrated, no spike |
| D7 | Network and database | Ktor client + Room |
| D8 | Crash reporting | Interface + logging default; no vendor SDK in the repo |
| D9 | Screenshot tool | Compose Preview Screenshot Testing — **superseded by D14** |
| D10 | Material 3 Expressive | No, standard M3 |
| D11 | Design system | KSD, imported as three layers |
| D12 | Dynamic colour | Removed, not defaulted off |
| D13 | Brand face | **Default: bundle Source Sans 3.** Previews and goldens need a face that renders without network, a POS device may have no Play Services, and 300–500 kB is nothing. Fetching the OFL-licensed TTFs into the repo is part of the item |
| D14 | Screenshot tool, second try | **Default: Roborazzi.** Runs under the Robolectric 4.16 that already works here, no alpha plugin, and scans the previews that exist rather than duplicating them. If the compatibility check fails within an hour, park it like 4.1 |
| D15 | End-to-end tool | **Default: Maestro.** `CLAUDE.md` already writes its syntax and every screen carries the ids |
| D16 | Component gallery in prod | **Default: no.** It moves under the debug menu (`shell.2`) and R8 drops it from `prod` |
| D17 | Merge policy for worktrees | **Default: one PR per item, rebase-merged, CI required.** `main` keeps one commit per item |
| D18 | Room wiring | **Default: a `convention.android.room` plugin** (Room + KSP), applied beside `convention.feature.data` by the modules that need it |
| D19 | Q2 · Template or product | **Template.** The four generic sample features stand, `feature/template` and the generators keep earning their cost, `ui.4` / `ui.5` stay where they are |
| D20 | Q1 · What the sample talks to | **Ktor `MockEngine` fixtures on the `dev` flavor.** No server to keep alive and CI runs it unchanged. The cost is that "offline" is a fixture told to fail, not a real network drop — `feat.5`'s Verify line says so rather than pretending otherwise |
| D21 | Q5 · Hardware | **A physical device exists.** `qa.5` stays scheduled behind `shell.1` |

## What Plan 2 taught

Fifty items landed in one day. Where the time went, and the rule each cost bought:

| Cost | Rule in this plan |
|---|---|
| ~2 h of the day on alpha Gradle plugins meeting AGP 9 + Gradle 9 + JDK 25 | Every item carries a risk tag. A `plugin` or `alpha` item starts with a compatibility check and gets 30 minutes to a green spike before it is parked |
| The plan grew to 1,140 lines, most of it notes on finished items; every session paid to read it | An item is four lines. A landed item keeps its Done line; the commit and PR hold the story |
| Coverage: 38 % overall, data layers 0 %, components 10 % | An item that adds code names its tests in its Verify line and ships them in the same commit |
| Device checks by hand through `adb` and `uiautomator dump` | Maestro flows over the ids that already exist (`qa.2`); hardware for what an emulator cannot settle (`qa.5`) |
| One agent, one branch, one item at a time | Four tracks with disjoint file ownership, so four worktrees can run at once |
| One open decision (D13) held the last Phase 3 item for the whole plan | Every D has a default. Only a Q waits, and a Q blocks only its own items |
| Full `./gradlew build` some thirty times per item | Iterate on the module task; run the gate once, before the commit |

## How to work this plan

**Worktrees.** One per item, on a branch named after it:

```bash
git worktree add ../<repo>-core.1 -b core.1-network
```

Rebase on `main` before the PR. PR title is `<id> <title>`; rebase-merge, so `main` stays one
commit per item. Branch protection with the three CI jobs required is `qa.9`.

**Shared files.** These are edited by more than one track, always additively — a new line, never
a rewrite. On a conflict keep both sides and run `doctor.py`; it checks every one of them.

| File | Who appends |
|---|---|
| `settings.gradle.kts`, `core/di/build.gradle.kts`, `core/di/Koin.kt`, `app/AppNavHost.kt`, `app/KoinGraphTest.kt`, the module tree in `CLAUDE.md` | any item that adds a feature or screen — the generators do it |
| `gradle/libs.versions.toml` | any item that adds a library |
| `build-logic/` | core track; `feat.1` adds one plugin file, `ui.3` one dependency line |
| `app/TopLevelDestination.kt` | `feat.2` adds a tab |
| this file | every item, its own line and the dashboard |

An item that must edit a file its track does not own says so in its Done line; nobody else has an
open item on that file at the same time.

**Item lifecycle.** `[ ]` → `[~]` when started (add the id to *Start now*) → `[x] (date)` when
the gate is green and the PR is merged; refresh the track row and the total, one `█` per 10 %.
`[-]` drops an item with one line saying why. New item: next number in its track. If what shipped
differs from the Done line, one **Landed:** sentence — not a paragraph.

**The gate,** in the order CI runs it:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew ktlintCheck && ./gradlew build
```

**Environment,** checked 2026-09-09 on this machine, because four `device` items assume it:
`adb` lives at `~/Library/Android/sdk/platform-tools/adb` and is not on `PATH`; one AVD exists
(`medium_phone_1`), so `ui.4` and `ui.5` need a tablet profile created before their Verify lines
can run; `gh` and `maestro` are not installed, and `qa.2`, `qa.6`, `qa.9` and D17's
one-PR-per-item flow all want them.

**Scope rules that stand.** No new scripts; an existing one changes only when something else
forces it, as part of that item. A `service/` module never references `:core:*`, `:feature:*` or
`:app`. A feature composes from `:core:ui` and never draws. Do not build a data layer against
invented data — that is what Q1 is for.

## Dependencies

Arrows are "must land first". Everything not drawn is independent and can start today.

```mermaid
flowchart LR
  classDef core fill:#DBE7FF,stroke:#3566E0,color:#1D3F96
  classDef ui fill:#FFEDC2,stroke:#DE9209,color:#915B06
  classDef app fill:#D0F4DF,stroke:#1FA463,color:#12693E
  classDef qa fill:#EEEEEC,stroke:#75756F,color:#3C3C37

  core1[core.1 network client]:::core
  core2[core.2 offline-first]:::core
  core4[core.4 nav results]:::core
  core5[core.5 forms]:::core
  ui1[ui.1 brand face]:::ui
  ui2[ui.2 screenshots]:::ui
  ui4[ui.4 size class]:::ui
  ui5[ui.5 list–detail]:::ui
  shell1[shell.1 deep links]:::app
  shell2[shell.2 debug menu]:::app
  shell4[shell.4 notification]:::app
  feat1[feat.1 favourites + Room]:::app
  feat2[feat.2 cart]:::app
  feat3[feat.3 profile]:::app
  feat5[feat.5 catalog remote]:::app
  feat8[feat.8 screen tests]:::app
  qa3[qa.3 template screen test]:::qa
  qa5[qa.5 hardware pass]:::qa

  core1 --> feat5
  core2 --> feat5
  feat1 --> feat5
  core4 --> feat2
  feat1 --> feat2
  core5 --> feat3
  ui1 --> ui2
  ui4 --> ui5
  shell1 --> shell4
  shell2 --> shell4
  shell1 --> qa5
  qa3 --> feat8
```

Independent: `core.3` `core.6` `ui.3` `shell.3` `shell.5` `feat.4` `feat.6` `feat.7` `qa.1`
`qa.2` `qa.4` `qa.6` `qa.7` `qa.8` `qa.9`.

---

## Track core · the reusable architecture

Owns `service/`, `core/di`, `build-logic/`. Every item here is API the app track then uses;
nothing here knows a feature.

- [ ] **core.1 `:service:network` — Ktor client** · L · `stable`
  Why: nothing crosses a network. The reusable half — client, error mapping, auth, refresh — is
  API-agnostic.
  Done: module on Ktor **3.5.2** with `api(projects.service.core.domain)` only; `HttpClient` factory
  (JSON, timeouts, logging on debug) **taking its engine as a parameter**, which is what lets D20's
  `MockEngine` replace it on `dev`; HTTP status → `DomainError` table; bearer auth behind a
  `TokenStore` interface with single-flight refresh; Chucker **4.3.1** on `debugImplementation`; `create_datasource.py --remote`
  emits the Ktor-backed variant (was 7.16); `export_service.py` picks the module up unedited.
  Verify: `MockEngine` tests for every row of the mapping table and for two parallel 401s causing
  one refresh; a `test_scripts.py` case for `--remote`; Chucker absent from `prodRelease`.

- [ ] **core.2 Offline-first combinator** · M · `stable`
  Why: was 6.2's first half. Cache-then-network is the shape every remote-backed screen needs.
  Done: `BaseRepository.cached(local: Flow<T?>, remote: suspend () -> T, write: suspend (T) -> Unit)`
  emitting cache, then remote, then the refreshed cache; a remote failure over a stale cache emits
  the data plus a failure the screen can show inline. Pure JVM — `local` is any flow.
  Verify: `BaseRepositoryTest` cases for hit, miss, stale-on-failure, and remote-then-local order.

- [ ] **core.3 Lifecycle-aware `observe`** · S · `stable`
  Why: was C8. `observe {}` collects for the ViewModel's whole life, including backgrounded. Fine
  for DataStore, wrong for a socket or a location stream.
  Done: `observe(flow, whileSubscribed = true)` collects the source only while `state` has a
  subscriber, with a 5 s grace; the KDoc says which variant to reach for.
  Verify: `BaseViewModelTest` shows the source is cancelled when the last collector leaves and
  re-collected when one returns.

- [ ] **core.4 Navigation results** · M · `stable`
  Why: was 7.2. No pattern for "pick something on screen B, return it to A", so it gets reinvented
  per feature. Navigation 3 has no `previousBackStackEntry` to lean on.
  Done: `service/core/ui/navigation/` — a result store keyed by the requesting entry, a
  `rememberNavResult<T>()` for the requester and `setNavResult(value)` for the responder, surviving
  process death through the saved-state decorator; documented in `CLAUDE.md`'s navigation recipe.
  The worked example is `feat.2`'s product picker.
  Verify: a Robolectric test round-trips a value across a push and pop; the pattern needs no
  `SavedStateHandle` in any ViewModel.

- [ ] **core.5 Form validation** · S · `stable`
  Why: was C10. `LoginState.canSubmit` and `SignUpState.canSubmit` are hand-rolled booleans that
  cannot say *why* submit is disabled.
  Done: `service/core/ui/form/` — `FieldState(value, error: UiText?, touched)`, validators
  (`required`, `email`, `minLength`), and a `Form` that derives `canSubmit` and the first error;
  `LoginState` and `SignUpState` migrate to it (edits `feature/auth/presentation`, app track's directory).
  Verify: JVM tests per validator; `LoginScreenTest` and both auth ViewModel tests unchanged and
  green; the error text appears under the field in the Login preview.

- [ ] **core.6 Analytics seam** · M · `stable`
  Why: was 7.4. Screen views are the one event every product wants, and the only place that knows
  every screen is `AppScaffold(screenId)`.
  Done: `Analytics` in `:service:core:domain` (`screen(id)`, `event(name, params)`), a logging
  default bound in `coreModule`, a `LocalAnalytics` in `:service:core:ui`; `AppScaffold` reports
  `screenId` once per entry (one call added in `core/ui`, ui track's directory); vendor recipe
  next to the crash-reporting one in `CLAUDE.md`.
  Verify: a Robolectric test that composing a scaffold twice reports one view; every sample screen
  passes `screenId` — `doctor.py` gains that check.

## Track ui · design system and adaptive

Owns `core/ui` and `feature/gallery`. `ui.5` also edits `app/AppNavHost.kt` and the catalog
destinations; no app-track item touches those while it is open.

- [ ] **ui.1 Bundle the brand face** · S · `decision` D13
  Why: was 3.8. The scale is right and the face is `FontFamily.Default`.
  Done: Source Sans 3 in weights 400/600/700/800 under `core/ui/src/main/res/font/`;
  `AppFontFamily` points at them; the APK delta recorded in this line.
  Verify: every `@ScreenPreview` renders the face offline; `aapt2 dump badging` size before and after.

- [ ] **ui.2 Screenshot tests with Roborazzi** · M · `plugin` D14 · needs ui.1
  Why: was 4.1. 41 components × three variants and ten screens × twelve renders sit unasserted.
  Done: compatibility check first — Roborazzi **1.74.0** and `ComposablePreviewScanner` **0.9.3**
  are both current and stable (checked 2026-09-09), so the spike is only the toolchain: AGP 9.4,
  Gradle 9.6, JDK 25, Robolectric 4.16. Roborazzi
  applied through `convention.android.library.compose`; `ComposablePreviewScanner` records every
  `@ComponentPreview` and `@ScreenPreview` without duplicating them; goldens committed;
  `verifyRoborazziDebug` in the CI build job.
  Verify: break one padding value on purpose and the verify task fails on that image only; restore.

- [ ] **ui.3 Component behaviour tests** · M · `stable`
  Why: `core/ui/component` is 1,689 lines at 10 % coverage. Previews show; nothing asserts.
  Done: `ui-test-junit4` + Robolectric added to `convention.android.library.compose` for
  `src/test`; tests for the interactive components — `AppButton` (loading keeps width, disabled
  emits nothing), `AppTextField` (error always carries text), `AppCheckbox` (indeterminate),
  `AppSelect`, `AppTabs`, `AppStepper`, `AppSheet`, `AppDialog`.
  Verify: the package leaves 10 % in the Kover report; each test finds by `testTag`, never by text.

- [ ] **ui.4 Window size class drives density** · S · `stable`
  Why: `AppTheme.density` has a `SizeClass` and nothing sets it; a tablet gets the phone scale.
  Done: `AppTheme` reads `currentWindowAdaptiveInfo()` and picks compact or regular density and
  typography from it; previews gain a tablet variant.
  Verify: the tablet preview shows regular typography and the 56 dp touch target.

- [ ] **ui.5 List–detail for the catalog on wide screens** · M · `stable` · needs ui.4
  Why: was 7.10's useful half. The source system runs on tablets; the catalog is exactly a
  list-detail shape.
  Done: `adaptive-navigation3` **1.3.0**, stable as of 2026-09-09 — the `alpha` tag this item
  carried is retired, and only its Navigation 3 range still wants checking against the catalog's
  1.1.7; a `ListDetailSceneStrategy` on the `NavDisplay`; products and product detail carry the metadata;
  phones unchanged.
  Verify: emulator tablet profile shows both panes, phone profile one; process death four screens
  deep restores on both.

## Track app · shell and sample features

Owns `app/` and `feature/*` except `gallery` and `template`. `shell.*` is the app shell;
`feat.*` is one feature per item, each proving one capability the architecture has and no sample
uses. Each feature is a worktree of its own — they meet only in the registration files.

- [ ] **shell.1 Deep links** · M · `device`
  Why: was 7.1. Getting the back stack right on a cold-start deep link is the part people get wrong.
  Done: a `VIEW` intent filter on `MainActivity` for `<app>://product/{id}`, the scheme named
  after the app; a
  `DeepLinks.kt` in `:app` parsing a URI into a `NavKey`; a cold start builds Home → Categories →
  Products → Detail so Up walks back; a warm start pushes onto the current tab.
  Verify: `adb shell am start -d <app>://product/croissant` cold and warm, both land on
  the product with Up working; a `MainViewModelTest` case for the synthesised stack.

- [ ] **shell.2 Debug menu, dev and staging only** · M · `stable` D16
  Why: was 7.5's useful half. Flavor, base URL, session and "crash now" are what a tester needs,
  and the gallery has no business in a prod build.
  Done: `:feature:devmenu` (presentation, di) reached from Settings when a `prod` source set's
  `DebugMenu.enabled` is false and the others' is true, so R8 strips it; shows build info,
  `BASE_URL`, session, `ErrorTracker` test crash, LeakCanary and Chucker launchers; Components
  moves here from Settings.
  Verify: `prodRelease` mapping file contains no `feature.gallery` or `feature.devmenu` class;
  a `SettingsScreenTest` case for the entry present and absent.

- [ ] **shell.3 Theme setting** · M · `stable`
  Why: light / dark / system is the first preference every app grows, and no sample shows a
  preference read at the root.
  Done: `:feature:settings` gains `domain` and `data` (`--layers domain,data --force`) with a
  `ThemePreference` in Preferences DataStore; a segmented control on Settings; `MainActivity`
  applies it through `AppTheme(darkTheme = …)`.
  Verify: `SettingsViewModelTest` and a screen test; the choice survives a restart on the emulator.

- [ ] **shell.4 Notification tap-through** · S · `device` · needs shell.1, shell.2
  Why: the channel exists and nothing posts to it.
  Done: the debug menu posts a notification whose `PendingIntent` carries a product deep link.
  Verify: tapping it from a cold start lands on the product with Up working.

- [ ] **shell.5 Onboarding flow** · M · `stable`
  Why: a third flow beside auth and main, gated by a stored flag, is the shape of every first-run
  screen and the one flow switch the template does not show.
  Done: `:feature:onboarding` full stack with a `seen` flag in DataStore; `MainViewModel` combines
  it with the session into `Unknown / Onboarding / SignedOut / SignedIn`; the splash holds through
  `Unknown`; three pages on an `AppPager` added to `:core:ui` with `create_component.py` (one
  new file in the ui track's directory).
  Verify: `MainViewModelTest` for all four states; first cold start shows onboarding then Login,
  the second skips it.

- [ ] **feat.1 Favourites — proves Room** · M · `plugin` D18
  Why: Room was chosen (D7) and nothing uses it; `SnackbarAction` is API nobody raises.
  Done: `convention.android.room` in `build-logic/` (Room **2.8.4** + KSP **2.3.11**, versions in
  the catalog; KSP decoupled from the Kotlin version at 2.3.0 — last coupled release was
  `2.2.21-2.0.5` — so nothing has to match Kotlin 2.4.20, checked 2026-09-09, and the spike that
  remains is KSP against AGP 9.4 on the JDK 25 daemon); `:feature:catalog:data` gets a `CatalogDatabase` with products
  seeded from the in-memory list and a favourites table; a heart on product detail; a Favourites
  section on Home (Home depends on catalog `domain`, which is allowed); remove with an undo snackbar.
  Verify: a DAO round-trip test under Robolectric; ViewModel tests; a `ProductDetailScreenTest`
  tap toggles the heart; favourites survive a restart.

- [ ] **feat.2 Cart — proves cross-feature domain, tab badge, plurals, nav results** · L · `stable` · needs core.4, feat.1
  Why: `toPluralUiText` and `AlertPayload` are unused API; no feature depends on another's domain.
  Done: `:feature:cart` full stack on its own Room database; add from product detail; a Cart tab
  with a count badge (`TopLevelDestination` gains an optional badge flow); quantity on
  `AppStepper`; "N items" through `toPluralUiText`; remove with undo; checkout → confirm dialog →
  clear; "Add item" opens the catalog in picker mode and the product comes back through core.4.
  Verify: ViewModel tests including the plural at 1, 2 and 5; a screen test; the badge updates
  from another tab.

- [ ] **feat.3 Profile — proves PermissionGate and forms** · M · `device` · needs core.5
  Why: `PermissionGate` and `PermissionRationale` ship unused; no sample has a form with rules.
  Done: `:feature:profile` full stack, DataStore-backed; name and e-mail on `FieldState`; avatar
  from the Photo Picker (no permission) or the camera behind `PermissionGate(CAMERA)`; shown with
  `AppImage`; reached from Settings.
  Verify: ViewModel tests for every validator path; a screen test; on the emulator deny the camera
  twice and the gate shows the settings rationale while the picker still works.

- [ ] **feat.4 Search — proves inline error per content id** · M · `stable`
  Why: 0.12 made inline retry work per content id and no screen has two content states.
  Done: a search screen from the Categories top bar; `AppSearchField` with a 300 ms debounce and
  `flatMapLatest`; results and recent searches as two content ids, each with its own inline
  error and empty state; recents in DataStore.
  Verify: ViewModel test with `advanceTimeBy` for the debounce and one for a failure on one id
  leaving the other; a screen test.

- [ ] **feat.5 Catalog over the network** · M · `stable` D20 · needs core.1, core.2, feat.1
  Why: was 6.1's second half. The first remote-backed feature.
  Done: `RemoteCatalogDataSource` on `core.1`, DTOs and mappers in `data`, `DefaultCatalogRepository`
  on `core.2` writing into `feat.1`'s database; JSON fixtures in `dev`'s source set behind a
  `MockEngine` that takes a failure toggle, so the stale-cache path can be exercised by hand;
  `BuildConfig.BASE_URL` names the fixture host, so a screen still never writes a URL.
  Verify: repository tests on `MockEngine` for hit, miss and stale; on `dev`, load once, flip the
  toggle, browse from cache. Airplane mode proves nothing here — the engine is in-process.

- [ ] **feat.6 Session carries an id; `setUser` wired** · S · `stable`
  Why: `ErrorTracker.setUser` has nothing to be called with, and the docs say so instead of the code.
  Done: `Session(id, email)`; the mock login mints an opaque id; `MainViewModel` calls
  `setUser(id)` on sign-in and `setUser(null)` on sign-out; an old encrypted session reads as
  signed out rather than crashing.
  Verify: `MainViewModelTest` asserts both calls; `AesGcmAeadTest` unchanged.

- [ ] **feat.7 Tests for the data layers that exist** · M · `stable`
  Why: `feature/*/data` is 84 lines at 0 %, the cheapest coverage in the repo.
  Done: `DefaultAuthRepository`, `DefaultLocalAuthDataSource` (in-memory DataStore + `AesGcmAead`,
  no Keystore), `DefaultCatalogRepository`, `DefaultLocalCatalogDataSource`.
  Verify: none of the four packages reads 0 % in the Kover report.

- [ ] **feat.8 A screen test for every existing screen** · M · `stable` · needs qa.3
  Why: `LoginScreenTest` is the pattern and nine screens do not follow it.
  Done: SignUp, Categories, Products, ProductDetail, Home, Settings, Permissions, Gallery and
  GalleryDetail, each in the shape `qa.3` generates — renders the fixed state, finds by tag,
  asserts the event a tap emits.
  Verify: `./gradlew test`; each test fails when its screen's tag is renamed on purpose.

## Track quality · tests, CI, release

Owns `.github/`, `.maestro/`, `scripts/`, `feature/template`, `docs/`, `LICENSE`,
`baselineprofile/`.

- [ ] **qa.1 LICENSE** · S · `decision` Q6
  Done: the file, and the licence named in `README.md`.

- [ ] **qa.2 Maestro golden-path flows** · M · `device` D15
  Why: every device check so far went through `adb` by hand.
  Done: `.maestro/` flows for sign in → Home, browse to a product, log out, grant a permission,
  all by `id:` and never by text; how to run them in `README.md`; CI left for later.
  Verify: `maestro test .maestro` passes on the emulator; a flow fails when its tag is renamed.

- [ ] **qa.3 The template ships a screen test** · S · `stable`
  Why: the seven-file unit has a ViewModel test and no screen test, so every generated screen
  starts without one.
  Done: `TemplateScreenTest` and `TemplateArgsScreenTest` in `feature/template`, cloned by
  `create_screen.py` and `create_feature.py`; the unit is eight files; `doctor.py`'s unit check
  and `test_scripts.py` follow; `CLAUDE.md`'s screen table gains the row.
  Verify: generate a throwaway feature, its screen test runs and passes, `delete_feature.py`
  leaves the tree byte-identical.

- [ ] **qa.4 Generator output compiles in CI** · M · `stable`
  Why: was 7.15. `test_scripts.py` checks text; a template change that breaks generated code is
  found by the next user.
  Done: `test_scripts.py --with-gradle` generates a feature in the temp copy and compiles its
  presentation module; run on the weekly schedule job only.
  Verify: break `feature/template` on purpose and the weekly job fails.

- [ ] **qa.5 Hardware pass** · M · `device` Q5 · needs shell.1
  Why: the Keystore path, the startup benchmark and predictive back have never run outside an
  emulator, and the emulator ANRs.
  Done: on a physical device — session round trip through the real Keystore; `StartupBenchmark`
  with and without the profile, numbers recorded in this line; predictive back on every screen;
  "Don't keep activities" four screens deep; a cold deep link; TalkBack through Login and Catalog.
  Verify: the numbers, and one line per check here.

- [ ] **qa.6 Renovate runs** · S · `stable` Q5
  Done: a Renovate PR has been opened against the repo, or the app is installed and one appears
  within a week; `dependencyDashboard` on.

- [ ] **qa.7 `resourcePrefix` per feature** · S · `stable`
  Why: was K4. `CLAUDE.md` asks for `user_profile_` prefixes and nothing enforces them.
  Done: `convention.feature.presentation` derives `resourcePrefix` from the module path;
  `:core:ui` sets `app_`; existing strings already comply.
  Verify: add an unprefixed string on purpose and lint fails.

- [ ] **qa.8 Compose compiler metrics** · S · `stable`
  Why: was I6. `@Immutable` is everywhere; whether anything is still unstable is a guess.
  Done: `composeCompiler { metricsDestination / reportsDestination }` behind `-PcomposeMetrics`
  in the compose convention plugin; the first report read and any unstable parameter fixed.
  Verify: the report lists every `XState` as stable.

- [ ] **qa.9 Required checks on `main`** · S · `decision` Q5 D17
  Done: the `secrets`, `conventions` and `build` jobs required for merge; direct pushes off.
  Verify: a PR with a failing `doctor.py` cannot be merged.

## Backlog

Parked, not scheduled. Promote by moving into a track with the next number.

- Feature-owned nav graphs (was 7.3) — the `--graph` grouping in `AppNavHost` does the job today.
- Logger backend and remote config (the rest of 7.5).
- `doctor.py --fix` (7.6) — only when a check's fix is mechanical. `create_service.py` (7.7) and
  `check_strings.py` (7.8) stay parked under the no-new-scripts rule.
- Localisation pipeline (7.9) — waits on Q3. Per-app language picker with it.
- Theme generated from KSD tokens — waits on Q4; replaces the `:core:designsystem` split (7.10).
- ViewModel-readable permission state (7.13) — build inside the first feature that needs it.
- Module graph rendered and layer rules asserted at build time (was K3).
- WorkManager sync and Paging 3 — once `feat.5` has a server to sync with and a list longer than
  a page.
- Baseline profile regenerated on release tags.
- detekt, when 2.x is stable. Compose Preview Screenshot Testing, if D14 fails and it leaves alpha.
- `explicitApi()` on the `service/` modules.

Dropped: ADRs and a release doc (7.11) — the Decisions table is the record and `CLAUDE.md`'s
Commands section is the release doc. Undecoded Plan 1 codes (7.14) — decoded from the review:
C8 → `core.3`, C9 done (2.7), C10 → `core.5`, H7 done (5.6), I6 → `qa.8`, K3 → backlog,
K4 → `qa.7`.

## Done before this plan

| Plan | When | Landed |
|---|---|---|
| Plan 1 · 33 items | 2026-09-07 | The four P0 defects, `Outcome` / `BaseRepository` / `BaseViewModel`, `Screen()` owning navigation and commands, inline states, the generator suite, `doctor.py`, CI |
| Plan 2 · Phase 0 · 14 | 2026-09-08 | Docs match code; the review's small defects fixed |
| Plan 2 · Phase 1 · 8 | 2026-09-08 | Convention plugins, plain-JVM domains, AGP 9.4 / Kotlin 2.4.20, ktlint, shared fixtures, `gateway` merged into `data` |
| Plan 2 · Phase 2 · 8 | 2026-09-08 | Navigation 3, SplashScreen API, `SessionState`, tabs with per-tab history, transitions, permissions, snackbar action, plurals |
| Plan 2 · Phase 3 · 9 of 10 | 2026-09-08 | KSD design system in three layers, 41 components, every screen composed from them, accessibility and test ids, the gallery, three `doctor.py` checks |
| Plan 2 · Phase 4 · 3 of 4 | 2026-09-08 | Preview variants, the screen-test pattern, coverage report |
| Plan 2 · Phase 5 · 8 | 2026-09-08 | Flavors, signing, `ErrorTracker`, LeakCanary, gitleaks, baseline profile, release CI, encrypted session |

Carried over: 3.8 → `ui.1`, 4.1 → `ui.2` (tool changed, D14), 6.1 → `core.1` + `feat.5`,
6.2 → `core.2` + `feat.1`, 7.x as listed in the backlog.
