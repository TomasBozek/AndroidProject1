# Data and network: service/network, service/core/data, feature data/domain layers, flavor wiring, Room and DataStore
<!-- Generated 2026-09-10 from the review agents' saved output. Evidence is path:line at commit 4e0d2fc. Not edited by hand; the ranked summary is ../REVIEW.md. -->

## Summary
The core of this area is genuinely good and above what most 2026 templates ship: the Ktor client with an idempotent-only retry policy and a MockEngine dev flavor, the Room setup with committed schemas and a migration harness that runs under plain `./gradlew test`, the Keystore-backed AES-GCM split, and a `cached()` combinator tested end to end against a real MockEngine and a real SQLite. The waste is in the layers wrapped around that core: ten `XDataSource` interfaces that have exactly one implementation and no fake anywhere in the repo (every repository test uses the `Default` class), a token-refresh scaffold that is bound nowhere and duplicates what Ktor 3.5 already does, and `OutcomeFlows.kt` with zero production callers. There is one real bug in the offline-first path: an empty-table-means-null mapping makes a category with no products spin forever and leaves the `products_empty` state unreachable. The month-one gaps that are not behind Q8 are a connectivity monitor and an HTTP cache; Paging, WorkManager and pinning are correctly parked.

## Winners
### HttpClientFactory retry policy is production-grade

An allowlist of RFC 9110 idempotent methods (never POST), timeouts and cancellation explicitly excluded from retry, exponential backoff with jitter and a cap, `retries = 0` uninstalls the plugin, and seven tests including the backoff timing and the POST case. This is the part most teams get wrong and it is right here.
Evidence: `service/network/src/main/kotlin/com/example/androidproject1/core/network/HttpClientFactory.kt:74-89`; `service/network/src/main/kotlin/com/example/androidproject1/core/network/HttpClientFactory.kt:125-153`; `service/network/src/test/kotlin/com/example/androidproject1/core/network/HttpClientRetryTest.kt:55-121`
### Room: committed schemas plus a migration test that runs without an emulator

`convention.android.room` puts the exported schemas on the unit-test assets and orders the merge after `copyRoomSchemas`; `MIGRATIONS` is one array read by both the Koin builder and the migration test, so a version bump without a migration fails on its own. Now in Android ships none of this. Favourites are correctly in the catalog DB (the JOIN in FavouritesDao is the reason) and the cart is correctly denormalised.
Evidence: `build-logic/src/main/kotlin/AndroidRoomConventionPlugin.kt:22-46`; `feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/database/CatalogDatabase.kt:20-29`; `feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/database/FavouritesDao.kt:17-24`; `feature/cart/data/src/main/kotlin/com/example/androidproject1/feature/cart/data/database/CartDatabase.kt:14-28`
### Crypto: AesGcmAead / KeystoreAead split is correct and small

Random 12-byte IV taken from the provider and prepended, 128-bit tag, 256-bit key, no user-auth requirement, undecryptable file handled as corruption and reset rather than crash. It replaces the deprecated security-crypto with ~90 lines and the JVM-testable part is the part with logic.
Evidence: `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/crypto/AesGcmAead.kt:20-36`; `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/crypto/KeystoreAead.kt:24-44`; `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/crypto/EncryptedStringSerializer.kt:28-37`
### `cached()` plus the end-to-end catalog repository test

One combinator gives every remote-backed screen cache-then-network-then-cache with stale-data-plus-failure semantics, and `DefaultCatalogRepositoryTest` proves it against a real MockEngine and a real in-memory Room instead of faking either side. This is lighter than NiA's Synchronizer and the right size for a template.
Evidence: `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/BaseRepository.kt:83-122`; `feature/catalog/data/src/test/kotlin/com/example/androidproject1/feature/catalog/data/repository/DefaultCatalogRepositoryTest.kt:43-84`
### Engine as a parameter, fixtures on dev

The same shape as NiA's demo flavor: the wrong engine is not in the build, fixtures are reviewable JSON, and the file-marker offline switch can be flipped from adb. When a real API arrives nothing in `service/network` changes; only `prod`'s `networkEngine` gains an OkHttp config.
Evidence: `app/src/dev/kotlin/com/example/androidproject1/network/NetworkEngine.kt:26-52`; `app/src/prod/kotlin/com/example/androidproject1/network/NetworkEngine.kt:13`; `app/src/main/kotlin/com/example/androidproject1/ApplicationModule.kt:22-34`

## Problems (ranked by the reviewer)
### P1 · A data-source interface per source, enforced by doctor and a 458-line generator, for a swap that never happens

**high · over-engineered · effort M · confidence 0.85**

