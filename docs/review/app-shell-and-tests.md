# App shell, sample features, and how they are tested
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

## Summary
The shell is the best part of this area: MainViewModel/SessionState, the flat tab back stack and the Navigation-3 glue are sound, well-commented and would pass review at a product company; the screen unit and the layer rules are strict but mostly earn their keep. The test suite is where the effort leaks: of ~1100 s of recorded test wall time, ~660 s is 349 pixel-exact screenshot tests, ~300 s is per-module Robolectric JVM warm-up (a direct cost of 56 modules), and ~100 s is the :app tests running once per flavor — while the ~110 ViewModel tests that carry most of the behavioural value finish in under 20 s. The mandated XScreenTest per screen mostly asserts template plumbing (a tag renders, a tap emits an event). The two structural changes with real payoff are cutting the golden matrix and test-variant duplication, and dropping the per-feature `di` module; everything else is trimming.

## Winners
### MainViewModel + SessionState + rememberNavBackStack composed on the first frame

Single owner of the flow, Unknown holds the system splash, the back stack is a rememberSaveable composed unconditionally and the flow is asserted from the stack's first key rather than a remembered flag — this is the process-death story most apps get wrong, and here it is right and explained in place.
Evidence: `app/src/main/kotlin/com/example/androidproject1/MainActivity.kt:35-36`; `app/src/main/kotlin/com/example/androidproject1/MainActivity.kt:58-67`; `app/src/main/kotlin/com/example/androidproject1/MainViewModel.kt`
### The flat tab back stack (TopLevelDestination.selectTab / currentTab)

One list, no per-tab Saver, per-tab history survives process death for free, Back from a tab root lands on the previously visited tab — 30 lines that replace nested graphs, and TopLevelDestinationTest (14 tests, 0.3 s) pins the segment arithmetic.
Evidence: `app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt`; `app/src/test/kotlin/com/example/androidproject1/TopLevelDestinationTest.kt`
### ViewModel tests as the behavioural backbone

~110 plain-JVM tests (e.g. HomeViewModelTest's undo-fires-once, CartViewModelTest's 17 cases) run in under 20 s total and read as product behaviour, not plumbing. This is the category with by far the best value per second.
Evidence: `feature/home/presentation/src/test/kotlin/com/example/androidproject1/feature/home/presentation/home/HomeViewModelTest.kt:39-91`; `feature/cart/presentation/src/test/kotlin/com/example/androidproject1/feature/cart/presentation/cart/CartViewModelTest.kt`
### KoinGraphTest, the Room migration tests and CartItemCountCzechTest

Each is one small test that catches a whole class of launch-time or ship-time failure the compiler cannot: an unbound dependency, a version bump without a migration, a Czech plural that silently falls back to 'other'. Exactly the kind of cheap, high-leverage wiring test a template should carry.
Evidence: `app/src/test/kotlin/com/example/androidproject1/KoinGraphTest.kt`; `feature/cart/data/src/test/kotlin/com/example/androidproject1/feature/cart/data/database/CartDatabaseMigrationTest.kt:69-97`; `feature/cart/presentation/src/test/kotlin/com/example/androidproject1/feature/cart/presentation/cart/CartItemCountCzechTest.kt`
### Cross-feature navigation as lambdas wired in AppNavHost

cartDestination(onPickProduct), productDetailDestination(onAddToCart), settingsDestination(navigateToDebugMenu = null in prod) — the standard answer to feature isolation, and the DebugMenu.ENABLED const folding the gallery out of prod is a neat, R8-friendly touch.
Evidence: `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:245-300`; `feature/cart/presentation/src/main/kotlin/com/example/androidproject1/feature/cart/presentation/cart/CartDestination.kt`

## Problems (ranked by the reviewer)
### P1 · Test wall time is spent on repetition, not on assertions

**high · inefficient · effort M · confidence 0.85**

