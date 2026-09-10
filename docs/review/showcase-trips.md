# Showcase feature design · trips
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

**Angle.** Trip planner: a three-step wizard (basics, travellers and budget, destination via a picker) that lands on a review screen and then on a dashboard of trips with a detail drill-down. Chosen because a trip form naturally needs every input control in the set (dates, times, select, radio, checkbox, slider, stepper, segmented), a trips dashboard naturally needs the status controls (tabs with a badge, status dots, progress, skeletons, accordion, FAB, menu, sheet), and nothing in the nine sample features owns dates, schedules or a wizard today (catalog/cart are commerce, profile/settings are single forms, onboarding is a pager).

**Pitch.** The user plans a trip in three short steps, picks a destination from a searchable list that hands its choice back, reviews a summary and saves it, then watches the trip appear on a dashboard with a budget bar and a status dot, and can open, edit or delete it from an overflow menu. It is not boring because every step changes something visible on the next screen (the budget bar, the traveller avatars, the tab badge), the destination comes back through a real result channel, and the save ends with a snackbar whose action jumps straight to the detail.

**Layers.** domain,data,presentation,di

## Screens
### Trips

Route args: `none`

Dashboard and root of the feature. AppTabs Upcoming / Past (badge = upcoming count, TabItem.badge in core/ui/.../AppTabs.kt:24). Each row is a feature component TripRow: AppListItem with an AppAvatar per traveller (initials, id-hashed colour), an AppStatusDot (Planned / Booked / Done via TagTone), an AppTag for transport, and an AppProgress showing budget used. AppSkeleton rows while the repository's first emission is pending (observe(loading = {})). AppEmptyState on an empty tab. AppFab 'Plan a trip'. AppTopBar overflow AppIconButton opens AppMenu (Sort, Clear past trips). Sort opens an AppSheet with an AppRadioGroup (by date / by budget). 'Clear past trips' goes through uiState.setAlert and SystemEvent.AlertResult.Confirmed exactly as SettingsViewModel.kt:61-79 does. AppScrollShadow at the top edge of the list.

Components: AppScaffold, AppTopBar, AppTabs, AppBadge, AppListItem, AppAvatar, AppStatusDot, AppTag, AppProgress, AppSkeleton, AppEmptyState, AppFab, AppIconButton, AppMenu, AppSheet, AppRadio, AppScrollShadow, AppDivider, AppSectionHeader, AppText

Proves: observe(flow) over a repository, ContentState.Empty per tab, confirm-then-act alert through Screen(), a tab root with no Up arrow, a badge fed from a flow (the Cart tab pattern in TopLevelDestination.hasBadge)

### TripPlanner

Route args: `none`

The wizard, one screen with a step index in state and AppStepProgress(steps = 3) on top. Step 1 Basics: AppFormField + AppTextField (name, required, errorText on blank), AppSegmented One way / Return (size = ControlSize.Small), AppDateField depart, AppDateField return (enabled only for Return), AppTimeField departure time. Step 2 People and budget: AppFieldGroup with AppFieldGroupRow rows holding AppStepper travellers (1..8), AppSlider budget with valueLabel from LocalFormats.current.moneyShort, AppRadioGroup travel class (Economy / Comfort / First), AppCheckbox extras (insurance, lounge — CheckState), AppSwitch 'remind me a day before', AppSelect transport (Train / Plane / Car / Bus). Step 3 Destination: AppButton 'Pick a destination' which navigates to DestinationPicker with a result key; the chosen name renders in an AppCard with an AppTag. AppBottomActionBar holds Back / Next, Next disabled until the step validates. Up on a dirty draft shows AppConfirmDialog 'Discard draft?' rendered by the screen from a state flag — the one dialog that is a component rather than the central alert.

Components: AppScaffold, AppTopBar, AppProgress, AppFormField, AppTextField, AppSegmented, AppDateField, AppFieldGroup, AppStepper, AppSlider, AppRadio, AppCheckbox, AppSwitch, AppSelect, AppCard, AppTag, AppButton, AppBottomActionBar, AppDialog, AppText, ControlSize

