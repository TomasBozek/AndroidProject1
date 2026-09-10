# Changelog

One block per release, in user words: what someone using the app would notice, not what moved in the
repository. The block is written by the ship task before the tag is pushed, and the release job
refuses a tag that has no block.

Shape:

```
## v<x.y.z> · release <letter> · <date>

- Three to eight lines, each a thing that changed for a user or an operator.

Tasks: <the ids, in ranges>
Estimate <n> · Actual <n> · Ratio <n>
```

The version is written out, never a placeholder: the release job greps this file for
`· <tag> ·` and refuses a tag with no block, so a worked example carrying a real version number
would satisfy the guard for a release nobody had written notes for.

The `Tasks:` line is what ties the block to the plan that produced it; the plan itself moves to
`../archive/plans/` at the same moment. The ratio sets the next release's lane budget.

<!-- Blocks go below this line, newest first. -->
