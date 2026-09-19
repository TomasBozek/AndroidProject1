# Features and screens

What the sample app contains. The layer rules and the module list are
[../CODEBASE.md](../CODEBASE.md).

## Features

| Feature | Layers | In the app |
|---|---|---|
| `auth` | domain · data · presentation · di | Sign-in and sign-up. Owns the session; every other feature reads it through `MainViewModel` |
| `catalog` | domain · data · presentation · di | Categories, products, product detail, search, a product picker. Holds the Catalog tab |
| `cart` | domain · data · presentation · di | The cart and its badge count. Holds the Cart tab |
| `profile` | domain · data · presentation · di | Name, email and an avatar taken from the photo picker |
| `settings` | domain · data · presentation · di | Theme, language, permissions, the way into profile and the debug menu. Holds the Settings tab. Reads `feature/auth/domain` to sign out; `LanguageRepository` is declared here and implemented in `:app` (D75) |
| `onboarding` | domain · data · presentation · di | The first-run flow, behind one stored flag |
| `home` | presentation · di | The landing tab: favourites, and the two cards that open Inventory and Movies. Reads `feature/inventory/domain` for the count |
| `gallery` | presentation · di | Every component in `:core:ui`, with its states. Reached from the debug menu |
| `devmenu` | presentation · di | Build information, a jump straight to any deep screen with a fixture on its route, the gallery, the component playground, the test tools, and two switches on the fixture engine: the server down (D68) and TMDB requests sent to the real host (D81), the second disabled with a hint when the build has no key. Debug builds only |
| `template` | domain · data · presentation · di | What the generators clone. Compiled by the build so it cannot rot |
| `trips` | domain · data · presentation · di | A trip list, a three-step wizard, a detail with tabs, a destination picker and a dashboard — the showcase for the components only the gallery reached before (B3S1) |
| `inventory` | domain · data · presentation · di | Things you own: a searchable, filterable, sortable list with selection mode, a four-step editor and a detail whose sections switch by width. Reached from Home (D59); the showcase that homed the last thirteen components (E3S1–E3S6, D65) |
| `movies` | domain · data · presentation · di | TMDB's popular list, paged as it scrolls, pulled to refresh, cached per page in Room (D79) so it reads offline, and a detail per row. The one feature over a host that is not `BASE_URL` (D80). Reached from Home |

## Screens

One row below per screen, each a directory of six files plus two tests — the count is the table's
length rather than a number written here, because a number written here goes stale the next time a
generator runs. A route key with no parameters is a
`data object`; one with parameters is a `data class`, and those parameters reach the view model
through its constructor.

