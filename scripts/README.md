# scripts/

Generators and checks for this template. **Prefer them to writing files by hand** — most of them
perform registrations that a manual copy silently skips, and `doctor.py` fails on every one you
miss.

Every script takes `--dry-run`. Every script explains itself with `--help`, including worked
examples. In Claude Code the common ones are also slash commands (`/new-feature`, `/new-screen`,
`/new-component`, `/new-datasource`, `/check`, `/rename-project`).

## Once per clone

```bash
git config core.hooksPath .githooks
```

Points Git at the committed `pre-commit` hook, which runs `doctor.py` — silent when it passes,
loud when it does not — and `test_scripts.py` too, but only on a commit that touches `scripts/`.
`doctor.py` prints a `[note]` until it is set, `init_project.py` sets it on the clone it rewrites,
and `/check` runs it when the note appears.

## Once per project

```bash
python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker" --dry-run
```

Rewrites the base package across every source file, moves the package directory in all 38 source
sets, and renames the Gradle project, the Android theme, the launcher label and `_common.py`. Run
it on a fresh clone before writing code of your own; it refuses a dirty working tree so
`git checkout .` stays an escape hatch.

## Day to day

| Script | What it does | Registers |
|---|---|---|
| `create_feature.py` | Clones `feature/template` into a new feature | `settings.gradle.kts`, `core/di/build.gradle.kts`, `Koin.kt`, `AppNavHost.kt` |
| `create_screen.py` | The eight-file screen unit, in a directory of its own, in an existing feature | the ViewModel in the feature's Koin module, the destination in `AppNavHost.kt`, its strings in every locale |
| `create_component.py` | A Compose component and its preview, in `:core:ui` or a feature's `component/` | nothing — a component needs none |
| `create_datasource.py` | Data source across `data`, optionally its repository in `domain` | the Koin bindings |
| `delete_feature.py` | The inverse of `create_feature.py` | undoes all five |
| `doctor.py` | the checks a compiler cannot make; `--list` names them | — |
| `board.py` | The sprint, drafts, releases and backlog as one JSON document, read from `docs/`; `/board` writes it to the board artifact | — |
| `test_scripts.py` | Tests for everything above; `--with-gradle` also compiles a generated feature | — |
| `export_service.py` | Copies `service/` and `build-logic/` into a *different* project | prints the `settings.gradle.kts` block |

### The two flags worth remembering

**`create_screen.py --with-args`** for any screen that takes route arguments:

```bash
python3 scripts/create_screen.py catalog ProductReview --with-args 'productId:String,rating:Int'
```

It generates the `@Serializable data class` route key, a ViewModel taking that key as a
constructor parameter, the `parametersOf(key)` hand-over in the destination, a plain JVM test and
the `KoinGraphTest` entry that stops `verify()` calling the key a missing definition. Do not
hand-convert a
`data object` route into a `data class` afterwards — that is what produced a screen loading from a
`LaunchedEffect` instead of from the key it was handed, which re-fires on recomposition and
restores nothing after process death.

**`create_screen.py --sub`** when the screen's own name makes a poor directory. A screen lands
in `presentation/<screen name, flat lowercase>/` by default; `--sub search` puts it in
`presentation/search/` instead. The directory holds that screen's six files and nothing else —
`doctor.py` fails on a stranger in it, and on a composable left in a screen file. Anything that
is not the screen goes to the feature's `component/` with `create_component.py --feature`.

**`create_datasource.py --repository`** when the data source needs one. Both halves of the source
land in `data.source` and the repository in `data.repository`; the only thing above `data` that may
name any of it is the `XRepository` interface in `domain`.

## Before you call the work done

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py &&
  ./gradlew ktlintCheck && ./gradlew test :app:lintDevDebug :app:assembleDevDebug &&
  ./gradlew verifyRoborazziDebug
```

Conventions first — they fail in seconds where the build takes minutes. This is the order CI runs
them in.

## Changing what gets generated

`feature/template` **is** the template. `create_feature.py` and `create_screen.py` clone it and only
rewrite names, so to change the shape of every future feature or screen, edit `feature/template` —
not the scripts. It holds two screens and one component:

- `template/Template*` — the plain screen, cloned by default.
- `templateargs/TemplateArgs*` — the argument-carrying variant, cloned by `--with-args`.
  `create_feature.py` deliberately skips it: a new feature starts with one screen, and copying the
  second would leave an unregistered destination behind.
- `component/TemplateHeadline.kt` — the one feature-local composable both screens compose, cloned
  alongside whichever screen is generated. It is what makes the `component/` package exist in
  every generated feature rather than being remembered.
- `res/values/strings.xml` and `res/values-cs/strings.xml` — the same names in both, because
  `doctor.py` fails on a module that ships one and not the other. Adding a locale means adding its
  file here and its code to `TRANSLATED_LOCALES` in `_common.py`; both generators then write it.

Both are compiled by `./gradlew test` and checked by `doctor.py`, so a broken template fails before
it can generate anything broken.

`create_component.py` and `create_datasource.py` are the exceptions — they generate from templates
held in the scripts, because neither has a counterpart in `feature/template` and adding a fake one
would ship a placeholder component (or data source) in the app.

`scripts/_common.py` holds the shared naming rules, paths and the idempotent file-editing helpers.
Anything used by two scripts belongs there.

### Renaming caveat

`create_feature.py` and `create_screen.py` use targeted replacements — `feature.template`,
`feature/template`, `Template`, `template` followed by an uppercase letter, and `template_` followed
by a resource name — rather than a blanket rename, so the word appearing in a comment or a string
survives. Preserve that if you edit them; see `_common.py:rewrite_source` and
`rewrite_resource_names`.

`init_project.py` and `export_service.py` are the deliberate exceptions: moving the whole base
package is the point there. Both rewrite the dotted **and** slash-separated forms —
`test_scripts.py` holds the package as a path, and missing that form leaves the suite pointed at a
directory the rename has just emptied.
