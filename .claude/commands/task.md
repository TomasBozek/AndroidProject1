---
description: Start a task from the open release plan
argument-hint: <id>, e.g. A1U1 — or nothing, to take the next one in your lane
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/doctor.py), Read, Glob, Grep
---

Start `$ARGUMENTS`. The rules are `docs/ai/PROCESS.md`; read it once per session, not once per
task.

1. `git fetch origin && git switch main && git pull --ff-only`.
2. Open the plan under `docs/ai/plans/` whose header says `Status: open`. Find the board line and
   the `### <id> <title>` section. With no argument, take the first `[ ]` line in your lane.
3. Refuse to start if: the line is already `[x]`, the header still says `Status: draft`, or the
   task's `Depends` is a line that is still `[ ]`. In the last case take the next task instead.
4. `git switch -c <id>-<slug>` — the slug is two or three words from the title, lowercase. If the
   open plan ships as one pull request (D47), that branch already exists: switch to it and skip
   step 1's `main`.
5. Restate, in one message: the **Why**, the **Done when**, the **Touches** set, and the **Checks**
   line. Open the files under **Read** before writing anything.
6. If the task has a **Decide first** line, settle it first and write the row into
   `docs/DECISIONS.md` under the number the plan pre-assigned. That row is part of this commit.

Then work it:

- Use the generators for any new module, screen, component or data source. Never hand-write the
  files they produce.
- `/check` once or twice while working. Never the whole gate in the loop.
- Touch only the shared files your lane owns. If you need one you do not own, stop, say so on the
  pull request, and take the next task.
- A fact you changed moves to its one doc in this same commit — `docs/ai/PROCESS.md` § Which doc
  changes when says which.

Finish it:

- `git rebase origin/main`, then `/check pr`.
- Flip your board line to `[x]` and append `· <est> → <actual>`, where actual is your elapsed
  minutes divided by five, rounded to the nearest band (3, 6, 12, 25, 50).
- **One commit**, titled `<id> <title>` exactly as the board spells it, carrying the code, the docs
  and the board line together.
- `gh pr create` with the template, then start the next task. Do not sit and wait for CI; check
  `gh pr checks` between tasks and merge with `--rebase --delete-branch`. Under D47 the pull request
  is created on the first task only, and nothing merges until the release ships.