Ten `XDataSource` interfaces (196 lines) each have exactly one `DefaultX` implementation and no fake anywhere: `grep -rl 'Fake[A-Za-z]*DataSource'` over feature/ and service/ finds nothing. The doctor check justifies the layer with 'a repository wired to an implementation cannot have its source swapped for a cache or a fake', yet every repository test constructs the `Default` data source over an in-memory Room or a real DataStore file (DefaultCatalogRepositoryTest:78, DefaultCartRepositoryTest:40-55, DefaultLocalAuthDataSourceTest). The rule buys nothing the tests use and costs four files plus a `FakeXRepository` for a boolean (onboarding) or an enum (theme). Now in Android draws the line differently: the DAO is the local data source, an interface exists for the network source only because a second implementation (demo) exists, and the repository is the seam the rest of the app fakes. `AuthService` is the same disease one layer up: `DefaultAuthService` is four pure delegations over `AuthRepository`.

**Proposal.** Leaner rule: one interface per feature at the domain boundary (`XRepository`) and its `FakeXRepository` in testFixtures; below it, concrete classes. A local source is the DAO or a small concrete `XLocalDataSource` over DataStore; a remote source gets an interface only when a second implementation actually exists (the engine swap already covers fixtures). Delete the ten interfaces, the `no repository imports a data source implementation` doctor check, the `bind X::class` lines, and shrink `create_datasource.py` to the concrete class plus repository pair; merge `DefaultAuthService` into `AuthRepository` or make `AuthService` the only interface. Update CLAUDE.md line 174 and the template feature accordingly.

Evidence: `scripts/doctor.py:489-512`; `scripts/create_datasource.py:1`; `feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/source/LocalCatalogDataSource.kt:1-34`; `feature/catalog/data/src/test/kotlin/com/example/androidproject1/feature/catalog/data/repository/DefaultCatalogRepositoryTest.kt:76-84`; `feature/cart/data/src/test/kotlin/com/example/androidproject1/feature/cart/data/repository/DefaultCartRepositoryTest.kt:40-55`; `feature/settings/domain/src/main/kotlin/com/example/androidproject1/feature/settings/domain/ThemeRepository.kt:1-19`; `feature/auth/domain/src/main/kotlin/com/example/androidproject1/feature/auth/domain/AuthService.kt:22-33`; `CLAUDE.md:174`
### P2 · Empty category never renders: empty table maps to null, `cached` drops null, screen spins forever

**high · bug · effort S · confidence 0.85**

`DefaultLocalCatalogDataSource.observeProducts` returns null for an empty table (`rows.takeIf { it.isNotEmpty() }`), while the interface it implements says an empty list must be a cached empty list, not a miss. Trace a category with zero products: `cached` reads the snapshot (null, nothing emitted), fetches `[]`, writes it, then `emitAll(local.transform { if (value != null ...) })` never emits because the table is still empty. `ProductsViewModel` starts with `initialState = null`, so the loading overlay never clears, and the `ContentState.Empty(products_empty)` branch at ProductsViewModel:41-44 is unreachable through the network path. The dev fixtures happen to give every one of the 4 categories products (12 products, no empty category), and `BaseRepositoryTest` uses a `FakeCache` that emits the written value, so no test sees it. The same mapping applies to `observeCategories` (an empty catalog spins).

**Proposal.** Make 'never fetched' explicit instead of inferring it from row count: a `catalog_sync` table (or a `fetchedAt` column per category) written by `replaceProductsIn`, with `observeProducts` returning null only while no sync row exists. Add a repository test 'a category the server returns empty renders the empty state'. Then delete the contradictory sentence in `LocalCatalogDataSource`.

Evidence: `feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/source/DefaultLocalCatalogDataSource.kt:23-30`; `feature/catalog/data/src/main/kotlin/com/example/androidproject1/feature/catalog/data/source/LocalCatalogDataSource.kt:12-14`; `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/BaseRepository.kt:110-118`; `feature/catalog/presentation/src/main/kotlin/com/example/androidproject1/feature/catalog/presentation/products/ProductsViewModel.kt:21`; `feature/catalog/presentation/src/main/kotlin/com/example/androidproject1/feature/catalog/presentation/products/ProductsViewModel.kt:38-46`
### P3 · Token refresh scaffold (TokenStore, TokenRefresher, SingleFlightTokenRefresher) is bound nowhere and duplicates Ktor

**medium · useless · effort S · confidence 0.8**

