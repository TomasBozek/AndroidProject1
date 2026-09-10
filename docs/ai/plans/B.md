# Release B · the refactors v1.0 skipped, and the two features that use them

Status: open · 2026-09-10
Agents: 4 · lane 0 106 (~8.5 h, runs first and alone) · lane 1 87 (~7 h) · lane 2 90 (~7.2 h) · lane 3 100 (~8 h)
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D43–D45, D49–D53

Release A shipped what a v1.0 tag must not carry. Everything here was dropped from it for one
reason: **it is not broken**. Two thirds is the refactor half — the module topology, the shell, the
text field, the release artifact. The last third is the two showcase features, which are why the
refactors are worth doing: 26 of the 46 `App*` components in `:core:ui` are reached only by the
gallery, and D38 says none of them is deleted until a real screen has had the chance to use it.

**Lane 0 runs first and alone, and merges before lanes 1–3 start.** Its four tasks are repo-wide —
one of them renumbers the module graph the other three write against. The Shared files table below
therefore arbitrates between lanes 1, 2 and 3 only; lane 0 precedes them and may touch anything.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

| File group | Owner lane | Tasks |
|---|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**` — `AppNavHost.kt` and `KoinGraphTest.kt` included | 3 | B3S1, B3S2 |
| `gradle/libs.versions.toml`, `build-logic/**`, `.github/workflows/build.yml` | 2 | B2P1, B2H1, B2H2, B2T1 |
| `scripts/**` | 2 | B2P2, B2P3 |
| `core/ui/**`, `service/core/ui/component/**` | 1 | B1U1, B1U2, B1U3, B1U4 |
| `service/core/ui/viewmodel/**`, `service/network/**`, `feature/*/data/**` | 2 | B2H3, B2P2, B2X1 |
| `CLAUDE.md`, `docs/ai/CODEBASE.md`, `docs/README.md` | 0 | B0P1, B0P2 |

`create_feature.py` writes four of the first row's files, which is why the showcase lane owns them
outright: a lane that ran a generator would otherwise take `app/` hostage from whoever else needed
it. A lane that finds it needs a file it does not own comments `needs <file>` on the pull request
and takes the next task — `../PROCESS.md` § Task loop 7.

## Board

The board is [../../STATUS.md](../../STATUS.md) — one place to look, and no second copy to drift.
What is left here is why each task is there and what finishes it.

## Tasks

### B0P1 The module topology, decided once (was F5 + F11) · 50 · decides D49

**Why** Two structural questions have been answered by accretion rather than by anyone: there are
ten `di` modules whose whole content is one `object XModule` of 13–61 lines, and `service/` is a
second copy of the layer names — packages stay `...core.*` while namespaces are `...service.core.*`,
because two Android modules cannot share one namespace. Every other task in this release writes
module paths, so this settles first or it invalidates them.
**Decide first** keep both splits and pay the configuration cost, fold each feature's `di` into its
`presentation` and keep `service/`, or fold `service/` into `:core:*` and keep the `di` modules →
D49. Recommended: **fold `di` into `presentation`, keep `service/`.** A `di` module exists so that
`:core:di` can depend on registration without depending on implementation, but `:core:di` already
takes `api(projects.feature.x.di)` and that module `api`s the presentation layer — the isolation is
notional. `service/` earns its split for the opposite reason: it is copied out by
`export_service.py` and reused, and the namespace duplication is the price of that, not a defect.
**Done when** `python3 scripts/doctor.py` passes; `./gradlew :app:assembleDevDebug test` passes;
`grep -c 'includeFeatureModule\|includeCoreModule\|includeServiceModule' settings.gradle.kts` matches
the tree the decision chose; `python3 scripts/test_scripts.py` passes, so the generators emit the new
shape; `python3 scripts/create_feature.py zzztemp --dry-run` names no module the decision removed;
D49 is a row in `../../DECISIONS.md`.
**Touches** `settings.gradle.kts`, every module's `build.gradle.kts`, `core/di/**`, `scripts/*.py`,
`CLAUDE.md`, `docs/ai/CODEBASE.md`, `docs/ai/reference/CORE.md`, `docs/ai/reference/SERVICES.md`.
**Read** `settings.gradle.kts:50-167` · `core/di/src/main/kotlin/com/example/androidproject1/core/di/Koin.kt`
· `feature/catalog/di/build.gradle.kts` · `build-logic/src/main/kotlin/FeatureDiConventionPlugin.kt`
· `scripts/create_feature.py:280-330` · `CLAUDE.md` § Module structure.
**Steps** 1. Write D49 first, with the measurement that decided it — configuration time before and
after, from `./gradlew :app:assembleDevDebug --profile`. 2. Change the generators before the tree:
`create_feature.py`, `delete_feature.py` and `create_datasource.py` are what make the shape
reproducible, and `test_scripts.py` is what proves they did. 3. Move the code. 4. Fold the
`convention.feature.di` plugin into whichever plugin survives, or delete it. 5. Update
`CLAUDE.md` § Module structure and § Convention plugins, `docs/ai/reference/CORE.md` and
`docs/ai/reference/SERVICES.md` in this commit — B0P2 moves those sections next, so leaving them
stale would move a lie.
**Checks** T1 + `python3 scripts/test_scripts.py` + the whole `./gradlew test`. **Depends** —

### B0P2 The module tree moves, CLAUDE.md goes on a diet · 25

**Why** `docs/ai/CODEBASE.md` is a twelve-line placeholder that says where the tree really is, which
is the one thing a docs tree is not allowed to say. `CLAUDE.md` is 388 lines against its own 300-line
budget, and `doctor.py` has 31 checks where three of the promised ones are missing: `check_task_ids`
and `check_doc_budgets` do not exist, and `check_docs_index` holds A0P2's three D48 greps and should
grow the tree check rather than gain a fourth sibling.
**Done when** `docs/ai/CODEBASE.md` holds the fenced module tree and the convention-plugin table and
`CLAUDE.md` holds neither; `[ $(wc -l < CLAUDE.md) -le 300 ]`; `grep -c 'def check_' scripts/doctor.py`
is 33; `python3 scripts/doctor.py` passes and fails on a planted 301st line, on a board line with a
malformed id and on a feature missing from the tree; `grep -rn CLAUDE_MD_FILE scripts/` returns
nothing; `python3 scripts/test_scripts.py` passes.
**Touches** `CLAUDE.md`, `docs/ai/CODEBASE.md`, `docs/README.md`, `scripts/_common.py`,
`scripts/doctor.py`, `scripts/test_scripts.py`.
**Read** `scripts/_common.py:25,426-475` · `scripts/doctor.py:393-431,1257-1300` ·
`scripts/test_scripts.py:185,249,413` · `CLAUDE.md:34-120,238-259` · `docs/README.md` § Budgets.
**Steps** 1. Move `CLAUDE.md:34-59` and `:60-120` into `docs/ai/CODEBASE.md` byte for byte and leave
one link behind. That is 87 lines and leaves 301, so one more section goes: § API you build on
(`:238-259`, 22 lines) is the candidate — it is a lookup table, which is what `ai/` is for.
2. Repoint `CLAUDE_MD_FILE` to `MODULE_TREE_FILE` in `_common.py` and at every call site.
3. Extend `check_docs_index` with the tree check rather than adding a fourth function.
4. Add `check_task_ids` (`^[A-Z][0-9][UXTHPS][1-9]$` over every board line in `docs/STATUS.md` and
`docs/ai/plans/`) and `check_doc_budgets` (`CLAUDE.md` ≤ 300, `docs/ai/PROCESS.md` ≤ 120).
5. `docs/README.md` § ai/ describes `CODEBASE.md` as holding the tree, not as pointing at it.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** B0P1

### B0X1 BaseViewModel's defaults, and its nullable state · 25 · decides D44

**Why** 22 of the 25 `execute`/`observe` call sites in the app pass `loading = {}` to switch the
overlay off, so the default is wrong at seven sites out of eight. Separately, `initialState` is
`State?` and four view models pass `null`; a `null` state silently drops any `update` that arrives
before the first load, which is the bug A1X4 fixed once by hand on the product-detail heart.
**Decide first** make the overlay opt-in (`loading = {}` becomes the default and the seven that want
it ask) and `initialState` non-null, or opt-in only and leave the state nullable → D44. Recommended:
**both.** A non-null `initialState` costs each of the four a `PREVIEW`-shaped empty state, which
`XState.PREVIEW` already is, and it removes the whole class of dropped update rather than the one
instance of it.
**Done when** `grep -rn 'loading = {}' --include='*.kt' feature core app | wc -l` is 0;
`grep -rn 'initialState = null' --include='*.kt' feature app | wc -l` is 0; `BaseViewModel`'s
`initialState` parameter is `State`, not `State?`; `./gradlew test verifyRoborazziDebug` passes in one
invocation; `feature/template` still compiles, so the generators clone the new shape; D44 is a row
in `../../DECISIONS.md`.
**Touches** `service/core/ui/src/main/kotlin/.../viewmodel/BaseViewModel.kt`,
`service/core/ui/src/main/kotlin/.../state/UiState.kt`, every `feature/*/presentation`,
`app/**/MainViewModel.kt`, `scripts/create_screen.py`'s ViewModel template, `CLAUDE.md` § MVI
conventions, `docs/ai/reference/CORE.md`.
**Read** `service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/viewmodel/BaseViewModel.kt:200-300`
· `feature/catalog/presentation/.../products/ProductsViewModel.kt:21` ·
`feature/catalog/presentation/.../productdetail/ProductDetailViewModel.kt:27` ·
`CLAUDE.md` § MVI conventions.
**Steps** 1. Write D44. 2. Flip the two defaults in `execute` and `observe`. 3. Give each of the four
nullable view models a real initial state — `XState.PREVIEW` is usually it, and the `UiState`
envelope keeps the loading overlay. 4. Delete the `State?` from `BaseViewModel` and from
`UiState.data` if the decision reaches that far. 5. Update `create_screen.py`'s template and
`CLAUDE.md` § MVI conventions in this commit.
**Checks** T1 + the whole `./gradlew test` with `verifyRoborazziDebug` in the same invocation.
**Depends** —

### B0U1 The tabs have test ids (was qa.16) · 6

**Why** The four tabs carry no `testTag`, so `.maestro/add-to-cart.yaml:7`, `log-out.yaml:7`,
`grant-a-permission.yaml:7` and `browse-to-a-product.yaml:10,24` tap the English labels `"Catalog"`,
`"Cart"`, `"Settings"` and `"Home"`. `CLAUDE.md` § Test identifiers says find by id, never by text,
and these flows are the only place in the repository that breaks it — a wording fix or the Czech
locale turns them red.
**Done when** `grep -c 'tabs_' app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt` is 4;
no `tapOn: "Home"`, `"Catalog"`, `"Cart"` or `"Settings"` remains under `.maestro/`;
`python3 scripts/doctor.py` passes and fails when a `TopLevelDestination` entry is added without an
id; `grep -c 'def check_' scripts/doctor.py` is 34.
**Touches** `app/**/AppNavHost.kt`, `app/**/TopLevelDestination.kt`, `.maestro/*.yaml`,
`scripts/doctor.py`, `scripts/test_scripts.py`.
**Read** `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:114-129` ·
`app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt:25-47` ·
`.maestro/add-to-cart.yaml` · `CLAUDE.md` § Test identifiers.
**Steps** 1. Give `TopLevelDestination` a `testTag` property — `tabs_homeTab`, `tabs_catalogTab`,
`tabs_cartTab`, `tabs_settingsTab` — rather than deriving it at the call site, so a new tab cannot
be added without one. 2. Apply it in the `item(...)` modifier at `AppNavHost.kt:115`. 3. Rewrite the
five `tapOn` lines to `tapOn: id: "tabs_<name>Tab"`. 4. Add `check_tab_test_ids` to `doctor.py`:
every `TopLevelDestination` entry has a tag, and every tag is `tabs_<name>Tab`. 5. The category and
product names the flows also tap (`"Beverages"`, `"Coffee"`) are fixture data, not labels — leave
them and put a line in `../../BACKLOG.md` if they should be ids too.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** —

### B1U1 The screen shell joins the design system (was F13) · 25 · decides D50

**Why** `Screen()` renders the loading overlay, the two content states and the alert dialog, and all
three of those components are raw Material with hard-coded numbers:
`ContentMessage.kt:36-58` has `32.dp`, `12.dp`, `8.dp` and `MaterialTheme.typography`,
`LoadingOverlay.kt:26-29` has `16.dp` and `MaterialTheme.colorScheme.scrim`, `AlertDialog.kt` is
Material's `AlertDialog` and `TextButton`. Every one of those is a rule `CLAUDE.md` § Design system
puts on features and exempts itself from. It is not laziness: `:service:core:ui` must not name
`:core:ui`, so it cannot read `AppTheme` at all.
**Decide first** slots on `Screen()` threaded from `AppNavHost`, or a `LocalScreenChrome` that
`AppTheme` installs and `Screen()` reads → D50. Recommended: **the composition local.** Every screen
already composes inside `AppTheme`, `:core:ui` already depends on `:service:core:ui`, and the
alternative would put `app/**/AppNavHost.kt` — lane 3's file — in this lane.
**Done when** `grep -n 'material3\|\.dp\|MaterialTheme' service/core/ui/src/main/kotlin/.../component/{ContentMessage,LoadingOverlay,AlertDialog}.kt`
returns nothing but the `Surface` in `Screen.kt`; `:service:core:ui` still names no `:core:*` module,
so `python3 scripts/doctor.py`'s `check_service_isolation` passes; `./gradlew test verifyRoborazziDebug`
passes in one invocation; `OverlayScreenshotTest` in `:core:ui` gains a case per chrome element;
D50 is a row in `../../DECISIONS.md`.
**Touches** `service/core/ui/src/main/kotlin/.../component/{Screen,ContentMessage,LoadingOverlay,AlertDialog}.kt`,
`core/ui/src/main/kotlin/.../theme/`, `core/ui/src/main/kotlin/.../component/`,
`core/ui/src/test/.../OverlayScreenshotTest.kt`, `docs/ai/reference/DESIGN-SYSTEM.md`,
`docs/ai/reference/SERVICES.md`.
**Read** `service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/Screen.kt:53-81,134-138`
· the three chrome files beside it · `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/`
· `CLAUDE.md` § Design system.
**Steps** 1. Write D50. 2. Define the chrome contract in `:service:core:ui` — a data holder of the
roles the four elements need, with a defaults object that keeps `service/` standing alone when it is
copied out. 3. Install the app's values from `AppTheme` in `:core:ui`. 4. Replace the literals.
5. Add each element to `GalleryCatalog.kt` and to `OverlayScreenshotTest`, because a dialog and an
overlay draw in a window of their own and no preview sees them. 6. Update
`docs/ai/reference/DESIGN-SYSTEM.md` and `docs/ai/reference/SERVICES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + the whole `./gradlew test`,
because `service/` moved. **Depends** —

