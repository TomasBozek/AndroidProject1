# Plan 4 · the four-worker split

**Round one ran on 2026-09-09 and is merged.** Twenty-six of the twenty-seven items it took
landed on `main`; `ui.2` was parked with its blocker corrected, and `feat.9` and `qa.5` were
never in it. What follows is the plan that produced that round, kept as the record of how the
split was drawn — read the [board](PLAN.md) for what is open now.

How the 30 open items on the [board](PLAN.md) divide between four agents working at once, each
in its own worktree and ending in one pull request, and what the fifth agent that merges them
has to know. The items themselves are not repeated here: every id points at its section in
[PLAN-DETAIL.md](PLAN-DETAIL.md), and the rules in that file's *How to work this plan* apply
unchanged. This file adds only what a worker needs to stay out of the others' way.

## The shape of the split

One item, `qa.13`, moves every file in every presentation module. Anything that edits a screen
in parallel with it merges as a rename-plus-edit at best and a conflict at worst. So the split
is by **directory**, not by track: one worker owns the layout change and everything that has to
follow it inside the presentation modules; the other three own directories that item never
touches. Two items stay out of this round: `feat.9` needs every other screen to exist first, and
`qa.5` needs a person with the physical device.

| Worker | Owns | Items | Load |
|---|---|---|---|
| **1 · layout** | `feature/*/presentation`, `feature/template`, `scripts/create_*.py`, `app/` tests | qa.13 · feat.8 · ui.2 · core.10 · ui.8 · shell.6 | 15 |
| **2 · core & build** | `service/`, `build-logic/`, `scripts/doctor.py`, `scripts/test_scripts.py`, the release job | qa.14 · core.8 · core.9 · qa.8 · qa.7 · qa.12 · core.7 · core.6 · qa.4 | 15 |
| **3 · design system** | `core/ui/`, the Maestro job | ui.9 · ui.4 · ui.3 · ui.7 · ui.5 · qa.11 | 14 |
| **4 · app shell** | `app/` sources, new feature modules, the emulator | shell.2 · shell.3 · shell.5 · feat.4 · shell.1 · shell.4 | 16 |

Load counts an `S` as 1, an `M` as 3 and an `L` as 6. Worker 4 carries the two `device` items
because there is one emulator on this machine; nobody else starts it.

**Before anyone branches**, `main` has to hold what is in the working tree today: the Plan 4
files and the `ui.6` gallery fix. Two commits, in this order, then the four worktrees are cut
from the same `main`:

```bash
git add docs/PLAN.md docs/PLAN-DETAIL.md docs/PLAN-WORKERS.md CLAUDE.md README.md
git commit -m "Plan: Plan 4 — the board, the detail file, the four-worker split"
git add feature/gallery
git commit -m "ui.6 Gallery demos are interactive"
```

Then mark `ui.6` landed on the board in the same commit or the next.

## Rules every worker follows

- **One worktree, one branch, one PR.** `git worktree add ../AndroidProject1-w<N> -b w<N>-<name>`,
  then `cp ../AndroidProject1/local.properties .` before the first Gradle call.
- **One commit per item,** titled `<id> <title>` exactly as the board spells it. The commit body
  says what differs from the Done line, if anything. Do not squash items together.
- **Finish an item or leave it alone.** An item that cannot be completed stays `[ ]` on the
  board, the branch carries no half of it, and the PR body says why.
- **The gate runs once per item, before its commit:**

  ```bash
  python3 scripts/doctor.py && python3 scripts/test_scripts.py && ./gradlew ktlintCheck && ./gradlew build
  ```

  Iterate on the module task in between; do not run the full build thirty times.
- **The plan files are edited additively.** Flip your own lines on the board to `[x] (date)` and
  delete your own sections from the detail file. Do not touch the dashboard, the track rows, the
  total or *Start now* — the merge agent recomputes them once. Do not edit another worker's
  lines.
- **Shared registration files are appended, never rewritten:** `settings.gradle.kts`,
  `core/di/build.gradle.kts`, `core/di/Koin.kt`, `app/AppNavHost.kt`, `app/KoinGraphTest.kt`,
  `gradle/libs.versions.toml`, the module tree in `CLAUDE.md`, `scripts/doctor.py` (new checks go
  at the end of the file). The merge agent expects conflicts in these and resolves them by
  keeping both sides.
- **Do not move or rename a file unless your item says so.** Only worker 1 moves files.
- **Test ids do not change.** Every `testTag` and `screenId` that exists today keeps its name,
  because the Maestro flows and the other workers' tests use them.
