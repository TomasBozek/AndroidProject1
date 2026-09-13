# Release D · the patch-up: what B3S1 left unreachable, and the rules that boxed it in

Status: closed 2026-09-13 · shipped as v1.1.0, with release B
Agents: 1 · 10 tasks · 8 shipped, estimate 42, actual 48 · D1X2 to the backlog, D1X4 into E1X1 (D62)
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D58–D60

B3S1 landed fifty points of Trips — five screens, thirty source files, ten tests and a Room
database — and **nothing anywhere constructs `TripsDestination`**. The feature ships in the APK and
no user can open it. `TripsScreen.kt:39` says why the obvious patch does not work: *"No up arrow:
this is a tab root, and the bottom bar is what leaves it."* It was built as a tab and never given
one, so pushing it from anywhere else strands the user on a screen with no way back.

Everything here is a fix, a wrong number or a stale row. **No new feature, no redesign, no refactor
that reaches `build-logic/`.** Three of these are broken now and silent: `export_service.py` does not
parse on the Python `README.md` promises, five generators end by recommending the one command
`CLAUDE.md` forbids, and nineteen test ids sit outside a vocabulary `doctor.py` never learned to
check.

The release runs **before** B3S2 and before C, with one agent and no other lane in flight. That is
what makes a 60-point release legal at all, and unpicking the rules that say otherwise is `D1P1`,
which runs first.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

One agent works this release top to bottom and no other lane is running, so the table records what
is touched rather than arbitrating. It still matters to what comes next: B3S2 and C0P1 both write
`app/**`, and C0P1 writes the very enum `D1X1` extends.

| File group | Owner lane | Tasks |
|---|---|---|
| `docs/ai/PROCESS.md`, `docs/README.md`, `docs/STATUS.md` | 1 | D1P1 |
| `CLAUDE.md` | 1 | D1P1, D1P3, D1X4 |
| `app/**` — `TopLevelDestination.kt` and `AppNavHost.kt` included | 1 | D1X1, D1X2 |
| `feature/devmenu/**` | 1 | D1X2 |
| `feature/{trips,cart,home,catalog}/presentation/**`, `.maestro/**` | 1 | D1X4 |
| `scripts/**` | 1 | D1X3, D1P2, D1P4, D1X5 |
| `docs/ai/CODEBASE.md`, `docs/ai/reference/**`, `docs/ai/TESTING.md`, `docs/ai/DEPENDENCIES.md` | 1 | D1P3, D1P4 |
| `core/di/**`, `feature/trips/presentation/**` | 0 | D0X1 |

`settings.gradle.kts`, `gradle/libs.versions.toml`, `build-logic/**` and
`.github/workflows/build.yml` are untouched: no task adds a module, a dependency or a job. That is
deliberate — a `build-logic/` edit escalates T1 to the whole `./gradlew test`, which is not what a
patch release is for. See § Not in this release. `core/di/**` was in that list until `D0X1`, which
is what a lane-0 task is for: it may touch anything, and the table records it afterwards.

## Board

The board this file was worked from is gone: it closed into the `v1.1.0` block in
[../../CHANGELOG.md](../../CHANGELOG.md), together with release B's. What is left here is why each
task was there and what finished it. Two tasks did not finish here: `D1X2` is a line in
[../../BACKLOG.md](../../BACKLOG.md) § Next, and `D1X4` is release E's `E1X1`. Both were marked
`blocked: no JDK 25 toolchain`, which was true of the sandbox they were attempted in and not of the
repository (D62).

## Tasks

### D0X1 The Trips tab crashes on open · 6

