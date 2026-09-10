---
description: Close the open release, or draft the next one
argument-hint: close | draft <letter>
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/doctor.py), Bash(./gradlew *), Read, Glob, Grep, Edit, Write
---

`$ARGUMENTS`. The rules are `docs/work/PROCESS.md`.

## `close`

Refuse unless every board line in the open plan is `[x]` or `[-]`. Then, in one commit titled
`<id> Ship <letter>`:

1. Sum the `est → act` pairs. Write the block into `docs/spec/CHANGELOG.md`: the heading
   `## v<x.y.z> · release <letter> · <date>`, three to eight lines **in user words** — what someone
   using the app would notice, not what moved in the repository — the `Tasks:` line, and
   `Estimate · Actual · Ratio`.
2. Sweep the docs: walk `docs/work/PROCESS.md` § Which doc changes when and check that each spec and
   reference file matches what shipped. Fix what drifted here rather than opening a task for it.
3. Point `docs/README.md` § Work at the next plan.
4. Move the plan into `docs/archive/plans/` and delete anything the archive was holding for it.
5. Run `/check pr`, then open the pull request.

The tag comes after the merge, and the owner pushes it — `docs/guides/OPERATIONS.md` § Releasing.

## `draft <letter>`

Write `docs/work/plans/<letter>.md` from `docs/work/plans/TEMPLATE.md`. Nothing else changes.

1. Recalibrate first: `gh pr list --state merged --search '<previous letter>' --json title,body`,
   read the `Points:` lines, and compute the ratio. If a band was off by more than 30 % on three or
   more tasks, rewrite that band's description in `docs/work/PROCESS.md`. The numbers never change.
2. Take the candidates from `docs/work/BACKLOG.md` § Next, in order.
3. Write one task section per candidate — Why, Decide first, Done when, Touches, Read, Steps,
   Checks, Depends. **Done when** is commands and greps, never a sentence. **Steps** name real
   paths, the generator to call and the doc row to update. A section a cold agent cannot start from
   alone is not finished.
4. Estimate each with the calibrated bands. Anything over 50 is split before it gets an id.
5. Cut lanes of 90–100 points that touch disjoint paths, using the `Touches` sets. A task that would
   straddle two lanes moves, or the work is deferred — never padded. Something repo-wide runs alone
   in lane 0 before the lanes start.
6. Write the Shared files table and the `Agents:` header line. The plan says how many agents; you do
   not decide the number first and cut lanes to fit it.
7. Assign ids **last**, once the lanes are settled, so nothing is renumbered.
8. Leave it `Status: draft`. The owner flips it to `open`.