| Screen | Feature | Route arguments | Reached from |
|---|---|---|---|
| `Onboarding` | onboarding | — | the first-run flow, when the seen flag is unset |
| `Login` | auth | — | the auth flow's root |
| `SignUp` | auth | — | Login |
| `Home` | home | — | the Home tab; two `AppCard`s side by side — the inventory count opens Inventory, the movies card opens Movies — the cross-feature pushes from a tab root |
| `Categories` | catalog | — | the Catalog tab |
| `Products` | catalog | `categoryId`, `categoryName` | Categories |
| `ProductDetail` | catalog | `productId` | Products, Search, the cart, a deep link |
| `ProductSearch` | catalog | — | Categories |
| `ProductPicker` | catalog | `resultKey` | the cart; returns its choice through the result store |
| `Cart` | cart | — | the Cart tab |
| `Settings` | settings | — | the Settings tab |
| `SettingsPermissions` | settings | — | Settings |
| `SettingsLanguage` | settings | — | Settings. The per-app language: one radio per `AppLanguage` — the device's own, English, Čeština — stored through `LanguageRepository` and applied by the platform at once (D75); the ring follows the store, never the tap |
| `Profile` | profile | — | Settings |
| `DevMenu` | devmenu | — | Settings, when the debug menu is enabled |
| `Gallery` | gallery | — | DevMenu |
| `GalleryDetail` | gallery | `componentId` | Gallery |
| `DevMenuPlayground` | devmenu | — | DevMenu, the Tools section. A component picked from `playgroundCatalog`, its knobs as controls generated from their shapes, the stage drawn from their values (D77) |
| `Template` | template | — | not reachable; the generators clone it |
| `TemplateArgs` | template | `templateId` | not reachable; cloned by `--with-args` |
| `Trips` | trips | — | the Trips tab |
| `TripsList` | trips | — | Trips |
| `TripWizard` | trips | — | Trips, TripsList |
| `TripDetail` | trips | `tripId` | TripsList, Trips' next-trip card |
| `DestinationPicker` | trips | `resultKey` | TripWizard; returns its choice through the result store |
| `Inventory` | inventory | — | the list: search by name, a row per item with the owner's avatar and the condition tag, a FAB to a new item. Reached from Home once E3S6 lands; a row opens the detail |
| `InventoryEditor` | inventory | `itemId` | Inventory (a new id, minted by the list, or an existing item's). Create and edit are one screen — a loaded item fills the form and `isDirty` compares against it, so opening and leaving asks nothing; save keeps the id: four steps behind `AppStepProgress` — basics, quantity and price, tags and owner, review — every kind of control the design system has, `DiscardBackHandler` on a dirty first step, save through `saveItem` and pop |
| `InventoryDetail` | inventory | `itemId` | Inventory, a row. Observes the item, so an edit shows up without a reload; picture, owner with a condition-toned status dot, tags, and three sections — Overview, Notes, History (derived) — switched by `AppBottomNav` on a compact width and `AppNavRail` otherwise, from `SizeClass`. A top-bar menu offers Edit (the editor on this id) and Delete, confirmed through an `AlertPayload` |
| `Movies` | movies | — | Home's movies card, and the dev menu; a row opens `MovieDetail`. TMDB's popular list a page at a time (D79): rows with poster, date and rating, the next page asked for three rows before the end, a footer spinner while it comes, `AppPullToRefresh` to start over, an in-screen empty state; page 1's cache draws first and a failed refresh over it is a snackbar |
| `MovieDetail` | movies | `movieId` | a `Movies` row, and the dev menu with a fixture id. The cached detail or one round trip then cached: the poster (shared with the row, D76), tagline, an `AppDescriptionList` of release date, runtime and rating through `LocalFormats`, the genres as tags that wrap, the overview |
<!-- create_screen.py appends a starter row here -->

## Tabs

Five, in this order: **Home**, **Catalog**, **Cart** (the only one with a badge), **Trips**,
**Settings** — five being Material's ceiling for a bottom bar, so a sixth is a decision (D59). They
are one `TopLevelDestination` entry each — a route key, a label string in `:app` and an icon — and
the bar renders as a rail once there is width for one. A tab root carries no up control; the bar is
what leaves it.

## Flows

**First run.** The stored seen flag is unset, so the session resolves to `Onboarding` and the
onboarding screen is the only entry composed. Marking it seen moves the session to `SignedOut`.

**Signing in.** `SignedOut` composes the auth entries alone. A successful login changes the session,
and `MainViewModel` swaps the whole display for the tabbed one. No screen navigates between the two
flows itself.

**Picking a product for the cart.** The cart pushes the picker with a result key, the picker sets a
result and pops, and the cart's registered callback fires once. The key travels as a route argument,
so one picker can serve several callers and knows nothing about any of them.

**Opening a product.** The row-to-detail push in the catalog is the shared-element showcase
(F4S1, D76): the product's name and price carry `Modifier.appSharedElement` on both screens, keyed
by the product id, so on a phone they travel from the row to the detail's title and figure and
back. On a wide window the list and the detail are one scene and nothing travels — the modifier is
a no-op there, as it is in every preview and test.

**Keeping an inventory.** Inventory has no tab (D59); the card on Home is its door, wired as a
lambda in `AppNavHost.homeEntries` the way Settings reaches Profile. From the list, the FAB mints
a new id and opens the editor on it; the four steps end in `saveItem` and a pop; a row opens the
detail, which observes the item; the detail's menu opens the editor on the same id or asks before
`deleteItems`, and leaves once — the delete and the observed `null` are one departure, not two.
`.maestro/inventory.yaml` drives the whole loop by id, sign-in to empty list.

**Debug menu.** Present on `dev` and `staging` only. Settings shows the entry, the entries
themselves are registered only when it is enabled, and the gallery and the component playground
sit behind it — so no release build contains a route to any of the three.

**Planning a trip.** `Trips` is the dashboard: a next-trip card and a way into `TripsList`. Either
one's "new trip" opens `TripWizard`, whose three steps are one screen with a `step` in its state.
The second step pushes `DestinationPicker` with a result key and gets a destination id back the
same way the cart gets a product — set, then pop, read once by `NavResultEffect`. Saving pops the
wizard; the list and the dashboard are both observing the same table, so the new trip appears on
either without a refresh.

`trips` is the fifth tab (D59). It registers under the signed-in flow's `mainEntries()` like every
other feature, and `TripsScreen` is a tab root with no up arrow — so the bottom bar is the only
thing that reaches it, and the four screens behind it are reached from there.