### B1U2 AppTextField rebuilt, the size enums folded (was F23 + M1) · 25 · decides D51

**Why** `AppTextField` is a `value: String` / `onValueChange` field with one `KeyboardOptions` at
`:122`, no IME action a form can set, and no autofill content type — so Login and Sign-up cannot
offer a saved password and the keyboard's action key says "done" on every field of a three-field
form. Separately there are two size enums for one idea: `ControlSize` (`Small/Medium/Large`, read by
`AppCheckbox`, `AppSwitch`, `AppDateField` and `AppTextField`) and `ButtonSize` (which carries its
own height, shape and padding, read by `AppButton`, `AppBottomActionBar`, `AppSectionHeader`).
*(M2, "three components with no `modifier`", is already false — every `App*.kt` takes one. It is not
in this task.)*
**Decide first** rebuild on `TextFieldState` and the Compose `BasicTextField` that takes it, or keep
`value`/`onValueChange` and add the missing parameters → D51. Recommended: **`TextFieldState`.** It
is where the platform is going, it removes the recomposition-per-keystroke shape, and this is the
release to pay for it — but read what it does to `XState` first: a `TextFieldState` is not a value a
data class can hold, so the field's text stops being part of the immutable state and
`check_state_immutability` has to be reasoned about rather than worked around.
**Done when** `AppTextField` takes an IME action and an autofill content type; Login, Sign-up and
Profile pass both; `grep -rn 'enum class .*Size' core/ui/src/main/kotlin` names one enum, not two;
`python3 scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug` passes in one invocation;
D51 is a row in `../../DECISIONS.md`.
**Touches** `core/ui/src/main/kotlin/.../component/{AppTextField,ControlSize,AppButton,AppCheckbox,AppSwitch,AppDateField,AppBottomActionBar,AppSectionHeader}.kt`,
`feature/auth/presentation/**`, `feature/profile/presentation/**`,
`feature/gallery/presentation/.../GalleryCatalog.kt`, `docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt`
· `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/ControlSize.kt:23` ·
`core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppButton.kt:49` ·
`feature/auth/presentation/.../login/LoginScreen.kt`.
**Steps** 1. Write D51. 2. Fold the enums first — it is mechanical and it makes the field rebuild a
smaller diff. `ButtonSize`'s three properties become a lookup on the folded enum, so the enum stays
data-free. 3. Rebuild the field. 4. Update every call site and `GalleryCatalog.kt`. 5. Re-record the
goldens with `recordRoborazziDebug` and **open what it wrote** before committing.
6. `docs/ai/reference/DESIGN-SYSTEM.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** —

### B1U3 The gallery is generated from the previews (was F7) · 25 · decides D52

**Why** `GalleryCatalog.kt` is 896 hand-written lines naming 44 entries, and nothing checks it
against the 46 `App*.kt` files beside it — `create_component.py` does not touch it, so the only thing
keeping it honest is `CLAUDE.md` § Design system asking politely. Two components are already missing
from it, and the number only goes one way.
**Decide first** generate the list at runtime from the `@ComponentPreview` functions with
`ComposablePreviewScanner`, or keep the hand-written list and add a `doctor.py` check that it names
every `App*.kt` → D52. Recommended: **the scanner**, but check the cost first: it is in the catalog
under D27's test-only leniency (D36), and the gallery ships in `dev` and `staging` (D16), so this
moves it from test-only to a debug-build dependency and needs its row in `docs/ai/DEPENDENCIES.md`.
If it will not run outside a test classpath, the check is the answer — and then say so in D52, since
the second option needs `scripts/doctor.py`, which lane 2 owns: comment `needs scripts/doctor.py`
and take the next task.
**Done when** every `@ComponentPreview` in `:core:ui` appears in the gallery with no line naming it
by hand, or a `doctor.py` check fails when one does not; `GalleryCatalog.kt` is under 200 lines or
gone; `./gradlew :feature:gallery:presentation:assembleDebug test` passes;
`./gradlew :app:assembleDevDebug` and `:app:assembleProdRelease` both pass, so nothing the scanner
needs reached the release build.
**Touches** `feature/gallery/presentation/**`, `gradle/libs.versions.toml` *(if D52 takes the
scanner — this file is lane 2's, so agree it with lane 2 or take the next task)*,
`docs/ai/DEPENDENCIES.md`, `docs/ai/reference/DESIGN-SYSTEM.md`, `CLAUDE.md` § Design system.
**Read** `feature/gallery/presentation/src/main/kotlin/.../GalleryCatalog.kt:1-60` ·
`service/core/ui/src/testFixtures/kotlin/.../screenshot/PreviewScreenshotSpec.kt:34-40` ·
`gradle/libs.versions.toml:117` · `docs/ai/DEPENDENCIES.md`.
**Steps** 1. Write D52, with the answer to whether the scanner runs off a debug classpath — try it
before deciding, it is twenty minutes. 2. Build the list. 3. Delete what the list replaces.
4. `CLAUDE.md` § Design system stops telling people to edit `GalleryCatalog.kt` by hand, or starts
telling them the check exists. 5. `docs/ai/DEPENDENCIES.md` gains or does not gain a row.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** B1U1, B1U2 —
both add and rename entries, and generating a list from a moving set twice is the same work twice.

### B1U4 Predictive back on dirty forms, auto-sizing numerics (was M3 + M4) · 12

**Why** `grep -rn BackHandler` over the repository returns nothing, so a half-filled Sign-up is
discarded by the back gesture with no warning. And `grep -rn TextAutoSize` returns nothing, so a
`TextRole.Numeric` price wraps in Czech, where "1 234,00 Kč" is four characters longer than "1,234.00".
**Done when** `PredictiveBackHandler` guards Sign-up, Login and Profile while their state differs
from the state they were entered with, and a `*ScreenTest` asserts the confirmation for each;
`AppText` with `TextRole.Numeric` shrinks rather than wraps, with a `@ComponentPreview` at a long
Czech value; `./gradlew test verifyRoborazziDebug` passes in one invocation.
**Touches** `core/ui/src/main/kotlin/.../component/AppText.kt`, `feature/auth/presentation/**`,
`feature/profile/presentation/**`, `docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppText.kt:44,79,107`
· `feature/auth/presentation/.../signup/SignUpScreen.kt` ·
`feature/profile/presentation/.../profile/ProfileScreen.kt` · `CLAUDE.md` § MVI conventions, for
where the confirmation belongs — it is an `AlertPayload`, not a dialog the screen draws.
**Steps** 1. The dirty check is a comparison against the initial state, not a flag each field sets.
2. The confirmation is `AlertState` through `Screen()` and comes back as
`SystemEvent.AlertResult`, so the screen draws nothing new. 3. `TextAutoSize` goes on the `Numeric`
branch of `AppText` only — a body paragraph that shrinks is a bug, not a feature.
4. `docs/ai/reference/DESIGN-SYSTEM.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** —

### B2P1 Two flavors (was F18) · 12 · decides D43

**Why** `staging` is `prod` plus an application-id suffix, a launcher label and
`https://staging.example.com/`, a host that does not resolve. It costs two source-set files
(`app/src/staging/kotlin/.../DebugMenu.kt` and `.../NetworkEngine.kt`) and a third variant of every
task name. What changed since A's draft: it no longer costs the pull-request gate anything, because
the gate compiles the flavor rather than assembling it. This is taste now, not cost.
**Decide first** cut `staging` and let `DebugMenu.ENABLED` follow `BuildConfig.DEBUG`, or keep three
→ D43. Recommended: **keep three.** A template's job is to show the shape, three environments is the
shape most apps have, and the two files are the demonstration rather than the cost. If it is cut,
D16 has to be rewritten, because it names dev and staging as where the debug menu lives.
**Done when** `grep -c 'STAGING' build-logic/src/main/kotlin/ProjectConfig.kt` matches the decision;
`./gradlew :app:assembleDevDebug` and `:app:assembleProdRelease` pass; `.github/workflows/build.yml`
compiles exactly the flavors that exist; D43 is a row in `../../DECISIONS.md` and D16 agrees with it.
**Touches** `build-logic/src/main/kotlin/ProjectConfig.kt`, `app/src/staging/**`,
`.github/workflows/build.yml`, `../../DECISIONS.md`, `CLAUDE.md` § Commands,
`docs/ai/ARCHITECTURE.md` § environments, `docs/RELEASING.md`.
**Read** `build-logic/src/main/kotlin/ProjectConfig.kt:41-59` ·
`build-logic/src/main/kotlin/AndroidConventions.kt:90` · `.github/workflows/build.yml:148` ·
`app/src/staging/kotlin/com/example/androidproject1/debug/DebugMenu.kt`.
**Steps** 1. Write D43. 2. Whatever it says, the flavor list is one edit in `ProjectConfig.Flavor` —
that is the point of it living there. 3. Update `CLAUDE.md` § Commands and `docs/ai/ARCHITECTURE.md`
in this commit, since both name three flavors by hand.
**Checks** T1. **Depends** —

### B2H1 The release ships an AAB with a tag-derived versionCode (was rest of F17c) · 12

**Why** A2H1 made the release job refuse an unsigned build; this is the other half of the same
step. `.github/workflows/build.yml:247` runs `assembleProdRelease`, so the release carries an APK,
and `ProjectConfig.kt:82` derives `versionCode` from `git rev-list --count HEAD` — which goes
*down* on a hotfix branched from an older tag, and a store refuses a code it has already seen. D32
says a GitHub release carrying the artifact is the release, so neither was needed for v1.0; reopen
D32 before starting if that has changed.
**Done when** the release job runs `bundleProdRelease` and uploads the `.aab`;
`releaseVersionCode()` is derived from the tag (`v1.2.3` → `10203`, so a hotfix on `v1.2` sorts
above `v1.2.0` and below `v1.3.0`) with a JVM test in `build-logic` covering a tag, a non-tag and a
malformed tag; `./gradlew :app:bundleProdRelease` produces a bundle;
`docs/RELEASING.md` says what the release page carries.
**Touches** `.github/workflows/build.yml`, `build-logic/src/main/kotlin/ProjectConfig.kt`,
`build-logic/src/test/**`, `docs/RELEASING.md`.
**Read** `.github/workflows/build.yml:240-290` · `build-logic/src/main/kotlin/ProjectConfig.kt:62-95`
· `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:28-29` · `docs/RELEASING.md`.
**Steps** 1. Parse the tag rather than counting commits, and keep the `ProjectConfig.VERSION_CODE`
fallback for every build that is not on one. 2. Test it in `build-logic` — this is the one place in
the repository where a wrong number is invisible until a store rejects it. 3. Swap the assemble task
and the uploaded path together; the mapping step A2H3 added reads `mapping/prodRelease/`, which the
bundle task also writes. 4. `docs/RELEASING.md`.
**Checks** T1 + `./gradlew :app:bundleProdRelease`. **Depends** —

### B2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45

**Why** The generated profile is at `app/src/devRelease/generated/baselineProfiles/baseline-prof.txt`
— the `devRelease` source set. `prodRelease` never sees it, so the whole benefit is lost, silently,
and has been since it was recorded. The module also drags in `convention.android.test` and
`androidx.baselineprofile`, two plugins with one consumer between them.
**Decide first** fix the wiring so the profile is recorded against and consumed by the shipping
variant, or cut `:baselineprofile` and both plugins → D45. Recommended: **fix it.** A template that
ships a startup profile is showing something worth copying; one that ships a dead directory is
teaching the opposite. But measure first — `macrobenchmark` on `prodRelease` with and without —
because a profile that buys nothing is a dependency that has not earned its place.
**Done when** `unzip -l app/build/outputs/bundle/prodRelease/*.aab | grep baseline` finds the
profile, or `:baselineprofile` and both plugin aliases are gone from `settings.gradle.kts` and
`gradle/libs.versions.toml`; `./gradlew :app:assembleProdRelease` passes; D45 is a row in
`../../DECISIONS.md` carrying the measurement.
**Touches** `baselineprofile/**`, `app/src/devRelease/**`, `app/build.gradle.kts`,
`settings.gradle.kts:167` *(lane 3's file — agree the one-line deletion with lane 3, or take the
next task)*, `gradle/libs.versions.toml`, `build-logic/src/main/kotlin/AndroidTestConventionPlugin.kt`,
`docs/ai/DEPENDENCIES.md`.
**Read** `baselineprofile/build.gradle.kts` · `app/build.gradle.kts:38-40` ·
`app/src/devRelease/generated/baselineProfiles/baseline-prof.txt` ·
`build-logic/src/main/kotlin/AndroidTestConventionPlugin.kt`.
**Steps** 1. Measure, then write D45. 2. If it is fixed: `missingDimensionStrategy` names the flavor
the profile is *recorded* against, and the variant it is *consumed* by is the release variant of that
same flavor — those are two settings and only the first is set today. 3. If it is cut, the plugin
`convention.android.test` loses its only consumer and goes with it. 4. `docs/ai/DEPENDENCIES.md`.
**Checks** T1 + `./gradlew :app:assembleProdRelease`. **Depends** B2H1, which decides whether the
artifact is a bundle or an APK, and the profile is packaged differently in each.

### B2T1 Coil moves to the module that imports it (was half of F24) · 6

**Why** `AndroidConventions.kt:158-159` adds Coil to every module applying the Compose convention —
all thirteen — so that `doctor.py` can forbid the import in twelve of them. One module imports it:
`core/ui/.../component/AppImage.kt`. *(The Ktor half of F24 is already done: `ktor-client` lives in
`ServiceNetworkConventionPlugin`, whose only consumer is `service/network`.)*
**Done when** `grep -c coil build-logic/src/main/kotlin/AndroidConventions.kt` is 0; `:core:ui` gets
Coil from a convention plugin only it applies — **not** from its own build file, which
`CLAUDE.md` forbids and `check_no_duplicated_android_config` enforces; the `IMAGE_LIBRARY` arm of
`check_features_use_the_design_system` is deleted along with its fixture in `test_scripts.py`;
`./gradlew :app:assembleDevDebug test` passes; `python3 scripts/doctor.py` passes.
**Touches** `build-logic/src/main/kotlin/AndroidConventions.kt`, a new
`build-logic/src/main/kotlin/CoreUiConventionPlugin.kt`, `build-logic/build.gradle.kts`,
`core/ui/build.gradle.kts`, `scripts/doctor.py:662`, `scripts/test_scripts.py`,
`docs/ai/DEPENDENCIES.md`, `CLAUDE.md` § Convention plugins *(lane 0's file until B0P2 moves the
table to `docs/ai/CODEBASE.md`, which it will have done)*.
**Read** `build-logic/src/main/kotlin/AndroidConventions.kt:146-170` · `scripts/doctor.py:662,680-736`
· `core/ui/build.gradle.kts` · `CLAUDE.md` § Convention plugins.
**Steps** 1. New `convention.core.ui` plugin: the compose library plugin plus the two Coil
artifacts. 2. `core/ui/build.gradle.kts` swaps its plugin alias; its project dependencies do not
move. 3. Delete the `coil3` arm of the design-system check and the fixture that proved it.
4. Update the convention-plugin table in `docs/ai/CODEBASE.md`.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** —

### B2P2 Where the data-source interface lives (was F15) · 12 · decides D53

**Why** There are ten `XDataSource` interfaces and exactly one implementation of each, and not one
fake of any of them — the twelve `Fake*` classes in the repository are all repositories, services or
loggers. So the interface buys nothing today: it is not a seam anyone tests through, and
`check_repository_depends_on_interface` exists to enforce a rule whose payoff is theoretical.
**Decide first** keep the per-source interface and give at least the two data sources with real
branching a `testFixtures` fake, or delete the interfaces and let a repository name
`DefaultXDataSource` directly with the seam at the repository, where the fakes already are → D53.
Recommended: **keep them, and write the fakes.** Deleting them makes `create_datasource.py` simpler
and the data layer thinner, but it also removes the only place a network error can be injected
without `MockEngine`, and the fakes are the cheaper half of the same argument.
**Done when** the decision holds in code for all ten sources, not the two that were convenient;
`python3 scripts/doctor.py` passes and `check_repository_depends_on_interface` either still passes or
is gone with its fixture; `python3 scripts/test_scripts.py` passes, so `create_datasource.py` emits
the shape D53 chose; `./gradlew test` passes; D53 is a row in `../../DECISIONS.md`.
**Touches** every `feature/*/data/**`, `scripts/create_datasource.py`, `scripts/doctor.py`,
`scripts/test_scripts.py`, `feature/template/data/**`, `CLAUDE.md` § Module structure *(after B0P2
this is `docs/ai/CODEBASE.md`)*, `docs/ai/reference/DOMAIN.md`.
**Read** `feature/catalog/data/src/main/kotlin/.../source/LocalCatalogDataSource.kt` ·
`feature/catalog/data/src/main/kotlin/.../repository/DefaultCatalogRepository.kt` ·
`scripts/doctor.py:531-557` · `scripts/create_datasource.py` · `CLAUDE.md` § Module structure.
**Steps** 1. Write D53. 2. Change `feature/template` and `create_datasource.py` first — the template
is what the generators clone, so if it is right the other nine are mechanical. 3. Apply to all ten.
4. `docs/ai/reference/DOMAIN.md`.
**Checks** T1 + `python3 scripts/test_scripts.py` + the whole `./gradlew test`. **Depends** —

### B2X1 A deep link to an uncached product opens it (was shell.7) · 12

**Why** `DefaultCatalogRepository.getProduct` reads the local table only — the comment at
`:48-49` says detail is always reached from a list, so the row is already there. A deep link is the
case that comment does not cover: `<applicationId>://product/<id>` on a cold start reaches a product
nobody browsed to, finds nothing, and the screen says the product is no longer available. A1X5 made
the link apply exactly once; this makes what it applies to resolve.
**Done when** `RemoteCatalogDataSource` has a `getProduct`, `DefaultCatalogRepository.getProduct`
falls back to it and caches the result, and a `DefaultCatalogRepositoryTest` case covers empty
table → remote hit → row written; the `dev` fixture engine answers the single-product route;
`.maestro/` gains a flow that cold-starts on a deep link to a product no earlier step visited;
`./gradlew test` passes.
**Touches** `feature/catalog/data/**`, `feature/catalog/domain/**`,
`app/src/dev/res/raw/fixture_products.json` and `app/src/dev/kotlin/.../network/NetworkEngine.kt`
*(lane 3's `app/**` — agree with lane 3 or take the next task)*, `.maestro/`,
`docs/ai/reference/DOMAIN.md`.
**Read** `feature/catalog/data/src/main/kotlin/.../repository/DefaultCatalogRepository.kt:45-58` ·
`feature/catalog/data/src/main/kotlin/.../source/RemoteCatalogDataSource.kt:16` ·
`app/src/dev/kotlin/com/example/androidproject1/network/NetworkEngine.kt` ·
`app/src/main/kotlin/com/example/androidproject1/MainActivity.kt:110-125`.
**Steps** 1. Add the remote call and the fixture route together. 2. The fallback belongs in the
repository, not the data source — the local source's job is the table, and `BaseRepository.cached()`
already has the shape. 3. Keep the comment at `:48-49` honest: it explains why the *list* path does
not refetch, and that is still true. 4. `docs/ai/reference/DOMAIN.md`.
**Checks** T1. **Depends** —

### B2H3 The HTTP cache, and state that survives process death (was half of F16) · 12

**Why** Two gaps with the same cause — nothing has needed them yet. The OkHttp client is built with
no `Cache`, so every `prod` request is a full round trip even for a body that has not changed; and
a view model's transient state (a half-typed search, a scroll position, a partly filled form) is
gone after process death, because `BaseViewModel` has no way to persist any of it. *(The connectivity
monitor, the third part of F16, needs a banner in `app/**` — lane 3's — and is in
`../../BACKLOG.md`.)*
**Done when** the OkHttp engine is configured with a `Cache` sized in `NetworkConfig` and a test
asserts a second identical GET is served from it; `BaseViewModel` exposes a `saved(key, default)`
whose value survives `SavedStateHandle` restoration, with a JVM test that restores one; the search
screen uses it for its query; `./gradlew test` passes; `docs/ai/reference/CORE.md` and
`docs/ai/reference/SERVICES.md` say so.
**Touches** `service/network/**`, `service/core/ui/src/main/kotlin/.../viewmodel/BaseViewModel.kt`,
`feature/catalog/presentation/**`, `app/**/ApplicationModule.kt` *(lane 3's — agree or take the next
task)*, `docs/ai/reference/{CORE,SERVICES}.md`.
**Read** `service/network/src/main/kotlin/.../HttpClientFactory.kt` ·
`service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/viewmodel/BaseViewModel.kt:60-115`
· `app/src/main/kotlin/com/example/androidproject1/ApplicationModule.kt:22-30` · `CLAUDE.md` § MVI
conventions, on why there is no `SavedStateHandle` detour for route arguments — this is not that.
**Steps** 1. The cache is a directory the platform gives the app and a size; both belong in
`NetworkConfig`, which `:app` already fills from `BuildConfig`. 2. `saved()` is a delegate over the
`SavedStateHandle` the view model can already be given, not a new dependency. 3. One real consumer
each, or neither lands — an unused seam is what this task is fixing.
**Checks** T1 + the whole `./gradlew test`, because `service/` moved. **Depends** B0X1, which
changes the `BaseViewModel` constructor this adds to.

### B2P3 Hooks become a committed .githooks (was F10) · 12

**Why** `scripts/install_hooks.py` is 153 lines that reimplement `git config core.hooksPath`, and
every clone has to run it because the thing it installs is not version controlled — which is the
problem `core.hooksPath` was added to solve. Beside it, `export_service.py --sync-versions` is a
170-line TOML resolver whose consumer is a script nobody has run against a second repository.
**Done when** `.githooks/pre-commit` is committed and executable; `README.md` names the single
`git config core.hooksPath .githooks` line; `scripts/install_hooks.py` is gone or under 30 lines;
`sync_versions` and its flag are gone from `export_service.py`; `python3 scripts/test_scripts.py`
passes with the fixtures for both removed; the hook itself runs `doctor.py` and runs
`test_scripts.py` only when the commit touches `scripts/`.
**Touches** `.githooks/pre-commit`, `scripts/install_hooks.py`, `scripts/export_service.py`,
`scripts/test_scripts.py`, `README.md`, `docs/ai/RECIPES.md`, `CLAUDE.md` § Recipes if it names the
script.
**Read** `scripts/install_hooks.py` · `scripts/export_service.py:100-130,374-470` ·
`scripts/test_scripts.py` for the two fixtures · `README.md`.
**Steps** 1. Write the hook as a shell script — it is `python3 scripts/doctor.py` plus a
`git diff --cached --name-only` grep. 2. Delete the installer, or leave a `--uninstall` that unsets
the config. 3. Delete `sync_versions`; `export_service.py` already prints what to merge.
4. `README.md` and `docs/ai/RECIPES.md`.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** —

### B3S1 Trips, a showcase feature (was S1) · 50

**Why** The template demonstrates a structure and five sample features, none of which needs a
multi-step flow, a result handed back up the stack, or a dashboard. So 26 of the 46 `App*`
components — the accordion, the stepper, the tabs, the sheet, the tooltip, the skeleton, the
progress, the slider, the status dot — are reached only by the gallery, and D38 keeps every one of
them until a real screen has had the chance to use it. This is that chance.
**Done when** `python3 scripts/create_feature.py trips` has produced the four layers and five
screens exist: a list, a three-step wizard, a detail, a picker that returns its result to the
wizard, and a dashboard; each has its `XViewModelTest` and `XScreenTest`; the feature composes
at least twelve components that only the gallery reached before, adding none to `:core:ui`;
`python3 scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug` passes in one
invocation; `docs/ai/reference/FEATURES.md` and `docs/ai/reference/DOMAIN.md` describe it.
**Touches** `feature/trips/**` (new), `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`,
`app/**/KoinGraphTest.kt`, `docs/ai/CODEBASE.md`, `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Read** `feature/catalog/presentation/**` as the closest existing shape · `CLAUDE.md` § Screen
structure and § MVI conventions · `docs/ai/RECIPES.md` · `feature/gallery/presentation/.../GalleryCatalog.kt`
for what is going unused.
**Steps** 1. `python3 scripts/create_feature.py trips --dry-run` first, then for real — it makes the
five registrations by hand copy silently skips. 2. `create_screen.py` per screen; the picker takes
`--with-args`, which also writes the `KoinGraphTest` line `doctor.py` checks for. 3. The result
comes back through the back stack, not through a shared view model — `CLAUDE.md` § MVI conventions,
and `AppNavHost` wires the lambda. 4. Data is a local Room table with a fixture seed, like catalog:
no new dependency, and D20 still stands. 5. Czech strings for every key, in the same commit —
`doctor.py` fails a module missing one. 6. `recordRoborazziDebug`, then **open what it wrote**.
7. `docs/ai/reference/FEATURES.md` and `DOMAIN.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` +
`python3 scripts/test_scripts.py`. **Depends** —

