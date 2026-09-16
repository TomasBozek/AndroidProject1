---
description: Run the checks for where you are — T0 while working, T1 before the pull request
argument-hint: [pr]
allowed-tools: Bash(python3 scripts/doctor.py), Bash(python3 scripts/test_scripts.py), Bash(./gradlew *), Bash(git diff *), Read, Glob, Grep
---

The tiers are defined in `CLAUDE.md` § Checks. This command runs one of them; it does not define
them, and it never runs `./gradlew build`.

**No argument — T0, the loop.** Once or twice while working, not after every edit:

```bash
python3 scripts/doctor.py && ./gradlew ktlintCheck
```

If `doctor.py` prints `[note] the pre-commit hook is not installed on this clone`, run the command
it names first — `git config core.hooksPath .githooks` — and delete a stale `.git/hooks/pre-commit`
if one is there; the committed hook is the one that runs `doctor.py` before every commit.

Then the touched module's own tests, e.g. `./gradlew :feature:cart:presentation:test`.

**`pr` — T1, once, after rebasing on `origin/main`.** Derive what to run from the diff:

```bash
git diff --name-only origin/main...HEAD
```

- Always: `python3 scripts/doctor.py`, `./gradlew ktlintCheck :app:assembleDevDebug`, and `test` for
  every module whose `src/main` changed.
- A path under `*/presentation/src/main`, `core/ui` or `service/core/ui` also means
  `./gradlew verifyRoborazziDebug`. If a preview changed on purpose, `recordRoborazziDebug` first
  and **open the images it wrote** — a golden nobody looked at is a test that passes forever.
- A path under `scripts/`, `feature/template/`, `.claude/commands/` or `docs/ai/CODEBASE.md` also
  means `python3 scripts/test_scripts.py`.
- A path under `build-logic/`, `gradle/`, `service/` or `core/` also means the whole `./gradlew test`.

Report what passed and what failed, with the actual output, and say which conditional steps you
skipped and why — that is what goes in the pull request body. Do not describe a run as passing
unless it did.

If `doctor.py` fails, fix the convention rather than the check: each one exists because a compiler
cannot catch it. If a check itself looks wrong, say so rather than weakening it silently.
