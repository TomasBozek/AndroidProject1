# Scripts, documentation, Claude Code commands and the day-to-day authoring flow
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

## Summary
The generators are the clear win of this area: create_feature/create_screen plus the registration checks in doctor.py genuinely remove the silent-omission class of bug, and feature/template-as-the-template is the right shape. The cost side has drifted: test_scripts.py takes 4.6 minutes on this machine (not the "~20 s" the docs claim) because it copies the .claude/worktrees directory 56 times; doctor.py spends 11 of its 13 s walking 62,583 files of which 428 matter; and the rulebook has grown to 983 lines with the same command tables and gate line duplicated across six files in three inconsistent versions, while docs/PLAN.md is edited in 100 of 118 commits. The one-time scripts (export_service --sync-versions, install_hooks) are over-built for a template. Plan 5 should spend on speed and subtraction, not on new checks or new plan-process.

## Winners
### create_feature.py / create_screen.py do the registrations a hand copy forgets

Five to six edits (settings.gradle.kts, core/di build file, Koin.kt, AppNavHost.kt, KoinGraphTest, both locales' strings.xml) are the exact things a compiler does not catch until runtime; the scripts do them and the with-gradle CI test proves the output compiles. This is what a template's tooling should be.
Evidence: `scripts/create_feature.py`; `scripts/_common.py:361`; `scripts/_common.py:398`; `scripts/test_scripts.py:881`; `.github/workflows/build.yml:193`
### feature/template is the template, not the scripts

Changing what every future screen looks like is a Kotlin edit that the ordinary build compiles, and the generators only rename. That keeps Python out of the design of the code.
Evidence: `scripts/README.md:76`; `scripts/_common.py:113`
### The registration and translation checks in doctor.py

'every ViewModel is registered', 'every destination is registered', 'every route key is in KoinGraphTest', 'every string ships in every locale' each guard a failure that is silent at compile time and that lint cannot see (a module with no values-cs at all). Cheap, precise, worth keeping.
Evidence: `scripts/doctor.py:313`; `scripts/doctor.py:337`; `scripts/doctor.py:393`; `scripts/doctor.py:415`; `scripts/doctor.py:1133`
### Slash commands with allowed-tools and the follow-up steps

They are thin (17–36 lines) and add what --help cannot: a permission scope and the three things to do after generating. Nothing to change here.
Evidence: `.claude/commands/new-screen.md`; `.claude/commands/new-feature.md`
### The plan rule 'one line per item, delete the section when it lands'

Plan 3's lesson was that landed paragraphs made every session pay to read the plan; Plan 4 applied it and PLAN-DETAIL stayed at 295 lines. Keep this rule; drop the rest of the ceremony (see problems).
Evidence: `docs/PLAN-DETAIL.md:256`; `docs/PLAN-DETAIL.md:23`

## Problems (ranked by the reviewer)
### P1 · test_scripts.py takes 4.6 minutes, not 20 s, and it is in the pre-commit hook and every CI run

**high · inefficient · effort S · confidence 0.95**

Measured: `python3 scripts/test_scripts.py` = 277 s wall (56 tests) on the 10-core machine; CLAUDE.md:854 says '~20s'. Every test's setUp does a full `shutil.copytree(REPO_ROOT, ...)` (test_scripts.py:73); one copy takes 2.3 s and copies 2,856 files of which 2,271 are `.claude/worktrees/*` — the four parallel-worker checkouts — because IGNORED (line 49) excludes build/.gradle/.git/screenshots but not `.claude`. 56 copies is ~130 s alone; the rest is 56 subprocess doctor.py runs inside the copies. This is the suite that the pre-commit hook runs whenever scripts/ is touched and that CI runs on every PR (there it is 35 s because CI has no worktrees, which is why nobody noticed). It also makes 'a fix to doctor.py' a five-minute commit.

**Proposal.** Two edits: add ".claude" (and ".idea", "*.iml") to IGNORED; copy the repo once per class in setUpClass and give each test a cheap restore (git-free `shutil.copytree` from that pristine snapshot, or run each generator in a fresh subdirectory of one copy). Then prune: the ~10 tests that assert doctor.py catches X are duplicated by doctor's own behaviour on the real repo and by the with-gradle test; keep one per script plus the with-gradle compile. Target: under 30 s, ~25 tests, and correct the number in CLAUDE.md.

Evidence: `scripts/test_scripts.py:49`; `scripts/test_scripts.py:69`; `scripts/test_scripts.py:73`; `CLAUDE.md:854`; `.github/workflows/build.yml:53`
### P2 · doctor.py spends 11 of its 13 s walking 62,583 files that are then thrown away

**high · inefficient · effort M · confidence 0.9**

Timed per check (scratch wrapper, repo copy untouched): total 13.4 s, 9.3 s of it system time. Slowest: 'no foreign project identifiers' 3.3 s (`REPO_ROOT.rglob("*")` at line 751 then filtering by suffix), 'Maestro ids' 2.3 s, and five checks at 1.3 s each (modules registered, android config, hardcoded versions, translations, plurals) — each does its own `rglob` over the whole tree. The tree is 62,583 files: 56,096 under build/, 1,771 in .gradle, 2,272 in .claude/worktrees, 1,472 in .git; only 428 .kt files and ~60 build files matter. `skipped_part` (line 69) filters after the walk instead of pruning the walk, so every check pays for Gradle's output. Separately, 'every public composable takes a Modifier parameter' is a 100-line hand-written Kotlin signature parser (lines 544–642) — the one check in the file that is genuinely brittle (annotation-then-`fun` scanning, 60-line lookahead, name-suffix exemptions) and the one that established lint already covers.

**Proposal.** One `os.walk` at startup that prunes `build`, `.gradle`, `.git`, `.claude`, `.idea` at the directory level and builds a shared index (kt files, build files, strings.xml); every check reads from it. That is a sub-second doctor and removes ~15 lines of per-check glob code. Then thin the list: drop 'no foreign project identifiers' (a one-time provenance concern that reads every .md/.xml/.py on every run); replace the Modifier parser with Compose lint — androidx's `ModifierParameter` plus Slack's compose-lints `ModifierMissing`, a large-vendor, build-only dependency that fits D27 — and delete lines 544–642. Keep the registration, translation, layer-direction and screen-unit checks as they are.

Evidence: `scripts/doctor.py:69`; `scripts/doctor.py:80`; `scripts/doctor.py:751`; `scripts/doctor.py:1112`; `scripts/doctor.py:544`
### P3 · The rulebook is 983 lines with the same material in six places, in three different versions

**high · doc-bloat · effort M · confidence 0.9**

Three copies of the generator command table: CLAUDE.md Recipes (495–522), README.md 'Generating code', scripts/README.md 'Day to day'. The 150-line 'Scaffolding scripts' section (CLAUDE.md:713–863) re-describes each script that scripts/README.md (117 lines) and each `--help` (22–33 lines, tested by test_every_script_documents_itself) already describe. The gate command appears six times with three different contents: CLAUDE.md:702 (doctor, ktlint, build, verifyRoborazzi — no test_scripts), README.md:31 (all five), scripts/README.md:94 and check.md:10 (no ktlint, no verifyRoborazzi), PLAN-DETAIL.md:52 and PLAN-WORKERS.md:54 (no verifyRoborazzi). README.md:36 is a dangling fragment ('gate; [CLAUDE.md](CLAUDE.md) lists the two categories of its output') — visible drift. CLAUDE.md was edited in 55 of 118 commits. Every session loads all 983 lines; the Crash reporting, Analytics, Permissions, Nav results, Room and Tabs recipes (CLAUDE.md:423–495, 595–688) are reference material a session needs once a month, not per turn. The module tree in CLAUDE.md is also a machine-edited registration file (_common.py:442 register_in_feature_tree, doctor.py:355 check_feature_tree, ~120 lines of code) — docs that scripts edit and a check enforces is a smell, and settings.gradle.kts already is the list.

**Proposal.** A 300-line CLAUDE.md: Project facts (20), module groups and the two portability rules (40), the eight-file screen unit and MVI conventions (80), the 'API you build on' table (25), the Recipes table pointing at scripts/README.md for detail (20), the single canonical gate (5), Known constraints (30). Move Crash reporting, Analytics, Permissions, Nav results, Room, Tabs and Design-system detail to docs/ARCHITECTURE.md (read on demand; link it once). Delete the 'Scaffolding scripts' section — scripts/README.md and --help own it. Define the gate in exactly one place (a `scripts/gate.sh` or a Gradle `check`-style alias) and have every doc say 'run the gate'. Drop the module tree from CLAUDE.md and delete register_in_feature_tree/unregister_from_feature_tree and check_feature_tree. Fix README.md:36.

Evidence: `CLAUDE.md:495`; `CLAUDE.md:713`; `CLAUDE.md:702`; `README.md:31`; `README.md:36`; `scripts/README.md:94`; `.claude/commands/check.md:10`; `docs/PLAN-DETAIL.md:52`; `docs/PLAN-WORKERS.md:54`
### P4 · The per-screen tax: 8 files + 15 goldens + 2 locales + a 40–90 minute CI build for one screen

**medium · over-enforced · effort M · confidence 0.8**

Walking 'a list screen and a detail screen' end to end: `create_screen.py catalog ItemList` and `create_screen.py catalog ItemDetail --with-args 'id:String'` (good — two commands, all registrations done); then edit State/Screen/ViewModel/Event/Navigation, strings in values and values-cs with four Czech plural forms, the ScreenTest tags; then doctor (13 s), ktlintCheck, `./gradlew build`, then `recordRoborazziDebug` and 'open what record wrote before committing' — the template shows a screen is 3 preview states × 5 device variants = 15 PNGs, so two screens plus their components are 30–36 images to open and look at, on top of 349 already in git (11 MB). CI's build job then takes 40–93 minutes (gh run list: successful runs 25–30 min wall, the last two build jobs failed after 40 and 93 min) and only then verifies the goldens. The screenshot variant matrix is where the review cost lives: 'Narrow phone', 'Phone' and 'Tablet' of the same state rarely disagree on a non-adaptive screen, and a golden nobody looked at is the failure mode the docs themselves name.

**Proposal.** Make the default @ScreenPreview matrix two variants (phone light, phone dark-with-large-font combined) and keep the five only on screens that opt in (`@AdaptiveScreenPreview` for the two list–detail screens). That is a 60 % cut in goldens per screen with no loss on the screens that adapt. Tier the gate and write it down once: pre-commit = doctor + ktlint on changed files; local before PR = affected module's test task + record/verify goldens for that module (`./gradlew :feature:x:presentation:verifyRoborazziDebug`); CI = build + verifyRoborazzi. Move test_scripts.py in CI behind a `paths: scripts/**` filter.

Evidence: `feature/template/presentation/src/test/screenshots`; `CLAUDE.md:245`; `CLAUDE.md:263`; `.github/workflows/build.yml:73`; `.github/workflows/build.yml:81`
### P5 · PLAN.md is edited in 100 of 118 commits; the plan process costs more than it steers

**medium · over-engineered · effort S · confidence 0.8**

Three plan files, 858 lines. The Status block (PLAN.md:14–27) carries a dashboard with progress bars per track, a coverage percentage pinned to a commit hash, and a repo-metrics line (56 modules, 498 tests, 349 goldens) that must be recomputed by a 'merge agent' after every round; the lifecycle rules (PLAN-DETAIL.md:23–33) require every item to touch two files twice. PLAN-WORKERS.md (349 lines) is a one-off brief for two rounds of parallel agents that are both merged, plus a 'Shared files / who appends' ownership table that only makes sense while four workers run at once. Six of the last 40 commits are pure 'Plan:' commits. For a template with one owner, this is a project-management layer sized for a team; the 'What Plan 3 taught' section (PLAN-DETAIL.md:256) already says the dashboard number was wrong for a whole plan.

**Proposal.** Plan 5 is one file, docs/PLAN.md, ≤150 lines: a Decisions table (keep — D1–D36 are the genuinely useful history), a flat checklist of items with Why/Done in two lines each, and a short Backlog. No progress bars, no track percentages, no coverage-at-commit line (Kover's HTML artifact is the number), no PLAN-WORKERS.md in the repo — a worker split is a PR description or a branch note, and history is `git show`. If Plan 5 runs parallel agents again, put the shared-file rules in the PR template, not a 349-line doc. Do not repeat: 30-item plans landed in a day across four worktrees, which is what produced the merge-agent role, the 'Plan:' reconcile commits and the 62 k-file worktree residue that slowed doctor and test_scripts.

Evidence: `docs/PLAN.md:14`; `docs/PLAN.md:20`; `docs/PLAN-DETAIL.md:23`; `docs/PLAN-WORKERS.md:1`; `docs/PLAN-WORKERS.md:228`
### P6 · export_service.py --sync-versions and install_hooks.py are more machinery than the template needs

**medium · over-engineered · effort S · confidence 0.8**

export_service.py is 479 lines, of which ~170 (lines 226–398) parse a Gradle version catalog in Python, resolve `libs.findBundle(...)` accessors inside the convention plugins, and merge entries into another project's libs.versions.toml with conflict reporting. That is a TOML dependency-resolver written in the standard library for a script run once per new project, if ever; it has 7 commits and one test. install_hooks.py (153 lines, 3 tests) reimplements what git provides: a committed `.githooks/pre-commit` plus `git config core.hooksPath .githooks` is the industry-standard versioned hook, needs no installer, no uninstall path and no 'refuses to overwrite a foreign hook' logic. init_project.py (292 lines) and delete_feature.py (216 lines) are proportionate — the first is the documented first step of every new project and the second is what makes the sample features removable.

**Proposal.** export_service.py: keep copy + package rewrite + printing the settings snippet, and replace --sync-versions with printing the catalog entries the copied modules reference (the reader pastes them). Delete merge_catalog/required_catalog_entries/sync_versions and their test. install_hooks.py: delete it; commit `.githooks/pre-commit` (doctor.py, test_scripts.py only when scripts/ changed) and put the one `git config core.hooksPath .githooks` line in README.md; delete the three hook tests. Net: ~350 lines of Python and 4 tests gone, no capability lost.

Evidence: `scripts/export_service.py:226`; `scripts/export_service.py:321`; `scripts/export_service.py:374`; `scripts/install_hooks.py:62`; `scripts/test_scripts.py:558`; `scripts/test_scripts.py:741`
### P7 · 'No new dependency without a decision line' has pushed lint into hand-written Python

**low · over-enforced · effort M · confidence 0.7**

The vendor policy (D27/D28) is sound discipline and has worked — Roborazzi, Maestro and ComposablePreviewScanner each got their line. Its side effect is that Android's own lint surface is under-used while doctor.py grows regex checks for what lint can do: the Modifier-parameter parser (544–642), material-import/dp-literal/colour-literal greps (642–699). lint.xml enables 13 issues; Compose's bundled checks and a Slack compose-lints (large vendor, build-only, D27-compliant) would express 'no Material import in features', 'Modifier parameter present and last', 'no hardcoded dp' as lint rules that run in the same `./gradlew build` a developer already waits for, with IDE highlighting — which regex in a Python script never gets. 'Do not add a script' is fine as stated; the only script-shaped thing missing is a single gate entry point, which is a shell alias rather than a new tool.

**Proposal.** Keep D27 and the 'no new script' rule. Add one decision line: compose-lints (Slack) as a build-only lint source, and move the three design-system greps and the Modifier check from doctor.py into lint.xml rules. That removes ~200 lines from doctor.py, gives the rules IDE feedback, and leaves doctor with the checks lint truly cannot make (registrations, screen unit, translations, service/ portability).

Evidence: `CLAUDE.md:22`; `scripts/doctor.py:544`; `scripts/doctor.py:642`; `lint.xml:1`; `docs/PLAN.md:98`

## Looks heavy but is justified
- Layer direction in doctor.py (doctor.py:459, 30 lines) rather than a Gradle plugin: the Gradle equivalent is more code in build-logic for the same grep, and settings-time helpers already fail the other direction (a listed module with no directory).
- The translation and plurals checks (doctor.py:1133, 1175): lint's MissingTranslation cannot see a module with no values-cs at all, and the four Czech plural forms are exactly what a hand translation drops.
- delete_feature.py (216 lines): the rename-project flow tells a new project to delete the sample features, and undoing five registrations by hand is where a stale Koin line comes from.
- init_project.py (292 lines): the one script every real project runs, with a dry-run and a clean-tree guard; its size is the 35 source sets it moves.
- The six slash commands: 17–36 lines each, with allowed-tools scoping and the post-generation steps — value over --help is the permission scope and the three follow-ups, and they are tested to reference scripts that exist (test_scripts.py:597).
- feature/template compiled in the build and cloned by the generators: it is what makes 'edit the template, not the scripts' true, and the with-gradle CI test is what proves the clone compiles.

## Questions only the owner can answer
- Is exporting service/ into a second project a real, planned use in the next year? If not, export_service.py can shrink to copy-and-rename now (or go entirely) rather than keeping the TOML merge.
- Will Plan 5 run parallel agent rounds again? That decides whether any worker brief belongs in the repo at all, or only in PR descriptions.
- Is Slack's compose-lints acceptable under D27 as a build-only dependency? It is the difference between deleting doctor's hand-written Kotlin parser and keeping it.
- Which screens genuinely need the tablet and narrow-phone goldens (the list–detail pair, presumably) — the answer sets the default preview matrix for everyone else.