**Why** Appended after `D1X1` shipped, because making Trips reachable is what made this reachable
too. `TripsViewModel`, `TripsListViewModel` and `TripDetailViewModel` each default a `Clock`
— `private val clock: Clock = Clock.systemDefaultZone()` — and no module binds one. Koin's `*Of`
builders resolve every constructor parameter through `get()` and never consult a Kotlin default, so
`koinViewModel<TripsViewModel>()` asks the graph for a `Clock`, finds none, and throws
`NoDefinitionFoundException` the moment the tab is tapped. `KoinGraphTest` does not catch it:
`verify()` treats a defaulted parameter as already satisfied. Nothing constructed these three view
models before `D1X1`, which is why a feature that had been merged and green for a day crashed on
first contact.
**Done when** `single<Clock>` is bound in `core/di`; no `*ViewModel` defaults a constructor
parameter; `python3 scripts/doctor.py` passes and **fails** when a default is put back;
`python3 scripts/test_scripts.py` passes; the Trips tab opens.
**Touches** `core/di/**/Koin.kt`, the three trips view models, `TripDetailViewModelTest`,
`scripts/doctor.py`, `scripts/test_scripts.py`, `docs/ai/CODEBASE.md`, `docs/ai/reference/CORE.md`.
**Read** `core/di/src/main/kotlin/.../Koin.kt` § `DefaultDispatcherProvider` ·
`feature/trips/presentation/.../trips/TripsViewModel.kt:13-17` ·
`app/src/test/kotlin/.../KoinGraphTest.kt:50-67` · `docs/ai/CODEBASE.md` § Known constraints.
**Steps** 1. Bind `Clock` beside `DispatcherProvider` — ambient system state a test has to be able
to fix, which is the same argument that put the dispatchers there. 2. Drop the three defaults: they
are what made the constructor read as safe. 3. `TripDetailViewModelTest` was relying on the
default and on the system clock with it, so it gains the fixed clock its two siblings already use.
4. `check_koin_constructor_defaults` in `doctor.py`, with its fixture — `verify()` cannot see this
class of defect, so something has to.
**Checks** T1. **Depends** D1X1

### D1P1 One plan open, a queue behind it, and room to write · 6 · decides D58

**Why** Three rules forbid this release as written. `../PROCESS.md` § States ends *"At most one plan
is open, and one a draft"* — B is open and C is a draft, so D is a third, and `/task` step 2, *"open
the plan under `docs/ai/plans/` whose header says `Status: open`"*, would have two files to choose
from and no written tie-break. § Points and lanes lowers agents *"until … none is under 85"*, which
forbids a 60-point release outright and whose neighbouring sentence — *"Never pad a lane"* — closes
the obvious workaround. And the line budget is at its ceiling on both files it names: `CLAUDE.md` is
299 of 300 and `../PROCESS.md` **120 of 120**, enforced by `doctor.py`'s `check_doc_budgets`, so any
edit to § States that is not line-neutral fails the build. The budget is also **written twice** — at
`../PROCESS.md:110` and in `../README.md` § Rules for these docs — which is the one thing
`../README.md` § Rules says never to do.
**Decide first** One plan is open; any number queue behind it as drafts, worked in the order the
owner sets — here D, then B's remainder, then C. The 90–100 band sizes a **lane** when there is more
than one agent, not a release; a release is as big as its work. The budgets rise and move to
`../README.md` alone, as targets: terse is the goal, and a line count was only ever a proxy for it
→ D58.
**Done when** each of the three rules appears once in `../PROCESS.md`; `grep -rn '≤ 300' docs/
CLAUDE.md` returns one file, not two; `docs/STATUS.md` carries release D's board **and** keeps
release B's seventeen `[x]` under a Paused heading rather than cutting them; `/task` with no
argument resolves to one plan; `python3 scripts/doctor.py` passes, `check_doc_budgets` and
`check_docs_index` included.
**Touches** `docs/ai/PROCESS.md`, `docs/README.md`, `docs/STATUS.md`, `CLAUDE.md`,
`.claude/commands/task.md`, `.claude/commands/release.md`, `scripts/doctor.py`.
**Read** `docs/ai/PROCESS.md:74-85` § States · `docs/ai/PROCESS.md:27-50` § Points and lanes ·
`docs/ai/PROCESS.md:110` · `docs/README.md` § Rules for these docs · `.claude/commands/task.md:11-14`
· `.claude/commands/release.md:11,20-23` · `scripts/doctor.py` § `check_doc_budgets`,
§ `check_docs_index`.
**Steps** 1. § States: one open plan, a queue of drafts behind it, and the order named. 2. § Points
and lanes: the band sizes a lane; a release is as big as its work. 3. Raise the budgets and move
them to `../README.md` alone; `../PROCESS.md:110` keeps the link and loses the numbers. Move
`doctor.py`'s constants with the rule — the check reads them, so it moves or it fails. 4. `/task`
gets its tie-break: the plan the id's release letter names, and with no argument the one open plan.
`/release close` gets the same. 5. `docs/STATUS.md` grows a **Paused · release B** section so
seventeen merged tasks survive the handover.
**Checks** T0 — no Kotlin moves. **Depends** —

