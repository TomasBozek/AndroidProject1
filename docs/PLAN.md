# Plan 5 · after the retrospective

> **Frozen 2026-09-10.** The open items live in [work/plans/A.md](work/plans/A.md) under new ids,
> each marked `(was F12)`; new decisions go to [spec/DECISIONS.md](spec/DECISIONS.md) and new ideas
> to [work/BACKLOG.md](work/BACKLOG.md). Mid-item? Finish it and tick it here as before — task A0P2
> mirrors it across and deletes this file.

The board, and the only plan file. What is open, why it is worth doing, and what finishes it. The
evidence behind every `F`/`H`/`M`/`S` id is [REVIEW.md](REVIEW.md) and [review/](review/), which
this file does not repeat. `ui.10`, `shell.7`, `shell.8` and `qa.16` carry over from Plan 4.

**How to work it.** One item per commit, titled `<id> <title>`; one or two agents at a time, never a
fan-out. Before each commit run `python3 scripts/doctor.py`, `./gradlew ktlintCheck` and the touched
module's own test task — never `./gradlew build`; `verifyRoborazziDebug` only when a preview changed.

**A `?` marks an open question**, with both readings on the item; it is settled when that item
starts, not before. Sixteen of the retrospective's twenty need the code open to answer honestly.

**Do first:** `F1` · `F2`+`F3` · `F17e` · `F8`+`F9` · `F4`. The first three cut what every later
item costs, the last two what every session costs; only `F8` and `F4` need an answer to start.

## Decisions

D1–D36 stand from Plans 2–4. A decision with a default is taken when its item starts.

| | Decision | Outcome |
|---|---|---|
| D1 | `gateway` module | Merged into `data` |
| D2 | Splash | SplashScreen API, no minimum hold |
| D3 | `UiState.loading` | Defaults to `null` |
| D4 | Convention plugins | Yes, and they own the shared dependencies |
| D5 | AGP | Stable 9.x |
| D6 | Navigation 3 | Migrated, no spike |
| D7 | Network and database | Ktor client + Room |
| D8 | Crash reporting | Interface + logging default; no vendor SDK in the repo |
| D9 | Screenshot tool | Compose Preview Screenshot Testing — superseded by D14 |
| D10 | Material 3 Expressive | No, standard M3 |
| D11 | Design system | KSD, imported as three layers |
| D12 | Dynamic colour | Removed, not defaulted off |
| D13 | Brand face | Source Sans 3, bundled as one variable font |
| D14 | Screenshot tool, second try | Roborazzi |
| D15 | End-to-end tool | Maestro |
| D16 | Component gallery in prod | No — it moves under the debug menu (`shell.2`) |
| D17 | Merge policy | One PR per item, rebase-merged; `main` is one commit per item |
| D18 | Room wiring | `convention.android.room`, applied beside `convention.feature.data` |
| D19 | Template or product | Template |
| D20 | What the sample talks to | Ktor `MockEngine` fixtures on the `dev` flavor |
| D21 | Hardware | A physical device exists; `qa.5` stays behind `shell.1` |
| D22 | Licence | None; all rights reserved |
| D23 | Sample features | All four stand: favourites, cart, profile, search |
| D24 | Locales | English and Czech, hand-written |
| D25 | Branch protection | Not possible on the free plan; CI reports, nothing enforces |
| D26 | Renovate | Configured in the repo, app not installed; parked |
| D27 | Vendor policy | Google, JetBrains, androidx first; then large and widely used; anything else earns a line here |
| D28 | D27 applied | Koin, Coil, Roborazzi and Maestro stay; mockk and `dependency-analysis` went |
| D29 | Theme | The hand port stands; no token pipeline until drift actually hurts |
| D30 | Maestro in CI | **Decided 2026-09-09 with `qa.11`: weekly schedule and `workflow_dispatch`, not per pull request** |
| D31 | Versioning | **Decided 2026-09-09 with `qa.12`: name from the `v*` tag, code from the commit count; local builds keep 1 / 1.0** |
| D32 | Store upload | **Decided 2026-09-09 with `qa.12`: none.** A GitHub release carrying the APK is the release |
| D33 | Icon set | **Decided 2026-09-09: Material icons stay; the three sizes are `AppTheme.icons.sm/md/lg`.** The design names Lucide, which has no first-party Compose artifact |
| D34 | Presentation layout | **Decided 2026-09-09: a directory per screen, even a lone one, and a file per component in the feature's `component/`** |
| D35 | Where a screenshot test lives | **Decided 2026-09-10 with `ui.2`: one per `presentation` module and one in `:core:ui`, cloned from `feature/template`.** A single copy in `:app` sees every module but runs three times over the flavors |
| D36 | `ComposablePreviewScanner` | **Decided 2026-09-10 with `ui.2`: in, under D27's test-only leniency.** Single maintainer, never in a release build; it reads the `@Preview` functions that already exist, and the alternative is a second list of all 349 goldens kept in step by hand |
| D37 | Plan format | **Decided 2026-09-10 with `F9`: one `PLAN.md` under 150 lines.** `PLAN-DETAIL.md` and `PLAN-WORKERS.md` are deleted; a worker split is a PR description |
| D38 | Deleting components | **Decided 2026-09-10: nothing in `:core:ui` is deleted in Plan 5.** The showcase features give the unused ones a home first; what is still unused after them is a decision then, with the counts in front of us |
| D39 | The retrospective's questions | **Decided 2026-09-10: answered at the item, not up front.** Each is marked `?` below with both readings; the recommended one is first |
| D40 | `doctor.py`'s weaker checks | **Decided 2026-09-10 with `F3`: all 30 stay.** The review proposed dropping the CLAUDE.md-tree, foreign-identifier and four style checks, mostly because they were slow; the pruned walk took the whole suite to 0.24 s, so the cost argument is gone and only taste is left. Reopen on taste, not on time |

