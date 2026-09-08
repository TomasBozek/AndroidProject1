# Hardening plan

The single plan for this template. Written to be picked up cold — a new session should be able to
read this file and carry on without any prior conversation.

**96 items across 14 groups. 33 done, 63 to go.** Phase 3 is in progress: track 2 of 7 is complete.

Companion docs: [CLAUDE.md](../CLAUDE.md) is the rulebook, [scripts/README.md](../scripts/README.md)
documents the generators, [README.md](../README.md) is the human orientation.

## Where it came from

This template's architecture — `BaseViewModel`, `UiState`, `Screen()`, `BaseRepository`, the
`Outcome` type, the screen-unit convention — grew out of a production Android client. An audit in
September 2026 produced 96 improvements, grouped A–N, each rated by priority (P0–P3), effort (S/M/L)
and how standard it is in the industry.

Nine of those were defects. Two turned out to be deliberate carry-overs and were withdrawn
(a two-second splash hold, and `UiState.loading` defaulting to non-null — both coherent in the
original). The other seven are fixed.

---

## Current state — verify before trusting

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew build
```

At the last run: **`doctor.py` 16 checks**, **`test_scripts.py` 36 tests**, **55 unit tests**,
build green. 26 Gradle modules, 10 scripts.

The working tree had **24 uncommitted files** (track 2's work) when this was written.

## What phase 3 builds on

API introduced in phases 1–2. Use these rather than reinventing them:

| Thing | Where | Notes |
|---|---|---|
| `ErrorDisplay.{Alert,Inline,Silent}` | `core/ui/viewmodel/BaseViewModel.kt` | `execute(errorDisplay = Inline)` puts a failure in place of the content and **remembers the call**, so the retry button re-runs it |
| `ContentState.{Error,Empty}` | `core/ui/state/ContentState.kt` | rendered by `Screen()` *instead of* content; `SystemEvent.ContentAction(id)` is its button |
| `Screen(onNavigation = …)` | `core/ui/component/Screen.kt` | destinations no longer write `CollectEffect` |
| `navArgs<T>()` | `BaseViewModel` | `SavedStateHandle` + `toRoute<T>()`; available in `init`, survives process death |
| `AlertPayload` | `core/ui/state/AlertState.kt` | typed; replaced `Map<String, Any>` |
| `AppTheme.spacing` | `core/ui/theme/Spacing.kt` | **defined but used nowhere** — G1 below finishes it |
| `MainDispatcherRule`, `FakeLogger` | `testFixtures(projects.service.core.ui)` | every ViewModel test uses both |
| `coreModule(isDebug)` | `core/di/Koin.kt` | binds a WARN-and-above logger in release |
| `DispatcherProvider` | `core/domain/coroutines/` | switch at the data source, **not** in `BaseRepository` |

**Gotchas found the hard way:**

- A screen with nav arguments needs Robolectric — `toRoute()` decodes through an `android.os.Bundle`.
  **Robolectric 4.16; 4.14 cannot read JDK 25 bytecode.** Use `@RunWith(RobolectricTestRunner::class)`
  and `@Config(sdk = [34])`, as `ProductsViewModelTest` does. That version floor is why **I3 is worth
  re-testing** — the detekt/ktlint rejection recorded in CLAUDE.md may simply have aged out.
- `testFixtures { enable = true }` works on AGP 9 alpha.
- `lint { checkDependencies }` belongs to `:app` only; on a library it re-lints the whole graph.
- The Modifier check in `doctor.py` exempts extensions, value-returning composables, and
  `*Theme`/`*Provider`/`*Screen`/`*Preview`. Extend that list rather than fighting it.
- `feature/template` holds **two** screens (`Template*` and `TemplateArgs*`). `create_feature.py`
  skips the args set deliberately — copying it leaves every generated feature with an unregistered
  destination.

---

## Done (33)

**Phase 1–2 — 27 items.** The four P0 defects (`observe()` decrementing the loading counter twice;
auto-backup carrying the session off-device; R8 disabled; nothing switching off the main thread),
nav arguments via `SavedStateHandle`, inline error/empty/retry states, `Screen()` owning navigation,
the snackbar command, typed alert payloads, the Modifier convention, the spacing scale, the testing
foundation (test bundle, Koin graph verification, `MainDispatcherRule`, a test per feature), the CI
and `doctor.py` gates (12 → 16 checks, layer direction, `lint.xml`), and the seven-file screen unit.

**Phase 3 track 2 — 6 items.** The generator gap:

| | |
|---|---|
| **L1** | `init_project.py` — renames the template into a project: 167 files, 39 source sets. Verified by a from-scratch build of the renamed project |
| **L2** | `create_component.py` — component + `@ComponentPreview`, optional `--state` |
| **L6** | `create_screen.py --with-args` — typed route, `navArgs` wiring, Robolectric test |
| **L8** | `install_hooks.py` — pre-commit `doctor.py`, honours `core.hooksPath`, won't clobber a foreign hook |
| **L9** | six slash commands in `.claude/commands/` carrying the follow-up steps |
| **L10** | `scripts/README.md` + worked `--help` examples, with a test that enforces both |

---

## Phase 3 — 25 items left

Sequencing constraints: **G2 before G4** (build components against real colour roles) and
**C7 before H3** (golden images are cheaper to record once than re-record per variant). Otherwise
the tracks are independent.

### Track 1 · Permissions, redesigned (5) — the headline

Nothing exists yet, and this is the part of the original request with nothing built for it. **A
redesign, not a port.** The original's `PermissionBox` has three problems worth fixing:

1. **The name describes the implementation, not the job.** It is a `Box`; what it does is guard
   content behind a permission.
2. **It leaks a footgun.** Its own KDoc tells the caller to write `if (!status.isGranted) return`
   *after* it — one forgotten line and the screen renders unguarded.
3. **`PermissionNotGrantedContentCallback`** is an interface where a lambda would do, and
   Accompanist is a dependency `service/` should not carry (it is being wound down as its modules
   move into androidx).

New package `service/core/ui/.../permission/`:

- **`PermissionStatus`** — sealed: `Granted`, `Granted.Partial` (Android 14 selected-photos, which a
  two-state API models wrongly), `Denied(canAskAgain: Boolean)`, `NotRequested`.
- **`rememberPermissionRequest(vararg permissions)`** → a handle exposing `status` and `request()`,
  hand-rolled on `rememberLauncherForActivityResult` + `shouldShowRequestPermissionRationale`.
  Single and multi-permission through one entry point. *(E2, E3)*
- **`PermissionGate(permission, rationale) { content }`** *(E1)* — content composed **only when
  granted**, making the footgun unrepresentable. The denied-permanently branch routes to app
  settings through the *same* helper `Screen()` already uses for `UiCommand.OpenAppSettings` —
  extract it rather than writing a second copy of that intent.
- **`PermissionRationale(title, message, confirmLabel)`** — a data class, not a content slot, so the
  common case needs no composable. Keep a slot for the uncommon one.
- **E4** — `POST_NOTIFICATIONS` in the manifest, a channel created on first launch, requested at a
  sensible moment rather than at startup.
- **E5** — a `Permissions` screen in `:feature:settings` as a full seven-file unit, listing each
  permission the app actually declares (read from `PackageManager`, so it cannot drift from the
  manifest) with a status chip and a route into system settings. Doubles as the demo.

*Deferred:* E6 (ViewModel-readable permission state) stays P3 — build it when a real feature needs
to branch on a permission.

### Track 3 · Design system (5)

- **G2** — a full M3 role set (primary/secondary/tertiary, surface, error) in light and dark from
  one seed, in `core/ui/theme/Color.kt`. Neutral and deliberately chosen, built to be replaced per
  project rather than to be a brand. `AppTheme(dynamicColor = false)` by default, so the app looks
  like itself out of the box.
- **G1-finish** — migrate the 24 `.dp` literals to `AppTheme.spacing`, and add a `doctor.py` check
  for a bare `.dp` literal in a feature screen. *Without this, G1 is a mechanism nobody uses.*
- **G4** — the component set: button, text field with error state, top bar, empty state, list item,
  skeleton/shimmer. Generate each with `create_component.py`.
- **G5** — one `AppImage` wrapper over Coil 3, so features never import Coil directly.
- **G7** — accessibility: `contentDescription` audit, 48 dp touch targets, a `testTag` convention
  (H4 needs it), and a font-scale variant added to `@ScreenPreview`.

### Track 4 · App shell (2)

- **D1** — bottom navigation with nested graphs on `NavigationSuiteScaffold`, so it becomes a rail
  on tablets for free. The first thing a real project needs and the one thing the template does not
  demonstrate.
- **D4** — one shared enter/exit transition set in `AppNavHost`.

### Track 5 · Testing and quality (5)

- **H3** — Compose Preview Screenshot Testing. Ten screens already carry a `@ScreenPreview` each,
  so this is near-free regression coverage for the whole UI.
- **C7** — `PreviewParameterProvider` for loaded / empty / long-text variants, added to
  `feature/template` so every generated screen gets one. Multiplies H3's coverage.
- **C5** — `kotlinx-collections-immutable` + `@Immutable` on state classes, plus a `doctor.py` check
  for a raw `List`/`Map`/`Set` in an `XState`.
- **H4** — one Compose UI test against `LoginScreen` as the clonable pattern.
- **I3** — re-test detekt 2.x and current ktlint (see the Robolectric note above). If they still
  fail, run the ktlint CLI as its own CI step on its own JDK rather than as a Gradle plugin.

### Track 6 · Shipping baseline (6)

**B3** flavors (dev/staging/prod + `BuildConfig` base URL) · **B4** signing config reading the
already-gitignored `keystore.properties` · **B5** `androidx.core.splashscreen` · **J1**
`:service:errortracker` (interface in `service/`, vendor impl in `:app`; four clean hook points
already exist in `BaseRepository` and `BaseViewModel`) · **J4** LeakCanary + Chucker on
`debugImplementation` · **M3** gitleaks in CI.

### Track 7 · Identity (2)

- **N1-finish** — redesign `updateData` and `isLoading` rather than renaming them. These are the
  last two helpers still essentially verbatim from the original. `isLoading`'s setter discards a
  custom loading message; fixing that *is* the redesign.
- **I5** — Renovate or Dependabot on the version catalog. coroutines 1.9.0 beside Compose BOM
  2026.02 is already drifting.

---

## Phase 4 — 38 items

Led by **F2** `:service:network` (Ktor client, status→`DomainError` mapping, auth headers,
single-flight token refresh) and **F3** Room/offline-first — both L, both deliberately held back
until there is a real API to point them at. `BaseRepository` in the original had a
`repositoryCall(remoteCall, localCall, updateLocal)` overload that emits cache then remote; porting
that one function is most of F3.

Then **K2** convention plugins (the standing decision — note the original *does* use them, which
was recorded incorrectly and is now corrected), **G3** `:core:designsystem` split, **G6** adaptive
layouts, **D2/D3/D5** deep links / nav results / feature-owned graphs, the rest of **J**
(analytics with automatic screen tracking off `Screen()`, logger backend, remote config, debug
menu), **L3/L4/L5/L7** (`doctor.py --fix`, `create_service.py`, `check_strings.py`, a localization
pipeline written fresh rather than ported), **M2/M4/M5** (token storage, ADRs, release process),
**C8/C9/C10**, **E6**, **H7**, **I6**, **K3/K4**, **N6/N8** (licence).

---

## Verification

Every track ends green on:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew build
```

Phase 3 should raise `doctor.py` to ~19 checks (bare `.dp` literal, unstable `XState` collection,
plus whatever the permissions work adds).

Track-specific:

- **Permissions** cannot be verified by unit test alone. `./gradlew :app:installDebug`, open the new
  settings Permissions screen, and check all three states: grant, deny once (rationale), deny twice
  (permanently denied → settings).
- **Generators** — smoke-test end to end, then revert, and add cases to `scripts/test_scripts.py`.
  That suite caught `create_feature.py` cloning both template screens; it earns its keep.
- **Screenshot tests** — `./gradlew updateDebugScreenshotTest` to record, then
  `validateDebugScreenshotTest`. Deliberately break one padding value and confirm it fails.
- **Colour/spacing** — `@ScreenPreview` renders every screen light and dark. Check the dark set
  after G2; the wizard scheme it replaces was never designed.
