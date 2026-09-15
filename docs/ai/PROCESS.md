# Process

How work is planned, sized, done and shipped. The rules for writing code are
[../../CLAUDE.md](../../CLAUDE.md); the open sprint is the file under [plans/](plans/) whose header
says `Status: open`, and its board is [../STATUS.md](../STATUS.md).

## Sprints and releases

Work runs in sprints (D66, D67). **A sprint is the unit of planning; a release is the unit of
shipping**, and a release holds one sprint or several — after a sprint closes the owner either
ships or adds another sprint to the same release, and adding is the default.

| Term | Here |
|---|---|
| product backlog | [../BACKLOG.md](../BACKLOG.md) § Next, in order, each line grouped by what it touches; the owner is the product owner |
| release | `plans/<letter>.md` — a name, `Status: open` or `shipped as v<x.y.z>`, the goal. `/release open <letter> <name>` |
| sprint | `plans/<letter><n>-<slug>.md` — `Sprint: <letter><n> · <name>`, `Status: draft / open / done`, `When: <start> → <end>`, `Goal:`, then one section per task. `/sprint draft <name>` writes one from the top of § Next |
| sprint backlog, board | the sprint's task sections; while it is open, its board lines live in [../STATUS.md](../STATUS.md) and nowhere else |
| drafts | the sprints still `Status: draft`, listed in `STATUS.md` § Drafts in order. **The first is the next sprint by default**; `/sprint open` takes it |
| daily | the first message of a session: the board, what is next, what is blocked |
| review | `/sprint close` — every line `[x]` or `[-]`, the file goes `done`, the lines move under `STATUS.md` § Release |
| ship | `/release close` — every done sprint of the letter becomes one changelog block, then the tag |
| retrospective | the `est → act` pairs, summed at ship; the band recalibration in `/sprint draft` |
| the board, anywhere | `/board` — `scripts/board.py` reads these files and the result is written to the board artifact (D67), so a phone or another session sees the same state without the repository |

A sprint's timebox is one agent-day — 100 points — unless the `When:` line says otherwise. A task
still open when it ends goes back to § Next with its `(was F1X1)`, and the next draft takes it or
deletes it; nothing is carried forward by default (D62). A release with no open sprint and no draft
is where "what can we do today" starts: `/sprint draft`.

## One agent

**One agent works a sprint, top to bottom.** A second is the owner's call, written in the sprint's
`Agents:` line, and only for a sprint whose tasks touch disjoint files — the § Files table says
which. Never more than three, and never one by reflex: every agent is a full context, roughly 100k
tokens read before its first edit. The same holds inside a session — do the work in the session; no
subagents and no workflow scripts unless the owner asks for them by name (D61).

## Ids

`<Release letter><Sprint digit><Kind letter><Seq digit>` — `A1U1`, `A0X1`, `F1H1`. Pattern
`^[A-Z][0-9][UXTHPS][1-9]$`, checked by `doctor.py`.

| Part | Means |
|---|---|
| Release letter | the file `plans/<letter>.md`; the tag is chosen at ship and bound in `../CHANGELOG.md` |
| Sprint digit | `1`–`9` = the sprint within the release. `0` = a chore or hotfix outside any sprint, which jumps the queue and may ship as a patch tag |
| Kind letter | `U` UI and design system · `X` fix · `T` trim · `H` harden · `P` platform (build, CI, scripts, docs, process) · `S` showcase |
| Seq digit | `1`–`9` within one (release, sprint, kind). A tenth means the sprint is too big |

An id appears in the board line, the `### <id> <title>` section, the commit title, the changelog's
`Tasks:` line and any decision that cites it. **Never in source.** A carried-over item keeps its
old id once, as `(was D1X4)`. Find one: `grep -rn '\bA1U1\b' docs .github` ·
`git log --oneline --grep='^A1U1 '`. Releases A–E predate sprints: their digit was a lane, a
file-disjoint group of tasks, and their plan files hold the tasks directly.

## Points

**100 points = one agent-day = 8 hours**, from `/task` to the pull request being open. It covers
T0, T1 and one round of CI fixes, but not waiting for CI. A point is five minutes.

