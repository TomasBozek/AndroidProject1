---
description: Open the next release, or ship the open one — every done sprint under it, as one tag
argument-hint: open <letter> <name> | close
allowed-tools: Bash(git *), Bash(gh *), Bash(python3 scripts/doctor.py), Bash(python3 scripts/board.py), Bash(./gradlew *), Read, Glob, Grep, Edit, Write
---

`$ARGUMENTS`. The rules are `docs/ai/PROCESS.md` § Sprints and releases. A release is
`docs/ai/plans/<letter>.md`: a name, `Status:`, `Sprints:`, the decisions pre-assigned, and three
to eight lines on what it is for. Its sprints are `docs/ai/plans/<letter><n>-*.md`.

## `open <letter> <name>`

Refuse if a release is already `Status: open`. Write `docs/ai/plans/<letter>.md` with
`Status: open`, an empty `Sprints:` line and the goal; add § Release `<letter>` to
`docs/STATUS.md`. Then `/sprint draft`.

## `close`

Refuse unless the open release has at least one `Status: done` sprint and no `Status: open` one.
Then, in one commit titled `<letter>0P1 Ship <letter>`:

1. Sum the `est → act` pairs of every done sprint under § Release `<letter>` in `docs/STATUS.md`.
   Write the block into `docs/CHANGELOG.md`: the heading `## v<x.y.z> · release <letter> · <date>`,
   three to eight lines **in user words** — what someone using the app would notice, not what
   moved in the repository — the `Tasks:` line with every id, and `Estimate · Actual · Ratio`.
2. Sweep the docs: walk `docs/ai/PROCESS.md` § Which doc changes when and check that each spec and
   reference file matches what shipped. Fix what drifted here rather than opening a task for it.
3. Flip the release file to `Status: shipped as v<x.y.z> on <date>`. Cut § Release `<letter>` out
   of `docs/STATUS.md` and write the release into § Shipped: the version, the sprints, the ratio.
   The sprint files stay where they are, briefs and retrospectives intact, boards gone. There is no
   archive (D48).
4. Run `/check pr`, open the pull request, and once it is merged **push the tag yourself** (D72):
   `git fetch origin && git tag v<x.y.z> origin/main && git push origin v<x.y.z>`, on the
   owner's word — the ship is the word; ask again only if the merge changed something. While CI
   is off (D73) a tag builds nothing: the close ends at the pushed tag, and a release page is the
   owner's to make by hand from a local `bundleProdRelease` if one is wanted. When CI is back,
   the close is not done until `gh release view v<x.y.z>` answers. A block with no tag was the
   state `v1.2.0` sat in for three days; do not leave another.
5. `/board`. The next `/release open` starts the next letter.