## Items

- [x] (2026-09-10) **F1 One variant in the PR gate** · S · first — Why `build` is six variants and 923 test
  executions for 498 tests. Done an explicit task list; unit tests only on `devDebug`; R8 on release.
- [x] (2026-09-10) **F2 `test_scripts.py` stops copying the worktrees** · S · first — Why they were
  2,271 of 2,856 copied files, 56 times over. Done ignored, and a guard test so it cannot come back.
- [x] (2026-09-10) **F3 `doctor.py` walks the tree once** · S · first — Why 17 `rglob`s over 62,583
  files: 14.0 s. Done one pruned walk into a shared index — 0.24 s, and all 30 checks kept (see D40).
- [x] (2026-09-10) **F17e The date picker fits its dialog** · M · first — Why `AppDialog` was
  platform-width, uncapped and unscrolled. Done `DatePickerDialog` themed; four overlay goldens.
- [x] (2026-09-10) **F8 `CLAUDE.md` on a diet** · M · first — Why 983 lines in every session, the
  gate in it six times. Done 363 lines plus `docs/ARCHITECTURE.md`; one gate, no contradictions.
- [x] (2026-09-10) **F4 The golden matrix** · M · first — Why 353 full-frame goldens, 11.9 MB, five
  per screen, no threshold. Done 3 per screen / 2 per component, half size, a threshold: 225, 3.9 MB.
- [ ] **F5 The ten `di` modules** · M · trim — Why 13–59 lines each, and each pays an AAR and a 20 s
  Robolectric warm-up. Done `?` bindings beside the classes (56 → 46), or `data`+`presentation` merged.
- [ ] **F11 The `service/` split, decided once** · L · trim — Why split packages, the namespace gotcha
  and an un-themed shell, for a reuse that never happened. Done `?` fold into `:core:*`, or keep the
  split and give `service/` its own package names — not both.
- [ ] **F10 `export_service.py` and `install_hooks.py` shrink** · S · trim — Why 170 lines of TOML
  resolver for a script with no consumer; hooks are `core.hooksPath`. Done `.githooks/` committed.
- [ ] **F14 The ~450 dead lines** · S · trim — Why `combineOutcomes`, `executeAsFlow`, `AlertPayload`,
  `displayMessage` and 222 lines of token refresh have no callers. Done `?` deleted, or rebuilt on Ktor.
- [ ] **F15 Where the data-source interface lives** · M · trim — Why ten interfaces, one implementation
  each, no fake anywhere. Done `?` interface at the repository with a fake, or the per-source rule stands.
- [ ] **F18 Flavors** · S · trim — Why `staging` is `prod` plus one line and an unreachable host, and
  triples every `:app` task. Done `?` `dev` + `prod` with `DebugMenu` following `BuildConfig.DEBUG`.
- [ ] **F19 Test plumbing stops being copied** · S · trim — Why the Robolectric pin is in 50 files and
  11 screenshot tests differ by a package string. Done `robolectric.properties` and one base class.
- [ ] **F24 Coil and the two single-consumer plugins** · S · trim — Why Coil is put on all 13 Compose
  modules so `doctor` can forbid it on 12. Done Coil in `core/ui`, Ktor in `service/network`.
- [ ] **F12 `BaseViewModel`'s defaults** · M · correct — Why 21 of ~28 sites pass `loading = {}`,
  `whileSubscribed` has no caller, nullable data drops updates. Done overlay opt-in. `?` non-null state.
- [ ] **F13 The screen shell joins the design system** · M · correct — Why overlay, error, empty and
  alert are raw Material with 17 dp literals. Done slots on `Screen()` filled in `AppNavHost`. After `F11`.
- [ ] **F16 SavedStateHandle, connectivity, HTTP cache** · M · correct — Why the saved-state artifact
  is used nowhere, offline is found by failing, prod has no cache. Done a `saved(key)` helper, a
  `NetworkMonitor` with one root banner, an OkHttp `Cache`.