### D1X1 Trips takes the fifth tab · 6 · decides D59

**Why** `TripsDestination` is constructed nowhere. Grepping every `.kt` outside `feature/trips/**`
returns `AppNavHost.kt`'s `tripsDestination(backStack)` — which registers the entry and never pushes
the key — and `KoinGraphTest`. `TopLevelDestination.kt:44-47` has four tabs and no Trips. The other
four trips screens each carry an `onNavigateUp` and are reachable *from* `TripsScreen`; only
`TripsScreen.kt:39` has none, because it is a tab root. One tab entry unlocks all five screens and
nothing short of one will.
Two secondary facts are wrong and go with it. `../../BACKLOG.md:41-43` and
`reference/FEATURES.md:85-87` both say the tab was skipped because the enum and `:app`'s strings were
*"outside that lane's file set"* — they were **inside** it (`B.md:21` gives all of `app/**` to lane
3); what they were outside of was B3S1's own `Touches` line. And `FEATURES.md:87` cites **D47** for
it, which reads *"For release A only"*.
**Decide first** Trips takes the fifth tab. Five is Material's ceiling for a bottom bar, so the slot
is single-use, and release C's drafted D55 spends it on the arcade hub — that premise is reversed
here, and `C0P1` picks its own entry point (its own text offers "a row on Home") when C is drafted.
Not amended here: C is a draft and editing it is not this release's business → D59. *(Taken by the
owner; the row records it and the reasoning.)*
**Done when** `grep -c 'testTag = "tabs_' app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt`
is 5, from 4 — **not** `grep -c 'tabs_'`, which is already 5 today because of the KDoc at `:32`, and
which is the defect in `B0U1`'s and `C0P1`'s own Done-when lines; `python3 scripts/doctor.py` passes
and `check_tab_test_ids` (`scripts/doctor.py:1338`) rejects the entry without `tabs_tripsTab`;
`tab_trips` is in both `app/src/main/res/values/strings.xml` and `values-cs/strings.xml`;
`./gradlew :app:test` passes; a Maestro flow reaches the feature by `id: "tabs_tripsTab"` and asserts
`id: "TripsScreen"`; `reference/FEATURES.md:85-87` and the backlog line are gone, not corrected.
**Touches** `app/**/TopLevelDestination.kt`, `app/src/main/res/values/strings.xml`,
`app/src/main/res/values-cs/strings.xml`, `.maestro/trips.yaml` (new),
`docs/ai/reference/FEATURES.md:48,85-87`, `docs/ai/reference/CORE.md`, `docs/BACKLOG.md`,
`docs/DECISIONS.md`.
**Read** `app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt:29-48` ·
`feature/trips/presentation/.../trips/TripsScreen.kt:39-41` · `scripts/doctor.py:1330-1365` ·
`.maestro/browse-to-a-product.yaml` · `docs/ai/reference/FEATURES.md:85-87` · `docs/DECISIONS.md` D47
· `docs/ai/plans/C.md:117-148` (C0P1, so the reversal is written knowing what it costs).
**Steps** 1. Add `Trips(TripsDestination, R.string.tab_trips, Icons.Filled.DateRange, testTag =
"tabs_tripsTab")`. The icon comes from `material-icons-core`: the *extended* set is not a dependency
and `CLAUDE.md` § Project says a new one has to earn its place, so it is `DateRange` (already used
here, and a trip is a date range) or `Place`. 2. `tab_trips` in both locales — Czech `Výlety`; five
labels is tight on a narrow phone, so check the bar at 320 dp before committing. 3. One Maestro flow:
sign in, tap `tabs_tripsTab`, assert `TripsScreen`, tap through to `TripsListScreen`. 4. Delete the
`FEATURES.md` paragraph and the backlog line; add the tab to `reference/CORE.md`. 5. Write D59.
**Checks** T1. No golden moves: the 276 goldens under `feature/*/presentation/src/test/screenshots`
are per-preview and `NavigationSuiteScaffold` lives in `AppNavHost`, outside every one of them.
**Depends** D1P1 *(which makes the release legal; the code does not depend on it)*

### D1X2 The dev menu jumps straight to a screen · 12

