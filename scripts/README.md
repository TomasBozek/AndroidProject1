# scripts/

Generators and checks for this template. **Prefer them to writing files by hand** — most of them
perform registrations that a manual copy silently skips, and `doctor.py` fails on every one you
miss.

Every script takes `--dry-run`. Every script explains itself with `--help`, including worked
examples. In Claude Code the common ones are also slash commands (`/new-feature`, `/new-screen`,
`/new-component`, `/new-datasource`, `/check`, `/rename-project`).

## Once per clone

```bash
python3 scripts/install_hooks.py
```

Installs a `pre-commit` hook that runs `doctor.py` — silent when it passes, loud when it does not.
Hooks are not version controlled, so this does not arrive with the clone.

## Once per project

```bash
python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker" --dry-run
```

Rewrites the base package across every source file, moves the package directory in all 39 source
sets, and renames the Gradle project, the Android theme, the launcher label and `_common.py`. Run
it on a fresh clone before writing code of your own; it refuses a dirty working tree so
`git checkout .` stays an escape hatch.

## Day to day

| Script | What it does | Registers |
|---|---|---|
| `create_feature.py` | Clones `feature/template` into a new feature | `settings.gradle.kts`, `core/di/build.gradle.kts`, `Koin.kt`, `AppNavHost.kt` |
| `create_screen.py` | The seven-file screen unit in an existing feature | the ViewModel in the feature's Koin module, the destination in `AppNavHost.kt` |
| `create_component.py` | A Compose component and its preview | nothing — a component needs none |
| `create_datasource.py` | Data source across `gateway`/`data`, optionally its repository | the Koin bindings |
| `delete_feature.py` | The inverse of `create_feature.py` | undoes all four |
| `doctor.py` | 16 checks a compiler cannot make | — |
| `test_scripts.py` | Tests for everything above | — |
| `export_service.py` | Copies `service/` into a *different* project | prints the `settings.gradle.kts` block |

### The two flags worth remembering

**`create_screen.py --with-args`** for any screen that takes route arguments:

```bash
python3 scripts/create_screen.py catalog ProductReview --with-args 'productId:String,rating:Int'
```

It generates the `@Serializable data class` route, a ViewModel taking a `SavedStateHandle` and
reading `navArgs<XDestination>()`, and a Robolectric-annotated test. Do not hand-convert a
`data object` route into a `data class` afterwards — that is what produced a screen loading from a
`LaunchedEffect` instead of its `SavedStateHandle`, which re-fires on recomposition and restores
nothing after process death.

**`create_datasource.py --repository`** when the data source needs one. The interface lands in
`gateway` and the implementation in `data`; that inversion is the point of the layer and the thing
most often got backwards by hand.

## Before you call the work done

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew build
```

Conventions first — they fail in seconds where the build takes minutes. This is the order CI runs
them in.

## Changing what gets generated

`feature/template` **is** the template. `create_feature.py` and `create_screen.py` clone it and only
rewrite names, so to change the shape of every future feature or screen, edit `feature/template` —
not the scripts. It holds two screens:

- `Template*` — the plain screen, cloned by default.
- `TemplateArgs*` — the argument-carrying variant, cloned by `--with-args`. `create_feature.py`
  deliberately skips it: a new feature starts with one screen, and copying the second would leave an
  unregistered destination behind.

Both are compiled by `./gradlew build` and checked by `doctor.py`, so a broken template fails before
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
