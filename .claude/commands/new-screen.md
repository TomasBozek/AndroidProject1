---
description: Add a screen to an existing feature, optionally one that takes route arguments
argument-hint: <feature> <ScreenName> [--with-args 'id:String'] [--sub subpackage]
allowed-tools: Bash(python3 scripts/*), Read, Edit, Glob, Grep
---

Add a screen with `scripts/create_screen.py`. It writes the seven-file unit, merges the strings,
registers the ViewModel in the feature's Koin module and the destination in `AppNavHost.kt`.

```bash
python3 scripts/create_screen.py $ARGUMENTS
```

**If the screen takes route arguments, pass `--with-args`** — for example
`--with-args 'productId:String,rating:Int'`. It generates the `@Serializable data class` route key,
a ViewModel taking that key as a constructor parameter, the `parametersOf(key)` hand-over in the
destination, a plain JVM test and the `KoinGraphTest` entry that stops Koin's `verify()` calling the
key a missing definition. Never hand-convert a `data object` route into a `data class`: doing that
is what produced a screen loading from a `LaunchedEffect` instead of from its route key, which
re-fires on recomposition and restores nothing after process death.

Then:

- Replace the placeholder state and its `PREVIEW`.
- For the call that *loads* the screen, use `execute(errorDisplay = ErrorDisplay.Inline)` — it shows
  a retryable message in place of the content, and `BaseViewModel` re-runs the failed call for you.
  Keep the default `Alert` for a call the user triggered on a screen that is already drawn.
- Never write try/catch, and never touch a loading flag.

Finish with `python3 scripts/doctor.py && ./gradlew build`.