**Why** Not reachability — `D1X1` fixes that, and after it **every** destination in the app is
reachable, so a menu of orphans would be empty. What is missing is *depth*: reaching `TripWizard`
means signing in, tapping a tab, tapping through a list and into a wizard, and a tester doing that
forty times a day is the case a debug menu exists for (D16). The pattern is already there —
`DevMenuDestination.kt:22` takes `navigateToComponents: () -> Unit` and `AppNavHost.kt` § `debugEntries`
wires it inside the `DebugMenu.ENABLED` branch, so `prod` folds it away and R8 drops the classes.
What is missing is a second one: the Tools section is three `AppButton`s (`DevMenuScreen.kt:105-131`)
and not one opens a screen. It also earns its keep the next time a feature lands without an entry
point — which is the thing that just happened.
**Done when** the menu opens `TripWizard`, `TripsList`, `ProductSearch`, `SettingsPermissions`,
`Profile` and `GalleryDetail` directly, each by `id: "devMenu_<name>Button"`; a destination that takes
a route argument is jumped to with a named fixture value, not a blank one; `python3 scripts/doctor.py`
passes; `./gradlew :feature:devmenu:presentation:test verifyRoborazziDebug` passes in one invocation;
the nine goldens under `feature/devmenu/presentation/src/test/screenshots` are re-recorded and
**opened before they are committed** — `CLAUDE.md` § Testing a screen, a golden nobody looked at is a
test that passes forever.
**Touches** `feature/devmenu/presentation/**` — `devmenu/DevMenuScreen.kt`, `DevMenuEvent.kt`,
`DevMenuNavigation.kt`, `DevMenuDestination.kt`, both `res/values*/strings.xml`, the two tests, the
nine goldens — and `app/**/AppNavHost.kt`.
**Read** `feature/devmenu/presentation/.../devmenu/DevMenuDestination.kt:16-41` ·
`DevMenuScreen.kt:100-131` · `DevMenuNavigation.kt` · `app/**/AppNavHost.kt` § `debugEntries` ·
`feature/catalog/presentation/.../productdetail/ProductDetailDestination.kt` *(a route with an
argument)* · `CLAUDE.md` § Screen structure, § Test identifiers.
**Steps** 1. Pass **one `List<DevMenuJump>`** — a label and a lambda — built in `AppNavHost`, rather
than growing `devMenuDestination()`'s parameter list one feature at a time; six parameters today is
twelve next release. A feature's presentation may not name another's, so the list is the seam and
`AppNavHost` is the only place that knows every destination. 2. One `DevMenuNavigation` intent
carrying the chosen jump, and one `DevMenuEvent`, following `Components` exactly. 3. Render with
`AppListItem`, already in `:core:ui`: a growing list of targets is a list, not six more buttons.
**No component is added** — `create_component.py` is not called. 4. A section-header string plus one
label per target in both locales, named `dev_menu_*`. 5. Extend `DevMenuScreenTest` to assert the tap
emits the event and `DevMenuViewModelTest` the navigation it produces.
**Checks** T1 + `verifyRoborazziDebug` **in the same `./gradlew` invocation as `test`** — a
presentation module moved, and two invocations run every Robolectric test twice. **Depends** D1X1
*(so the list is written once, with Trips already a tab)*

### D1X3 `export_service.py` parses on the Python the README promises · 3

**Why** `scripts/export_service.py:265` nests same-type quotes inside an f-string — PEP 701, which is
a hard `SyntaxError` before Python 3.12. `README.md:18` states the requirement as "Python 3.10+,
standard library only". On 3.11 the script cannot print `--help`, and `scripts/test_scripts.py` fails
2 of its 57 tests. CI never sees it: `.github/workflows/build.yml:92,302` pin `python-version: '3.12'`.
It is the only one of the ten scripts that does not byte-compile.
**Done when** `python3 -m py_compile scripts/export_service.py` succeeds on 3.11 — today it reports
`SyntaxError: f-string: unterminated string`; `python3 scripts/test_scripts.py` is 57 of 57, from 55;
`python3 scripts/export_service.py --help` prints.
**Touches** `scripts/export_service.py` — *or* `README.md:18`, not both.
**Read** `scripts/export_service.py:263-267` · `README.md:18` · `.github/workflows/build.yml:92,302`.
**Steps** 1. Hoist the inner `", ".join(...)` into a local before the f-string; the nesting is the
whole defect and it is the only line of its kind in the repository. 2. If the owner would rather
raise the floor to 3.12, change `README.md:18` instead and leave the workflow pins alone — but do one
or the other, because doing both leaves the README describing a floor nothing enforces.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### D1P2 The generators stop recommending the build that is forbidden · 6

