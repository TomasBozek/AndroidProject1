# Docs

The rules every session already has are [../CLAUDE.md](../CLAUDE.md). Everything below is read when
a task names it, not by default. A file under `docs/` that is not linked here fails `doctor.py`.

## Spec — changes when the fact changes, or at a release

| Doc | What you get |
|---|---|
| [spec/ARCHITECTURE.md](spec/ARCHITECTURE.md) | layers, how a value moves through a screen, navigation, session, environments |
| [spec/DEPENDENCIES.md](spec/DEPENDENCIES.md) | which library does which job and which plugin applies it; how a version bump happens |
| [spec/DECISIONS.md](spec/DECISIONS.md) | every decision, outcome only |
| [spec/CHANGELOG.md](spec/CHANGELOG.md) | one block per release, in user words |

`docs/spec/CODEBASE.md` — the module tree and the convention plugins — arrives with task A0P2. Until
then it is `CLAUDE.md` § Module structure.

## Reference — the inventory

| Doc | What you get |
|---|---|
| [reference/DOMAIN.md](reference/DOMAIN.md) | entities, results and failures, repositories, what holds the data |
| [reference/FEATURES.md](reference/FEATURES.md) | ten features, eighteen screens with their routes, the tabs, the flows |
| [reference/DESIGN-SYSTEM.md](reference/DESIGN-SYSTEM.md) | the three layers, the theme roles, all the components, previews and goldens |
| [reference/SERVICES.md](reference/SERVICES.md) | what `service/` contains and what makes it portable |
| [reference/CORE.md](reference/CORE.md) | `core/ui`, `core/di` and `app`, and what is registered where |

## Guides — do this, then that

| Doc | What you get |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | the recipes and the testing guide. Splits into `guides/RECIPES.md` and `guides/TESTING.md` in task A0P2 |
| [guides/OPERATIONS.md](guides/OPERATIONS.md) | cutting a release |
| [../scripts/README.md](../scripts/README.md) | the generators and the convention checks |

## Work — changes per task

| Doc | What you get |
|---|---|
| [work/PROCESS.md](work/PROCESS.md) | ids, points, lanes, the task loop, states, what to ship |
| [work/plans/A.md](work/plans/A.md) | **the current plan** · open · ships as v1.0.0 |
| [work/plans/B.md](work/plans/B.md) | the next release · draft |
| [work/BACKLOG.md](work/BACKLOG.md) | one line per idea, no ids |
| [work/plans/TEMPLATE.md](work/plans/TEMPLATE.md) | what a release plan looks like |
| [PLAN.md](PLAN.md) | Plan 5, frozen. Its open items are release A's tasks; task A0P2 deletes it |

## Tooling

| Where | What |
|---|---|
| [../.claude/commands/](../.claude/commands/) | `/task` `/check` `/release` `/new-feature` `/new-screen` `/new-component` `/new-datasource` `/rename-project` |
| [../.github/workflows/build.yml](../.github/workflows/build.yml) | what CI runs, and when |
| [../gradle/libs.versions.toml](../gradle/libs.versions.toml) | every version |

## Archive

`docs/archive/` holds material kept only until it stops being cited. Nothing there is maintained.
The 2026-09 retrospective moves in with task A0P2 and is deleted when release A ships.

## Rules for these docs

- A fact lives in exactly one file. Write it twice and one copy is already wrong.
- Statements, not explanations. What to do, not where it came from.
- No history — git holds that. A landed task grows no paragraph anywhere.
- Spec and reference change in the same commit as the fact they describe, or at a release. Work
  changes one board line per task. Which file goes with what is
  [work/PROCESS.md](work/PROCESS.md) § Which doc changes when.
- Budgets: `CLAUDE.md` ≤ 300 lines, `work/PROCESS.md` ≤ 120, both failed by `doctor.py` from task
  A0P2, which is what brings `CLAUDE.md` under it. The rest
  are targets — spec and guide files around 120, reference files around 100. A doc over budget has
  started explaining itself.
