---
description: Draft the next sprint from the backlog, open the first draft, or close the open sprint
argument-hint: draft <name> [lines…] | open | close
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/doctor.py), Bash(python3 scripts/board.py), Read, Glob, Grep, Edit, Write
---

`$ARGUMENTS`. The rules are `docs/ai/PROCESS.md` § Sprints and releases and § Drafting a sprint.
A sprint file is `docs/ai/plans/<letter><n>-<slug>.md` from `docs/ai/plans/TEMPLATE.md`; the
letter is the open release's, `n` the next free digit under it, the slug the name in lowercase
with hyphens.

## `draft <name> [lines…]`

Refuse if no release is `Status: open` — `/release open <letter> <name>` first.

1. Recalibrate: read the shipped blocks' `Estimate · Actual · Ratio` in `docs/CHANGELOG.md`. A band
   off by more than 30 % on three or more tasks gets its description in `docs/ai/PROCESS.md`
   rewritten; the numbers never change.
2. Take the candidates: the lines the owner named, else `docs/BACKLOG.md` § Next in order until
   the timebox is full — 100 points unless the owner said otherwise. An improvement sprint takes
   § DevOps instead. Remove each line from the backlog in the same edit.
3. Read before writing: `python3 scripts/doctor.py --list` (the checks a *Done when* has to pass),
   `CLAUDE.md` § Test identifiers (an id a brief names comes from that vocabulary — F1H1's
   `main_offlineBanner` did not, and the task found out at T0), and the last row of
   `docs/DECISIONS.md` (the next free `D<n>` — F1H1 pre-assigned one F1P1 had taken). Then one
   task section per candidate — Why, Decide first, Done when, Touches, Read, Steps, Checks,
   Depends. **Done when** is commands and greps, never a sentence. **Steps** name real paths, the
   generator to call and the doc row to update. A section a cold agent cannot start from alone is
   not finished.
4. Estimate with the bands; split anything over 50 before it gets an id.
5. Order: tasks that write shared files first, then dependencies, then the largest. Fill § Files.
6. Write `Sprint:`, `Status: draft`, `When:` (the owner's dates, else the next working day 09:00 →
   18:00), `Goal:`, `Release:`, `Agents: 1 · <pts> points`, and `Decisions pre-assigned: D<a>–D<b>`
   from the next free number after the last row — **and extend the release file's
   `Decisions pre-assigned:` line to `D<b>` in the same edit**; `doctor.py` fails on a sprint whose
   range lies outside its release's. Assign task ids **last**.
7. The board lines go under the draft's `## Board`. Add the draft to `docs/STATUS.md` § Drafts, in
   queue order — the first is the next sprint. Run `python3 scripts/doctor.py`, then `/board`.

## `open`

Refuse if a sprint is already `Status: open`. Take the first draft in `docs/STATUS.md` § Drafts
(or the one the owner named): flip its header to `Status: open`, cut its `## Board` out of the file
and into `docs/STATUS.md` § Board with the sprint's header line above it, remove it from § Drafts,
and add its digit to the release file's `Sprints:` line. The first `/task` carries the edit in its
commit. `/board`.

## `close`

Refuse unless every board line is `[x]` or `[-]` and every task's pull request is merged into `develop`.

1. Flip the header to `Status: done <date>`. Append `## Retrospective` to the sprint file: three
   lines — what the briefs got wrong, what the checks missed, one thing to change next sprint.
2. Audit the process against its own docs. The list is fixed; run every item, and every miss is
   one line in `docs/BACKLOG.md` § DevOps (kind `P`, group `git`, `ci`, `release`, `claude` or
   `process`) — never a fix made on the spot, and never an item skipped because it passed last
   time:
   - **the hook** — `git config core.hooksPath` prints `.githooks`. A miss is anything else; an
     old `.git/hooks/pre-commit` that still runs is the same miss, only quieter.
   - **every merge on a green local gate** — CI is off (D73), so the record is the pull request:
     `gh pr view <n> --json body --jq .body` holds a `/check pr` tail that says `pass` and names
     doctor's count and the Gradle tasks that ran. A miss is a merged pull request whose body has
     no tail, or a tail that names a check as skipped without a reason. While CI is off,
     `gh run list` is not consulted; when it is back, a run whose annotation says the job *was
     not started* is `not started: <reason>`, never `fail`.
   - **a tag for every shipped block** — `git tag -l 'v*'` against `grep '^## v[0-9]'
     docs/CHANGELOG.md`. A miss is a heading with no tag of the same version.
   - **the board republished after the last merge** — `/board read`, then `syncedAt` against
     `git log -1 --format=%cI origin/develop`. A miss is a `syncedAt` older than the merge.
   - **`est → act`** — every pair on the board whose `act / est` is outside 0.7–1.3 is named in
     the retrospective's first line; three such pairs in one band across the release is a miss:
     `the <n> band is rewritten`, and `/sprint draft` does it.

   End by printing § DevOps's point total —
   `python3 scripts/board.py | python3 -c "import json,sys; print(sum(i['pts'] or 0 for i in json.load(sys.stdin)['backlog']['devops']))"`
   — as `DevOps holds <n> points; an improvement sprint is due at 50`.
3. Move the board lines from `docs/STATUS.md` § Board to § Release `<letter>`, under a
   `**<letter><n> · <name>** — done <date> · <est> → <act>` line. § Board says no sprint is open
   and names the first draft.
4. Ask the owner, in one line: **ship now (`/release close`) or draft the next sprint into
   release `<letter>` (`/sprint draft`)?** The default is the next sprint; do not ship unasked.
5. `python3 scripts/doctor.py`; the edit rides the next commit on `main` — a ship, or the next
   sprint's first task. `/board`.
