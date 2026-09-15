# Sprint F1 · Dev menu and offline

Sprint: F1 · Dev menu and offline
Status: done 2026-09-15
When: 2026-09-15 09:00 → 2026-09-15 18:00
Goal: a tester reaches any screen in one tap, and an offline request stops looking like a failed one
Release: F
Agents: 1 · 36 points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D66–D68

The first sprint (D66, D67). Three things are wrong today and each is small. Nothing bounded a
release in time and nothing showed the board outside the repository — F1P1 is the process itself.
The debug menu's Tools section is three buttons and not one opens a screen, so a tester reaching
the editor's third step signs in, taps a tab, taps through a list and into a wizard — forty times
a day, which is the case a debug menu exists for (D16). And nothing observes connectivity: a request
that fails because the device is offline is indistinguishable from one the server refused, so the
user is told something went wrong when nothing did.

The two code tasks were ranked first in the grooming of 2026-09-13 (D62) and neither made release
E. They are disjoint — F1X1 writes `feature/devmenu/**` and `AppNavHost.kt`, F1H1 writes
`service/network/**`, `MainActivity.kt` and `:core:ui` — so a second agent could take one, but the
day does not need one. What this sprint leaves alone: the Maestro flows that tap fixture text and
the `ContentState` decision, both still first in [../../BACKLOG.md](../../BACKLOG.md) § Next; Field
report was deleted, not deferred.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

| File group | Task |
|---|---|
| `docs/ai/PROCESS.md`, `docs/ai/plans/{TEMPLATE,F,F1-*}.md`, `docs/{DECISIONS,BACKLOG,STATUS,README}.md`, `CLAUDE.md`, `.claude/commands/**`, `scripts/{board,test_scripts}.py`, `scripts/README.md` | F1P1 |
| `feature/devmenu/**`, `app/**/AppNavHost.kt`, `docs/ai/reference/FEATURES.md` | F1X1 |
| `service/network/**`, `app/**/MainActivity.kt`, `app/**/MainViewModel.kt`, `app/**/network/**`, `app/**/ApplicationModule.kt`, `core/ui/**`, `feature/gallery/**`, `docs/ai/reference/{SERVICES,CORE,DESIGN-SYSTEM}.md` | F1H1 |

`settings.gradle.kts`, `core/di/**`, `gradle/libs.versions.toml`, `build-logic/**` and
`.github/workflows/build.yml` are untouched: no task adds a module, a dependency or a job.

## Tasks

### F1P1 Sprints, releases and the board · 12 · decides D66, D67

**Why** The release machinery — ids, bands, lanes, the task loop, `/release` — worked and nothing
in it bounded a release in time: E ran until its backlog ran out, and the day's first question had
no file to answer it. The owner asked for scrum: a sprint backlog drafted from the product backlog,
one sprint at a time, a release that collects sprints and ships when the owner says, plans with a
name and a date, a backlog grouped by what it touches — and a board that can be read from a phone
or from a session on another machine, without the repository.
**Done when** `docs/ai/PROCESS.md` § Sprints and releases says what a sprint, a release and a draft
are and which command moves each; `grep -c '' docs/ai/PROCESS.md` ≤ 160; `plans/F.md` is a release
file and `plans/F1-dev-menu-and-offline.md` a sprint file, with `Sprint:`, `When:`, `Goal:` and
`Release:` lines; `docs/STATUS.md` has § Board, § Drafts, § Release F and § Shipped; every line in
`docs/BACKLOG.md` § Next carries a group; `.claude/commands/{sprint,release,board,task}.md` exist
and reference scripts that exist; `python3 scripts/board.py` prints one JSON document from these
files and `python3 scripts/test_scripts.py` covers it; the board artifact renders it (D67); D66 and
D67 are rows; `python3 scripts/doctor.py` passes.
**Touches** `docs/ai/PROCESS.md`, `docs/ai/plans/{TEMPLATE,F,F1-*}.md`, `docs/DECISIONS.md`,
`docs/BACKLOG.md`, `docs/STATUS.md`, `docs/README.md`, `CLAUDE.md`, `.claude/commands/**`,
`scripts/{board,test_scripts}.py`, `scripts/README.md`, `.github/pull_request_template.md`.
**Read** `docs/ai/PROCESS.md` · `scripts/doctor.py` § `check_docs_index`, `check_task_ids`.
**Steps** 1. The 12- and 25-point bands missed by more than 30 % on six and five of E's tasks —
rewrite the descriptions, never the numbers. 2. `PROCESS.md`: the sprint digit replaces the lane
digit; the id pattern does not change. 3. The release and sprint files, the template, the four
commands. 4. `board.py` — a parser, no network: the session that runs `/board` writes its output
to the artifact. 5. The artifact, once, with `db`; its URL in `docs/README.md`. 6. D66, D67.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F1X1 The dev menu jumps straight to a screen (was D1X2) · 12

