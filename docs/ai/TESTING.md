# Testing

What each kind of test answers, what runs under Robolectric and why, and what the build reports
when you ask it. The rules are [../../CLAUDE.md](../../CLAUDE.md) § Checks; the recipes are
[RECIPES.md](RECIPES.md).

## Testing a screen

Two tests per screen, and they answer different questions:

| | Asks | Runs as |
|---|---|---|
| `XViewModelTest` | what the state becomes, and what navigation is emitted | plain JVM test |
| `XScreenTest` | what is on screen, and what a tap does | Robolectric unit test |

The second is the half a ViewModel test cannot reach. `LoginScreenTest` is the pattern: it renders
the stateless screen with a fixed state, collects the events it emits, and finds everything by
`testTag` — never by text, because copy gets reworded and translated. It runs under Robolectric as
an ordinary unit test, so `./gradlew test` covers it and CI needs no emulator.

**Robolectric's API level is pinned once**, in `build-logic/robolectric/robolectric.properties`,
which `configureRobolectricSdk` puts on every module's test resources. Robolectric ships an SDK
image per API level and has none for this project's `targetSdk`, so an unpinned test would not
start at all. It used to be a `const` and a paragraph in each of 51 test files. A test that needs a
different level still says so with `@Config(sdk = [...])`, which wins over the file.

One thing a screen test does have to say out loud, and it costs a comment in the file:

- **A compound component is tagged on its group.** A caller's `modifier` goes to the outermost
  element, so `Modifier.testTag("login_emailField")` on an `AppTextField` tags the label, input and
  supporting line together. Assertions about the group use the tag directly; a test that types
  reaches the input inside it with `hasSetTextAction() and hasAnyAncestor(hasTestTag(…))`.

`convention.feature.presentation` carries the dependencies, so a new screen's test needs no
build-file edit.

## Screenshots

A third test, but one per **module** rather than one per screen: `PreviewScreenshotTest` records a
golden image for every `@ScreenPreview` and `@ComponentPreview` on its own classpath and fails when
one of them changes. It is in every `presentation` module and in `:core:ui` (D35), because a
module's test sees only its own classpath — but the twelve copies differ by one package string, so
everything else is `PreviewScreenshotSpec` in `:service:core:ui`'s `testFixtures` and each subclass
is a dozen lines. `create_feature.py` clones `feature/template`'s copy, so a generated feature is
covered the day it is generated.

```bash
./gradlew recordRoborazziDebug   # write the goldens after a deliberate change
./gradlew verifyRoborazziDebug   # check them; the CI build job runs this
```

**The previews are the list.** Nothing is registered anywhere: adding a component with a
`@ComponentPreview` adds two goldens, adding a screen adds three, and deleting either leaves its
images for `git status` to point at. The images live in each module's `src/test/screenshots/` and
are committed — which is the one thing to be careful about, because **a golden nobody looked at is
a test that passes forever**. Open what `record` wrote before committing it; a blank or clipped
image asserts the blankness just as firmly as a correct one asserts the layout.

Two details the test file explains and that a new one must keep:

- **The clock is advanced by hand, one frame.** A screen holding a `CircularProgressIndicator`
  never reaches idle, and a capture that waits for idle waits forever.
- **`manualAdvance` is added to the preview's own options, not to fresh ones.** Those options carry
  the `@Preview`'s device, `uiMode` and `fontScale`; replace them and a screen's five variants come
  out as five identical files that assert nothing.

**An overlay needs a golden of its own**, and that is the one thing the previews are not the list
for. A dialog, a sheet, a menu and a picker each draw in a window of their own, and a capture of a
preview captures the composable — so all 349 goldens were green while the date picker was clipped
on a 360 dp phone. `OverlayScreenshotTest` in `:core:ui` is the answer: it pins the clock and the time zone first —
the date picker rings *today*, so without that these goldens fail once a day for a reason that
has nothing to do with the code — then opens the overlay and
uses `captureScreenRoboImage`, which captures the screen and so takes the window with it. Add an
overlay component and add a case there, recorded at the shapes an overlay actually breaks on —
the narrowest phone and a phone in landscape, not the comfortable 400×900 the previews use.

