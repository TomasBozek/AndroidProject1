# Decisions

One row each, outcome only. A decision is taken when the task that needs it starts, and the row is
written in that task's own pull request under the number its release plan pre-assigned. Cite the
task id when the decision came from one.

| | Decision | Outcome |
|---|---|---|
| D1 | A `gateway` module | No. Merged into `data` |
| D2 | Splash | The SplashScreen API, with no minimum hold |
| D3 | `UiState.loading` | Defaults to `null` |
| D4 | Convention plugins | Yes, and they own the shared dependencies |
| D5 | Android Gradle Plugin | Stable 9.x |
| D6 | Navigation 3 | Migrated, with no spike first |
| D7 | Network and database | Ktor client and Room |
| D8 | Crash reporting | An interface with a logging default; no vendor SDK in the repo |
| D9 | Screenshot tool | Compose Preview Screenshot Testing — superseded by D14 |
| D10 | Material 3 Expressive | No. Standard Material 3; its components exist only in an alpha |
| D11 | Design system | Imported from the KSD system as three layers |
| D12 | Dynamic colour | Removed, not defaulted off |
| D13 | Brand face | Source Sans 3, bundled as one variable font |
| D14 | Screenshot tool, second try | Roborazzi |
| D15 | End-to-end tool | Maestro |
| D16 | The component gallery in a release build | No. It lives under the debug menu |
| D17 | Merge policy | One pull request per task, rebase-merged; `main` is one commit per task |
| D18 | Room wiring | `convention.android.room`, applied beside `convention.feature.data` |
| D19 | Template or product | A template |
| D20 | What the sample talks to | Ktor `MockEngine` fixtures on the `dev` flavor |
| D21 | Hardware | A physical device exists and is used for the device-only checks |
| D22 | Licence | None. All rights reserved |
| D23 | Sample features | Favourites, cart, profile and search all stand |
| D24 | Locales | English and Czech, hand-written |
| D25 | Branch protection | Not available on this plan; CI reports, nothing enforces |
| D26 | Renovate | Configured in the repo, the app not installed; parked |
| D27 | Vendor policy | Google, JetBrains and androidx first; then large and widely used. Anything else earns a row here, and if it ships in the release build, a first-party alternative that was tried and found wanting |
| D28 | D27 applied | Koin, Coil, Roborazzi and Maestro stay; mockk and `dependency-analysis` went |
| D29 | Theme | The hand port stands; no token pipeline until drift hurts |
| D30 | Maestro in CI | Weekly and on demand, never per pull request |
| D31 | Versioning | The name comes from the `v*` tag, the code from the commit count; a local build is 1 / `"1.0"` |
| D32 | Store upload | None. A GitHub release carrying the artifact is the release |
| D33 | Icon set | Material icons, in the three sizes `AppTheme.icons.sm/md/lg`. The design names Lucide, which has no first-party Compose artifact |
| D34 | Presentation layout | A directory per screen, even a lone one, and a file per component in the feature's `component/` |
| D35 | Where a screenshot test lives | One per `presentation` module and one in `:core:ui`, cloned from `feature/template` |
| D36 | `ComposablePreviewScanner` | In, under D27's test-only leniency: it reads the `@Preview` functions that already exist, and the alternative is a second list of every golden kept in step by hand |
| D37 | Plan format | Superseded by D41 |
| D38 | Deleting components | Nothing in `:core:ui` is deleted until the showcase features have given the unused ones a home |
| D39 | Open questions in a plan | Answered at the task, not up front. A task that carries one has a `Decide first` line with the recommended reading first |
| D40 | `doctor.py`'s weaker checks | All of them stay. The pruned walk took the suite to a quarter of a second, so the cost argument is gone; reopen on taste, not on time |
| D41 | Documentation system | Work is planned as releases of point-estimated tasks with four-character ids, and checks are tiered T0–T4; `CLAUDE.md` holds the rules alone. Replaces D37's single plan file. Its five zones by kind are superseded by D48 |
| D42 | The 2026-09 retrospective | Deleted with A0P2 rather than archived — the archive's only purpose was to hold it until release A shipped, so there is none. A task that came from it says so once, as `(was F12)` |
| D46 | The token-refresh scaffold | Kept. It is built on Ktor's `bearer` provider, its stale-token comparison reads correctly and it is tested; nothing supplies it yet only because there is no real API. Deleting working, tested concurrency code to be rewritten later is a worse trade than carrying it |
| D47 | How release A ships | One pull request for the whole release, not one per task — the release is a cleanup, and 19 pull requests would be 19 rebases of the same tree. For release A only: D17's one-per-task stands from release B on |
| D48 | The documentation tree | Sorted by depth of audience, not by kind: six files directly under `docs/` for a human, everything else under `docs/ai/`. `ai/` means AI-*only*, not AI-*all* — the agent reads the whole tree, and audience decides how deep a file sits, never what it contains, so a human edition and a machine edition of one fact cannot be written. Everything stays under `docs/`, which is what CI's documentation-only test matches on |
| D49 | The module topology | Both splits stay, and the packages under `service/` move to match them. The ten `di` modules are the only modules allowed to see all four layers, and folding one into its `presentation` would put `data` on a presentation classpath — the rule the template exists to demonstrate. They cost nothing worth having: median cold configuration over five runs each was 1.78 s with their plugins and 1.73 s without, and 0.7 s either way once the configuration cache is warm, so this is D40's answer again — reopen on taste, not on time. `service/` keeps its directory split, which is what `check_service_isolation` enforces and what the export recipe copies, but its Kotlin packages move from `…core.*` to `…service.core.*` (and `…core.network` to `…service.network`). The defect that fixes: `…core.ui.component` existed in both `:core:ui` and `:service:core:ui`, one package split across two modules, so an import said nothing about where a symbol came from and `internal` behaved in a way nobody expected. Taken in B0P1 |
