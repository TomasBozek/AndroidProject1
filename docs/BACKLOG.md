# Backlog

One line per idea. **No ids** — an id is assigned when a task is written into a release plan, and
until then this file is the cheap place to put something down. The band is a guess, and the
retrospective id it came from is `(was F6)`.

Add a line here for anything you find while working on something else. Never edit the open plan.

## Next

Candidates for the next release, roughly in order. What is already carrying an id is release B's
plan, not this file.

- Unused components earn their place (was F6) · 25 · 16 of the 47 `App*` components are reached only
  by the gallery, down from 27 before Trips. The count D38 was waiting on is now known and the method
  is in the design-system reference; B3S2 is the last input. Carries an id already — C0T1.
- A connectivity banner (was a third of F16) · 12 · nothing observes connectivity, so a request that
  fails offline is indistinguishable from one that failed. One `NetworkMonitor` and one root banner;
  the banner is in `app/**`, which release B's showcase lane owns for the whole release.
- network_security_config and StrictMode (was H7 + H8) · 6 · the audit corrected the premise —
  cleartext is already denied by the platform at this `targetSdk`. What stands: a debug build cannot
  be proxied without a config, and a main-thread DataStore read goes unnoticed without StrictMode.
- The design system stops speaking POS (was F22) · 6 · 64 mentions of till, void and cash, and
  `AppDensity` scans `InputDevice`.
- The dependency graph is submitted (was H6) · 6 · nothing watches the resolved dependencies for
  known vulnerabilities, and Renovate is parked — so it would be submitted to nothing that acts on it.
- The category and product names the Maestro flows tap are text, not ids. Fixture data rather than
  labels, so the tab fix does not cover them; decide whether fixture rows get ids at all.
- A history, not just a record (arcade) · 12 · the table holds one row per game on purpose (D54);
  every run kept, plus a sparkline per game, is a second table and a chart component that does not
  exist yet.
- Sound in the arcade · 12 · a tick on the stepper and a buzz on a false start. Needs an asset
  pipeline and a settings switch to turn it off, and neither exists.
- Difficulty per game (arcade) · 25 · after the nine exist and someone has actually played them.

- `service/core/ui`'s build file declares about fifteen library dependencies directly (and
  `service/core/data`'s a few), against CLAUDE.md's "a module build file is a `plugins` block and
  its project dependencies. Nothing else." Found during lane 1 of release B, filed here rather than
  during B2T1: that task's scope was Coil specifically, and folding this in would have grown a
  6-point task into a much larger one for a different, unrelated defect. Stays its own task.
- `AppSectionHeader`'s action carries no test id, so `TripsScreen`'s "view all" — the only route to
  `TripsListScreen` — is reachable by text alone, which `CLAUDE.md` § Test identifiers forbids.
  Found writing D1X1's Maestro flow, which had to stop at the tab root because of it. The fix is a
  parameter on the component, so it is `:core:ui`'s and not a feature's.

## Someday

- Certificate pinning · ETag caching · WorkManager sync · Paging 3.
- Renovate installed as an app, not just configured.
- A token pipeline from the design source, when drift actually hurts.
- A ViewModel-readable permission state, when a view model has to decide on one.
- The module graph asserted at build time · `doctor.py --fix` · feature-owned navigation graphs.
- A logger backend · a language picker · `explicitApi()` on `service/` · generated test ids.
- detekt, when 2.x is stable — 1.23.8 cannot run on the JDK the daemon is pinned to.
- Play upload · feedback roles · keyboard shortcuts · z-order roles.
- Two `doctor.py` checks that would keep the reference docs honest: every destination appears in
  the features reference, every `App*` file in the design-system reference.

## Behind a decision

- Real token issuance and the refresh path, and everything else that needs a real API. The sample
  talks to Ktor `MockEngine` fixtures on the `dev` flavor and nothing else, on purpose.
- A shared-element transition from a product row to its detail: a showcase, not a rule.
- Lifecycle, feature-flag and push seams: each is a real seam, none has a caller yet.
