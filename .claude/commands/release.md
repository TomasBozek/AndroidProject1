---
description: Close the open release, or draft the next one
argument-hint: close | draft <letter>
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/doctor.py), Bash(./gradlew *), Read, Glob, Grep, Edit, Write
---

`$ARGUMENTS`. The rules are `docs/ai/PROCESS.md`.

## `close`

Refuse unless every board line in the open plan is `[x]` or `[-]`. Then, in one commit titled
`<id> Ship <letter>`:

1. Sum the `est → act` pairs. Write the block into `docs/CHANGELOG.md`: the heading
   `## v<x.y.z> · release <letter> · <date>`, three to eight lines **in user words** — what someone
   using the app would notice, not what moved in the repository — the `Tasks:` line, and
   `Estimate · Actual · Ratio`.
2. Sweep the docs: walk `docs/ai/PROCESS.md` § Which doc changes when and check that each spec and
   reference file matches what shipped. Fix what drifted here rather than opening a task for it.
3. Cut the shipped board out of `docs/STATUS.md` and put the next plan's in its place, with its
   header line — the release title, `Open · ships as v<x.y.z>`, the counts, a link to the briefs.
   If the next plan is still a draft, `docs/STATUS.md` says so and links it instead: a draft's
   board is written among its briefs and is not a board until `/release draft` has cut it. If it is
   `Status: paused`, its board is already in `docs/STATUS.md` under a `Paused` heading — flip its
   header to `open` and lift the board into place, keeping every `[x]` it earned before it paused.
4. The plan file stays where it is, briefs intact, board gone. There is no archive (D48). A draft
   the owner abandons is deleted, and a decision row says why.
5. Run `/check pr`, then open the pull request.

The tag comes after the merge, and the owner pushes it — `docs/RELEASING.md` § Releasing.

## `draft <letter>`

Write `docs/ai/plans/<letter>.md` from `docs/ai/plans/TEMPLATE.md`. Nothing else changes.

1. Recalibrate first: read the shipped blocks' `Estimate · Actual · Ratio` in `docs/CHANGELOG.md`.
   If a band was off by more than 30 % on three or more tasks, rewrite that band's description in
   `docs/ai/PROCESS.md`. The numbers never change.
2. Take the candidates from `docs/BACKLOG.md` § Next, in order.
3. Write one task section per candidate — Why, Decide first, Done when, Touches, Read, Steps,
   Checks, Depends. **Done when** is commands and greps, never a sentence. **Steps** name real
   paths, the generator to call and the doc row to update. A section a cold agent cannot start from
   alone is not finished.
4. Estimate each with the bands. Anything over 50 is split before it gets an id.
5. Cut lanes by what the work touches — the Kotlin fixes, the build, a feature — so their file sets
   are disjoint. A lane is as long as its work; nothing is padded or trimmed to a number. Something
   repo-wide that every lane depends on runs first, in lane 0.
6. Write the `Agents:` line — `1` unless the owner has said otherwise, never more than 3 — and the
   Shared files table, which says which lane owns which contested file.
7. Assign ids **last**, once the lanes are settled, so nothing is renumbered.
8. Leave it `Status: draft`. The owner flips it to `open`.