### B3S2 Field report, a showcase feature (was S2) · 50

**Why** The platform seams a real app needs are half-shown: `feature/profile` already drives the
Photo Picker and a camera capture (`component/PictureButtons.kt`, `component/CaptureButton.kt`), and
`feature/settings` already has a permission screen — but nothing in the repository asks for a
runtime location, and nothing writes a file the user chose through the Storage Access Framework. A
feature that does both in one flow is what turns the permission screen from a settings list into
something with a reason to exist.
**Done when** `python3 scripts/create_feature.py fieldreport` has produced the four layers; a report
captures a photo, a coarse location and a note, and exports itself through
`ActivityResultContracts.CreateDocument`; the location permission is requested in context with a
rationale, and the denied-permanently path reaches app settings through `UiCommand`; no new
dependency appears in `gradle/libs.versions.toml`; each screen has both its tests;
`python3 scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug` passes in one invocation.
**Touches** `feature/fieldreport/**` (new), `settings.gradle.kts`, `core/di/**`,
`app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`, `app/src/main/AndroidManifest.xml`,
`docs/ai/CODEBASE.md`, `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Read** `feature/profile/presentation/src/main/kotlin/.../component/{PictureButtons,CaptureButton}.kt`
· `feature/settings/presentation/src/main/kotlin/.../permissions/SettingsPermissionsScreen.kt` ·
`app/src/main/AndroidManifest.xml` · `service/core/ui/src/main/kotlin/.../event/UiCommand.kt`.
**Steps** 1. Generate the feature. 2. Location comes from the platform `LocationManager`, not from
Play Services — a first-party alternative that works is what `CLAUDE.md` asks for before a
dependency, and coarse location is enough for a field report. 3. The permission decision belongs to
the screen, not the view model: `CLAUDE.md` and `../../BACKLOG.md` § Someday both note there is no
ViewModel-readable permission state, and this feature is not the task that adds one — if it turns out
to need one, that is a backlog line, not scope. 4. SAF export is a contract launcher and a
`ContentResolver` write off the main thread. 5. Czech strings for every key.
6. `recordRoborazziDebug`, then **open what it wrote**. 7. `docs/ai/reference/FEATURES.md` and
`DOMAIN.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` +
`python3 scripts/test_scripts.py`. **Depends** B3S1, which owns the same four shared files and runs
the same generator.

## Not in this release

Moved to [../../BACKLOG.md](../../BACKLOG.md) § Next rather than padding a lane, and why:

- **Unused components earn their place (was F6) · 25.** It cannot be estimated until B3S1 and B3S2
  have shipped and the count of components nothing composes is known. D38 is the rule; this release
  is what makes it answerable.
- **The design system stops speaking POS (was F22) · 6** — 64 mentions of till, void and cash across
  `core/ui` and the gallery, and `AppDensity` reading `InputDevice`. Cosmetic while the showcase
  features are still being written against those names.
- **A connectivity banner (was a third of F16) · 12** — needs a banner in `app/**`, which lane 3
  owns for the whole release.
- **network_security_config and StrictMode (was H7 + H8) · 6.** The audit corrected the premise:
  cleartext is already denied by the platform at this `targetSdk`. What stands is that a debug build
  cannot be proxied without a config and a main-thread DataStore read goes unnoticed without
  StrictMode.
- **The dependency graph is submitted (was H6) · 6** — Renovate is parked, so the graph would be
  submitted to nothing that acts on it.

## What v1.0 deliberately left standing

Recorded here so B does not rediscover it as a defect:

- The token-refresh scaffold stays — D46, taken in A2T1. It is built on Ktor's `bearer` provider,
  its stale-token comparison is correct, and it is tested. It has no caller because there is no real
  API, which is D20, not a bug.
- `docs/archive/` was never created. D42's review material was deleted outright in A0P2 rather than
  archived, because the archive's only stated purpose was to hold it until release A shipped.
- The 70 test ids no Maestro flow drives are a `doctor.py` note, not a failure. Coverage of the
  flows is D30's question, not a convention violation.
- Every `App*.kt` already takes a `modifier` — M2's premise is gone, which is why B1U2 carries M1
  and not M2.
