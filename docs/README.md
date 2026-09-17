# Docs

What to open for what. Six files here, for a person; everything an agent needs beyond
[../CLAUDE.md](../CLAUDE.md) is under [ai/](ai/) and is read when a task names it, never by default.
The repository is public — a template is read (D71). CI is switched off for now (D73): the gate is `/check pr` on the machine that merges.

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
| [ai/PROCESS.md](ai/PROCESS.md) | sprints and releases, ids, points, the task loop, states, the board |
| [ai/ARCHITECTURE.md](ai/ARCHITECTURE.md) | layers, how a value moves through a screen, navigation, session, environments |
| [ai/CODEBASE.md](ai/CODEBASE.md) | the module tree, the convention plugins, the API already built, and what the build refuses |
| [ai/RECIPES.md](ai/RECIPES.md) | how to do a thing here for the first time |
| [ai/TESTING.md](ai/TESTING.md) | the tests per screen, the goldens, what the build reports |
| [ai/DEPENDENCIES.md](ai/DEPENDENCIES.md) | which library does which job, and how a version bump happens |
| [ai/reference/](ai/reference/) | the inventory: `DOMAIN` `FEATURES` `DESIGN-SYSTEM` `SERVICES` `CORE` |
| [ai/plans/](ai/plans/) | one file per release and one per sprint: why each task is there and what finishes it |

The generators and the convention checks are [../scripts/README.md](../scripts/README.md); the slash
commands that drive them are [../.claude/commands/](../.claude/commands/).

## Rules for these docs

- A fact lives in exactly one file. Write it twice and one copy is already wrong.
- Statements, not explanations. What to do, not where it came from.
- No history — git holds that. A landed task grows no paragraph anywhere.
- A file under `docs/` names no source path; if it needs one, it belongs under `ai/`.
- Which file goes with what you changed is [ai/PROCESS.md](ai/PROCESS.md) § Which doc changes when.
- Budgets, and this is the only place they are written: `CLAUDE.md` ≤ 400 lines,
  `ai/PROCESS.md` ≤ 160 — the two the checker enforces. The rest are targets — around 150 for a
  guide, around 120 for a reference. A number is a proxy for the thing that matters, which is that a
  doc over its budget has started explaining itself instead of saying what to do; raise one when the
  content earns it, and cut when it does not.

## The board

The open sprint, the drafts, the backlogs and what shipped, readable from a phone or a session
with no checkout: <https://claude.ai/artifact/5Jyu1PrDAmkt3TmSTBDCsi>. It is a window on this
tree, never the other way round — `/board` republishes it from `STATUS.md`, `BACKLOG.md`,
`CHANGELOG.md` and `ai/plans/` after a board line changes (D67), and a cloud routine —
*AndroidProject1 · /board each weekday morning*, at <https://claude.ai/code/routines> — does the
same at 07:00 Prague on weekdays from the open sprint's branch, or `main` when none is ahead of it
(D70). The page's footer says when it was last synced and from which commit; between the two, a
session that flipped a line and did not run `/board` is the only way it is stale.
