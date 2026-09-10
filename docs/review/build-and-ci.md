# Build logic, Gradle and CI
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

## Summary
The build infrastructure is the strongest part of this template: ten small convention plugins, a namespace and applicationId derived from one gradle.properties line, six-line module build files, typed ProjectConfig, a catalog-only dependency policy that Renovate can actually drive, and a release keystore that never touches git. It is proportionate — Now in Android carries thirteen convention plugins for a similar module count — and the JDK/toolchain, config-cache, ktlint-only and Kover-as-signal choices are the right ones. The problems are concentrated in what CI actually executes and in two features that were bolted on late: the PR gate is `./gradlew build`, which assembles all six app variants with three R8 passes and runs every unit test once per variant while build.yml claims it only assembles debug; the baseline profile is generated into `src/devRelease` and never reaches prodRelease (confirmed from the path and the missing `mergeIntoMain`); three flavors buy 193 lines of difference at the cost of tripling app variants; and the release job publishes an APK rather than an AAB and will happily publish a debug-signed one to a GitHub Release when the keystore secret is absent. Fix the gate, wire or cut the baseline profile, drop staging, and harden the release job — everything else here is fine as it is.

## Winners
### One property renames the project: namespace, applicationId and launcher label are derived

`namespaceFromPath()` plus `basePackage`/`appName` in gradle.properties means init_project.py rewrites one line instead of 27 namespaces; doctor.py refuses any module that repeats SDK config. This is cleaner than Now in Android, which still writes `namespace` per module.
Evidence: `build-logic/src/main/kotlin/AndroidConventions.kt:67-70`; `gradle.properties:22-29`; `scripts/doctor.py:778`
### Six-line module build files with libraries owned by the convention plugins

Every feature layer is `plugins { alias(...) }` + project dependencies; the plugin carries the test stack, so a generated screen's test compiles with no build-file edit. Ten plugins for 56 modules is proportionate (NiA: 13 plugins, ~30 modules).
Evidence: `feature/auth/presentation/build.gradle.kts:1-10`; `build-logic/src/main/kotlin/FeaturePresentationConventionPlugin.kt:25-43`; `build-logic/build.gradle.kts:36-78`
### Typed ProjectConfig and a tag-driven versionName

SDK levels, Java target and flavors are Kotlin constants (a typo is a compile error), and `versionName` comes from the `v*` tag so the APK cannot disagree with the tag it was built from. `git()` never throws, so a source zip still configures.
Evidence: `build-logic/src/main/kotlin/ProjectConfig.kt:10-17`; `build-logic/src/main/kotlin/ProjectConfig.kt:65-99`
### Keystore handling and secrets scanning done the standard way

`keystore.properties` and `*.jks` are gitignored, CI writes them from secrets only in the release job, a present-but-incomplete file is an error rather than a silent debug-key fallback, and gitleaks runs on every PR with full history.
Evidence: `build-logic/src/main/kotlin/AndroidConventions.kt:38-48`; `.gitignore:45-49`; `.github/workflows/build.yml:19-37`; `.github/workflows/build.yml:132-141`
### Catalog-only dependencies, grouped Renovate, JDK pinned once for daemon and CI

Every version lives in libs.versions.toml (doctor.py enforces it), renovate.json groups androidx/kotlin/koin/AGP so a template's catalog does not rot, and `gradle-daemon-jvm.properties` + foojay give the same JDK 25 locally and in the three CI jobs that state it.
Evidence: `renovate.json:12-38`; `gradle/gradle-daemon-jvm.properties:12`; `.github/workflows/build.yml:61-65`
### Screenshots and coverage kept out of the ordinary build

Roborazzi is only exercised by `verifyRoborazziDebug`, so `./gradlew test` costs nothing extra; Kover has no threshold and is uploaded as an artifact — exactly the 'signal, not gate' posture a template should ship.
Evidence: `.github/workflows/build.yml:80-87`; `build.gradle.kts:33-46`

## Problems (ranked by the reviewer)
### P1 · The PR gate is `./gradlew build`, which assembles all six app variants (three R8 passes) and runs every unit test once per variant — and build.yml believes it only assembles debug

**high · inefficient · effort S · confidence 0.9**

