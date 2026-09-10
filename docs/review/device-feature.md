# Device-capabilities feature design (and that agent's own retrospective notes)
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

RETROSPECTIVE — AndroidProject1 after four plans
(read-only review; paths relative to repo root)

CLEAR WINNERS — keep as they are
- Convention plugins + version catalog: a module build file is a plugins block and project deps (build-logic/src/main/kotlin/AndroidConventions.kt; e.g. feature/auth/presentation/build.gradle.kts). This is exactly what Now in Android does; nothing to change.
- BaseViewModel.execute/observe + Screen() as the single interpreter of UiState/UiCommand (service/core/ui/.../viewmodel/BaseViewModel.kt, service/core/ui/.../component/Screen.kt:50-133). Loading, alert, snackbar and toast are rendered once, centrally. The SnackbarHost is real: Screen.kt:85 hosts it, :105-117 shows a ShowSnackbar and posts SystemEvent.SnackbarAction(id) back to the ViewModel.
- The generators for the registration problem (create_feature/create_screen) and the registration half of doctor.py (settings, Koin, AppNavHost, KoinGraphTest). This is the one thing a copy-paste template gets wrong every time, and it is solved.
- Directory per screen (D34), find-by-testTag, screenId on AppScaffold doubling as the analytics screen view, permissions in service/core/ui/permission/ with a four-state status (PermissionStatus.kt:10-34), tag-based versioning (D31), Czech translation + four plural forms check (a genuine lint blind spot).
- Roborazzi "the previews are the list" with manualAdvance — the mechanism is right; the volume is the problem (see 3).

PROBLEMS, ranked by impact

1. :core:ui is a component library built ahead of demand — 27 of 47 components are composed by no feature and no :app code
   Evidence: zero non-test, non-gallery uses in feature/ and app/ for AppAccordion, AppAvatar, AppBadge, AppBottomNav, AppCheckbox, AppDateField, AppDialog, AppFab, AppFieldGroup, AppFormField, AppImage, AppMenu, AppNavRail, AppProgress, AppRadio, AppScrollShadow, AppSelect, AppSheet, AppSkeleton, AppSlider, AppSpinner, AppStatusDot, AppTabs, AppToast, AppToolbar, AppTooltip, ControlSize. Each carries 3 goldens (core/ui/src/test/screenshots: 141 files, 3.4 MB) and a gallery entry (48 in GalleryCatalog.kt), and doctor.py:719 enforces the preview. Two of them cannot be used at all: AppFab has no slot in AppScaffold (core/ui/.../AppScaffold.kt:38-44 has topBar only), and AppBottomNav/AppNavRail are dead because :app uses NavigationSuiteScaffold. There are two dialogs: Screen() renders service's StateAlertDialog (service/core/ui/.../component/AlertDialog.kt:16) while AppDialog is unused. AppDateField/AppTimeField ship English literals as defaults ("Choose a date", AppDateField.kt:62,124) — a localisation bug no check catches because :core:ui is exempt from the feature-string rules.
   The project's own rule ("an unused dependency is removed, not kept for symmetry") applies to components. Proposal: the showcase feature below gives ~14 of these a real user; delete or fold the rest (Accordion, BottomNav, NavRail, ScrollShadow, Skeleton, Tooltip, StatusDot, Toolbar, FieldGroup/FormField, Spinner/Progress, Avatar) unless a screen wants them. Add a `floatingAction` slot to AppScaffold. Localise the date/time placeholders (app_ resources) or make them required.

