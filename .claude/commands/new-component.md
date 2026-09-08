---
description: Scaffold a shared Compose component with its preview
argument-hint: <ComponentName> [--feature <name>] [--state] [--sub subpackage]
allowed-tools: Bash(python3 scripts/*), Read, Edit, Glob, Grep
---

```bash
python3 scripts/create_component.py $ARGUMENTS
```

With no `--feature` the component lands in `:core:ui`, where every feature can reach it. Pass
`--feature` when it belongs to one feature and should not be shared. `--state` adds an `@Immutable`
state class with the `PREVIEW` fixture `doctor.py` requires.

A component needs no registration — no Koin binding, no nav entry — so this script edits nothing
outside the files it writes.

After generating: replace the placeholder content, keep the `modifier` parameter and pass it to the
outermost element (`doctor.py` enforces this), and use `AppTheme.spacing` rather than `.dp` literals.
