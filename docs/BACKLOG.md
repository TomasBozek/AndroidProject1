# Backlog

One line per idea, in every section — `- <title> · <pts> · <group> [· <kind>] · <why>` (D69).
**No ids** — an id is assigned when a task is written into a sprint, and until then this file is
the cheap place to put something down; the id a line arrived with is `(was D1X2)`. `pts` is a band
guess — 3, 6, 12, 25, 50 — or `?`, and `?` is allowed only under § Someday and § Behind a decision.
The group is what the work touches, so the board can be read by area, feature and layer: a module
path at any depth — `app`, `core:ui`, `feature:auth:data`, `service:network`, one that does not
exist yet — or a process area: `build`, `ci`, `release`, `git`, `process`, `templates`, `claude`,
`tests`. The kind is the id letter the line will get — `U` UI · `X` fix · `T` trim · `H` harden ·
`P` platform · `S` showcase — given where it is known. A section says how ready a line is, never
what it touches. `doctor.py` holds the shape.

**A bug is a line with a shape**: kind `X`, and a why that reads `seen on <flavor> <version>,
expected <what>, steps <n>` before anything else — F4X1 was written from one. The sprint that
takes it writes the `X` task from it.

Add a line here for anything you find while working on something else. Never edit the open sprint.

**Groomed 2026-09-13 (D62).** Everything open in releases B, C and D and every line that was here
was ranked — fixes first, then the build, then the design system, then new features — and the top
two thirds became release E. What follows is the rest, and it is **not carried forward by
default**: the next grooming does each line or deletes it.

## Next

In order. What the sprint after F takes first.

- The Maestro flows stop tapping fixture text · 12 · tests · H · `"Beverages"`, `"Coffee"` and
  `"Olive oil"` are tapped by label. List rows carry one constant tag today, so a per-row id means
  `categories_item_<id>`, and the vocabulary check E1X1 adds has to learn a suffix before anything
  else does — which is why this follows it rather than sitting in E.
- `ContentState` stands in for the whole screen · 6 · service:core:ui · X · `Screen()` draws a
  `ContentMessage` instead of the content, scaffold included, so a non-root screen showing `Empty`
  loses its up arrow and its screen id — E0X1 found `TripsListScreen` unreachable to a flow on a
  cleared app and moved it to an in-screen `AppEmptyState`. Decide whether the chrome's message
  should render inside the screen's shell (a slot the scaffold fills) or whether
  `showContent(Empty)` is for root screens only, and say so in `CLAUDE.md` § MVI.

## DevOps

How the project is built, checked, shipped and planned — with Claude. An improvement sprint drafts
from this section the way a feature sprint drafts from § Next, and the reviewer's job is to keep it
honest: a line lands here whenever the process is caught not doing what its docs say.

- The hook note reads only the literal `.githooks` · 3 · templates · P · `doctor.py` prints
  "the pre-commit hook is not installed" on a clone whose `core.hooksPath` is the same directory
  as an absolute path — which is what a worktree sees — while the hook runs on every commit there.
  Compare resolved paths, or check `git rev-parse --git-path hooks`. Seen in F4U1.
- CI comes back · 3 · ci · P · `build.yml` is disabled (D73) because Actions is paid for this
  account. When the owner wants it: `gh workflow enable build.yml`, put `required_status_checks`
  (`changes`, `conventions`, `build`) back into the `main` ruleset, flip `CLAUDE.md` § Checks and
  `docs/RELEASING.md` § 4 back to "CI runs T2–T4", and run one `main` build to read the coverage
  line F3P6 wrote. Until then nothing watches `main` after a merge and a tag builds nothing.
