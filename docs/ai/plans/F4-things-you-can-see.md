# Sprint F4 · Things you can see

Sprint: F4 · Things you can see
Status: done 2026-09-19
When: 2026-09-17 09:00 → 2026-09-18 18:00
Goal: A UI sprint — one bug, two screens, a showcase and two palette additions, each of them something a person can look at on the device
Release: F
Agents: 1 · 64 points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D75–D78

Three sprints of release F fixed the process; nothing in them changed what the app looks like. This
one takes the five UI lines the owner named from [../../BACKLOG.md](../../BACKLOG.md), in the
owner's order: the one open bug first — the description list's label collapses to a letter per
line at a large font scale — then a language picker in Settings, a shared-element transition from
a product row to its detail, the component playground behind the dev menu (was C1U6), and
feedback roles in the palette. Before any of them, one DevOps line: `/check pr` gains
`:app:lintDevDebug`, the check that would have caught F3X2 and the only one CI ran that the local
gate does not, because CI is off (D73) and the local gate is the whole gate.

What it leaves alone: `service/`, `core/di` and `scripts/` are not touched except where a task
strictly forces it — a generator run, a Koin binding for a screen the generator registers. The 6
band was rewritten in `PROCESS.md` § Points before this draft, as the F3 retrospective asked; the
estimates below use the rewritten bands. Two screens were drawn first, as `RECIPES.md` § A new
screen says: the canvases are on their briefs' *Read* lines.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

What each task writes. With one agent nothing is arbitrated; with two, a task owns its row.

