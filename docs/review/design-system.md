# Design system (:core:ui) and the component gallery (:feature:gallery)
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

## Summary
The three-layer theme is the right shape and it is genuinely enforced: Ramp and Scale are internal, no feature contains a .dp literal, a material3 import or a raw colour, doctor.py greps for all three, and ContrastTest turns the palette into a pure-function test. The problem is volume and duplication, not structure. 47 components for 18 screens, 27 of them used by nothing but the gallery, with several pairs doing the same job (Avatar/AvatarPhoto, FormField/FieldGroup, Spinner/IndeterminateProgress, Toast/Snackbar, Dialog/AlertDialog, BottomNav+NavRail/NavigationSuiteScaffold); a hand-written 896-line catalog that restates every @ComponentPreview by hand with no check keeping them in sync; and 349 goldens (11 MB) of which the Narrow-phone and Tablet screen variants rarely say anything the Phone one does not. The design system also still speaks its source product's language (till, POS, void, cash, mouse-only sizes), which a generic template should not. Plan 5 should cut, merge and rename rather than add: delete about a dozen components, make the gallery read the previews instead of its own registry, trim screen previews to the three variants that catch regressions, and fill the five real gaps (banner, chips, pull-to-refresh, password toggle, swipe-to-dismiss).

## Winners
### Three-layer theme with internal core layer, enforced by the compiler and doctor.py

Ramp (theme/Ramp.kt:17) and Scale (theme/Spacing.kt:14) are internal, AppTheme exposes only semantic roles (theme/Theme.kt:81-118), zero Ramp references leak into component/, zero .dp/material3/MaterialTheme in any feature presentation module, and doctor.py greps for all of them (scripts/doctor.py:620-660). This is what makes a re-brand one file, and it actually holds in the code, not just the docs.
Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Ramp.kt:17`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Spacing.kt:14`; `scripts/doctor.py:620`; `scripts/doctor.py:630`
### ContrastTest

189 lines, 4 tests, no device: every text role against every surface at 4.5:1, every border at 3:1, both palettes, with an ACCEPTED list that carries a floor and is itself checked for staleness. This is the check a re-brand needs and the one nobody does by eye. Keep exactly as is.
Evidence: `core/ui/src/test/kotlin/com/example/androidproject1/core/ui/theme/ContrastTest.kt:1-60`
### Formats via LocalFormats plus the doctor NumberFormat check

Five call sites in cart and catalog, one locale, and a grep that fails on java.text.NumberFormat in a feature (scripts/doctor.py:630). Small, standard, and it prevents the two-copies-of-Price.kt drift the comment describes. Not ceremony.
Evidence: `scripts/doctor.py:630`; `feature/cart/presentation/src/main/kotlin/com/example/androidproject1/feature/cart/presentation/cart/CartScreen.kt`
### AppScaffold as the single screen shell

