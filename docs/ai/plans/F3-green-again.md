# Sprint F3 · Green again

Sprint: F3 · Green again
Status: open
When: 2026-09-16 15:30 → 2026-09-17 18:00
Goal: CI runs again and every miss the last two audits found is fixed, so the process does what its docs say
Release: F
Agents: 1 · 42 points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D71–D72

The second improvement sprint, drafted from [../../BACKLOG.md](../../BACKLOG.md) § DevOps, which
held 42 of the 50 points at which one is due. Every line here is something the process was caught
not doing: CI refused every run for three days on account billing and two sprints merged on the
local gate; the pre-commit hook the README promises is unset on this clone; `v1.2.0` has a
changelog block and no tag; a generator wrote a gallery entry that did not compile; two briefs
were written from memory of the checks rather than against them; the release file's decision line
stopped following its sprints. And three things the tooling never had: a launch config so `/run`
can show a screen, a coverage number anyone reads, and a place for Claude Design in a screen's
recipe.

The owner decided the first one before this draft: **the repository is public** (D71), so Actions
is free and `CLAUDE.md` § Checks stays as written — the rerun of #29's run was green on the jobs
billing had refused. F3P1 records that and makes `main` protected, which public also unlocks.
Everything else is small, and the order is: what changes CI first, then the hook and the tag,
then the generators and the commands, then the three additions.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

| File group | Task |
|---|---|
| `.github/workflows/build.yml`, `docs/RELEASING.md`, `docs/DECISIONS.md` | F3P1, F3P3, F3P6 |
| the repository's ruleset (GitHub settings, via `gh api`) | F3P2 |
| `.githooks/pre-commit`, `scripts/init_project.py`, `scripts/doctor.py`, `.claude/commands/check.md`, `scripts/README.md` | F3P4 |
| `.claude/commands/{sprint,release}.md`, `docs/ai/plans/F.md`, `docs/ai/PROCESS.md` | F3P3, F3P5 |
| `scripts/_common.py`, `scripts/test_scripts.py` | F3X1 |
| `.claude/launch.json`, `docs/ai/RECIPES.md`, `docs/ai/TESTING.md` | F3P7, F3P8 |
| `CLAUDE.md` | F3P1 (§ Checks, one sentence), F3P4 |

`build-logic/**`, `gradle/**`, `settings.gradle.kts` and every Kotlin module are untouched; T1's
"whole `./gradlew test`" conditional does not fire, `test_scripts.py` does.

## Tasks

### F3P1 CI is back: the repository is public · 6 · decides D71

**Why** Actions refused every job from 2026-09-13 to 2026-09-16 — "recent account payments have
failed" — so F1 and F2 merged on the local gate and `CLAUDE.md` § Checks ("CI runs T2, T3 and
T4") was false for three days. The owner will not pay for GitHub and chose public on 2026-09-16;
`gh repo edit --visibility public` was run and the rerun of run `35097369930` went green on the
jobs billing had refused. What is left is to say so where it is read, and to make sure a run
that never started can never again read as a build result.
**Decide first** `public, and CI stays the gate as written`, or `private with T2–T4 local` → D71.
The decision is taken — public — and the row records why: a template's value is being read, and
Actions minutes on a public repository cost nothing; what it gives up is the fixture data and the
mock secrets, none of which were secret (the history scan found only `KeystoreAead.kt`).
**Done when** `gh repo view --json visibility --jq .visibility` prints `PUBLIC`; the latest run
on `main` — `gh run list --branch main --limit 1` — is `success`, or its only failure is a job that
ran; `docs/RELEASING.md` § 4 says a job with 0 steps and a *not started* annotation is a billing or
runner stop, not a result, and names the `gh api …/annotations` line that reads it; `CLAUDE.md`
§ Checks has one sentence: the repository is public so that CI is free, and a run that never
started is `not started: <reason>`, never `fail`; `docs/README.md` says the repository is public
in its first paragraph; D71 is a row; `python3 scripts/doctor.py` passes.
**Touches** `docs/RELEASING.md`, `CLAUDE.md` § Checks, `docs/README.md`, `docs/DECISIONS.md`.
**Read** `docs/RELEASING.md:5-40` · `CLAUDE.md` § Checks · `.claude/commands/task.md` (F2P3's
*not started* wording, to reuse) · `.github/workflows/build.yml:26-60` (the `changes` job).
**Steps** 1. `gh run rerun 35098341868` if the `main` run is still the billing failure; wait
for it. 2. The RELEASING paragraph and the CLAUDE sentence — the same words as `task.md`, not new
ones. 3. D71. 4. `docs/README.md`, one clause.
**Checks** T0. **Depends** —