| File group | Task |
|---|---|
| `CLAUDE.md` § Checks, `.claude/commands/check.md` | F4P1 |
| `core/ui/**/component/AppDescriptionList.kt`, `core/ui/src/test/**/AppDescriptionListTest.kt`, goldens of every screen that composes it | F4X1 |
| `feature/settings/{domain,presentation,di}/**`, `app/**/MainActivity.kt`, `app/**/ApplicationModule.kt`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/**`, `build-logic/**/AndroidApplicationConventionPlugin.kt`, `gradle/libs.versions.toml`, `app/**/AppNavHost.kt` (the generator's registration) | F4U1 |
| `core/ui/**/theme/Motion.kt` or a new `core/ui/**/layout/SharedElement.kt`, `app/**/AppNavHost.kt` (the scope), `feature/catalog/presentation/**/{products,productdetail}/*Screen.kt` | F4S1 |
| `feature/devmenu/presentation/**`, `feature/devmenu/di/**`, `app/**/AppNavHost.kt` (the generator's registration) | F4U2 |
| `core/ui/**/theme/Color.kt`, `core/ui/**/component/{AppBanner,AppToast,AppTag,AppStatusDot,AppAvatar}.kt`, `core/ui/src/test/**/ContrastTest.kt`, `feature/gallery/presentation/**/GalleryCatalog.kt` | F4U3 |
| `docs/ai/reference/{FEATURES,DESIGN-SYSTEM,CORE}.md`, `docs/ai/DEPENDENCIES.md`, `docs/DECISIONS.md` | the task that changes the fact |

`app/**/AppNavHost.kt` is written by three tasks, one after another — the generator's one-line
registration in F4U1 and F4U2, the `SharedTransitionLayout` in F4S1 — which is fine with one agent
and is why `Agents:` says 1. `scripts/` is untouched, so `test_scripts.py` does not fire; `core/`
and `build-logic/` are, so the whole `./gradlew test` does.

## Tasks

### F4P1 `/check pr` runs lint · 3

**Why** CI is off (D73), so the pull-request body's `/check pr` tail is the whole gate — and T1
does not run `:app:lintDevDebug`, the check that failed on F1H1's missing permission the first
time CI ran (F3X2). Three days of merges went through a gate lint was not part of.
**Done when** `grep -n 'lintDevDebug' CLAUDE.md` hits T1's "always" line in § Checks;
`grep -n 'lintDevDebug' .claude/commands/check.md` hits the `pr` section's "Always" bullet, in the
same `./gradlew` invocation as `ktlintCheck :app:assembleDevDebug`; `./gradlew ktlintCheck
:app:assembleDevDebug :app:lintDevDebug` passes on `develop` before this task merges, so the
first tail that carries it is green; `docs/ai/TESTING.md` § Lint (or the paragraph that names
`./gradlew lint`) says T1 runs the `devDebug` variant and why one variant is enough;
`python3 scripts/doctor.py` passes.
**Touches** `CLAUDE.md` § Checks (the T1 row and the "Always" sentence), `.claude/commands/check.md`,
`docs/ai/TESTING.md`.
**Read** `CLAUDE.md` § Checks · `.claude/commands/check.md` § `pr` · `docs/ai/TESTING.md` (grep
`lint`) · `docs/ai/plans/F3-green-again.md` § F3X2 (what lint caught).
**Steps** 1. Run the three-task invocation once on a warm daemon and note the time in the
`check.md` line (the backlog said ~1 minute; write what it was). 2. The two command edits, one
invocation not two. 3. The TESTING paragraph, three lines. 4. `/check pr` on this branch is the
first tail with lint in it.
**Checks** T1 — which now includes lint. **Depends** —

### F4X1 The description list wraps its value, not its label · 6

**Why** Seen on `devDebug` 1.0 (1), the dev menu's `Large_font` golden: at 1.5× `AppDescriptionList`
gives the label column whatever is left after the value takes its intrinsic width, and "Application
id" against `com.example.androidproject1.dev` is one character wide, a letter per line. Expected
the value to wrap first. Steps: open the debug menu with the font scale at 1.5. The cause is
`AppDescriptionList.kt:38` — `Modifier.weight(1f)` is on the label and the value has no
constraint at all.
**Done when** `AppDescriptionList` measures the label at its own width up to a share of the row
(`Modifier.weight(1f, fill = false)` with `Modifier.fillMaxWidth(LABEL_MAX_FRACTION)` or an
equivalent that reads in the file's own comment) and the value takes the rest, end-aligned and
wrapping — `grep -n 'weight' core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppDescriptionList.kt`
shows the value carrying a weight and the label bounded; a new
`core/ui/src/test/kotlin/com/example/androidproject1/core/ui/component/AppDescriptionListTest.kt`
extends `ComponentTest` and asserts, with a long value in a narrow `Box`, that the label node's
width is at least the width of the same label composed alone (so it never collapsed) and that the
value node's height is more than one line (it wrapped) — one test that fails on the current
code before the fix, run once to see it red; `./gradlew :core:ui:test` passes;
`./gradlew recordRoborazziDebug` is run and every golden whose screen composes the list is
**opened** — `DevMenuScreen`, `TripDetailScreen`, the inventory and trip review steps, the gallery
entry — and the `Large_font` ones show "Application id" on one line; `./gradlew test
verifyRoborazziDebug` passes in one invocation; `docs/ai/reference/DESIGN-SYSTEM.md` § Components
Content row still names `AppDescriptionList`, and the component's own KDoc says which column
wraps; `python3 scripts/doctor.py` passes.
**Touches** `core/ui/**/component/AppDescriptionList.kt`, `core/ui/src/test/**/AppDescriptionListTest.kt`,
the goldens under `feature/{devmenu,trips,inventory,gallery}/presentation/src/test/screenshots/`.
**Read** `core/ui/**/component/AppDescriptionList.kt:24-52` · `core/ui/src/test/**/component/ComponentTest.kt`
(the base and its rule about reaching by tag) · `core/ui/src/test/**/component/AppSwitchTest.kt`
(the shape of one) · `feature/devmenu/presentation/src/test/screenshots/*DevMenuScreenKt.Preview.Large_font*_0.png`
(what wrong looks like) · `CLAUDE.md` § Testing a screen ("a golden nobody looked at").
**Steps** 1. The test first, red. 2. The two modifiers: label `weight(1f, fill = false)` capped
by `fillMaxWidth(fraction)` where the fraction is a named constant with a comment, value
`weight(1f)` with `textAlign = TextAlign.End`; `AppText` may need a `textAlign` parameter — if it
does not have one, add it there rather than wrapping the text in a `Box`. 3. Record, open, verify.
4. The KDoc line.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`, and the whole `./gradlew
test` (a `core/` path). **Depends** —

### F4U1 A language picker in Settings · 12 · decides D75

**Why** Two locales ship — `values` and `values-cs` — and only the system setting chooses between
them; a tester checking Czech copy changes the whole device. Per-app language is a platform
feature since API 33 and a compat one below it, and the app has neither. Drawn first:
<https://claude.ai/artifact/3zJBaghVcsxwSnitHbyCDu>.
**Decide first** `AppCompatDelegate.setApplicationLocales through an AppCompatActivity, the
repository in :feature:settings:domain and its implementation in :app` (recommended), or
`LocaleManager on API 33+ only, the row hidden below` → D75. The recommended reading follows D68
exactly: `LanguageRepository` is an interface in `:feature:settings:domain` beside
`ThemeRepository`; `AppCompatLanguageRepository` lives in `:app` (`locale/`), because
`AppCompatDelegate` is an `androidx.appcompat` class and `:app` is the one module that already
depends on the activity stack — no data module gains AppCompat. `MainActivity` becomes an
`AppCompatActivity`, which is what applies a stored locale on API 29–32 in `attachBaseContext`
before the first frame; `Theme.AndroidProject1`'s parent becomes
`Theme.AppCompat.DayNight.NoActionBar`, and `AppLocalesMetadataHolderService` with
`autoStoreLocales = true` in the manifest is what persists the choice below 33, so nothing is
written to DataStore — AppCompat is the store, and the repository reads it back with
`getApplicationLocales()`. The alternative hides the row on a `minSdk = 29` device, which is the
emulator F3P7 boots. AppCompat is androidx: no `DECISIONS.md` row for the dependency itself, a
row in `DEPENDENCIES.md` § Ships in the app.
**Done when** `python3 scripts/create_screen.py settings SettingsLanguage --sub language --graph
settings` has been run and the six files, the two tests and the `AppNavHost` registration are
what it wrote plus edits, never hand-written; `AppLanguage` is an enum in
`feature/settings/domain/**/AppLanguage.kt` — `System`, `English`, `Czech` — each with a `tag`
(`""`, `"en"`, `"cs"`) and a `DEFAULT`; `LanguageRepository` in the same module has
`observeLanguage(): Flow<Outcome<AppLanguage>>` and `suspend fun setLanguage(AppLanguage):
Outcome<Unit>`, with a `FakeLanguageRepository` in its `testFixtures`; `grep -rn
'AppCompatActivity' app/src/main` hits `MainActivity.kt`; `grep -n 'Theme.AppCompat.DayNight.NoActionBar'
app/src/main/res/values/themes.xml` hits; `grep -n 'AppLocalesMetadataHolderService'
app/src/main/AndroidManifest.xml` hits, with `autoStoreLocales` `true`; `app/src/main/res/xml/locales_config.xml`
lists `en` and `cs` and the manifest's `<application>` names it in `android:localeConfig`;
`SettingsLanguageScreen` composes an `AppRadioGroup` tagged `settingsLanguage_optionsGroup` with
one `AppRadio` per `AppLanguage.entries` tagged `settingsLanguage_${language.tag.ifEmpty { "system" }}Item`,
an `AppTopBar` with `navigateUpTestTag = "settingsLanguage_upButton"`, and no other widget;
`SettingsScreen` gains an `AppButton` tagged `settings_languageButton` between the theme control
and the profile button, and `SettingsScreenTest` asserts the tap emits `SettingsEvent.LanguageClicked`;
`SettingsLanguageViewModelTest` says a `LanguageSelected` calls `setLanguage` and the state follows
the flow, `SettingsLanguageScreenTest` says a tap on `settingsLanguage_csItem` emits
`LanguageSelected(Czech)`; every `settings_language_*` string is in `values` and `values-cs`;
`./gradlew :feature:settings:presentation:test :feature:settings:domain:test :app:test` passes;
`recordRoborazziDebug` is run, the new screen's goldens and Settings' are **opened** in both
themes; `/run devDebug` shows the picker and choosing Čeština re-renders Settings in Czech without
a restart — one screenshot in the pull request; D75 is a row; `docs/ai/reference/FEATURES.md`
§ Screens gains `SettingsLanguage`, § Features' `settings` row names the language;
`docs/ai/reference/CORE.md` § `:app` gains the `locale/AppCompatLanguageRepository` row and
`MainActivity`'s row says it is an `AppCompatActivity` and why; `docs/ai/DEPENDENCIES.md` § Ships
in the app gains AppCompat; `python3 scripts/doctor.py` passes.
**Touches** `feature/settings/domain/**` (enum, interface, fixture), `feature/settings/presentation/**`
(the generated screen, `SettingsScreen.kt`, `SettingsEvent.kt`, `SettingsNavigation.kt`,
`SettingsViewModel.kt`, `SettingsDestination.kt`, `res/values*/strings.xml`, tests), `feature/settings/di/**`
(the generator's `viewModelOf`), `app/**/locale/AppCompatLanguageRepository.kt`,
`app/**/ApplicationModule.kt`, `app/**/MainActivity.kt`, `app/src/main/AndroidManifest.xml`,
`app/src/main/res/values/themes.xml`, `app/src/main/res/xml/locales_config.xml`,
`build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt`, `gradle/libs.versions.toml`,
`app/**/AppNavHost.kt` (the generator's line), `docs/DECISIONS.md`, `docs/ai/reference/{FEATURES,CORE}.md`,
`docs/ai/DEPENDENCIES.md`.
**Read** the canvas above · `feature/settings/presentation/**/settings/SettingsScreen.kt:55-76`
(the theme control — the sibling this row sits under) · `feature/settings/presentation/**/permissions/SettingsPermissionsScreen.kt`
(the closest existing screen: a non-root Settings screen with an up arrow) ·
`core/ui/**/component/AppRadio.kt:31-99` (`AppRadio`, `AppRadioGroup`) ·
`feature/settings/domain/**/{ThemePreference,ThemeRepository}.kt` (the shape to mirror) ·
`app/**/network/AndroidConnectivityMonitor.kt` and `app/**/ApplicationModule.kt` (D68's shape for
an `:app` implementation) · `app/**/MainActivity.kt:36-60` · `app/src/main/res/values/themes.xml` ·
`build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:91-100` · `CLAUDE.md`
§ Known constraints (`rememberNavBackStack` on the first frame — the activity change must not move
it) · `docs/ai/RECIPES.md` § Translations (Czech's four plural forms, if a quantity string appears).
**Steps** 1. D75. 2. `androidx-appcompat` in the catalog and one `add("implementation", …)` in the
application plugin; `MainActivity : AppCompatActivity`, the theme parent, the manifest service and
`locales_config.xml`; build and run once — the app must look exactly as before. 3. The domain
enum, interface and fake. 4. The `:app` repository: `observeLanguage()` is a `MutableStateFlow`
seeded from `AppCompatDelegate.getApplicationLocales()` and re-read after every `set`; on API 33+
the platform re-creates nothing and on 29–32 AppCompat re-creates the activity, both of which are
the framework's business. 5. The generator, then the screen from the canvas. 6. The Settings
row, event, navigation, destination lambda. 7. Strings in both locales. 8. Goldens, opened.
9. `/run devDebug`, the screenshot. 10. The three reference rows.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`, and the whole `./gradlew
test` (a `build-logic/` path). **Depends** —