screenId publishes testTags as resource ids, sends the analytics screen view, and applies insets once (component/AppScaffold.kt:37-66). 16 uses, the most of any component. One place, one argument, three concerns handled. Model for the rest.
Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppScaffold.kt:37`
### Preview-driven screenshot tests with no registry

PreviewScreenshotTest scans @ScreenPreview/@ComponentPreview via ComposablePreviewScanner (core/ui/src/test/.../screenshot/PreviewScreenshotTest.kt:88), so adding a component adds its goldens with no list to edit. The mechanism is right; only the variant count needs trimming (see problems).
Evidence: `core/ui/src/test/kotlin/com/example/androidproject1/core/ui/screenshot/PreviewScreenshotTest.kt:52-89`

## Problems (ranked by the reviewer)
### P1 · 27 of 47 components are used by nothing but the gallery, and several are duplicates of each other or of framework chrome

**high · over-engineered · effort M · confidence 0.85**

Classification of the 47 (signatures read from component/*.kt). (a) Standard, well made, keep even if unused today (31): AppScaffold, AppTopBar, AppButton, AppIconButton, AppText, AppTextField, AppSearchField, AppListItem, AppDivider/AppVerticalDivider/AppLabelledDivider, AppEmptyState, AppSectionHeader, AppCard, AppSwitch, AppCheckbox, AppRadio/AppRadioGroup, AppSegmented, AppTabs, AppSheet (wraps ModalBottomSheet), AppMenu (wraps DropdownMenu), AppSelect, AppImage (the one Coil seam), AppAvatarPhoto, AppStepper, AppProgress/AppStepProgress, AppTag, AppBadge, AppSkeleton, AppPager, AppDescriptionList, AppBottomActionBar, AppFab, AppDateField/AppTimeField, AppDialog (content dialog), AppSlider, AppFieldGroup. (b) Duplicates of something the app already renders another way, delete (5): AppToast (Screen() shows Material Snackbar, Screen.kt:50, and nothing can host a toast composable; see problem 7 for the rename alternative), AppConfirmDialog (Screen()'s alerts are StateAlertDialog over Material AlertDialog, AlertDialog.kt:23, so confirm-then-act already has a home in BaseViewModel), AppBottomNav and AppNavRail (the tabs are NavigationSuiteScaffold, AppNavHost.kt:113; a second bar the app never shows is a second look to maintain), AppSpinner (AppIndeterminateProgress does the same with a label). (c) Speculative or one-of-a-pair, delete or merge (8): AppToolbar (a contextual selection bar; no screen selects anything), AppTooltip (no modifier, rarely used on touch), AppStatusDot (AppTag with tone covers it), AppAvatar (fold into AppAvatarPhoto, which already falls back to initials), AppFormField (fold into AppFieldGroup; today a field's label/helper/error can come from AppTextField, AppFormField or AppFieldGroup, three anatomies), AppScrollShadow, AppAccordion, AppRangeSlider. Net: about 13 fewer files, 39 fewer goldens (3 each), 13 fewer catalog entries, and one look per job. The 28 components that import no material3 at all are a deliberate Foundation-based brand and that is fine to keep, but it means every state (focus, disabled, ripple, RTL, TalkBack) is yours; every component you do not need is maintenance you do not get back.

**Proposal.** One Plan 5 item: delete the 5 duplicates, merge the 8 pairs, remove their catalog entries and goldens in the same commit. Write down in CLAUDE.md the rule that already exists for dependencies: a component that no sample feature uses has to earn its place too.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppToast.kt:30`; `service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AlertDialog.kt:23`; `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:113`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppAvatarPhoto.kt:12-23`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppFormField.kt:10-20`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppToolbar.kt:12-18`
### P2 · GalleryCatalog is a hand-maintained 896-line registry that restates every @ComponentPreview, with no check keeping the two in sync

**high · repetitive · effort M · confidence 0.8**

48 entry(...) blocks, each listing the same states the component's own @ComponentPreview already renders (47 previews in core/ui, 141 goldens). The catalog header says a component 'is added here in the same change', but doctor.py has no check for it (grep GalleryCatalog scripts/doctor.py: nothing), which is the exact drift the rest of the repo is designed to prevent ('the previews are the list'). The gallery feature is 2 screens, 4 tests, 10 goldens (916 KB), a di module and this file, all for a debug-only browser (D16). Industry practice is one source: Airbnb's Showkase generates the on-device browser from @Preview via KSP, and Now in Android ships no gallery at all and relies on previews plus screenshots.

**Proposal.** Make the previews the catalog. Turn each component's private Preview() composable into an internal/public XPreview(), and have galleryCatalog be one line per component: entry("button", "AppButton", "Action") { AppButtonPreview() }. The catalog drops to roughly 120 lines, the states live once, and a golden and a gallery page cannot disagree. Then add the one doctor line: every @ComponentPreview in :core:ui has a catalog entry (or, if the gallery earns no use in the next project, delete the feature and let src/test/screenshots be the browsable catalog).

Evidence: `feature/gallery/presentation/src/main/kotlin/com/example/androidproject1/feature/gallery/presentation/GalleryCatalog.kt:119-140`; `feature/gallery/presentation/src/main/kotlin/com/example/androidproject1/feature/gallery/presentation/GalleryCatalog.kt:878-896`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppScaffold.kt:70-76`; `scripts/doctor.py:730`
### P3 · Component API inconsistencies: a second size enum, three composables with no modifier, a colour escape hatch, and a 14-parameter text field

**medium · inconsistent · effort M · confidence 0.8**

Most of the set follows the Compose guidelines (required params, then modifier, then optionals, content slot last) and 44 of 47 take a modifier. The exceptions: (1) ButtonSize (AppButton.kt:49) is a separate enum with its own fixed dp table (40/52/64) while ControlSize.Medium follows density (ControlSize.kt:24-27); the ControlSize doc even says it carries 'the same rule ButtonSize.Small carries'. Two scales for one idea. (2) AppBadge(count), AppTooltip(text) and AppLabelledDivider(label) take no modifier, so they cannot be testTagged, padded or placed, which breaks the repo's own 'find by id' rule. (3) AppText(color: Color? = null) is the one place a feature can pass any colour, bypassing the role system the rest of the layer enforces. (4) AppTextField has 14 parameters including three overlapping booleans/enums for the keyboard (numeric, keyboardType, password) plus frame; the sibling controls are uneven on size (Button, IconButton, Checkbox, DateField, Segmented, Switch, TextField have it; Select, Stepper, SearchField, Radio, Tag do not). The 65 .dp literals inside component/ (progress track 6.dp, checkbox box 18/22.dp, top bar 64.dp) are component geometry and are fine at layer 3; do not chase them.