**The matrix is three per screen and two per component**, and both numbers were argued down from
five and three. A narrow phone and an 800 dp tablet were previewed on every screen and their images
were the phone's again, because no screen here has an adaptive layout — 140 images asserting a third
and fourth time what the phone already said. A screen that does respond to width carries its own
extra `@Preview` naming that width. Components lost their large-font variant because the gallery is
a screen, so its `@ScreenPreview` renders all of them at 1.5× already.

**Goldens are recorded at half size** (`roborazzi.record.resizeScale` in `gradle.properties`): a
screen preview is a 400x900 dp frame at xhdpi, so full size is 800x1800 px of mostly flat colour.
Half still shows a moved element, a clipped row or a wrong colour, and is a quarter of the bytes in
git. **And there is a comparison threshold**, `CHANGE_THRESHOLD`, so a Robolectric or Compose bump
that shifts antialiasing by a pixel does not re-record every golden and bury a real change in the
diff. It is 0.1 % of pixels, which is not blind: shifting every component by one dp fails it.

An ordinary `./gradlew test` leaves Roborazzi switched off, so the class costs the build nothing —
which is also why CI needs the separate `verifyRoborazziDebug` step.

## The design system's edges

`toColorScheme()` / `toTypography()` / `toShapes()` also populate Material's own theme, so
`service/core/ui`'s `Screen()` — which cannot depend on `:core:ui` without losing its portability —
picks the system up for free. `Previews.kt` lives in `:core:ui` rather than in `service/` for the
same reason in reverse: it references `AppTheme`.

`:service:core:ui`'s `permission/` package is the one part of `service/` whose tests need
Robolectric. It is a wrapper over `PackageManager` and the activity-result contract, so there is no
logic there to test without a shadowed framework, and `convention.android.library.compose` already
puts Robolectric on that module's test classpath. Everything else under `service/` is JVM-tested —
`R.string.x` is only an `Int` and `UiText` defers resolution, so keep it that way rather than
reaching for the framework to test logic.

`:service:core:ui` re-exports `FakeLogger` and `TestDispatchers` with `testFixturesApi`, so a screen
test needs one `testFixtures(projects.service.core.ui)` line and the convention plugin already adds
it.

## Build reports

`./gradlew assembleDevDebug -PcomposeMetrics` writes the Compose compiler's stability reports under
`build/compose-reports/<module>/`. Off by default because they cost a compiler pass on every module;
on when you want to know whether a state the code calls `@Immutable` is one the compiler agrees
about. `<module>-classes.txt` is the file to read: every `XState` should say `stable class`, with no
`runtime` or `unstable` member. The one exception is `UiState`, which is generic — its stability is
its type argument's, which is what `Parameter(Data)` on its `<runtime stability>` line means.

What the compiler cannot work out for itself is stated in `build-logic/compose-stability.conf`:
read-only collections, and the domain models, whose modules are Kotlin/JVM and so never see the
Compose compiler at all — it treats every class from one as unstable rather than unknown. A line
there is a promise, so do not add one to quiet a report that is right. Compose plugin options are
not task inputs, so a report needs `--rerun-tasks` (or a clean) to be regenerated.

`./gradlew koverHtmlReport` is coverage, and deliberately not a gate: there is no threshold, because
a number that has to be met gets met by tests written for the number. It is a signal — which module
the tests avoid. CI uploads it as an artifact. Previews and generated classes are filtered out.

ktlint's rule set lives in `.editorconfig`, not a second config file: `intellij_idea` style rather
than `ktlint_official`, with `class-signature`, `function-signature` and `parameter-list-spacing`
off (all three read a multi-line parameter list as if it were on one line, so every constructor in
the repo would be a violation) and `function-naming` off (a `@Composable` is PascalCase and a test
name is a backtick-quoted sentence).

**There is still no detekt.** Re-tested 2026-09-08 on 1.23.8, the current release: its embedded
Kotlin compiler rejects the JDK 25 the daemon is pinned to — it refuses `--jvm-target 25`, and once
that is pinned to 17 it fails on the JDK's version string instead. detekt 2.x is `2.0.0-alpha`.
Revisit when 2.x is stable.
