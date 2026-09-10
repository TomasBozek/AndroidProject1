# Release <letter> · <one-line goal>

Status: draft
Agents: <N> · lane 1 <pts> (~<h> h) · lane 2 <pts> (~<h> h) · lane 0 open
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D<n>–D<n>

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Shared files

| File group | Owner lane | Tasks |
|---|---|---|
| `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt` | | |
| `gradle/libs.versions.toml` | | |
| `CLAUDE.md`, `docs/ai/CODEBASE.md`, `docs/README.md` | | |
| `.github/workflows/build.yml` | | |

## Board

### Lane 0 · priority

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
