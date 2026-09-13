# Release E · the fixes the backlog was carrying, and a showcase that gives every component a home

Status: open · 2026-09-13
Agents: 1 · lane 0 15 · lane 1 Kotlin and core fixes 30 · lane 2 build and platform 46 · lane 3 the design system and its showcase 148
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D63–D65

Three plans were in flight on 2026-09-13 — D open with two tasks "blocked", B paused at its last
task, C a 380-point four-agent draft — and a backlog whose lines had been moved from plan to plan
since release A. E0P1 closed B and D as `v1.1.0`, deleted C, ranked everything that was left —
fixes first, then the build, then the design system, then new features — and took the top two
thirds (D62). What it did not take is [../../BACKLOG.md](../../BACKLOG.md) § Next, in order, and
is not carried forward by default.

Two things are worth knowing before the first task. **The two D tasks were never blocked here**:
`blocked: no JDK 25 toolchain` was true of the sandbox they were attempted in; this machine and CI
both provision it, so nothing in this plan waits on a toolchain. And **the showcase is one plain
feature, not nine games**: Inventory — a list, a detail, a four-step editor with create, edit and
delete — composes the sixteen `App*` files nothing outside `:core:ui` and the gallery names today,
in the shape a real app has. It is what release C was for, at a third of the cost.

**One agent, lanes in order: 1, then 2, then 3.** Lane 1 is a morning and touches files lanes 2 and
3 read, so it merges first. If the owner adds a second agent, it takes lane 3 once lane 1 has
merged; lanes 2 and 3 are disjoint by the table below. Nothing here needs a third.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

| File group | Owner lane | Tasks |
|---|---|---|
| `scripts/doctor.py`, `scripts/test_scripts.py`, `CLAUDE.md` | 1, then 2 | E1X1, E2P1 |
| `feature/{trips,cart}/presentation/**`, `.maestro/add-to-cart.yaml`, `.maestro/trips.yaml` | 1 | E1X1, E1X3 |
| `service/core/ui/**`, `feature/devmenu/**` | 1 | E1X2 |
| `feature/trips/data/**` | 1 | E1H1 |
| `app/**/TopLevelDestination.kt`, `lint.xml`, `core/ui/**/AppSectionHeader.kt` | 1 | E1T1, E1X3 |
| `build-logic/**`, `gradle/**`, every module build file that exists today, `docs/ai/DEPENDENCIES.md` | 2 | E2P1 |
| `.github/workflows/build.yml`, `.maestro/config.yaml`, the `appId` line of every flow, `docs/RELEASING.md` | 2 | E2H1, E2P2 |
| `app/src/debug/**`, `app/**/App.kt`, `docs/ai/ARCHITECTURE.md` | 2 | E2H2 |
| `core/ui/**` (after E1X3), `feature/gallery/**`, `docs/ai/reference/DESIGN-SYSTEM.md` | 3 | E3U1, E3S5, E3T1 |
| `feature/inventory/**`, `feature/home/**`, `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`, `.maestro/inventory.yaml` | 3 | E3S1–E3S6 |
| `docs/ai/CODEBASE.md` | 2 § Convention plugins · 3 § Module structure and § API | E2P1, E3S1, E3S3 |
| `docs/ai/reference/{FEATURES,DOMAIN,CORE,SERVICES}.md`, `docs/ai/RECIPES.md` | the task that changes the fact | — |

## Board

The board is [../../STATUS.md](../../STATUS.md) — one place to look, and no second copy to drift.
What is left here is why each task is there and what finishes it.

## Tasks

### E0P1 Two releases close, one opens, and the process learns one agent · 12 · decides D61, D62

**Why** The lane model was built for parallel agents and priced as if a context were free.
**Done when** `docs/ai/PROCESS.md` § One agent exists; B and D read `Status: closed`; `C.md` is
gone; `docs/CHANGELOG.md` holds `## v1.1.0 ·`; `docs/STATUS.md` holds this board and nothing paused;
`docs/BACKLOG.md` is ranked; D61 and D62 are rows; `python3 scripts/doctor.py` passes.
**Touches** `docs/**`, `CLAUDE.md`, `.claude/commands/{task,release}.md`, `scripts/README.md`.
**Checks** T0. **Depends** —

### E0X1 The emulator runner runs out of disk before it boots · 3

