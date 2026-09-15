# Design system

What `:core:ui` offers a screen. The rules for using it — compose, never draw — are
[../../../CLAUDE.md](../../../CLAUDE.md) § Design system.

## Three layers

**Core** is the raw ramps and the 4 dp scale, in `theme/Ramp.kt` and `theme/Spacing.kt`. It is
`internal`, so no screen can name a step.

**Semantic** is roles, in `theme/{Color,Type,Shape,Elevation,Motion,Density,Icon,Spacing}.kt`. This
is the only layer that differs between light and dark, which is what makes a re-brand one file
rather than a sweep.

**Component** binds a role to an element and its states, in `component/`.

## Roles

Read the semantic layer through `AppTheme`.

| Accessor | Holds |
|---|---|
| `AppTheme.colors` | surface, content, accent and status roles — `statusPositive`, `statusWarning`, `statusNegative`, aliases of `confirm`, `warning` and `destructive` so a status row and a button cannot drift apart. `TagTone` names the same five states a tag, a status dot or an avatar can be in: `Neutral`, `Positive`, `Warning`, `Negative`, `Info` — states, never transactions (E3U1). Also populates Material's own scheme, so `:service:core:ui` picks the theme up without depending on `:core:ui` |
| `AppTheme.typography` | text roles including `Numeric`, which is tabular and the one figures use |
| `AppTheme.shapes` | corner roles |
| `AppTheme.elevation` | elevation roles. A pressable surface uses `Modifier.keySurface(…)`, a hard bottom edge that shortens on press, not `Modifier.shadow` |
| `AppTheme.motion` | durations and easings |
| `AppTheme.density` | `minTouchTarget`, and the compactness the layout adapts to |
| `AppTheme.spacing` | spacing roles, the only source of a gap |
| `AppTheme.icons` | `sm` 18 in a row, `md` 24 for a control, `lg` 32 where the icon is the thing being looked at. Three sizes, and only three |

A touch target is `AppTheme.density.minTouchTarget` and a different question from an icon size.

## Components

Forty-seven in the gallery, in seven groups, plus `AppScaffold` (the screen shell, which every gallery
page already is) and `ControlSize` (the shared sm/md/lg scale, which shows up as the size variants
of the controls that read it).

| Group | Components |
|---|---|
| Content | `AppText` `AppCard` `AppListItem` `AppDescriptionList` `AppAccordion` `AppSectionHeader` `AppDivider` `AppImage` `AppEmptyState` `AppScrollShadow` |
| Action | `AppButton` `AppIconButton` `AppFab` |
| Form | `AppTextField` `AppSearchField` `AppSelect` `AppCheckbox` `AppRadio` `AppSwitch` `AppSegmented` `AppStepper` `AppSlider` `AppDateField` `AppFieldGroup` `AppFormField` |
| Navigation | `AppTopBar` `AppToolbar` `AppBottomNav` `AppNavRail` `AppTabs` `AppPager` `AppBottomActionBar` |
| Overlay | `AppDialog` `AppSheet` `AppMenu` `AppTooltip` `AppToast` |
| Shell | `AppScaffold` `AppScreenChrome` — the second is what `Screen()` draws around every feature (D50): the base surface, the loading overlay, the empty and error states, the alert and the snackbar host. A feature composes none of them; it puts a `ContentState` or a `LoadingState` in its `UiState` |
| Status | `AppBadge` `AppTag` `AppAvatar` `AppAvatarPhoto` `AppProgress` `AppSpinner` `AppSkeleton` `AppStatusDot` `AppBanner` |

`AppScaffold` is the screen shell: base surface, system insets, an optional `AppTopBar`, and the
screen id that becomes both the test id and the analytics screen view. A screen with a scaffold does
not apply insets itself; a screen without one does, because the activity is edge to edge.

**Every non-root screen passes `onNavigateUp`, and with it `navigateUpTestTag = "<stem>_upButton"`.**
A tab root passes neither — the bottom bar is what leaves it. The arrow needs a tag of its own
because the caller's `modifier` goes to the bar, and the label beside it is translated.
`AppSectionHeader`'s action is the same case, so it takes `actionTestTag = "<stem>_<name>Button"`
for the same reason, and a `MenuItem` carries `testTag = "<stem>_<name>Item"` because a menu row is
a value with no modifier and draws in a window of its own.

