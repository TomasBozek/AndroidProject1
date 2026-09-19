# Status

**G1 · Movies, paged and offline is open** — 79 points, 2026-09-21 09:00 → 18:00: a `movies`
feature over TMDB that pages as it scrolls, pulls to refresh, reads its Room cache offline, opens
a detail from a row, and reaches the real host from the dev menu; three decisions (D79–D81) open,
each with a task. Release G is open with no done sprint under it. § DevOps of
[BACKLOG.md](BACKLOG.md) holds 15 of the 50 points at which an improvement sprint is due. CI is
off (D73), so the gate is `/check pr` on the machine that merges. The rules are
[ai/PROCESS.md](ai/PROCESS.md); `/board` publishes this page to the board artifact.

## Board

**G1 · Movies, paged and offline** — open · 2026-09-21 09:00 → 2026-09-21 18:00 · 79 points ·
[ai/plans/G1-movies-paged-and-offline.md](ai/plans/G1-movies-paged-and-offline.md)

- [x] G1P1 TMDB fixtures on `dev`, and the key in `BuildConfig` · 6 → 6 · decides D80
- [x] G1H1 The movies data layer: a page is a cache entry · 25 → 12 · decides D79 · after G1P1
- [x] G1U1 `AppPullToRefresh` in `:core:ui` · 6 → 6
- [x] G1U2 The movies list pages, pulls and reads its cache · 12 → 12 · after G1H1, G1U1
- [x] G1U3 A movie detail from a row · 12 → 6 · after G1H1
- [x] G1P2 The dev build reaches the real host · 12 → 6 · decides D81 · after G1P1
- [ ] G1H2 Two Maestro flows through the movies · 6 · after G1U3

## Drafts

None. The first draft is the next sprint by default; `/sprint draft <name>` writes one from
[BACKLOG.md](BACKLOG.md) § Next, or § DevOps for an improvement sprint.

## Release G · A real API

Open — [ai/plans/G.md](ai/plans/G.md). Collects sprints until `/release close` ships them as
`v1.4.0`; a done sprint's lines move here from § Board. None yet.

## Shipped

**v1.3.0 · release F**, closed on 2026-09-19: four sprints — F1 · Dev menu and offline, F2 · One
backlog, one board, F3 · Green again, F4 · Things you can see — twenty-eight tasks, estimate 175,
actual 123, ratio 0.70. No band missed three times across the release; the 6 band was rewritten
before F4 and held. What shipped, in user words, is the `v1.3.0` block in
[CHANGELOG.md](CHANGELOG.md); the plan is [ai/plans/F.md](ai/plans/F.md), the sprint files
`ai/plans/F1-*.md` to `F4-*.md` with their briefs and retrospectives. Tagged by `/release close`
(D72); CI is off, so the tag builds nothing.

**v1.2.0 · release E**, closed on 2026-09-13: nineteen tasks, all shipped, estimate 239, actual 117,
ratio 0.49. The 25-point band was the one that missed — the three 25-point Inventory briefs and
the convention-plugin move each took 12 or less — and F's draft recalibrated it. What
shipped, in user words, is the `v1.2.0` block in [CHANGELOG.md](CHANGELOG.md); the plan is
[ai/plans/E.md](ai/plans/E.md). Tagged at `70de4f3` in F3P3, three days after the block — the
gap that made the tag the command's job (D72).

**v1.1.0 · releases B and D** — closed together on 2026-09-13; the plans are
[ai/plans/B.md](ai/plans/B.md) and [ai/plans/D.md](ai/plans/D.md).

**v1.0.0 · release A** — tagged and published. The plan is [ai/plans/A.md](ai/plans/A.md).

[RELEASING.md](RELEASING.md) says what CI does with a tag, and what it refuses. Ideas that are in
no plan are [BACKLOG.md](BACKLOG.md).
