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
`## <tag> ·`, refuses a tag with no such heading, and publishes what follows it as the release
notes — so a worked example carrying a real version number would both satisfy the guard and become
the release page for a release nobody had written notes for.

The `Tasks:` line is what ties the block to the plan that produced it; that plan stays where it is,
with its board cut out and its briefs intact. The ratio sets the next release's lane budget.

<!-- Blocks go below this line, newest first. -->

## v1.0.0 · release A · 2026-09-10

- Every screen has a way back: the four that only had the system gesture now show the arrow.
- A category with nothing in it says so, instead of loading for ever.
- A product added to the cart stays added when the phone is turned.
- Fields, dates and dialogs speak the device's language; a text field says its own name to a
  screen reader.
- Login and Sign-up reach their submit button at any font size and with the keyboard open.
- A link opened from outside the app is applied once, so turning the phone no longer throws away
  where you had navigated to since.
- A release carries the mapping file its crash reports need, and refuses to publish a build that
  was not signed with the release key.

Tasks: A0P2–A0P6 · A0X1–A0X3 · A1T1 · A1U1, A1U2, A1U4 · A1X1, A1X2, A1X4, A1X5 · A2H1, A2H3 · A2T1
Estimate 136 · Actual 108 · Ratio 0.79