222 lines of main plus test code. `coreModule` builds the client with neither `tokenStore` nor `tokenRefresher` (Koin.kt:72-76), no class in app/, core/ or feature/ implements `TokenStore`, and the stored session is `id` + `email` with no token to hand over (DefaultLocalAuthDataSource:44). So the `Auth` block in `HttpClientFactory` is dead in every flavor. Ktor 3.5.2's `AuthTokenHolder` already serialises `loadToken`/`setToken` under a mutex (ktor-client-auth-jvm-3.5.2 sources, `AuthTokenHolder.kt`), so the single-flight is a second lock around a first one. Worse, the class's guard compares `staleAccessToken` with the store, but `HttpClientFactory` feeds it `oldTokens`, which Ktor 3.5.2 fills from `tokensHolder.loadToken()` (BearerAuthProvider.kt:245) — the already-refreshed cached value for a waiter — so the 'another caller refreshed' branch is unlikely ever to fire. Nothing tests the path through the real plugin: the 99-line test drives the class in isolation, never a `MockEngine` answering 401.

**Proposal.** Delete the three classes, `FakeTokenStore` and `SingleFlightTokenRefresherTest`, and the `Auth` block in `HttpClientFactory`. When Q8 lands, wire Ktor's `bearer { loadTokens/refreshTokens }` straight to the session store and add one `HttpClientFactory` test with a `MockEngine` that returns 401 once, plus `clearToken()` on logout. If the owner wants to keep a demonstration, keep only the two interfaces and the `bearer` block, and add that 401 test now.

Evidence: `core/di/src/main/kotlin/com/example/androidproject1/core/di/Koin.kt:71-77`; `service/network/src/main/kotlin/com/example/androidproject1/core/network/HttpClientFactory.kt:104-119`; `service/network/src/main/kotlin/com/example/androidproject1/core/network/SingleFlightTokenRefresher.kt:30-52`; `feature/auth/data/src/main/kotlin/com/example/androidproject1/feature/auth/data/source/DefaultLocalAuthDataSource.kt:44-45`; `service/network/src/test/kotlin/com/example/androidproject1/core/network/SingleFlightTokenRefresherTest.kt:1`
### P4 · OutcomeFlows.kt has no production caller

**medium · useless · effort S · confidence 0.9**

`combineOutcomes` (two arities) and `chainOutcomes` are referenced only from `OutcomeTest.kt`; `grep -rn 'combineOutcomes\|chainOutcomes'` over app/, core/, feature/ and service/ main sources finds nothing but the definitions. The offline-first combinator that is actually used is `BaseRepository.cached()`, and the `UNREACHABLE` failure branch is the sign of a shape that has not met a real caller. `service/` is meant to be copied into other projects; every unused function there is copied too.

**Proposal.** Delete the file and its four tests. If a screen ever needs two repository flows combined, `combine(a, b) { ... }` on `Outcome` in the ViewModel is four lines and the template already has `Outcome.map`.

Evidence: `service/core/domain/src/main/kotlin/com/example/androidproject1/core/domain/result/OutcomeFlows.kt:14-61`; `service/core/domain/src/test/kotlin/com/example/androidproject1/core/domain/result/OutcomeTest.kt:95-118`
### P5 · Two month-one items are not behind Q8 and are missing: a connectivity monitor and an HTTP cache

**medium · missing-standard · effort M · confidence 0.8**

