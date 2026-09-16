# Status

**Sprint F3 · Green again** — open · 2026-09-16 15:30 → 2026-09-17 18:00 · release F. Goal: CI
runs again and every miss the last two audits found is fixed, so the process does what its docs
say. The improvement sprint: nine tasks, 42 points, every line § DevOps held; the briefs are
[ai/plans/F3-green-again.md](ai/plans/F3-green-again.md) and the rules are
[ai/PROCESS.md](ai/PROCESS.md). Take one with `/task <id>`; `/board` publishes this page to the
board artifact.

## Board

- [x] F3P1 CI is back: the repository is public · 6 → 3 · decides D71
- [x] F3P2 Main is protected · 3 → 3 · after F3P1
- [x] F3P3 The tag is pushed when the ship commit merges · 6 → 3 · decides D72
- [ ] F3P4 The pre-commit hook is installed by something · 3
- [ ] F3X1 `create_component.py` writes an entry that compiles · 3
- [ ] F3P5 A brief is written against the checks, and the release line follows its sprints · 6
- [ ] F3P6 Coverage goes somewhere · 3 · after F3P1
- [ ] F3P7 A launch config for the emulator · 6
- [ ] F3P8 A design change starts in Claude Design · 6

## Drafts

None. The first draft is the next sprint by default; `/sprint draft <name>` writes one from
[BACKLOG.md](BACKLOG.md) § Next, or § DevOps for an improvement sprint.

## Release F · Sprints

Open — [ai/plans/F.md](ai/plans/F.md). Collects sprints until `/release close` ships them as
`v1.3.0`; a done sprint's lines move here from § Board.

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