From the recorded XML: 1098 s of test time across modules. 349 PreviewScreenshotTest cases account for ~660 s (60%). The first Robolectric test in each module costs 20-26 s of JVM + SDK warm-up (LoginScreenTest 23.4 s, HomeScreenTest 25.1 s, GalleryScreenTest 25.7 s, AppButtonTest 24.4 s, DevMenuScreenTest 23.9 s, migration tests 17.5 s each) — roughly 300 s across ~16 modules, a direct tax of the 56-module split. :app's 45 tests run three times, once per flavor (97 s, of which 90 s is AppNavHostAnalyticsTest booting Robolectric three times). CI then runs `./gradlew build` (all unit tests) followed by `verifyRoborazziDebug`, which re-executes every presentation module's Robolectric suite with the roborazzi flag flipped. Meanwhile the ~110 ViewModel tests, the ~140 service unit tests and the 55 data tests together finish in well under a minute.

**Proposal.** (1) In the application convention plugin disable unit tests for every variant except devDebug (`androidComponents.beforeVariants { it.enableUnitTest = it.flavorName == "dev" && it.buildType == "debug" }`), and in the library plugins disable release unit tests — one edit, a third of :app's test time gone. (2) Make CI's test step `./gradlew build -x test` (or `assemble lint ktlintCheck`) followed by `testDevDebugUnitTest verifyRoborazziDebug`, so the Robolectric suite runs once. (3) Cut the golden matrix (next finding) which removes most of the 660 s. Together this should halve the CI test phase without deleting one behavioural assertion.

Evidence: `app/build/test-results/ (testDevDebugUnitTest, testStagingDebugUnitTest, testProdDebugUnitTest: 45 tests each, ~32 s each, AppNavHostAnalyticsTest ~30 s in each)`; `feature/home/presentation/build/test-results/ (both testDebugUnitTest and roborazzi directories — the suite runs twice)`; `.github/workflows/build.yml:73-81`; `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt (no beforeVariants { enableUnitTest } — every flavor runs unit tests)`
### P2 · 349 pixel-exact goldens is the wrong trade for a template

**high · over-enforced · effort S · confidence 0.8**

@ScreenPreview stamps five variants — Pixel 7 (411 dp), 'Narrow phone' (360 dp), Tablet 800x1280 @240 dpi, Dark, Large font — on all 18 screens, and @ComponentPreview three on 47 components: 90 + 141 + extra component previews in features = 349 PNGs, 11 MB in git. Narrow phone vs Pixel 7 is a 51 dp difference that almost never changes a layout; the tablet variant is the largest file and only means something on the two catalog list-detail screens. There is no Roborazzi comparison threshold, so the match is pixel-exact: a Robolectric or Compose BOM bump that changes anti-aliasing re-records all 349 with no app change, and an AppButton padding change re-records every screen that contains a button — effectively all 90 screen goldens plus the button's three. The template feature alone carries 33 goldens (768 KB) that assert the shape of placeholder text. Every re-record is a PR reviewer scrolling images nobody looks at, which the code's own comment admits ('a golden nobody looked at is a test that passes forever').

