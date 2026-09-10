# Release C · the arcade: the design system learns to play

Status: draft
Agents: 4 · lane 0 124 (~10 h, first and last) · lane 1 85 (~6.8 h) · lane 2 85 (~6.8 h) · lane 3 86 (~6.9 h)
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D54–D57

The app has five features and twenty-one screens, and **26 of the 46 `App*` components in `:core:ui`
are reached only by the gallery** — a screen that exists to list them. D38 has said since the design
system landed that none of them is deleted until a real screen has had the chance to use it. Nothing
in a catalogue, a cart or a settings list wants a tri-state checkbox, a date field, a tooltip or a
skeleton, so the chance never came.

This release is that chance, and it is meant to be enjoyable rather than dutiful: **nine small games
that are each an excuse to drive one group of components properly.** Set a stepper to a target in
the fewest taps. Release a button at exactly three seconds. Name the component on screen. Find the
prize behind a tab. Each is one screen, one view model and two tests — the unit this template is
built around — so the release is mostly the same task nine times, which is the point: it is what the
architecture is for.

**Twenty-five of the twenty-six get a home here.** The one that does not is `AppImage`, and C0T1 is
the task that decides it — see § What the games are for.

## Relationship to release B

B is open with 14 tasks. Nothing here needs B, and B needs nothing here, with two exceptions worth
knowing before you start:

- **B1U3** decides where the gallery's list comes from (D52). C2U4's question bank wants the same
  source, so if B1U3 has landed, read D52 first and use what it chose. If it has not, C2U4 owns its
  own list and D56 says so.
- **B3S1 and B3S2** (Trips, Field report) are also showcase features, and they showcase something
  else: platform seams — location, the Photo Picker, the Storage Access Framework. They are not
  duplicated by this release. If the two releases cannot both be afforded, they are the cheaper cut,
  because the arcade is what answers D38 and they are not.

## Shared files

