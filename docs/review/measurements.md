# Build and test measurements
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

Now compiling the final JSON output.

```json
{
  "doctor_s": 2.68,
  "test_scripts_s": 441.86,
  "configure_s": 0.71,
  "one_module_test_s": 40.76,
  "ktlint_s": 3.53,
  "build_total_s": 28.04,
  "build_configure_s": 0.52,
  "slowest_tasks": [
    {"task": ":app:lintReportDevDebug", "s": 2.544},
    {"task": ":baselineprofile:mergeBenchmarkReleaseAssets", "s": 2.171},
    {"task": ":baselineprofile:mergeNonMinifiedReleaseAssets", "s": 1.961},
    {"task": ":baselineprofile:compileBenchmarkReleaseKotlin", "s": 1.819},
    {"task": ":app:mergeExtDexDevDebug", "s": 1.774},
    {"task": ":baselineprofile:desugarNonMinifiedReleaseFileDependencies", "s": 1.658},
    {"task": ":app:minifyProdBenchmarkReleaseWithR8", "s": 1.579},
    {"task": ":app:mergeStagingDebugJavaResource", "s": 1.492},
    {"task": ":app:mergeProdNonMinifiedReleaseJavaResource", "s": 1.479},
    {"task": ":app:mergeDevBenchmarkReleaseJavaResource", "s": 1.413},
    {"task": ":app:mergeStagingNonMinifiedReleaseJavaResource", "s": 1.395},
    {"task": ":app:checkDevBenchmarkReleaseDuplicateClasses", "s": 1.382},
    {"task": ":app:mergeProdBenchmarkReleaseJavaResource", "s": 1.372},
    {"task": ":app:mergeStagingBenchmarkReleaseJavaResource", "s": 1.336},
    {"task": ":baselineprofile:mergeBenchmarkReleaseNativeLibs", "s": 1.295}
  ],
  "tests_total": 923,
  "tests_seconds": 772.56,
  "tests_per_module": [
    {"module": "core/ui", "count": 184, "s": 110.15},
    {"module": "app", "count": 135, "s": 97.10},
    {"module": "feature/catalog/presentation", "count": 99, "s": 75.79},
    {"module": "feature/cart/presentation", "count": 37, "s": 65.93},
    {"module": "feature/auth/presentation", "count": 34, "s": 60.87},
    {"module": "feature/profile/presentation", "count": 57, "s": 48.28},
    {"module": "feature/settings/presentation", "count": 43, "s": 48.04},
    {"module": "feature/devmenu/presentation", "count": 27, "s": 43.94},
    {"module": "feature/onboarding/presentation", "count": 28, "s": 41.38},
    {"module": "feature/home/presentation", "count": 20, "s": 41.05},
    {"module": "feature/gallery/presentation", "count": 24, "s": 40.44},
    {"module": "feature/catalog/data", "count": 20, "s": 25.12},
    {"module": "service/core/ui", "count": 72, "s": 22.75},
    {"module": "feature/cart/data", "count": 20, "s": 20.49},
    {"module": "feature/auth/data", "count": 15, "s": 13.69},
    {"module": "feature/template/presentation", "count": 41, "s": 13.47},
    {"module": "service/network", "count": 21, "s": 1.42},
    {"module": "service/core/data", "count": 32, "s": 1.33},
    {"module": "service/core/domain", "count": 14, "s": 1.30}
  ],
  "slowest_test_classes": [
    {"cls": "com.example.androidproject1.core.ui.screenshot.PreviewScreenshotTest", "s": 85.53},
    {"cls": "com.example.androidproject1.feature.auth.presentation.screenshot.PreviewScreenshotTest", "s": 36.51},
    {"cls": "com.example.androidproject1.feature.catalog.presentation.categories.CategoriesScreenTest", "s": 34.40},
    {"cls": "com.example.androidproject1.feature.catalog.presentation.screenshot.PreviewScreenshotTest", "s": 34.09},
    {"cls": "com.example.androidproject1.AppNavHostAnalyticsTest (devDebug)", "s": 29.89},
    {"cls": "com.example.androidproject1.AppNavHostAnalyticsTest (stagingDebug)", "s": 29.71},
    {"cls": "com.example.androidproject1.AppNavHostAnalyticsTest (prodDebug)", "s": 29.49},
    {"cls": "com.example.androidproject1.feature.cart.presentation.screenshot.PreviewScreenshotTest", "s": 27.86},
    {"cls": "com.example.androidproject1.feature.devmenu.presentation.devmenu.DevMenuScreenTest", "s": 26.78},
    {"cls": "com.example.androidproject1.feature.profile.presentation.profile.ProfileScreenTest", "s": 25.79},
    {"cls": "com.example.androidproject1.feature.cart.presentation.cart.CartItemCountCzechTest", "s": 25.48},
    {"cls": "com.example.androidproject1.feature.settings.presentation.screenshot.PreviewScreenshotTest", "s": 24.31},
    {"cls": "com.example.androidproject1.feature.onboarding.presentation.screenshot.PreviewScreenshotTest", "s": 24.11},
    {"cls": "com.example.androidproject1.feature.gallery.presentation.gallery.GalleryScreenTest", "s": 23.72},
    {"cls": "com.example.androidproject1.feature.home.presentation.screenshot.PreviewScreenshotTest", "s": 22.23}
  ],
  "roborazzi_s": 177.28,
  "warnings": [
    "w: .../build-logic/src/main/kotlin/AndroidRoomConventionPlugin.kt:34:49 'fun srcDir(srcDir: Any): Any' is deprecated. Use `directories` mutable set instead.",
    "w: .../feature/template/presentation/src/test/kotlin/.../screenshot/PreviewScreenshotTest.kt:60:19 'fun createComposeRule(effectContext: CoroutineContext = ...): ComposeContentTestRule' is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead.",
    "w: .../PreviewScreenshotTest.kt:69:17 This declaration needs opt-in. Its usage should be marked with '@com.github.takahirom.roborazzi.ExperimentalRoborazziApi' or '@OptIn(...)'  (repeats at lines 71,72,73,79,80, and in every module's copy of this file)",
    "w: .../TemplateScreenTest.kt:36:19 'fun createComposeRule(...)' is deprecated. Use v2 createComposeRule instead. (repeats per-module, same pattern)",
    "w: .../TemplateArgsScreenTest.kt:27:19 same createComposeRule deprecation",
    "[Incubating] Problems report is available at: file:///.../build/reports/problems/problems-report.html (configuration-cache problems report generated on the `build` run)",
    "WARNING: A restricted method in java.lang.System has been called (Robolectric native runtime, java.lang.System::load) — recurring on every Robolectric-backed test task",
    "WARNING: Restricted methods will be blocked in a future release unless native access is enabled"
  ],
  "repo_size": "12.48 MiB (.git, count-objects -vH: 1396 loose objects 12.48MiB + 6706 in 3 packs at 1.68MiB)",
  "goldens_size": "~11.5 MB across 349 PNGs in 11 src/test/screenshots directories (core/ui 3.4M, catalog 1.4M, profile 1.2M, devmenu 1.1M, gallery 916K, settings 760K, template 768K, auth 640K, onboarding 608K, home 364K, cart 376K)",
  "observations": [
    "doctor.py and ktlintCheck are fast and not a bottleneck (2.7s and 3.5s respectively, the latter mostly build-cache hits).",
    "`./gradlew help` with configuration cache warm is 0.7s; the stated 19s doctor.py wall time from the task brief did not reproduce here (measured 2.7s) — doctor.py itself is not the drag.",
    "test_scripts.py measured at 441.9s (7:22) wall, far above the ~20-60s the codebase's own docs and PLAN.md status line imply ('test_scripts 52'). Root cause found in code, not just contention: scripts/test_scripts.py:73 calls `shutil.copytree(REPO_ROOT, self.repo, ...)` in `setUp()`, which unittest runs before EVERY one of the 56 test methods — so the ~56-module repo tree (minus build/.gradle/.git/screenshots) is copied to a fresh temp dir 56 separate times in one run, serially. This run also overlapped with a concurrent `./gradlew build` and `verifyRoborazziDebug`, which inflated it further via disk contention (34.5s user / 166.2s system time — mostly I/O, not CPU) — so the isolated number is lower than 441.9s but the repeated-copytree design is still the structural cost regardless of load.",
    "`./gradlew build` (the full gate) finished in 28s with 3859 actionable tasks (2059 executed, 1455 FROM-CACHE, 345 up-to-date) — this is misleadingly fast because it rode a warm local build cache from the immediately preceding one-module-test and ktlintCheck runs; a genuinely cold `build` on this 56-module, 3-flavor x 4-build-type template would take much longer. The task count itself is the real finding: plain `./gradlew build` assembles, lints, dexes and merges resources for all 12 variants (dev/staging/prod x debug/release/nonMinifiedRelease/benchmarkRelease from the baselineprofile plugin) rather than the one variant a developer is iterating on — the slowest-task list is dominated by per-flavor `mergeXJavaResource`/`mergeExtDex`/lint tasks that are near-duplicates of each other (e.g. mergeStagingDebugJavaResource, mergeProdNonMinifiedReleaseJavaResource, mergeDevBenchmarkReleaseJavaResource all present, all sub-2s but numerous).",
    "The test-results XML sum (923 tests, 772.6s total) is much larger than the fact sheet's '498 tests' because `build` runs `testDebugUnitTest`-equivalents for all three flavors (dev/staging/prod), not just once — e.g. AppNavHostAnalyticsTest appears three times in the slowest-class list at ~29-30s each, once per flavor, testing identical code paths. Unit tests are flavor-independent in this template (no flavor-specific source sets under test), so this is 3x redundant CI/local time for zero additional coverage.",
    "verifyRoborazziDebug (349 screenshot goldens) took 2m57s in this run, and PreviewScreenshotTest is consistently the single slowest test class per module (85.5s in core/ui alone, the module with the most components) — screenshot verification is a meaningful and correctly-isolated slice of total test time, not obviously wasteful given it only runs in `build`/`verifyRoborazziDebug`, not in plain `test`.",
    "No configuration-cache invalidation problems, deprecation gate failures, or 'not cacheable' warnings were seen beyond the ones listed; the codebase's own doctor.py/ktlint/build layers are all healthy in isolation — the DX cost, where it exists, is concentrated in (a) `build` doing 12 variants' worth of work for a 1-variant edit-test loop, and (b) test_scripts.py's per-test full-tree copy.",
    "Git repository itself is small and not a DX concern (12.48 MiB total object store); the ~11.5 MB of screenshot goldens live in the working tree/history as committed PNGs, consistent with the fact sheet's ~11 MB figure."
  ]
}
```
