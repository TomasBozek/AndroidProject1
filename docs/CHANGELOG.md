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

## v1.1.0 · releases B and D · 2026-09-13

- A fifth tab, Trips: plan a trip in three steps, pick its destination from a list, watch the next
  one count down on a dashboard, and open, review or delete any trip.
- A half-filled sign-up, profile or trip asks before the back gesture throws it away.
- On a sign-in form the keyboard's action key moves to the next field, and a password manager can
  offer a saved login.
- Prices and figures shrink to fit their line instead of wrapping, which Czech amounts used to do.
- Loading, errors, empty lists and dialogs look like the rest of the app rather than stock Material.
- A product link opened before that product was ever browsed still opens it.
- A search you were typing survives the app being killed in the background.
- For operators: the release is an app bundle whose version code comes from the tag, and an
  unchanged response is served from the on-disk cache rather than fetched twice.

Tasks: B0P1, B0P2, B0X1, B0U1 · B1U1–B1U4 · B2P1–B2P3, B2H1–B2H3, B2T1, B2X1 · B3S1 · D0X1 · D1P1–D1P4, D1X1, D1X3, D1X5
Estimate 375 · Actual 369 · Ratio 0.98

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