**Why** `CLAUDE.md` § Checks says **never `./gradlew build`** — it assembles every variant and runs R8
three times — and `README.md:40` repeats it. Four generators and the initialiser close by telling the
user to run exactly that, so the first thing a new contributor does after scaffolding a feature is
the one command the rules forbid. `A.md:227` shows this was caught once already: A0P4 fixed
`README.md` and never swept the scripts. `create_component.py:228` is the one that does it right.
**Done when** `grep -rn 'gradlew build' scripts/ docs/ CLAUDE.md` returns only prose that is *about*
the prohibition; `python3 scripts/create_feature.py userProfile --dry-run` ends on a command from
`CLAUDE.md` § Checks; `python3 scripts/test_scripts.py` passes.
**Touches** `scripts/create_feature.py:357`, `scripts/create_screen.py:448`,
`scripts/create_datasource.py:454`, `scripts/delete_feature.py:212`, `scripts/init_project.py:281`,
`scripts/README.md:98`, `scripts/test_scripts.py:11`, `docs/ai/RECIPES.md:184`.
**Read** `scripts/create_component.py:228` *(the one already right)* · `CLAUDE.md` § Checks ·
`docs/ai/plans/A.md:227`.
**Steps** 1. Replace each with the T0 pair — `python3 scripts/doctor.py && ./gradlew
:app:assembleDevDebug test` — or the generated module's own `:…:assembleDebug`, the way
`create_component.py` does. 2. Correct the three prose copies in the same commit. 3. No test asserts
the old string (`grep -n 'Done. Run' scripts/test_scripts.py` is empty), so nothing else moves.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### D1X5 `init_project.py` rewrites the LICENSE it leaves behind · 3

**Why** `init_project.py` rewrites the whole repository into a new project and never touches
`LICENSE`, so every project generated from this template ships this template's copyright holder.
The cause is mechanical: the walk selects files by extension (`TEXT_SUFFIXES` — `.kt .kts .xml .toml
.py .md …`) and `LICENSE` has none, so it is skipped silently. `grep -n 'LICENSE\|copyright'
scripts/init_project.py` returns nothing.
**Done when** `python3 scripts/init_project.py --package com.acme.app --name "My App" --author
"Acme"` leaves `LICENSE` naming Acme; a `test_scripts.py` case asserts it; `python3 scripts/doctor.py`
passes.
**Touches** `scripts/init_project.py`, `LICENSE`, `scripts/test_scripts.py`, `docs/ai/RECIPES.md`,
`README.md` *(only if it lists the flags)*.
**Read** `scripts/init_project.py` § `TEXT_SUFFIXES` and the rewrite walk · `LICENSE`.
**Steps** 1. Add `--author`, defaulting to the `--name` value so the flag is optional. 2. Add
`LICENSE` to the rewrite set by name rather than by suffix. 3. One `test_scripts.py` case.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### D1P3 The reference docs catch up with B3S1 · 6

**Why** Eight one-line facts are wrong, six made wrong by the Trips merge. They are the inventory an
agent reads *instead of* the code, so each is a wrong answer to a question somebody will ask. The
worst is `reference/DESIGN-SYSTEM.md:88` — "Twenty-seven of the components are currently composed
only by the gallery" — because that number is the premise of D38, of release C's opening paragraph
and of the backlog line saying the work is "not estimable until … the count of components nothing
composes is known". It is knowable now, and it is not 27.
**Done when** each figure matches what the repository contains, proved by the grep in its step, and
`python3 scripts/doctor.py` passes.
**Touches** `docs/ai/reference/DESIGN-SYSTEM.md:36,48,88`, `docs/ai/TESTING.md:42`,
`docs/ai/DEPENDENCIES.md:27`, `docs/ai/CODEBASE.md:116`, `docs/ai/reference/SERVICES.md:16`,
`docs/ai/RECIPES.md:14`, `CLAUDE.md:17`, `docs/ai/plans/C.md:7`, `docs/BACKLOG.md`.
**Read** each line named above, then the grep that proves it.
**Steps** 1. `DESIGN-SYSTEM.md`: "Forty-four in the gallery" → 46 (`:36`); add the missing
`AppAvatarPhoto` to the Status group row (`:48`); and at `:88` **write the method beside the
number** — composed by nothing outside `core/ui` and `feature/gallery` gives **16**, or **15** if
`AppScreenChrome` is excluded as the shell `AppTheme` installs rather than a component a screen
composes. C0T1 re-runs this scan and must get the same answer, which a bare number does not give it.
2. `TESTING.md:42`: eleven `PreviewScreenshotTest` copies → twelve. 3. `DEPENDENCIES.md:27`: Room
serves "the two databases" → three; `feature/trips/data` applies `convention.android.room` too.
4. `CODEBASE.md:116`: the `SessionState` row lists three of four — add `Onboarding`, the one that
outranks the others. 5. `SERVICES.md:16`: drop `Credentials`, which is a KDoc sentence on
`UnauthorizedError`, not a type. 6. `RECIPES.md:14`: `create_feature.py` clones **four** layers, not
five. 7. `CLAUDE.md:17`: "five sample features" → drop the number rather than correct it to ten, so
it cannot go stale a third time. 8. `C.md:7` and the backlog's "Unused components" line: five
features / twenty-one screens / 26 of 46 → ten / twenty-three / the number step 1 settles. C is a
draft, so this is not an edit to the open plan.
**Checks** T0. **Depends** D1X1 *(which rewrites `FEATURES.md:85-87` itself)*