### F3P2 Main is protected · 3 · after F3P1

**Why** Branch protection needed a public repository or GitHub Pro; the first is now true. Until
now the only gate was the discipline in `/task`, and a rebase-merge from a red pull request went
through — twice, on runs that never started.
**Done when** `gh api repos/{owner}/{repo}/rulesets --jq '.[].name'` lists `main`; the ruleset
targets `refs/heads/main`, requires a pull request, requires the status checks `changes`,
`conventions` and `build` (a check whose job was skipped by `changes` satisfies a required status
check; one that never started does not), forbids force-push and deletion, and lets the owner
bypass — `gh pr merge --rebase --delete-branch` still works on a green pull request; a push
straight to `main` — `git push origin HEAD:main` from a scratch commit, then delete it — is
refused; `docs/RELEASING.md` § Hotfix says a hotfix goes through a pull request like everything
else, and `docs/ai/PROCESS.md` § Task loop 9 says the merge is what the ruleset lets through.
**Touches** the ruleset (settings, not a file), `docs/RELEASING.md`, `docs/ai/PROCESS.md`.
**Read** `.github/workflows/build.yml:26-60,81-110` (the job names) · `docs/RELEASING.md` § A
hotfix · GitHub's REST `POST /repos/{owner}/{repo}/rulesets` — create it with `gh api` and a JSON
body, so the body is in the pull request for the record.
**Steps** 1. The JSON: `target: branch`, `conditions.ref_name.include: ["refs/heads/main"]`,
rules `pull_request`, `required_status_checks` with the three contexts, `non_fast_forward`,
`deletion`; `bypass_actors` the repository admin role with `always`. 2. `gh api` it; paste the
body into the pull request under *What changed*. 3. The two doc lines.
**Checks** T0. **Depends** F3P1

### F3P3 The tag is pushed when the ship commit merges · 6 · decides D72

**Why** `v1.2.0` has had a changelog block since 2026-09-13 and no tag: `/release close` ends at
"the owner pushes the tag" and nobody did, so the release job never built E and the audit flags it
every close. A step that ends in someone else's hands is a step that does not happen.
**Decide first** `/release close pushes the tag itself, after the ship pull request merges` or
`the release job tags main when a block lands with no matching tag` → D72. Recommended: the
command. A tag pushed by a workflow with `GITHUB_TOKEN` starts no other workflow, so the job
would have to build the release in the same run it tagged in, which turns the tag job into the
release job with an extra branch; and a tag is the one act the owner said is theirs — the command
asks once, then does it, and the audit's *tag for every shipped block* stays as the check.
**Done when** `git tag -l 'v*'` lists `v1.2.0` at `70de4f3` (`E0P2 Ship E`) and its release job
ran green — `gh run list --event push --json headBranch,conclusion --jq '.[] |
select(.headBranch=="v1.2.0")'`; `.claude/commands/release.md` § `close` step 4 ends with the
push and a check that the tag's run started; `docs/RELEASING.md` § 3 says the command pushes,
on the owner's word, in the same session that merged; D72 is a row; `docs/ai/PROCESS.md`
§ Sprints and releases *ship* row says "then the tag, pushed by the command".
**Touches** `.claude/commands/release.md`, `docs/RELEASING.md`, `docs/ai/PROCESS.md`,
`docs/DECISIONS.md`, one tag.
**Read** `.claude/commands/release.md` § `close` · `docs/RELEASING.md:5-40` ·
`.github/workflows/build.yml:197-300` (the release job, what it refuses) · `docs/CHANGELOG.md`
§ v1.2.0.
**Steps** 1. D72. 2. The three doc edits. 3. `git tag v1.2.0 70de4f3 && git push origin v1.2.0`
— on the owner's word, which this task is; watch the release job; if it refuses, the block is
what is wrong and this task fixes the block.
**Checks** T0. **Depends** —

### F3P4 The pre-commit hook is installed by something · 3

