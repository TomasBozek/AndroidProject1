---
description: Scaffold a new feature module (full stack, or presentation-only)
argument-hint: <featureName> [--layers presentation,di] [--graph main|auth|none]
allowed-tools: Bash(python3 scripts/*), Read, Edit, Glob, Grep
---

Create a feature with `scripts/create_feature.py`. Do not write the module files by hand — the
script performs five registrations that are easy to miss (`settings.gradle.kts`,
`core/di/build.gradle.kts`, `Koin.kt`, `AppNavHost.kt`, the module tree in `CLAUDE.md`), and
`doctor.py` fails on each one you skip.

```bash
python3 scripts/create_feature.py $ARGUMENTS
```

Run it with `--dry-run` first if the arguments look ambiguous. Then, in order:

1. Replace the placeholder fields in `XState`, its `PREVIEW` and the three states in
   `XStatePreviews` with the real ones. Keep all three — empty and long text are where layouts break.
   `PREVIEW` is not optional — it is the preview fixture and usually the `initialState`.
2. Write `XScreen.kt`. Strings used only in the composable go through `stringResource(...)`; strings
   a ViewModel needs go into the state as `UiText`. Both live in the feature's own `strings.xml`,
   prefixed with the feature name. Lint fails on a hardcoded literal.
3. Add cases to `XEvent`, handle them in `XViewModel.onUiEvent`, and emit `XNavigation` to move on.
   The `onNavigation` lambda in `XDestination.kt` is where an intent becomes a back-stack call.
4. Fill in `XViewModelTest` — it is generated, so it exists whether or not you use it.

Use `--layers presentation,di` when the feature has no data of its own. Finish with
`python3 scripts/doctor.py && ./gradlew build`.