`grep -rl 'ConnectivityManager\|NetworkMonitor\|HttpCache\|okhttp3.Cache'` over the repo finds nothing. NiA's `NetworkMonitor` (a `callbackFlow` over `ConnectivityManager`, ~60 lines in `service/core/data`) and its 'you are offline' snackbar need no server and are the first thing a product team adds; they also give `FixtureNetwork.failing` a real counterpart. The `prod` engine is a bare `OkHttp.create()` with no `Cache`, and Ktor's `HttpCache` plugin is not installed, so the day a real API arrives every list is re-downloaded on every collection even with ETags on the server — PLAN-DETAIL parks 'response caching with ETags' behind Q8, but installing the cache is a config line that needs no API to write. Smaller and in the same file: `HttpErrorMapper` sends 429 to `BadRequestError` ('the client's fault') and 403 to `UnauthorizedError` (sign-out semantics), and the retry policy does not treat 429 or honour `Retry-After`. Paging 3, WorkManager and certificate pinning are correctly parked: they need a real list and a real host.

**Proposal.** Plan 5: (1) `NetworkMonitor` interface in `:service:core:domain`, `ConnectivityManagerNetworkMonitor` in `:service:core:data`, collected once in `MainViewModel` and surfaced as a `UiCommand.Snackbar`; (2) an OkHttp `Cache(context.cacheDir, 10 MB)` in `prod`/`staging`'s `networkEngine` or Ktor `HttpCache` in the factory; (3) a `RateLimitedError` (or `ServerError`) for 429 and a `ForbiddenError` distinct from 401. Leave Paging, WorkManager and pinning where they are.

Evidence: `docs/PLAN-DETAIL.md:211-213`; `app/src/prod/kotlin/com/example/androidproject1/network/NetworkEngine.kt:13`; `service/network/src/main/kotlin/com/example/androidproject1/core/network/HttpClientFactory.kt:57-66`; `service/network/src/main/kotlin/com/example/androidproject1/core/network/HttpErrorMapper.kt:41-53`
### P6 · The staging flavor is a build that cannot load data

**low · inefficient · effort S · confidence 0.7**

`diff -r app/src/staging app/src/prod` is one line (`DebugMenu.ENABLED`). Staging uses the OkHttp engine against `https://staging.example.com/`, a host that does not exist, so every catalog screen on a staging build shows the inline error. Three flavors on one dimension is the industry norm and cheap to declare, but this one exists only to carry a const while multiplying every `assemble`/`test`/`lint` variant by 1.5 until Q8 decides whether a server will ever exist.

**Proposal.** Either give staging the fixture engine too (move `NetworkEngine.kt` and the `res/raw` fixtures from `dev` to a shared `fixtures` source set that both non-prod flavors include) so it is a testable build, or drop it to two flavors and re-add staging with Q8. Decision for the owner; see questions.

Evidence: `build-logic/src/main/kotlin/ProjectConfig.kt:55-57`; `app/src/staging/kotlin/com/example/androidproject1/network/NetworkEngine.kt:13`; `app/src/staging/kotlin/com/example/androidproject1/debug/DebugMenu.kt:9`
### P7 · Session is hand-encoded as a unit-separated string instead of a serialized type

**low · over-engineered · effort S · confidence 0.7**

`EncryptedDataStoreProvider` is a `DataStore<String>`, so the auth data source carries its own wire format: a `VERSION` prefix, `` separators, a `FIELD_COUNT` check and a decoder with its own malformed-input tests. kotlinx.serialization is already on every module's classpath (the presentation plugin and Ktor both use it). A `DataStore<T>` with a generic `EncryptedSerializer<T>(aead, serializer)` would make the store typed, drop the parser, and make the day the session gains an access token a one-field change instead of a `VERSION` bump. Small, but this is the file every real project edits first.

**Proposal.** Generalise `EncryptedStringSerializer` to `EncryptedJsonSerializer<T : Any>(aead, KSerializer<T>, default)`; make `StoredSession` `@Serializable`; delete `encode`/`decode` and the separator tests, keep the 'undecryptable reads as signed out' test.

Evidence: `feature/auth/data/src/main/kotlin/com/example/androidproject1/feature/auth/data/source/DefaultLocalAuthDataSource.kt:44-69`; `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/EncryptedDataStoreProvider.kt:24-36`; `service/core/data/src/main/kotlin/com/example/androidproject1/core/data/crypto/EncryptedStringSerializer.kt:21-25`

## Looks heavy but is justified
- One shared Preferences DataStore file (`app_preferences`) keyed per feature with `distinctUntilChanged` at each reader — the same shape as NiA's single `UserPreferences` store; a file per feature would be more files for no isolation gain.
- `EncryptedDataStoreProvider` separate from `DataStoreProvider` — encrypting only the session and not the theme is the right trade, and Keystore AEAD over a DataStore is the first-party replacement for the deprecated security-crypto.
- The Room migration harness (`MIGRATIONS` array, committed `schemas/`, `MigrationTestHelper` under Robolectric) — it looks heavy for two version-1 databases but it is exactly the thing that is expensive to add after the first migration, and a database per feature keeps each feature's schema its own.
- `BaseRepository.cached()` emitting stale data and then the failure on one flow — it is what makes `ErrorDisplay.Inline` work and the eight tests cover every ordering; lighter than a Store5/Synchronizer dependency.
- `DispatcherProvider` switched at the data source, not the repository — correct, and the comment on `DefaultRemoteCatalogDataSource` explains why; do not move it.
- Repository tests over a real MockEngine and a real in-memory Room (`DefaultCatalogRepositoryTest`, `DefaultCartRepositoryTest`) with inline executors — this is the test pattern to keep even after the data-source interfaces go.
- The delete-then-insert `@Transaction` replace in `CatalogDao` and the denormalised `cart_items` row — both are deliberate and the comments say why; a joined cart would empty on catalog refresh.

## Questions only the owner can answer
- Q8: is a real API ever coming for the sample? If never, `service/network` should ship GET-only fixtures and no Auth block; if yes, the token scaffold should be rebuilt against Ktor's bearer provider with a 401 MockEngine test rather than kept as is.
- Is the data-source interface rule (CLAUDE.md:174, doctor check, `create_datasource.py`) something you would relax to 'interface at the repository only'? It touches the template feature, the generator and the doc, so it is a Plan 5 decision, not a drive-by.
- Should `staging` share the dev fixtures until there is a staging host, or be dropped until Q8 lands?