`build` = `assemble` + `check`. With the `environment` dimension (dev/staging/prod × debug/release) `assemble` on :app builds six variants, three of them through R8 (optimization.enable = true at AndroidApplicationConventionPlugin.kt:66-68), and `check` runs :app's 45 tests six times and every library's tests twice (testDebugUnitTest + testReleaseUnitTest — including the Robolectric screen tests on 12 Compose modules). Nothing in build-logic filters variants (no `androidComponents.beforeVariants`, grep confirms). Meanwhile build.yml:103 says "`build` only assembles debug" and uses that to justify a separate weekly `release` job that runs `assembleProdRelease lintProdRelease` (build.yml:143-144) — so either the comment is wrong and every PR already pays for R8 three times (it does), or the weekly job is redundant. The same file also claims the conventions job "fails before the slow build starts" (build.yml:40-41) but there is no `needs:`; the jobs run in parallel. The local gate in `.claude/commands/check.md:10` and CLAUDE.md's 'before you call the work done' inherit the full cost. Now in Android's CI runs explicit tasks (`testDemoDebug`, `:app:lintProdRelease`, `:app:assembleDemoDebug`) and never `build`.

**Proposal.** Make the gate explicit and single-variant: PR = `ktlintCheck testDevDebugUnitTest :app:lintDevDebug :app:assembleDevDebug verifyRoborazziDebug` (or add `beforeVariants { if (buildType == "release") enableUnitTest = false }` in `convention.android.library` and restrict :app tests to `devDebug`, then keep `build`). Keep the weekly/tag `release` job as the only place R8 runs — that is what its comment already says it is for. Update the two stale comments and `check.md`. Ask the build-measuring agent for before/after wall time of `./gradlew build` vs the explicit task list.

Evidence: `.github/workflows/build.yml:73-74`; `.github/workflows/build.yml:102-106`; `.github/workflows/build.yml:39-41`; `.claude/commands/check.md:10`; `.github/workflows/build.yml:143-144`; `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:33-45`; `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:61-70`
### P2 · The baseline profile is recorded from the dev flavor into src/devRelease and never packaged into prodRelease (qa.15 confirmed)

**high · bug · effort S · confidence 0.95**

The producer targets `dev` via `missingDimensionStrategy("environment", "dev")` (baselineprofile/build.gradle.kts:14), so the AGP baseline-profile plugin writes the 6,164-line profile to `app/src/devRelease/generated/baselineProfiles/` — the variant source set, because the consumer never sets `baselineProfile { mergeIntoMain = true }` (grep of build-logic and app/build.gradle.kts finds no `mergeIntoMain`). `prodRelease`, the only APK the release job ships, therefore has no profile: the whole benefit is lost and `:app:generateDevReleaseBaselineProfile` is a device-attached ritual that produces a committed file nothing reads. Two further problems: the profile is recorded against the dev app (`PACKAGE = "com.example.androidproject1.dev"`), whose startup path loads JSON fixtures through MockEngine (app/src/dev/kotlin/.../network/NetworkEngine.kt, 108 lines) that prod never executes; and the module pins `benchmark = "1.5.0-rc02"`, a pre-release in a catalog whose renovate.json:33-37 rule says a template starts on stable. It also needs its own convention plugin (`convention.android.test`, 26 lines) and an exclusion in the root Kover aggregation (build.gradle.kts:41-45).

**Proposal.** Decide once: (a) keep it and make it real — `baselineProfile { mergeIntoMain = true }` in the app plugin, record against the prod flavor (`missingDimensionStrategy("environment", "prod")`, a `prod` package name), and move to the stable benchmark release; or (b) cut `:baselineprofile`, `convention.android.test`, the Kover exclusion and the committed profile, and leave a two-line note that Google's own `baselineprofile` module template adds it back in ten minutes. For a template that ships no real startup path, (b) is the honest choice; (a) is one line if the owner wants the demo.

Evidence: `app/src/devRelease/generated/baselineProfiles/baseline-prof.txt`; `baselineprofile/build.gradle.kts:10-15`; `baselineprofile/src/main/kotlin/com/example/androidproject1/baselineprofile/StartupBaselineProfile.kt:16-18`; `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:15-17`; `app/build.gradle.kts:37-38`; `gradle/libs.versions.toml:2`; `renovate.json:33-37`
### P3 · Three environment flavors for 193 lines of difference — staging is prod with a different label

**medium · over-engineered · effort S · confidence 0.7**

