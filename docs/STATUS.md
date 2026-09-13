# Status

**No release is open.** E shipped on 2026-09-13 as `v1.2.0` and the next plan has not been drafted:
`/release draft F` writes it from [BACKLOG.md](BACKLOG.md) § Next, in order, and the owner opens it.
The rules the board runs by are [ai/PROCESS.md](ai/PROCESS.md).

## Shipped

**v1.2.0 · release E**, closed on 2026-09-13: nineteen tasks, all shipped, estimate 239, actual 117,
ratio 0.49. The 25-point band was the one that missed — the three 25-point Inventory briefs and
the convention-plugin move each took 12 or less — and the next draft recalibrates it. What
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