- Every release so far is signed with the debug key · 6 · release · H · the repository has
  **no secrets** (`gh api …/actions/secrets` → 0), and `v1.0.0`, `v1.1.0` and `v1.2.0` each have
  a release page with an AAB anyway: the step that refuses an unsigned tag lives in the `build`
  job and reads `KEYSTORE_BASE64` from an env only the `release` job sets, so `build` fails on
  every tag while `release` skips the keystore step and publishes the CI-debug AAB `RELEASING.md`
  says it never will. Move the guard into the `release` job before the checkout, make the four
  secrets exist (the owner's keystore), and delete the three release pages or mark them
  pre-release. Found by F3P3's tag run.

## Someday

- Certificate pinning · ? · service:network · H · the client trusts the system store; a pin needs a
  real host to pin (D20).
- ETag caching · ? · service:network · H · every fetch is a full fetch; a conditional request when a
  real API answers with one.
- WorkManager sync · ? · service:core:data · H · nothing runs while the app is closed; a sync worker
  when something has to.
- Paging 3 · ? · feature:catalog:data · H · the lists load whole; paged when a fixture is too long
  to.
- Renovate installed as an app, not just configured · 3 · ci · P · `renovate.json` is in the
  repository and nothing reads it (D26).
- A token pipeline from the design source · ? · core:ui · P · the KSD port is by hand and drift is
  invisible until a re-brand (D29).
- A ViewModel-readable permission state · ? · service:core:ui · H · `PermissionGate` is a
  composable; a view model that has to decide on a permission cannot read one.
- The module graph asserted at build time · ? · build · P · `doctor.py` greps the layer direction;
  a Gradle check would refuse the dependency at configuration time.
- `doctor.py --fix` · ? · templates · P · every check says what is wrong and none repairs it; the
  mechanical ones — a missing registration, a missing translation — could.
- Feature-owned navigation graphs · ? · app · P · `AppNavHost` holds every destination; a feature
  with several screens could own its sub-graph.
- A logger backend · ? · service:core:data · H · `AndroidLogger` writes to Logcat, so a release
  build logs nowhere.
- `explicitApi()` on `service/` · ? · service · H · a public API by default is a reuse contract
  nobody wrote; explicit mode makes every export a decision.
- Generated test ids · ? · templates · P · the `<screenStem>_<element>` ids are typed by hand and
  checked by `doctor.py`; the generators could derive them.
- detekt, when 2.x is stable · ? · build · P · 1.23.8 cannot run on the JDK the daemon is pinned
  to.
- Play upload · ? · release · P · the release job builds and signs `prodRelease` and stops; an
  internal-track upload needs a service account.
- Keyboard shortcuts · ? · core:ui · U · nothing answers a hardware keyboard beyond focus traversal;
  a tablet with one could.
- Z-order roles · ? · core:ui · U · elevation roles say height and nothing says stacking order, so
  a sheet over a banner over a snackbar is decided per screen.
- An arcade of small games, one per component group · ? · feature:arcade · S · the release C draft,
  abandoned for the Inventory showcase (D62). The briefs are in git:
  `git show a64d498:docs/ai/plans/C.md`.

## Behind a decision

- Real token issuance · ? · feature:auth:data · H · the sample talks to Ktor `MockEngine` fixtures
  on the `dev` flavor and nothing else, on purpose (D20); a login that returns a real token waits
  on a real API.
- The refresh path is bound · ? · feature:auth:data · H · `HttpClientFactory` refreshes on a
  `401` once it is given a `TokenStore` and a `TokenRefresher`, and the DI graph passes neither:
  the seam waits on an endpoint the fixtures do not answer (D20).
- A real API behind `staging` and `prod` · ? · service:network · H · `BASE_URL` differs per flavor
  and nothing answers at either; everything that needs a real API waits on one.
- A lifecycle seam · ? · app · H · a real seam, no caller yet: nothing reacts to the app going to
  the background.
- A feature-flag seam · ? · service:flags · H · a real seam, no caller yet: no behaviour is switched
  at runtime.
- A push seam · ? · service:push · H · a real seam, no caller yet: nothing arrives while the app is
  closed.
