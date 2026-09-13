# Process

How work is planned, sized, done and shipped. The rules for writing code are
[../../CLAUDE.md](../../CLAUDE.md); the open plan is the file under [plans/](plans/) whose header
says `Status: open`, and its board is [../STATUS.md](../STATUS.md).

## One agent

**One agent works a release, lane by lane, top to bottom.** A second or third is the owner's call,
written in the plan's `Agents:` line, and only for a release whose lanes are different kinds of
work — a build lane beside a feature lane, say. Never more than three, and never one per lane by
reflex: every agent is a full context, roughly 100k tokens read before its first edit, so a lane is
not a reason to start one. The same holds inside a session — do the work in the session; no
subagents and no workflow scripts unless the owner asks for them by name (D61).

## Ids

`<Release letter><Lane digit><Kind letter><Seq digit>` — `A1U1`, `A0X1`, `E3S2`. Pattern
`^[A-Z][0-9][UXTHPS][1-9]$`, checked by `doctor.py`.

| Part | Means |
|---|---|
| Release letter | the plan file `plans/<letter>.md`; the tag is chosen at ship and bound in `../CHANGELOG.md` |
| Lane digit | `0` = chores and hotfixes, which jump the queue and may ship as a patch tag. `1`–`9` = a group of related work whose files no other lane writes |
| Kind letter | `U` UI and design system · `X` fix · `T` trim · `H` harden · `P` platform (build, CI, scripts, docs, process) · `S` showcase |
| Seq digit | `1`–`9` within one (release, lane, kind). A tenth means the lane is too big |

An id appears in the board line, the `### <id> <title>` section, the commit title, the changelog's
`Tasks:` line and any decision that cites it. **Never in source.** A carried-over item keeps its
old id once, as `(was D1X4)`. Find one: `grep -rn '\bA1U1\b' docs .github` ·
`git log --oneline --grep='^A1U1 '`.

## Points and lanes

**100 points = one agent-day = 8 hours**, from `/task` to the pull request being open. It covers
T0, T1 and one round of CI fixes, but not waiting for CI. A point is five minutes.

| Band | Means |
|---|---|
| 3 | one file, a flag, a documentation row |
| 6 | one module changed **with its test**, or a new check and the code it forces. A fix across two or three files that adds no test is a 3 |
| 12 | two to four modules, or a screen with both its tests |
| 25 | a flow of screens, a cross-cutting refactor, a CI job |
| 50 | a feature slice. The ceiling — anything larger is split before it gets an id |

**A lane is a group, not a day.** Cut lanes by what the work touches — one for the Kotlin fixes,
one for the build, one for a feature — so their file sets are disjoint and a second agent could
take one without stepping on the first. A lane is as long as its work and a release is as big as
its work; neither is padded and neither is trimmed to a number. Inside a lane: tasks that write
shared files first, then dependencies, then the largest. Lane 0 needs no plan: the release's own
chores, and anything that must jump the queue. A hotfix to a shipped release keeps that release's
letter — `A0X1` — ships as a patch tag and gets a patch block in the changelog.

**Calibration.** The agent appends `· est → act` to its board line; the ship task writes
`Estimate · Actual · Ratio` into the changelog block. A band off by 30 % on three tasks gets its
description rewritten; the numbers never change.

## Task loop

1. Read `CLAUDE.md` (already loaded), this file once per session, then the plan's opening and the
   lane you are on.
2. `/task <id>` takes the first `[ ]` in your lane whose `Depends` is settled. The branch is the
   lane's — `<letter><lane>-<slug>`, made from a fresh `origin/main` on the lane's first task — and
   every task of the lane is one commit on it.
3. A `Decide first` line is settled before the code, as a row in [../DECISIONS.md](../DECISIONS.md)
   under the pre-assigned number, in the same commit.
4. Use the generators for any new module, screen, component or data source.
5. T0 once or twice while working. Never the whole gate in the loop, never `./gradlew build`.
6. A fact you changed moves to its one file in the same commit (§ Which doc changes when).
7. Work you find on the way is one line in [../BACKLOG.md](../BACKLOG.md), never an edit to the
   plan. A brief that turns out wrong: build what is right, say so in the pull request, and the
   owner amends the brief.
8. Flip your board line to `[x]` with `est → act` in the task's own commit, titled `<id> <title>`,
   carrying the code, the docs and the board line together.
9. `/check pr` once per lane, after `git rebase origin/main`; paste its tail into the pull-request
   body. Open the pull request with the template on the lane's first task and push every later
   task onto it. Do not wait for CI: check `gh pr checks` between tasks, keep it green, and never
   weaken a check. `gh pr merge --rebase --delete-branch` once the lane is done and green — `main`
   stays one commit per task (D17).
10. With more than one agent, touch only the files your lane owns (§ Shared files); one that needs
    another lane's file finishes what it can, says so on the pull request and takes the next task.
    Alone, there is nothing to arbitrate.

## States

| Board line | Means | Written by |
|---|---|---|
| `- [ ] A1U1 … · 12` | open | the planner |
| `- [ ] A1U1 … · 12 · blocked: <≤5 words>` | cannot start here; the agent took the next task | the agent |
| `- [x] A1U1 … · 12 → 15` | done, with the actual | the agent, in the task's own commit |
| `- [-] A1U1 … · 12 · dropped: <why>` | dropped | the owner or the ship task |

There is no "doing" state: a lane is worked top to bottom, so the first `[ ]` is the one in hand.
A release is `Status: draft` → `Status: open` (the owner says the word) → closed by the ship task.
**Exactly one plan is open.** One the owner sets aside becomes `Status: paused` and keeps its board
in `../STATUS.md` under a `Paused` heading, done tasks intact; drafts queue behind, and
`../STATUS.md` says which is next.

## Shared files

Only a release with more than one agent needs the table; a one-agent plan still lists what each
lane writes, so the next planner can hand a lane to a second agent without re-reading the code.
The files two lanes most often want: `settings.gradle.kts` · `core/di/**` · `app/**/AppNavHost.kt`
· `app/**/KoinGraphTest.kt` · `gradle/libs.versions.toml` · `build-logic/**` · `scripts/doctor.py`
· `CLAUDE.md` · `docs/ai/CODEBASE.md` · `.github/workflows/build.yml`. A generator edit counts:
`create_feature.py` writes four of them.

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
above → the next plan's board into [../STATUS.md](../STATUS.md) → the owner pushes the tag. The
plan file stays where it is, briefs intact, board gone; there is no archive. A draft the owner
abandons is deleted, and a decision row says why.

`/release draft <letter>`: read [../BACKLOG.md](../BACKLOG.md) § Next in order and the shipped
`est → act`, write one task section per item, estimate with the bands, cut lanes by what they
touch, write the `Agents:` line — `1` unless the owner has said otherwise — assign ids last, leave
it a draft. About 25 points.