| Band | Means |
|---|---|
| 3 | one file, a flag, a documentation row |
| 6 | one module changed **with its test**, or a new check and the code it forces. A fix across two or three files that adds no test is a 3 |
| 12 | a screen with both its tests, a data layer with its store, or a refactor that rewrites every module's build. Two to four modules with no screen and no new test is a 6 |
| 25 | three or more screens sharing a data layer, or a CI job with the code it exercises. A list, an editor and a detail were a 12 each in E |
| 50 | a feature slice. The ceiling — anything larger is split before it gets an id |

A sprint is as big as its work and never padded to its timebox. Inside a sprint: tasks that write
shared files first, then dependencies, then the largest. A hotfix to a shipped release keeps that
release's letter — `A0X1` — ships as a patch tag and gets a patch block in the changelog.

**Calibration.** The agent appends `· est → act` to its board line; the ship task writes
`Estimate · Actual · Ratio` into the changelog block. A band off by 30 % on three tasks gets its
description rewritten; the numbers never change.

## Task loop

1. Read `CLAUDE.md` (already loaded), this file once per session, then the sprint's opening.
2. `/task <id>` takes the first `[ ]` on the board whose `Depends` is settled. The branch is the
   sprint's — `<letter><n>-<slug>`, made from a fresh `origin/main` on the sprint's first task —
   and every task of the sprint is one commit on it.
3. A `Decide first` line is settled before the code, as a row in [../DECISIONS.md](../DECISIONS.md)
   under the pre-assigned number, in the same commit.
4. Use the generators for any new module, screen, component or data source.
5. T0 once or twice while working. Never the whole gate in the loop, never `./gradlew build`.
6. A fact you changed moves to its one file in the same commit (§ Which doc changes when).
7. Work you find on the way is one line in [../BACKLOG.md](../BACKLOG.md), never an edit to the
   sprint. A brief that turns out wrong: build what is right, say so in the pull request, and the
   owner amends the brief.
8. Flip your board line to `[x]` with `est → act` in the task's own commit, titled `<id> <title>`,
   carrying the code, the docs and the board line together. Then `/board`.
9. `/check pr` once per sprint, after `git rebase origin/main`; paste its tail into the pull-request
   body. Open the pull request with the template on the sprint's first task and push every later
   task onto it. Do not wait for CI: check `gh pr checks` between tasks, keep it green, and never
   weaken a check. `gh pr merge --rebase --delete-branch` once the sprint is done and green —
   `main` stays one commit per task (D17).
10. With more than one agent, touch only the files your tasks own (§ Files); one that needs
    another's file finishes what it can, says so on the pull request and takes the next task.
    Alone, there is nothing to arbitrate.

## States

| Board line | Means | Written by |
|---|---|---|
| `- [ ] A1U1 … · 12` | open | the planner |
| `- [ ] A1U1 … · 12 · blocked: <≤5 words>` | cannot start here; the agent took the next task | the agent |
| `- [x] A1U1 … · 12 → 15` | done, with the actual | the agent, in the task's own commit |
| `- [-] A1U1 … · 12 · dropped: <why>` | dropped | the owner or the ship task |

There is no "doing" state: a board is worked top to bottom, so the first `[ ]` is the one in hand.
A sprint is `Status: draft` → `Status: open` (`/sprint open`, the owner's word) → `Status: done`
(`/sprint close`). **Exactly one sprint is open**, and one release: a release is `Status: open`
from `/release open` until `/release close` writes `shipped as v<x.y.z>`.

## Files

Every sprint lists what each task writes, so a second agent could take a task without re-reading
the code. The files two tasks most often want: `settings.gradle.kts` · `core/di/**` ·
`app/**/AppNavHost.kt` · `app/**/KoinGraphTest.kt` · `gradle/libs.versions.toml` · `build-logic/**`
· `scripts/doctor.py` · `CLAUDE.md` · `docs/ai/CODEBASE.md` · `.github/workflows/build.yml`. A
generator edit counts: `create_feature.py` writes four of them.

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

## Drafting a sprint

`/sprint draft <name>`: recalibrate from the shipped `Estimate · Actual · Ratio` first; take
[../BACKLOG.md](../BACKLOG.md) § Next in order until the timebox is full, or the lines the owner
named; write one task section per item — Why, Decide first, Done when, Touches, Read, Steps, Checks,
Depends, a section a cold agent can start from alone; estimate with the bands, split anything over
50; write the `When:`, `Goal:`, `Agents:` lines and the § Files table; assign ids last. The board
lines stay in the draft until `/sprint open` lifts them into `STATUS.md`. A draft the owner
abandons is deleted, and a decision row says why.
