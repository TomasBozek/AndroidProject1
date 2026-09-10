# Handoff · finishing the retrospective and writing Plan 5

Written 2026-09-10 at the end of the session that ran the review. For the next session (any
model). Read this file and `../REVIEW.md` first; do not re-run the review — everything it
produced is in this directory.

## Where everything is

| What | Where |
|---|---|
| Ranked summary, grades, ids `F`/`H`/`M`/`S` | `docs/REVIEW.md` |
| Full reviewer output per area (evidence, proposals, "keep" lists, owner questions) | `docs/review/{architecture,architecture-pass2,build-and-ci,design-system,app-shell-and-tests,scripts-and-docs,data-and-network}.md` |
| Build and test timings | `docs/review/measurements.md` |
| Hardening proposals, full | `docs/review/hardening.md` |
| Newer Compose APIs, checked in the resolved jars | `docs/review/modern-compose.md` |
| Date-picker diagnosis and fix | `docs/review/date-picker.md` |
| Two showcase designs | `docs/review/showcase-trips.md`, `docs/review/device-feature.md` |
| The interactive report with the owner's decisions | https://claude.ai/code/artifact/3cd1e78c-a274-43ef-a621-d25d132c632d |
| The owner's answers | the artifact's database, collection `decisions`, one document per decision id (`F3`, `F4`, `F5`, `F6`, `F7`, `F8`, `F9`, `F11`, `F12`, `F14`, `F15`, `F17`, `F18`, `F20`, `F21`, `F22`, `H`, `M`, `S`, `S2`) with fields `index`, `label`, `note`, `q`, `at`. Read with the Artifact tool: `action: read_db`, `db_op: list`, `collection: decisions`, `url` above |
| Raw agent results (JSON) | `~/.claude/projects/-Users-tomasbozek-Projects-android-AndroidProject1/7d35ca0c-6471-4063-8ffb-b63673b6dca7/subagents/workflows/wf_da2126f7-dd6/journal.jsonl` — on this machine only |

Nothing in the code changed this session. New files: `docs/REVIEW.md`, `docs/review/*`.

## What was done and what was not

Done: six area reviews (architecture ran twice; both passes kept, they agree on substance and
differ on counts), one build measurement, three side tasks (date picker, modern Compose, device
feature), one hardening sweep, one showcase design, a hand-written synthesis, the report page.

Not done, in the order it should happen:

1. **The owner's decisions.** Twenty cards on the report page. Until they are answered Plan 5
   cannot be written; the recommended option is first on every card.
2. **Verification.** No adversarial pass ran (cut for cost). The findings are evidence-based
   but unverified by a second reader. Spot-check the load-bearing ones with the greps below —
   about ten minutes, no agents.
3. **Three contradictions between reviewers** to settle while spot-checking:
   - Screen tests: the app-and-tests reviewer says make `XScreenTest` optional; the date-picker
     agent's notes say they are real tests (`HomeScreenTest.kt:48-80` covers the empty branch).
     It is decision `F20`; the owner's answer settles it.
   - `doctor.py` time: 2.7 s (measurement agent, warm file cache) vs 13–19 s (scripts reviewer
     and the main session, cold). Both are true; the cause (one `rglob` per check over 62k
     files) is not in dispute.
   - Snackbar senders: one review says `UiCommand.ShowSnackbar` has zero senders, another cites
     the stale-catalog snackbar in `ProductsViewModel.kt:57-62`. Check with
     `grep -rn "ShowSnackbar\|ShowToast" feature app --include=*.kt`.
4. **Plan 5** — one file, `docs/PLAN.md`, under 150 lines (see `REVIEW.md` "Plan 5 shape"),
   replacing the current `PLAN.md`, `PLAN-DETAIL.md` and `PLAN-WORKERS.md` if decision `F9` says
   so. Carry over the five open Plan 4 items: `ui.10`, `shell.7`, `shell.8`, `qa.15` (= `F17d`),
   `qa.16`.
5. **The work itself**, one item per commit, `<id> <title>`, in the order `REVIEW.md` gives.

## Spot-checks (no agents; each is one command)

```bash
grep -n "run: ./gradlew build" .github/workflows/build.yml; grep -rn "beforeVariants\|enableUnitTest" build-logic/   # F1: expect the first, none of the second
sed -n 45,80p scripts/test_scripts.py; ls .claude/worktrees                                                     # F2: copytree in setUp, .claude not ignored
grep -c "rglob" scripts/doctor.py                                                                                # F3: one walk per check
grep -rn "loading = {}" feature/*/presentation/src/main --include=*.kt | wc -l                                   # F12: ~20
grep -rn "whileSubscribed" feature app --include=*.kt | wc -l                                                    # F12: 0
grep -rn "combineOutcomes\|chainOutcomes\|executeAsFlow\|AlertPayload\|TokenRefresher" feature app core --include=*.kt | wc -l   # F14: 0
sed -n 15,35p feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/source/DefaultLocalCatalogDataSource.kt   # F17a: takeIf { isNotEmpty() }
sed -n 30,50p core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppDialog.kt                  # F17e: Dialog with default properties
find . -path '*/src/test/screenshots/*.png' -not -path './.claude/*' | wc -l; ls -d feature/*/di                   # F4, F5: 349 and 10
```

Component usage (features only, gallery and template excluded), the basis of `F6` and both
showcase designs: AppScaffold 16, AppButton 13, AppText 12, AppTopBar 9, AppListItem 8,
AppDivider 5, AppEmptyState 5, AppIconButton 4, AppSectionHeader 4, AppTextField 3, AppCard 2,
AppAvatarPhoto/AppBottomActionBar/AppDescriptionList/AppPager/AppSearchField/AppSegmented/
AppStepper/AppSwitch/AppTag 1 each; the other 27 are zero.

## How to finish efficiently

The review cost about three million subagent tokens, half of it on a parallel run that hit the
session limit after 46 agents in seven minutes and was cut. The lean rerun (12 agents, one at a
time) still cost 1.6 million, because every subagent re-reads `CLAUDE.md` and its area from
scratch. Do not repeat either.

- **No subagents for the rest.** Reading answers, spot-checking, writing Plan 5 and implementing
  the items are all main-session work with targeted reads. Budget: about 150k tokens to reach a
  written Plan 5; then per item, one module's test task rather than `./gradlew build`.
- **If a subagent is ever needed** (the weekly `test_scripts.py --with-gradle` compile, say), one
  at a time, `medium` effort, told to grep `CLAUDE.md` for a section rather than read it, and to
  finish in about 40 tool calls.
- **Land `F1` first** (explicit single-variant gate) and `F2`/`F3` (script speed) — they cut
  every later iteration's cost. `F8` (a 300-line `CLAUDE.md`) cuts every session's cost.
- **Do not run `./gradlew build` locally** until `F1` lands; run `:module:testDevDebugUnitTest`
  (or `testDebugUnitTest` for a library) for the module touched, and `verifyRoborazziDebug` only
  when a preview changed.
- **Stale worktrees.** Four under `.claude/worktrees/` (15 MB) and their `claude/*` branches are
  merged or superseded per `PLAN.md`. Removing them is `git worktree remove <path>` and
  `git branch -D <name>` — confirm with the owner first; it is not reversible.

## If the review is ever repeated

One agent per area, sequential, `medium` effort, with the area's file list in the prompt, a
token note (grep the rulebook, read ranges, ~40 tool calls) and a structured-output schema; no
verifier or judge panels; the synthesis by hand from the saved results. Expect ~100k tokens per
area. The measurement agent can be a cheaper model — it mostly waits on Gradle.