**A selection toolbar's actions wear the inverse kind.** `AppToolbar` is a dark wash, so what sits
on it asks for it: `AppIconButton(kind = IconButtonKind.Inverse)` and `AppCheckbox(inverse = true)`
— the tri-state master over the rows. `AppListItem` takes `leading` (an avatar, or the row's
checkbox in selection mode) and `onLongClick` beside `onClick`, which is how a list enters that
mode; `AppSheet`'s content scrolls, because a form is taller than the narrowest phone.

**A field's label is a property of the input, not a `Text` beside it.** `AppTextField` puts it on
the input's own semantics node and clears the visible label's, so a screen reader announces the name
once and a test finds it with `hasText` on the node that `hasSetTextAction`.

**The gallery lists every component, and something checks it.** `GalleryCatalog.kt` is written by
hand because an entry carries variants and live demos that a `@ComponentPreview` does not — but
`create_component.py` writes the starter entry and `doctor.py` fails on a component with none
(D52). Two files are deliberately not entries: `AppScaffold`, which every gallery page already is,
and `ControlSize`, which is a scale rather than a component.

**A field in a form says what comes next, and what it holds.** `AppTextField` takes `imeAction`
(`Next` on every field but the last, `Done` on the last), `onImeAction` for what `Done` runs, and
`contentType` — the one line that lets a password manager fill it. A form whose fields leave all
three at their defaults gives every field the same keyboard key and is never offered a saved
sign-in, which is the most common thing an Android form gets wrong (D51).

**A figure shrinks rather than wraps.** `TextRole.Numeric` auto-sizes down to 12 sp on one line:
"1 234,00 Kč" is four characters longer than "1,234.00", so the row that fits in English wrapped in
Czech, and a wrapped figure in a column of figures is worse than a slightly smaller one.

**One size scale, not one per component.** `ControlSize` is `Small` / `Medium` / `Large` and every
control that comes in sizes reads it, so `Small` means the same thing everywhere. A button's
metrics are taller than a field's; that is a lookup inside `AppButton`, not a second enum (D51).
`SizeClass` in `theme/` is a different idea — the width class of the window.

**A label a component supplies itself is a resource, never a literal.** The picker's Cancel and
Choose and the confirm dialog's default Cancel live in `:core:ui`'s `strings.xml` under `app_`,
in both locales, beside the back arrow and the stepper's keys — a component's own control carries
the same name everywhere it appears, and `doctor.py`'s translation check does not read Kotlin.

Three of the components are composed by nothing outside `:core:ui` and the gallery — down from
sixteen before Inventory landed and twenty-seven before Trips. The scan behind that number is
literal, which is the only way it stays reproducible: for each `App*.kt`, whether any file outside
`core/ui/` and `feature/gallery/` names it. The three are the chrome's own, and each stays for one
reason: `AppScreenChrome`, which `AppTheme` installs behind every screen and no feature names on
purpose (D50); `AppDialog`, which the chrome draws for every alert; and `AppToast`, which it draws
for every snackbar. Nothing is homeless, so nothing was deleted — D38's promise came due in E3T1
and D65 records the outcome per component. Re-run the scan when a component is added; one that
is still homeless after the next showcase is deleted, not deprecated.

## Previews and goldens

Every component carries a `@ComponentPreview` and every screen a `@ScreenPreview`; `doctor.py` fails
a component without one. **The previews are the list** — nothing is registered anywhere, so adding a
component adds its goldens and deleting one leaves its images for `git status` to point at.

Goldens are recorded at half the device's pixel size, which still shows a moved element, a clipped
row or a wrong colour at a quarter of the bytes. They live in each module's `src/test/screenshots/`
and are committed.

An overlay needs a golden of its own. A dialog, a sheet, a menu and a picker each draw in a window
of their own, so capturing the preview captures the composable and not the window — which is how
every golden stayed green while the date picker was clipped on a narrow phone.
`OverlayScreenshotTest` in `:core:ui` opens the overlay and captures the screen instead; adding an
overlay means adding a case there.

## The gallery

`feature/gallery` renders every component with the states worth looking at, reached from the debug
menu and present in debug builds only. Its catalog lists each entry's id, name, group and variants
by hand, which is the one place in the design system with no check that it matches reality.

## Adding one

`python3 scripts/create_component.py PrimaryButton` puts it in `:core:ui` with a preview;
`--feature catalog` puts it in that feature's `component/` instead. A component a second feature
wants moves to `:core:ui` rather than being copied. Either way it is added to the gallery catalog
and to the group table above in the same change.
