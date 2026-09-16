# Sprint F2 · One backlog, one board

Sprint: F2 · One backlog, one board
Status: done 2026-09-16
When: 2026-09-15 15:35 → 2026-09-16 18:00
Goal: every idea is one line of one shape, and the board opens on the state of the project
Release: F
Agents: 1 · 27 points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D69–D70

The first improvement sprint, drafted from [../../BACKLOG.md](../../BACKLOG.md) § DevOps rather
than § Next (D67). F1 gave the project a board and a grouped backlog and left three things half
done: § Someday still holds four ideas per line and no group, so a third of the backlog cannot be
read by flag; the board's head shows the open sprint and nothing else, so with F1 closed it shows
"none"; and the close found by hand what nothing checks — two pull requests merged on a CI run
that never started. The six lines that serve that goal are taken and the one about the
retrospective is deleted, because F1P1's `/sprint close` already writes one. What stays in
§ DevOps is the tooling — the tag push, the hook, the launch config, the generator — for a sprint
of its own. No band moves: F1's three 12-point briefs came in at 12, 6 and 12, one off by half,
below the three the rule needs.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

`docs/ai/PROCESS.md` is written by three tasks and is 152 of its 160 lines; each adds a sentence,
none a section. With one agent nothing is arbitrated.

| File group | Task |
|---|---|
| `docs/BACKLOG.md`, `scripts/{board,doctor,test_scripts}.py`, `docs/ai/PROCESS.md` (the product-backlog row), `CLAUDE.md` (one sentence), `docs/DECISIONS.md` (D69) | F2P1 |
| `.claude/commands/sprint.md`, `docs/ai/PROCESS.md` (the `review` and `retrospective` rows) | F2P2 |
| `.claude/commands/task.md`, `docs/ai/PROCESS.md` (task loop step 9), `CLAUDE.md` § Checks, `.github/pull_request_template.md` | F2P3 |
| `docs/README.md` § The board, `.claude/commands/board.md`, `docs/DECISIONS.md` (D70); the routine lives outside the repository | F2P4 |
| `docs/ai/board/index.html`, `scripts/board.py` (after F2P1), `scripts/test_scripts.py` | F2P5 |

`settings.gradle.kts`, `core/di/**`, `gradle/libs.versions.toml`, `build-logic/**`,
`.github/workflows/build.yml` and every Kotlin file are untouched: nothing here is app code.

## Tasks

### F2P1 One backlog grammar in every section · 6 · decides D69

