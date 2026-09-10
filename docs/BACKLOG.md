# Backlog

One line per idea. **No ids** — an id is assigned when a task is written into a release plan, and
until then this file is the cheap place to put something down. The band is a guess, and the
retrospective id it came from is `(was F6)`.

Add a line here for anything you find while working on something else. Never edit the open plan.

## Next

Candidates for the next release, roughly in order.

- The module topology, decided once (was F5 + F11) · 50 · the ten `di` modules and the `service/`
  split are one question: fold `service/` into `:core:*`, or keep the split and give it its own
  package names — not both. Repo-wide, so it runs alone in lane 0 before the lanes start.
- The screen shell joins the design system (was F13) · 25 · loading, error, empty and alert are raw
  Material with dp literals; slots on `Screen()` filled in `AppNavHost`. After the topology.
- Where the data-source interface lives (was F15) · 12 · ten interfaces, one implementation each, no
  fake anywhere: interface at the repository with a fake, or the per-source rule stands.
- SavedStateHandle, connectivity, HTTP cache (was F16) · 25 · a `saved(key)` helper, a
  `NetworkMonitor` with one root banner, an OkHttp `Cache`.
- A deep link to an uncached product opens it (was shell.7) · 12 · `getProduct` reads the local
  table only, so a cold link lands on "no longer available".
- The dependency graph is submitted (was H6) · 6 · nothing watches the resolved dependencies for
  known vulnerabilities and Renovate is parked.
- Unused components earn their place (was F6) · 25 · 27 of 47 are used only by the gallery and
  `AppFab` has no scaffold slot. Pairs with the showcase features.
- The gallery is generated from the previews (was F7) · 25 · the gallery catalog hand-copies 44
  entries in 896 lines with no check that it matches.
- AppTextField rebuilt, size enums folded (was F23 + M1 + M2) · 25 · two size enums, three
  components with no `modifier`, no IME action or autofill; rebuild on `TextFieldState`.
- The design system stops speaking POS (was F22) · 6 · 52 mentions of till, void and cash, and
  `AppDensity` scans `InputDevice`.
- Trips, a showcase feature (was S1) · 50 · five screens give 22 unused components a home: wizard,
  nav result, snackbar action, dashboard.
- Field report, a showcase feature (was S2) · 50 · location, camera, Photo Picker and SAF in
  context, with no new dependency.

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