**Proposal.** Fold ButtonSize into ControlSize (height from density, shape from AppTheme.shapes). Add modifier to the three that lack it. Replace AppText's color with a small tone enum (or drop it; TextRole already carries colour). Collapse AppTextField's numeric/password/keyboardType into one input: FieldInput enum (Text, Email, Number, Phone, Password) and give Password a visibility toggle, which is also the missing standard field. Give size to the remaining controls only if a screen asks for it.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppButton.kt:49-53`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/ControlSize.kt:12`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppBadge.kt:18`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTooltip.kt:20`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppDivider.kt:63`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppText.kt:64`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt:56-70`
### P4 · Five screen-preview variants per screen: Narrow phone and Tablet rarely catch anything the Phone variant does not, and 11 MB of PNG grows with every deliberate change

**medium · inefficient · effort S · confidence 0.75**

349 goldens: 141 in core/ui (3.4 MB), 208 across features (catalog 49 at 1.4 MB, profile 32 at 1.2 MB, devmenu 15 at 1.1 MB, template 33). Per screen the five are Phone 412 dp, Narrow 360 dp, Tablet 800 dp, Dark, Large font 1.5x. Dark catches colour-role mistakes and Large font catches overflow; those two plus Phone are the regression set. Narrow (360 vs 412) differs only when text wraps, which Large font already forces harder, and Tablet matters only for the list-detail pairs that use listPane/detailPane (layout/PaneMetadata.kt), not for a login form. Every intentional UI change rewrites dozens of full-resolution PNGs into git history; at the current rate the repo will carry more screenshot history than Kotlin within a couple of plans. The component set of three (Light, Dark, Large font) is proportionate.

**Proposal.** Trim @ScreenPreview to Phone, Dark, Large font, and keep a separate @AdaptivePreview (Tablet) only on the screens that declare a pane. Record with RoborazziOptions.RecordOptions(resizeScale = 0.5), which quarters the bytes and still shows a padding regression. Expect roughly 349 to 260 goldens and 11 MB to about 2 MB. If the repo is to live for years, put src/test/screenshots on git LFS now rather than later.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/common/Previews.kt:14-18`; `core/ui/src/test/kotlin/com/example/androidproject1/core/ui/screenshot/PreviewScreenshotTest.kt:52-73`; `feature/catalog/presentation/src/test/screenshots`; `feature/devmenu/presentation/src/test/screenshots`
### P5 · The design system still speaks its source product's language (till, POS, void, cash, mouse-only sizes) inside a generic template

**medium · inconsistent · effort S · confidence 0.7**

52 occurrences of till/POS/void/cash/terrace/'Order N' across core/ui and the gallery demos; 12 mentions of 'till' in core/ui alone. ControlSize.Small is documented as 'mouse only' and AppDensity scans InputDevice for a pointer at runtime (Density.kt:81-87) so 'a till still gets its 56'. AppAvatar's status dot is 'whether that person is on shift'. The gallery's AppButton demo is Pay / Void / Card / Cash. This is the KSD point-of-sale design imported wholesale. For a template whose next consumer is unknown, that vocabulary misleads the person reading a component's doc about when to use it, and the pointer-driven density (a wall-mounted till with a mouse) is a requirement almost no phone app has; it is a 92-line file plus a five-test AppDensityTest that exists for a device class the sample app never targets.

**Proposal.** One editing pass: neutral demo strings in the catalog and previews (Save / Delete / Continue), neutral KDoc on ControlSize, Density, AppAvatar, AppText. Decide on pointer detection: keep it only if a till or desktop-class device is a real target for the next project; otherwise reduce AppDensity to size-class-only (Compact/Regular) and delete pointerPresent() and its tests. Either way the design system stops describing a product it is no longer attached to.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/ControlSize.kt:12-27`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Density.kt:15-48`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Density.kt:81-92`; `feature/gallery/presentation/src/main/kotlin/com/example/androidproject1/feature/gallery/presentation/GalleryCatalog.kt:140-175`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppAvatar.kt:14-24`
### P6 · Two visual languages for chrome: hand-drawn App* dialog/toast/nav next to the Material dialog, snackbar and NavigationSuite the framework actually renders

**low · inconsistent · effort S · confidence 0.7**