| File group | Owner lane | Tasks |
|---|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**` — `AppNavHost.kt`, `KoinGraphTest.kt`, `TopLevelDestination.kt` | 0 | C0P1, C0P2 |
| `feature/arcade/domain/**`, `feature/arcade/data/**`, `feature/arcade/di/**` | 0 | C0P1, C0P3 |
| `feature/arcade/presentation/component/**`, `.../arcadehub/**`, `.../arcadesection/**` | 0 | C0P1, C0U2 |
| `service/core/ui/.../format/**` | 0 | C0U1 |
| `core/ui/**`, `feature/gallery/**` | 3 | C3U3, and C0T1 once every game has merged |
| one directory per game under `feature/arcade/presentation/` | 1, 2 | one task each — disjoint by construction |

**Lane 0 runs first and alone, then waits.** Its first five tasks put the feature, every route and
the records table in place *before* lanes 1–3 start, because `create_feature.py` and
`create_screen.py` write four shared files each and nine games would otherwise be nine collisions.
Its last two run after every game has merged. Lanes 1 and 2 then touch nothing but their own screen
directories and their own `strings.xml`.

## Board

### Lane 0 · the arcade exists, then the reckoning · 124

- [ ] C0P1 The arcade, its hub and a fifth tab · 25 · decides D54, D55
- [ ] C0U1 A stopwatch role for Formats · 6
- [ ] C0P2 Nine routes, generated and registered · 25 · after C0P1
- [ ] C0P3 Records: the domain, the table and the repository · 25 · after C0P1
- [ ] C0U2 How to play: one sheet, nine explanations · 12 · after C0P2
- [ ] C0T1 Unused components earn their place (was F6) · 25 · after every game
- [ ] C0P4 Ship C · 6 · after every other task

### Lane 1 · the precision games · 85

- [ ] C1U1 Set The Stepper · 12
- [ ] C1U2 Slider Sniper · 12
- [ ] C1U3 Switchboard · 12
- [ ] C1U4 Date Dash · 12
- [ ] C1U5 Tab Chase · 12
- [ ] C1U6 The component playground · 25 · decides D57

### Lane 2 · the reflex games, and what a record does · 85

- [ ] C2U1 Three Seconds · 12
- [ ] C2U2 Reaction · 12
- [ ] C2U3 Memory Match · 12
- [ ] C2U4 Name That Component · 25 · decides D56
- [ ] C2U5 A record that falls is felt, and can be shared · 12
- [ ] C2U6 Achievements: a badge for what you have beaten · 12

### Lane 3 · the scoreboard and the daily run · 86

- [ ] C3U1 The scoreboard: every game, every best · 25
- [ ] C3U2 The daily run: one seed a day, and a streak · 25
- [ ] C3U3 The gallery names the game that uses each component · 12
- [ ] C3U4 A deep link opens a game · 12
- [ ] C3U5 The hub remembers what you played last · 12

## What the games are for

The mapping is the plan. A game whose brief says "compose `AppSkeleton`" is not decoration — it is
the only reason that component still exists, and a game that quietly substitutes something easier
has failed even if it plays well.

| Game | Task | Components it is the home for |
|---|---|---|
| Set The Stepper | C1U1 | `AppStepper` · `AppFieldGroup` · `AppFormField` · `AppProgress` |
| Slider Sniper | C1U2 | `AppSlider` · `AppTooltip` · `AppStatusDot` |
| Switchboard | C1U3 | `AppCheckbox` (its tri-state `CheckState`) · `AppSelect` · `AppRadio` |
| Date Dash | C1U4 | `AppDateField` · `AppSheet` · `AppMenu` |
| Tab Chase | C1U5 | `AppTabs` · `AppBottomNav` · `AppNavRail` |
| The playground | C1U6 | `AppAccordion` · `AppScrollShadow` · `AppToolbar` |
| Three Seconds | C2U1 | `AppProgress` · `AppStatusDot` · `AppToast` |
| Reaction | C2U2 | `AppSpinner` · `AppFab` · `AppStatusDot` |
| Memory Match | C2U3 | `AppSkeleton` · `AppAvatar` · `AppCard` |
| Name That Component | C2U4 | `AppRadio` / `AppRadioGroup` · `AppBadge` · `AppDialog` |

That is 25 of the 26. **`AppImage` is the one left**, because nothing in an arcade wants a remote
image and inventing a reason would be the dishonest version of this exercise. It is also Coil's only
consumer, so C0T1 is deciding a component and a dependency at once — which is exactly the shape D38
wanted the question asked in.

## Tasks

### C0P1 The arcade, its hub and a fifth tab · 25 · decides D54, D55

**Why** There is nowhere to put a game. The feature does not exist, and neither does the screen that
lists what is in it.
**Decide first** two decisions, both cheap and both worth writing down. **D54:** a record is a row
in a Room table in `:feature:arcade:data`, or a value in DataStore. Recommended: **Room** — the
scoreboard (C3U1) is a query over rows ordered by score, `:feature:cart:data` and
`:feature:catalog:data` already apply `convention.android.room`, and DataStore would mean hand-rolled
serialisation of a growing map. **D55:** the arcade is a fifth tab, or a row on Home. Recommended:
**a fifth tab** — it is a destination of its own with its own back stack, `TopLevelDestination`
already carries everything a tab needs including its test id (B0U1), and the adaptive suite turns
five tabs into a rail on width without any work here.
**Done when** `python3 scripts/create_feature.py arcade` has produced four layers;
`ArcadeHubScreen` lists the sections and their games from a domain list, not a hard-coded one;
`grep -c 'tabs_' app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt` is 5 and
`python3 scripts/doctor.py` passes, `check_tab_test_ids` included; the hub has both its tests;
`./gradlew :app:assembleDevDebug test verifyRoborazziDebug` passes in one invocation; D54 and D55
are rows in `../../DECISIONS.md`; `docs/ai/reference/FEATURES.md` and `docs/ai/CODEBASE.md` list it.
**Touches** `feature/arcade/**` (new), `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`,
`app/**/KoinGraphTest.kt`, `app/**/TopLevelDestination.kt`, `app/src/main/res/values*/strings.xml`,
`docs/ai/CODEBASE.md`, `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Read** `app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt` ·
`feature/catalog/presentation/.../categories/` as the closest existing hub ·
`docs/ai/RECIPES.md` · `CLAUDE.md` § Screen structure.
**Steps** 1. Write D54 and D55. 2. `python3 scripts/create_feature.py arcade --dry-run`, then for
real. 3. The domain owns `Game` (id, section, title, the component group it exercises) and `Section`
— a sealed list in `domain`, not strings in a screen, because C3U3 and C0U2 both read it. 4. The tab
entry is `Games(ArcadeHubDestination, R.string.tab_games, Icons.Filled.SportsEsports, testTag =
"tabs_gamesTab")` — the tag is not optional and `doctor.py` says so. 5. Czech for every key.
6. `recordRoborazziDebug`, then **open what it wrote**.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + `python3 scripts/test_scripts.py`.
**Depends** —

### C0U1 A stopwatch role for Formats · 6

