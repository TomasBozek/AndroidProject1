# Sprint G1 · Movies, paged and offline

Sprint: G1 · Movies, paged and offline
Status: open
When: 2026-09-21 09:00 → 2026-09-21 18:00
Goal: A movies feature over TMDB — a list that pages as it scrolls, pulls to refresh, reads its cache offline, opens a detail, and can be pointed at the real host from the dev menu
Release: G
Agents: 1 · 79 points
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D79–D81

Every list in the sample loads whole from a fixture and refreshes because its screen opened. This
sprint adds one feature, `movies`, over the one public API the owner chose on 2026-09-19 — TMDB,
`https://api.themoviedb.org/3/`, which needs a free key — and gives it what the catalog does not
have: `GET /movie/popular?page=n` fetched page by page as the list reaches its end, a pull to
refresh that starts the list over, a Room cache the list draws from when the request fails, a
detail reached from a row, and a dev-menu switch that sends the fixture build's TMDB requests to
the real host when a key is present. The owner named the lines; § Next of the backlog is untouched
and its two lines stay first for the sprint after this one.

Three decisions are open and each has a task: how a page is cached over `cached()` (D79 — the
owner chose pages over `cached()` rather than Paging 3, so the row in `DECISIONS.md` says why),
how a feature talks to a host that is not `BASE_URL` (D80), and how the `dev` engine reaches a
real server without ceasing to be the fixture engine (D81). What it leaves alone: the catalog keeps
loading whole — the Someday line "Paging 3 · feature:catalog:data" stays in the backlog; the
`cached()` combinator is not changed, only called per page; `service/` is not touched except
where a generator writes a registration. The key never lands in git: `tmdb.apiKey` in
`local.properties`, empty by default, and an empty key is a build that only ever sees fixtures.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a task appended by the owner. Nothing else.

## Files

What each task writes. With one agent nothing is arbitrated; with two, a task owns its row.

