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
- A `Trips` tab · 3 · B3S1 registered the whole feature under `mainEntries()` but did not add a
  bottom-bar tab for it — the bottom-bar enum and `:app`'s own strings are outside that lane's
  file set. One entry, one label string, one `xEntries()` call.
- **MAX PRIORITY — `./gradlew test` must always be green.** `OverlayScreenshotTest`'s two
  date-picker goldens (`the date picker on the narrowest phone`, `the date picker in landscape`)
  fail under a full multi-module `./gradlew test` but pass reliably run alone, even at
  `--max-workers=1` — found running T1 for lane 2 of release B, on code none of that lane's tasks
  touch, and reproduced again independently on B3S1 (`gh` PR #17), same two tests, same way. The
  same failure reproduces against a clean `origin/main` checkout run the same way, and does not
  reproduce running `:core:ui` by itself against either.
  **What it actually is, confirmed by reading the Roborazzi diff
  (`core/ui/build/outputs/roborazzi/overlay_datePicker_narrowPhone_compare.png`):** not pixel
  antialiasing noise — the "today" ring on the calendar grid lands on a different day than the
  golden (off by exactly one day in the reproduction seen on B3S1), while the *selected* date
  circle is unchanged. `@Before pinTheClock()` sets `android.os.SystemClock.setCurrentTimeMillis`
  because the Material3 `DatePicker`'s "today" comes from the wall clock; something specific to
  running alongside other modules' Robolectric suites makes that pin not hold by the time the
  picker composes. **Ruled out:** `changeThreshold` tolerance (`PreviewScreenshotSpec`'s own
  `CHANGE_THRESHOLD = 0.001f` pattern) — a whole day's digit is not a rendering-jitter difference
  and papering over it with pixel tolerance would hide a real date bug, not a flaky one.
  **Confirmed, not just theorized:** `./gradlew :core:ui:testDebugUnitTest --tests
  "*OverlayScreenshotTest*"` passes 100% alone — the pin mechanism itself is sound; only the
  concurrent-with-other-modules condition breaks it. Leading cause, per Gradle's own docs on
  `forkEvery` ("a way to manage leaky tests or frameworks that have static state that can't be
  cleared or reset between tests") and Robolectric's own history of parallel-execution timing
  issues: Gradle defaults `forkEvery` to unlimited, so one Test task's worker JVM is reused across
  every test class in that module — static JVM/JDK state (`TimeZone`, native graphics
  initialization) is not guaranteed clean between classes, and that is *before* factoring in
  several other modules' Robolectric suites competing for CPU at the same time, which is exactly
  the condition the docs describe scheduling losing precision under. The well-established fix is
  `forkEvery = 1` on the Robolectric test tasks that need real clock isolation, which is real
  ongoing cost to test speed and belongs in a `build-logic` convention plugin, not a module build
  file (`core/ui` is lane 1's, and CLAUDE.md's "a module build file is a `plugins` block and its
  project dependencies, nothing else" forbids putting it there directly) — asked the user directly
  rather than silently touching build-logic or another lane's file from an unrelated task's branch.
  **Tried `forkEvery = 1` on `:core:ui`'s own test task, via its convention plugin, per the user's
  go-ahead — and it did not hold.** Same two tests failed the same way in a full `./gradlew test`
  with every test class in a fresh JVM. That rules the leaked-static-JVM-state theory out; the
  change was reverted rather than left in for a cost with no benefit. What is left standing:
  something that specifically needs *other modules* running at the same time, which points at CPU
  contention affecting the wall-clock read itself rather than anything `:core:ui`'s own test task
  can isolate on its own — matching what Robolectric's own issue tracker says about parallel Gradle
  execution making `currentTimeMillis`/`nanoTime`-sensitive tests lose precision under load, not a
  state-leak story. Next things worth trying, in rough order of cost: (1) read
  `androidx.compose.material3.CalendarModel` (or the `Legacy`/`Api26Impl` it resolves to at this
  Robolectric SDK) to confirm exactly which clock call "today" comes from, since `Calendar
  .getInstance()` and `LocalDate.now()` are shadowed by different Robolectric mechanisms and only
  one of `android.os.SystemClock` and `java.lang.System.currentTimeMillis` is guaranteed to move
  together with it; (2) once that is known, pin whichever clock is actually read, directly, rather
  than the `android.os.SystemClock` shadow the test pins today; (3) if the actual mechanism turns
  out to be scheduler precision under real CPU load rather than a wrong clock, a golden that
  asserts the picker's *shape* without asserting which specific day is ringed (two goldens instead
  of one, or a crop that excludes the calendar grid) is the fallback that keeps the check honest
  without waiting on Robolectric to fix its own parallel-execution timing.

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
