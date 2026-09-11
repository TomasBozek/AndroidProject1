# Status

**Release D is open.** This file is its board: every task, its points, its state. Why each task is
there and what finishes it is [ai/plans/D.md](ai/plans/D.md); the rules the board runs by are
[ai/PROCESS.md](ai/PROCESS.md). Release B is **paused** below with its merged tasks intact, and
release C is a draft behind it: D, then B's last task, then C.

## Release D · the patch-up: what B3S1 left unreachable, and the rules that boxed it in

Opened 2026-09-11. 9 tasks, 60 points, one agent. B3S1 shipped the Trips feature and nothing
constructs `TripsDestination`, so five screens are in the APK and unreachable. That is the headline;
the rest are fixes that are broken now and silent. A task's state is `[ ]` open, `[x]` merged with
its actual, `[-]` dropped.

### Lane 1 · reachability, then the patch-ups · 60

- [x] D1P1 One plan open, a queue behind it, and room to write · 6 → 6 · decides D58
- [ ] D1X1 Trips takes the fifth tab · 6 · decides D59 · after D1P1
- [ ] D1X2 The dev menu jumps straight to a screen · 12 · after D1X1
- [ ] D1X3 `export_service.py` parses on the Python the README promises · 3
- [ ] D1P2 The generators stop recommending the build that is forbidden · 6
- [ ] D1X5 `init_project.py` rewrites the LICENSE it leaves behind · 3
- [ ] D1P3 The reference docs catch up with B3S1 · 6 · after D1X1
- [ ] D1P4 Two `doctor.py` checks that keep the reference docs honest · 6 · after D1P3
- [ ] D1X4 The test ids rejoin the closed vocabulary · 12 · decides D60 · after D1P4

## Paused · release B · the refactors v1.0 skipped, and the two features that use them

Opened 2026-09-10, paused 2026-09-11 with one task left. 18 tasks, 383 points, four agents.
**Lane 0 ran first and alone** — one of its tasks renumbers the module graph the other three lanes
write against — and merged before lanes 1–3 started. It resumes at B3S2 when D closes.

### Lane 0 · repo-wide, before the lanes start · 106

- [x] B0P1 The module topology, decided once (was F5 + F11) · 50 → 25 · decides D49
- [x] B0P2 The module tree moves, CLAUDE.md goes on a diet · 25 → 12 · after B0P1
- [x] B0X1 BaseViewModel's defaults, and its nullable state · 25 → 25 · decides D44
- [x] B0U1 The tabs have test ids (was qa.16) · 6 → 6

### Lane 1 · the design system · 87

- [x] B1U1 The screen shell joins the design system (was F13) · 25 → 25 · decides D50
- [x] B1U2 AppTextField rebuilt, the size enums folded (was F23 + M1) · 25 → 12 · decides D51
- [x] B1U3 The gallery is checked against the components (was F7) · 25 → 12 · decides D52
- [x] B1U4 Predictive back on dirty forms, auto-sizing numerics (was M3 + M4) · 12 → 12

### Lane 2 · build, release and the data layer · 90

- [x] B2P1 Two flavors (was F18) · 12 · decides D43 · 12 → 6
- [x] B2H1 The release ships an AAB with a tag-derived versionCode (was rest of F17c) · 12 → 12
- [x] B2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45 · 12 → 25
- [x] B2T1 Coil moves to the module that imports it (was half of F24) · 6 → 12
- [x] B2P2 Where the data-source interface lives (was F15) · 12 · decides D53 · 12 → 25
- [x] B2X1 A deep link to an uncached product opens it (was shell.7) · 12 → 25
- [x] B2H3 The HTTP cache, and state that survives process death (was half of F16) · 12 → 25
- [x] B2P3 Hooks become a committed .githooks (was F10) · 12 → 12

### Lane 3 · the showcase · 100

- [x] B3S1 Trips, a showcase feature (was S1) · 50 → 50
- [ ] B3S2 Field report, a showcase feature (was S2) · 50

## Next · release C

[ai/plans/C.md](ai/plans/C.md) — the arcade: nine small games that are each an excuse to drive one
group of components properly, and the release that finally answers D38. 24 tasks, 380 points, four
agents. It is a **draft**; the owner opens it when B closes. C0P1 plans a fifth bottom-bar tab for
the arcade hub — D1X1 spends that slot on Trips (D59), so C0P1 picks another entry point when C is
drafted for real.

## Shipped

Release A · what a v1.0 tag must not carry. Closed 2026-09-10, all 19 tasks landed: estimate 136,
actual 108, ratio 0.79 — which is why release B's lane budget is 100. What shipped, in user words,
is the `v1.0.0` block in [CHANGELOG.md](CHANGELOG.md); the plan is [ai/plans/A.md](ai/plans/A.md).

**It becomes a release when the tag is pushed**, which is the owner's:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

[RELEASING.md](RELEASING.md) says what CI then does, and what it refuses.

Ideas that are in neither plan are [BACKLOG.md](BACKLOG.md).