**Why** Every destination is reachable, so a menu of orphans would be empty; what is missing is
*depth*. Reaching `TripWizard` or the Inventory editor's third step means signing in, tapping a
tab, tapping through a list and into a wizard, and a tester doing that forty times a day is what a
debug menu is for (D16). The pattern exists — `DevMenuDestination.kt` takes
`navigateToComponents: () -> Unit` and `AppNavHost.kt` § `debugEntries` wires it inside the
`DebugMenu.ENABLED` branch, so `prod` folds it away — but the Tools section is three `AppButton`s
(`DevMenuScreen.kt:110-134`) and not one opens a screen.
**Done when** the menu opens `TripWizard`, `TripsList`, `ProductSearch`, `SettingsPermissions`,
`Profile`, `GalleryDetail`, `InventoryDetail` and `InventoryEditor` directly, each by
`id: "devMenu_<name>Item"`; a destination that takes a route argument is jumped to with a named
fixture value, not a blank one; the list is one `List<DevMenuJump>` built in `AppNavHost`, not a
parameter per target — `grep -c 'navigateTo' feature/devmenu/presentation/src/main/kotlin/**/DevMenuDestination.kt`
is 1; `python3 scripts/doctor.py` passes; `./gradlew :feature:devmenu:presentation:test
verifyRoborazziDebug` passes in one invocation; the goldens under
`feature/devmenu/presentation/src/test/screenshots` are re-recorded and **opened before they are
committed**; `docs/ai/reference/FEATURES.md`'s `devmenu` row says the menu jumps.
**Touches** `feature/devmenu/presentation/**` — `devmenu/DevMenuScreen.kt`, `DevMenuEvent.kt`,
`DevMenuNavigation.kt`, `DevMenuDestination.kt`, both `res/values*/strings.xml`, the two tests,
the goldens — `app/**/AppNavHost.kt`, `docs/ai/reference/FEATURES.md`.
**Read** `feature/devmenu/presentation/.../devmenu/DevMenuDestination.kt` · `DevMenuScreen.kt:100-134`
· `DevMenuNavigation.kt` · `DevMenuEvent.kt` · `app/**/AppNavHost.kt` § `debugEntries` ·
`feature/catalog/presentation/.../productdetail/ProductDetailDestination.kt` *(a route with an
argument)* · `feature/inventory/presentation/.../inventoryeditor/InventoryEditorDestination.kt` ·
`CLAUDE.md` § Screen structure, § Test identifiers.
**Steps** 1. `DevMenuJump(id: String, label: UiText, navigate: () -> Unit)` in the devmenu
presentation module; `devMenuDestination(backStack, jumps: List<DevMenuJump>)` replaces
`navigateToComponents`, and the gallery becomes the first jump. `AppNavHost` builds the list: it is
the only place that knows every destination, and a feature's presentation may not name another's.
2. One `DevMenuNavigation.Jump(jump)` and one `DevMenuEvent.JumpClicked(jump)`, following
`Components` exactly; the state carries the list. 3. Render with `AppListItem` under an
`AppSectionHeader` — a growing list of targets is a list, not more buttons. **No component is
added**; `create_component.py` is not called. 4. A route with an argument gets a fixture value the
`dev` engine serves — the product and inventory ids the flows in `.maestro/` already use. 5. One
header string in both locales, `dev_menu_jumps`; the labels come from `AppNavHost` as `UiText`.
6. `DevMenuScreenTest` asserts a tap on `devMenu_tripWizardItem` emits `JumpClicked`;
`DevMenuViewModelTest` asserts the navigation it produces.
**Checks** T1 + `verifyRoborazziDebug` **in the same `./gradlew` invocation as `test`**.
**Depends** —

### F1H1 A connectivity banner (was a third of F16) · 12 · decides D68

