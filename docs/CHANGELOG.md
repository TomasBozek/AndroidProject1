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

## v1.3.0 · release F · 2026-09-19

- Settings has a Language row: English, Čeština or the device's own, applied the moment it is
  picked — the whole app repaints, no restart — and remembered across launches.
- "You're offline" appears above the screen while there is no connection, and goes away by
  itself when there is one.
- Opening a product from a list, its name and price travel into the detail and back, rather than
  the page sliding in over them.
- Messages have colours of their own: a banner, a toast or a status dot in the green, amber, red
  or blue of what it says, no longer the colour of a button; a neutral dot is a visible grey mark
  instead of the same white as the page.
- At a large font size a detail row's label no longer breaks into one letter per line —
  "Application id" stays whole and the value beside it wraps.
- For testers, on `dev` and `staging`: the debug menu jumps straight to any deep screen with a
  fixture on its route, a switch pretends the server is down, and a component playground lets
  you pick a component, turn its knobs and watch it change.
- For operators: work runs in sprints under a release, read from a board on any device; the
  repository is public; CI is switched off and the local gate — now with lint — is what a pull
  request merges on; a launch config starts the app on the emulator; the branches are gitflow.

Tasks: F0P1–F0P4 · F1P1, F1X1, F1H1 · F2P1–F2P5 · F3P1–F3P9, F3X1, F3X2 · F4P1, F4X1, F4U1, F4S1, F4U2, F4U3
Estimate 175 · Actual 123 · Ratio 0.70

## v1.2.0 · release E · 2026-09-13

- Inventory: the things you own, reached from a card on Home. Search, filter and sort the list;
  add an item in four steps; open one to see its picture, owner, tags and history; edit or delete
  it; hold a row to select several and delete them or mark them favourites at once.
- Status tags say New, Pending or Failed rather than Paid or Void, and the sample copy no longer
  reads like a cash register.
- A long form keeps its Next button on screen, and a sheet's last control can be reached on the
  smallest phone.
- A trip stored by a newer version of the app still shows instead of taking the whole list down,
  and an empty "All trips" keeps its back arrow.
- Toasts look like the app.
- For operators: the weekly run installs the release build and signs in on it; the resolved
  dependency graph is submitted, so Dependabot can raise alerts; a debug build can be proxied
  through a user certificate and logs main-thread disk reads; every library dependency lives in
  a convention plugin, so a copied `service/` brings its build with it.

Tasks: E0P1, E0X1, E0P2 · E1X1–E1X3, E1H1, E1T1 · E2P1, E2P2, E2H1, E2H2 · E3U1, E3S1–E3S6, E3T1
Estimate 239 · Actual 117 · Ratio 0.49

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
