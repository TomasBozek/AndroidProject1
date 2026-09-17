---
description: Start a task from the open release plan
argument-hint: <id>, e.g. F1X1 — or nothing, to take the next one on the board
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/*), Bash(./gradlew *), Read, Edit, Write, Glob, Grep
---

Start `$ARGUMENTS`. The rules are `docs/ai/PROCESS.md`; read it once per session, not once per
task. One agent works the sprint, top to bottom — do not spawn subagents or workflows for it.

1. `git fetch origin`. Open the sprint under `docs/ai/plans/` whose header says `Status: open`;
   its board is `docs/STATUS.md`. Find the board line and the `### <id> <title>` section. With no
   argument, take the first `[ ]` line on the board.
2. Refuse to start if: the line is already `[x]`, the header still says `Status: draft`, or the
   task's `Depends` is a line that is still `[ ]`. In the last case take the next task instead.
3. The branch is the task's — `feature/<id>-<slug>`, two or three words from the title,
   lowercase, e.g. `feature/F3P4-pre-commit-hook` — created from a fresh `origin/develop`
   (gitflow, D74; `docs/ai/PROCESS.md` § Branches). A sprint-0 chore is the same shape; a hotfix
   to a shipped release is `hotfix/<id>-<slug>` from `origin/main`.
4. Restate, in one message: the **Why**, the **Done when**, the **Touches** set, and the **Checks**
   line. Open the files under **Read** before writing anything.
5. If the task has a **Decide first** line, settle it first and write the row into
   `docs/DECISIONS.md` under the number the plan pre-assigned. That row is part of this commit.

Then work it:

- Use the generators for any new module, screen, component or data source. Never hand-write the
  files they produce.
- `/check` once or twice while working. Never the whole gate in the loop.
- With more than one agent on the sprint, touch only the files your task owns (the sprint's
  § Files); if you need one you do not own, finish what you can, say so on the pull request, and
  take the next task. Alone, there is nothing to arbitrate.
- A fact you changed moves to its one doc in this same commit — `docs/ai/PROCESS.md` § Which doc
  changes when says which.
- A brief that turns out wrong: build what is right, say so in the pull request. Work you find on
  the way is one line in `docs/BACKLOG.md`, never an edit to the plan.

Finish it:

- `git rebase origin/develop`, then `/check pr` — once per task, before its pull request.
- Flip your board line to `[x]` and append `· <est> → <actual>`, where actual is your elapsed
  minutes divided by five, rounded to the nearest band (3, 6, 12, 25, 50).
- **One commit**, titled `<id> <title>` exactly as the board spells it, carrying the code, the docs
  and the board line together.
- `gh pr create --base develop` with the template, titled `<id> <title>`; paste the `/check pr`
  tail into it. When the tail is green, `gh pr merge --merge --delete-branch` — a merge commit,
  never a rebase or a squash — then `git checkout develop && git pull`. When the board is all
  `[x]`, `/sprint close`.
- **Read why a job is red before calling it red.** `gh pr checks` prints the same `fail` for a
  build that broke and for a run GitHub refused before a job started — four pull requests merged
  on the second. One command prints, per job, its name, its conclusion and what GitHub said:

  ```bash
  run=$(gh pr checks --json link --jq '.[0].link' | sed -E 's#.*/runs/([0-9]+)/.*#\1#'); gh run view "$run" --json jobs --jq '.jobs[] | "\(.name) \(.conclusion) \(.databaseId)"'; for job in $(gh run view "$run" --json jobs --jq '.jobs[] | select(.conclusion=="failure") | .databaseId'); do gh api "repos/{owner}/{repo}/check-runs/$job/annotations" --jq '.[].message'; done
  ```

  An annotation that says *The job was not started* is not a failure: say `not started: <reason>`
  in the pull request's Checks line and to the owner, and do not merge on it. A conclusion of
  `failure` with a real annotation is red, and the fix is yours.
- `/board`, so the artifact shows the line you flipped.
