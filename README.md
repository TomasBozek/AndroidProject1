# AndroidProject1

A multi-module Android template: Kotlin, Jetpack Compose, Koin, single activity, Navigation 3.

| Read this | For |
|---|---|
| [CLAUDE.md](CLAUDE.md) | the conventions, in full. Read it before writing code |
| [docs/PLAN.md](docs/PLAN.md) | the board: what is open, what is next, what needs a decision |
| [docs/PLAN-DETAIL.md](docs/PLAN-DETAIL.md) | each open item in full, and how to work the plan |
| [scripts/README.md](scripts/README.md) | the generators, in detail |

## Requirements

| | Version | Note |
|---|---|---|
| JDK | 25 | pinned in `gradle/gradle-daemon-jvm.properties`; Gradle provisions it via foojay on first run, so it needs network once |
| Android SDK | API 37 | `compileSdk` and `targetSdk`; `minSdk` is 29 |
| Python | 3.10+ | the scripts in `scripts/`; standard library only, nothing to install |

Gradle 9.6 comes from the wrapper. Android Studio is optional — everything below runs from the
command line.

## Build and run

```bash
./gradlew build                # compile, lint and unit-test everything
./gradlew :app:installDebug    # install the debug build on a connected device
./gradlew test
./gradlew lint
./gradlew ktlintCheck          # ktlintFormat fixes what it can
./gradlew verifyRoborazziDebug  # the screenshot goldens; recordRoborazziDebug rewrites them
```

One module at a time: `./gradlew :feature:auth:presentation:assembleDebug`.

Before you call a change done, in the order CI runs it:

```bash
python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew ktlintCheck && ./gradlew build && ./gradlew verifyRoborazziDebug
```

`python3 scripts/install_hooks.py` installs the first of those as a pre-commit hook, once per clone.

gate; [CLAUDE.md](CLAUDE.md) lists the two categories of its output that are deliberate here.

## Make it your project

```bash
python3 scripts/init_project.py --package com.acme.tracker --name "Field Tracker"
```

Run once, on a clean tree. It rewrites the base package in every source file, moves the package
directories in all source sets, and renames the Gradle project, the Android theme, the launcher
label and `scripts/_common.py`. `--dry-run` shows the plan; it refuses to run on a dirty tree.

## Versions

All in [gradle/libs.versions.toml](gradle/libs.versions.toml) — never hardcode one in a module
build file. Renovate keeps them current, grouping androidx, Kotlin, Koin and AGP, and skipping
pre-releases.

AGP 9.4.0 · Kotlin 2.4.20 · Compose BOM 2026.08.00 · Koin 4.2.2 · navigation3 1.1.7 · Java target 17.

## Layout

```
build-logic/                    the convention.* plugins — an included build, not a module
service/core/{domain,data,ui}   the reusable architecture — knows nothing about this app
core/{ui,di}                    this app's theme and its single Koin registration point
app                             one activity, AppNavHost, MainViewModel, the bottom-bar tabs
feature/<name>/{domain,data,presentation,di}
```

Inside a `presentation` module, every screen has a directory of its own — named after the screen,
even when the feature has only one — and every composable that is not a screen lives in the
module's `component/`, one file each:

```
feature/catalog/presentation/…/presentation/
    categories/  products/  productdetail/  productpicker/
    component/
```

`service/` is portable: drop the directory into another project, add three `includeServiceModule`
lines, and you have the architecture without this app's identity. `scripts/export_service.py` does
the copy, brings `build-logic/` along and rewrites the package.

A module build file is a `plugins` block and its project dependencies, and nothing else:

```kotlin
plugins { alias(libs.plugins.convention.feature.presentation) }

dependencies {
    api(projects.core.ui)
    api(projects.feature.auth.domain)
}
```

SDK levels, the Java target, lint, the namespace and every shared library dependency live in
`build-logic/`.

Layer direction, enforced by `doctor.py`:

| Layer | May depend on |
|---|---|
| `domain` | `service:core:domain` only — a plain Kotlin/JVM module, so `android.*` will not compile |
| `data` | `service:core:data` + own `domain` |
| `presentation` | `core:ui` + own `domain` |
| `di` | all of the above |

A feature's `presentation` must never depend on another feature's `presentation`.

## Generating code

A screen is eight files in a directory of its own — `XDestination`, `XScreen`, `XState`,
`XEvent`, `XNavigation`, `XViewModel`, `XViewModelTest`, `XScreenTest` — plus six registrations
that are easy to forget. Generate them.

| I need | Command |
|---|---|
| a feature, full stack | `python3 scripts/create_feature.py userProfile` |
| a screen-only feature | `python3 scripts/create_feature.py userProfile --layers presentation,di` |
| another screen | `python3 scripts/create_screen.py userprofile UserProfileDetail` |
| a screen with route arguments | `python3 scripts/create_screen.py userprofile Detail --with-args 'id:String'` |
| a shared UI component | `python3 scripts/create_component.py PrimaryButton` |
| a component one feature needs | `python3 scripts/create_component.py ProductCard --feature catalog` |
| a data source | `python3 scripts/create_datasource.py userprofile LocalUserProfile --repository` |
| to undo a feature | `python3 scripts/delete_feature.py userProfile` |
| to check the conventions | `python3 scripts/doctor.py` |

`--dry-run` works on all of them, and `--help` carries worked examples. In Claude Code they are also
slash commands: `/new-feature`, `/new-screen`, `/new-component`, `/new-datasource`, `/check`,
`/rename-project`.

`feature/template` is what the generators clone. To change the shape of every future feature, edit
that module rather than the scripts.