**Why** § Someday holds four ideas per line and neither it nor § Behind a decision carries a
group, so `board.py` cannot tag them and the board shows them as prose; a bug has no shape to
arrive in. The owner asked for one backlog, read by flag — layer, module, feature, kind.
**Decide first** `the group is a module path at any depth (feature:auth:data) or a process area,
the kind is the id letter the line will get, and a section says how ready a line is — never what
it touches`, or `a second tag vocabulary beside the module tree` → D69. The first is recommended:
the module tree already exists and `doctor.py` already knows it, so feature, layer and area are
derived from one path and there is no second list to keep in step.
**Done when** every `- ` line in every section of `docs/BACKLOG.md` is
`- <title> · <pts> · <group> [· <kind>] · <why>`, where `pts` is a band or `?`, `?` only under
§ Someday and § Behind a decision; `python3 scripts/board.py` prints `group`, `kind`, `area`,
`feature` and `layer` for every backlog item (`null` where the path has none); `python3
scripts/doctor.py --list` names a check that fails on a line with no group, a kind outside
`UXTHPS`, or a `pts` that is neither a band nor `?`, and `python3 scripts/test_scripts.py` proves
it with one bad line per rule; `grep -c '^- ' docs/BACKLOG.md` is at least 40 — § Someday split one
idea per line; `BACKLOG.md`'s header gives the bug shape — kind `X` and a `seen on <flavor>
<version>, expected <what>, steps <n>` clause — and `CLAUDE.md`'s one sentence on the backlog
points at it; D69 is a row.
**Touches** `docs/BACKLOG.md`, `scripts/board.py`, `scripts/doctor.py`, `scripts/test_scripts.py`,
`docs/ai/PROCESS.md` (the product-backlog row of § Sprints and releases), `CLAUDE.md` (the
`Work you find on the way…` sentence), `docs/DECISIONS.md`.
**Read** `docs/BACKLOG.md` (whole) · `scripts/board.py` § `parse_backlog_item`, `parse_backlog`,
`GROUP` · `scripts/doctor.py`, the check behind `every task id on a board is well formed and used
once` (the shape to copy) · `scripts/test_scripts.py` §
`test_board_reads_the_open_sprint_and_refuses_a_second` · `docs/ai/PROCESS.md` § Ids (the six kind
letters) · `docs/ai/CODEBASE.md` § the module tree (the paths a group may name).
**Steps** 1. D69 as a row. 2. `BACKLOG.md`: rewrite the header to the grammar and the bug shape;
split § Someday one idea per line; give every line in § Someday and § Behind a decision a group
and, where known, a kind; the C arcade line keeps its `git show` pointer. 3. `board.py`:
`parse_backlog_item` reads an optional kind token (`^[UXTHPS]$`) after the group, accepts `?` as
`None` points, requires the group in every section, and derives `area` (first path segment),
`feature` (the segment after `feature:`) and `layer` (a trailing `presentation`, `domain`, `data`
or `di`). 4. `doctor.py`: one check over every `- ` line of `BACKLOG.md`, a message that quotes the
line. 5. `test_scripts.py`: a line with no group fails both scripts; `?` under § Next fails the
check. 6. The product-backlog row in `PROCESS.md` names the grammar in one sentence; `CLAUDE.md`
points at the bug shape.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F2P2 The sprint close audits the process · 3

**Why** The retrospective says why a band missed; nothing says whether the process did what its
docs say. F1 merged two pull requests on a CI run that never started, ran no pre-commit hook on
this clone, and left `v1.2.0` untagged — each of them written down somewhere, none of them checked.
**Done when** `.claude/commands/sprint.md` § `close` has an audit step with a fixed list, each item
a command and what a miss looks like: the hook (`git config core.hooksPath` is `.githooks`), every
merge of the sprint on a run that started and passed (`gh run list --branch <branch>` and the
annotation of a failed job), a tag for every shipped block (`git tag -l 'v*'` against
`CHANGELOG.md`'s headings), the board republished after the last merge (`syncedAt` in the artifact
against the merge time), every `est → act` off by more than 30 %; every miss is one line in
`BACKLOG.md` § DevOps; the step ends by printing § DevOps's point total and `an improvement sprint
is due at 50`; `docs/ai/PROCESS.md`'s `retrospective` row names the three lines and the audit, and
`grep -c '' docs/ai/PROCESS.md` ≤ 160; `python3 scripts/test_scripts.py` passes.
**Touches** `.claude/commands/sprint.md`, `docs/ai/PROCESS.md`.
**Read** `.claude/commands/sprint.md` § `close` · `docs/ai/PROCESS.md` § Sprints and releases (the
`review` and `retrospective` rows) · `docs/ai/plans/F1-dev-menu-and-offline.md` § Retrospective
(what the first close found by hand).
**Steps** 1. The audit as a numbered step between the retrospective and the board move. 2. The
`retrospective` row: ratio, three lines, the audit — one sentence, inside the budget; cut a clause
elsewhere in the table if it does not fit. 3. Nothing in `F1-*.md` changes.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F2P3 A run that never started reads as a failure · 3

**Why** Every run since 2026-09-13 17:46 was refused by GitHub on account billing before a job
started, and `gh pr checks` printed the same `fail` a red build does, so four pull requests
merged on it and nobody read the reason.
**Done when** `.claude/commands/task.md` gives one command that prints, per job, its name, its
conclusion and its annotation text — `gh run view <id> --json jobs` and `gh api
repos/{owner}/{repo}/check-runs/<job id>/annotations` — and tells the agent to say `not started:
<reason>` rather than `fail`; `docs/ai/PROCESS.md` § Task loop step 9 and `CLAUDE.md` § Checks say
`check gh pr checks between tasks and read why a job is red`; `.github/pull_request_template.md`'s
Checks line gains `CI: green | not started (<why>)`; `python3 scripts/test_scripts.py` passes.
**Touches** `.claude/commands/task.md`, `docs/ai/PROCESS.md`, `CLAUDE.md`,
`.github/pull_request_template.md`.
**Read** `.claude/commands/task.md` (the *Finish it* list) · `docs/ai/PROCESS.md` § Task loop
step 9 · `CLAUDE.md` § Checks (the paragraph after the table) · `.github/pull_request_template.md`.
**Steps** 1. The command in `task.md`, tried against run `34974308042` (F1's, refused on billing)
so the printed line is real. 2. The sentence in `PROCESS.md` and `CLAUDE.md`. 3. The template line.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F2P4 The board publishes from a cloud routine · 3 · decides D70

**Why** `/board` needs a session with the repository; a merge on `main` that flips a board line
leaves the artifact stale until someone runs it, and the constraint is fixed — the artifact store is
written through the Artifact tool only, never a workflow step.
**Decide first** `a scheduled cloud routine (/schedule) that checks out main each weekday morning
and runs /board`, or `the artifact is as fresh as the last session, and the page says how old it
is` → D70. Try the first; the second is the fallback and F2P5 draws the age either way.
**Done when** D70 is a row; if the routine: `/schedule` lists it, its first run wrote `board/state`
(the page's footer shows a `syncedAt` no session published), and `docs/README.md` § The board says
who republishes and when; if not: `README.md` says the board is as fresh as the last `/board` and
`.claude/commands/board.md`'s `read` section says to check `syncedAt` first.
**Touches** `docs/DECISIONS.md`, `docs/README.md`, `.claude/commands/board.md`; the routine
itself lives outside the repository.
**Read** `.claude/commands/board.md` · `docs/README.md` § The board · the `schedule` skill's own
instructions, loaded in the session.
**Steps** 1. `/schedule`: a weekday 07:00 routine whose prompt is `/board`, on this repository; if
the routine cannot reach the checkout or the artifact, say so in D70 and take the fallback. 2. D70.
3. The `README.md` sentence and the `board.md` `read` sentence.
**Checks** T0. **Depends** —

### F2P5 The board opens on the state of the project · 12 · after F2P1

**Why** The head shows the open sprint and nothing else, so with F1 closed the board opens on
"none"; the Backlog tab is four headings and a select; no tab shows the previous sprint, the last
shipped version or a decision. The owner opens this from a phone to know where the project is.
**Done when** `docs/ai/board/index.html`'s head carries a strip with the last shipped version and
its date (and `untagged` when `git tag -l` has no such tag), the open release with its done sprints
and their `est → act`, the next draft's name or `none`, and § DevOps's point total; a
*Previous sprint* card under the head with the last done sprint's goal, `est → act` and
retrospective; the Sprint tab reads `No sprint is open — next: <draft>` with that draft's board
when none is open; the Backlog tab is one list behind filter chips — `area`, `feature`, `layer`,
`kind`, `section` — built from the data, multi-select, the chosen chips kept in `localStorage`
inside `try/catch`; a Decisions tab with the last ten rows of `docs/DECISIONS.md`;
`scripts/board.py` prints `tags`, `previous`, `decisions` and `devopsPoints`, and
`python3 scripts/test_scripts.py` asserts all four; the page is republished with `capabilities:
{db: {}}` kept, `/board` run after, and both widths — phone and desktop — opened, one screenshot
per tab in the pull request.
**Touches** `docs/ai/board/index.html`, `scripts/board.py`, `scripts/test_scripts.py`.
**Read** `docs/ai/board/index.html` (whole — `renderHead`, `renderSprint`, `renderBacklog`,
`grouped`, `itemList`, `renderReleases`, `renderFoot`) · `scripts/board.py` § `build`,
`parse_changelog`, `git` · `docs/DECISIONS.md` (the row shape) · the `artifact-design` skill,
loaded before the page is edited · `.claude/commands/board.md` (how the page is republished).
**Steps** 1. `board.py`: `tags` from `git tag -l 'v*'`; `previous` = the done sprint with the
latest date; `decisions` = the last ten `| D<n> | title | text |` rows, text cut at the first
sentence; `devopsPoints` = the sum of § DevOps. 2. The strip in `renderHead` and the card below it.
3. The Backlog tab: one flat list, chips from the union of each field's values, a count per chip.
4. The Decisions tab. 5. Publish the page, run `/board`, open the artifact at phone width and
desktop, and put the screenshots in the pull request.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** F2P1.

## Retrospective

- **What the briefs got wrong.** Two pairs sit outside 0.7–1.3: F2P1 `6 → 3` and F2P5 `12 → 3`.
  F2P1 was sized as "a new check and the code it forces" and was a grammar the backlog already
  half followed; F2P5 was sized as a screen with its tests and was one page's `render*` functions
  and a parser's four fields — a page with no compile step and no golden is a 6 at most. Across
  release F the 12 band has missed twice in four (F1X1 `12 → 6`, F2P5 `12 → 3`); one more and
  `/sprint draft` rewrites it.
- **What the checks missed.** Nothing new — everything it could not see. All five F2 runs and the
  `main` push behind #28 were `not started: billing`; #29 merged on the local gate (doctor 41/41,
  `test_scripts.py` 67/67), which is what F2P3 made sayable. The audit: the hook is still unset
  (`core.hooksPath` empty, the old `.git/hooks/pre-commit` runs), `v1.2.0` is still untagged, the
  board was 16 minutes older than the merge — D70's routine is a morning one, and the close
  republishes. All three already have their § DevOps line.
- **One thing to change.** The owner has said GitHub will not be paid, so "CI runs T2–T4" has been
  false for three days and is now a decision, not an outage — § DevOps, `CI without paid Actions`,
  and the next improvement sprint takes it first. DevOps holds 42 points; an improvement sprint is
  due at 50.
