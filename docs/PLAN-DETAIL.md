# Plan 4 · detail

The long half of [PLAN.md](PLAN.md). The board says what is open and in what state; this file
says what each open item is for, what "done" means, how it is verified, how to work the plan,
and why the decisions that needed a reason were made. When an item lands, its section here is
deleted — the commit holds the story — and its line on the board becomes `[x] (date)`. Nothing
in this file describes finished work.

## How to work this plan

**Worktrees.** One per item, on a branch named after it:

```bash
git worktree add ../<repo>-core.7 -b core.7-room-migrations
```

Rebase on `main` before the PR. PR title is `<id> <title>`; rebase-merge, so `main` stays one
commit per item. Branch protection is not available (D25), so the rule holds by convention.

`local.properties` is gitignored, so a fresh worktree has no SDK path and every Gradle task fails
with "SDK location not found" before it compiles anything. Copy it in first:

```bash
cp ../<repo>/local.properties .
```

**Item lifecycle.** On the board: `[ ]` → `[~]` the moment a branch exists for it (name the branch
on the line, and add the id to *Start now*) → `[x] (date)` when the gate is green and the PR is
merged. Then: refresh the track row and the total, one `█` per 10 %; delete the item's section
from this file. `[-]` drops an item with one line saying why. A new item takes the next number in
its track and gets a line on the board and a section here. If what shipped differs from the Done
line and the item is still open, one **Landed:** sentence in its section — never a paragraph.

**Shared files.** Edited by more than one track, always additively — a new line, never a
rewrite. On a conflict keep both sides and run `doctor.py`; it checks every one of them.

| File | Who appends |
|---|---|
| `settings.gradle.kts`, `core/di/build.gradle.kts`, `core/di/Koin.kt`, `app/AppNavHost.kt`, `app/KoinGraphTest.kt`, the module tree in `CLAUDE.md` | any item that adds a feature or screen — the generators do it |
| `gradle/libs.versions.toml` | any item that adds a library |
| `build-logic/` | core track; `qa.12` edits `ProjectConfig`, `core.7` edits the Room plugin |
| every `feature/*/presentation` | app track, except while `qa.13` is open — it moves every package, and nothing else touches a presentation module until it lands |
| `core/ui/component/` | ui track; another track adds a component only through `create_component.py` and says so in its Done line |
| `.github/workflows/build.yml` | quality track |
| `docs/PLAN.md`, this file | every item, its own line and section, and the dashboard |

An item that must edit a file its track does not own says so in its Done line; nobody else has an
open item on that file at the same time.