**Proposal.** Keep component goldens in :core:ui (the design system is what screenshots are good at) but trim @ComponentPreview to Light + Dark (94 files). For screens, keep two variants — Phone light and Phone dark with fontScale 1.5 combined — giving 36 screen goldens, and put the Tablet variant only on the two catalog pane screens via an explicit extra @Preview. Set a small change threshold (`roborazzi.compare.changeThreshold` ~0.5%) so a toolchain bump does not re-record the world. Drop the screenshot test from feature/template (generator source does not need goldens; `create_feature.py` can still clone the test class). Result: ~140 files instead of 349, and a re-record that a reviewer can actually inspect.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/common/Previews.kt:30-41`; `core/ui/src/test/screenshots (141 files, 3.4 MB); feature/catalog/presentation/src/test/screenshots (49, 1.4 MB); feature/template/presentation/src/test/screenshots (33 files for a feature nobody ships)`; `feature/home/presentation/src/test/kotlin/com/example/androidproject1/feature/home/presentation/screenshot/PreviewScreenshotTest.kt:48-60 (no comparison threshold; exact match)`
### P3 · One Robolectric XScreenTest per screen is mandated but mostly asserts template plumbing

**medium · repetitive · effort S · confidence 0.7**

Read five: ProductDetailScreenTest has three tests, two of which assert that tapping the heart emits FavouriteToggled — once favourited, once not, same event. HomeScreenTest and CartScreenTest assert 'list renders / empty state renders / tap emits event'. Only a few tests carry state-dependent logic worth pinning (Login submit disabled until valid, Settings theme option selected, Settings debug entry hidden in prod). ~85 screen tests exist; they would fail only if someone deleted a testTag or unwired an onClick, both of which the goldens and Maestro also catch. Each is the first Robolectric test in its module and so pays the 20-25 s warm-up. doctor.py enforces the file's existence, so a generated screen ships a test that asserts a placeholder counter increments.

**Proposal.** Make XScreenTest optional and say when it earns its place: a screen with conditional rendering (empty/error branch, enabled gating, per-build entries) gets one; a screen that only maps state to rows and taps to events does not. Change doctor.py's unit check to require the six main files plus XViewModelTest, and have create_screen.py generate the screen test only behind a flag. Keep LoginScreenTest as the documented pattern. Net: fewer files per screen, the same behavioural coverage.

Evidence: `feature/catalog/presentation/src/test/kotlin/com/example/androidproject1/feature/catalog/presentation/productdetail/ProductDetailScreenTest.kt:35-58`; `feature/template/presentation/src/test/kotlin/com/example/androidproject1/feature/template/presentation/template/TemplateScreenTest.kt:49-68`; `feature/auth/presentation/src/test/kotlin/com/example/androidproject1/feature/auth/presentation/login/LoginScreenTest.kt:55-107`; `scripts/doctor.py ('every screen is a complete eight-file unit')`
### P4 · A `di` Gradle module per feature is a module whose only content is a Koin list

**medium · over-engineered · effort M · confidence 0.7**

Ten `di` modules exist so that something can see both `presentation` and `data` without either seeing the other. Three of them (home, gallery, devmenu) register a single ViewModel; the largest (catalog, 59 lines) is still just bindings. Each is a Gradle project with its own configuration, AAR, lint pass and manifest merge, and pushes the count to 56 modules — which is what the ~300 s of per-module Robolectric warm-up and the configuration time the other agent is measuring both scale with. Now in Android's answer is that bindings live beside the classes they bind and only the app assembles them; Koin supports that just as well. The `domain` modules are a different case: they are JVM-only, cost almost nothing and enforce the Android-free rule via the compiler, so a 2-file domain (profile: Profile.kt + ProfileRepository.kt) is still proportionate.

**Proposal.** Delete the `di` layer. `presentation` exposes `val homePresentationModule = module { viewModelOf(::HomeViewModel) }`; `data` exposes its repository/data-source/Room bindings; `:core:di`'s `appModules()` includes both lists (it already depends on everything transitively through `di`'s `api`). Update `create_feature.py`/`delete_feature.py` and doctor.py's 'every feature di module is wired into Koin' to check the two lists instead. Ten modules and ten build files fewer, one registration step fewer, and a template that scales to 20 features at 3 modules each instead of 4.

Evidence: `feature/home/di/src/main/kotlin/com/example/androidproject1/feature/home/di/HomeModule.kt (13 lines, one viewModelOf)`; `feature/gallery/di/src/main/kotlin/com/example/androidproject1/feature/gallery/di/GalleryModule.kt (15 lines)`; `feature/devmenu/di/src/main/kotlin/com/example/androidproject1/feature/devmenu/di/DevMenuModule.kt (17 lines)`; `feature/cart/di/build.gradle.kts`; `settings.gradle.kts:91-158`
### P5 · AppNavHost carries cart business logic on a composition-scoped coroutine

**medium · bug · effort S · confidence 0.85**

'Add to cart' from product detail is implemented in the nav host: look up the product, build a CartItem, insert — inside `rememberCoroutineScope().launch`. That scope dies with AppNavDisplay's composition, so a rotation or backgrounding in the half-second after the tap cancels the insert silently; the comment claims the opposite ('so it is not cancelled either way'). The Product→CartItem mapping is duplicated verbatim at lines 157 and 270 (the picker path), and both swallow a Failure or a missing product with no snackbar. This is the one place in the shell that would fail a product-company review: the host is supposed to be glue, and here it is the only implementation of a user-visible write.

**Proposal.** Add an `AddProductToCart` use case in `:feature:cart:domain` that depends on `:feature:catalog:domain` (domain→domain is already allowed, see settings→auth) and does the lookup + insert, returning an Outcome. Bind it in the cart's Koin module, inject it into ProductDetailViewModel and CartViewModel, and run it through `execute {}` so it gets error handling and a snackbar for free. `ProductDetailNavigation.AddToCart` and the `onAddToCart`/`onProductPicked` lambdas disappear, and AppNavHost loses ~40 lines of logic it should never have had.

Evidence: `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:149-165`; `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:262-278`; `app/src/main/kotlin/com/example/androidproject1/MainViewModel.kt (CatalogRepository injected into the shell for deep-link path synthesis)`
### P6 · Robolectric's SDK pin is copied into 50 files and the toolchain bump will touch all of them

**medium · repetitive · effort S · confidence 0.8**

Every Robolectric test file declares `private const val ROBOLECTRIC_SDK = 35` plus the same explanatory comment, and CLAUDE.md asks new files to repeat it. When Robolectric ships an SDK 36 image (or the pin has to move for a Compose BOM that needs a newer platform), that is a 50-file diff for a one-number change. Robolectric's own mechanism for this — `robolectric.properties` on the test classpath, or a single `@Config` constant in a shared fixture — exists precisely so the pin lives once. The same duplication is what makes the eleven PreviewScreenshotTest copies differ only in a package string.

**Proposal.** Put `sdk=35` in one `robolectric.properties` shipped from `:service:core:ui`'s testFixtures resources (or expose `object RobolectricConfig { const val SDK = 35 }` there) and delete the 50 private constants and their comments. For PreviewScreenshotTest, keep the per-module class (the scanner needs the module classpath) but reduce it to a five-line subclass of a base in `:core:ui`'s testFixtures that takes the package to scan.

Evidence: `grep 'const val ROBOLECTRIC_SDK' → 50 files, 0 of them in testFixtures`; `feature/cart/presentation/src/test/kotlin/com/example/androidproject1/feature/cart/presentation/cart/CartItemCountCzechTest.kt:2-16 (the same three-line justification comment, repeated)`; `no src/test/resources/robolectric.properties anywhere`
### P7 · Onboarding is a fourth Gradle stack and a third app flow for one stored boolean

**low · useless · effort M · confidence 0.55**

Onboarding proves 'a first-run flag gates a flow' — but that is already proven by the session gating Login, and the price is four modules, an extra SessionState case, 28 tests (80 s, mostly Robolectric warm-up and 18 goldens) and a third `xEntries` block in AppNavHost. A pager with three static pages is not something a team copies from a template; they write their own. Of the other sample features: home earns its place (tab root, snackbar-undo pattern), cart/catalog carry Room, list-detail, nav results and deep links, profile carries the permission API, settings carries confirm-then-act and theme, devmenu is prod-stripped tooling a real app wants, and gallery is the browsable design system — filler only insofar as the 27 unused components it justifies are.

**Proposal.** Fold onboarding into auth: a `first_run` flag in the auth data source, the pager as a screen in `:feature:auth:presentation` that LoginDestination shows before the form, and SessionState back to Unknown/SignedIn/SignedOut. If the pager is worth keeping as a demonstration, keep only its screen unit; the domain/data/di modules add nothing over the auth ones. Four modules fewer and MainViewModel loses a combine.

Evidence: `feature/onboarding/presentation/src/main/kotlin/com/example/androidproject1/feature/onboarding/presentation/onboarding/OnboardingViewModel.kt (43 lines: page++ and markSeen)`; `feature/onboarding/domain/src/main/kotlin/com/example/androidproject1/feature/onboarding/domain/OnboardingRepository.kt (19 lines)`; `app/src/main/kotlin/com/example/androidproject1/MainViewModel.kt (onboardingSeen combined into SessionState; SessionState.Onboarding)`; `feature/onboarding/presentation/src/test/screenshots (18 goldens, 608 KB)`
### P8 · Test fixtures are not where the rule says they are

**low · inconsistent · effort S · confidence 0.75**

CLAUDE.md says a fake lives in testFixtures of the module that owns the type and 'never write a second copy'. Cart, catalog, onboarding, settings and auth follow it; profile's fake sits in the presentation module's test tree, so the day :app or another feature needs it, the second copy the rule forbids is the path of least resistance. doctor.py has no check for this, so the drift is invisible.

**Proposal.** Move FakeProfileRepository into feature/profile/domain testFixtures and apply `java-test-fixtures` there; then either make the convention plugin apply `java-test-fixtures` to every feature domain module by default (so the placement is never a decision) or drop the rule to 'a fake goes beside the interface when a second module needs it'. Small either way; the point is to pick one.

Evidence: `feature/profile/presentation/src/test/kotlin/com/example/androidproject1/feature/profile/presentation/FakeProfileRepository.kt (in a presentation test source set, not domain testFixtures)`; `feature/profile/domain/build.gradle.kts (no java-test-fixtures plugin)`; `feature/cart/domain/build.gradle.kts (has it, with a comment explaining why)`

## Looks heavy but is justified
- The eight-file screen unit in a directory per screen: XNavigation with one case and a 20-line Destination look like ceremony, but they are the seam that keeps cross-feature navigation as lambdas and lets a ViewModel test assert navigation as data — the empty HomeNavigation is fine because it says 'this is a tab root' in one line, and merging Event/Navigation/State into one file saves a directory listing while losing the doctor checks that keep a screen file free of stray composables.
- Separate JVM `domain` modules even at two files: `convention.kotlin.jvm` costs no AAR, no manifest and no lint pass, and it is the compiler — not doctor.py — that keeps `android.*` out; the per-module cost that hurts is Robolectric warm-up, which domain modules never pay.
- MainViewModel holding the splash until session, first-run flag and theme are all read: three DataStore reads before the first frame, each with a safe fallback on failure, and it prevents a light-then-dark flash — the standard pattern.
- AppNavHostAnalyticsTest at 30 s: one Robolectric boot to prove ProvideAnalytics is actually composed is worth it, because nothing else can see that binding reach composition — it only needs to run once, not three times (see the first finding).
- The Room migration tests (3 tests, ~17 s per database): they fail on a version bump alone and pass once the migration exists, which is exactly the behaviour a template should hard-wire against fallbackToDestructiveMigration.
- Maestro as six weekly flows on a real emulator: the right cadence for a template — end-to-end proof that sign-in, add-to-cart, permissions and log-out still work, without blocking every PR on an emulator; qa.16 (tabs tapped by English text) is a known small fix.
- KoinGraphTest with hand-listed `injectedParameters`: the manual list is a wart, but Koin's verify() has no other way to see parametersOf, and doctor.py closes the gap; it is one 60-line test that replaces a crash at launch.
- feature/template with TemplateArgs: compiled into every build so the generator source cannot rot, and the args variant is the only worked example of a route with parameters — keep both screen units; just drop its 33 goldens (second finding).

## Questions only the owner can answer
- Is the screenshot suite meant to catch design-system regressions (component goldens) or screen-layout regressions (screen goldens)? The answer decides whether screens keep any goldens at all or rely on component goldens plus ViewModel tests.
- Do the dev/staging/prod flavors need to differ in anything a unit test could observe? If not, running unit tests only on devDebug is a free win; if yes, name the one test that should run per flavor and gate only that.
- Is the template's target 'one team, one product' (where 20 features is plausible and module count matters) or 'small app from a clean start' (where 56 modules is already the ceiling)? The di-module and onboarding proposals are worth more in the first case.
- Should Koin bindings live beside the classes they bind (Hilt-style, which removes the di modules) or stay in one per-feature module? This is the one structural choice the scripts, doctor.py and CLAUDE.md all encode, so it has to be a deliberate decision rather than a drive-by.