Proves: Multi-step form state in one ViewModel with per-step validation, NavResultEffect<String> consuming a picker result once (CartDestination.kt:36 pattern), LocalFormats for money, a screen-owned dialog beside the central one, a guarded Up

### DestinationPicker

Route args: `resultKey:String`

A searchable list of ~20 fixed destinations (a domain fixture list, no network). AppSearchField filters as you type; AppListItem rows with an AppAvatar showing the country code and a supporting line; AppEmptyState when nothing matches; AppScrollShadow on the list. Tapping a row calls rememberNavResultSender(key.resultKey)(destinationId) then pops, exactly as ProductPickerDestination.kt:17-22 does. Up pops without a result.

Components: AppScaffold, AppTopBar, AppSearchField, AppListItem, AppAvatar, AppEmptyState, AppScrollShadow, AppDivider, AppText

Proves: create_screen.py --with-args, a route key as a ViewModel constructor parameter registered in KoinGraphTest.injectedParameters (app/.../KoinGraphTest.kt:53), the responder half of NavResultStore, Up on a non-root screen

### TripReview

Route args: `none`

Reads the draft from TripRepository.draft (a StateFlow, so no argument is needed) and lays it out as an AppCard with an AppDescriptionList (name, dates from LocalFormats.date/time, travellers as a plural, budget via money, class, transport) plus an AppAccordion 'Extras' that expands the checkbox choices, and a row of AppAvatar for travellers with an AppStatusDot 'Planned'. AppSectionHeader per group. AppBottomActionBar: Edit (pops back to the planner) and Confirm. Confirm runs execute { repository.save(draft) } with loadingMessage, then emits UiCommand.ShowSnackbar('Trip saved', action 'Open', id = SNACKBAR_OPEN) and TripReviewNavigation.Done (pop to the dashboard). SystemEvent.SnackbarAction(SNACKBAR_OPEN) in onSystemEvent emits TripReviewNavigation.OpenTrip(id); the destination turns it into backStack.add(TripDetailDestination(id)). Up pops to the planner.

Components: AppScaffold, AppTopBar, AppCard, AppDescriptionList, AppAccordion, AppAvatar, AppStatusDot, AppSectionHeader, AppTag, AppBottomActionBar, AppButton, AppText, AppDivider

Proves: execute {} with the central loading overlay, a snackbar whose action returns as SystemEvent.SnackbarAction and drives navigation, R.plurals via toPluralUiText for the traveller count (with all four Czech forms), a screen that renders only what a repository holds

### TripDetail

Route args: `tripId:String`

Opened from a dashboard row or from the review's snackbar. AppTopBar with Up and an overflow AppIconButton opening AppMenu (Mark as booked, Delete). Body: AppCard header with AppAvatar row, AppStatusDot and AppTag; AppProgress 'Budget used' with an AppSlider 'Spent so far' underneath that updates it live (the one playful control); AppDescriptionList of the facts; an AppAccordion per day of the itinerary generated from the date range, each holding two AppListItem placeholders; AppSegmented Overview / Itinerary switching the two sections. Delete uses uiState.setAlert with an AlertPayload carrying the trip id and acts on AlertResult.Confirmed, then emits Back. Mark as booked runs execute and flips the status dot. Unknown id renders ContentState.Error with a retry.

Components: AppScaffold, AppTopBar, AppIconButton, AppMenu, AppCard, AppAvatar, AppStatusDot, AppTag, AppProgress, AppSlider, AppDescriptionList, AppAccordion, AppListItem, AppSegmented, AppSectionHeader, AppText, AppDivider

Proves: A second argument route (so KoinGraphTest gains two lines), ContentState.Error with ErrorDisplay.Inline retry, AlertPayload-typed confirm-then-act (the pattern CLAUDE.md names but no sample carries a payload), a screen reachable from two callers

## Flow

