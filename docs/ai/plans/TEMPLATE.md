# Sprint <letter><n> · <name>

Sprint: <letter><n> · <name>
Status: draft
When: <yyyy-mm-dd hh:mm> → <yyyy-mm-dd hh:mm>
Goal: <one line>
Release: <letter>
Agents: 1 · <pts> points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D<n>–D<n>

<Three to eight lines: what is wrong today, what this sprint does about it, and what it leaves
alone. Written for the agent who opens it cold.>

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

What each task writes. With one agent nothing is arbitrated; with two, a task owns its row.

| File group | Task |
|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt` | |
| `gradle/libs.versions.toml`, `build-logic/**`, `.github/workflows/build.yml` | |
| `scripts/**`, `CLAUDE.md`, `docs/ai/CODEBASE.md` | |

## Board

Stays here while the sprint is a draft; `/sprint open` lifts it into `docs/STATUS.md`.

- [ ] <id> <title> · <pts> · decides D<n> · after <id>

## Tasks

### <id> <title> · <pts>

**Why** one line: what is wrong today.
**Decide first** `<recommended reading>`, or `<alternative>` → D<n>. *(only when the item is open)*
**Done when** the commands and greps that prove it.
**Touches** the paths this task writes — this is what fills the Files table.
**Read** `file:lines` to open before starting.
**Steps** 1. … 2. … — concrete paths, the generator to call, the doc row to update.
**Checks** T1 + <extras this touch set adds>. **Depends** `<id>` or —.
