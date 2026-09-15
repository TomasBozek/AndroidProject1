# Backlog

One line per idea — `- <title> · <pts> · <group> · <why>`. **No ids** — an id is assigned when a
task is written into a sprint, and until then this file is the cheap place to put something down.
The band is a guess, and the id a line arrived with is `(was D1X2)`. The group is what the work
touches, so the board can be read by layer: a module path — `core:ui`, `feature:trips`,
`service:network`, `app` — or one of `build`, `tests`, `process`.

Add a line here for anything you find while working on something else. Never edit the open sprint.

**Groomed 2026-09-13 (D62).** Everything open in releases B, C and D and every line that was here
was ranked — fixes first, then the build, then the design system, then new features — and the top
two thirds became release E. What follows is the rest, and it is **not carried forward by
default**: the next grooming does each line or deletes it.

## Next

In order. What the sprint after F takes first.

- The Maestro flows stop tapping fixture text · 12 · tests · `"Beverages"`, `"Coffee"` and `"Olive oil"` are
  tapped by label. List rows carry one constant tag today, so a per-row id means
  `categories_item_<id>`, and the vocabulary check E1X1 adds has to learn a suffix before anything
  else does — which is why this follows it rather than sitting in E.
- A component playground (was C1U6) · 25 · feature:devmenu · pick a component, drive its properties from real
  controls, watch it change: the bench the gallery is not. A screen behind the dev menu, built from
  the controls it shows; a component's knobs are data, not a `when` per component.
- `ContentState` stands in for the whole screen · 6 · service:core:ui · `Screen()` draws a `ContentMessage` instead
  of the content, scaffold included, so a non-root screen showing `Empty` loses its up arrow and
  its screen id — E0X1 found `TripsListScreen` unreachable to a flow on a cleared app and moved
  it to an in-screen `AppEmptyState`. Decide whether the chrome's message should render inside
  the screen's shell (a slot the scaffold fills) or whether `showContent(Empty)` is for root
  screens only, and say so in `CLAUDE.md` § MVI.

## DevOps

How the project is built, checked, shipped and planned — with Claude. A group here is `ci`,
`release`, `git`, `gradle`, `process`, `templates` or `claude`. An improvement sprint drafts from
this section the way a feature sprint drafts from § Next, and the reviewer's job is to keep it
honest: a line lands here whenever the process is caught not doing what its docs say.

- The tag is pushed when the ship commit merges · 6 · release · `v1.2.0` has had a changelog block
  since 2026-09-13 and no tag: the ship task ends at "the owner pushes the tag" and nobody did.
  Either `/release close` ends with the push, or the release job tags `main` itself when a
  `## v<x.y.z>` block lands with no matching tag.
- The pre-commit hook is installed by something · 3 · git · `git config core.hooksPath .githooks`
  is a README sentence and unset on this clone, so `doctor.py` never ran before a commit here.
  `/check` sets it when it is missing, or `init_project.py` does.
- A launch config for the emulator · 6 · claude · there is no `.claude/launch.json`, so `/run` has
  nothing to start and a change is verified by tests and goldens only. One entry that boots the
  `devDebug` install and one that runs a Maestro flow, so a session can watch a screen it changed.
- Main is protected · 3 · git · branch protection needs a public repository or GitHub Pro; until
  then the only gate is the discipline in `/task`, and a rebase-merge from a red pull request goes
  through. Decide: public, Pro, or a `gh pr merge` alias that refuses while `gh pr checks` fails.
- A bug is a line with a shape · 3 · templates · nothing says how a bug is reported. One line in
  § Next — `- <what happens> · <pts> · <group> · seen on <flavor> <version>, expected <what>,
  steps <n>` — and the sprint that takes it writes the `X` task from it.
- The sprint close writes a retrospective · 3 · process · the ratio is the only retrospective, and
  it cannot say *why* a 25 took 12. `/sprint close` appends three lines to the sprint file: what
  the briefs got wrong, what the checks missed, one thing to change — and the next draft reads it.
- The board publishes from CI · 3 · ci · `/board` needs a session; a merge to `main` that flips a
  board line leaves the artifact stale until someone runs it. Constraint: the artifact store is
  written through the Claude tool only, so this is a cloud routine (`/schedule`) that checks out
  `main` and runs `/board` each weekday morning, not a workflow step.
- One backlog grammar in every section · 6 · process · § Someday holds four ideas per line, and
  neither it nor § Behind a decision carries a group, so `board.py` cannot tag them. Every line is
  `- <title> · <pts> · <group> [· <kind>] · <why>` — the group a module path at any depth
  (`feature:auth:data`) or a process area, the kind the id letter it will get — and `doctor.py`
  holds it. A section says how ready a line is, never what it touches.
- The board opens on the state of the project · 12 · claude · the head shows the open sprint and
  nothing else. A strip with the last shipped version and its date, the open release and its done
  sprints, the next draft and this section's total; a previous-sprint card with its retrospective;
  the backlog as one list behind filter chips — area, feature, layer, kind, readiness — instead of
  four sections and a select. `board.py` already carries the data; only the page changes.
- The sprint close audits the process · 3 · process · `/sprint close` checks that the process did
  what its docs say — hook installed, tag pushed, board republished, checks run, `est` against
  `act` — and every miss is a line here. It ends by saying how many points this section holds, so
  the owner knows when an improvement sprint is due: every third sprint, or sooner past 50.
- Coverage goes somewhere · 3 · ci · `koverHtmlReport` runs on `main` and the report is an
  artifact nobody opens. A one-line total in the job summary, and a threshold that warns and never
  fails (`CLAUDE.md`: a signal, never a gate).
- A design change starts in Claude Design · 6 · claude · the KSD system is imported once and edited
  in Kotlin since; drift goes unnoticed until a re-brand. The `/design` canvas for a new screen
  before its `create_screen.py`, and a note in `RECIPES.md` on when that pays and when it does not.

## Someday

- Certificate pinning · ETag caching · WorkManager sync · Paging 3.
- Renovate installed as an app, not just configured (D26).
- A token pipeline from the design source, when drift actually hurts (D29).
- A ViewModel-readable permission state, when a view model has to decide on one.
- The module graph asserted at build time · `doctor.py --fix` · feature-owned navigation graphs.
- A logger backend · a language picker · `explicitApi()` on `service/` · generated test ids.
- detekt, when 2.x is stable — 1.23.8 cannot run on the JDK the daemon is pinned to.
- Play upload · feedback roles · keyboard shortcuts · z-order roles.
- An arcade of small games, one per component group — the release C draft, abandoned for the
  Inventory showcase (D62). The briefs are in git: `git show a64d498:docs/ai/plans/C.md`.

## Behind a decision

- Real token issuance and the refresh path, and everything else that needs a real API. The sample
  talks to Ktor `MockEngine` fixtures on the `dev` flavor and nothing else, on purpose (D20).
- A shared-element transition from a product row to its detail: a showcase, not a rule.
- Lifecycle, feature-flag and push seams: each is a real seam, none has a caller yet.
