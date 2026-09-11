# Process

How work is planned, sized, done and shipped. The rules for writing code are
[../../CLAUDE.md](../../CLAUDE.md); the open plan is the file under [plans/](plans/) whose header
says `Status: open`.

## Ids

`<Release letter><Lane digit><Kind letter><Seq digit>` — `A1U1`, `A0X1`, `B2H3`. Pattern
`^[A-Z][0-9][UXTHPS][1-9]$`, checked by `doctor.py`.

| Part | Means |
|---|---|
| Release letter | the plan file `plans/<letter>.md`; the tag is chosen at ship and bound in `../CHANGELOG.md` |
| Lane digit | `0` = the priority lane: one agent, jumps the queue, may ship as a patch tag. `1`–`9` = planned lanes, one agent each |
| Kind letter | `U` UI and design system · `X` fix · `T` trim · `H` harden · `P` platform (build, CI, scripts, docs, process) · `S` showcase |
| Seq digit | `1`–`9` within one (release, lane, kind). A tenth means the lane is too big |

An id appears in the board line, the `### <id> <title>` section, the branch, the commit and
pull-request title, the changelog's `Tasks:` line, and any decision that cites it. **Never in
source**, and under D47 never in the branch or the pull request, which carry the release instead. A
carried-over item keeps its old id once, as `(was F19)`. Find one: `grep -rn '\bA1U1\b' docs .github` ·
`git log --oneline --grep='^A1U1 '` · `gh pr list --state all --search 'A1U1 in:title'`.

## Points and lanes

**100 points = one agent-day = 8 hours of one agent**, from `/task` to the pull request being open.
It covers T0, T1 and one round of CI fixes, but not waiting for CI. A point is five minutes.

| Band | Means |
|---|---|
| 3 | one file, a flag, a documentation row |
| 6 | one module changed **with its test**, or a new check and the code it forces. A fix across two or three files that adds no test is a 3 — six of release A's seven 6s landed at 3 that way |
| 12 | two to four modules, or a screen with both its tests |
| 25 | a flow of screens, a cross-cutting refactor, a CI job |
| 50 | a feature slice. The ceiling — anything larger is split before it gets an id |

**A lane holds 90–100 points**, so one agent finishes in roughly 7.5–8 hours. The band sizes a
**lane**, not a release: a release is as big as its work, and a small one is a single lane under the
floor rather than a padded one. Agents = `ceil(total ÷ budget)`, lowered until the lanes touch
disjoint paths and no lane of a multi-lane release is under 85. Never pad
a lane; move the work to [../BACKLOG.md](../BACKLOG.md). Inside a lane: shared-file tasks first, then
dependencies, then the largest. The header line is written last — `Agents: 2 · lane 1 91 (~7.3 h) ·
lane 2 97 (~7.8 h) · lane 0 open`.

**Lane 0** needs no plan: one agent, the release's own chores, and anything that must jump the
queue. A hotfix to a shipped release keeps that release's letter — `A0X1` — ships as a patch tag and
gets a patch block in the changelog.

**Calibration.** The agent appends `· est → act` to its board line; the ship task writes
`Estimate · Actual · Ratio` into the changelog block and the next budget is `min(100, round(100 ÷
ratio))`. A band off by 30 % on three tasks gets its description rewritten; the numbers never change.

## Task loop

1. Read `CLAUDE.md` (already loaded), this file once per session, then your lane and your task
   section — not the other lanes.
2. `/task <id>` takes the first `[ ]` in your lane; branch `<id>-<slug>` from a fresh `origin/main`.
   If its `Depends` is still `[ ]`, take the next.
3. A `Decide first` line is settled before the code, as a row in
   [../DECISIONS.md](../DECISIONS.md) under the pre-assigned number, in this pull request.