**Why** Nothing observes connectivity. A request that fails offline surfaces as the same
`DomainError.Network` alert as one that failed, so the user reads "something went wrong" when the
answer is "you are offline" — and the retry policy in `HttpClientFactory` spends its attempts on a
device that has no route at all.
**Decide first** `the monitor's interface in service/network, its Android implementation in
:app`, or `service/network becomes an Android library` → D67. The recommended reading keeps
`service/network` the Kotlin/JVM module it is — the same seam `CachedOkHttpEngine` already uses:
the port is in the service, the platform half is in `:app`'s `network/`, bound in
`ApplicationModule`. `MockEngine` on `dev` needs a fake, which is the `OfflineSwitch` the debug menu
already has — one source of truth for "the server is down" on that flavor.
**Done when** `service/network` has `interface ConnectivityMonitor { val online: StateFlow<Boolean> }`
with no `android.*` import — `grep -rn 'import android' service/network/src/main` is empty;
`app/**/network/AndroidConnectivityMonitor.kt` implements it over `ConnectivityManager`'s
default-network callback, and a Robolectric test in `app/src/test/.../network/` flips it;
`AppBanner` exists in `:core:ui` with a `@ComponentPreview` and a gallery entry —
`python3 scripts/doctor.py` passes; `MainActivity` draws it above `AppNavHost` while `online` is
false, id `"main_offlineBanner"`, and `MainViewModel` exposes the state; on `dev` the banner
follows `OfflineSwitch`; `HttpClientFactory`'s retry does not retry while offline, with a case in
`HttpClientRetryTest`; `docs/ai/reference/SERVICES.md`, `CORE.md` and `DESIGN-SYSTEM.md` say so.
**Touches** `service/network/src/{main,test}/**`, `app/**/network/**`,
`app/**/ApplicationModule.kt`, `app/**/MainActivity.kt`, `app/**/MainViewModel.kt`, `core/ui/**`
(one component), `feature/gallery/**` (its entry), `docs/ai/reference/{SERVICES,CORE,DESIGN-SYSTEM}.md`,
`docs/DECISIONS.md`.
**Read** `service/network/.../HttpClientFactory.kt` · `service/network/.../NetworkConfig.kt` ·
`app/**/network/CachedOkHttpEngine.kt` and its test *(the seam to copy)* ·
`app/**/ApplicationModule.kt:20-60` · `app/**/MainActivity.kt:57-100` ·
`feature/devmenu/presentation/.../OfflineSwitch.kt` · `core/ui/.../component/AppToast.kt` *(the
nearest component; a banner is persistent and has no action, so it is not this — D63)* ·
`docs/ai/reference/SERVICES.md` § `:service:network`.
**Steps** 1. D68 as a row. 2. The interface and a `StateFlow` in `service/network`; the retry
policy takes it as an optional parameter and gives up at once when `online` is false. 3.
`AndroidConnectivityMonitor` in `app/**/network/`, registered with `ConnectivityManager.registerDefaultNetworkCallback`
on first collection and unregistered on last — `callbackFlow` + `stateIn` — bound in
`ApplicationModule` next to the engine; the `dev` binding wraps `OfflineSwitch`. 4.
`python3 scripts/create_component.py Banner`; the component is a full-width surface in the
`warning` role with one line of text, and nothing else. 5. `MainViewModel.online: StateFlow<Boolean>`;
`MainActivity` composes `AppBanner` above the display inside `AppTheme`, so the splash is not
affected. 6. Tests: the Robolectric flip, the retry case, the component's preview golden — opened
before it is committed. 7. The three reference rows.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation *(`core/ui` moved)* + the whole
`./gradlew test` *(`service/` moved)*. **Depends** —

## Retrospective

- **What the briefs got wrong.** Two criteria could not be met as written: F1H1's id
  `main_offlineBanner` fails the closed vocabulary D60 had already decided, and F1X1's
  `grep -c 'navigateTo'` counted a parameter the task removes. Neither brief listed the file its
  change forces — `KoinGraphTest` for a `parametersOf`, `Koin.kt` for a new client parameter — and
  F1H1 pre-assigned D67, a number F1P1 had taken. A brief is written against `DECISIONS.md` and
  `doctor.py --list`, not from memory of them.
- **What the checks missed.** CI has refused every run since 2026-09-13 on account billing, and
  `gh pr checks` printed the same `fail` a red build does, so both pull requests merged on a run
  that never started. `create_component.py` wrote a gallery entry with no import and a call with no
  argument, which only the module's compile caught; ktlint's argument-wrapping rule caught the
  hand-written entry after it.
- **One thing to change.** `/task` reads the check run's annotation and says *not started* out
  loud — a line in § DevOps — and the generators are trusted to compile what they write only
  once `test_scripts.py --with-gradle` says so.
