# Docs

What to open for what. Six files here, for a person; everything an agent needs beyond
[../CLAUDE.md](../CLAUDE.md) is under [ai/](ai/) and is read when a task names it, never by default.

| Doc | What you get |
|---|---|
| [STATUS.md](STATUS.md) | the open release's board: every task, its points, its state |
| [CHANGELOG.md](CHANGELOG.md) | one block per release, in user words |
| [DECISIONS.md](DECISIONS.md) | every decision, outcome only |
| [RELEASING.md](RELEASING.md) | cutting a release: the tag, the four secrets, the hotfix rule |
| [BACKLOG.md](BACKLOG.md) | one line per idea, no ids |
| [ai/](ai/) | the machine-facing tree, below |

## ai/ — read when a task names it

`ai/` is a depth, not a dialect: the agent reads the whole tree, and a file sits here because a
person has no reason to open it — not because it says anything different (D48).

| Doc | What you get |
|---|---|
| [ai/PROCESS.md](ai/PROCESS.md) | ids, points, lanes, the task loop, states, what to ship |
| [ai/ARCHITECTURE.md](ai/ARCHITECTURE.md) | layers, how a value moves through a screen, navigation, session, environments |
| [ai/CODEBASE.md](ai/CODEBASE.md) | the module tree and the convention plugins |
| [ai/RECIPES.md](ai/RECIPES.md) | how to do a thing here for the first time |
| [ai/TESTING.md](ai/TESTING.md) | the tests per screen, the goldens, what the build reports |
| [ai/DEPENDENCIES.md](ai/DEPENDENCIES.md) | which library does which job, and how a version bump happens |
| [ai/reference/](ai/reference/) | the inventory: `DOMAIN` `FEATURES` `DESIGN-SYSTEM` `SERVICES` `CORE` |
| [ai/plans/](ai/plans/) | one file per release: why each task is there and what finishes it |

The generators and the convention checks are [../scripts/README.md](../scripts/README.md); the slash
commands that drive them are [../.claude/commands/](../.claude/commands/).

## Rules for these docs

- A fact lives in exactly one file. Write it twice and one copy is already wrong.
- Statements, not explanations. What to do, not where it came from.
- No history — git holds that. A landed task grows no paragraph anywhere.
- A file under `docs/` names no source path; if it needs one, it belongs under `ai/`.
- Which file goes with what you changed is [ai/PROCESS.md](ai/PROCESS.md) § Which doc changes when.
- Budgets: `CLAUDE.md` ≤ 300 lines, `ai/PROCESS.md` ≤ 120. The rest are
  targets — around 120 for a guide, around 100 for a reference. A doc over budget has started
  explaining itself.