4. Use the generators for any new module, screen, component or data source.
5. T0 once or twice while working. Never the whole gate in the loop, never `./gradlew build`.
6. A fact you changed moves to its one file in the same commit (§ Which doc changes when).
7. Touch only the shared files your lane owns. Otherwise stop, comment `needs <file>` on the pull
   request, take the next task. Lane 0 may touch anything; other lanes rebase after it merges.
8. `/check pr` once, after `git rebase origin/main`; paste its tail into the pull-request body.
9. Flip your board line to `[x]` with `est → act` in the same single commit as the code, titled
   `<id> <title>`. Open the pull request with the template. Do not wait for CI. **Under D47 that
   pull request is opened once, on the first task, and every later commit lands on its branch.**
10. Between tasks, `gh pr checks`. Green: `gh pr merge --rebase --delete-branch`. Red: fix, amend,
    force-push; never weaken a check — under D47 nothing merges between tasks, so keep it green and
    take the next. Work you find on the way is one line in [../BACKLOG.md](../BACKLOG.md), never an
    edit to the plan.

## States

| Board line | Means | Written by |
|---|---|---|
| `- [ ] A1U1 … · 12` | open | the planner |
| `- [ ] A1U1 … · 12 · blocked: <≤5 words>` | cannot start; the agent took the next task | the agent |
| `- [x] A1U1 … · 12 → 15` | merged, with the actual — committed, under D47 | the agent, in the task's own commit |
| `- [-] A1U1 … · 12 · dropped: <why>` | dropped | the owner or the ship task |

There is no "doing" state: a lane is worked top to bottom by one agent, so the first `[ ]` is the
one in hand. A release is `Status: draft` → `Status: open` (the owner says the word) → closed by the
ship task, which cuts its board out of `../STATUS.md`.

**Exactly one plan is open.** A release the owner sets aside before it closes becomes
`Status: paused` and keeps its board in `../STATUS.md` under a `Paused` heading, merged tasks
intact — so work already done is never cut to make room. Any number of plans may sit behind the open
one as `draft` or `paused`; the owner says which is next, and `../STATUS.md` says so in one line.
That is what keeps "the plan whose header says `Status: open`" a question with one answer.

## Shared files

`settings.gradle.kts` · `core/di/**` · `app/**/AppNavHost.kt` · `app/**/KoinGraphTest.kt` ·
`gradle/libs.versions.toml` · `CLAUDE.md` · `docs/ai/CODEBASE.md` · `docs/README.md` ·
`.github/workflows/build.yml`. Each belongs to one lane per release, named in the plan's Shared files
table. A generator edit counts: `create_feature.py` writes four of them.

## Which doc changes when

| You changed | Update, in the same commit |
|---|---|
| a module | `CODEBASE.md` (the generators do it) |
| a screen, a route, a feature's shape | `reference/FEATURES.md` |
| an entity, a repository, a store | `reference/DOMAIN.md` |
| a component or a theme role | `reference/DESIGN-SYSTEM.md` |
| anything under `service/` | `reference/SERVICES.md` |
| `core/ui`, `core/di`, `app` | `reference/CORE.md` |
| a dependency | `DEPENDENCIES.md` |
| a rule | `../../CLAUDE.md` |
| a recipe's steps | `RECIPES.md` |
| a decision | `../DECISIONS.md` |
| nothing above | nothing. Do not touch a doc to prove you were here |

Every doc carries a length target, and they are written once, in
[../README.md](../README.md) § Rules for these docs.

## Ship, and draft the next plan

`/release close`: every line `[x]` or `[-]` → the changelog block with the ratio → the doc sweep
above → the next release's board into [../STATUS.md](../STATUS.md) → the owner pushes the tag. The
plan file stays where it is, briefs intact, board gone; there is no archive.

`/release draft <letter>`: read [../BACKLOG.md](../BACKLOG.md) § Next and the merged pull requests'
`est → act`, write one task section per item, estimate with the calibrated bands, cut disjoint lanes,
write the shared-file table and the agents line, assign ids last, leave it a draft. About 25 points.