Entry: a fifth tab 'Trips' in app/src/main/kotlin/com/example/androidproject1/TopLevelDestination.kt (one enum entry with hasBadge = true fed by TripRepository.upcomingCount, one tab_trips string in :app, one tripsEntries() block in AppNavHost.kt beside homeEntries/catalogEntries/settingsEntries at AppNavHost.kt:259-261, and a 'trips' name in NAV_GRAPHS in scripts/_common.py so create_screen.py --graph trips works). Trips (root, no Up) --FAB--> TripPlanner --'Pick a destination'--> DestinationPicker(resultKey = TRIP_DESTINATION_RESULT) --tap row: setNavResult + pop--> TripPlanner (NavResultEffect consumes it once) --Next on step 3--> TripReview --Confirm--> save, pop to Trips, snackbar 'Trip saved / Open' --SnackbarAction--> TripDetail(tripId). Trips row tap --> TripDetail(tripId). TripDetail menu Delete --> central alert --> Confirmed --> delete --> Back. Every non-root screen passes onNavigateUp on its AppTopBar, which emits NavigateUpClicked and the destination calls backStack.removeLastOrNull() as DevMenuScreen.kt:45 does. Commands, in order: create_feature.py trips --graph trips (after the NAV_GRAPHS edit; the generated root screen is renamed to Trips) then create_screen.py trips TripPlanner --sub planner --graph trips; create_screen.py trips DestinationPicker --sub picker --with-args 'resultKey:String' --graph trips; create_screen.py trips TripReview --sub review --graph trips; create_screen.py trips TripDetail --sub detail --with-args 'tripId:String' --graph trips; create_datasource.py trips InMemoryTrip --repository; create_component.py TripRow --feature trips --state.

## Data

Full stack, in memory only. :feature:trips:domain holds Trip (id, name, destinationId, departDate, returnDate?, departTime, travellers, budgetCents, spentCents, travelClass, transport, extras, remind, status) as plain Kotlin using java.time (the JVM module has it; AppDateField already takes LocalDate), TripDraft, Destination, and TripRepository with observeTrips(): Flow<List<Trip>>, draft: StateFlow<TripDraft>, updateDraft(), save(): Outcome<Trip>, get(id), setStatus(), setSpent(), delete(), clearPast(). :feature:trips:data holds DefaultTripRepository over an InMemoryTripDataSource (a MutableStateFlow seeded with three sample trips and a fixed list of ~20 destinations as a Kotlin object). No DataStore, no Room, no network: the point of the feature is the screens, and a DataStore would add a Context, Robolectric on the repository test and a schema to maintain for a demo. The draft lives in the repository rather than in a SavedStateHandle so TripPlanner and TripReview are two ViewModels over one source, which is what a real form-then-review flow does. The first TripRepository emission is delayed ~600 ms in the data source so the dashboard's AppSkeleton state is actually visible. Previews and screen tests use TripsState.PREVIEW fixtures; the ViewModel tests use a FakeTripRepository in :feature:trips:domain's testFixtures (the FakeAuthService precedent), not a second copy in each test.

## Cost

Goldens: 5 screens x 5 = 25 plus TripRow's 3 component goldens = 28 (roughly 0.9 MB in git at the current ~30 KB each). Strings: about 70 names per locale (Trips ~14, TripPlanner ~28 incl. 6 select/radio option labels, DestinationPicker ~4, TripReview ~14, TripDetail ~12) in values and values-cs, plus one R.plurals (trip_review_travellers) with two English and four Czech forms, plus tab_trips in :app; one Czech translation pass. Tests: 10 screen-unit tests (5 ViewModelTest + 5 ScreenTest), 1 PreviewScreenshotTest (cloned), 1 DefaultTripRepositoryTest (plain JVM), 2 new injectedParameters lines in KoinGraphTest written by create_screen.py, and one FakeTripRepository test fixture. Doctor checks that apply: six-file screen directory and nothing else (so every dialog/sheet body must go to presentation/component/, which adds up to 3 more components and 9 more goldens if they grow), screen file holds only the screen and its previews, XState.PREVIEW on every state and on TripRowState, resource names prefixed per screen (trips_, trip_planner_, destination_picker_, trip_review_, trip_detail_), every string in values-cs with all four plural forms, screenId on every AppScaffold, NAV_GRAPHS entry, KoinGraphTest injectedParameters for both argument routes, no NumberFormat and no material3 import in the feature. Roughly 2,000 lines of Kotlin (sample features run 300-500 lines per screen including tests) and one Maestro flow if the owner wants the wizard driven end to end.