Because :service:core:ui cannot depend on :core:ui, Screen() renders alerts with Material AlertDialog and messages with Material Snackbar, and :app renders the tabs with NavigationSuiteScaffold. Those are the dialog, snackbar and bottom bar a user actually sees. Meanwhile :core:ui ships a hand-drawn AppDialog (ui.window.Dialog plus foundation), AppToast, AppBottomNav and AppNavRail that no screen composes. The user sees Material chrome over Foundation-drawn content. This is workable today only because AppTheme populates Material's colorScheme/typography/shapes (Theme.kt:53-55), so the Material chrome is tinted correctly; but the dialog's shape, button style and elevation are Material's, not the key-surface brand. keySurface itself is not the issue: it is 20 lines of clip/background/padding used by AppButton and AppFab only (Elevation.kt:56-77), touches no Material API, and cannot fight an M3 update. Leave it.

**Proposal.** Pick the owner of chrome and delete the other. The cheap direction: accept Material chrome for alert, snackbar and tabs (it is themed already), delete AppConfirmDialog, AppToast, AppBottomNav, AppNavRail, and keep AppDialog only as the content dialog. The other direction, letting :core:ui inject its dialog/snackbar into Screen() through a CompositionLocal of slots, is a Plan 5 item only if the brand demands it; it is not needed for a template.

Evidence: `service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/Screen.kt:50-67`; `service/core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AlertDialog.kt:16-23`; `app/src/main/kotlin/com/example/androidproject1/AppNavHost.kt:113`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppDialog.kt:29`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/theme/Theme.kt:53-55`
### P7 · Standard product-app components missing while niche ones ship

**medium · missing-standard · effort M · confidence 0.75**

grep across core/ui and every feature: PullToRefresh 0, FilterChip/AssistChip 0, SwipeToDismiss 0, stickyHeader 0, inline banner 0, OTP 0; AppTextField masks a password (AppTextField.kt:106) but has no show/hide toggle. Yet AppScrollShadow, AppAccordion, AppTooltip and AppRangeSlider are here. Ten LazyColumn call sites across features and not one can refresh or swipe. Note that AppToast(message, tone, actionLabel, onAction) is not a toast at all: it is an inline banner with a tone and an action, which is exactly the missing component under the wrong name.

**Proposal.** Rename AppToast to AppBanner and keep it. Add, each as a thin wrapper over the Material or Foundation primitive with theme roles: AppChip/AppChipGroup (FilterChip), AppPullToRefresh (PullToRefreshBox around a LazyColumn), AppSwipeToDismiss (SwipeToDismissBox with a tone for the reveal), a password visibility toggle inside AppTextField, and a sticky AppSectionHeader overload for LazyListScope. Wire pull-to-refresh into ProductsScreen and swipe-to-dismiss into CartScreen so the template demonstrates them; OTP and a time picker can wait for a screen that needs them.

Evidence: `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppToast.kt:30`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppTextField.kt:106-113`; `core/ui/src/main/kotlin/com/example/androidproject1/core/ui/component/AppScrollShadow.kt:46`

## Looks heavy but is justified
- keySurface (theme/Elevation.kt:56-77): 20 lines of foundation modifiers, used by AppButton and AppFab, no Material dependency, so nothing for an M3 update to break; it is the one visible brand signature and it is cheap.
- ContrastTest, and the ACCEPTED-exception list with a floor: pure function, zero infrastructure, catches the one failure a re-brand reliably produces.
- The Foundation-drawn controls (Switch, Checkbox, Radio, Tabs, Segmented) despite not wrapping M3: they set Role and toggleable/selectable semantics and the minTouchTarget, the nine component tests cover the interactive ones, and rewriting 28 files to wrap Material would be exactly the large-scale change the owner ruled out.
- The 65 .dp literals inside component/: they are component geometry (track height, checkbox box, bar height), which is what layer 3 exists to own; the rule that matters is the one enforced on features, and that one holds at zero.
- AppImage as the only Coil import (doctor.py:625 IMAGE_LIBRARY): swapping the image loader is one file.
- Three component-preview variants (Light, Dark, Large font) and their goldens: each answers a distinct question and the count per component is small.
- Formats/LocalFormats with the doctor check: five call sites is not many, but the check is what stops the sixth from formatting money its own way.
- AppIcons with exactly three sizes: it replaced five ad-hoc dp values and a feature has something to ask for; 34 lines.

## Questions only the owner can answer
- Is the Foundation-drawn look (key-surface buttons, hand-drawn switches and dialogs) the brand every project from this template should inherit, or should the template's default be thin wrappers over Material 3 that a project re-skins? The answer decides whether the 28 hand-drawn components are an asset or a maintenance liability.
- Is a point-of-sale or desktop-class device (till, mouse) a real target for the next project? If not, pointer-driven density and the mouse-only Small size can go.
- Does anyone (designer, QA) open the on-device gallery, or is opening src/test/screenshots enough? That decides between making the catalog read the previews and deleting the feature.
- Is git LFS acceptable for this repo's screenshot goldens, or should golden size be controlled by resolution and variant count alone?
