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
| `AppTheme.colors` | surface, content, accent and status roles; also populates Material's own scheme, so `:service:core:ui` picks the theme up without depending on `:core:ui` |
| `AppTheme.typography` | text roles including `Numeric`, which is tabular and the one figures use |
| `AppTheme.shapes` | corner roles |
| `AppTheme.elevation` | elevation roles. A pressable surface uses `Modifier.keySurface(…)`, a hard bottom edge that shortens on press, not `Modifier.shadow` |
| `AppTheme.motion` | durations and easings |
| `AppTheme.density` | `minTouchTarget`, and the compactness the layout adapts to |
| `AppTheme.spacing` | spacing roles, the only source of a gap |
| `AppTheme.icons` | `sm` 18 in a row, `md` 24 for a control, `lg` 32 where the icon is the thing being looked at. Three sizes, and only three |

A touch target is `AppTheme.density.minTouchTarget` and a different question from an icon size.

## Components

Forty-four in the gallery, in six groups, plus `AppScaffold` (the screen shell, which every gallery
page already is) and `ControlSize` (the shared sm/md/lg scale, which shows up as the size variants
of the controls that read it).

| Group | Components |
|---|---|
| Content | `AppText` `AppCard` `AppListItem` `AppDescriptionList` `AppAccordion` `AppSectionHeader` `AppDivider` `AppImage` `AppEmptyState` `AppScrollShadow` |
| Action | `AppButton` `AppIconButton` `AppFab` |
| Form | `AppTextField` `AppSearchField` `AppSelect` `AppCheckbox` `AppRadio` `AppSwitch` `AppSegmented` `AppStepper` `AppSlider` `AppDateField` `AppFieldGroup` `AppFormField` |
| Navigation | `AppTopBar` `AppToolbar` `AppBottomNav` `AppNavRail` `AppTabs` `AppPager` `AppBottomActionBar` |
| Overlay | `AppDialog` `AppSheet` `AppMenu` `AppTooltip` `AppToast` |
| Status | `AppBadge` `AppTag` `AppAvatar` `AppProgress` `AppSpinner` `AppSkeleton` `AppStatusDot` |

`AppScaffold` is the screen shell: base surface, system insets, an optional `AppTopBar`, and the
screen id that becomes both the test id and the analytics screen view. A screen with a scaffold does
not apply insets itself; a screen without one does, because the activity is edge to edge.

**Every non-root screen passes `onNavigateUp`, and with it `navigateUpTestTag = "<stem>_upButton"`.**
A tab root passes neither — the bottom bar is what leaves it. The arrow needs a tag of its own
because the caller's `modifier` goes to the bar, and the label beside it is translated.

**A field's label is a property of the input, not a `Text` beside it.** `AppTextField` puts it on
the input's own semantics node and clears the visible label's, so a screen reader announces the name
once and a test finds it with `hasText` on the node that `hasSetTextAction`.

**A label a component supplies itself is a resource, never a literal.** The picker's Cancel and
Choose and the confirm dialog's default Cancel live in `:core:ui`'s `strings.xml` under `app_`,
in both locales, beside the back arrow and the stepper's keys — a component's own control carries
the same name everywhere it appears, and `doctor.py`'s translation check does not read Kotlin.

Twenty-seven of the components are currently composed only by the gallery. None is deleted until the
showcase features have had a chance to give them a home.

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