The flavor source sets total 193 lines: dev = 108 (fixture MockEngine) + 33 (debug menu), staging = 13 + 13, prod = 13 + 13. Staging differs from prod only in `DebugMenu.ENABLED = true` and a `%s Staging` label; its BASE_URL is a placeholder (`https://staging.example.com/`, ProjectConfig.kt:54) with nothing behind it. The dimension costs every :app task ×3 (six variants for assemble/test/lint), forced D35 to put the screenshot test in every presentation module because one in :app "runs three times over the flavors" (docs/PLAN.md:113), and makes every consumer of :app name a flavor (baselineprofile:14, build.yml:219, Maestro's appId). Now in Android — the reference for this — ships exactly two flavors, `demo` (fixtures) and `prod`, and that is the industry norm for a template: the dev/fixtures split is the one with real content. Staging is the classic third flavor a product company adds the day it has a staging backend, which is one enum entry here by the file's own comment.

**Proposal.** Cut to `dev` (fixtures + debug menu) and `prod`: delete the `STAGING` entry and `app/src/staging/`, and let `DebugMenu.ENABLED` follow `BuildConfig.DEBUG` rather than the flavor so a `prodDebug` build still has the menu. Record in Decisions that staging returns as one `Flavor` entry plus a source set when a staging URL exists. Halves app variants; nothing else changes.

Evidence: `build-logic/src/main/kotlin/ProjectConfig.kt:47-56`; `app/src/staging/kotlin/com/example/androidproject1/debug/DebugMenu.kt:1-13`; `app/src/staging/kotlin/com/example/androidproject1/network/NetworkEngine.kt`; `app/src/prod/kotlin/com/example/androidproject1/debug/DebugMenu.kt`; `docs/PLAN.md:113`; `baselineprofile/build.gradle.kts:14`; `.github/workflows/build.yml:218-219`
### P4 · The release job ships an APK, will publish a debug-signed one when the keystore secret is missing, and `rev-list --count` versionCodes go backwards on a hotfix branch

**medium · bug · effort S · confidence 0.85**

Three standard-practice gaps in one job. (1) `assembleProdRelease` + `app/build/outputs/apk/...` (build.yml:144,150): Google Play has required AAB for new apps since 2021, so the artifact this pipeline produces cannot be uploaded to the store a product company ships to; `bundleProdRelease` is the task, and the APK can stay as a sideload artifact. (2) The keystore step is guarded by `if: env.KEYSTORE_BASE64 != ''` (build.yml:133) but the assemble and publish steps are not: on a tag pushed before the four secrets exist, the build falls back to `signingConfigs.getByName("debug")` (AndroidApplicationConventionPlugin.kt:63) and `gh release create` (build.yml:166) publishes a debug-signed APK as the GitHub release for that tag — a trap the next developer meets on their first tag. The fallback is right for local and PR builds, wrong on a tag. (3) `versionCode = git rev-list --count HEAD` (ProjectConfig.kt:77-80) is monotonic only along one branch: a `v1.2.1` hotfix tagged on a branch cut at `v1.2.0` has fewer commits than main's later `v1.3.0`, so the store rejects it as a downgrade. Common alternatives are encoding the tag (`1.2.1` → 10201) or a CI-supplied `-PversionCode=$GITHUB_RUN_NUMBER`; encoding the tag keeps the 'a release is a tag' property this template likes.

**Proposal.** Release job: build `bundleProdRelease` (and the APK as a secondary artifact), add a step that fails when `github.ref` is a tag and `KEYSTORE_BASE64` is empty, and derive `versionCode` from the tag (`major*10000 + minor*100 + patch`) with the commit count as the fallback for non-tag builds. Keep the `--generate-notes` release creation; it is good.

Evidence: `.github/workflows/build.yml:143-151`; `.github/workflows/build.yml:130-141`; `.github/workflows/build.yml:158-168`; `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:47-63`; `build-logic/src/main/kotlin/ProjectConfig.kt:77-80`
### P5 · `configureCompose` puts Coil and the Robolectric test stack on every Compose module, and two plugins exist for one module each

**low · over-engineered · effort S · confidence 0.75**

The rule 'a module build file is a plugins block and project dependencies, nothing else' (enforced by doctor.py:778 for SDK config, and by convention for libraries) has two side effects worth naming. First, a library that only one module needs still has to go into a plugin: `configureCompose` adds `coil-compose` + `coil-network` to all 13 Compose modules (AndroidConventions.kt:133-134) while the comment beside it says only `:core:ui` may import them and doctor.py forbids it elsewhere — a dependency put on 12 modules so that a script can forbid it on 12 modules. Second, it produces single-consumer plugins: `convention.service.network` (one module, 28 lines) and `convention.android.test` (one module, 26 lines). There is also plain duplication: `configureCompose` already adds compose-ui-test-junit4, Robolectric, the test manifest and `isIncludeAndroidResources = true` (AndroidConventions.kt:140-146), and `FeaturePresentationConventionPlugin` adds all four again (lines 20-23, 36-40). None of this is expensive; it is the kind of thing that makes the next developer distrust the rulebook. NiA's split — plugins own configuration and the *shared* stacks; a module may still name a library only it uses — is the pragmatic line.

**Proposal.** Relax the rule to 'no SDK/Java/lint/namespace config in a module build file' (keep the doctor check exactly as it is) and let a module declare a library that only it uses: move Coil to `core/ui/build.gradle.kts`, the Ktor bundle to `service/network/build.gradle.kts` (on `convention.kotlin.jvm` + serialization), and delete `convention.service.network`. Remove the four duplicated test lines from `FeaturePresentationConventionPlugin`. `convention.android.test` goes with the baseline-profile decision.

Evidence: `build-logic/src/main/kotlin/AndroidConventions.kt:130-146`; `build-logic/src/main/kotlin/FeaturePresentationConventionPlugin.kt:20-23`; `build-logic/src/main/kotlin/FeaturePresentationConventionPlugin.kt:36-40`; `build-logic/src/main/kotlin/ServiceNetworkConventionPlugin.kt:13-27`; `build-logic/src/main/kotlin/AndroidTestConventionPlugin.kt:13-25`; `scripts/doctor.py:778`

## Looks heavy but is justified
- Ten convention plugins is not too many: Now in Android has thirteen for fewer modules, and each one here maps to a real module kind (jvm / library / compose / data / di / presentation / room / application) — merge only the two single-consumer ones named above.
- `build-logic` as an included build rather than `buildSrc`, with the catalog shared via `from(files("../gradle/libs.versions.toml"))` (build-logic/settings.gradle.kts:30-34): it is what lets export_service.py copy the plugins with `service/`, and it avoids buildSrc's whole-build recompilation.
- JDK 25 daemon with Java 17 target: the daemon JDK is pinned once in gradle/gradle-daemon-jvm.properties and stated in each CI job, `jvmTarget` is set explicitly for both Android and JVM modules (AndroidConventions.kt:108-112), and the reason detekt is absent is documented and re-tested — nothing to change until detekt 2.x is stable.
- `org.gradle.parallel/caching/configuration-cache` all on and honoured (git is read through `providers.exec`, ProjectConfig.kt:91-99, so the config cache stays valid); `-Xmx4096m` is the conventional number and Kotlin/test workers add their own heaps — whether 16 GB is tight under `parallel` with 12 Robolectric test tasks is for the build-measuring agent, not a change to make blind.
- Kover with no threshold, aggregated off the project tree (build.gradle.kts:41-46) so a new module is covered without an edit; ktlint driven purely from .editorconfig with four rules off for stated reasons — both are the right size for a template.
- `lint.checkDependencies = true` only on :app and `abortOnError` everywhere with a shared lint.xml of ~15 issues: standard, and the `GradleDependency` demotion to informational is correct when Renovate owns freshness.
- Weekly cadence for the `generators` (compile what the scripts write) and `maestro` (emulator) jobs, with `workflow_dispatch` for the latter: minutes of emulator per PR is not proportionate for six golden-path flows, and D30 records the choice.
- gitleaks downloaded as a binary rather than gitleaks-action (build.yml:29-37): the action needs a paid licence for org repos; a template must not depend on that. Dependency verification metadata and build scans are deliberately absent and should stay so — verification-metadata.xml under Renovate churn is a maintenance sink NiA also declined, and a Develocity endpoint is a per-company decision.
- `includeModule` failing at settings time when a directory or build file is missing (settings.gradle.kts:41-47): cheap, and it is the check that makes the generator scripts' registrations safe.

## Questions only the owner can answer
- Is the shipping target Google Play (AAB required) or sideload/enterprise distribution (APK fine)? The release-job proposal depends on it.
- Will a staging backend ever exist for the sample, or is `staging` there only to show that a third flavor is one enum entry? If the latter, that sentence in ProjectConfig is the documentation and the flavor can go.
- Does anyone read the macrobenchmark numbers from `:baselineprofile`'s StartupBenchmark? If not, wiring the profile is one line and the benchmark can be deleted with the decision.
- What is the PR wall-time budget on the runner you will actually use (GitHub-hosted 2-core vs a self-hosted Mac)? That decides whether the explicit-task gate is enough or whether the `build` job should also be split across runners (test vs lint+assemble vs screenshots).
- Hotfix policy: will releases ever be tagged on a branch other than main? If never, the `rev-list --count` versionCode is acceptable and only the AAB and keystore-guard fixes remain.
