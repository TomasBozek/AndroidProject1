# Backlog

One line per idea. **No ids** — an id is assigned when a task is written into a release plan, and
until then this file is the cheap place to put something down. The band is a guess, and the
retrospective id it came from is `(was F6)`.

Add a line here for anything you find while working on something else. Never edit the open plan.

## Next

Candidates for the next release, roughly in order. What is already carrying an id is release B's
plan, not this file.

- Unused components earn their place (was F6) · 25 · 26 of the 46 `App*` components are reached only
  by the gallery. Not estimable until B3S1 and B3S2 have shipped and the count of components nothing
  composes is known; D38 keeps every one of them until then.
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

## Someday

- Certificate pinning · ETag caching · WorkManager sync · Paging 3.
- Renovate installed as an app, not just configured.
- A token pipeline from the design source, when drift actually hurts.
- A ViewModel-readable permission state, when a view model has to decide on one.
- The module graph asserted at build time · `doctor.py --fix` · feature-owned navigation graphs.
- A logger backend · a language picker · `explicitApi()` on `service/` · generated test ids.
- detekt, when 2.x is stable — 1.23.8 cannot run on the JDK the daemon is pinned to.
- Play upload · feedback roles · keyboard shortcuts · z-order roles.
- `init_project.py` rewrites `LICENSE`'s copyright holder, which today names this template's.
- Two `doctor.py` checks that would keep the reference docs honest: every destination appears in
  the features reference, every `App*` file in the design-system reference.

## Behind a decision

- Real token issuance and the refresh path, and everything else that needs a real API. The sample
  talks to Ktor `MockEngine` fixtures on the `dev` flavor and nothing else, on purpose.
- A shared-element transition from a product row to its detail: a showcase, not a rule.
- Lifecycle, feature-flag and push seams: each is a real seam, none has a caller yet.
