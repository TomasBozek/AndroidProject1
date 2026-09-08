---
description: Turn this template into a named project (one-time, rewrites the whole repo)
argument-hint: --package com.acme.app --name "My App"
allowed-tools: Bash(python3 scripts/*), Read, Glob, Grep
---

One-time, on a fresh clone, before writing any code of your own.

**Always dry-run first** — this rewrites every source file in place:

```bash
python3 scripts/init_project.py $ARGUMENTS --dry-run
```

Show the user what it would change, and only then run it for real. It refuses to run on a dirty
working tree, which is deliberate: `git checkout .` has to stay a usable escape hatch.

Afterwards: `python3 scripts/doctor.py && ./gradlew build`, then replace the sample features
(`delete_feature.py catalog`, and so on). Keep `feature/template` — it is what the generators clone.