**Why** `git config core.hooksPath .githooks` is a README sentence, unset on this clone; what
runs here is a stale `.git/hooks/pre-commit` a deleted `install_hooks.py` left behind, and a
fresh clone has neither. Two audits in a row named it.
**Done when** `python3 scripts/doctor.py` prints a `[note]` when `git config core.hooksPath` is
not `.githooks` — a note, not a failure, because CI's checkout has no hooks and must stay green —
naming the one command; `/check` (`.claude/commands/check.md`) runs that command first when the
note appears; `scripts/init_project.py` sets it on a clone it rewrites; `git config
core.hooksPath` on this clone prints `.githooks` and `.git/hooks/pre-commit` is gone;
`scripts/README.md` § Once per clone still says the command, and adds that `doctor.py` reminds;
`python3 scripts/test_scripts.py` passes.
**Touches** `scripts/doctor.py`, `scripts/init_project.py`, `.claude/commands/check.md`,
`scripts/README.md`, `CLAUDE.md` § Checks (one clause: T0 starts with the hook note).
**Read** `scripts/doctor.py` § the `[note]` mechanism (grep `note(`) · `scripts/init_project.py`
§ the git steps · `scripts/README.md:11-18` · `.githooks/pre-commit`.
**Steps** 1. The doctor note, with `git config` read through `subprocess` and a `FileNotFoundError`
treated as "not a checkout". 2. `init_project.py`. 3. `check.md`, one line. 4. On this clone: the
command, and `rm .git/hooks/pre-commit`.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F3X1 `create_component.py` writes an entry that compiles · 3

**Why** The starter gallery entry calls `{Pascal}()` with no argument and adds no import to
`GalleryCatalog.kt`, so the gallery module fails to compile until both are fixed by hand — F1H1
found it with `AppBanner`. The generator knows the name and the signature it just wrote.
**Done when** `register_in_gallery` in `scripts/_common.py` adds `import
<base>.core.ui.component.<Pascal>` in sorted position when it is missing, and the variant lambda
calls the component with every required parameter of the composable it generated — the template's
signature is the generator's own, so the call is written from it, not guessed; a test in
`scripts/test_scripts.py` generates a component and asserts the import line and a call that names
each required parameter; `python3 scripts/test_scripts.py --with-gradle` compiles
`:feature:gallery:presentation` after a generated component (the existing Gradle test extends to
this, or a second one is added under the same flag); `python3 scripts/doctor.py` passes.
**Touches** `scripts/_common.py`, `scripts/test_scripts.py`.
**Read** `scripts/_common.py:540-566` · the component template the generator writes (grep
`COMPONENT_TEMPLATE` or the `create_component.py` write) · `scripts/test_scripts.py` § the
`--with-gradle` test · `feature/gallery/presentation/**/GalleryCatalog.kt:30-40,955-970` (a real
import and a real entry).
**Steps** 1. Parse the parameters out of the template string the generator just wrote — the
`@Composable fun <Pascal>(` line to its `)` — and emit `name = <placeholder>` per parameter with no
default: `""` for `String`, `{}` for a lambda, `Modifier` never (it defaults). 2. The import. 3.
The two tests.
**Checks** T0 + `python3 scripts/test_scripts.py --with-gradle`. **Depends** —

### F3P5 A brief is written against the checks, and the release line follows its sprints · 6

**Why** Two lines with one cause — `/sprint draft` writes from memory. F1's briefs named a test id
the vocabulary refuses (`main_offlineBanner`) and a decision number already taken (D67); F2
pre-assigned D69–D70 and `F.md` still said D66–D68. The draft step reads nothing before it writes.
**Done when** `.claude/commands/sprint.md` § `draft` step 3 begins with reading `python3
scripts/doctor.py --list` and the last row of `docs/DECISIONS.md`, says a *Done when* names ids
from the vocabulary in `CLAUDE.md` § Test identifiers, and step 6 takes decision numbers from the
last row and **extends the release file's `Decisions pre-assigned:` line** in the same edit;
`docs/ai/plans/F.md` says `D66–D72`; `docs/ai/PROCESS.md` § Drafting a sprint carries the same
two sentences; `python3 scripts/doctor.py` gains a check that every sprint file's pre-assigned
range lies inside its release file's, and passes.
**Touches** `.claude/commands/sprint.md`, `docs/ai/plans/F.md`, `docs/ai/PROCESS.md`,
`scripts/doctor.py`, `scripts/test_scripts.py`.
**Read** `.claude/commands/sprint.md` § `draft` · `docs/ai/PROCESS.md` § Drafting a sprint ·
`scripts/doctor.py` § `check_task_ids` (the shape of a docs check) · `docs/ai/plans/F.md`,
`F1-*.md`, `F2-*.md` headers.
**Steps** 1. The command and the PROCESS sentences. 2. `F.md`. 3. `check_decisions_pre_assigned`:
parse `Decisions pre-assigned: D<a>–D<b>` from each `plans/<letter>.md` and each
`plans/<letter><n>-*.md`; a sprint range outside its release's is a problem. 4. Its test.
**Checks** T0 + `python3 scripts/test_scripts.py`. **Depends** —

