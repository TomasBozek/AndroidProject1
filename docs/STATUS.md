# Status

**F4 · Things you can see is open** — a UI sprint of 64 points, 2026-09-17 09:00 → 2026-09-18
18:00: `/check pr` gains lint, then the description-list bug, a language picker, a shared-element
showcase, the component playground and feedback roles. Release F is open with three done sprints
under it. CI is off (D73), so the gate is `/check pr` on the machine that merges. The rules are
[ai/PROCESS.md](ai/PROCESS.md); `/board` publishes this page to the board artifact. § DevOps holds 6
of the 50 points at which an improvement sprint is due.

## Board

**F4 · Things you can see** — open · 2026-09-17 09:00 → 2026-09-18 18:00 · 64 points ·
[ai/plans/F4-things-you-can-see.md](ai/plans/F4-things-you-can-see.md)

- [x] F4P1 `/check pr` runs lint · 3 → 3
- [x] F4X1 The description list wraps its value, not its label · 6 → 6
- [x] F4U1 A language picker in Settings · 12 → 12 · decides D75
- [x] F4S1 A shared-element transition from a product row to its detail · 12 → 12 · decides D76
- [ ] F4U2 A component playground behind the dev menu (was C1U6) · 25 · decides D77
- [ ] F4U3 Feedback roles in the palette · 6 · decides D78

## Drafts

None. The first draft is the next sprint by default; `/sprint draft <name>` writes one from
[BACKLOG.md](BACKLOG.md) § Next, or § DevOps for an improvement sprint.

## Release F · Sprints

Open — [ai/plans/F.md](ai/plans/F.md). Collects sprints until `/release close` ships them as
`v1.3.0`; a done sprint's lines move here from § Board.

**F3 · Green again** — done 2026-09-17 · 48 → 33 ·
[ai/plans/F3-green-again.md](ai/plans/F3-green-again.md)

- [x] F3P1 CI is back: the repository is public · 6 → 3 · decides D71
- [x] F3P2 Main is protected · 3 → 3 · after F3P1
- [x] F3P3 The tag is pushed when the ship commit merges · 6 → 3 · decides D72
- [x] F3P4 The pre-commit hook is installed by something · 3 → 3
- [x] F3X2 The connectivity monitor declares its permission · 3 → 3
- [x] F3X1 `create_component.py` writes an entry that compiles · 3 → 6
- [x] F3P5 A brief is written against the checks, and the release line follows its sprints · 6 → 3
- [x] F3P6 Coverage goes somewhere · 3 → 3 · after F3P1
- [x] F3P7 A launch config for the emulator · 6 → 3
- [x] F3P8 A design change starts in Claude Design · 6 → 3
- [x] F3P9 CI is switched off · 3 → 3 · decides D73

**F2 · One backlog, one board** — done 2026-09-16 · 27 → 15 ·
[ai/plans/F2-one-backlog-one-board.md](ai/plans/F2-one-backlog-one-board.md)

- [x] F2P1 One backlog grammar in every section · 6 → 3 · decides D69
- [x] F2P2 The sprint close audits the process · 3 → 3
- [x] F2P3 A run that never started reads as a failure · 3 → 3
- [x] F2P4 The board publishes from a cloud routine · 3 → 3 · decides D70
- [x] F2P5 The board opens on the state of the project · 12 → 3 · after F2P1

**F1 · Dev menu and offline** — done 2026-09-15 · 36 → 30 ·
[ai/plans/F1-dev-menu-and-offline.md](ai/plans/F1-dev-menu-and-offline.md)

- [x] F1P1 Sprints, releases and the board · 12 → 12 · decides D66, D67
- [x] F1X1 The dev menu jumps straight to a screen (was D1X2) · 12 → 6
- [x] F1H1 A connectivity banner (was a third of F16) · 12 → 12 · decides D68

## Shipped

**v1.2.0 · release E**, closed on 2026-09-13: nineteen tasks, all shipped, estimate 239, actual 117,
ratio 0.49. The 25-point band was the one that missed — the three 25-point Inventory briefs and
the convention-plugin move each took 12 or less — and F's draft recalibrated it. What
shipped, in user words, is the `v1.2.0` block in [CHANGELOG.md](CHANGELOG.md); the plan is
[ai/plans/E.md](ai/plans/E.md). **It becomes a release when the tag is pushed**, which is the
owner's:

```bash
git tag v1.2.0 && git push origin v1.2.0
```

**v1.1.0 · releases B and D** — closed together on 2026-09-13; the plans are
[ai/plans/B.md](ai/plans/B.md) and [ai/plans/D.md](ai/plans/D.md).

**v1.0.0 · release A** — tagged and published. The plan is [ai/plans/A.md](ai/plans/A.md).

[RELEASING.md](RELEASING.md) says what CI does with a tag, and what it refuses. Ideas that are in
no plan are [BACKLOG.md](BACKLOG.md).