- **The PR** is opened with `gh pr create` against `main`, titled
  `Worker <N> · <name>: <ids in order>`, and its body lists each item as landed or not, the gate
  result, and every file outside the worker's own directories that it had to touch. End the body
  with `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.

## Worker 1 · layout

Branch `w1-layout`. Owns every `feature/*/presentation`, `feature/template`, the three
`create_*.py` generators, and `app/`'s tests. Nothing else in this round edits a presentation
module, so this worker can move files freely; in return it must not touch `core/ui/component`
beyond what `ui.8` needs, `service/`, `build-logic/` beyond `ui.2`'s one plugin line, or
`.github/`.

Order, and why:

1. **qa.13 A directory per screen, a file per component** — first, because every later item on
   this branch lives in the new layout. Follow the Done list in the detail file in its order:
   template, generators, moves with `git mv`, the composable split, the two `doctor.py` checks,
   the docs. Keep `GalleryCatalog.kt` where it is — it is neither a screen nor a component.
2. **feat.8 A screen test for every screen** — seven tests in the new directories, then the
   `doctor.py` tightening as the last commit of the item.
3. **ui.2 Screenshot tests with Roborazzi** — the parked branch `ui.2-roborazzi` holds the
   plugin wiring; take it from there and redo it on top of the layout rather than merging the
   branch. The blocker is that previews are `private`; make every `@ScreenPreview` and
   `@ComponentPreview` in the repo `internal`. This is the one item that edits `core/ui` previews
   and one line of `build-logic/`; say so in the PR body.
4. **core.10 Format roles** — the roles land in `:service:core:ui`, which is worker 2's
   directory, but the item is here because it deletes the two `Price.kt` files this branch has
   just moved. Add the `format/` package only; do not touch anything else in `service/`.
5. **ui.8 Icon roles** — the theme role lands in `core/ui/theme`, and every `Icon(...)` call in
   the features reads it. Change the icon-size lines only; worker 3 is editing the same
   components for other reasons.
6. **shell.6 Tests for the app shell** — tests only, in `app/src/test`; no conflict with anyone.

The emulator is worker 4's; verify `ui.2` and `feat.8` with the JVM tasks, which is what they
are for.

## Worker 2 · core and build

Branch `w2-core`. Owns `service/`, `build-logic/`, `scripts/doctor.py`, `scripts/test_scripts.py`
and the release job in `.github/workflows/build.yml`. Never edits a presentation module,
`core/ui/component` or `app/`, with one exception named below.

Order, cheap first so the branch is worth merging early if the day ends:

1. **qa.14 Maestro ids exist in the code** — a new check appended to `doctor.py`.
2. **core.8 Retry with backoff on the client** — `service/network` only.
3. **core.9 Permission helpers tested** — tests in `service/core/ui`.
4. **qa.8 Compose compiler metrics** — `build-logic/` only.
5. **qa.7 `resourcePrefix` per feature** — `build-logic/` and one line in `core/ui`'s build
   file; the existing strings already comply.
6. **qa.12 Version and release notes from the tag** — `ProjectConfig` and the release job.
7. **core.7 Room migrations are tested** — the Room convention plugin, and migration tests in
   `feature/catalog/data` and `feature/cart/data`. Those are `data` modules, which worker 1 does
   not move; still, add files, do not reshape.
8. **core.6 Analytics seam** — the exception: it adds one call inside `AppScaffold` in
   `core/ui/component`. Make it one line, and say so in the PR body; worker 3 owns that file.
9. **qa.4 Generator output compiles in CI** — last, because it exercises the generators worker 1
   is changing; write it against today's generators and let the merge agent re-run it after
   worker 1 lands.

`doctor.py` will conflict on merge with worker 1's two checks and, possibly, worker 3's; that
is expected and is why new checks go at the end of the file.

## Worker 3 · design system

Branch `w3-ui`. Owns `core/ui/` and the Maestro job in `.github/workflows/build.yml`. Edits a
presentation module in exactly two places, both named below; otherwise never.

Order:

1. **ui.9 Contrast is asserted** — a JVM test in `core/ui`; the first item because it may find
   pairs that the later items should fix.
2. **ui.4 Window size class drives density** — `core/ui/theme` only, including the mouse density.
   Create the tablet AVD profile for the preview check, but do not start it while worker 4 has
   the phone AVD running; a preview is enough to verify this one.
3. **ui.3 Component behaviour tests** — tests in `core/ui`, found by tag.
4. **ui.7 Components match the design's component document** — closes the gap rows of the
   table in the detail file, inside `core/ui/component`, and adds the missing variants to
   `feature/gallery/presentation/…/GalleryCatalog.kt`. That file is the first of the two
   presentation edits; worker 1 leaves it in place, so it merges cleanly. Leave every `Icon`
   size argument exactly as it is — worker 1 is changing those lines in `ui.8`.
5. **ui.5 List–detail for the catalog on wide screens** — the second presentation edit: the
   scene strategy in `app/AppNavHost.kt` and metadata on the two catalog destinations. Worker 1
   moves those destination files; edit them where they are today and expect the merge agent to
   re-apply the hunks in the new location. Keep the edit small: metadata on the keys, nothing
   structural.
6. **qa.11 Maestro flows in CI** — the workflow job. To verify it on the PR, trigger the job on
   `pull_request` while the PR is open and switch it to the schedule and `workflow_dispatch` in
   the item's final commit.

## Worker 4 · app shell

Branch `w4-app`. Owns `app/` sources, the new feature modules it creates, and the emulator.
Edits existing presentation modules as little as possible — a settings list entry and a
navigation lambda, nothing more — and never moves a file.

Two rules specific to this worker, both because worker 1 is changing the generators underneath
it:

- **Generate new screens straight into the D34 layout.** After `create_feature.py`, `git mv` the
  screen's eight files into a sub-package named after the screen and add
  `import <base>.feature.<name>.presentation.R` to the files that use `R`; for a second screen
  use `create_screen.py … --sub <screen>`. A feature-private composable goes into the module's
  `component/`, one file each, with a `@ComponentPreview`. Worker 1's new `doctor.py` checks will
  run against these modules after the merge, and they should pass without a move.
- **Edit `SettingsScreen.kt` and `SettingsViewModel.kt` additively** — new lines, no
  reformatting — so the rename worker 1 performs on them still auto-merges.

Order, with the two device items last so the emulator is started once:

1. **shell.2 Debug menu, dev and staging only** — a new `:feature:devmenu`; the gallery entry
   moves from Settings to the debug menu.
2. **shell.3 Theme setting** — Settings gains `domain` and `data` layers with `--layers
   domain,data --force`; the switch is read at the root in `MainActivity`.
3. **shell.5 Onboarding flow** — a new `:feature:onboarding`; the `AppPager` it needs is added
   to `core/ui` through `create_component.py` and is the one file this worker adds under
   `core/ui`. Say so in the PR body.
4. **feat.4 Search** — a new screen in `:feature:catalog`, generated with `--sub search`, plus
   the recents data source with `create_datasource.py`.
5. **shell.1 Deep links** — start the emulator here; cold and warm checks by `adb`.
6. **shell.4 Notification tap-through** — needs `shell.1` and `shell.2` from this same branch.

## The merge agent

Runs after all four PRs are open. Merges in this order, rebasing each branch onto the current
`main` before its rebase-merge, and runs the gate after every merge, not only at the end:

1. **Worker 1 · layout.** First, because the other three's conflicts are easiest to resolve
   against the new layout rather than the old one.
2. **Worker 2 · core and build.** Expected conflicts: `scripts/doctor.py` (three sets of appended
   checks — keep all), `scripts/test_scripts.py` (worker 1's generator tests and `qa.4`'s
   `--with-gradle`), `CLAUDE.md` (worker 1's screen table and worker 2's API-table rows),
   `build-logic/` (`ui.2`'s plugin line beside `qa.7`, `qa.8`, `core.7`), the two plan files.
   After the merge, re-run `qa.4`'s `--with-gradle` against the merged generators.
3. **Worker 3 · design system.** Expected conflicts: `core/ui/component/*` where `ui.8`'s icon
   lines and `ui.7`'s component changes meet — take both; `AppScaffold` where `core.6` added its
   line; the two catalog destination files, which worker 1 moved — re-apply `ui.5`'s metadata
   hunks in `catalog/presentation/products/` and `productdetail/`; `app/AppNavHost.kt` imports.
4. **Worker 4 · app shell.** Expected conflicts: every registration file (append both sides);
   `SettingsScreen.kt` and `SettingsViewModel.kt`, moved by worker 1 and edited here — re-apply
   in `settings/presentation/settings/`; `MainActivity` and `MainViewModel` where `shell.3` and
   `shell.5` both add state. Then run `doctor.py`: the new modules must pass worker 1's layout
   checks, and any that do not get the same `git mv` treatment.

After the fourth merge: run the gate once more and `./gradlew koverXmlReport`; recompute the
board's track rows, total and *Start now*; write the new coverage number with the commit hash;
delete any landed section a worker left in the detail file; and put `feat.9` and `qa.5` at the
top of *Start now* as the next round. One commit, `Plan: round one merged`.

If a worker's PR body reports an item not finished, leave its board line `[ ]` and its detail
section in place, and say so in the plan commit — do not try to finish it during the merge.