### F3P6 Coverage goes somewhere · 3 · after F3P1

**Why** `koverHtmlReport koverXmlReport` runs on every push to `main` and the report is an upload
artifact nobody opens. A number nobody reads is not a signal.
**Done when** the `build` job's coverage step, on `push` only, appends one line to
`$GITHUB_STEP_SUMMARY` — `Coverage: <line %> (<covered>/<total> lines)` read from
`build/reports/kover/report.xml` with a ten-line Python step, no action — and a `::warning::`
when it is below the previous `main` run's number by more than two points, never a failure;
`docs/ai/TESTING.md` § Coverage says where the number is and that it warns and never gates;
the next `main` run's summary shows the line.
**Touches** `.github/workflows/build.yml:178-196`, `docs/ai/TESTING.md`.
**Read** `.github/workflows/build.yml:103-196` · `docs/ai/TESTING.md` § Coverage · Kover's XML:
`<counter type="LINE" missed=… covered=…/>` at the report root.
**Steps** 1. The step, after the report, `if: github.event_name == 'push'`. 2. The previous number
comes from the last successful `main` run's summary is not readable from a job — so the warning
compares against a `COVERAGE_FLOOR` env at the top of the workflow, set to today's number
rounded down, and the step prints the number to update it. 3. TESTING.md, four lines.
**Checks** T0; the workflow is proved by the `main` run after merge. **Depends** F3P1

### F3P7 A launch config for the emulator · 6

**Why** There is no `.claude/launch.json`, so `/run` has nothing to start and a change is verified
by tests and goldens only; a screen a session changed is never looked at on a device by the
session that changed it.
**Done when** `.claude/launch.json` has two configurations: `devDebug` — `./gradlew installDevDebug`
then `adb shell am start -n <applicationId>.dev/.MainActivity` on the booted emulator — and
`maestro` — `maestro test .maestro/<flow>.yaml` with the flow as its argument; `docs/ai/RECIPES.md`
§ Seeing it on a device says which emulator to boot (`emulator -list-avds`, the first), that
`/run devDebug` installs and starts, and that a screenshot goes into the pull request when a
screen moved; `/run devDebug` on this machine ends with the app on screen; `python3
scripts/doctor.py` passes.
**Touches** `.claude/launch.json`, `docs/ai/RECIPES.md`.
**Read** the `run` skill's description in this session's skill list · `app/build.gradle.kts` and
`build-logic/**/ProjectConfig.kt` § Flavor (the application id suffix) · `.maestro/config.yaml`
(the `appId` line) · `docs/ai/RECIPES.md` § A new screen (where the new section goes after).
**Steps** 1. `emulator -list-avds`; boot one. 2. The two entries, `runtimeExecutable` and
`runtimeArgs`, no `port` (nothing listens). 3. The recipe, ten lines. 4. Run it once.
**Checks** T0. **Depends** —

### F3P8 A design change starts in Claude Design · 6

**Why** The KSD system was imported once into `:core:ui` and edited in Kotlin since; drift goes
unnoticed until a re-brand, and a new screen is drawn straight in Compose with no picture anyone
agreed to. The `/design` canvas exists and nothing in the recipes reaches for it.
**Done when** `docs/ai/RECIPES.md` § A new screen has a first step — *draw it first* — that says
when a `/design` canvas pays (a screen with a layout nobody has seen; a component new to the
system) and when it does not (a form of existing components, a fix), how the canvas's artboard is
named after the screen (`<Domain><Purpose>Screen`, the same id the scaffold carries), and that
its link goes into the sprint brief's *Read* line; `docs/ai/reference/DESIGN-SYSTEM.md` § Source
says the KSD project is the source and the canvas is where a change is proposed before
`create_component.py`; one canvas exists as the worked example — the F1H1 banner, drawn after
the fact, linked from the recipe; `python3 scripts/doctor.py` passes.
**Touches** `docs/ai/RECIPES.md`, `docs/ai/reference/DESIGN-SYSTEM.md`, one artifact.
**Read** `docs/ai/RECIPES.md` § A new screen · `docs/ai/reference/DESIGN-SYSTEM.md` § Source ·
the `design` skill (load it before drawing) · `core/ui/**/AppBanner.kt`.
**Steps** 1. The canvas, one artboard, the banner in both themes. 2. The recipe step. 3. The
reference sentence.
**Checks** T0. **Depends** —