### D1P4 Two `doctor.py` checks that keep the reference docs honest · 6

**Why** `D1P3` corrects eight facts by hand and nothing stops the ninth. Both drifts it fixes are
mechanically checkable, and the backlog has wanted them since release A: every destination appears in
`reference/FEATURES.md`, every `App*` file in `reference/DESIGN-SYSTEM.md`. The pattern exists —
`check_gallery_lists_every_component` (`scripts/doctor.py:1382`) is the same shape, added by B1U3 for
the same reason. Both targets are correct once `D1P3` lands, so the checks lock in a true state
rather than opening a cleanup.
**Done when** `grep -c 'def check_' scripts/doctor.py` is 37, from 35; `python3 scripts/doctor.py`
passes, and **fails** when a `*Destination.kt` is added without a `FEATURES.md` row or an `App*.kt`
without a `DESIGN-SYSTEM.md` row; both fixtures are in `python3 scripts/test_scripts.py`.
**Touches** `scripts/doctor.py`, `scripts/test_scripts.py`, `docs/BACKLOG.md` *(the Someday line
goes)*, `scripts/README.md` *(only if it counts the checks)*.
**Read** `scripts/doctor.py:1382` § `check_gallery_lists_every_component` · its fixture in
`scripts/test_scripts.py` · `docs/ai/reference/FEATURES.md` § Screens · `reference/DESIGN-SYSTEM.md:42-48`.
**Steps** 1. `check_features_lists_every_destination`: 23 `*Destination.kt` under
`feature/*/presentation/src/main` against the Screens table. 2.
`check_design_system_lists_every_component`: the `App*.kt` files against the group rows — the same
scan `D1P3` step 1 writes the method for, so the two agree by construction. 3. A fixture each.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** D1P3

### D1X4 The test ids rejoin the closed vocabulary · 12 · decides D60

