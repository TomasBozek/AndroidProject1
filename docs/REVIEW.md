# Review · after Plans 1–4

The retrospective that feeds Plan 5, written 2026-09-10 from seven area reviews, one build
measurement and three side investigations. The interactive version with the decision cards is
the artifact [AndroidProject1 Retrospective](https://claude.ai/code/artifact/3cd1e78c-a274-43ef-a621-d25d132c632d); answers given there are the input to Plan 5.
This file is the durable summary; the full reviewer output, the measurements, the side reports and the two
showcase designs are in [review/](review/), and [review/HANDOFF.md](review/HANDOFF.md) says how to finish.
Ids (`F`, `H`, `M`, `S`) are shared with the artifact.

## Verdict

The architecture and build system are the right shape. The waste is around them: a PR gate that
builds six app variants and runs every test two or three times, a script suite that takes minutes
rather than the documented twenty seconds, 349 pixel-exact goldens that are 60 % of test time and
blind to overlays, a design system with 27 of 47 components used only by its gallery, and a
983-line rulebook loaded into every session. Most fixes are deletions.

| Area | Grade | One line |
|---|---|---|
| Build logic & CI | B+ | best structure in the repo; the wrong gate |
| Architecture | B | right shape, inverted defaults, ~450 dead lines |
| Design system | B− | enforced theme; too many components, duplicated gallery |
| App shell & features | A− | shell strong; onboarding and a nav-host write are weak |
| Tests | C+ | ViewModel tests carry the value; goldens and repetition the time |
| Scripts & docs | C | generators are the win; doctor, test_scripts, CLAUDE.md the cost |
| Data & network | B | production-grade core, ceremony around it, one real bug |

## Numbers (warm cache, this machine)

configure 0.7 s · `build` 3,859 tasks over 12 variants · 923 test executions for 498 tests,
772 s · `verifyRoborazziDebug` 177 s · one presentation module's tests 41 s · `test_scripts.py`
277–442 s · `doctor.py` 2.7 s warm, 13–19 s cold · goldens 11 MB / 349 PNG.

## Clear winners (leave alone)

Convention plugins and the one-property rename · `Screen()` as the single collector with buffered
channels · `Outcome` + `BaseRepository.cached()` · the generators and doctor's registration and
translation checks · the flat tab back stack and `SessionState` · KoinGraphTest, Room migration
tests, the Czech plurals test, ContrastTest · the Ktor retry policy and fixtures on `dev` · the
Keystore AEAD split · vendor-free analytics and crash seams · previews as the screenshot list.

## Findings

### 1 · Waste to cut

- **F1** The PR gate is `./gradlew build`: six variants, three R8 passes, tests per variant, and
  `build.yml` believes it assembles debug only. → explicit single-variant gate; unit tests only on
  `devDebug`. S
- **F2** `test_scripts.py` copies the repo 56 times, including `.claude/worktrees` (2,271 of
  2,856 files). → ignore `.claude`, copy once per class, prune ~10 duplicate tests; delete the four
  stale worktrees. S
- **F3** `doctor.py` walks 62,583 files per check; 428 matter. → one pruned walk and a shared
  index; drop the CLAUDE.md-tree and foreign-identifier checks; style greps to lint. S
- **F4** 349 goldens, five variants per screen, full-frame, no threshold, overlays invisible. →
  Phone/Dark/LargeFont for screens, Light/Dark for components, half resolution, a threshold, one
  screen-capture per overlay, template excluded. M
- **F5** A `di` module per feature (13–59 lines each); 56 modules cost ~300 s of Robolectric
  warm-up. → bindings beside the classes, `:core:di` aggregates; optionally two modules per
  feature. M
- **F6** 27 of 47 components unused; duplicate pairs; `AppFab` has no scaffold slot. → showcase
  features use 18; rename `AppToast`→`AppBanner`; add the slot; delete/merge the nine left. M
- **F7** `GalleryCatalog.kt` (896 lines) hand-copies every preview with no sync check. → the
  previews are the catalog. M
- **F8** `CLAUDE.md` 983 lines, six duplicates, three gate versions, one self-contradiction,
  `SessionState` missing `Onboarding`. → ~300 lines plus `docs/ARCHITECTURE.md`. M
- **F9** `PLAN.md` edited in 100 of 118 commits; 858 lines of plan files. → one file ≤150 lines;
  delete `PLAN-WORKERS.md`. S
- **F10** `export_service.py --sync-versions` (a TOML resolver) and `install_hooks.py` (vs
  `.githooks` + `core.hooksPath`). → shrink and delete. S
- **F11** The `service/` portability split: split packages, namespace gotcha, un-themed shell,
  no consumer. → fold into `:core:*` or keep with own packages; not both. L

### 2 · Corrections

- **F12** `BaseViewModel` defaults inverted: 20 of 28 call sites pass `loading = {}`;
  `whileSubscribed` has no caller; nullable data drops updates (ProductDetail favourite race). →
  overlay opt-in, non-null state, delete `whileSubscribed`, shared retry helper. M
- **F13** Loading/error/empty/alert/rationale are raw Material with dp literals outside the design
  system; `ContentState` replaces the whole screen. → slots filled by `:core:ui`. M
- **F14** ~450 dead lines: `OutcomeFlows`, `recover`/`flatMap`, `executeAsFlow`, 7 of 8 Formats
  roles, `AlertPayload`, `displayMessage`; the token-refresh scaffold is bound nowhere and
  duplicates Ktor. → delete; rebuild on Ktor `bearer` at Q8. S
- **F15** Ten `XDataSource` interfaces with one implementation and no fake. → interface at the
  repository only. M
- **F16** Missing: `SavedStateHandle` (shipped, unused), a connectivity monitor, an HTTP cache. M
- **F17** Bugs: (a) empty category spins forever; (b) add-to-cart in `AppNavHost` on a
  composition scope; (c) release job can publish a debug-signed APK, ships APK not AAB, hotfix
  versionCode regresses; (d) baseline profile never reaches `prodRelease`; (e) date picker overflows
  `AppDialog` (platform-width dialog, 24 dp padding around a 360 dp picker, no height cap). M
- **F18** Staging is prod plus a constant and an unreachable host. → `dev` + `prod`. S
- **F19** Robolectric SDK pin in 50 files; 11 screenshot-test copies; a fake in the wrong place. S
- **F20** Mandatory `XScreenTest` mostly asserts plumbing at 20–25 s warm-up each. → optional. S
- **F21** Onboarding: four modules and a third flow for one boolean. → fold into auth. M

### 3 · Design system

- **F22** POS vocabulary (till, void, cash) and pointer-driven density from the source product. S
- **F23** `ButtonSize` vs `ControlSize`; three components without a modifier; `AppTextField` has
  no IME action, autofill, password toggle or multi-line. → rebuild on `TextFieldState`. M
- **F24** Coil on all 13 Compose modules; two single-consumer plugins. S

## Hardening and modern APIs

Recommended: **H1** R8 mapping kept per release · **H3** forms scroll under the keyboard ·
**H5** pull-to-refresh on the catalog lists · **H6** dependency-graph submission for Dependabot
alerts · **H7** `network_security_config` with debug overrides · **H8** StrictMode in debug ·
**M1** `TextFieldState` fields · **M2** autofill content types · **M3** `PredictiveBackHandler`
for dirty forms · **M4** `TextAutoSize` on numeric text. Optional: **H9** lifecycle seam, **H10**
feature flags, **H11** push seam, **M5** shared-element transition as a showcase.

Skip: Material 3 Expressive — its components exist only in material3 1.5.0-alpha while the BOM
pins 1.4.0, and only five of 47 components delegate to Material; D10 stands, reworded. Also
SearchBar, Carousel, Tooltip, Credential Manager.

## Showcase features

- **S1 Trips** — three-step wizard (dates, segmented, stepper, slider, radio, checkbox, select, a
  destination picker returning a result) → review with a snackbar action → tabbed dashboard with
  badge, status dots, progress, skeletons, FAB, menu, sheet → detail with accordion and typed
  delete. Five screens, in-memory full stack, covers 22 components.
- **S2 Field report** — location via `LocationManagerCompat` (rationale, approximate-only,
  never-ask-again), camera and Photo Picker, SAF open/create document, every dialog and picker kind,
  undo snackbar, export, open in maps. Four screens, no new dependency, moves the camera helper into
  `service/`.

The bar holds five tabs and four exist: one feature gets the tab, the other is reached from Home.

## Plan 5 shape

One `docs/PLAN.md` under 150 lines. Do first: F1, F2+F3, F17e, F8+F9, F4. Then Trim (F5, F10/11,
F14, F15, F18, F19, F24), Correct (F12, F13, F16, F17a–d), Design system (F6, F7, F23, F22, H3,
H5, M1–M4), Showcase (S1, S2), Harden (H1, H6, H7, H8). One or two agents at a time.

## Method and limits

Reviewers read code with file:line evidence; one agent measured builds; side tasks checked the
Compose APIs in the resolved jars. No adversarial verification ran; where reviewers disagreed both
readings are a decision card. Not reviewed line by line: Maestro flows, Czech wording, component
internals beyond signatures.