**Why** `Formats.duration` renders `2 h 10 m` — day, hour, minute and second only, two units at a
time. Three of these games are decided in milliseconds, and `CLAUDE.md` § Design system forbids a
screen formatting a number itself, so there is nothing a game can call. A reaction of 240 ms would
print as `0 s`.
**Done when** `Formats.stopwatch(duration)` renders `3.421 s` and `1:04.900` past a minute, from the
same `Locale` as the rest of the file — so the decimal separator is a comma in Czech; a JVM test in
`:service:core:ui` covers zero, sub-second, past a minute and negative; `LocalFormats` exposes it;
`docs/ai/reference/SERVICES.md` lists it beside the others.
**Touches** `service/core/ui/src/main/kotlin/.../format/Formats.kt`,
`service/core/ui/src/test/kotlin/.../format/FormatsTest.kt`, `docs/ai/reference/SERVICES.md`,
`docs/ai/CODEBASE.md` § API you build on.
**Read** `service/core/ui/src/main/kotlin/com/example/androidproject1/service/core/ui/format/Formats.kt:94-104`
· its test beside it.
**Steps** 1. Beside `duration`, not inside it — they answer different questions and the existing one
is right for what it does. 2. The unit letters stay untranslated for the reason the file already
gives; the *separator* does not, and that is what the Czech assertion is for. 3. One row in the API
table.
**Checks** T1 + the whole `./gradlew test`, because `service/` moved. **Depends** —

### C0P2 Nine routes, generated and registered · 25 · after C0P1

**Why** Every `create_screen.py` run writes `AppNavHost.kt` and `KoinGraphTest.kt`. Nine games in
three lanes would be nine edits to two shared files by three agents. Generating all nine routes once
is what makes lanes 1 and 2 disjoint, and it is a quarter of an hour.
**Done when** nine screen directories exist under `feature/arcade/presentation/`, each with its six
files and its two tests; each renders a placeholder naming the game and its task id and nothing
else; every route is in `AppNavHost.kt` and every route key is in `KoinGraphTest`'s
`injectedParameters`; `python3 scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug`
passes in one invocation.
**Touches** `feature/arcade/presentation/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`,
`feature/arcade/presentation/src/main/res/values*/strings.xml`.
**Read** `docs/ai/RECIPES.md` § another screen in an existing feature · `CLAUDE.md` § Known
constraints, on why the route key is listed by hand in `KoinGraphTest`.
**Steps** 1. `python3 scripts/create_screen.py arcade <Name>` for `ArcadeSection` (`--with-args
'sectionId:String'`) and the nine games. Each game takes `--with-args 'seed:Long'` — the daily run
(C3U2) needs a game to be reproducible from a number, and retrofitting a route argument later means
touching `KoinGraphTest` again from the wrong lane. 2. The placeholder is an `AppEmptyState` naming
the task, so a half-finished release is legible on a device rather than blank. 3. One commit, nine
directories: this is bookkeeping, and splitting it would put the shared files back in play.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + `python3 scripts/test_scripts.py`.
**Depends** C0P1

### C0P3 Records: the domain, the table and the repository · 25 · after C0P1

**Why** Nine games with nothing to beat is nine toys. A record is the only state the arcade keeps,
and every game in lanes 1 and 2 writes one — so it exists before they start or each invents its own.
**Done when** `ArcadeRepository` exposes `observeBest(gameId): Flow<Outcome<Record?>>`,
`observeAll(): Flow<Outcome<List<Record>>>` and `submit(gameId, score): Outcome<Boolean>` — the
boolean is *this beat the record*, which C2U5 and C2U6 both need and neither should recompute;
scores are compared by the game's own direction, so a lower time and a higher streak both win;
a `FakeArcadeRepository` is in `testFixtures` of `:feature:arcade:domain`, because ten screen tests
will want it; the Room schema is committed; `./gradlew test` passes.
**Touches** `feature/arcade/domain/**`, `feature/arcade/data/**`, `feature/arcade/data/schemas/**`,
`feature/arcade/data/build.gradle.kts`, `feature/arcade/di/**`,
`docs/ai/reference/DOMAIN.md`.
**Read** `feature/cart/data/build.gradle.kts` for the Room plugin pair ·
`feature/catalog/data/src/main/kotlin/.../repository/DefaultCatalogRepository.kt` ·
`build-logic/src/main/kotlin/AndroidRoomConventionPlugin.kt` · `CLAUDE.md` § Module structure.
**Steps** 1. `python3 scripts/create_datasource.py arcade LocalArcade --repository`. 2. One table:
game id, score, when. One row per game — a record, not a history; a history is a backlog line if
anyone wants a graph. 3. The direction (lower wins / higher wins) belongs on `Game` in `domain`,
beside the title, not in a `when` in the repository. 4. `docs/ai/reference/DOMAIN.md`.
**Checks** T1 + `python3 scripts/test_scripts.py`. **Depends** C0P1

### C0U2 How to play: one sheet, nine explanations · 12 · after C0P2

