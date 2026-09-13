# Backlog

One line per idea. **No ids** — an id is assigned when a task is written into a release plan, and
until then this file is the cheap place to put something down. The band is a guess, and the id a
line arrived with is `(was D1X2)`.

Add a line here for anything you find while working on something else. Never edit the open plan.

**Groomed 2026-09-13 (D62).** Everything open in releases B, C and D and every line that was here
was ranked — fixes first, then the build, then the design system, then new features — and the top
two thirds became release E. What follows is the rest, and it is **not carried forward by
default**: the next grooming does each line or deletes it.

## Next

In order. What the release after E takes first.

- The dev menu jumps straight to a screen (was D1X2) · 12 · the Tools section is three buttons and
  not one opens a screen; a tester reaching the editor's third step does it forty times a day. One
  list of jumps built in the nav host — the only place that knows every destination — rendered as
  list items, a fixture value for any route argument. Its old brief is in the closed D plan.
- A connectivity banner (was a third of F16) · 12 · nothing observes connectivity, so a request
  that fails offline is indistinguishable from one that failed. One monitor in `service/network`,
  one root banner in `app`.
- The Maestro flows stop tapping fixture text · 12 · `"Beverages"`, `"Coffee"` and `"Olive oil"` are
  tapped by label. List rows carry one constant tag today, so a per-row id means
  `categories_item_<id>`, and the vocabulary check E1X1 adds has to learn a suffix before anything
  else does — which is why this follows it rather than sitting in E.
- A component playground (was C1U6) · 25 · pick a component, drive its properties from real
  controls, watch it change: the bench the gallery is not. A screen behind the dev menu, built from
  the controls it shows; a component's knobs are data, not a `when` per component.
- Field report (was B3S2) · 50 · a photo, a coarse location and a note, exported through the
  Storage Access Framework — the showcase for the platform seams nothing drives yet: location, the
  photo picker, a document contract. New development; it waited on purpose (D62). The brief is in
  the closed B plan.

## Someday

- Certificate pinning · ETag caching · WorkManager sync · Paging 3.
- Renovate installed as an app, not just configured (D26).
- A token pipeline from the design source, when drift actually hurts (D29).
- A ViewModel-readable permission state, when a view model has to decide on one.
- The module graph asserted at build time · `doctor.py --fix` · feature-owned navigation graphs.
- A logger backend · a language picker · `explicitApi()` on `service/` · generated test ids.
- detekt, when 2.x is stable — 1.23.8 cannot run on the JDK the daemon is pinned to.
- Play upload · feedback roles · keyboard shortcuts · z-order roles.
- An arcade of small games, one per component group — the release C draft, abandoned for the
  Inventory showcase (D62). The briefs are in git: `git show a64d498:docs/ai/plans/C.md`.

## Behind a decision

- Real token issuance and the refresh path, and everything else that needs a real API. The sample
  talks to Ktor `MockEngine` fixtures on the `dev` flavor and nothing else, on purpose (D20).
- A shared-element transition from a product row to its detail: a showcase, not a rule.
- Lifecycle, feature-flag and push seams: each is a real seam, none has a caller yet.
