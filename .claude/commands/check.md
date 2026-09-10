---
description: Run the full gate — conventions, generator tests, then the build
allowed-tools: Bash(python3 scripts/*), Bash(./gradlew *), Read, Glob, Grep
---

Run the same gate CI runs, in the same order — conventions first, because they fail in seconds
where the build takes minutes:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py &&
  ./gradlew ktlintCheck && ./gradlew test :app:lintDevDebug :app:assembleDevDebug &&
  ./gradlew verifyRoborazziDebug
```

Named tasks rather than `./gradlew build`: that assembles all six app variants and runs every test
once per variant. This is the list CI runs.

If `doctor.py` fails, fix the convention rather than the check — each one exists because a compiler
cannot catch it. If a check itself looks wrong, say so rather than weakening it silently.

Report what passed and what failed, with the actual output. Do not describe the build as passing
unless it did.