| File group | Task |
|---|---|
| `build-logic/**/{ProjectConfig,AndroidApplicationConventionPlugin}.kt`, `app/src/dev/kotlin/**/network/NetworkEngine.kt`, `app/src/dev/res/raw/fixture_movie*.json`, `app/src/test/**/network/**` | G1P1 |
| `settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`, `app/**/ApplicationModule.kt` (the generator's registrations, then the TMDB config binding) | G1H1 |
| `feature/movies/{domain,data,di}/**`, `feature/movies/data/schemas/**` | G1H1 |
| `core/ui/**/component/AppPullToRefresh.kt`, `core/ui/src/test/**/AppPullToRefreshTest.kt`, `feature/gallery/presentation/**/GalleryCatalog.kt` | G1U1 |
| `feature/movies/presentation/**/movies/**`, `feature/movies/presentation/**/component/MovieRow.kt`, `feature/movies/presentation/src/main/res/**`, `feature/home/presentation/**`, `feature/devmenu/presentation/**/devmenu/DevMenuState.kt`, `app/**/AppNavHost.kt` (the Home card's lambda and the jump) | G1U2 |
| `feature/movies/presentation/**/moviedetail/**`, `feature/movies/presentation/**/component/MovieSharedElementKeys.kt`, `app/**/AppNavHost.kt` (the generator's registration) | G1U3 |
| `feature/devmenu/presentation/**/ApiSwitch.kt`, `feature/devmenu/presentation/**/devmenu/**`, `app/src/dev/kotlin/**/network/**`, `app/src/{staging,prod}/kotlin/**/network/NetworkEngine.kt`, `app/**/ApplicationModule.kt` (the switch binding) | G1P2 |
| `.maestro/browse-movies.yaml`, `.maestro/open-a-cached-movie.yaml` | G1H2 |
| `docs/ai/reference/{FEATURES,DESIGN-SYSTEM,CORE}.md`, `docs/ai/CODEBASE.md`, `docs/ai/DEPENDENCIES.md`, `docs/DECISIONS.md`, `docs/BACKLOG.md` | the task that changes the fact |

`app/**/AppNavHost.kt` is written by three tasks in turn — the generator's registrations in G1H1
and G1U3, the Home card's lambda and the jump in G1U2 — and `ApplicationModule.kt` by two, which
is fine with one agent and is why `Agents:` says 1. `build-logic/` and `core/` are touched, so T1
runs the whole `./gradlew test`; `feature/movies/presentation` and `core/ui` are, so
`verifyRoborazziDebug` rides in the same invocation.

## Tasks

### G1P1 TMDB fixtures on `dev`, and the key in `BuildConfig` · 6

**Why** `BASE_URL` is one host per flavor and every fixture answers under it; TMDB is a second
host, needs a key, and the `dev` engine has no route for either. Nothing in this sprint compiles
against a real request until the build knows the host and the fixture engine answers its paths.
**Decide first** `a third-party host is a config object of its own — TmdbConfig(baseUrl, imageBaseUrl, apiKey) built in ApplicationModule from ProjectConfig constants and BuildConfig.TMDB_API_KEY — and a request to it carries an absolute URL, which Ktor's defaultRequest leaves alone`, or
`TMDB becomes BASE_URL on every flavor and the catalog fixtures move under it` → D80. The first:
the catalog's host is the template's own API and stays; a sample that only ever talks to one
host is a sample of the wrong thing.
**Done when** `grep -n 'TMDB_API_KEY' build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt`
hits a `buildConfigField` read from `local.properties`' `tmdb.apiKey` with `""` when absent;
`grep -n 'tmdb.apiKey' README.md docs/ai/RECIPES.md` names the property once, in the setup table
beside `sdk.dir`; `grep -n 'TMDB' build-logic/src/main/kotlin/ProjectConfig.kt` hits `apiBaseUrl` and
`imageBaseUrl` constants; `ls app/src/dev/res/raw/ | grep -c fixture_movie` prints `4` —
`fixture_movies_page_1.json`, `_2`, `_3` (twenty rows each, TMDB's field names) and
`fixture_movie_details.json` (one object per id across the three pages: `runtime`, `genres`,
`tagline`); `./gradlew :app:testDevDebugUnitTest --tests '*NetworkEngine*'` passes a new test
that asks the dev engine for `https://api.themoviedb.org/3/movie/popular?page=2` and gets page 2,
for `/3/movie/<id>` and gets the row merged with its detail, for `?page=4` and gets a `422` the
way TMDB answers a page past the end, and for the whole lot with `FixtureNetwork.failing` set and
gets `503`; `./gradlew :app:assembleDevDebug` passes with no `local.properties` entry;
`docs/DECISIONS.md` has row D80; `docs/ai/CODEBASE.md` § Known constraints (or the flavor
paragraph) says the key is read from `local.properties` and never from git.
**Touches** `build-logic/src/main/kotlin/ProjectConfig.kt`,
`build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt`,
`app/src/dev/kotlin/com/example/androidproject1/network/NetworkEngine.kt`,
`app/src/dev/res/raw/fixture_movie*.json`, `app/src/test/kotlin/com/example/androidproject1/network/`,
`README.md` (the setup row), `docs/DECISIONS.md`, `docs/ai/CODEBASE.md`.
**Read** `app/src/dev/kotlin/.../network/NetworkEngine.kt:20-70` (the route table and the
failing switch), `build-logic/src/main/kotlin/AndroidApplicationConventionPlugin.kt:28-60` (the
flavor block and how `keystore.properties` is read, the same shape for `local.properties`),
`build-logic/src/main/kotlin/ProjectConfig.kt:40-60`, `app/src/test/.../network/CachedOkHttpEngineTest.kt`
(an engine test's shape), `CLAUDE.md` § Modules (the "never a version in a module build file"
rule; a key is the same kind of thing).
**Steps** 1. `ProjectConfig`: a `Tmdb` object with `apiBaseUrl = "https://api.themoviedb.org/3/"`
and `imageBaseUrl = "https://image.tmdb.org/t/p/w342"`; the convention plugin reads
`tmdb.apiKey` from `local.properties` (absent file, absent key → `""`) and writes
`buildConfigField("String", "TMDB_API_KEY", …)` on every flavor, plus `TMDB_API_BASE_URL` and
`TMDB_IMAGE_BASE_URL` from the constants. 2. Write the four fixtures by hand from TMDB's public
`popular` shape — `page`, `results[]` with `id`, `title`, `overview`, `poster_path`,
`release_date`, `vote_average`, `genre_ids`, then `total_pages: 3`, `total_results: 60`; real
titles, sixty distinct ids. 3. The dev engine matches on host before path: a request to
`api.themoviedb.org` routes `movie/popular` by `page` (a page over 3 → `422`, TMDB's
`success:false` body), `movie/<id>` to the row plus its detail; every other host keeps the table
it has. 4. The engine test in `app/src/test`, `NetworkEngineTest`, under the `dev` source set's
unit tests. 5. Row D80; the CODEBASE note.
**Checks** T1 + the whole `./gradlew test` (build-logic moved). **Depends** —.

### G1H1 The movies data layer: a page is a cache entry · 25

**Why** Nothing in the sample pages. `cached()` takes one local flow, one remote call and one
write; a list that grows page by page needs a way to call it per page and a table that remembers
which page a row came from, and a refresh that starts the list over without leaving stale pages
behind.
**Decide first** `a page is the unit of caching: movies(id PK, page, position, …) ordered by (page, position); observePage(n) is cached() over the DAO's observePage(n), the remote's getPopular(n) and a replacePage(n, rows) write; refresh() fetches page 1 and, on success, replaces the whole table with it in one transaction, so pages 2.. are gone until scrolled to again; a detail is a second table read local-or-network the way getProduct is`, or
`Paging 3 with room-paging and a RemoteMediator` → D79. The owner chose the first on 2026-09-19:
it stays inside `UiState<State>`, tests on the JVM with fakes, and adds no dependency; the row
names what Paging 3 would have given — placeholders, jank-free prefetch — and why the sample does
without them.
**Done when** `python3 scripts/doctor.py` passes with `feature/movies` in the tree —
the generator's five registrations included; `ls feature/movies/domain/src/main/kotlin/**/domain/`
lists `Movie.kt`, `MovieDetail.kt`, `MoviePage.kt`, `MoviesRepository.kt`;
`grep -n 'cached(' feature/movies/data/src/main/kotlin/**/repository/DefaultMoviesRepository.kt`
hits `observePage`; `grep -n '@Transaction' feature/movies/data/src/main/kotlin/**/database/MoviesDao.kt`
hits `replaceAll` (the refresh write) and `replacePage`;
`ls feature/movies/data/schemas/*/1.json` exists; `./gradlew :feature:movies:data:testDebugUnitTest`
passes `DefaultMoviesRepositoryTest` (page 1 emits the cache first and the remote's rows after;
a failing remote on page 2 emits `Failure` and leaves page 1 in the table; `refresh()` on a
failing remote returns `Failure` and drops nothing; `refresh()` on a good remote leaves exactly
page 1's rows; `getMovie(id)` hits the network once on a cold cache and never on a warm one),
`MoviesDaoTest` (order by page then position; `replacePage(2, …)` touches no page-1 row) and
`MoviesDatabaseMigrationTest` the way `CatalogDatabaseMigrationTest` is;
`./gradlew :app:testDevDebugUnitTest --tests '*KoinGraphTest*'` passes with the new module;
`docs/DECISIONS.md` has row D79; `docs/ai/reference/FEATURES.md` § Features has a `movies` row and
`docs/ai/reference/DOMAIN.md` names `MoviesRepository`; `docs/ai/DEPENDENCIES.md` is unchanged
(no new dependency).
**Touches** `feature/movies/{domain,data,di}/**`, `feature/movies/data/schemas/**`,
`feature/movies/data/build.gradle.kts` (the room convention plugin, the way catalog's has it),
`settings.gradle.kts`, `core/di/**`, `app/**/AppNavHost.kt`, `app/**/KoinGraphTest.kt`,
`app/**/ApplicationModule.kt` (the `TmdbConfig` binding), `docs/DECISIONS.md`,
`docs/ai/reference/{FEATURES,DOMAIN}.md`, `docs/ai/CODEBASE.md` (the generator's tree row).
**Read** `service/core/data/src/main/kotlin/.../BaseRepository.kt:83-130` (`cached()`, and why the
snapshot is emitted before the refresh), `feature/catalog/data/src/main/kotlin/.../repository/DefaultCatalogRepository.kt`
(the shape, and `getProduct`'s local-or-network), `feature/catalog/data/src/main/kotlin/.../database/{CatalogDao,CatalogDatabase,Entities,Mappers}.kt`,
`feature/catalog/data/src/main/kotlin/.../source/{CatalogDto,DefaultRemoteCatalogDataSource}.kt`
(a Ktor source against the shared client), `feature/catalog/data/src/test/.../CatalogDatabaseMigrationTest.kt`,
`docs/ai/RECIPES.md` § A new feature (the follow-up steps after the generator), `CLAUDE.md` §
Modules (the `data` split: `repository` and `source`).
**Steps** 1. `python3 scripts/create_feature.py movies --graph home` — all four layers; the
`Movies` screen it writes is the list G1U2 fills, leave it as generated. 2.
`python3 scripts/create_datasource.py movies RemoteMovies --remote --repository Movies` and
`python3 scripts/create_datasource.py movies LocalMovies`; swap the local source's DataStore for
Room — `convention.android.room` on `:feature:movies:data`, `MoviesDatabase` with `MovieEntity`
(`id`, `page`, `position`, `title`, `overview`, `posterPath?`, `releaseDate?`, `voteAverage`)
and `MovieDetailEntity` (`id`, `runtime?`, `tagline?`, `genres` as a comma-joined string,
`backdropPath?`), `MoviesDao`. 3. Domain: `Movie`, `MovieDetail` (a `Movie` plus the detail
fields), `MoviePage(page, movies, totalPages)`, `MoviesRepository { observePage(page): Flow<Outcome<MoviePage>>; suspend refresh(): Outcome<Unit>; suspend getMovie(id): Outcome<MovieDetail> }`.
4. DTOs after TMDB's field names with `@SerialName`; the remote source takes `TmdbConfig` and
builds absolute URLs with `api_key` as a query parameter; a poster path becomes a full URL in the
mapper, from `imageBaseUrl`, so the domain never sees a path. 5. `DefaultMoviesRepository` as
the decision says; `RETRIES` the way catalog has it. 6. `TmdbConfig` in `:feature:movies:domain`
(a data class, no Android), bound in `ApplicationModule` from `BuildConfig`. 7. The three tests;
the schema directory committed. 8. Row D79; the FEATURES and DOMAIN rows.
**Checks** T1 + `KoinGraphTest` + the whole `./gradlew test` (`core/di` moved). **Depends** G1P1.

### G1U1 `AppPullToRefresh` in `:core:ui` · 6

**Why** A feature never imports `androidx.compose.material3.*`, and nothing in the set wraps a
list in a pull gesture; the movies list needs one and the trips and inventory lists could use the
same.
**Done when** `ls core/ui/src/main/kotlin/**/component/AppPullToRefresh.kt` exists with a
`@ComponentPreview`; `grep -n 'PullToRefreshBox' core/ui/src/main/kotlin/**/component/AppPullToRefresh.kt`
hits — material3's box, the indicator drawn in `AppTheme.colors` roles, the offset in
`AppTheme.spacing`; the signature is `AppPullToRefresh(refreshing: Boolean, onRefresh: () -> Unit, modifier, content: @Composable BoxScope.() -> Unit)`;
`grep -n 'AppPullToRefresh' feature/gallery/presentation/src/main/kotlin/**/GalleryCatalog.kt`
hits (the generator writes it); `./gradlew :core:ui:testDebugUnitTest --tests '*AppPullToRefresh*'`
passes a Robolectric test that composes content, swipes down on it and sees `onRefresh` called
once, and sees the indicator when `refreshing` is true; `python3 scripts/doctor.py` passes;
`docs/ai/reference/DESIGN-SYSTEM.md` has the row.
**Touches** `core/ui/src/main/kotlin/**/component/AppPullToRefresh.kt`,
`core/ui/src/test/kotlin/**/AppPullToRefreshTest.kt`, `feature/gallery/presentation/**/GalleryCatalog.kt`,
`core/ui/src/test/**/PreviewScreenshotTest` goldens, `docs/ai/reference/DESIGN-SYSTEM.md`.
**Read** `core/ui/src/main/kotlin/**/component/AppScrollShadow.kt` (a container component's
shape), `core/ui/src/main/kotlin/**/component/AppProgress.kt` (the indicator role), `CLAUDE.md`
§ Design system, `docs/ai/TESTING.md` § Screen tests (a swipe in a Robolectric test).
**Steps** 1. `python3 scripts/create_component.py AppPullToRefresh`. 2. Fill it over
`PullToRefreshBox`; the preview shows the refreshing state. 3. The test. 4.
`recordRoborazziDebug`, open the golden, commit it. 5. The DESIGN-SYSTEM row.
**Checks** T1 + `verifyRoborazziDebug` in the `test` invocation + the whole `./gradlew test`
(`core/` moved). **Depends** —.

### G1U2 The movies list pages, pulls and reads its cache · 12

**Why** The screen G1H1's generator wrote is the template's stub; the feature is the list — the
first thing in the sample that grows as it scrolls and survives a request failing.
**Done when** `assertVisible: id: "MoviesScreen"` holds after the Home card is tapped;
`grep -n 'testTag' feature/movies/presentation/src/main/kotlin/**/movies/MoviesScreen.kt` hits
`movies_list`, `movies_item`, `movies_empty`, `movies_moreProgress` and nothing outside the
vocabulary; `grep -n 'home_moviesCard' feature/home/presentation/src/main/kotlin/**/home/HomeScreen.kt`
hits, and `HomeNavigation` has `OpenMovies` wired in `AppNavHost`, the way `OpenInventory` is;
`grep -n '"movies"' feature/devmenu/presentation/src/main/kotlin/**/devmenu/DevMenuState.kt`
hits a jump; `./gradlew :feature:movies:presentation:testDebugUnitTest` passes `MoviesViewModelTest`
(the initial state is `PREVIEW`, no overlay; page 1 from a fake repository fills `movies`;
`LoadMore` appends page 2 and sets `endReached` when `page == totalPages`; a `Failure` on page 2
raises an alert and keeps page 1; `Refresh` calls `refresh()`, sets `refreshing` while it runs
and clears it after; `OpenMovie(id)` emits `MoviesNavigation.ToDetail(id)`) and
`MoviesScreenTest` (a tap on the first `movies_item` emits `OpenMovie` with its id; scrolling
`movies_list` to the last index emits `LoadMore` once; `refreshing = true` shows the indicator;
an empty state shows `movies_empty`); `ls feature/movies/presentation/src/main/res/values*/strings.xml`
lists every locale with the same keys, all prefixed `movies_`; `python3 scripts/doctor.py` passes;
`docs/ai/reference/FEATURES.md` § Screens has the `Movies` row and the `Home` row names the card.
**Touches** `feature/movies/presentation/src/main/kotlin/**/movies/*.kt` (the six),
`feature/movies/presentation/src/main/kotlin/**/component/MovieRow.kt`,
`feature/movies/presentation/src/test/**`, `feature/movies/presentation/src/main/res/**`,
`feature/home/presentation/**` (the card, `HomeNavigation.OpenMovies`, its strings),
`feature/devmenu/presentation/**/devmenu/DevMenuState.kt`, `app/**/AppNavHost.kt` (the two
lambdas), `docs/ai/reference/FEATURES.md`.
**Read** `feature/catalog/presentation/src/main/kotlin/**/products/{ProductsScreen,ProductsViewModel,ProductsState}.kt`
(a list over `observe(flow = …)`), `feature/home/presentation/src/main/kotlin/**/home/HomeScreen.kt:40-70`
(the inventory card, the one cross-feature push from a tab root, D59),
`feature/inventory/presentation/src/main/kotlin/**/inventory/InventoryScreen.kt` (`AppSkeleton`
while empty and loading), `CLAUDE.md` § MVI conventions (`observe`, the opt-in overlay, `updateData`),
`CLAUDE.md` § Test identifiers, `feature/auth/presentation/src/test/**/LoginScreenTest.kt`.
**Steps** 1. `python3 scripts/create_component.py MovieRow --feature movies`: an `AppListItem`
with the poster in `leading` through `AppImage` (a fixed 2:3 box from `AppTheme.spacing`), the
title as headline, the release year and the rating as supporting — the year through
`LocalFormats.current.date` on the `LocalDate`, the rating as `percent(voteAverage / 10)`.
2. `MoviesState(movies, page, totalPages, loadingMore, refreshing)` with `endReached` derived;
`MoviesEvent { Refresh, LoadMore, OpenMovie(id) }`; `MoviesNavigation.ToDetail(id)`. 3. The view
model: `observe(flow = repository.observePage(1))` in `init` with `loading = overlay()` only
while `movies` is empty — a cache hit draws at once; `LoadMore` observes `observePage(page + 1)`,
guarded by `loadingMore` and `endReached`; `Refresh` is `execute { repository.refresh() }` with
`refreshing` set around it and no overlay (the indicator is the overlay). 4. The screen:
`AppScaffold(screenId = "MoviesScreen")`, `AppPullToRefresh` around a `LazyColumn` of `MovieRow`
with `movies_item` on every row (one constant tag — the per-row-id line in § Next is not this
sprint's), an `AppProgress` row tagged `movies_moreProgress` while `loadingMore`, `AppEmptyState`
tagged `movies_empty` when the list is empty and nothing loads; `LoadMore` fires from a
`snapshotFlow` over the last visible index. 5. The Home card, the dev-menu jump, the strings in
`values` and `values-cs`. 6. The two tests; goldens for the previews. 7. The FEATURES rows.
**Checks** T1 + `verifyRoborazziDebug` in the `test` invocation. **Depends** G1H1, G1U1.

### G1U3 A movie detail from a row · 12

**Why** A row that opens nothing is a table; the detail is where the second TMDB endpoint, the
local-or-network read and the D76 shared element each get a second caller.
**Done when** `assertVisible: id: "MovieDetailScreen"` holds after a row is tapped;
`grep -n 'testTag' feature/movies/presentation/src/main/kotlin/**/moviedetail/MovieDetailScreen.kt`
hits `movieDetail_posterTile`, `movieDetail_ratingValue`, `movieDetail_runtimeValue`,
`movieDetail_genreGroup` and nothing outside the vocabulary; `grep -n 'appSharedElement'
feature/movies/presentation/src/main/kotlin/**/{movies/MoviesScreen,moviedetail/MovieDetailScreen}.kt`
hits both with the same key from `component/MovieSharedElementKeys.kt`;
`./gradlew :feature:movies:presentation:testDebugUnitTest` passes `MovieDetailViewModelTest`
(`movieId` from the route reaches `getMovie`; the state carries the detail; a `Failure` raises
an alert and `initialState` is `null`, since nothing draws before the read) and
`MovieDetailScreenTest` (title, rating, runtime and every genre tag visible from `PREVIEW`; the
up arrow emits `Back`); `grep -n '"movieDetail"' feature/devmenu/presentation/src/main/kotlin/**/devmenu/DevMenuState.kt`
hits a jump with a fixture id on its route; the FEATURES § Screens row exists.
**Touches** `feature/movies/presentation/src/main/kotlin/**/moviedetail/*.kt` (the six),
`feature/movies/presentation/src/main/kotlin/**/component/MovieSharedElementKeys.kt`,
`feature/movies/presentation/src/test/**`, `feature/movies/presentation/src/main/res/**`,
`feature/movies/presentation/src/main/kotlin/**/movies/{MoviesDestination,MoviesScreen}.kt` (the
navigation lambda and the shared-element modifier), `feature/devmenu/presentation/**/devmenu/DevMenuState.kt`,
`app/**/AppNavHost.kt` (the generator's registration), `docs/ai/reference/FEATURES.md`.
**Read** `feature/catalog/presentation/src/main/kotlin/**/productdetail/{ProductDetailScreen,ProductDetailViewModel}.kt`
(a detail with a route argument and a `null` initial state), `feature/catalog/presentation/src/main/kotlin/**/component/SharedElementKeys.kt`
and `docs/DECISIONS.md` row D76, `core/ui/src/main/kotlin/**/component/{AppDescriptionList,AppTag,AppImage}.kt`,
`service/core/ui/src/main/kotlin/**/format/Formats.kt:76-105` (`percent`, `date`, `duration`).
**Steps** 1. `python3 scripts/create_screen.py movies MovieDetail --with-args 'movieId:Int'`.
2. `MovieDetailState(detail)` with a `PREVIEW` built from a fixture row; the view model
`execute(loading = overlay()) { repository.getMovie(movieId) }` in `init`. 3. The screen: the
poster in an `AppImage` with `Modifier.appSharedElement(MovieSharedElementKeys.poster(id))`,
title and tagline in `AppText` roles, an `AppDescriptionList` for release date (`date`), runtime
(`duration` of the minutes) and rating (`percent`), the genres as `AppTag`s in a flow row tagged
`movieDetail_genreGroup`, the overview as body text. 4. The same modifier on the row's poster in
`MoviesScreen`. 5. The jump, the strings in both locales, the two tests, the goldens. 6. The
FEATURES row.
**Checks** T1 + `verifyRoborazziDebug` in the `test` invocation. **Depends** G1H1.

### G1P2 The dev build reaches the real host · 12

**Why** `dev` is the build everyone runs and it never leaves the process (D20); the one way to see
TMDB answer is a `staging` build, which nobody installs. The fixture engine should be able to hand
a request to the network on request — and only the TMDB host, so the catalog keeps its fixtures.
**Decide first** `the dev engine stays a MockEngine and gains one branch: a request to the TMDB host, while FixtureNetwork.realApi is set and BuildConfig.TMDB_API_KEY is not empty, is forwarded to the cachedOkHttpEngine from app/src/main and its response returned as the mock's; the flag is a file beside the failing marker, so it survives a restart and a test can flip it from a var; staging and prod bind ApiSwitch.Unsupported`, or
`a second HttpClient for TMDB, chosen at start from the flag` → D81. The first: one client, one
engine per flavor, and a switch that is a `dev` concern in `dev`'s source set — the shape D68 gave
the offline switch.
**Done when** `ls feature/devmenu/presentation/src/main/kotlin/**/ApiSwitch.kt` exists with
`isSupported`, `isKeyPresent`, `isRealApi()`, `setRealApi(Boolean)` and an `Unsupported` object;
`grep -n 'realApi' app/src/dev/kotlin/**/network/*.kt` hits the marker file and the override var;
`grep -n 'ApiSwitch' app/src/{staging,prod}/kotlin/**/network/NetworkEngine.kt` binds
`Unsupported`; `./gradlew :app:testDevDebugUnitTest --tests '*NetworkEngine*'` passes two more
cases — with `realApiOverride` set and a key present a request to `api.themoviedb.org` reaches
a fake delegate engine and its response comes back, while a request to `dev.example.com` still
gets the fixture; with the flag set and no key the TMDB request gets the fixture and the switch
reports `isKeyPresent = false`; `./gradlew :feature:devmenu:presentation:testDebugUnitTest`
passes the dev-menu test extended with the row (`devMenu_realApiSwitch` toggles and re-reads;
the row is absent when `isSupported` is false; disabled with a hint when the key is absent);
`grep -n 'devMenu_realApiSwitch' feature/devmenu/presentation/src/main/kotlin/**/devmenu/DevMenuScreen.kt`
hits; `docs/DECISIONS.md` has row D81; `docs/ai/reference/FEATURES.md`'s `devmenu` row names the
switch; `README.md`'s setup row for `tmdb.apiKey` (G1P1's) says what the switch does with it.
**Touches** `feature/devmenu/presentation/src/main/kotlin/**/ApiSwitch.kt`,
`feature/devmenu/presentation/src/main/kotlin/**/devmenu/{DevMenuScreen,DevMenuState,DevMenuEvent,DevMenuViewModel}.kt`,
`feature/devmenu/presentation/src/test/**`, `feature/devmenu/presentation/src/main/res/**`,
`app/src/dev/kotlin/**/network/*.kt`, `app/src/{staging,prod}/kotlin/**/network/NetworkEngine.kt`,
`app/src/main/kotlin/**/ApplicationModule.kt` (`debugMenuBindings`), `app/src/test/**/network/NetworkEngineTest.kt`,
`docs/DECISIONS.md`, `docs/ai/reference/FEATURES.md`, the setup doc.
**Read** `feature/devmenu/presentation/src/main/kotlin/**/OfflineSwitch.kt` (the shape to
mirror), `app/src/dev/kotlin/**/network/NetworkEngine.kt:85-130` (`FixtureNetwork`, the marker
file, the override var), `app/src/main/kotlin/**/ApplicationModule.kt:56-70` (`debugMenuBindings`),
`app/src/main/kotlin/**/network/CachedOkHttpEngine.kt`, `docs/DECISIONS.md` rows D20 and D68.
**Steps** 1. `ApiSwitch` beside `OfflineSwitch`. 2. `FixtureNetwork.realApi` with its marker
file and `realApiOverride`; the dev `networkEngine` builds the OkHttp delegate lazily and, in the
`MockEngine` block, forwards a TMDB request when the flag and the key say so — `delegate.execute(request)`
through Ktor's engine API, the response's status, headers and body returned as the mock's. 3.
`DebugMenu.apiSwitch(context)` in `dev`, `ApiSwitch.Unsupported` in the other two, the binding in
`debugMenuBindings`. 4. The row under the offline switch on the dev menu, `devMenu_realApiSwitch`,
with `stringResource` hints in both locales. 5. The tests. 6. Row D81; the docs.
**Checks** T1 + `verifyRoborazziDebug` in the `test` invocation (the dev menu moved). **Depends** G1P1.

### G1H2 Two Maestro flows through the movies · 6

**Why** T4 is the only tier that runs the app end to end, and a feature no flow reaches is one a
refactor can break at the seams — the Home card, the route argument, the offline read — with
every unit test green.
**Done when** `ls .maestro/browse-movies.yaml .maestro/open-a-cached-movie.yaml` both exist;
`browse-movies` signs in, taps `home_moviesCard`, asserts `MoviesScreen`, scrolls `movies_list`
until a row from page 2 is visible (its title is the fixture's twenty-first), taps a `movies_item`,
asserts `MovieDetailScreen`, presses back and asserts `MoviesScreen`; `open-a-cached-movie`
does the same to the detail, then walks Settings → `settings_debugMenuButton` → taps
`devMenu_offlineSwitch` (the first flow to use it), returns and opens the same row again and asserts
`MovieDetailScreen` with `movieDetail_ratingValue` visible; every id either flow names exists —
`python3 scripts/doctor.py` holds it; `maestro test .maestro/browse-movies.yaml` and
`… open-a-cached-movie.yaml` pass on a `devDebug` emulator, and the run's tail is in the pull
request; the flow list in `docs/ai/RECIPES.md` (the `maestro` paragraph) names both.
**Touches** `.maestro/browse-movies.yaml`, `.maestro/open-a-cached-movie.yaml`, `docs/ai/RECIPES.md`.
**Read** `.maestro/browse-to-a-product.yaml`, `.maestro/open-an-uncached-product.yaml` (a
`clearState` flow), `.maestro/config.yaml`, `feature/devmenu/presentation/**/devmenu/DevMenuScreen.kt:110-125`
(the switch's id), `docs/ai/RECIPES.md:80-90`.
**Steps** 1. The two flows, ids only — no `tapOn: "<title>"`, which is the § Next line this sprint
does not take. 2. Run both on the emulator; paste the tail. 3. The RECIPES line.
**Checks** T1. **Depends** G1U3.
