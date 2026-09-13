# Status

**Release E is open.** This file is its board: every task, its points, its state. Why each task is
there and what finishes it is [ai/plans/E.md](ai/plans/E.md); the rules the board runs by are
[ai/PROCESS.md](ai/PROCESS.md). Nothing is paused and nothing is drafted behind it — what E did not
take is [BACKLOG.md](BACKLOG.md) § Next, in order.

## Release E · the fixes the backlog was carrying, and a showcase that gives every component a home

Opened 2026-09-13. 18 tasks, 236 points, **one agent** working the lanes in order — 1, then 2, then
3. Lane 1 is a morning and touches files the other two read, so it merges first; if the owner adds
a second agent it takes lane 3 once lane 1 has merged. A task's state is `[ ]` open, `[x]` done
with its actual, `[-]` dropped.

### Lane 0 · chores · 12

- [x] E0P1 Two releases close, one opens, and the process learns one agent · 12 → 12 · decides D61, D62

### Lane 1 · Kotlin and core fixes · 30

- [x] E1X1 The test ids rejoin the closed vocabulary (was D1X4) · 12 → 3
- [x] E1X2 A toast wears the theme · 6 → 3 · decides D63
- [x] E1H1 A stored trip the code no longer understands does not crash the read · 6 → 3
- [x] E1X3 The section header's action carries a test id · 3 → 3
- [ ] E1T1 Three small things the review found · 3

### Lane 2 · build and platform · 46

- [ ] E2P1 Every library dependency moves into a convention plugin · 25 · decides D64
- [ ] E2H1 The release build is launched, not just assembled · 12
- [ ] E2H2 A network security config and StrictMode for the debug build · 6
- [ ] E2P2 The dependency graph is submitted · 3

### Lane 3 · the design system and its showcase · 148

- [ ] E3U1 The design system stops speaking POS · 12
- [ ] E3S1 Inventory: the feature, its table and the list · 25 · after E1H1, E3U1
- [ ] E3S2 Inventory: the four-step editor · 25 · after E3S1
- [ ] E3S3 Inventory: the detail, its sections and delete · 25 · after E3S1
- [ ] E3S4 Inventory: editing an item reuses the editor · 12 · after E3S2, E3S3
- [ ] E3S5 Inventory: filter sheet, sort, and a selection toolbar · 25 · after E3S1
- [ ] E3S6 Home opens Inventory, and a flow drives the whole loop · 12 · after E3S4, E3S5
- [ ] E3T1 Unused components earn their place (was F6, C0T1) · 12 · decides D65 · after E3S6

## Shipped

**v1.1.0 · releases B and D**, closed together on 2026-09-13: seventeen of B's eighteen tasks and
eight of D's ten, estimate 375, actual 369, ratio 0.98 — which is why E's bands are unchanged. What
shipped, in user words, is the `v1.1.0` block in [CHANGELOG.md](CHANGELOG.md); the plans are
[ai/plans/B.md](ai/plans/B.md) and [ai/plans/D.md](ai/plans/D.md). **It becomes a release when the
tag is pushed**, which is the owner's:

```bash
git tag v1.1.0 && git push origin v1.1.0
```

**v1.0.0 · release A** — tagged and published. The plan is [ai/plans/A.md](ai/plans/A.md).

[RELEASING.md](RELEASING.md) says what CI does with a tag, and what it refuses. Ideas that are in
no plan are [BACKLOG.md](BACKLOG.md).