**The gate,** in the order CI runs it:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew ktlintCheck && ./gradlew build
```

Iterate on the module task (`./gradlew :feature:x:presentation:testDebugUnitTest`); run the gate
once, before the commit. When an item changes coverage noticeably, `./gradlew koverXmlReport` and
write the new number and the commit on the board.

**Environment,** checked 2026-09-09 on this machine:

| | |
|---|---|
| `adb` | `~/Library/Android/sdk/platform-tools/adb`, not on `PATH` |
| AVDs | `medium_phone_1` and `medium_tablet_1` (API 37, created by `ui.4` and never started — worker 4 has the phone) |
| Maestro CLI | `$(brew --prefix maestro)/bin/maestro`, 2.10.0. `brew install maestro` gives the desktop app with no `maestro test`; the CLI is `brew install mobile-dev-inc/tap/maestro` |
| `gh` | `/opt/homebrew/bin/gh` |
| Design source | the KSD project in Claude Design. `DesignSync` needs `/design-login` once from an interactive session; until then the Chrome route in the project memory reads the files |

**Scope rules that stand.** No new scripts; an existing one changes only when something else
forces it, as part of that item. A `service/` module never references `:core:*`, `:feature:*` or
`:app`. A feature composes from `:core:ui` and never draws. Do not build a data layer against
invented data — that is what Q8 is for. A `plugin` or `alpha` item starts with a compatibility
check and gets 30 minutes to a green spike before it is parked.

## Decisions — the reasoning

Only the ones that need it. The outcomes are on the board.

- **D13 Brand face.** Previews and goldens need a face that renders without network, and a POS
  device may have no Play Services. One variable font (`SourceSans3[wght].ttf`, 233 kB in the
  APK) covers every weight; the `wght` axis is set per weight because a device that cannot apply
  variations synthesises it and 600 and 700 come out identical. The OFL sits in `licenses/`.
- **D14 Roborazzi over Compose Preview Screenshot Testing.** Runs under the Robolectric that
  already works here, no alpha plugin, and scans the previews that exist instead of duplicating
  them. Google's tool has been `0.0.1-alpha` for two years.
- **D16 Gallery out of prod.** A component catalogue is for the people building the app. R8 drops
  the module once nothing in `prod` references it, which is what the debug menu's source-set
  split gives.
- **D20 Fixtures on `dev`.** No server to keep alive and CI runs unchanged. The cost is that
  "offline" is a fixture told to fail, not a network drop — `feat.5`'s Verify said so rather than
  pretending otherwise, and Q8 is where that cost is paid down.
- **D22 No licence.** All rights reserved. Reconcile with D19 the day the repo is published: a
  template nobody may legally fork is a template in name only.
- **D25 No branch protection.** The repo is private on the free plan, so required checks cost
  money. CI still reports on every PR; nothing enforces it. Raise it again if that ever bites.
- **D26 Renovate parked.** `renovate.json` is finished work and stays; only the GitHub app is
  missing. Dependency churn is most expensive while several worktrees are open, so it waits for
  the plan to thin out. Consider `"dependencyDashboardApproval": true` on the first run.
- **D28 Vendor policy applied.** Koin stays (23 files deep; a Hilt migration is a plan of its
  own), Coil stays (no first-party image loader exists), Roborazzi (D14) and Maestro (D15) are
  build- and test-only. mockk went — one usage, replaced by a Robolectric test against the real
  resource table; `dependency-analysis` went — advisory, one maintainer, pinned behind this AGP.
- **D29 Theme by hand.** No Style Dictionary and no Node in this repo. The accepted cost is
  drift: a KSD token change is re-ported by hand and nothing detects it. Promote the generator
  from the backlog the first time that actually hurts. `ui.7` is the first measurement of it.
- **D30 Maestro weekly, not per PR.** An emulator runner adds minutes to every run and the flows
  cover golden paths, not each change. The schedule finds a broken path within a week; a
  `workflow_dispatch` runs it on demand before a release.
- **D31 Version from the tag.** A tag is the one input a release cannot lack, and the commit
  count is reproducible on a laptop, unlike a CI run number. Local and PR builds keep 1 / 1.0 so
  the version never changes underneath a developer.
- **D32 No store upload.** A Play upload needs a service account and a signed listing, both
  outside a template. A GitHub release with the APK and generated notes is what a tag produces;
  the upload step is one action away when a project wants it.
- **D33 Material icons stay.** The design names Lucide on a 24 grid with a 1.9 stroke. Lucide has
  no first-party Compose artifact, so D27 keeps Material; what the code was actually missing is
  the three sizes as roles, and those are `ui.8` regardless of the set. Revisit if a project
  brings its own SVG pipeline, which the design's icon chapter assumes.
- **D34 A directory per screen, a file per component.** Decided 2026-09-09. A feature's
  presentation package was flat: the catalog's four screens, their states, events and ViewModels
  sat in one directory of 24 files, and a screen file carried whatever private composables it
  grew. Now every screen's eight-file unit lives in a sub-package named after the screen — a
  single-screen feature gets its directory too, so every feature reads the same and a second
  screen never forces a move — and every composable that is not a screen lives in the feature's
  `component/`, one file each, with a `@ComponentPreview`. One `component/` per feature rather
  than one per screen: a screen-private component and a feature-shared one would otherwise need
  two homes, and the day one is used from a second screen it would have to move. A component two
  features need still goes to `:core:ui` through `create_component.py`. Test ids do not change,
  so the Maestro flows do not either.

## Items

### Track core · the reusable architecture

Owns `service/`, `core/di`, `build-logic/`. Every item here is API the app track then uses;
nothing here knows a feature.


### Track ui · design system and adaptive

Owns `core/ui` and `feature/gallery`. `ui.5` also edits `app/AppNavHost.kt` and the catalog
destinations; no app-track item touches those while it is open.

**ui.2 Screenshot tests with Roborazzi** · M · `plugin` D14 · started, parked on `ui.2-roborazzi`
Why: 42 components × three variants and thirteen screens sit unasserted; a padding change is
found by eye or not at all.
Done: Roborazzi 1.74.0 and `ComposablePreviewScanner` 0.9.3 applied through
`convention.android.library.compose`; the scanner records every `@ComponentPreview` and
`@ScreenPreview` without duplicating them; goldens committed; `verifyRoborazziDebug` in the CI
build job.
Verify: break one padding value on purpose and the verify task fails on that image only; restore.
**Landed so far:** the toolchain half passes, re-checked on the D34 layout on 2026-09-09 — both
dependencies resolve and the plugin applies on AGP 9.4 / Gradle 9.6 / JDK 25.
**The `private` diagnosis was wrong.** `ComposablePreviewScanner` 0.9.3 already calls ClassGraph's
`ignoreMethodVisibility()` and `setAccessible(true)`; what the chain was missing is one call.
Adding `.includePrivatePreviews()` after `scanPackageTrees(...)` returns all 126 of `:core:ui`'s
previews — 42 components × light, dark and 1.5× — and the parameterised runner then produces a
test per golden. So **nothing becomes `internal`, and this item never touches `core/ui`'s
previews**; the wiring on `ui.2-roborazzi` applies unchanged on the new layout.
What is left is a decision the Done line does not settle: **where the test lives.** A module's
test only scans its own classpath, so `:core:ui`'s copy covers the 42 components and not one
screen. Either a copy per `presentation` module — cloned from `feature/template`, so every
generated feature gets one — or a single copy in `:app`, which sees every module through
`:core:di` but runs three times over the flavors. Then ~300 goldens to commit, and one
`verifyRoborazziDebug` step in the CI build job.

**ui.3 Component behaviour tests** · M · `stable`
Why: `core/ui/component` is 1,709 lines at 22 %. Previews show; nothing asserts, and `ui.6` was
the first time anyone noticed a component could not be tried.
Done: the test dependencies are already in `convention.android.library.compose`. Tests for the
interactive components — `AppButton` (loading keeps width, disabled emits nothing), `AppTextField`
(an error always carries text), `AppCheckbox` (indeterminate), `AppSelect`, `AppTabs`,
`AppStepper` (floor and ceiling), `AppSheet`, `AppDialog`, `AppSwitch`, `AppSegmented`.
Verify: the package leaves 22 % in the Kover report; each test finds by `testTag`, never by text.

**ui.5 List–detail for the catalog on wide screens** · M · `stable` · needs ui.4
Why: the source system runs on tablets, and the catalog is exactly a list–detail shape.
Done: `adaptive-navigation3` 1.3.0 (stable; check its Navigation 3 range against 1.1.7); a
`ListDetailSceneStrategy` on the `NavDisplay`; products and product detail carry the metadata;
phones unchanged.
Verify: the emulator's tablet profile shows both panes, the phone profile one; process death four
screens deep restores on both.

**ui.7 Components match the design's component document** · M · `stable` · needs qa.13
Why: the components were ported from the KSD system's `03-Komponenty` by hand and nothing had
compared them since. The document was read on 2026-09-09 through the Chrome route in the project
memory; the first pass below is what it found. `DesignSync` after `/design-login` is the tidier
route for the next read. Waits for `qa.13` because it edits `GalleryCatalog.kt`, which moves.
Two things the design says in two ways, and the side the code took: its breakpoints are
720 / 1280 dp in the *Prostor a velikosti* table and 600 / 1000 in *Hustota* — the code follows
the first; its element ids are `orderDetail_payButton` in document 02 and
`ksd.pos.payment.preview.btn.toTender` in document 05 — the code and every Maestro flow follow
02. Both are worth a line in the design source, see *Notes for the design source* below.
Done: every "gap" row below fixed or turned into an accepted difference with its reason written
here; `GalleryCatalog` gains a variant for every state the document names, so the gallery becomes
the running copy of the document; `AppDialog`, `AppSheet` and `AppMenu` join the gallery.
Verify: no row below still says *gap*; every accepted difference has a reason; the gallery lists
every component in `core/ui/component`.

The document has two halves. Layer 3 **primitives** (A–G, 41 of them) are what the code ports.
The **composites** (A–L, some sixty: keypads, order lines, payment, floor plan, stock, shift,
reports, drawer, print) are the POS product's own and stay out of a template by D19 — a project
built on this template adds the ones it needs with `create_component.py`.

| Document | Code | State |
|---|---|---|
| Text, Money · NumericValue | `AppText` roles | matches |
| Avatar sm / md / lg | `AppAvatar(size)` | matches; **gap:** the "with status" variant |
| StatusDot, Tag, Badge | `AppStatusDot`, `AppTag`, `AppBadge` | matches |
| Kbd | — | accepted difference: a keyboard hint is desktop-only |
| TextField sm 40 / md 48 / lg 56 | `AppTextField`, one size | **gap:** sizes |
| NumberField with a unit suffix | `AppTextField(numeric = true)` | **gap:** the suffix |
| SearchField, Select | `AppSearchField`, `AppSelect` | matches |
| DateField · TimeField | — | **gap:** a native picker behind the field tokens |
| SignaturePad | — | product-specific |
| Button, six kinds × three sizes, loading keeps width | `AppButton` | matches |
| IconButton sm / md / lg, confirm / destructive, square | `AppIconButton`, one size, one kind | **gap:** sizes, kinds, the square shape |
| KeyCap | `Modifier.keySurface` only | product-specific; the travel effect is already the modifier |
| Switch sm / md | `AppSwitch`, one size | **gap:** the small size |
| Checkbox sm / md, mixed, error, disabled | `AppCheckbox`, one size, no error | **gap:** the error state and the small size |
| Radio, Segmented sm / md, Stepper | `AppRadio`, `AppSegmented` (one size), `AppStepper` | matches; **gap:** segmented sizes |
| Slider · RangeSlider | `AppSlider` | **gap:** the range variant |
| PinDots | — | product-specific |
| Progress determinate / indeterminate / steps | `AppProgress(fraction)` | **gap:** indeterminate and stepped |
| Spinner, Skeleton, Toast, Tooltip, EmptyState | the five `App*` | matches |
| Surface at three levels | `AppCard`, one level | **gap:** sunken and raised-panel levels as one component |
| Divider horizontal / strong / labelled / vertical | `AppDivider()` | **gap:** the other three |
| ListRow plain / icon / selected / pressed | `AppListItem` | **gap:** a selected state |
| Tabs, Accordion, Menu | `AppTabs`, `AppAccordion`, `AppMenu` | matches; Menu is not in the gallery |
| Breadcrumb · Pagination | — | accepted difference: administration only, no pagination on a phone |
| ScrollShadow | — | **gap:** the one cue that a list continues on touch |
| QrCode | — | product-specific until a screen shows one |
| FormField, DescriptionList, SectionHeader | the three `App*` | matches |
| FieldGroup | — | **gap:** fields sharing one frame |
| SwipeAction, LongPressHint | — | product-specific; revisit if a sample list needs a swipe |
| NoDataCell | `DescriptionRow(value = null)` draws the dash | matches |
| TopBar, NavRail · BottomNav, Toolbar, BottomActionBar | the four `App*` | matches |
| Sheet with peek / half / full | `AppSheet` | matches the default; sizes are an accepted difference; not in the gallery |
| Dialog, ConfirmDialog | `AppDialog`, `AppConfirmDialog` | matches; not in the gallery |
| ScreenEmpty, ScreenError | `ContentState.Empty` / `.Error` in `:service:core:ui` | matches |
| ThemeToggle, OnboardingStep | `shell.3`, `shell.5` | scheduled elsewhere |
| SyncStatusBar · OfflineBanner | a snackbar in `feat.5` | **gap:** the document wants a bar that never covers content |
| Popover, ActionSheet, FullScreenModal, FilterBar, SideDrawer, SettingsList, ProfileMenu, CommandPalette, ScreenOffline, UpdateBanner, DeviceStatusStrip, FirstRunChecklist and the B–H composites | — | product-specific; a project adds what it needs |


### Track app · shell and sample features

Owns `app/` and `feature/*` except `gallery` and `template`. `shell.*` is the app shell; `feat.*`
is one feature per item, each proving one capability the architecture has and no sample uses.
Each feature is a worktree of its own — they meet only in the registration files.

**shell.1 Deep links** · M · `device`
Why: getting the back stack right on a cold-start deep link is the part people get wrong.
Done: a `VIEW` intent filter on `MainActivity` for `<app>://product/{id}`, the scheme named after
the app; a `DeepLinks.kt` in `:app` parsing a URI into a `NavKey`; a cold start builds Home →
Categories → Products → Detail so Up walks back; a warm start pushes onto the current tab.
Verify: `adb shell am start -d <app>://product/croissant` cold and warm, both land on the
product with Up working; a `MainViewModelTest` case for the synthesised stack.

**shell.2 Debug menu, dev and staging only** · M · `stable` D16
Why: flavor, base URL, session and "crash now" are what a tester needs, the offline toggle is a
file nobody should have to `touch` by hand, and the gallery has no business in a prod build.
Done: `:feature:devmenu` (presentation, di) reached from Settings when a `prod` source set's
`DebugMenu.enabled` is false and the others' is true, so R8 strips it; shows build info,
`BASE_URL`, session, an `ErrorTracker` test crash, LeakCanary, and a switch for `feat.5`'s
`fail_network` toggle; Components moves here from Settings.
Verify: the `prodRelease` mapping file contains no `feature.gallery` or `feature.devmenu` class;
a `SettingsScreenTest` case for the entry present and absent.

**shell.3 Theme setting** · M · `stable`
Why: light / dark / system is the first preference every app grows, and no sample shows a
preference read at the root.
Done: `:feature:settings` gains `domain` and `data` (`--layers domain,data --force`) with a
`ThemePreference` in Preferences DataStore; a segmented control on Settings; `MainActivity`
applies it through `AppTheme(darkTheme = …)`.
Verify: `SettingsViewModelTest` and a screen test; the choice survives a restart on the emulator.

**shell.4 Notification tap-through** · S · `device` · needs shell.1, shell.2
Why: the channel exists and nothing posts to it.
Done: the debug menu posts a notification whose `PendingIntent` carries a product deep link.
Verify: tapping it from a cold start lands on the product with Up working.

**shell.5 Onboarding flow** · M · `stable`
Why: a third flow beside auth and main, gated by a stored flag, is the shape of every first-run
screen and the one flow switch the template does not show.
Done: `:feature:onboarding` full stack with a `seen` flag in DataStore; `MainViewModel` combines
it with the session into `Unknown / Onboarding / SignedOut / SignedIn`; the splash holds through
`Unknown`; three pages on an `AppPager` added to `:core:ui` with `create_component.py` (one new
file in the ui track's directory).
Verify: `MainViewModelTest` for all four states; the first cold start shows onboarding then
Login, the second skips it.

**feat.4 Search — proves inline error per content id** · M · `stable`
Why: inline retry works per content id and no screen has two content states.
Done: a search screen from the Categories top bar; `AppSearchField` with a 300 ms debounce and
`flatMapLatest`; results and recent searches as two content ids, each with its own inline error
and empty state; recents in DataStore.
Verify: a ViewModel test with `advanceTimeBy` for the debounce and one for a failure on one id
leaving the other; a screen test.

**feat.9 Czech alongside English** · M · `stable` D24 · needs feat.4, feat.8
Why: Czech has four plural forms against English's two, so a second locale is what proves
`toPluralUiText` rather than decorating it. Last in the track on purpose: translating strings for
screens not yet written is waste.
Done: `values-cs/strings.xml` in every `presentation` module, `:core:ui` and `:service:core:ui`
(whose `core_*` strings ship to consumers); hand-written, no pipeline; `%d` and `%s` positions
preserved.
Verify: every `<string>` and `<plurals>` name in `values/` has a `values-cs/` counterpart; the
cart's "N items" reads correctly at 1, 2 and 5 under `cs`; no screen clips at Czech's longer words.

### Track quality · tests, CI, release

Owns `.github/`, `.maestro/`, `scripts/`, `feature/template`, `docs/`, `baselineprofile/`.

**qa.5 Hardware pass** · M · `device` D21 · needs shell.1
Why: the Keystore path, the startup benchmark and predictive back have never run outside an
emulator, and the emulator ANRs.
Done: on a physical device — session round trip through the real Keystore; `StartupBenchmark`
with and without the profile, numbers recorded here; predictive back on every screen; "Don't keep
activities" four screens deep; a cold deep link; TalkBack through Login and Catalog.
Verify: the numbers, and one line per check here.

**qa.11 Maestro flows in CI** · M · `plugin` D30
Why: five flows pass on a laptop and run nowhere else, and the log-out flow already caught a bug
no unit test could — a dialog whose ids were invisible to anything driving the device.
Done: a `maestro` job in `build.yml` on the weekly schedule and `workflow_dispatch`, on
`reactivecircus/android-emulator-runner` with an API 35 image, installing `devDebug` and running
`maestro test .maestro`; the Maestro CLI from the vendor's install script; recordings and logs
uploaded on failure. The action is third-party and build-only; D27 applies and it earns its line
on the board through D30.
Verify: the job passes on the schedule; rename one tag on purpose and the dispatch run fails on
that flow.


## Backlog

Parked, not scheduled. Promote by moving onto the board with the next number in a track.

- **Behind Q8, once an API exists:** certificate pinning; response caching with ETags; real
  token issuance and the refresh path against a server; WorkManager sync and Paging 3 for a list
  longer than a page.
- Renovate (D26) — the config is written; installing the app is the whole item. Promote when a
  bump landing on `main` no longer rebases several worktrees.
- Theme generated from KSD tokens — parked by D29; promote the first time `ui.7` finds drift that
  had to be re-ported by hand.
- ViewModel-readable permission state — build inside the first feature that needs it.
- Module graph rendered and layer rules asserted at build time, replacing `doctor.py`'s greps
  for the layer direction.
- `doctor.py --fix` — only when a check's fix is mechanical.
- Feature-owned nav graphs — the `--graph` grouping in `AppNavHost` does the job today.
- Logger backend and remote config.
- Localisation pipeline and a per-app language picker — parked by D24.
- Baseline profile regenerated on release tags.
- detekt, when 2.x is stable. Compose Preview Screenshot Testing, if D14 fails and it leaves alpha.
- `explicitApi()` on the `service/` modules.
- Play upload (D32) — one action away when a project has a Play Console service account.
- An `Ids` object generated from one registry, the design's document 02 — `qa.14`'s grep is the
  cheap half; the registry earns its place when a second platform or a design tool reads it.
- Sound and haptic feedback roles (`feedback.tap`, `.scan`, `.confirm`, `.reject`, `.weigh`) —
  the design defines five; a template has nothing to attach them to until a keypad exists.
- Keyboard shortcuts as a property of a screen (F2 pay, F3 search) — desktop and till only.
- Layer roles for z-order and the one-overlay-at-a-time rule — Compose handles the stacking;
  the rule would be a `doctor.py` check the day a second overlay appears.

## Notes for the design source

Things the code found in the KSD documents that belong back in Claude Design, not in this repo.
Whoever next edits the project there should take them; nothing here blocks an item.

- Document 06 lists Stage I infrastructure at 0 of 9 and names it as the step before coding. This
  repo did a Compose-only version of it by hand — the token layers, the id grammar, the state and
  preview conventions, the smoke flows — under D29. Record that, or the same work gets planned
  twice.
- Two breakpoint tables disagree (720 / 1280 versus 600 / 1000 dp). The code uses the first.
- Two id grammars disagree between documents 02 and 05. The code and the flows use 02's.
- Two dark greys were a step short. `textSecondary` on a raised surface measured 4.17:1 and
  `borderStrong` 2.54:1, because the three dark surfaces sit far closer together in luminance than
  the three light ones, so a role mirrored step for step from light lands under the threshold on a
  card. The code moved both one step lighter (`ui.9`); the source's dark palette wants the same.
- Small slips: four elevation levels announced and five tabled; a checkbox radius of 6 that no
  shape role has; "four variants" of a button that lists six; a `primary` role in the token
  example that the role table never defines.

## What Plan 3 taught

Sixteen items landed in a day, counting the coverage fix. Where the time went, and the rule
each cost bought:

| Cost | Rule in this plan |
|---|---|
| The dashboard said 38 % coverage while the real number was 55 %, and the task that produces it had been failing on `:baselineprofile` without anyone noticing | The coverage line carries the commit it was measured at; an item that moves it re-measures. A report task that fails is a red gate, not a stale number |
| Every landed item grew a "Landed" paragraph again; the plan gained 60 % in a day and every session paid to read it | The board is one line per item; the detail file describes only open work and deletes an item when it lands |
| Two WIP branches nobody had claimed cost the next session a read each to decide whether to keep them | An item is `[~]` with its branch named the moment the branch exists |
| The gallery showed 42 components and nobody had tapped one; the first person who did found they could not | A demo is a use, not a picture (`ui.6`); the components are compared to the document they came from (`ui.7`) |
| Five tests failed under the pre-commit hook and passed by hand, because git's hook environment leaked into the scripts' subprocesses | `test_scripts.py` strips `GIT_*` from every subprocess; a hook is tested through the hook |
| Alpha and plugin spikes ate the morning in Plan 2; in Plan 3 every one had a tag and a 30-minute limit, and `ui.2` was parked at the limit with its blocker written down | Keep the tags and the limit; a parked item names its blocker and its next step |

## What Plan 3 landed

Fifteen items, one commit each, named `<id> <title>`; `git log --oneline` finds them.

| Id | Item |
|---|---|
| core.1 | `:service:network` — Ktor client, error mapping, single-flight token refresh |
| core.2 | Offline-first combinator |
| core.3 | Lifecycle-aware `observe` |
| core.4 | Navigation results |
| core.5 | Form validation |
| ui.1 | Bundle the brand face |
| feat.1 | Favourites — proves Room |
| feat.2 | Cart — proves cross-feature domain, tab badge, plurals, nav results |
| feat.3 | Profile — proves PermissionGate and forms |
| feat.5 | Catalog over the network |
| feat.6 | Session carries an id; `setUser` wired |
| feat.7 | Tests for the data layers that exist |
| qa.2 | Maestro golden-path flows |
| qa.3 | The template ships a screen test |
| qa.10 | Retire mockk and `dependency-analysis` |

Dropped in Plan 3: qa.1 LICENSE (D22), qa.6 Renovate (D26, to the backlog), qa.9 required checks
on `main` (D25).

Plans 1 and 2 are summarised at the end of Plan 3 — `git show dff021c:docs/PLAN.md`.