- [ ] **F17a An empty category stops spinning** · M · correct — Why an empty table maps to `null`, so
  `cached()` never emits and `products_empty` is unreachable. Done a fetched-at marker, plus a test.
- [ ] **F17b Add-to-cart leaves the nav host** · M · correct — Why it runs on `rememberCoroutineScope`,
  so a rotation after the tap cancels the insert. Done an `AddProductToCart` use case, via `execute`.
- [ ] **F17c The release job cannot ship a debug-signed APK** · M · correct — Why a tag before the
  secrets exist publishes one, Play needs an AAB, versionCode regresses. Done `bundleProdRelease`, a
  hard fail without a keystore, the version code from the tag.
- [ ] **F17d The baseline profile reaches the shipping build** · M · correct (was `qa.15`) — Why it is
  recorded minified into `src/devRelease` and `prodRelease` never sees it. Done `?` fixed, or cut.
- [ ] **shell.7 A deep link to an uncached product opens it** · M · correct — Why `getProduct` reads
  the local table only, so a cold link lands on "no longer available". Done an unbrowsed product opens.
- [ ] **shell.8 Every non-root screen carries an Up control** · S · correct — Why three screens have a
  bar with no arrow and `ProductDetailScreen` has no bar. Done four catch up with the other five.
- [ ] **qa.16 The tabs have test ids** · S · correct — Why they carry no `testTag`, so five Maestro
  flows tap English labels and break under `cs`. Done `tabs_homeTab` and a `doctor.py` check.
- [ ] **ui.10 A text field says its own name** · S · correct — Why a field's label does not reach its
  semantics node, so a screen reader announces an unnamed field. Done the label is announced.
- [ ] **F6 Unused components earn their place** · M · design — Why 27 of 47 are used only by the
  gallery and `AppFab` has no slot. Done the showcase gives 18 a home, `AppScaffold` a slot; none deleted.
- [ ] **F7 The gallery is generated from the previews** · M · design — Why `GalleryCatalog.kt`
  hand-copies 48 previews in 896 lines with no check. Done one line per preview, plus that check.
- [ ] **F23 `AppTextField` rebuilt, size enums folded** · M · design — Why two size enums, three
  components with no `modifier`, no IME action or autofill. Done `TextFieldState` (M1), content types (M2).
- [ ] **H3 Forms scroll under the keyboard** · S · design — Why `LoginScreen` is a fixed centred column
  and its submit is unreachable at large font. Done `scrollable` on Login, SignUp and Profile.
- [ ] **H5 Pull-to-refresh on the catalog lists** · M · design — Why cache-then-refresh exists but
  nothing triggers it; zero `Refresh` events. Done `AppPullToRefresh` over `PullToRefreshBox`.
- [ ] **M3+M4 Predictive back on dirty forms, auto-sizing numerics** · S · design — Why zero
  `BackHandler` uses and Czech strings wrap prices. Done the unsaved-changes confirm; `TextAutoSize`.
- [ ] **F22 The design system stops speaking POS** · S · design — Why 52 mentions of till/void/cash,
  and `AppDensity` scans `InputDevice`. Done `?` neutral copy and no pointer detection, or both kept.
- [ ] **S1 Trips** · L · showcase, after `F4` and `F6` — Why five screens give 22 unused components a
  home: wizard, nav result, snackbar action, dashboard. Done full stack in memory. `?` which takes the tab.
- [ ] **S2 Field report** · L · showcase — Why location, camera, Photo Picker and SAF in context, giving
  `PartiallyGranted` a real user, with no new dependency. Done four screens; the camera helper moves.
- [ ] **H1 The R8 mapping ships with the release** · S · harden — Why a minified stack trace is
  unreadable without it. Done `mapping.txt` as an artifact, attached to the tagged release.
- [ ] **H6 The dependency graph is submitted** · S · harden — Why nothing watches the resolved
  dependencies for CVEs and Renovate is parked (D26). Done `generate-and-submit` on the main job.
- [ ] **H7+H8 `network_security_config` and StrictMode** · S · harden — Why cleartext is not denied, a
  debug build cannot be proxied, main-thread DataStore reads go unnoticed. Done the config and policies.

## Backlog

- **Behind Q8:** certificate pinning · ETag caching · real token issuance and the refresh path (`F14`) · WorkManager sync · Paging 3.
- **Optional seams `H9`–`H11`:** foreground/background with a session re-check · feature flags with debug-menu overrides · a push seam through the deep-link parser. None in Plan 5. **`M5`** shared-element product row → detail is a showcase, not a rule.
- Renovate (D26) · KSD token pipeline (D29) · ViewModel-readable permission state · module graph asserted at build time · `doctor.py --fix` · feature-owned nav graphs · logger backend · language picker (D24) · detekt at 2.x · `explicitApi()` on `service/` · Play upload (D32) · generated `Ids` · feedback roles · keyboard shortcuts · z-order roles.