**Why** `CLAUDE.md` § Test identifiers closes the element half of a tag to thirteen words. Nineteen
tags end in something else — `State`, `Stepper`, `Empty`, `Group`, `Skeleton`, `tabs`, `Accordion`,
`Slider`, `Card`, `Summary`, `Progress`, `Segmented` — and **fourteen arrived with B3S1**, so the
showcase feature has been extending the vocabulary by writing. `doctor.py` does not catch it: none of
its 35 checks reads the element list, which is why a green run says nothing. The rule is either real
and enforced or it is not a rule.
**Decide first** Rename the nineteen to the closed list, or widen the list to name what screens
actually hold — a stepper, a slider and an accordion are each a control, and `_emptyState` says
something `_tile` does not. Renaming is 12 and keeps thirteen words; widening is 3 and admits the
vocabulary was short. **Whichever wins, `doctor.py` gets the check** — that is the half that stops it
recurring, and the generators already write conforming tags → D60.
**Done when** `python3 scripts/doctor.py` passes and **fails** when a tag outside the vocabulary is
added; `grep -c 'def check_' scripts/doctor.py` is one higher than `D1P4` left it; the touched
modules' `./gradlew test verifyRoborazziDebug` passes in one invocation;
`.maestro/add-to-cart.yaml` still resolves `cart_emptyState` or its replacement, because a flow
reads that one and `check_maestro_ids_exist` will say so.
**Touches** `scripts/doctor.py`, `scripts/test_scripts.py`, `CLAUDE.md` § Test identifiers *(if the
vocabulary widens)*, and under the rename `feature/{trips,cart,home,catalog}/presentation/src/{main,test}`
and `.maestro/add-to-cart.yaml`.
**Read** `CLAUDE.md` § Test identifiers · `feature/trips/.../component/TripDetailsStep.kt:52,73,80` ·
`feature/trips/.../trips/TripsScreen.kt:47,81,125` · `feature/cart/.../cart/CartScreen.kt:53,84` ·
`feature/home/.../home/HomeScreen.kt:47` · `.maestro/add-to-cart.yaml`.
**Steps** 1. Settle D60 first — the rename set depends on it. 2. Apply it to the four presentation
modules and their `*ScreenTest.kt`, and to the one Maestro flow that names a tag. 3. Add
`check_test_id_vocabulary` to `doctor.py` with its fixture. 4. If the vocabulary widened, `CLAUDE.md`
§ Test identifiers carries the new list — and stays inside the budget `D1P1` has by then made a
target rather than a wall.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`, and `python3
scripts/test_scripts.py`. **Depends** D1P1 *(the `CLAUDE.md` budget)*, D1P4 *(the check count)*

## Not in this release

Each verified, sized and left where it is — with the backlog line corrected in `D1P3` where the
number was wrong.

- **The Maestro flows still tap fixture text** — `"Beverages"`, `"Coffee"`, `"Olive oil"`. B0U1 fixed
  the tabs and deferred this deliberately. It is **12, not the backlog's implied small change**: list
  rows carry a *constant* tag today (`CategoriesScreen.kt:50` is `categories_item`), so a per-row id
  means `categories_item_<id>`, which breaks the closed vocabulary `D1X4` is busy enforcing and needs
  `doctor.py`'s `declared_test_ids()` to learn a suffix. It wants its own decision and it should
  follow `D1X4`, not precede it.
- **`service/core/ui`'s build file declares 16 library dependencies** (`service/core/data` 3, `:app`
  3, `core/ui` 1, `service/core/domain` 1) against `CLAUDE.md`'s "a module build file is a `plugins`
  block and its project dependencies. Nothing else." The backlog's "about fifteen" is close enough.
  It is **6, not 12** — B2T1 set the precedent with `CoreUiConventionPlugin`, and `export_service.py`
  copies `build-logic/` with the service modules so the export recipe survives — but it puts
  `build-logic/` in the diff and escalates T1 to the whole `./gradlew test`. Wrong shape for a patch
  release; right shape for B's lane 2 or C's lane 0.
- **The design system stops speaking POS** — the backlog's "64 mentions" is wrong twice over: 80 by
  case-insensitive substring (counting "s**till**", "a**void**", "**cach**e"), **50** by word
  boundary across 26 files. And it is not cosmetic: `TagTone.Void` and `AppTheme.colors.statusVoid`
  are public design-system API used 49 times in 8 files, two of them *feature* components. It
  re-records roughly ten components' previews out of `:core:ui`'s 102 goldens. **12, and a redesign.**
- **`network_security_config` and StrictMode** · 6 — verified absent (no `app/src/debug/` source set
  exists at all). It is hardening, not a patch-up, and it adds a source set.
- **The dependency graph is submitted** · **3, not 6** — one `dependency-graph: generate-and-submit`
  line on the existing `setup-gradle` step. Held on value, not cost: D26 has Renovate parked, so it
  submits to nothing that acts on it.
- **Unused components earn their place** — already `C0T1`, still blocked on B3S2, and `D1P3` step 1
  gives it the method it needs. 16 homeless today, down from 27 before Trips.
- **A connectivity banner** · 12 — a new UI surface, which is what this release excludes.

## What was not checked

`./gradlew ktlintCheck` and every other Gradle task **did not run** while this plan was written:
Gradle needs a JDK 25 toolchain and `api.foojay.io` is blocked from this environment (`403` through
the proxy). Every claim above comes from `doctor.py` (35 checks, green), `test_scripts.py` (55 of 57),
`python3 -m py_compile` and reading. The first task to run T1 on a machine with the toolchain settles
whether ktlint has anything to say — treat a ktlint failure as expected work, not a surprise.
