# Codebase

The module tree and the convention plugins: which modules exist, what each one is allowed to depend
on, and which `convention.*` plugin supplies its libraries.

**Until B0P1 fills this file, the tree lives in [../../CLAUDE.md](../../CLAUDE.md) § Module structure
and § Convention plugins.** It moves here byte for byte — that task is what brings `CLAUDE.md` under
its 300-line budget, and it repoints the generators' `CLAUDE_MD_FILE` at this file. Nothing is
written twice in the meantime.

What the modules contain, rather than how they are wired, is [reference/](reference/): `CORE.md`,
`DOMAIN.md`, `FEATURES.md`, `SERVICES.md`, `DESIGN-SYSTEM.md`.