**Why** A game whose rules are not on screen is a puzzle about the game. Nine screens each inventing
their own explanation is nine layouts to keep in step, and it is exactly the case
`CLAUDE.md` § Screen structure means by "a composable a screen grows has one home".
**Done when** `feature/arcade/presentation/component/HowToPlaySheet.kt` exists with its
`@ComponentPreview`; it is an `AppSheet` whose body is `AppAccordion` sections — rules, scoring,
what it is showing off; every game screen opens it from one `AppIconButton` in its top bar; the copy
comes from each game's own `strings.xml` keys, in both locales; `python3 scripts/doctor.py` passes,
`check_screen_files_hold_only_the_screen` included.
**Touches** `feature/arcade/presentation/component/**`, every game screen's top bar,
`feature/arcade/presentation/src/main/res/values*/strings.xml`.
**Read** `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/{AppSheet,AppAccordion}.kt`
· `CLAUDE.md` § Design system, on `create_component.py --feature`.
**Steps** 1. `python3 scripts/create_component.py HowToPlay --feature arcade`. 2. The sheet takes a
`Game`, not nine booleans. 3. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2

### C0T1 Unused components earn their place (was F6) · 25 · after every game

**Why** D38 has held every unused component in `:core:ui` since the design system landed, on the
promise that a real screen would get the chance to use it. The games are that chance, and this is
the day the promise comes due. It cannot run earlier: the count is only true once lanes 1 and 2 have
merged.
**Decide first** nothing pre-assigned — the decision is per component and D38 is the standing rule.
Expect exactly one survivor to argue about: `AppImage` has no game, and it is `coil` 's only
consumer, so keeping it keeps a dependency. Recommended: **keep it, and say why in the commit** — an
Android template with no remote-image component is missing something every real app needs, which is
a different test from "a screen in this app uses it".
**Done when** the scan is re-run and recorded in the commit — for each `App*.kt`, whether anything
outside `feature/gallery` and `core/ui` composes it; every component with no home is deleted or has
one sentence saying why it stays; `GalleryCatalog` and the goldens agree with what is left;
`python3 scripts/doctor.py` passes; `./gradlew test verifyRoborazziDebug` passes in one invocation;
`docs/ai/reference/DESIGN-SYSTEM.md` matches; if anything was deleted, `docs/DECISIONS.md`'s D38 row
gains its outcome.
**Touches** `core/ui/**`, `feature/gallery/presentation/**`, `docs/ai/reference/DESIGN-SYSTEM.md`,
`docs/DECISIONS.md`.
**Read** the § What the games are for table above · `docs/DECISIONS.md` D38 ·
`feature/gallery/presentation/src/main/kotlin/.../GalleryCatalog.kt`.
**Steps** 1. Re-run the scan; do not trust the table above, which was written before the games were.
2. Delete rather than deprecate — this is a template, and a component nobody composes teaches
someone to compose it. 3. Re-record the goldens and **open what `record` wrote**.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test` + the whole `./gradlew test`.
**Depends** every task in lanes 1 and 2

### C0P4 Ship C · 6 · after every other task

**Why** `/release close`, which is `../PROCESS.md` § Ship.
**Done when** every board line in `docs/STATUS.md` is `[x]` or `[-]`; the `v1.2.0` block is in
`docs/CHANGELOG.md` in user words with its `Tasks:` line and its `Estimate · Actual · Ratio`; the
doc sweep in `../PROCESS.md` § Which doc changes when has been walked; `docs/README.md` § Work
points at the next plan; this file is in `docs/archive/plans/`.
**Touches** `docs/**`.
**Read** `../PROCESS.md` § Ship, and draft the next plan · `.claude/commands/release.md`.
**Steps** `/release close`.
**Checks** T1. **Depends** every other task

### C1U1 Set The Stepper · 12

**Why** `AppStepper` is composed by one screen in the app and `AppFieldGroup` and `AppFormField` by
none. A game that is nothing but "get three steppers to a target" drives all three properly, and it
is the smallest possible game — so it is the one to write first and copy from.
**Done when** three `AppStepper`s start at a default and a target is shown; the score is taps used
against the theoretical minimum, so it is deterministic and testable; `AppProgress` shows how close
the three are; the record is submitted through `ArcadeRepository.submit` and a new one is reported;
`SetTheStepperViewModelTest` asserts the score for a fixed target and a fixed sequence of taps, and
`SetTheStepperScreenTest` finds by `testTag` and asserts the event a tap emits; both locales.
**Touches** `feature/arcade/presentation/setthestepper/**`,
`feature/arcade/presentation/src/main/res/values*/strings.xml`.
**Read** `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/{AppStepper,AppFieldGroup,AppFormField,AppProgress}.kt`
· `feature/template/presentation/.../template/` for the shape · `CLAUDE.md` § Test identifiers.
**Steps** 1. The seed from the route key picks the target, so the daily run can reproduce it.
2. Ids are `setTheStepper_<element>` from the closed vocabulary. 3. `recordRoborazziDebug`, then
**open what it wrote**. 4. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C1U2 Slider Sniper · 12

**Why** `AppSlider` and `AppRangeSlider` are composed by nothing, and `AppTooltip` by nothing. Hit an
exact value on a slider with no numeric readout, with a tooltip as the one hint you are allowed.
**Done when** the slider shows no value; `AppStatusDot` reports hotter or colder on release with a
`TagTone` per band; `AppTooltip` gives the value once per round and the score records that it was
used; the round is decided by distance, so a fixed drag has a fixed score in the view-model test;
both tests; both locales.
**Touches** `feature/arcade/presentation/slidersniper/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppSlider,AppTooltip,AppStatusDot}.kt` · `AppTag.kt` for `TagTone`.
**Steps** 1. `AppRangeSlider` is the harder round — two handles, both on target — and is worth a
second difficulty rather than a second game. 2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C1U3 Switchboard · 12

**Why** `AppCheckbox` takes a tri-state `CheckState` that nothing in the app has ever set to its
third value, and `AppSelect` and `AppRadio` are composed by nothing. Reproduce a shown pattern of
checkboxes, radios and selects in as few moves as possible.
**Done when** a target pattern is shown and the board is set to match it; at least one round uses
`CheckState`'s indeterminate value as a target the player has to reach, which is the whole reason
this game exists; the score is moves against the minimum; both tests; both locales.
**Touches** `feature/arcade/presentation/switchboard/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppCheckbox,AppSelect,AppRadio}.kt` — `CheckState` is in
`AppCheckbox.kt`.
**Steps** 1. The pattern comes from the seed. 2. A move is one interaction, counted in the view
model, never in the composable. 3. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C1U4 Date Dash · 12

**Why** `AppDateField` is the component A0X3 fixed the locale of and `OverlayScreenshotTest` was
written for, and nothing composes it. `AppSheet` and `AppMenu` are composed by nothing. Reach a
target date in the fewest interactions.
**Done when** the target is shown in an `AppSheet`; `AppMenu` offers three hints, each costing
score; the date picker is reached and driven; the round is scored on interactions and elapsed time
from `Formats.stopwatch`; an `OverlayScreenshotTest` case covers the picker open, because a picker
draws in a window of its own and no preview sees it — `CLAUDE.md` § Testing a screen; both tests;
both locales.
**Touches** `feature/arcade/presentation/datedash/**`, the feature's `strings.xml`,
`core/ui/src/test/.../OverlayScreenshotTest.kt` *(lane 3's `core/ui/**` — agree it with lane 3 or
take the next task)*.
**Read** `core/ui/.../component/{AppDateField,AppSheet,AppMenu}.kt` ·
`core/ui/src/test/kotlin/.../screenshot/OverlayScreenshotTest.kt`.
**Steps** 1. Dates come from the seed and are always reachable. 2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3, C0U1

### C1U5 Tab Chase · 12

**Why** `AppTabs`, `AppBottomNav` and `AppNavRail` are three navigation components the app itself
never composes, because `:app` uses the adaptive suite instead. A prize hides behind one of several
tabs and moves each time you miss.
**Done when** the round is played on `AppTabs`, and the width class chooses `AppBottomNav` or
`AppNavRail` for the same round — one game, three components, and a real reason for the app to look
at `AppTheme.density`; the score is switches used; a `@ScreenPreview` covers each width; both tests;
both locales.
**Touches** `feature/arcade/presentation/tabchase/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppTabs,AppBottomNav,AppNavRail}.kt` ·
`core/ui/.../theme/Density.kt` for `SizeClass` · `core/ui/.../common/` for the preview annotations.
**Steps** 1. The width class decides the component, nothing else. 2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C1U6 The component playground · 25 · decides D57

**Why** The other half of what a design system's own app should offer, and the thing the request
that started this release actually named: not a game but a bench — pick a component, drive its
properties from real controls, watch it change. The gallery shows each component in a fixed state;
this is the same components with the knobs attached.
**Decide first** the playground is a screen of its own in the arcade, or it replaces the gallery's
detail screen → D57. Recommended: **a screen of its own**, and leave the gallery alone. The gallery
is reached from the debug menu and exists to be exhaustive; this is reached from the arcade and
exists to be played with. Merging them makes one screen that is bad at both, and B1U3 may be
rewriting the gallery's list from under you.
**Done when** at least eight components are drivable; the controls are themselves `AppSelect`,
`AppSlider`, `AppCheckbox` and `AppSegmented`, so the bench is built from the thing it is showing;
`AppAccordion` groups the properties, `AppScrollShadow` marks the long list's edges and `AppToolbar`
sits over the preview; the chosen state survives a rotation; both tests; both locales; D57 is a row
in `../../DECISIONS.md`.
**Touches** `feature/arcade/presentation/playground/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppAccordion,AppScrollShadow,AppToolbar,AppSegmented}.kt` ·
`feature/gallery/presentation/.../GalleryCatalog.kt` for what a catalogue entry costs ·
`CLAUDE.md` § Known constraints, on state that survives process death.
**Steps** 1. Write D57. 2. A component's knobs are data — a list of property descriptors — not a
`when` per component, or the ninth component is a rewrite. 3. `docs/ai/reference/FEATURES.md` and
`docs/ai/reference/DESIGN-SYSTEM.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2

### C2U1 Three Seconds · 12

**Why** The game the request named: press and hold, release at exactly three seconds, keep the
record. It is the smallest thing that needs `Formats.stopwatch`, and it gives `AppProgress`,
`AppStatusDot` and `AppToast` a home.
**Done when** press starts a clock and release stops it, with **no countdown shown** — that is the
game; the error is rendered by `Formats.stopwatch` and by an `AppProgress` bar that fills against
three seconds *after* the release, never during; `AppStatusDot` reports the band and `AppToast` a
new best; the clock is `TestDispatchers`-driven so `ThreeSecondsViewModelTest` can assert a 2 900 ms
hold without waiting 2.9 seconds; both tests; both locales.
**Touches** `feature/arcade/presentation/threeseconds/**`, the feature's `strings.xml`.
**Read** `service/core/domain/src/testFixtures/kotlin/.../TestDispatchers.kt` ·
`core/ui/.../component/{AppProgress,AppStatusDot,AppToast}.kt` ·
`service/core/ui/.../format/Formats.kt`.
**Steps** 1. The elapsed time comes from a monotonic clock behind an interface the test can drive,
never `System.currentTimeMillis()` in a composable. 2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3, C0U1

### C2U2 Reaction · 12

**Why** `AppSpinner` and `AppFab` are composed by nothing. Wait for the surface to turn, tap as fast
as you can, and a false start costs the round.
**Done when** the arming delay is random from the seed and the false start is detected and reported
with `AppToast`; `AppSpinner` shows the arming state and `AppFab` is the target; the reaction is in
milliseconds through `Formats.stopwatch`; five rounds make a run and the record is the median, so
one lucky tap is not a record; the clock is test-driven; both tests; both locales.
**Touches** `feature/arcade/presentation/reaction/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppSpinner,AppFab,AppStatusDot}.kt` · C2U1's clock, which this
shares — take it from `presentation/component/` rather than writing a second one.
**Steps** 1. The median, not the mean: one slow round should not erase four good ones, and one lucky
one should not make a record. 2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3, C0U1

### C2U3 Memory Match · 12

**Why** `AppSkeleton` exists to stand in for content that has not arrived, and nothing composes it.
A face-down card *is* content that has not arrived. `AppAvatar` generates a deterministic face from
a string, which is exactly a card face that needs no asset.
**Done when** a grid of `AppCard`s is face down as `AppSkeleton` and face up as `AppAvatar`, so the
game needs no drawable at all; the layout comes from the seed; `AppProgress` shows pairs found; the
score is moves and time; both tests, and the view-model test plays a whole board deterministically;
both locales.
**Touches** `feature/arcade/presentation/memorymatch/**`, the feature's `strings.xml`.
**Read** `core/ui/.../component/{AppSkeleton,AppAvatar,AppCard,AppProgress}.kt`.
**Steps** 1. The board is state, and flipping is an event — no animation state in the view model.
2. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C2U4 Name That Component · 25 · decides D56

**Why** The request's first idea, and the only game that is *about* the design system rather than
merely built from it: a component is rendered, four names are offered, pick the right one. It is
also the one that will find the components whose names do not match what they look like, which is
worth knowing.
**Decide first** the question bank reads the gallery's list, or keeps its own → D56. Read D52 first:
if B1U3 has landed and the gallery is generated from the previews, read the same source and there is
one list. If it has not, or if D52 kept a hand-written list in `:feature:gallery`, this feature must
not depend on another feature's `presentation` — `CLAUDE.md` § Module structure — so the bank is its
own list in `:feature:arcade:domain`, and D56 says which and why.
**Done when** at least twenty components are in the bank; a round renders one and offers four names
in an `AppRadioGroup`, three of them plausible rather than random — a `AppSlider` round should offer
`AppRangeSlider`; `AppBadge` carries the streak and `AppDialog` closes the round; a wrong answer says
what the right one is, because the game is also a way to learn the set; the score is correct answers
and streak; both tests; both locales; D56 is a row in `../../DECISIONS.md`.
**Touches** `feature/arcade/presentation/nameit/**`, `feature/arcade/domain/**`, the feature's
`strings.xml`.
**Read** `feature/gallery/presentation/src/main/kotlin/.../GalleryCatalog.kt` ·
`docs/DECISIONS.md` D52 if it exists · `CLAUDE.md` § Module structure, on cross-feature dependencies.
**Steps** 1. Write D56 after reading D52, not before. 2. The distractors are part of the bank data —
each entry names the components it is confusable with — not a random pick, which makes the game
trivial. 3. `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C2U5 A record that falls is felt, and can be shared · 12

**Why** `submit` already returns whether the record fell (C0P3) and nothing does anything with it.
Two platform seams the template does not demonstrate anywhere: haptics, and the system share sheet.
Neither needs a dependency.
**Done when** beating a record fires one `HapticFeedbackType.Confirm` and nothing else does;
a share action sends the score through the system share sheet as a new `UiCommand.Share`, interpreted
in `Screen()` like every other command — never an `Intent` in a feature; `UiCommandTest` covers it;
the sheet is reachable from every game's result and from the scoreboard; both locales.
**Touches** `feature/arcade/presentation/component/**`, every game's result,
`service/core/ui/src/main/kotlin/.../event/UiCommand.kt`,
`service/core/ui/src/main/kotlin/.../component/Screen.kt`,
`docs/ai/reference/{SERVICES,CORE}.md`.
**Read** `service/core/ui/src/main/kotlin/com/example/androidproject1/service/core/ui/event/UiCommand.kt`
· `service/core/ui/.../component/Screen.kt`, the only interpreter of a command.
**Steps** 1. `UiCommand.Share(text)` — plain data, like the rest. 2. Haptics belong to the
composable, not the view model: the view model says *a record fell*, the screen decides that is
worth a buzz. 3. `docs/ai/reference/SERVICES.md` and `CORE.md`.
**Checks** T1 + the whole `./gradlew test`, because `service/` moved. **Depends** C0P3

### C2U6 Achievements: a badge for what you have beaten · 12

**Why** Nine records are nine numbers. A handful of named achievements — *every game played*, *three
records in a day*, *a reaction under 250 ms* — is what makes the hub worth opening again, and it is
what `AppTag` and `AppBadge` are for.
**Done when** achievements are derived from the records rather than stored separately, so there is
one source of truth and no migration; the hub shows how many are earned; each is an `AppTag` with a
`TagTone` for earned and not; earning one raises a snackbar with its name; a view-model test covers
the derivation for a fixed set of records; both locales.
**Touches** `feature/arcade/domain/**`, `feature/arcade/presentation/arcadehub/**` *(lane 0's — agree
it with lane 0 or take the next task)*, `feature/arcade/presentation/component/**`.
**Read** `core/ui/.../component/{AppTag,AppBadge}.kt` · `feature/arcade/domain/` after C0P3.
**Steps** 1. Derived, not stored — an achievement that can be recomputed cannot go stale. 2. The
rules are data in `domain`, one per achievement. 3. `docs/ai/reference/DOMAIN.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P3

### C3U1 The scoreboard: every game, every best · 25

**Why** Nine games each showing their own best is nine places to look. The scoreboard is the screen
that makes the records feel like something, and it is the reason D54 chose a table over a value.
**Done when** one screen lists every game with its best, its date and whether it has ever been
played; it sorts by name, by best and by recency through `AppSegmented`; a game never played shows
`AppEmptyState`'s inline form rather than a zero, because a zero is a lie; `AppDescriptionList`
renders the per-game detail and `AppListItem` the rows; tapping a row opens that game; both tests;
both locales.
**Touches** `feature/arcade/presentation/scoreboard/**`, the feature's `strings.xml`,
`docs/ai/reference/FEATURES.md`.
**Read** `core/ui/.../component/{AppSegmented,AppDescriptionList,AppListItem,AppEmptyState}.kt` ·
`feature/arcade/domain/` after C0P3 · `feature/home/presentation/.../home/` for a list screen.
**Steps** 1. The sort is state, and the sorted list is derived in the view model — a composable does
not sort. 2. Dates through `LocalFormats`, never a `DateTimeFormatter`; `doctor.py` fails on one.
3. `docs/ai/reference/FEATURES.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C3U2 The daily run: one seed a day, and a streak · 25

**Why** The reason to open the app tomorrow. Every game already takes a seed (C0P2), so a daily run
is one number derived from the date, three games in a row, and one score — almost no new machinery,
and it is what makes the seed argument earn its place.
**Done when** the seed is derived from the local date, so every device plays the same three games
the same way on the same day, with no network and no clock skew to argue about; the run is three
games back to back with a single result at the end; a streak counts consecutive days and survives
process death and a reboot; missing a day breaks it and the screen says so plainly; a view-model
test covers the streak across a fixed sequence of dates, including a missed day and a device whose
clock went backwards; both tests; both locales.
**Touches** `feature/arcade/presentation/dailyrun/**`, `feature/arcade/domain/**`,
`feature/arcade/data/**`, the feature's `strings.xml`.
**Read** `feature/onboarding/data/` for a stored flag · `feature/arcade/domain/` after C0P3 ·
`service/core/data/src/main/kotlin/.../DataStoreProvider.kt`.
**Steps** 1. The date is a value the domain is given, never `LocalDate.now()` inside it — that is
what makes the streak testable. 2. A clock that goes backwards must not extend a streak; assert it.
3. `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P2, C0P3

### C3U3 The gallery names the game that uses each component · 12

**Why** The gallery lists 46 components and says nothing about where any of them is used, which is
the question anyone actually has. After this release most of them have an answer, and the answer is
a screen you can open.
**Done when** every gallery entry whose component a game composes shows the game's name and opens it;
the mapping is read from `:feature:arcade:domain` — the `Game` list already names the components it
exercises (C0P1), so there is no second list; `:feature:gallery:presentation` depends on
`:feature:arcade:domain` and **not** its presentation, which `check_cross_feature_presentation`
enforces; an entry with no game says so plainly, which is the shortlist C0T1 works from; both
locales.
**Touches** `feature/gallery/presentation/**`, `feature/gallery/di/**`,
`app/**/AppNavHost.kt` *(lane 0's — the cross-feature open is a lambda; agree it with lane 0)*,
`docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `feature/gallery/presentation/src/main/kotlin/.../GalleryCatalog.kt` ·
`scripts/doctor.py`'s `check_cross_feature_presentation` · `CLAUDE.md` § Module structure.
**Steps** 1. Read the mapping from the domain; never copy it. 2. Opening the game is a lambda wired
in `AppNavHost`, because a feature's presentation may not name another's. 3.
`docs/ai/reference/DESIGN-SYSTEM.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P1

### C3U4 A deep link opens a game · 12

**Why** The app has one deep link, to a product, and A1X5 and B2X1 are both about it. A second one
is what proves the first was not a special case — and a shared score (C2U5) that opens nothing is a
worse share than none.
**Done when** `<applicationId>://game/<id>` opens that game, and `<applicationId>://game/<id>?seed=`
opens it on that seed; a cold start applies it once, which is A1X5's rule and is where the first one
broke; an unknown id lands on the hub with a message rather than a blank screen; a Maestro flow
cold-starts on a game link; `python3 scripts/doctor.py` passes, `check_maestro_ids_exist` included.
**Touches** `app/src/main/AndroidManifest.xml`, `app/**/MainActivity.kt` *(lane 0's `app/**` — agree
it with lane 0 or take the next task)*, `.maestro/`, `docs/ai/reference/FEATURES.md`.
**Read** `app/src/main/kotlin/com/example/androidproject1/MainActivity.kt:105-130` ·
`app/src/main/AndroidManifest.xml` · `docs/DECISIONS.md` for A1X5's rule.
**Steps** 1. Beside the product filter, not instead of it. 2. The seed is optional and defaults to
today's, so a link shared without one still plays. 3. `docs/ai/reference/FEATURES.md`.
**Checks** T1. **Depends** C0P2

