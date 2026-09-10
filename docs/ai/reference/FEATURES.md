# Features and screens

What the sample app contains. The layer rules are [../../../CLAUDE.md](../../../CLAUDE.md) and the
module list is [../CODEBASE.md](../CODEBASE.md).

## Features

| Feature | Layers | In the app |
|---|---|---|
| `auth` | domain · data · presentation · di | Sign-in and sign-up. Owns the session; every other feature reads it through `MainViewModel` |
| `catalog` | domain · data · presentation · di | Categories, products, product detail, search, a product picker. Holds the Catalog tab |
| `cart` | domain · data · presentation · di | The cart and its badge count. Holds the Cart tab |
| `profile` | domain · data · presentation · di | Name, email and an avatar taken from the photo picker |
| `settings` | domain · data · presentation · di | Theme, permissions, the way into profile and the debug menu. Holds the Settings tab. Reads `feature/auth/domain` to sign out |
| `onboarding` | domain · data · presentation · di | The first-run flow, behind one stored flag |
| `home` | presentation · di | The landing tab |
| `gallery` | presentation · di | Every component in `:core:ui`, with its states. Reached from the debug menu |
| `devmenu` | presentation · di | Build information and the way into the gallery. Debug builds only |
| `template` | domain · data · presentation · di | What the generators clone. Compiled by the build so it cannot rot |

## Screens

Eighteen, each a directory of six files plus two tests. A route key with no parameters is a
`data object`; one with parameters is a `data class`, and those parameters reach the view model
through its constructor.

| Screen | Feature | Route arguments | Reached from |
|---|---|---|---|
| `Onboarding` | onboarding | — | the first-run flow, when the seen flag is unset |
| `Login` | auth | — | the auth flow's root |
| `SignUp` | auth | — | Login |
| `Home` | home | — | the Home tab |
| `Categories` | catalog | — | the Catalog tab |
| `Products` | catalog | `categoryId`, `categoryName` | Categories |
| `ProductDetail` | catalog | `productId` | Products, Search, the cart, a deep link |
| `ProductSearch` | catalog | — | Categories |
| `ProductPicker` | catalog | `resultKey` | the cart; returns its choice through the result store |
| `Cart` | cart | — | the Cart tab |
| `Settings` | settings | — | the Settings tab |
| `SettingsPermissions` | settings | — | Settings |
| `Profile` | profile | — | Settings |
| `DevMenu` | devmenu | — | Settings, when the debug menu is enabled |
| `Gallery` | gallery | — | DevMenu |
| `GalleryDetail` | gallery | `componentId` | Gallery |
| `Template` | template | — | not reachable; the generators clone it |
| `TemplateArgs` | template | `templateId` | not reachable; cloned by `--with-args` |

## Tabs

Four, in this order: **Home**, **Catalog**, **Cart** (the only one with a badge), **Settings**. They
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

**Debug menu.** Present on `dev` and `staging` only. Settings shows the entry, the entries
themselves are registered only when it is enabled, and the gallery sits behind it — so no release
build contains a route to either.
