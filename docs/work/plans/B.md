# Release B · the refactors v1.0 was allowed to skip

Status: draft
Agents: to be cut when this opens · carried over 141 · backlog candidates not yet estimated
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D43, D44, D45

Everything here was in release A's draft and was dropped from it for one reason: **it is not
broken**. Each is a refactor, a subtraction or new behaviour, and v1.0 shipped without it on
purpose. The points are the ones A's draft carried, re-checked against the code on 2026-09-10.

This is a draft. `/release draft B` finishes it: the backlog candidates below get task sections,
the bands get recalibrated against release A's `Estimate · Actual · Ratio`, and the lanes get cut
so they touch disjoint paths. Assign ids last; the ones below are provisional.

## Carried over from release A · 141

### Lane 0 · docs and conventions

- [ ] B0P1 The module tree moves, CLAUDE.md goes on a diet · 25

The half of A0P2 that v1.0 did not need: `docs/spec/CODEBASE.md` holds the fenced module tree and
the convention-plugin table byte for byte, `CLAUDE.md` comes under its own 300-line budget, and
`doctor.py` grows `check_docs_index`, `check_task_ids` and `check_doc_budgets` — 33 checks, not 30.
Touches `scripts/_common.py`, `scripts/doctor.py`, `scripts/test_scripts.py`, so it runs alone.
Repoint `CLAUDE_MD_FILE` to `MODULE_TREE_FILE`. Read `scripts/_common.py:25,65,426-475` ·
`scripts/doctor.py:392-427` · `scripts/test_scripts.py:185,249,413`.

### Lane 1 · screens and view models

- [ ] B1X1 BaseViewModel's defaults, and its nullable state · 25 · decides D44

21 of about 28 call sites pass `loading = {}`, and nullable state drops updates. A1X4 already fixed
the one place that hurt — the product-detail heart — so what is left is the cross-cutting half.
**Decide first** make the overlay opt-in and the state non-null, or opt-in only → D44. Touches
`service/core/ui` and every `feature/*/presentation`, so it takes the whole `test` run with it.

- [ ] B1U1 The tabs have test ids (was qa.16) · 6

The four tabs carry no `testTag`, so the Maestro flows tap English labels — against `CLAUDE.md`'s
own "find by id, never by text" rule. Each tab gets `tabs_<name>Tab`, the flows use them, and a
`doctor.py` check fails a tab without one. A0X2 fixed why the flows were red; this fixes why they
were fragile.

- [ ] B1U2 Predictive back on dirty forms, auto-sizing numerics (was M3+M4) · 6

There is no `BackHandler` anywhere, so a half-filled form is lost silently, and Czech price strings
wrap. `PredictiveBackHandler` on the dirty form, `TextAutoSize` on numeric text, a test each.

### Lane 2 · build, release, data

- [ ] B2H1 The release ships an AAB with a tag-derived versionCode (was rest of F17c) · 25

A2H1 made the job refuse to publish unsigned; this is the other half. `bundleProdRelease` rather
than an APK, and `versionCode` from the tag rather than `git rev-list --count HEAD`, which regresses
on a hotfix. D32 says a GitHub release carrying the artifact is the release, so neither was needed
for v1.0 — reopen D32 first if that has changed.

- [ ] B2P1 Two flavors (was F18) · 12 · decides D43

`staging` is `prod` plus one constant and an unreachable host. **Decide first** cut it and let
`DebugMenu` follow `BuildConfig.DEBUG`, or keep three → D43. Note the measurement that changed since
A's draft: the flavor no longer costs the pull-request gate anything, because the gate compiles it
rather than assembling it. This is now taste, not cost.

- [ ] B2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45

The profile is recorded minified into `src/devRelease`, so `prodRelease` never sees it and the whole
benefit is lost silently. **Decide first** fix the wiring, or cut `:baselineprofile` and its two
Gradle plugins → D45.

- [ ] B2P2 Hooks become a committed .githooks (was F10) · 12

`install_hooks.py` is 153 lines reimplementing `core.hooksPath`, and `export_service.py
--sync-versions` is a 170-line TOML resolver for a script with no consumer. `.githooks/pre-commit`
is committed, `README.md` names the one `core.hooksPath` line, and both scripts shrink.

- [ ] B2H3 network_security_config and StrictMode (was H7+H8) · 12

Note what the audit corrected: cleartext is **already** denied by the platform at this `targetSdk`,
so the original premise was wrong. What stands is that a debug build cannot be proxied without a
config, and a main-thread DataStore read goes unnoticed without StrictMode. Re-scope before
estimating.

- [ ] B2T1 Coil and the two single-consumer plugins (was F24) · 6

Coil is put on all thirteen Compose modules so that `doctor.py` can forbid it on twelve. Coil moves
to `core/ui` alone and Ktor to `service/network` alone, and the check that forbade it is deleted.
Watch the collision A's audit found: moving Ktor into a module build file runs into `CLAUDE.md`'s
"a module build file is a `plugins` block and its project dependencies, nothing else" rule — the
dependency has to stay in a convention plugin that only that module applies.

## Backlog candidates · not yet estimated

From [../BACKLOG.md](../BACKLOG.md) § Next, roughly in order. These get task sections when this plan
is drafted properly; until then the backlog line is the record.

- The module topology, decided once (was F5 + F11) · 50 · the ten `di` modules and the `service/`
  split are one question. Repo-wide, so it runs alone in lane 0 before the lanes start — and it
  should be the first thing B decides, because B0P1 writes the module tree it would invalidate.
- The screen shell joins the design system (was F13) · 25 · after the topology.
- Where the data-source interface lives (was F15) · 12.
- SavedStateHandle, connectivity, HTTP cache (was F16) · 25.
- A deep link to an uncached product opens it (was shell.7) · 12.
- The dependency graph is submitted (was H6) · 6.
- Unused components earn their place (was F6) · 25 · pairs with the showcase features.
- The gallery is generated from the previews (was F7) · 25.
- AppTextField rebuilt, size enums folded (was F23 + M1 + M2) · 25.
- The design system stops speaking POS (was F22) · 6.
- Trips, a showcase feature (was S1) · 50.
- Field report, a showcase feature (was S2) · 50.

**The two showcase features are the expansion.** Everything above them is maintenance; `S1` and
`S2` are what give the 27 unused components a home and what turn the template from a structure into
a demonstration. If B has to be one release rather than two, cut the maintenance, not those.

## What v1.0 deliberately left standing

Recorded here so B does not rediscover it as a defect:

- The token-refresh scaffold stays — D46, taken in A2T1. It is built on Ktor's `bearer` provider,
  its stale-token comparison is correct, and it is tested. It has no caller because there is no real
  API, which is D20, not a bug.
- `docs/archive/` was never created. D42's review material was deleted outright in A0P2 rather than
  archived, because the archive's only stated purpose was to hold it until release A shipped.
- The 70 test ids no Maestro flow drives are a `doctor.py` note, not a failure. Coverage of the
  flows is D30's question, not a convention violation.