### C3U5 The hub remembers what you played last · 12

**Why** Nine games behind two sections is two taps to the one you are actually practising. This is
the smallest thing that makes the hub feel used rather than listed.
**Done when** the hub shows the last game played and its best at the top, with a resume that opens it
on a fresh seed; the value is stored, so it survives process death and a reinstall of the activity
but not of the app; nothing is shown before the first game rather than an empty slot; a view-model
test covers first run, after one game and after a game that was abandoned; both locales.
**Touches** `feature/arcade/presentation/arcadehub/**` *(lane 0's — agree it with lane 0 or take the
next task)*, `feature/arcade/data/**`, `feature/arcade/domain/**`.
**Read** `feature/onboarding/data/` for the stored-flag shape · `feature/arcade/` after C0P3.
**Steps** 1. Written when a game *finishes*, not when it opens — abandoning a game is not playing it.
2. `docs/ai/reference/{FEATURES,DOMAIN}.md`.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** C0P1, C0P3

## Not in this release

Moved to [../../BACKLOG.md](../../BACKLOG.md) rather than padding a lane:

- **A history, not just a record** · 12 — every run kept, and a sparkline per game. The table holds
  one row per game on purpose (D54); a history is a second table and a chart component that does not
  exist.
- **Sound** · 12 — a tick on the stepper and a buzz on a false start. It needs an asset pipeline and
  a settings switch to turn it off, and neither exists.
- **A leaderboard beyond this device** · 50 — needs a real API, which is D20.
- **Difficulty per game** · 25 — after the nine exist and someone has actually played them.