### F4S1 A shared-element transition from a product row to its detail · 12 · decides D76

**Why** A showcase, not a rule: the catalog's row-to-detail push is the one place in the sample
where the same fact — the product's name and its price — is on both screens, and today it slides
away and slides back in. Compose's shared elements are the platform's answer and the template
shows nothing of them.
**Decide first** `a :core:ui modifier over two composition locals, the scope provided once in
AppNavHost` (recommended), or `the catalog feature opts into the animation API itself` → D76.
The recommended reading keeps the rule "a feature composes components; it never draws": `:core:ui`
gains `Modifier.appSharedElement(key: String)` (in `core/ui/**/layout/SharedElement.kt`, beside
`PaneMetadata.kt`) that reads `LocalSharedTransitionScope` — a `:core:ui` local — and
navigation3's `LocalNavAnimatedContentScope`, applies `sharedBounds` when both are present and is
a no-op when either is missing, which is what a preview, a Robolectric test and the gallery see.
`AppNavHost` wraps `NavDisplay` in `SharedTransitionLayout` and provides the local. The motion is a
token: `AppMotion.screenMillis` already exists and the bounds transform uses it. The alternative
puts `@OptIn(ExperimentalSharedTransitionApi::class)` and `androidx.compose.animation` scopes into a
feature screen, which is the widget set the rules keep out of features.
**Done when** `grep -rn 'appSharedElement' feature/catalog/presentation/src/main` hits the
product name in `ProductsScreen.kt` (the row's `headline` — `AppListItem` gets a
`headlineModifier` or the whole row carries the key; pick the one that animates the text, not the
row) and the title in `ProductDetailScreen.kt`, plus the price in both, with keys built from the
product id — `"product/${id}/name"`, `"product/${id}/price"`; `grep -rn 'SharedTransitionLayout'
app/src/main` hits `AppNavHost.kt` once; `grep -rn 'ExperimentalSharedTransitionApi' feature/`
is empty — the opt-in lives in `:core:ui` and `:app` only; a `core/ui/src/test/**/layout/SharedElementTest.kt`
composes a tagged `AppText` with `Modifier.appSharedElement("k")` outside any scope and asserts
it is displayed (the no-op path is the one every test and preview takes); on a wide window the
list–detail scene shows both panes and the modifier is still a no-op there (both entries are in
one scene, nothing animates) — `/run devDebug` on the phone emulator shows the name and price
travel from the row to the detail and back, and a screen recording or two screenshots go in the
pull request; `recordRoborazziDebug` is run and the catalog goldens are **opened** — they must be
pixel-identical to before, which is the proof the no-op path draws nothing;
`docs/ai/reference/DESIGN-SYSTEM.md` § Roles' `AppTheme.motion` row names the shared-element
transform and its token; `docs/ai/reference/FEATURES.md` § Flows' catalog paragraph says the
row-to-detail push is a shared-element showcase; D76 is a row; `python3 scripts/doctor.py`
passes (a public composable takes a `Modifier`; a modifier extension is not a composable).
**Touches** `core/ui/**/layout/SharedElement.kt`, `core/ui/src/test/**/layout/SharedElementTest.kt`,
`core/ui/**/component/AppListItem.kt` (if the headline needs a modifier of its own),
`app/**/AppNavHost.kt`, `feature/catalog/presentation/**/products/ProductsScreen.kt`,
`feature/catalog/presentation/**/productdetail/ProductDetailScreen.kt`, `docs/DECISIONS.md`,
`docs/ai/reference/{DESIGN-SYSTEM,FEATURES}.md`.
**Read** `app/**/AppNavHost.kt:161-240` (`AppNavDisplay`, the transition specs, the list–detail
strategy) · `core/ui/**/layout/PaneMetadata.kt` (the one `layout/` file, the shape to sit beside) ·
`core/ui/**/theme/Motion.kt` · `feature/catalog/presentation/**/products/ProductsScreen.kt:36-51` ·
`feature/catalog/presentation/**/productdetail/ProductDetailScreen.kt:32-74` ·
`core/ui/**/component/AppListItem.kt:41-60` · navigation3's `LocalNavAnimatedContentScope`
(`androidx.navigation3.ui`) and Compose's `SharedTransitionScope.sharedBounds` — check whether
`ExperimentalSharedTransitionApi` is still required under BOM `2026.08.00` before writing the
opt-in.
**Steps** 1. D76. 2. The local and the modifier, with the no-op branch first. 3. `AppNavHost`.
4. The two screens, four call sites. 5. The test. 6. `/run devDebug`, both directions, the
tablet size once (`adb shell wm size 1280x800` on the emulator, then reset). 7. Goldens, opened,
unchanged. 8. The two reference sentences.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`, and the whole `./gradlew
test` (a `core/` path). **Depends** —

### F4U2 A component playground behind the dev menu (was C1U6) · 25 · decides D77

**Why** The gallery shows every component in the states its author listed and nothing else; a
question like "what does this button look like disabled at `lg` with a long label" is answered by
editing Kotlin. A playground answers it with a control: pick a component, drive its properties,
watch it change. Drawn first: <https://claude.ai/artifact/V4CBL2VxCLW95ZCbJ46vHW>.
**Decide first** `a component's knobs are data — a sealed Knob type, a KnobValues map, a render
lambda — and the screen generates its controls from the knobs' types` (recommended), or `a
composable per component with its own controls` → D77. The recommended reading is the backlog
line's own sentence: `PlaygroundEntry(id, name, knobs: List<Knob>, render: @Composable
(KnobValues) -> Unit)` in `feature/devmenu/presentation/**/PlaygroundCatalog.kt`, beside
`DevMenuJump.kt`, the way `GalleryCatalog.kt` sits in the gallery feature. `Knob` is sealed —
`Toggle(key, label, default)`, `Choice(key, label, options, default)`, `Text(key, label,
default)`, `Number(key, label, range, default)` — and the screen holds a `when (knob)` over the
**four knob types**, never over a component; adding a component is one `entry(...)` and no screen
change. The catalog does not reuse `GalleryCatalog` — that is another feature's `presentation`,
which this one may not name (`doctor.py`); the eight entries here are the controls the canvas
shows and the ones with the most states: `AppButton`, `AppSwitch`, `AppTextField`, `AppTag`,
`AppStepper`, `AppSegmented`, `AppBadge`, `AppProgress`. The alternative is the `when` per
component the backlog line forbids.
**Done when** `python3 scripts/create_screen.py devmenu DevMenuPlayground --sub playground` has
been run and the six files, the two tests and the `AppNavHost` line are its output plus edits;
`grep -c 'when (' feature/devmenu/presentation/src/main/kotlin/com/example/androidproject1/feature/devmenu/presentation/playground/DevMenuPlaygroundScreen.kt`
is at most 1 and that `when` is over `Knob`; `grep -rn 'is Knob\.' feature/devmenu/presentation/src/main`
hits the screen (or its `component/`) and nothing else — no entry inspects a knob's type;
`DevMenuPlaygroundState` holds `entryId: String` and `values: Map<String, KnobValue>` and is
`@Immutable` with a `PREVIEW`; the screen composes an `AppSelect` tagged
`devMenuPlayground_componentField`, an `AppCard` tagged `devMenuPlayground_stageCard` whose content
is `entry.render(values)`, an `AppSectionHeader`, then per knob an `AppTextField` tagged
`devMenuPlayground_${key}Field`, an `AppSegmented` tagged `devMenuPlayground_${key}Tab` (an
`AppSelect` with the same tag when there are more than four options), an `AppSwitch` tagged
`devMenuPlayground_${key}Switch`, or an `AppStepper` tagged `devMenuPlayground_${key}Field`, and
an `AppTopBar` with `navigateUpTestTag = "devMenuPlayground_upButton"`; the four knob controls are
one feature-local component each or one `PlaygroundKnob` component in `presentation/component/`,
written with `python3 scripts/create_component.py PlaygroundKnob --feature devmenu`;
`DevMenuScreen` gains an `AppButton` tagged `devMenu_playgroundButton` under Tools, and
`DevMenuScreenTest` asserts its tap emits `DevMenuEvent.PlaygroundClicked`;
`DevMenuPlaygroundViewModelTest` says `ComponentPicked(id)` resets `values` to that entry's
defaults and `KnobChanged(key, value)` writes one key; `DevMenuPlaygroundScreenTest` renders the
`AppButton` entry, toggles `devMenuPlayground_enabledSwitch` and asserts `KnobChanged("enabled",
KnobValue.Bool(false))` — by tag, never by text; every `dev_menu_playground_*` string is in
`values` and `values-cs`; `./gradlew :feature:devmenu:presentation:test` passes;
`recordRoborazziDebug` is run and the new goldens **opened** in both themes and at the large font;
`/run devDebug` reaches the playground from the dev menu and a knob changes the stage — one
screenshot in the pull request; D77 is a row; `docs/ai/reference/FEATURES.md` § Screens gains
`DevMenuPlayground` and § Flows' debug-menu paragraph names it; `docs/ai/reference/DESIGN-SYSTEM.md`
§ The gallery says what the playground is and is not; `python3 scripts/doctor.py` passes.
**Touches** `feature/devmenu/presentation/**/PlaygroundCatalog.kt`, `feature/devmenu/presentation/**/playground/**`
(generated), `feature/devmenu/presentation/**/component/PlaygroundKnob.kt` (generated),
`feature/devmenu/presentation/**/devmenu/{DevMenuScreen,DevMenuEvent,DevMenuNavigation,DevMenuViewModel,DevMenuDestination}.kt`,
`feature/devmenu/presentation/src/main/res/values*/strings.xml`, the two `devmenu` tests,
`feature/devmenu/di/**` (the generator's `viewModelOf`), `app/**/AppNavHost.kt` (the generator's
line), `docs/DECISIONS.md`, `docs/ai/reference/{FEATURES,DESIGN-SYSTEM}.md`.
**Read** the canvas above · `feature/gallery/presentation/**/GalleryCatalog.kt:108-150,955-999`
(`GalleryEntry`, `entry(...)`, the `Demo` holder — the closest existing shape, in the feature it
may not import) · `feature/gallery/presentation/**/gallerydetail/GalleryDetailScreen.kt` (the
closest existing screen: one component on a stage) · `feature/devmenu/presentation/**/devmenu/DevMenuScreen.kt:100-140`
(the Tools section the button joins) · `feature/devmenu/presentation/**/DevMenuJump.kt` (a
feature-level data class beside the screens) · `core/ui/**/component/{AppSelect,AppSegmented,AppSwitch,AppStepper,AppTextField,AppCard}.kt`
signatures · `CLAUDE.md` § Screen structure (a screen file holds the screen and its previews,
so the knob controls go to `component/`).
**Steps** 1. D77. 2. `Knob`, `KnobValue`, `KnobValues`, `PlaygroundEntry` and the eight entries in
`PlaygroundCatalog.kt`. 3. The generator, then the screen from the canvas; `initialState` is the
first entry with its defaults. 4. `create_component.py PlaygroundKnob --feature devmenu`, the
`when (knob)`. 5. The dev-menu button, event, navigation, destination push. 6. Strings, both
locales. 7. The three tests. 8. Goldens, opened. 9. `/run devDebug`, the screenshot. 10. The
reference rows.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`. **Depends** —