**Why** E2H1's proof run — the first maestro run this repository has on record — died before the
emulator existed: `sdkmanager` installing the API 35 system image hit *No space left on device*.
The job now assembles `devDebug` and `prodRelease` (R8) before the emulator, and the hosted runner
ships with several GB of tooling the job never touches — .NET, CodeQL, the Android NDK. Nothing
in E2H1's diff is wrong; the runner is full. Appended by the owner on 2026-09-13. The second run,
with the disk freed, found the next thing: `.gitignore`'s `release/` — meant for Android Studio's
signed-build output — had swallowed E2H2's `app/src/release/` source set, so `main` had a
`prodRelease` that compiled on the machine that wrote it and nowhere else.
The third run booted and ran 5 of 7 flows: `trips.yaml` could not see `TripsListScreen` on a
cleared app, because `TripsListViewModel` showed its empty table as a `ContentState`, which the
chrome draws *instead of* the screen — scaffold, up arrow and screen id included. The list now
draws `AppEmptyState` inside its shell, as `TripsScreen` does; whether the chrome should ever
replace a shell is a backlog line. The seventh flow died in 115 ms with the emulator, and
`--debug-output` was writing where the upload step did not look, so the next run keeps
Maestro's output and logcat.
The fourth found that the emulator action runs each `script:` line in a shell of its own, so
the debug path had been `/dev/.maestro`; the flow commands are one script file now. The fifth
run passed: 7/7 on `devDebug`, then `prodRelease` installed and signed in.
**Done when** the maestro job frees the unused tooling before the emulator step, in one plain
`rm -rf` step (no third-party action: D27 needs a first-party alternative tried first, and `rm`
is it); `.gitignore` ignores `/app/release/` and nothing named `release` elsewhere, and
`app/src/release/**` is tracked; `TripsListScreen` keeps its scaffold when the table is empty
and `tripsList_empty` is its id; the job uploads `maestro-debug/**` on failure with logcat beside
it; `gh workflow run build.yml` followed by `gh run watch` is green with both Maestro passes in
the log — the proof E2H1 still owes.
**Touches** `.github/workflows/build.yml` (the maestro job), `.gitignore`, `app/src/release/**`,
`feature/trips/presentation/**` (`TripsList*`, its strings and goldens), `docs/BACKLOG.md`.
**Read** the failed run `34764746105` · `.github/workflows/build.yml` § maestro.
**Checks** T0 — YAML only; the proof is the dispatched run. **Depends** —

### E1X1 The test ids rejoin the closed vocabulary (was D1X4) · 12

**Why** D60 settled the vocabulary on 2026-09-12 — eighteen words, the component-named tags
renamed — and the commit that recorded it touched `docs/DECISIONS.md` and `docs/STATUS.md` only.
`CLAUDE.md:112-114` still lists thirteen words, the eighteen tags D60 names are still in the code,
and `doctor.py` has no check, so a green run says nothing. The rule is either enforced or it is not
a rule.
**Done when** `CLAUDE.md` § Test identifiers lists the eighteen words D60 names; the five
component-named tags are `Field` (`cart_quantityField`, `tripWizard_travelersField`,
`tripWizard_budgetField`, `tripWizard_typeField`, `tripWizard_advancedField`),
`cart_emptyState` and `trips_emptyState` are `cart_empty` and `trips_empty`, and
`tripWizard_reviewSummary` is `tripWizard_reviewTile`; `check_test_id_vocabulary` is in
`scripts/doctor.py` with its fixture in `scripts/test_scripts.py`, folds case and plural (so
`cart_item`, `categories_list` and `tripDetail_tabs` pass, as D60 requires), and fails on a planted
`foo_barStepper`; `grep -c 'def check_' scripts/doctor.py` is 39; `.maestro/add-to-cart.yaml`
resolves the renamed empty state, which `check_maestro_ids_exist` proves; `python3 scripts/doctor.py`
and `python3 scripts/test_scripts.py` pass; `./gradlew :feature:trips:presentation:test
:feature:cart:presentation:test verifyRoborazziDebug` passes in one invocation — a `testTag` is not
drawn, so no golden moves.
**Touches** `scripts/doctor.py`, `scripts/test_scripts.py`, `CLAUDE.md` § Test identifiers,
`feature/{trips,cart}/presentation/src/{main,test}/**`, `.maestro/add-to-cart.yaml`.
**Read** `docs/DECISIONS.md` D60 · `CLAUDE.md:104-117` · `scripts/doctor.py:971` §
`check_maestro_ids_exist` and the `declared_test_ids()` it reads · the tags themselves:
`grep -rhoE 'testTag\("[a-zA-Z]+_[a-zA-Z]+"\)' feature/*/presentation/src/main | sort -u`.
**Steps** 1. The check first, against today's code, so its failure list *is* the rename list —
if it disagrees with D60's eighteen, D60 is right about the rule and the check is wrong about the
reading. 2. Rename in `main`, then in the `*ScreenTest.kt` beside each, then the one Maestro flow.
3. `CLAUDE.md` § Test identifiers: the eighteen words, in one line, and nothing else changes there.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** —

### E1X2 A toast wears the theme · 6 · decides D63

**Why** `Screen.kt:103` renders `UiCommand.ShowToast` with `Toast.makeText` — a window-level
Android toast that ignores the theme, sits outside every screen test and is the one thing
`Screen()` draws that D50 did not move into the chrome. The snackbar path *was* moved:
`AppScreenChrome.SnackbarHost` renders through `AppToast`. So there are two mechanisms for one job,
and the rule-breaking one is what `BaseViewModel.handleError` (`BaseViewModel.kt:435`) uses for every
`ServerError`, and what `CartViewModel.kt:115` and `DevMenuViewModel.kt:59,66` call.
**Decide first** fold `ShowToast` into the snackbar — `showToast` becomes a snackbar with no
action, `UiCommand.ShowToast` and the Android import go — or add a `Toast` slot to `ScreenChrome`
and keep both → D63. Recommended: **fold.** A snackbar is per screen and all four call sites speak
to the screen they are on; a message that has to outlive its screen is a different feature and
nothing here has one.
**Done when** `grep -rn 'android.widget.Toast' --include='*.kt' service core feature app` returns
nothing; `grep -rn 'ShowToast' --include='*.kt' service feature app` returns nothing, or only the
chrome if D63 kept it; `BaseViewModelTest` asserts the command a `ServerError` in `Alert` mode
produces; `./gradlew test` passes; `docs/ai/reference/SERVICES.md` and `docs/ai/CODEBASE.md` § API
(`UiCommand` row) say what a toast is now.
**Touches** `service/core/ui/src/main/kotlin/.../component/Screen.kt`, `.../event/UiCommand.kt`,
`.../viewmodel/BaseViewModel.kt`, `service/core/ui/src/test/.../viewmodel/BaseViewModelTest.kt`,
`feature/cart/presentation/.../cart/CartViewModel.kt`,
`feature/devmenu/presentation/.../devmenu/DevMenuViewModel.kt`, `docs/ai/reference/SERVICES.md`,
`docs/ai/CODEBASE.md`, `docs/DECISIONS.md`.
**Read** `Screen.kt:96-125` · `BaseViewModel.kt:185-210,425-440` ·
`core/ui/src/main/kotlin/.../component/AppScreenChrome.kt:105-115` · `UiCommand.kt`.
**Steps** 1. D63. 2. `showToast(message)` delegates to `showSnackbar(message)`; delete the command
and the branch in `Screen()`. 3. The test. 4. Two doc rows.
**Checks** T1 + the whole `./gradlew test`, because `service/` moved. **Depends** —

