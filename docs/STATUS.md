# Status

**Nothing is open.** Release A is closed and waiting for its tag; release B is drafted but not yet
open. This file is the open release's board — it fills up again when one is.

## Release A · what a v1.0 tag must not carry

Closed 2026-09-10. All 19 tasks landed: estimate 136, actual 108, ratio 0.79. What shipped, in user
words, is the `v1.0.0` block in [CHANGELOG.md](CHANGELOG.md); why each task was there and what
finished it is [ai/plans/A.md](ai/plans/A.md).

**It becomes a release when the tag is pushed**, which is the owner's:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

[RELEASING.md](RELEASING.md) says what CI then does, and what it refuses.

## Next · release B

[ai/plans/B.md](ai/plans/B.md) — the refactors v1.0 skipped, and the two showcase features that
give the 26 unused components a home. Drafted: 18 tasks, 383 points, four agents — lane 0 runs first
and alone, then three lanes on disjoint paths. It is still a **draft**, and the owner opens it; its
board moves here at that point.

Ideas that are not in either plan are [BACKLOG.md](BACKLOG.md).
