# Status

**Release B is open.** This file is its board: every task, its points, its state. Why each task is
there and what finishes it is [ai/plans/B.md](ai/plans/B.md); the rules the board runs by are
[ai/PROCESS.md](ai/PROCESS.md).

## Release B · the refactors v1.0 skipped, and the two features that use them

Opened 2026-09-10. 18 tasks, 383 points, four agents. **Lane 0 runs first and alone** — one of its
tasks renumbers the module graph the other three lanes write against — and merges before lanes 1–3
start. A task's state is `[ ]` open, `[x]` merged with its actual, `[-]` dropped.

### Lane 0 · repo-wide, before the lanes start · 106

- [x] B0P1 The module topology, decided once (was F5 + F11) · 50 → 25 · decides D49
- [x] B0P2 The module tree moves, CLAUDE.md goes on a diet · 25 → 12 · after B0P1
- [x] B0X1 BaseViewModel's defaults, and its nullable state · 25 → 25 · decides D44
- [x] B0U1 The tabs have test ids (was qa.16) · 6 → 6

### Lane 1 · the design system · 87

- [ ] B1U1 The screen shell joins the design system (was F13) · 25 · decides D50
- [ ] B1U2 AppTextField rebuilt, the size enums folded (was F23 + M1) · 25 · decides D51
- [ ] B1U3 The gallery is generated from the previews (was F7) · 25 · decides D52
- [ ] B1U4 Predictive back on dirty forms, auto-sizing numerics (was M3 + M4) · 12

### Lane 2 · build, release and the data layer · 90

- [ ] B2P1 Two flavors (was F18) · 12 · decides D43
- [ ] B2H1 The release ships an AAB with a tag-derived versionCode (was rest of F17c) · 12
- [ ] B2H2 The baseline profile reaches the shipping build (was F17d) · 12 · decides D45
- [ ] B2T1 Coil moves to the module that imports it (was half of F24) · 6
- [ ] B2P2 Where the data-source interface lives (was F15) · 12 · decides D53
- [ ] B2X1 A deep link to an uncached product opens it (was shell.7) · 12
- [ ] B2H3 The HTTP cache, and state that survives process death (was half of F16) · 12
- [ ] B2P3 Hooks become a committed .githooks (was F10) · 12

### Lane 3 · the showcase · 100

- [ ] B3S1 Trips, a showcase feature (was S1) · 50
- [ ] B3S2 Field report, a showcase feature (was S2) · 50

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