### E1H1 A stored trip the code no longer understands does not crash the read · 6

**Why** `feature/trips/data/.../database/Mappers.kt:13-15` parses `type` with `TripType.valueOf`
and the two dates with `LocalDate.parse`, inside the `map` of a Room `Flow`. A renamed enum
constant in a later version, or one hand-edited row, throws inside the flow; `observe()` retries
three times and emits a failure, and `TripsScreen` shows an error for every trip because one row is
bad — with no migration that could fix it, since the schema did not change. No `@TypeConverter`
exists anywhere (`grep -rn TypeConverter feature/*/data/src/main` is empty), so every entity
hand-rolls its string columns and the next one will copy this.
**Done when** `feature/trips/data` holds a converters class for `LocalDate` and `TripType`,
registered on `TripsDatabase`, and `TripEntity`'s columns carry the domain types; the storage stays
`TEXT`, so the version stays 1 and `git diff --stat feature/trips/data/schemas` is empty; an unknown
type name reads as a fallback rather than throwing, and a `TripsDatabaseTest` case inserts a raw
row with `type = 'Cruise'` and reads a trip back; `Mappers.kt` stops parsing; `./gradlew
:feature:trips:data:test` passes; `docs/ai/RECIPES.md` § Changing a Room schema says converters
are how a non-primitive column is stored, so E3S1 follows it; `docs/ai/reference/DOMAIN.md` § What
holds the data says so in one clause.
**Touches** `feature/trips/data/src/main/kotlin/.../database/{Entities,Mappers,TripsDatabase}.kt`,
a new `Converters.kt` beside them, `feature/trips/data/src/test/.../TripsDatabaseTest.kt`,
`docs/ai/RECIPES.md`, `docs/ai/reference/DOMAIN.md`.
**Read** the three database files · `TripsDatabaseTest.kt` · `docs/ai/RECIPES.md:81-94`.
**Steps** 1. Converters, then entity types, then delete the parsing from the mapper. 2. The
fallback is `TripType.Leisure` and a comment saying why it is not a throw. 3. The raw-row test.
4. The recipe.
**Checks** T1. **Depends** —

### E1X3 `AppSectionHeader`'s action carries a test id · 3

**Why** `core/ui/.../component/AppSectionHeader.kt:20-24` takes `actionLabel` and `onAction` and
no tag for the action, so `TripsScreen.kt:60`'s "view all" — the only route to `TripsListScreen` —
is reachable by text alone, which `CLAUDE.md` § Test identifiers forbids. `.maestro/trips.yaml`
stops at the tab root for exactly this reason and says so.
**Done when** `AppSectionHeader` takes `actionTestTag: String? = null` and puts it on the action;
`TripsScreen` passes `trips_viewAllButton`; `.maestro/trips.yaml` taps it and asserts
`TripsListScreen`; `python3 scripts/doctor.py` passes, `check_maestro_ids_exist` included;
`./gradlew :core:ui:test :feature:trips:presentation:test verifyRoborazziDebug` passes in one
invocation with no golden moved.
**Touches** `AppSectionHeader.kt`, `feature/trips/presentation/.../trips/TripsScreen.kt` and its
`TripsScreenTest.kt`, `.maestro/trips.yaml`, `docs/ai/reference/DESIGN-SYSTEM.md` (the paragraph
on `navigateUpTestTag` gains its sibling).
**Read** `AppSectionHeader.kt` · `TripsScreen.kt:55-70` · `.maestro/trips.yaml` ·
`docs/ai/reference/DESIGN-SYSTEM.md` § Components.
**Checks** T1 + the whole `./gradlew test`, because `core/` moved. **Depends** —

### E1T1 Three small things the review found · 3

**Why** `app/**/AppNavHost.kt` § `mainEntries` lists the five trips destinations inline while every
other tab has its own `xEntries` block and the file's KDoc promises one per tab;
`TopLevelDestination.label` is a bare `Int` where `@StringRes` lets lint catch a wrong id; and
`lint.xml:3-4` says it is applied through a `lint { }` block in each module's build file, which
B0P1 moved into the convention plugin.
**Done when** `grep -c 'Entries(backStack)' app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt`
is one higher than today; `grep -c '@StringRes' app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt`
is 1; `lint.xml`'s header names `build-logic`; `./gradlew :app:test` passes.
**Touches** `app/**/AppNavHost.kt`, `app/**/TopLevelDestination.kt`, `lint.xml`.
**Read** `AppNavHost.kt` § `mainEntries` and `catalogEntries` · `TopLevelDestination.kt:25-35`.
**Checks** T1. **Depends** —

