---
description: Publish the sprint board, drafts, backlog and releases to the board artifact
argument-hint: [read]
allowed-tools: Bash(python3 scripts/board.py), Bash(git *), Read, Artifact
---

`$ARGUMENTS`. The board artifact (D67) is the one place the sprint can be read without the
repository — a phone, another machine, a session with no checkout. Its URL is in `docs/README.md`
§ The board. The repository is the truth and the artifact is a window on it: **never edit the
artifact's data by hand, never let it drift**.

## No argument — publish

1. `python3 scripts/board.py > <scratchpad>/board.json`. It reads `docs/STATUS.md`,
   `docs/BACKLOG.md`, `docs/CHANGELOG.md` and `docs/ai/plans/*.md`, and prints one JSON object;
   it fails loudly on a header it cannot parse rather than publishing a half board.
2. `Artifact` with `action: "write_db"`, `db_op: "set"`, `collection: "board"`, `doc_id: "state"`,
   `file_path: <scratchpad>/board.json`, `url: <the URL in docs/README.md>`.
3. Say what changed in one line: the sprint, the counts, the commit it was read at.

The page itself is republished only when its code changes — `docs/ai/board/index.html` is its
source; `Artifact` with that `file_path`, the same `url`, and `capabilities: {db: {}}` kept.

## `read`

`Artifact` with `action: "read_db"`, `db_op: "get"`, `collection: "board"`, `doc_id: "state"`.
Use it from a session without the repository to answer "what is on the board"; with the
repository, read `docs/STATUS.md` instead — it is the truth.