### F4U3 Feedback roles in the palette · 6 · decides D78

**Why** The palette has action roles — `confirm`, `destructive`, `info`, `warning`, `neutral` —
and the three status aliases point at three of them, so a success message is drawn in the colour
of the Pay button and an error in the colour of Delete. A message and a button are not the same
thing: a feedback surface is a soft container with strong text and an accent, and it never has an
edge, because it cannot be pressed.
**Decide first** `a FeedbackColors triple — container, onContainer, accent — in four roles, with
the status aliases moved onto it` (recommended), or `four more ActionColors` → D78. The
recommended reading: `AppColors` gains `success`, `warning`, `error` and `info` as
`FeedbackColors(container, onContainer, accent)` under one `feedback` field — the existing
`warning` and `info` action roles stay, so `AppTheme.colors.warning.bg` is still a button and
`AppTheme.colors.feedback.warning.container` is a message; `statusPositive`, `statusWarning` and
`statusNegative` become aliases of `feedback.success`, `feedback.warning`, `feedback.error` and
keep their type by exposing `container`/`onContainer` (or the three `TagTone` consumers read
`feedback` directly and the aliases are deleted — the task picks one and the row says why). Light
is `100 / 700 / 500` on each ramp, dark is `800 / 200 / 400`, the same steps the action roles'
soft pair already use, so nothing is re-picked by hand. The alternative gives a message an `edge`
it must never draw.
**Done when** `grep -n 'FeedbackColors\|val feedback' core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Color.kt`
hits the class and the field in `AppColors`, `lightAppColors()` and `darkAppColors()`; `grep -rn
'statusPositive\|statusWarning\|statusNegative' core/ui/src/main` either hits only `Color.kt`
(aliases onto `feedback`) or is empty (deleted, consumers read `feedback`); `AppBanner` takes a
`tone: BannerTone` — `Info`, `Success`, `Warning`, `Error`, default `Warning` so the offline
banner is unchanged — drawn from `feedback`; `AppToast`'s `ToastTone` gains `Info` and `Warning`
and all four read `feedback`; `ContrastTest` gains a case that every `feedback.*.onContainer` reads
at 4.5:1 on its `container` in both themes and every `accent` at 3:1 on `surfaceBase` and
`surfaceRaised` — run once before the values are final to see it fail on any pair that does not;
`./gradlew :core:ui:test` passes; the gallery's `banner` entry shows four variants and `toast`
five, so the four roles are looked at in both themes through the components that carry them —
`recordRoborazziDebug` is run and the `AppBanner`, `AppToast`, `AppTag`, `AppStatusDot` and
`AppAvatar` goldens plus the gallery detail's are **opened**; `verifyRoborazziDebug` passes in
the same invocation as `test`; `docs/ai/reference/DESIGN-SYSTEM.md` § Roles' `AppTheme.colors` row
says what a feedback role is and that a status alias is one; D78 is a row; `python3
scripts/doctor.py` passes.
**Touches** `core/ui/**/theme/Color.kt`, `core/ui/**/component/{AppBanner,AppToast}.kt` (the
tones), `core/ui/**/component/{AppTag,AppStatusDot,AppAvatar}.kt` (only if the aliases go),
`core/ui/src/test/**/theme/ContrastTest.kt`, `feature/gallery/presentation/**/GalleryCatalog.kt`
(the `banner` and `toast` entries), goldens, `docs/DECISIONS.md`, `docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `core/ui/**/theme/Color.kt:10-160` (`ActionColors`, `AppColors`, the two factories, the
aliases at 94-98) · `core/ui/**/component/AppBanner.kt` and `AppToast.kt:20-46` (what borrows
today) · `core/ui/**/component/AppTag.kt:15-36` (`TagTone`, the alias consumer) ·
`core/ui/src/test/**/theme/ContrastTest.kt` (the four cases, the shape of a fifth) ·
`feature/gallery/presentation/**/GalleryCatalog.kt:675-683,959-965` (the two entries) ·
`docs/ai/reference/DESIGN-SYSTEM.md:17-25`.
**Steps** 1. D78. 2. The class, the field, the twelve values. 3. The contrast case, red if any
pair is short, then the values. 4. The two tones. 5. The aliases, one way or the other. 6. The
gallery variants. 7. Goldens, opened. 8. The reference row.
**Checks** T1 + `verifyRoborazziDebug` in the same invocation as `test`, and the whole `./gradlew
test` (a `core/` path). **Depends** —

## Retrospective

- **What the briefs got wrong.** One pair outside 0.7–1.3: F4U2 at `25 → 6`. The playground was
  estimated as a feature slice and built in forty minutes, because the brief had already decided
  the shape — the knob model, the one `when`, the eight entries — and the generator wrote the eight
  files; a 25 is three screens sharing a data layer, and this was one screen and one data class.
  The other five landed on their number, and the rewritten 6 band held on both of its tasks. Two
  briefs asked for something the code could not give and the task said so: F4S1's navigation3
  local throws outside an entry, so `:core:ui` owns a second nullable local the host re-provides;
  F4U1's `LocaleManager` alternative was the right thing to reject and the row says why. One
  brief was wrong in a way the task fixed in passing: F4P1's test suite asserted a real backlog
  line the draft had just taken — the same class of miss F2P1's line count was.
- **What the checks missed.** Nothing green went wrong on the device — every screen was looked
  at on the emulator and the two mid-flight frames are in the session. `ContrastTest` caught the
  feedback accents at 2.99:1 and 2.38:1 before they shipped, which is what it is for. What no
  check catches: a legacy-graphics Robolectric test measures a pixel per glyph, so
  `AppDescriptionListTest` had to opt into `NATIVE` to see the bug at all — a component test that
  asserts a width without it passes on anything. And the hook audit and doctor's note compare
  `core.hooksPath` to the literal `.githooks`, so on this worktree both report the hook unset
  while it runs on every commit (the § DevOps line from F4U1).
- **One thing to change.** A screenshot from the device belongs in the pull request, not in the
  session: `gh` cannot attach an image, so the three device proofs this sprint are files the
  owner saw once and a sentence in each body. A § DevOps line for the next improvement sprint:
  a place a device screenshot can be pushed to and linked from a pull-request body. The audit:
  the hook runs and reports as unset (the literal comparison — the existing line); every merged
  pull request carries a tail that says `pass` and names doctor's 42 and the Gradle tasks; every
  changelog block has its tag; the board was republished at `03f4636`, after the last merge.
  DevOps holds 15 points; an improvement sprint is due at 50.
