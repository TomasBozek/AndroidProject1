# Release <letter> · <one-line goal>

Status: draft
Agents: 1 · lane 1 <what it holds> <pts> · lane 2 <what it holds> <pts> · lane 0 open
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D<n>–D<n>

<Three to eight lines: what is wrong today, what this release does about it, and what it leaves
alone. Written for the agent who opens it cold.>

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

What each lane writes. With one agent nothing is arbitrated; with two, a lane owns its rows.

| File group | Owner lane | Tasks |
|---|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt` | | |
| `gradle/libs.versions.toml`, `build-logic/**`, `.github/workflows/build.yml` | | |
| `scripts/**`, `CLAUDE.md`, `docs/ai/CODEBASE.md` | | |

## Board

### Lane 0 · chores

- [ ] <id> <title> · <pts>

### Lane 1 · <what it owns, in three words>

- [ ] <id> <title> · <pts> · decides D<n> · after <id>

### Lane 2 · <what it owns, in three words>

- [ ] <id> <title> · <pts>

## Tasks

### <id> <title> · <pts>

**Why** one line: what is wrong today.
**Decide first** `<recommended reading>`, or `<alternative>` → D<n>. *(only when the item is open)*
**Done when** the commands and greps that prove it.
**Touches** the paths this task writes — this is what fills the Shared files table.
**Read** `file:lines` to open before starting.
**Steps** 1. … 2. … — concrete paths, the generator to call, the doc row to update.
**Checks** T1 + <extras this touch set adds>. **Depends** `<id>` or —.
