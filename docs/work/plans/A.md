# Release A · the retrospective's corrections, and a docs system that survives them

Status: draft
Agents: 2 · lane 1 91 (~7.3 h) · lane 2 97 (~7.8 h) · lane 0 open
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D43–D46

Every task carries an id of its own; the retrospective id it came from is on the line as `(was F12)`
and nowhere else. The evidence behind those ids is `docs/archive/2026-09-review/`, deleted when this
release ships.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

| File group | Owner lane | Tasks |
|---|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt` | 1 | A1X2 |
| `gradle/libs.versions.toml` | 2 | A2T2 |
| `CLAUDE.md`, `docs/spec/CODEBASE.md`, `docs/README.md` | 0 | A0P1, A0P2, A0P3 |
| `.github/workflows/build.yml` | 2 | A2P1, A2H1, A2H3 |
| `scripts/**` | 2 | A2P2 — except `doctor.py`, appended by A1U3 and A0P2 |

Lane 1 owns `core/ui`, `feature/*/presentation`, `service/core/ui`, `app/src/main/kotlin`, every
`strings.xml` and every golden. Lane 2 owns `build-logic`, `gradle`, `scripts`, `.github`,
`baselineprofile`, `app`'s build file and manifest, `service/core/{domain,data}`, `service/network`
and every `feature/*/{domain,data,di}`. Lane 0 owns the docs.

## Board

### Lane 0 · priority

- [x] A0P1 The docs system · 50 → 50
- [ ] A0P2 Retire PLAN.md, move the module tree, slim CLAUDE.md · 25
- [ ] A0P3 Ship A, draft B · 12

### Lane 1 · screens, design system, view models

- [ ] A1T1 Test plumbing stops being copied (was F19) · 12
- [ ] A1U1 Every non-root screen carries an Up control (was shell.8) · 6
- [ ] A1U2 A text field says its own name (was ui.10) · 6
- [ ] A1U3 The tabs have test ids (was qa.16) · 6
- [ ] A1X1 An empty category stops spinning (was F17a) · 12
- [ ] A1X2 Add-to-cart leaves the nav host (was F17b) · 12
- [ ] A1X3 BaseViewModel's defaults, and its dead lines (was F12 + half of F14) · 25 · decides D44
- [ ] A1U4 Forms scroll under the keyboard (was H3) · 6
- [ ] A1U5 Predictive back on dirty forms, auto-sizing numerics (was M3+M4) · 6

### Lane 2 · build, release, data, network

- [ ] A2P1 Two flavors (was F18) · 12 · decides D43
- [ ] A2H1 The release job ships a signed AAB or fails (was F17c) · 25 · after A2P1
- [ ] A2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45 · after A2P1
- [ ] A2H3 The R8 mapping ships with the release (was H1) · 6 · after A2H1
- [ ] A2P2 Hooks become a committed .githooks (was F10) · 12
- [ ] A2T1 The dead lines in domain and network (was half of F14) · 12 · decides D46
- [ ] A2T2 Coil and the two single-consumer plugins (was F24) · 6
- [ ] A2H4 network_security_config and StrictMode (was H7+H8) · 12

## Tasks

### A0P1 The docs system · 50

**Why** spec and work are mixed, there is no release unit, and every doc tells you to run the whole
gate.
**Done when** `docs/README.md` links every doc; `docs/{spec,reference,guides,work}` exist;
`build.yml` has a `changes` job; `CLAUDE.md` § Checks defines T0 and T1; `docs/PLAN.md` carries the
frozen banner and no item line has moved.
**Touches** `docs/**` (new only), `.github/**`, `.claude/commands/**`, `CLAUDE.md` head and § Checks.
**Checks** T1. **Depends** —

### A0P2 Retire PLAN.md, move the module tree, slim CLAUDE.md · 25

**Why** the tree lives in the file every session loads, `docs/ARCHITECTURE.md` mixes spec with
recipes, and `PLAN.md` is superseded.
**Done when** `docs/spec/CODEBASE.md` holds the fenced tree and `python3 scripts/doctor.py` is
33/33; `python3 scripts/test_scripts.py` is green; `wc -l CLAUDE.md` ≤ 300;
`grep -rn 'docs/ARCHITECTURE.md\|docs/PLAN.md' --include='*.md' --include='*.py' .` finds only the
stub; `docs/archive/2026-09-review/` holds the review.
**Touches** `CLAUDE.md`, `README.md`, `docs/**`, `scripts/_common.py`, `scripts/doctor.py`,
`scripts/test_scripts.py`.
**Read** `scripts/_common.py:25,65,426-475` · `scripts/doctor.py:392-427` ·
`scripts/test_scripts.py:185,249,413`.
**Steps** 1. Rename `docs/ARCHITECTURE.md` to `docs/guides/RECIPES.md`, then split its testing and
operations sections out. 2. Move `CLAUDE.md`'s fenced tree and convention-plugin table into
`docs/spec/CODEBASE.md`, byte for byte. 3. Repoint `CLAUDE_MD_FILE` to `MODULE_TREE_FILE`. 4. Add
`check_docs_index`, `check_task_ids`, `check_doc_budgets`. 5. Archive the review. 6. Stub `PLAN.md`.
**Checks** T1 + `test_scripts.py`. **Depends** A0P1

### A0P3 Ship A, draft B · 12

**Why** a release is a tag plus a changelog block, and the next plan should cost one command.
**Done when** every line above is `[x]` or `[-]`; `docs/spec/CHANGELOG.md` has the `v0.1.0` block
with `Estimate · Actual · Ratio`; the archive and the `PLAN.md` stub are deleted; this file is under
`docs/archive/plans/`; `docs/work/plans/B.md` exists as a draft.
**Touches** `docs/**`. **Checks** T1. **Depends** every other task

### A1T1 Test plumbing stops being copied (was F19) · 12

**Why** the Robolectric SDK pin is repeated in about 50 test files and eleven `PreviewScreenshotTest`
copies differ only by a package string.
**Done when** `grep -rn '@Config(sdk' --include='*.kt' feature core service app` returns nothing
outside the base class; a `robolectric.properties` carries the pin; one base class carries the
screenshot test and each module's copy is its subclass; `./gradlew test verifyRoborazziDebug` green.
**Touches** every `*/src/test`, `feature/template/presentation/src/test`.
**Read** `feature/template/presentation/src/test/kotlin/**/screenshot/PreviewScreenshotTest.kt`.
**Checks** T1 + goldens + `test_scripts.py` (the template changes). **Depends** —

### A1U1 Every non-root screen carries an Up control (was shell.8) · 6

**Why** three screens have a top bar with no arrow and `ProductDetailScreen` has no bar at all, so
there is no way back but the system gesture.
**Done when** all four match the other five; each has a `<stem>_upButton` test id; their screen
tests assert the navigation event.
**Touches** `feature/catalog/presentation`, `feature/settings/presentation`, goldens.
**Checks** T1 + goldens. **Depends** —

### A1U2 A text field says its own name (was ui.10) · 6

**Why** a field's label does not reach its semantics node, so a screen reader announces an unnamed
field.
**Done when** `AppTextField`'s label is on the input's semantics; a test in `:core:ui` asserts it by
`hasText` on the node with `hasSetTextAction`.
**Touches** `core/ui/component/AppTextField.kt` and its test. **Checks** T1 + goldens. **Depends** —

### A1U3 The tabs have test ids (was qa.16) · 6

**Why** the four tabs carry no `testTag`, so five Maestro flows tap English labels and break the
moment a device runs in Czech.
**Done when** each tab carries `tabs_<name>Tab`; the five flows use the ids; a `doctor.py` check
fails a tab without one.
**Touches** `app/src/main/kotlin/**/TopLevelDestination.kt`, `app/**/AppNavHost.kt`, `.maestro/**`,
`scripts/doctor.py` (appended at the end — lane 2 owns the rest of the file).
**Checks** T1 + `test_scripts.py`. **Depends** —

### A1X1 An empty category stops spinning (was F17a) · 12

**Why** an empty table maps to `null`, so `cached()` never emits and `products_empty` is
unreachable — the screen spins forever.
**Done when** a fetched-at marker distinguishes "not loaded" from "loaded and empty"; a test opens
an empty category and asserts the empty state.
**Touches** `feature/catalog/data`, `feature/catalog/presentation/products`.
**Read** `feature/catalog/data/**/source/DefaultLocalCatalogDataSource.kt:15-35`.
**Checks** T1. **Depends** —

### A1X2 Add-to-cart leaves the nav host (was F17b) · 12

**Why** the insert runs on `rememberCoroutineScope` inside `AppNavHost`, so a rotation just after
the tap cancels it and the item never reaches the cart.
**Done when** an `AddProductToCart` use case is called through `execute {}` from the product
detail view model; `AppNavHost` holds no coroutine scope; a view-model test covers it.
**Touches** `app/**/AppNavHost.kt`, `feature/catalog/{domain,presentation}`, `feature/cart/domain`.
**Checks** T1. **Depends** —

### A1X3 BaseViewModel's defaults, and its dead lines (was F12 + half of F14) · 25 · decides D44

**Why** 21 of about 28 call sites pass `loading = {}`, `whileSubscribed` has no caller, and nullable
state drops updates — the product-detail favourite race.
**Decide first** make the overlay opt-in and the state non-null, or opt-in only → D44.
**Done when** the overlay is opt-in; `grep -rn 'loading = {}' feature` returns nothing;
`whileSubscribed`, `executeAsFlow`, `AlertPayload` and `displayMessage` are gone; every module's
tests pass.
**Touches** `service/core/ui`, every `feature/*/presentation`.
**Read** `service/core/ui/**/viewmodel/BaseViewModel.kt`.
**Checks** T1 + full `test` (`service/` changed). **Depends** —

### A1U4 Forms scroll under the keyboard (was H3) · 6

**Why** `LoginScreen` is a fixed centred column, so at large font its submit button is unreachable.
**Done when** Login, Sign-up and Profile scroll; a large-font golden shows the button.
**Touches** `feature/auth/presentation`, `feature/profile/presentation`, goldens.
**Checks** T1 + goldens. **Depends** —

### A1U5 Predictive back on dirty forms, auto-sizing numerics (was M3+M4) · 6

**Why** there is no `BackHandler` anywhere, so a half-filled form is lost silently, and Czech price
strings wrap.
**Done when** a dirty form confirms before leaving with `PredictiveBackHandler`; numeric text uses
`TextAutoSize`; both have a test.
**Touches** `feature/profile/presentation`, `core/ui/component/AppText.kt`, goldens.
**Checks** T1 + goldens. **Depends** —

### A2P1 Two flavors (was F18) · 12 · decides D43

**Why** `staging` is `prod` plus one constant and an unreachable host, and it triples every `:app`
task.
**Decide first** cut `staging` and let `DebugMenu` follow `BuildConfig.DEBUG`, or keep three → D43.
**Done when** `ProjectConfig.Flavor` has two entries; `:app:compileStagingDebugKotlin` is out of
`build.yml`; the debug menu still appears on `dev` and not on `prod`.
**Touches** `build-logic/**/ProjectConfig.kt`, `app/build.gradle.kts`, `app/src/staging`,
`.github/workflows/build.yml`, `feature/devmenu`.
**Checks** T1 + full `test`. **Depends** —

### A2H1 The release job ships a signed AAB or fails (was F17c) · 25 · after A2P1

**Why** a tag pushed before the secrets exist publishes a debug-signed APK, Play needs an AAB, and a
hotfix's `versionCode` regresses because it counts commits.
**Done when** the job runs `bundleProdRelease`, fails hard without a keystore, and derives
`versionCode` from the tag; a dry run on a throwaway tag proves both paths.
**Touches** `.github/workflows/build.yml`, `build-logic/**/ProjectConfig.kt`, `app/build.gradle.kts`.
**Checks** T1 + `:app:bundleProdRelease`. **Depends** A2P1

### A2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45 · after A2P1

**Why** the profile is recorded minified into `src/devRelease`, so `prodRelease` never sees it and
the whole benefit is lost silently.
**Decide first** fix the wiring, or cut the module and its two Gradle plugins → D45.
**Done when** either `prodRelease`'s merged assets contain `baseline-prof.txt`, or
`:baselineprofile` and its plugins are gone and `settings.gradle.kts` no longer includes it.
**Touches** `baselineprofile/**`, `app/build.gradle.kts`, `build-logic/**`, `settings.gradle.kts`.
**Checks** T1 + `:app:assembleProdRelease`. **Depends** A2P1

### A2H3 The R8 mapping ships with the release (was H1) · 6 · after A2H1

**Why** a minified stack trace is unreadable without `mapping.txt`, and nothing keeps it.
**Done when** the release job uploads `mapping.txt` as an artifact and attaches it to the tagged
release.
**Touches** `.github/workflows/build.yml`. **Checks** T1. **Depends** A2H1

### A2P2 Hooks become a committed .githooks (was F10) · 12

**Why** `install_hooks.py` is 153 lines reimplementing `core.hooksPath`, and
`export_service.py --sync-versions` is a 170-line TOML resolver for a script with no consumer.
**Done when** `.githooks/pre-commit` is committed and `README.md` names the one `core.hooksPath`
line; `install_hooks.py` is gone; `export_service.py` prints the catalog entries instead of merging
them; `scripts/README.md` and `test_scripts.py` follow.
**Touches** `.githooks/**`, `scripts/**`, `README.md`.
**Checks** T1 + `test_scripts.py`. **Depends** —

### A2T1 The dead lines in domain and network (was half of F14) · 12 · decides D46

**Why** `combineOutcomes`, `recover`, `flatMap` and 222 lines of token-refresh scaffold have no
caller, and the last duplicates what Ktor's `bearer` already does.
**Decide first** delete them, or rebuild the refresh path on Ktor now → D46.
**Done when** `grep -rn 'combineOutcomes\|chainOutcomes\|TokenRefresher' --include='*.kt' .` returns
nothing, or returns only a Ktor-based implementation with a test.
**Touches** `service/core/domain`, `service/network`.
**Checks** T1 + full `test`. **Depends** —

### A2T2 Coil and the two single-consumer plugins (was F24) · 6

**Why** Coil is put on all thirteen Compose modules so that `doctor.py` can forbid it on twelve.
**Done when** Coil is declared by `core/ui` alone and Ktor by `service/network` alone; the
convention plugins no longer add either; the `doctor.py` check that forbade Coil is deleted.
**Touches** `build-logic/**`, `core/ui/build.gradle.kts`, `service/network/build.gradle.kts`,
`gradle/libs.versions.toml`, `scripts/doctor.py`.
**Checks** T1 + full `test`. **Depends** —

### A2H4 network_security_config and StrictMode (was H7+H8) · 12

**Why** cleartext is not denied, a debug build cannot be proxied, and a main-thread DataStore read
goes unnoticed.
**Done when** a `network_security_config` denies cleartext with a debug override; StrictMode's
thread and VM policies are installed in debug only; both are asserted by a test or a manifest check.
**Touches** `app/src/main/res/xml`, `app/src/main/AndroidManifest.xml`, `app/**/App.kt`.
**Checks** T1. **Depends** —