## Components covered

AppScaffold, AppTopBar, AppButton, AppText, AppListItem, AppDivider, AppEmptyState, AppIconButton, AppSectionHeader, AppTextField, AppCard, AppBottomActionBar, AppDescriptionList, AppSearchField, AppSegmented, AppStepper, AppSwitch, AppTag, AppAccordion, AppAvatar, AppBadge, AppCheckbox, AppDateField, AppDialog, AppFab, AppFieldGroup, AppFormField, AppMenu, AppProgress, AppRadio, AppScrollShadow, AppSelect, AppSheet, AppSkeleton, AppSlider, AppStatusDot, AppTabs, ControlSize

## Still unused after this feature

AppAvatarPhoto (used by profile already, not here), AppPager (used by onboarding already, not here), AppBottomNav, AppNavRail, AppImage, AppSpinner, AppToast, AppToolbar, AppTooltip

## Risks and decisions

- Tab or debug menu: a fifth tab is what makes the feature discoverable and gives the dashboard the root-without-Up shape the design assumes, but it also makes a demo flow a permanent part of the sample app's shell (5 tabs is the Material maximum for a bar) and needs a NAV_GRAPHS edit in scripts/_common.py before create_feature.py can target it. The alternative is an AppButton on Home ('Plan a trip') wired as a lambda in homeEntries, which keeps the shell as is but then Trips is a pushed screen and should carry Up. Owner decides; the design works either way, only the Trips top bar changes.
- Presentation-only versus full stack: presentation-only cannot carry a draft from TripPlanner to TripReview or a saved trip to the dashboard without a shared holder the repo does not have, so the wizard would collapse to one screen. The in-memory full stack costs three thin modules (the generator writes them) and one repository test; recommend full stack, no persistence.
- Two dialog mechanisms on one screen: confirm-then-act is the central alert (Screen.kt:66-76 renders a Material dialog from uiState.alert), so AppDialog/AppConfirmDialog in core/ui can only appear if a screen renders it itself from a state flag (the 'Discard draft?' guard on TripPlanner). That is legitimate for a dialog with custom content but the owner should decide whether the template wants to show both or whether Screen()'s StateAlertDialog should itself be re-pointed at AppConfirmDialog (it cannot today: :service:core:ui must not depend on :core:ui). If not, drop AppDialog from the plan and it stays at 0 usage.
- AppSheet and AppMenu are stateful pickers that a screen test must open before asserting; both hold an expanded flag, and a screen file may hold only the screen, so the sort sheet body and the overflow menu items go to presentation/component/ as feature components with their own previews (3 goldens each).
- The nine components that remain unused after this feature are unused for a reason worth acting on in Plan 5: AppBottomNav and AppNavRail duplicate NavigationSuiteScaffold in :app, AppToast duplicates Screen()'s SnackbarHost, AppTooltip and AppToolbar have no consumer in any product screen, AppImage needs a Coil model and a network or a bundled drawable, and AppSpinner is covered by the central loading overlay. Consider deleting AppBottomNav, AppNavRail and AppToast rather than inventing a screen for them.
- AppDateField uses java.time (LocalDate/LocalTime) and the domain module is Kotlin/JVM, so the Trip model can use them directly; but the picker inside AppDateField is a Material DatePickerDialog and the screenshot test's single manual frame will capture the closed field only, which is fine but means the open picker is never a golden.
- Roborazzi will add 28 PNGs (~0.9 MB) to a repository already holding 11 MB of goldens; if Plan 5 decides to prune goldens (for example to record only the phone light variant per screen) this feature should be generated after that decision, not before.
- The feature is a second thing named 'planner/trips' next to the catalog's commerce story; keep its copy generic (no prices from the catalog) so it reads as an independent slice a real project would keep or delete whole with delete_feature.py.
