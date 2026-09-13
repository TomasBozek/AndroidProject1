---
description: Start a task from the open release plan
argument-hint: <id>, e.g. E1X1 — or nothing, to take the next one in your lane
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/*), Bash(./gradlew *), Read, Edit, Write, Glob, Grep
---

Start `$ARGUMENTS`. The rules are `docs/ai/PROCESS.md`; read it once per session, not once per
task. One agent works the release, lane by lane — do not spawn subagents or workflows for it.

1. `git fetch origin`. Open the plan under `docs/ai/plans/` whose header says `Status: open`; its
   board is `docs/STATUS.md`. Find the board line and the `### <id> <title>` section. With no
   argument, take the first `[ ]` line in your lane.
2. Refuse to start if: the line is already `[x]`, the header still says `Status: draft`, or the
   task's `Depends` is a line that is still `[ ]`. In the last case take the next task instead.
3. The branch is the lane's — `<letter><lane>-<slug>`, e.g. `E1-core-fixes`, two or three words
   from the lane's title, lowercase. On the lane's first task create it from `origin/main`; on every
   later one switch to it and `git rebase origin/main`.
4. Restate, in one message: the **Why**, the **Done when**, the **Touches** set, and the **Checks**
   line. Open the files under **Read** before writing anything.
5. If the task has a **Decide first** line, settle it first and write the row into
   `docs/DECISIONS.md` under the number the plan pre-assigned. That row is part of this commit.

Then work it:

- Use the generators for any new module, screen, component or data source. Never hand-write the
  files they produce.
- `/check` once or twice while working. Never the whole gate in the loop.
- With more than one agent on the release, touch only the files your lane owns; if you need one
  you do not own, finish what you can, say so on the pull request, and take the next task. Alone,
  there is nothing to arbitrate.
- A fact you changed moves to its one doc in this same commit — `docs/ai/PROCESS.md` § Which doc
  changes when says which.
- A brief that turns out wrong: build what is right, say so in the pull request. Work you find on
  the way is one line in `docs/BACKLOG.md`, never an edit to the plan.

Finish it:

- `git rebase origin/main`, then `/check pr` — once per lane is enough when the tasks are small;
  once per task when one moved a `presentation` module or `build-logic/`.
- Flip your board line to `[x]` and append `· <est> → <actual>`, where actual is your elapsed
  minutes divided by five, rounded to the nearest band (3, 6, 12, 25, 50).
- **One commit**, titled `<id> <title>` exactly as the board spells it, carrying the code, the docs
  and the board line together.
- On the lane's first task `gh pr create` with the template; on every later one push onto the same
  pull request. Do not sit and wait for CI; check `gh pr checks` between tasks and, once the lane is
  done and green, merge with `--rebase --delete-branch`.