2. The Gradle grain is too fine: 56 modules for 18.5k lines (~330 lines per module)
   Evidence: the ten `di` modules are 13-59 lines of Kotlin each (feature/home/di 13, feature/gallery/di 15, feature/template/di 15, feature/onboarding/di 22…); `domain` modules are 19-89 lines (feature/onboarding/domain 19, feature/cart/domain 36). Every Android library module pays configuration, manifest merge, lint, R class, AAR packaging and a test task; 44 build.gradle.kts files. Google's guidance and NiA modularise by feature, with layers as packages inside; the layer boundary that matters (domain has no android.*) is already a JVM module in service/core/domain.
   Proposal for Plan 5: collapse `di` into the module that owns the bindings (Koin's own recommendation: the module beside the classes it binds) — 10 modules gone with no architectural loss — and let a feature be `:feature:x:domain` (only when it has one, JVM) + `:feature:x` (data + presentation as packages). 56 → roughly 30. Keep the doctor "repository imports the interface, never DefaultXDataSource" check; it works on packages as well as modules. Let the build-measuring agent's numbers decide between "drop di only" and "one module per feature".

3. Every screen is verified three ways, and the goldens are 5x what changes
   Evidence: per screen a ViewModelTest, a Robolectric ScreenTest and 5 goldens (Phone, Narrow, Tablet, Dark, Large font — core/ui/.../common/Previews.kt:30-35); per component 3. 349 goldens, ~11 MB in git, +5 per screen forever. feature/template alone commits 33 goldens for a feature that never ships (feature/template/presentation/src/test/screenshots). ScreenTests such as `renders the fields and the submit button` (feature/auth/.../LoginScreenTest.kt:55-61) assert what the golden already proves; test lines in feature/ are 5.7k against 8.0k main.
   Proposal: ScreenPreview = 2 variants (phone light, phone dark + fontScale 1.5 combined) and keep 3 for components: ~349 → ~160, and a padding change touches 2 files not 5. Exclude feature/template from verifyRoborazzi (its test still gets cloned). Keep ScreenTest only where it asserts a tap → event or an enabled/disabled derivation; drop "is displayed" tests. Consider a `--changed-modules` filter in CI for verifyRoborazzi later, not now.

4. doctor.py is a second compiler that also lints the docs — 30 checks, 1,256 lines, ~19 s in the pre-commit hook
   Evidence: checks that enforce shape and documentation rather than correctness: "every feature is listed in CLAUDE.md's module tree" (scripts/doctor.py:354), "a screen file holds no composable but the screen and its previews" (:880), "every public composable takes a Modifier parameter" (:543), "no foreign project identifiers" (:749), "every :core:ui component has a preview" (:719). Plus scripts/test_scripts.py at 922 lines / 56 tests to test the scripts. The commit hook runs all of it (install_hooks.py).
   Proposal: split into `doctor.py --fast` (registrations, portability, translations; target < 5 s; the hook) and the style checks in CI only; delete the CLAUDE.md-tree check and the module tree it polices (the tree is `ls feature/`), and the "screen file holds only the screen" check (D34's directory rule already gives a composable exactly one home). Compose's bundled lint covers Modifier naming/position; presence is a code-review matter.

5. CLAUDE.md is 983 lines and has started to contradict itself
   Evidence: CLAUDE.md:212 "A screen does not call safeDrawingPadding() itself" vs CLAUDE.md:391 "A screen without a Scaffold pads itself with .safeDrawingPadding()". The Scaffolding-scripts section restates every script's --help; the Recipes section restates .claude/commands/*.md (148 lines). A rulebook this long is skimmed, not followed, by both people and models.
   Proposal: CLAUDE.md ≈ 250 lines of rules + the API table + Known constraints; the narrative "why" moves to docs/ARCHITECTURE.md (the doc-writing preference in memory already says "what to do, not where it came from"). Scripts are documented once, in scripts/README.md and --help.

6. service/ portability machinery for a reuse that has not happened
   Evidence: export_service.py (479 lines) with --sync-versions resolving libs.* accessors out of convention plugins, the namespace-vs-package split (`...service.core.ui` namespace, `...core.ui` package, explained at CLAUDE.md Module structure), the "no foreign project identifiers" check, and the test_scripts coverage for all of it. No second project exists (D19 says template; memory notes the reference project was the source, not a consumer).
   Proposal: keep the service/ modules and the cheap rule (no :core/:feature/:app imports, doctor.py:126); delete export_service.py and its tests; document "copy service/ and build-logic/, add includeBuild + three includeServiceModule lines" in one paragraph. If the namespace/package split exists only for export, revisit it too.

7. The template shows APIs it never uses
   Evidence: UiCommand.ShowSnackbar / ShowToast have zero senders (grep over feature/ and app/ finds only NavigateBack and OpenAppSettings); NavResultStore has one caller (cart → product picker); AlertPayload confirm-then-act has one (SettingsViewModel.kt:72-83); PartiallyGranted has none. A template's sample is the documentation; an API with no sample is documented only in CLAUDE.md, which is the file that is too long. The feature below fixes this without adding machinery.

Fine as is, and worth saying: three flavors on one dimension, Maestro weekly (D30), no vendor SDKs (D8/D27), Koin, Coil, the KSD three-layer theme, Kover as signal not gate, ktlint config in .editorconfig.

------------------------------------------------------------
SIDE TASK — DEVICE-CAPABILITIES FEATURE DESIGN

Overview
One feature, `:feature:report` ("Field report"): the user files a short site report — title, category, when, where (location), photos (camera / photo picker), an attached document (SAF) — reviews it, exports it as a file, and can delete it with Undo. One feature rather than two because location, media and files are all inputs to the same object, which is what makes the navigation (editor → place picker → back with a result → detail) believable rather than a menu of demos. Full stack: domain (Report, GeoPoint, ReportRepository, LocationSource), data (in-memory DefaultReportRepository seeded with two reports; DefaultLocationDataSource; DefaultAttachmentDataSource), presentation, di — generated with `create_feature.py report --graph main`, then `create_screen.py` three more times.

Screen list (4 screens, each an 8-file unit)

1. ReportListScreen — tab root (`ReportListDestination` as a fifth TopLevelDestination "Reports"; alternative: a button on Home). Purpose: the list and the Undo pattern.
   Components: AppScaffold(screenId = "ReportListScreen", floatingAction = AppFab "New report" — the new slot), AppTopBar with actions = AppMenu(Sort by date/title, "Clear all" destructive), AppSegmented All/Draft/Sent, AppListItem rows with AppBadge (photo count) and AppStatusDot (draft/sent), AppEmptyState with action, AppSkeleton while seeding.
   Exercises: AlertPayload confirm-then-act for "Clear all" (SystemEvent.AlertResult.Confirmed) → UiCommand.ShowSnackbar(id = "undo_clear", action = Undo) → SystemEvent.SnackbarAction restores; NavResultEffect<String>("report_deleted") from the detail screen → the same snackbar with Undo (a snackbar cannot outlive the screen that popped, so the surviving screen shows it — worth a comment). ContentState.Empty. Tab root: no Up arrow.
   State: reports: List<ReportRow>, filter: ReportFilter, lastDeleted: Report? (for undo).
   Nav: FAB → ReportEditorDestination(reportId = null); row → ReportDetailDestination(reportId).

2. ReportEditorScreen — `--with-args 'reportId:String'` (empty string = new; the args template does not allow nullable). Purpose: the form and every picker.
   Components: AppTopBar(onNavigateUp, actions: Save), AppTextField title, AppSelect category, AppDateField + AppTimeField, AppCheckbox "Urgent", AppSlider severity, AppRadio visibility, feature components LocationSection, PhotoStrip, AttachmentRow (each via `create_component.py --feature report`), AppSheet "Add photo" (Camera / Library rows as AppListItem), AppToast inline "Saved as draft" (the inline banner component, distinct from the system toast), AppTooltip on the severity label.
   Exercises: PermissionGate(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION) with rationale → PermissionStatus.PartiallyGranted (coarse only: the first real user of that state, shows "approximate") → Denied(canAskAgain = false) → "Open settings" → UiCommand.OpenAppSettings; location services off → ContentState.Error inline with retry (ErrorDisplay.Inline); events LocationRequested / LocationArrived(GeoPoint) / PhotoCaptured(uri) / PhotosPicked(uris) / AttachmentPicked(uri) / PlaceChosen(id); NavResultEffect<String>("report_place") from the place picker; UiCommand.ShowToast on save; back with unsaved changes → AlertPayload discard-confirm → UiCommand.NavigateBack.
   State: draft: ReportDraft (title, categoryIndex, date: LocalDate?, time: LocalTime?, location: LocationState (Idle/Locating/Found(GeoPoint, approximate)/Unavailable), photos: List<String> (uri strings), attachment: AttachmentInfo?, urgent, severity), canSave (derived), dirty.
   Nav: "Choose place" → ReportPlacePickerDestination(resultKey = "report_place"); Save → pop.

3. ReportPlacePickerScreen — `--with-args 'resultKey:String'` (the ProductPicker pattern). Purpose: the responder half of NavResultStore and the "map placeholder".
   Components: AppTopBar(onNavigateUp), AppSearchField, AppListItem per named place (fixed fixture list, distance formatted with LocalFormats), "Use current location" row that reuses LocationSection, feature component MapPlaceholder (a tile that prints lat/lng and a compass — no Maps SDK; D27 and API-key-free). AppTabs Nearby / Saved.
   Exercises: rememberNavResultSender(resultKey), setNavResult(placeId) then pop; the value is a String because a Bundle must hold it and Parcelize would be a new plugin.
   State: query, places: List<Place>, selectedId.

4. ReportDetailScreen — `--with-args 'reportId:String'`. Purpose: the read view, export, delete.
   Components: AppTopBar(onNavigateUp, actions: AppMenu Edit / Export / "Open in maps" / Delete destructive), AppDescriptionList (category, when, where, severity), AppPager over the photos with AppImage, AppTag chips, AppCard for the attachment, AppDivider, AppAccordion "Raw data" (JSON of the report — gives Accordion a job).
   Exercises: CreateDocument("application/json") export with the write done in the data source; OpenBrowser("https://maps.google.com/?q=lat,lng") (no GMS needed); AlertPayload delete-confirm → repository delete → setNavResult("report_deleted") + UiCommand.NavigateBack; ContentState.Error when the id is unknown (a deep link to a deleted report).
   State: report: Report, photos, attachment, exporting flag.

Navigation graph
  Reports tab (ReportList) ── FAB ──> ReportEditor(reportId="") ── Choose place ──> ReportPlacePicker(resultKey) ──[result]──> back to editor
  ReportList ── row ──> ReportDetail(reportId) ── Edit ──> ReportEditor(reportId)
  ReportDetail ── Delete (confirm) ──[result "report_deleted"]──> back to ReportList → snackbar Undo
  All Up arrows pop; the tab root has none. Registered in AppNavHost as a `reportEntries(backStack)` block beside home/catalog/settings, one TopLevelDestination entry, one `tab_reports` string, `report` added to NAV_GRAPHS in scripts/_common.py.

Component / API coverage table
  Location        LocationManagerCompat.getCurrentLocation (androidx.core:core 1.19.0, already resolved; API level 30+ has the framework call too but Compat covers 29) — DefaultLocationDataSource wraps it in suspendCancellableCoroutine with a CancellationSignal; provider chosen by isProviderEnabled (GPS, else network); LocationManagerCompat.isLocationEnabled for the "off" state. Play-services-location (FusedLocationProvider) rejected: not in the cache, drags GMS, dead on AOSP/Huawei/GMS-less emulators; the LocationSource interface in domain is the seam a product swaps it at, and that sentence goes in the Decisions table (D37).
  Never-ask-again  PermissionStatus.Denied(canAskAgain=false) → button → UiCommand.OpenAppSettings (existing). Location services off is a different settings screen; proposal: generalise to UiCommand.OpenSystemSettings(action) with OpenAppSettings kept as the common case — owner decision.
  Camera          ActivityResultContracts.TakePicture via the existing CaptureToUri contract + cache-file FileProvider helper (feature/profile/.../CaptureButton.kt:56-70). CameraX is not in ~/.gradle/caches (no androidx.camera), so `camera-compose` 1.5.x with CameraXViewfinder would be 4 new artifacts; D27-clean (androidx) but it cannot run under Robolectric, needs a Preview lifecycle, and is more than a template should carry. Recommend: move CaptureToUri and the FileProvider helper into service/core/ui `media/` as `rememberTakePicture(onResult)` and make profile's CaptureButton use it — otherwise this feature writes the second copy CLAUDE.md forbids.
  Image picker    PickVisualMedia / PickMultipleVisualMedia(maxItems = 5) from androidx.activity 1.13.0 (declared, resolved), Photo Picker, no permission; profile already uses the single form (PictureButtons.kt:30). Same move: `rememberPickPhotos(max)` in service media/.
  File picker     OpenDocument(arrayOf("application/pdf", "image/*", "text/*")) + contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION) in DefaultAttachmentDataSource; name/size via OpenableColumns (framework, no androidx.documentfile). Export via CreateDocument("application/json"). Both D27-clean (framework + androidx.activity).
  Dialogs/pickers AppDialog/AppConfirmDialog via AlertPayload (three uses), AppSheet (add-photo chooser), AppMenu (two overflow menus), AppSelect, AppDateField, AppTimeField (exists, AppDateField.kt:118), AppToast (inline), UiCommand.ShowToast, UiCommand.ShowSnackbar with action → SystemEvent.SnackbarAction (two uses), AppFab (new AppScaffold slot), AppTabs, AppSegmented, AppCheckbox, AppRadio, AppSlider, AppPager, AppImage, AppAccordion, AppDescriptionList, AppSkeleton, AppTooltip, AppStatusDot, AppBadge, AppEmptyState, AppSearchField, AppCard, AppTag. Leaves unused after this feature: AppBottomNav, AppNavRail, AppScrollShadow, AppToolbar, AppFieldGroup/AppFormField, AppSpinner/AppProgress, AppAvatar — candidates for deletion (problem 1).
  Not covered on purpose: a map SDK, notifications (devmenu's NotificationTester has that), CameraX.

Dependency list
  androidx.core:core-ktx 1.19.0 — already in the presentation plugin's transitive graph; add explicitly to convention.feature.data if data needs LocationManagerCompat. D27-clean.
  androidx.activity:activity-compose 1.13.0 — declared, already used by profile. D27-clean.
  Nothing else. No play-services-location, no CameraX, no androidx.documentfile, no Maps. Robolectric's ShadowLocationManager (4.16, on the data test classpath through convention.feature.data) simulates getCurrentLocation, so the location data source gets a JVM test.
  Manifest: ACCESS_COARSE_LOCATION + ACCESS_FINE_LOCATION in feature/report/presentation/src/main/AndroidManifest.xml (the profile pattern for CAMERA); a `<queries>` entry for the camera intent is already needed by profile.

What it adds
  Files: 4 × 8 screen files, 4 feature components (LocationSection, PhotoStrip, AttachmentRow, MapPlaceholder), 3 data sources + 1 repository pair, 1 Koin module, 1 AppScaffold slot, 2 helpers moved into service media/.
  Strings: ~50 names in values + values-cs (report_list_*, report_editor_*, report_place_picker_*, report_detail_*, report_* shared); one `tab_reports` in :app; 2 plurals (photo count, attachment size units) with all four Czech forms.
  Goldens: 4 screens × 5 + 4 components × 3 = 32 (or 4 × 2 + 12 = 20 under problem 3's proposal), plus 3 for the AppScaffold-with-FAB preview.
  Tests: 4 ViewModelTests (undo path, discard-confirm, PartiallyGranted state, export), 4 ScreenTests (FAB → event, place row → event, menu delete → event, save disabled until title), DefaultReportRepositoryTest, DefaultLocationDataSourceTest (ShadowLocationManager: granted + provider off), DefaultAttachmentDataSourceTest (Robolectric ContentResolver). Roughly 35 @Test. KoinGraphTest injectedParameters gets three route keys (create_screen writes them).
  Maestro `file-a-report.yaml`: sign-in → tapOn "Reports" tab → assertVisible ReportListScreen → tapOn id reportList_newButton → assertVisible ReportEditorScreen → inputText into reportEditor_titleField → tapOn reportEditor_locationButton → tapOn text "While using the app" optional (system dialog) → tapOn reportEditor_saveButton → assertVisible reportList_reportItem → tapOn it → assertVisible ReportDetailScreen → tapOn reportDetail_menuButton → tapOn reportDetail_deleteItem → tapOn the confirm (StateAlertDialog needs a stable id — add `screen_alertConfirm` to service's dialog) → assertVisible ReportListScreen → tapOn text "Undo" (Material's snackbar action is not taggable; the one text-match, commented) → assertVisible reportList_reportItem.
  Needs a device or emulator, not Robolectric: the camera capture round trip, the Photo Picker and SAF UIs, the system permission dialog, a real fix. Everything else — ViewModels, both permission fallbacks (rendered with a fixed state), the location data source, export writing — runs under `./gradlew test`.

Cost
  About the size of catalog (three screens with args, 49 goldens) — one L item, or two M items split as (a) service media/ helpers + AppScaffold slot + profile refactor, (b) the feature. 2–3 focused days.

Risks
  - Two copies of the capture/pick helpers if (a) is skipped; doctor does not detect that.
  - Snackbar after pop: if the delete snackbar is emitted from the detail ViewModel it is lost with the screen; the design routes it through a nav result to the list — document it in the destination, it is the sort of thing the next feature copies wrong.
  - Robolectric renders AppSheet (ModalBottomSheet) and AppMenu poorly in goldens; keep them out of the screen previews (preview the closed state) or accept a component golden only.
  - A fifth tab changes the bar in every existing screen golden that shows it — none do (Screen() renders below the scaffold), but `AppNavHostAnalyticsTest` and the tab tests in :app will need the new entry.
  - Location on the CI emulator: no provider; the Maestro step must stay `optional`.

Owner decisions needed
  1. Entry point: fifth tab "Reports" (recommended: it exercises TopLevelDestination and the tab-segment back stack) or a lambda from Home.
  2. Location: framework LocationManagerCompat (recommended, D27-clean, GMS-free) vs play-services-location behind the same interface.
  3. Camera: keep TakePicture (recommended) or add CameraX + camera-compose for an in-app viewfinder.
  4. Move CaptureToUri / photo-pick helpers into service/core/ui media/ and refactor profile onto them (recommended) — or accept a copy.
  5. Persistence: in-memory seeded repository (recommended for a sample) vs Room (a second schema + migration test) vs DataStore JSON.
  6. Generalise UiCommand.OpenAppSettings to OpenSystemSettings(action) for the location-services-off case, or leave that state as text only.
  7. Whether this feature's landing is the trigger for deleting the components it still leaves unused (problem 1), and for cutting ScreenPreview to two variants (problem 3) before it adds 32 more goldens.