### E2P1 Every library dependency moves into a convention plugin · 25 · decides D64

**Why** `CLAUDE.md`: *"A module build file is a `plugins` block and its project dependencies.
Nothing else."* Five break it: `service/core/ui/build.gradle.kts` (sixteen library lines, plus
`testFixtures { enable = true }` by hand where `convention.android.library.testfixtures` exists for
exactly that), `service/core/data` (three), `service/core/domain` (`java-test-fixtures` and one),
`core/ui` (one), `app` (three, one of them `devImplementation`). `check_no_duplicated_android_config`
(`scripts/doctor.py:804-826`) reads five Android settings and nothing about dependencies, so a
green run says nothing. Filed in B's lane 1, deferred in D for escalating T1 — the right shape for
a lane whose whole job is the build.
**Decide first** what a module build file may still hold besides plugins and project
dependencies → D64. Recommended: **`resourcePrefix` only.** It is per module by nature.
Flavor-specific dependencies move too — `convention.android.application` already iterates
`ProjectConfig.Flavor`, so `devImplementation` is one `add("devImplementation", …)` there — and
`testFixtures` blocks go to the plugin that exists for them.
**Done when** `grep -rl 'libs\.' --include='build.gradle.kts' . | grep -v '^./build-logic' | grep -v '^./build.gradle.kts'`
returns nothing; `grep -rn 'testFixtures {' --include='build.gradle.kts' service core feature app`
returns nothing; a `check_module_build_files_are_thin` in `scripts/doctor.py` fails on a planted
`implementation(libs.junit)` in a module and on a planted `testFixtures {` block, with a fixture
each in `scripts/test_scripts.py`; `python3 scripts/export_service.py --to /tmp/x --package com.acme.app --dry-run`
lists the new plugins among what it copies, so the export recipe survives; `./gradlew
:app:assembleDevDebug test` passes; `versionCodeFor` in `build-logic/src/main/kotlin/ProjectConfig.kt`
loses its `it!!` on the way — two lines in a file this task already opens; `docs/ai/CODEBASE.md`
§ Convention plugins and `docs/ai/DEPENDENCIES.md`'s "Applied by" column match the build.
**Touches** `build-logic/src/main/kotlin/*.kt` (new `ServiceCoreUiConventionPlugin`,
`ServiceCoreDataConventionPlugin`, `ServiceCoreDomainConventionPlugin`; `CoreUiConventionPlugin`
and `AndroidApplicationConventionPlugin` grow), `build-logic/build.gradle.kts`,
`gradle/libs.versions.toml` (the plugin aliases), the five module build files, `scripts/doctor.py`,
`scripts/test_scripts.py`, `scripts/export_service.py` if it names plugins, `docs/ai/CODEBASE.md`,
`docs/ai/DEPENDENCIES.md`, `docs/DECISIONS.md`.
**Read** `build-logic/src/main/kotlin/CoreUiConventionPlugin.kt` (B2T1's precedent) ·
`build-logic/build.gradle.kts` § `gradlePlugin` · `gradle/libs.versions.toml:161-173` ·
`scripts/doctor.py:804-826` · `scripts/export_service.py` for what it copies ·
`docs/ai/CODEBASE.md` § Known constraints, before touching `build-logic/`.
**Steps** 1. D64. 2. One plugin per service module that needs libraries, named for the module, so
a copied `service/` brings its plugins with it. 3. `core/ui`'s one line into `convention.core.ui`;
`:app`'s three into the application plugin. 4. The check, written against today's five so its
failure list is the work list. 5. Docs.
**Checks** T1 + the whole `./gradlew test` + `python3 scripts/test_scripts.py`. **Depends** —

### E2H1 The release build is launched, not just assembled · 12

**Why** `build.yml`'s release job says it itself: *"R8 runs nowhere else… a keep rule that goes
missing would show up when someone cuts a release, which is the worst time to find it."* The weekly
run assembles and lints `prodRelease`; nothing installs or starts it. `app/src/main/keepRules/rules.keep`
is AGP's template with no rule of its own, so every keep rule in the build is a library's consumer
rule, and what those miss — the app's `@Serializable` route keys restored by Navigation 3, Koin's
reflective `*Of` constructors, Room's generated code — fails at first launch, not at compile time.
Auth is in memory (`DefaultAuthService`), so the sign-in flow runs against `prod` without a
backend.
**Done when** the maestro job also runs `:app:assembleProdRelease`, installs the APK it produces
(signed with the CI debug key, which is fine — this run proves the app starts, not who signed it)
and runs `.maestro/sign-in.yaml` against `com.example.androidproject1`; every flow's `appId` reads
an `APP_ID` env with `config.yaml` defaulting it to the `dev` id, so the dev flows are unchanged;
`gh workflow run build.yml` followed by `gh run watch` is green with both passes in the log;
`docs/RELEASING.md` says the weekly run starts the release build; `CLAUDE.md` § Checks' T4 row
says "the end-to-end flows on `devDebug` and a launch of `prodRelease`".
**Touches** `.github/workflows/build.yml` (the maestro job), `.maestro/config.yaml`, the `appId`
line of every `.maestro/*.yaml`, `docs/RELEASING.md`, `CLAUDE.md` § Checks.
**Read** `.github/workflows/build.yml:317-380` · `.maestro/config.yaml` · `.maestro/sign-in.yaml` ·
`docs/RELEASING.md` · `CLAUDE.md` § Checks.
**Steps** 1. Maestro reads `${APP_ID}` from `-e APP_ID=…`; one variable, two invocations in the
emulator step. 2. Assemble both before the emulator boots. 3. Dispatch it and watch; paste the run
URL into the pull request.
**Checks** T0 — YAML only; the proof is the dispatched run. **Depends** —

### E2H2 A network security config and StrictMode for the debug build · 6

**Why** No `app/src/debug/` source set exists. Without a network security config a debug build
trusts no user certificate, so it cannot be proxied through Charles or mitmproxy at all; without
StrictMode a main-thread disk read — DataStore's first read behind `MainViewModel`, a Room query
that lost its dispatcher — goes unnoticed until a real device stutters. Both are one file, both are
debug-only, and both were verified absent in D and left as hardening.
**Done when** `app/src/debug/res/xml/network_security_config.xml` trusts user certificates for
debug and a debug manifest overlay references it, so the main manifest and `prodRelease` are
untouched; `App.onCreate` calls an `installDebugTooling()` that the `debug` source set implements
with `StrictMode` thread and VM policies on `penaltyLog()` — not death: LeakCanary and Compose
tooling trip it — and the `release` source set implements as a no-op, the way `DebugMenu` is
per-flavor; `./gradlew :app:assembleDevDebug :app:assembleProdRelease` passes;
`docs/ai/ARCHITECTURE.md` § Environments says what the debug build type adds;
`docs/ai/reference/CORE.md`'s `:app` table gains the row.
**Touches** `app/src/debug/**` (new), `app/src/release/**` (new, the no-op), `app/**/App.kt`,
`docs/ai/ARCHITECTURE.md`, `docs/ai/reference/CORE.md`.
**Read** `app/src/dev/kotlin/com/example/androidproject1/debug/DebugMenu.kt` and its `prod`
twin — the source-set-pair pattern · `app/**/App.kt` · `app/src/main/AndroidManifest.xml`.
**Checks** T1 + `./gradlew :app:assembleProdRelease`. **Depends** —

### E2P2 The dependency graph is submitted · 3

**Why** Nothing watches the resolved dependencies for known vulnerabilities. D held this "on value:
Renovate is parked (D26), so it submits to nothing that acts on it" — and the premise is wrong.
GitHub's Dependabot *alerts* read the submitted dependency graph and need neither Renovate nor
Dependabot updates; for a Gradle project, submitting the graph is the only way to get them at all.
**Done when** the build job's `gradle/actions/setup-gradle@v4` step carries
`dependency-graph: generate-and-submit`, guarded to pushes to `main` (a pull request from a fork
has no write token, and the job needs `contents: write` for the submission); the repository's
Insights → Dependency graph lists the Gradle manifests after the first `main` run;
`docs/ai/DEPENDENCIES.md` § Updating says where an alert comes from and that Renovate is still
parked.
**Touches** `.github/workflows/build.yml`, `docs/ai/DEPENDENCIES.md`.
**Read** `.github/workflows/build.yml:104-135` · `docs/ai/DEPENDENCIES.md` § Updating.
**Checks** T0. **Depends** —

### E3U1 The design system stops speaking POS · 12

**Why** The KSD import came from a point-of-sale system and kept its vocabulary. `TagTone` is
`Neutral, Paid, Open, Void, Info` (`core/ui/.../component/AppTag.kt:15`); `AppTheme.colors.statusVoid`
is a colour role; the previews say "Print receipt", "Void this order?", "Order sent to the kitchen",
and the gallery repeats them — 56 word-boundary hits across 24 files
(`grep -rniwE 'till|void|cash|receipt|cashier' core/ui/src/main feature/gallery/presentation/src/main`).
A feature component already leans on it: `TripStatusDot` marks a completed trip `TagTone.Void`,
which is a template teaching that a finished trip is void. `pointerPresent()` in
`core/ui/.../theme/Density.kt:75-88` is not POS — a mouse is a mouse — and stays.
**Done when** `TagTone` names states, not transactions — `Neutral, Positive, Warning, Negative,
Info` or equivalent — and the colour roles follow (`statusVoid` → `statusNegative` and its
siblings); the grep above returns nothing; the gallery's copy reads as a generic app;
`./gradlew test verifyRoborazziDebug` passes after `recordRoborazziDebug` on the components whose
preview copy changed, and **the images were opened before they were committed**;
`docs/ai/reference/DESIGN-SYSTEM.md` § Roles names the tones.
**Touches** `core/ui/src/main/kotlin/.../component/*.kt` (`AppTag`, `AppAvatar`, `AppStatusDot`,
the previews), `core/ui/.../theme/Color.kt`, `feature/gallery/presentation/**`,
`feature/trips/presentation/.../component/TripStatusDot.kt`, the goldens under
`core/ui/src/test/screenshots` and `feature/gallery/presentation/src/test/screenshots`,
`docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `AppTag.kt:1-40` · `core/ui/.../theme/Color.kt` § status roles · the grep above.
**Steps** 1. Rename the enum and the roles; let the compiler find every user. 2. The preview and
gallery copy — real sentences a generic app would show, not lorem. 3. Record, open, commit.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + the whole
`./gradlew test`, because `core/` moved. **Depends** — *(runs first in lane 3, so Inventory never
names a tone about to be renamed)*

### E3S1 Inventory: the feature, its table and the list · 25

**Why** Sixteen `App*` files are composed by nothing outside `:core:ui` and the gallery —
`AppAvatar`, `AppBadge`, `AppBottomNav`, `AppCheckbox`, `AppDialog`, `AppFormField`, `AppImage`,
`AppNavRail`, `AppRadio`, `AppScreenChrome`, `AppSelect`, `AppSheet`, `AppSlider`, `AppSpinner`,
`AppToast`, `AppToolbar` (the scan is in `docs/ai/reference/DESIGN-SYSTEM.md`). D38 holds them until
a real screen has had its chance. Inventory is that screen, six tasks long: things you own, each
with a category, a condition, a quantity, a price, tags, an owner, a picture and notes — a field for
every kind of control — with a list, a detail and a four-step editor. This task is the skeleton and
the list.
**Done when** `python3 scripts/create_feature.py inventory` has produced four layers, and the
generated first screen *is* the list, the way `TripsScreen` is the dashboard;
`feature/inventory/domain` holds `Item`, `ItemCategory`, `ItemCondition`, `ItemTag`,
`InventoryRepository` (`observeItems`, `observeItem(id)`, `getItem`, `saveItem`, `deleteItems(ids)`)
and a `FakeInventoryRepository` in `testFixtures`; `feature/inventory/data` holds a Room
`InventoryDatabase` with an `items` table using the converters E1H1 established, seeded from a
fixture list of twelve items on first read exactly as `DefaultLocalDestinationsDataSource` seeds,
schema exported; `InventoryScreen` renders `AppListItem` rows with `AppAvatar(owner)` leading and
`AppTag(condition)` trailing, an `AppSearchField` filtering by name in the view model, `AppSkeleton`
rows until the first emission, `AppEmptyState` when the filter matches nothing and `AppFab` to a new
item; ids `inventory_list`, `inventory_item`, `inventory_searchField`, `inventory_skeleton`,
`inventory_empty`, `inventory_newButton`; both tests; Czech for every key; `python3
scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug` passes in one invocation;
`docs/ai/CODEBASE.md`, `docs/ai/reference/FEATURES.md` and `docs/ai/reference/DOMAIN.md` list it.
**Touches** `feature/inventory/**` (new), `settings.gradle.kts`, `core/di/**`,
`app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`, `docs/ai/CODEBASE.md`,
`docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Read** `feature/trips/**` as the closest shape — the Room plugin pair, the seeding data source,
the fakes · `docs/ai/RECIPES.md` § A new feature, § A new data source, § Changing a Room schema ·
`CLAUDE.md` § Screen structure, § Test identifiers.
**Steps** 1. `create_feature.py inventory --dry-run`, then for real. 2. `create_datasource.py
inventory LocalInventory --repository`, then swap the DataStore body for Room as trips does.
3. Domain: `ItemCategory` (Tools, Electronics, Furniture, Books, Other), `ItemCondition` (New,
Good, Worn), `ItemTag` (Fragile, Lent, ForSale, Favourite); `Item.priceMinor: Long` in minor units
like `Product.price`; `Item.owner: String`, a name `AppAvatar` draws from; `Item.imageUrl: String?`,
a fixture URL the offline `dev` build never loads — which is the honest `AppImage` state, and D20
stands. 4. The list. 5. `recordRoborazziDebug`, then **open what it wrote**. 6. Docs.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** E1H1, E3U1

### E3S2 Inventory: the four-step editor · 25 · after E3S1

**Why** The form group is where most of the homeless components live — `AppSelect`, `AppRadio`,
`AppCheckbox`, `AppSlider`, `AppFormField` — and a four-step editor is the one screen that wants
all of them at once. Create and edit are one screen: an editor edits an item by id, and a new item
is a new id, minted by the list.
**Done when** `python3 scripts/create_screen.py inventory InventoryEditor --with-args 'itemId:String'`
has produced the unit; the editor loads the item with that id if it exists and starts blank if it
does not; four steps behind `AppStepProgress` — **Basics**: `AppTextField` name (required, through
`FieldState` and `required()` from `service.core.ui.form`), `AppSelect` category, `AppRadioGroup`
condition, `AppDateField` acquired on; **Quantity and price**: `AppStepper` quantity, `AppSlider`
price with `LocalFormats.current.money` as its `valueLabel`, `AppSwitch` insured; **Tags and
owner**: one `AppCheckbox` per tag under a master whose `CheckState.Indeterminate` shows when some
are ticked, `AppSelect` owner beside an `AppAvatar` of the choice, `AppTextField` image URL over an
`AppImage` preview, `AppTextField` notes; **Review**: an `AppDescriptionList` inside an `AppCard`
and the save button — and every control without a label of its own sits in an `AppFormField`
carrying the helper or error text; `DiscardBackHandler` guards a dirty form as `TripWizard` does;
save writes through `saveItem` and pops; `InventoryEditorViewModelTest` covers each step's
`canContinue`, the master checkbox's three states and the save; `InventoryEditorScreenTest` finds
every field by id — `inventoryEditor_stepProgress`, `_nameField`, `_categoryField`,
`_conditionField`, `_acquiredField`, `_quantityField`, `_priceField`, `_insuredSwitch`,
`_tagsGroup`, `_allTagsCheckbox`, `_ownerField`, `_imageField`, `_notesField`, `_reviewTile`,
`_nextButton`, `_upButton`; one `@ScreenPreview` per step; both locales; `./gradlew test
verifyRoborazziDebug` in one invocation.
**Touches** `feature/inventory/presentation/.../inventoryeditor/**`,
`feature/inventory/presentation/.../component/**` (one file per step, through
`create_component.py --feature inventory`), the feature's `strings.xml` pair,
`app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`.
**Read** `feature/trips/presentation/.../tripwizard/*` and `.../component/TripDetailsStep.kt` (the
closest) · `service/core/ui/.../form/{FieldState,Validator,Form}.kt` · the signatures of
`AppSelect`, `AppRadio`, `AppCheckbox`, `AppSlider`, `AppFormField` in `core/ui`.
**Steps** 1. The generator, with `--with-args`. 2. One component per step — a screen file holds the
screen and its previews and nothing else. 3. `FieldState` for the text fields; the rest is plain
state. 4. Record, open, commit.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** E3S1

### E3S3 Inventory: the detail, its sections and delete · 25 · after E3S1

**Why** `AppBottomNav` and `AppNavRail` are two navigation components the app never composes,
because `:app` uses the adaptive suite for the tabs. A detail with sections is the honest place for
them — and `SizeClass` decides which one, which makes this the first screen in the app that reads
the width class rather than leaving it to the suite.
**Done when** `python3 scripts/create_screen.py inventory InventoryDetail --with-args 'itemId:String'`
has produced the unit; the screen observes `observeItem(id)`, so an edit elsewhere shows up
without a reload; an `AppImage` header (its placeholder state offline), `AppAvatar` of the owner
with a status tone by condition, `AppTag`s for the tags, an `AppBadge` on any section with
something in it; three sections — Overview (`AppDescriptionList`), Notes, History (derived:
acquired on, last updated) — switched by `AppBottomNav` on a compact width and `AppNavRail`
otherwise, chosen from `SizeClass`; an `AppMenu` off the top bar with Edit and Delete; Delete goes
through `setAlert` carrying an `AlertPayload` with the id and comes back as
`SystemEvent.AlertResult.Confirmed` — the first real payload user, which `docs/ai/CODEBASE.md` § API
says nothing has yet; a confirmed delete pops the screen; ids `inventoryDetail_imageValue`,
`_ownerValue`, `_sectionTab`, `_menuButton`, `_editItem`, `_deleteItem`, `_notesValue`,
`_historyList`; a `@ScreenPreview` per width; both tests; both locales; `./gradlew test
verifyRoborazziDebug` in one invocation.
**Touches** `feature/inventory/presentation/.../inventorydetail/**`, `.../component/**`, the
`strings.xml` pair, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`, `docs/ai/CODEBASE.md` (the
`AlertPayload` row), `docs/ai/reference/FEATURES.md`.
**Read** `feature/trips/presentation/.../tripdetail/*` · `core/ui/.../theme/Density.kt` §
`SizeClass` · `service/core/ui/.../state/AlertState.kt` · `SettingsViewModel` for confirm-then-act
· the `AppBottomNav` and `AppNavRail` signatures.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** E3S1

### E3S4 Inventory: editing an item reuses the editor · 12 · after E3S2, E3S3

**Why** Create without edit is half a CRUD, and the editor already takes an id.
**Done when** Edit on the detail opens the editor prefilled from the item; `isDirty` compares
against the loaded item rather than against blank, so opening and leaving asks nothing; save keeps
the id, and the detail's `observeItem` shows the change without a reload; the top bar says New or
Edit from the state; `InventoryEditorViewModelTest` gains the prefilled path and the
untouched-form-is-not-dirty case; `InventoryDetailScreenTest` asserts the Edit event; both locales;
`./gradlew :feature:inventory:presentation:test verifyRoborazziDebug` in one invocation.
**Touches** `feature/inventory/presentation/.../{inventoryeditor,inventorydetail}/**`,
`app/**/AppNavHost.kt` (the lambda from detail to editor), the `strings.xml` pair.
**Read** E3S2's and E3S3's files · `feature/trips/presentation/.../tripwizard/TripWizardState.kt`
§ `isDirty`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** E3S2, E3S3

### E3S5 Inventory: filter sheet, sort, and a selection toolbar · 25 · after E3S1

**Why** `AppSheet`, `AppToolbar` and `AppSpinner` are the last three the list can home, and each has
a real job on one. It is also the second screen to use `saved()` — the seam B2H3 added and one
screen consumes — so the seam earns its place or is shown not to.
**Done when** a filter `AppIconButton` in the top bar wearing an `AppBadge` with the count of active
filters opens an `AppSheet` holding `AppSelect` category, one `AppCheckbox` per tag under a
tri-state master, `AppSlider` maximum price with a money label, and a Clear `AppButton`; a sort
`AppMenu` (name, price, acquired); the filtered, sorted list is derived in the view model and never
in a composable; a long press on a row enters selection mode — `AppToolbar` "N selected" replaces
the top bar with Delete and a favourite toggle, rows show `AppCheckbox`, the toolbar's own master is
indeterminate when some are selected; Delete confirms through `setAlert` with the ids as payload and
calls `deleteItems`; a row whose write is in flight shows `AppSpinner` in its trailing slot from a
`pendingIds` set in state; filter and sort survive process death through `saved()`;
`OverlayScreenshotTest` gains the sheet open at the narrowest phone; ids `inventory_filterButton`,
`_filterBadge`, `_filterSheet`, `_categoryField`, `_tagsGroup`, `_allTagsCheckbox`,
`_maxPriceField`, `_clearButton`, `_sortButton`, `_sortItem`, `_selectionGroup`,
`_selectAllCheckbox`, `_deleteButton`, `_favouriteButton`, `_itemCheckbox`, `_itemProgress`; both
tests; both locales; `./gradlew test verifyRoborazziDebug` in one invocation.
**Touches** `feature/inventory/presentation/.../inventory/**`, `.../component/**`, the
`strings.xml` pair, `core/ui/src/test/.../screenshot/OverlayScreenshotTest.kt`.
**Read** the `AppSheet`, `AppToolbar`, `AppMenu`, `AppSpinner` signatures · `BaseViewModel.saved`
and `ProductSearchViewModel`, its one consumer · `core/ui/src/test/.../OverlayScreenshotTest.kt`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + the whole
`./gradlew test`, because a `core/` test moved. **Depends** E3S1

### E3S6 Home opens Inventory, and a flow drives the whole loop · 12 · after E3S4, E3S5

**Why** Five tabs is Material's ceiling and Trips has the fifth (D59), so Inventory is reached from
Home — the one tab root with no cross-feature push yet; its navigation type is empty on purpose. A
card saying how many items there are is the entry, and a Maestro flow is what stops this feature
shipping unreachable, which is exactly what happened to Trips.
**Done when** `HomeState` gains `inventoryCount` from `InventoryRepository.observeItems` — Home's
`presentation` depends on `feature/inventory/domain`, which `CLAUDE.md` allows, and its build file
gains that one project line; an `AppCard` on Home shows the count with an Open action tagged
`home_inventoryCard`; `HomeNavigation.OpenInventory` is wired in `AppNavHost.homeEntries` as a
lambda, exactly as `settingsEntries` wires Profile; `.maestro/inventory.yaml` signs in, opens the
card, creates an item through all four steps, opens it, edits it, deletes it and asserts the list —
every step by id; `python3 scripts/doctor.py` passes, `check_maestro_ids_exist` included; `./gradlew
:feature:home:presentation:test :app:test verifyRoborazziDebug` in one invocation, Home's goldens
re-recorded and **opened**; `docs/ai/reference/FEATURES.md` § Flows gains "Keeping an inventory".
**Touches** `feature/home/**`, `app/**/AppNavHost.kt`, `.maestro/inventory.yaml` (new),
`docs/ai/reference/FEATURES.md`.
**Read** `feature/home/presentation/.../home/{HomeScreen,HomeState,HomeNavigation,HomeViewModel}.kt`
· `app/**/AppNavHost.kt` § `settingsEntries` · `.maestro/trips.yaml`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** E3S4, E3S5

### E3T1 Unused components earn their place (was F6, C0T1) · 12 · decides D65 · after E3S6

**Why** D38 has held every unused component since the design system landed, on the promise that a
real screen would get the chance to use it. Trips and Inventory were that chance, and this is the
day the promise comes due.
**Decide first** per component, with D38 as the standing rule → D65 records the scan and the
outcome for each of the sixteen that were homeless on 2026-09-13. Expect four to argue about, and
expect all four to stay: `AppScreenChrome`, installed by `AppTheme` and named by no feature on
purpose (D50); `AppDialog` and `AppToast`, which the chrome renders for every alert and snackbar;
and `AppImage`, Coil's only consumer, which Inventory now composes. Anything that is still homeless
after that is deleted, not deprecated — a template with a component nobody composes teaches
someone to compose it.
**Done when** the scan in `docs/ai/reference/DESIGN-SYSTEM.md` § Components is re-run and the
count written beside the method; every component with no home is deleted or has one sentence
saying why it stays; `GalleryCatalog` and the goldens agree with what is left; `python3
scripts/doctor.py` passes, `check_gallery_lists_every_component` and
`check_design_system_lists_every_component` included; `./gradlew test verifyRoborazziDebug` in one
invocation; D38's row gains its outcome and D65 is written.
**Touches** `core/ui/**`, `feature/gallery/presentation/**`, `docs/ai/reference/DESIGN-SYSTEM.md`,
`docs/DECISIONS.md`.
**Read** `docs/ai/reference/DESIGN-SYSTEM.md` § Components · `docs/DECISIONS.md` D38, D50, D52 ·
`feature/gallery/presentation/.../GalleryCatalog.kt`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + the whole
`./gradlew test`. **Depends** E3S1–E3S6

## Not in this release

Ranked below the cut and left in [../../BACKLOG.md](../../BACKLOG.md) § Next, in this order: the
dev menu's jump list (was D1X2) · a connectivity banner · the Maestro flows that still tap fixture
text · a component playground (was C1U6) · Field report (was B3S2). The next grooming does each
line or deletes it; nothing here is carried forward by default (D62).
