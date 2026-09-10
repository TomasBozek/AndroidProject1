# Release A · what a v1.0 tag must not carry

Status: open
Agents: 1 · 19 tasks · 136 on the board, 124 of work (~10.3 h) · one branch, one pull request
Rules: [../PROCESS.md](../PROCESS.md) · Checks: `CLAUDE.md` § Checks · Decisions pre-assigned: D46, D47, D48

This release ships as **one pull request**, not one per task — D47, taken in A0P6. That is the only
rule this plan bends, and it bends it once: D17 stands for every release after this one.

One agent, so there is no lane split and no shared-file table to arbitrate. Tasks carry the ids
they already had where the work survived from the draft; the retrospective id is on the line as
`(was F12)` and nowhere else.

Everything this plan drops is dropped for one reason: **it is not broken**. Refactors, subtractions
and new behaviour are release B — [B.md](B.md) holds them with their points intact.

Edits after `Status: open`: your own board line (`[ ]`→`[x]` with `est → act`, or
`· blocked: <≤5 words>`), a `[-]` by the owner, a lane-0 task appended by the owner. Nothing else.

## Board

Worked top to bottom. A0P2 is first because every task after it cites a path it moves.

### A · the docs, before anything else · 25

- [ ] A0P2 Two audiences, one tree · 25 · decides D48

### B · the tag cannot be pushed until these land · 9

- [ ] A0X1 The release job's changelog guard can never match · 6
- [ ] A0P6 One pull request per release · 3 · decides D47

### C · defects · 57

- [ ] A1X1 An empty category stops spinning (was F17a) · 12
- [ ] A1X2 Add-to-cart leaves the nav host (was F17b) · 12
- [ ] A1U1 Every non-root screen carries an Up control (was shell.8) · 12
- [ ] A0X3 The date picker and the dialog speak the device's language · 6
- [ ] A1U2 A text field says its own name (was ui.10) · 6
- [ ] A1U4 Login and Sign-up scroll under the keyboard (was H3) · 3
- [ ] A1X4 The product-detail heart keeps its first value · 3
- [ ] A1X5 A cold-start deep link is applied once · 3

### D · a repository that is finished · 39 on the board, 27 of work

- [ ] A2H3 The R8 mapping ships with the release (was H1) · 6
- [ ] A2H1 The release job refuses to publish a debug-signed build (was F17c) · 6
- [ ] A0P4 README is the front door again · 6
- [ ] A0P5 .gitignore, .gitkeep and a LICENSE · 3
- [ ] A0X2 The Maestro flows start where the app starts · 3
- [ ] A2T1 The dead lines in domain and network (was half of F14) · 3 · decides D46
- [ ] A1T1 Test plumbing stops being copied (was F19) · 12 · landed as commit `fe6e3bf`

### E · ship · 6

- [ ] A0P3 Ship A · 6 · after every other task

## Tasks

### A0X1 The release job's changelog guard can never match · 6

**Why** `.github/workflows/build.yml:199` greps `"· $GITHUB_REF_NAME ·"`, and the block heading
`docs/CHANGELOG.md:10` documents is `## v<x.y.z> · release <letter> · <date>` — the version is
preceded by `## `, never by a middot. Verified: `grep -qF "· v1.0.0 ·"` against that heading exits
non-zero. Every tag fails at the first step of the release job, before anything is built. Two more
holes ride along: `docs/RELEASING.md` still worked-examples `v0.1.0`, and
`--generate-notes` on the first tag of a repository has no previous tag to diff against, so the
v1.0.0 release page would be the entire commit history.
**Done when** the guard greps `"## $GITHUB_REF_NAME ·"`; a throwaway heading proves both the match
and the refusal (`grep -qF '## v1.0.0 ·'` passes, `grep -qF '## v9.9.9 ·'` fails);
`RELEASING.md` says `v1.0.0`; the release step passes `--notes-file` or a written note rather than
`--generate-notes` for a first tag.
**Touches** `.github/workflows/build.yml`, `docs/RELEASING.md`.
**Read** `.github/workflows/build.yml:193-205,238-247` · `docs/CHANGELOG.md:1-25`.
**Checks** T1. **Depends** —

### A0P6 One pull request per release · 3 · decides D47

**Why** `../PROCESS.md` § Task loop step 9 and D17 both say one pull request per task, and
`/task` refuses a plan that is not `Status: open`. Shipping this release as one pull request is
otherwise a violation of the repository's own rules, recorded nowhere.
**Decide first** one pull request for this release only with D17 standing afterwards, or one pull
request per release from now on → D47. The first is recommended: the per-task rule earned itself on
Plans 1–4 and only fails here because the release is a cleanup, not a body of work.
**Done when** D47 is a row in `../../DECISIONS.md`; `../PROCESS.md` names the exception in the
five sentences that assert the per-task rule (§ Ids, § Task loop 9 and 10, § States, § Ship);
`CLAUDE.md` § Working a task says the same; no other sentence in either file contradicts it.
**Touches** `../../DECISIONS.md`, `../PROCESS.md`, `CLAUDE.md`.
**Read** `../PROCESS.md:20,66-72,84-92` · `CLAUDE.md` § Working a task.
**Checks** T1. **Depends** —

### A1X1 An empty category stops spinning (was F17a) · 12

**Why** `DefaultLocalCatalogDataSource.observeProducts` maps an empty table to `null` with
`rows.takeIf { it.isNotEmpty() }`, so `cached()` reads "never fetched" and never emits — the screen
spins for ever and `products_empty` is unreachable. The class KDoc claims the opposite of what the
code does: "Once the remote has written, even an empty write is a list" is false, because the
`takeIf` is on the read, not the write.
**Done when** a fetched-at marker distinguishes "not loaded" from "loaded and empty"; a test opens
an empty category and asserts `ContentState.Empty`; the KDoc describes the code.
**Touches** `feature/catalog/data`, `feature/catalog/presentation/products`.
**Read** `feature/catalog/data/**/source/DefaultLocalCatalogDataSource.kt:11-30` ·
`service/core/data/**/BaseRepository.kt`.
**Checks** T1. **Depends** —

### A1X2 Add-to-cart leaves the nav host (was F17b) · 12

**Why** `AppNavHost.kt:149` takes a `rememberCoroutineScope()` and `:154` launches the cart insert
in it, so a rotation just after the tap cancels the write and the item never reaches the cart. The
comment above it explaining why that is safe is factually wrong.
**Done when** an `AddProductToCart` use case is called through `execute {}` from the product-detail
view model; `grep -n 'rememberCoroutineScope' app/src/main/kotlin/**/AppNavHost.kt` returns
nothing; a view-model test covers the insert.
**Touches** `app/**/AppNavHost.kt`, `feature/catalog/{domain,presentation}`, `feature/cart/domain`.
**Read** `app/src/main/kotlin/**/AppNavHost.kt:145-165`.
**Checks** T1. **Depends** —

### A1U1 Every non-root screen carries an Up control (was shell.8) · 12

**Why** `ProductsScreen`, `ProductPickerScreen` and `GalleryScreen` show a top bar with no arrow,
and `ProductDetailScreen` has no bar at all — four screens whose only way back is the system
gesture. `AppTopBar` already takes `onNavigateUp` and renders the arrow, so the component is ready.
**Done when** all four match the other five; each arrow carries `<stem>_upButton`; each screen test
asserts the navigation event the tap emits.
**Touches** `feature/catalog/presentation`, `feature/gallery/presentation`, `core/ui` (a test tag
forwarded to `AppTopBar`'s icon button), goldens.
**Read** `core/ui/**/component/AppTopBar.kt:30` · `feature/catalog/**/productdetail/ProductDetailScreen.kt:32`.
**Checks** T1 + goldens. **Depends** —

### A0X3 The date picker and the dialog speak the device's language · 6

**Why** `AppDateField.kt:103,107,178,180` and `AppDialog.kt:110` carry the literals `"Cancel"` and
`"Choose"`. Every other string in the app ships in English and Czech, and `doctor.py` fails a module
that misses a translation — the design system is the one place that escapes the check.
**Done when** the five literals are `stringResource(...)` against `core_` resources present in both
locales; `grep -rn '"Cancel"\|"Choose"\|"OK"' --include='*.kt' core/ui/src/main service/core/ui/src/main`
returns only KDoc prose; the overlay goldens still pass.
**Touches** `core/ui/component/{AppDateField,AppDialog}.kt`, `core/ui/src/main/res/values{,-cs}/strings.xml`.
**Checks** T1 + goldens. **Depends** —

### A1U2 A text field says its own name (was ui.10) · 6

**Why** the label is a sibling `Text`, not a semantics property of the input, so a screen reader
announces an unnamed field.
**Done when** `AppTextField`'s label is on the input's semantics node; a test in `:core:ui` asserts
it by `hasText` on the node with `hasSetTextAction`.
**Touches** `core/ui/component/AppTextField.kt` and its test.
**Checks** T1 + goldens. **Depends** —

### A1U4 Login and Sign-up scroll under the keyboard (was H3) · 3

**Why** both are fixed centred columns, so at large font the submit button is unreachable. The draft
plan named three screens; `ProfileScreen` already scrolls, so this is two.
**Done when** Login and Sign-up scroll; a large-font golden shows the submit button.
**Touches** `feature/auth/presentation`, goldens.
**Checks** T1 + goldens. **Depends** —

### A1X4 The product-detail heart keeps its first value · 3

**Why** the favourite flow and the product load race, and nullable state drops the loser — the heart
shows the wrong value until something else recomposes it. This is the one-file carve-out from the
`BaseViewModel` work; the rest of that is B1X1.
**Done when** the two sources are combined rather than written independently; a view-model test
drives the race and asserts the heart.
**Touches** `feature/catalog/presentation/productdetail`.
**Checks** T1. **Depends** —

### A1X5 A cold-start deep link is applied once · 3

**Why** the launch intent is re-read on every activity recreation, so a rotation on a deep-linked
screen re-applies the link and wipes the back stack built since.
**Done when** the intent is consumed once; a test or a documented manual rotation proves the back
stack survives.
**Touches** `app/src/main/kotlin/**`.
**Checks** T1. **Depends** —

### A0P2 Two audiences, one tree · 25 · decides D48

**Why** two files are called `ARCHITECTURE.md` — `docs/ARCHITECTURE.md` (408 lines) and
`docs/spec/ARCHITECTURE.md` (121) — and `docs/README.md` links both, one of them as "splits in task
A0P2". Nine notes across seven documents promise what "task A0P2" will do. `docs/PLAN.md` duplicates
41 of the 42 rows of `../spec/DECISIONS.md` and the copies have already drifted. Underneath that,
33 files are sorted by *kind* — spec, reference, guides, work — which tells a human nothing about
which of them are for them. Sixteen of the 33 are retrospective material that D42 says dies when
this release ships.

**Decide first** sort the tree by depth-of-audience, or keep D41's five zones by kind → D48. Taken:
**by depth**. `ai/` means AI-*only*, not AI-*all* — the agent reads the whole tree, and audience
decides how deep a file sits, never what it contains. That last clause is the whole decision: it is
what makes a human edition and a machine edition of the same fact impossible to write, and D41's
five zones are superseded.

**The tree it lands on**

```
docs/              for a human — six files, none longer than one screen
  README.md        the rozcestník: what to open for what
  STATUS.md        the open release's board, at a glance
  CHANGELOG.md     one block per release, in user words
  DECISIONS.md     every decision, outcome only
  RELEASING.md     cutting a release: the tag, the four secrets, the hotfix rule
  BACKLOG.md       one line per idea, no ids
  ai/              read when a task names it, never by default
    ARCHITECTURE.md  CODEBASE.md  RECIPES.md  TESTING.md  PROCESS.md  DEPENDENCIES.md
    reference/       CORE.md DESIGN-SYSTEM.md DOMAIN.md FEATURES.md SERVICES.md
    plans/           A.md B.md TEMPLATE.md — task briefs, no board
```

**Everything stays under `docs/`, and that is not taste.** `.github/workflows/build.yml:56` decides
"documentation-only" with `grep -qvE '^(docs/|README\.md$|CLAUDE\.md$|\.claude/|…)'`. A sibling
root — `context/`, `ai/` — is not in that allowlist, so every documentation change would classify as
code and pay a 25-minute build, silently and for ever.

**The migration is `git mv` and one cut. There is no step that writes a human version of anything** —
that step is the disease this decision exists to prevent. `STATUS.md` is not a summary of the board;
it *is* the board, cut out of this file at the seam that already exists (`## Board` at line 20,
`## Tasks` at line 53). A moved section has no drift surface and needs no check to keep it honest.

**Done when**
- the tree above is on disk; `docs/PLAN.md`, `docs/REVIEW.md` and `docs/review/` are deleted
- `grep -rn 'docs/PLAN.md\|docs/ARCHITECTURE.md\|docs/REVIEW.md\|docs/spec/\|docs/work/\|docs/guides/\|docs/archive/2026-09-review\|task A0P2\|arrives with task' --include='*.md' --include='*.py' --include='*.yml' .`
  returns nothing
- `docs/STATUS.md` holds this release's board and this file holds no `- [ ]` line
- three checks land in `doctor.py`, inside B0P1's already-scoped `check_docs_index` so the count
  stays 33: **(1) granularity** — no file directly under `docs/` names a path ending `.kt`, `.kts`,
  `.toml` or `.xml`; **(2) one board** — `^- \[[ x-]\] [A-Z][0-9][UXTHPS][1-9] ` matches only in
  `docs/STATUS.md`; **(3) closed set** — `docs/` holds exactly the six named files and `ai/`
- `python3 scripts/doctor.py` and `python3 scripts/test_scripts.py` are green

**Touches** `CLAUDE.md`, `README.md`, `docs/**`, `.claude/commands/*.md`, `scripts/doctor.py`
(appended).

**Steps**
1. `git rm docs/PLAN.md docs/REVIEW.md docs/review/*` — D42 says the review dies when A ships, so
   skip the archive round-trip and never create `docs/archive/`.
2. `git mv` the survivors onto the tree above. `docs/ARCHITECTURE.md` splits at its own headings
   into `ai/RECIPES.md` and `ai/TESTING.md`; `docs/guides/OPERATIONS.md` becomes `RELEASING.md`.
3. Cut this file's `## Board` section into `docs/STATUS.md`, adding one header line: the release
   title, `Open · ships as v1.0.0`, the counts, and a link to the briefs.
4. Repoint the citations. 97 today, of which 28 are inside files step 1 deletes. `CLAUDE.md` has 12,
   `.claude/commands/release.md` 9, `.claude/commands/task.md` 4, `build.yml` 5, `README.md` 3.
5. Strip the nine forward references and rewrite `docs/README.md` as the six-row rozcestník.
6. Append the three checks to `doctor.py`.

**Read** `.github/workflows/build.yml:56` · `docs/README.md` · this file's lines 20–52.
**Checks** T1 + `test_scripts.py`. **Depends** —

**Deferred to B0P1:** `docs/ai/CODEBASE.md`'s content (the module tree moved out of `CLAUDE.md`) and
the `CLAUDE.md` ≤ 300 diet. This task creates the file and its path; B0P1 fills it and repoints
`scripts/_common.py:25`'s `CLAUDE_MD_FILE`.


### A2H3 The R8 mapping ships with the release (was H1) · 6

**Why** a minified stack trace is unreadable without `mapping.txt`, and nothing keeps it.
**Done when** the release job uploads `mapping.txt` as an artifact and attaches it to the tagged
release.
**Touches** `.github/workflows/build.yml`.
**Checks** T1. **Depends** A0X1

### A2H1 The release job refuses to publish a debug-signed build (was F17c) · 6

**Why** `KEYSTORE_BASE64` is optional, so a tag pushed before the secrets exist publishes a GitHub
release carrying an APK signed with an ephemeral CI debug key — indistinguishable from a real one on
the release page.
**Done when** the release job fails hard on a `v*` tag with no keystore, and still falls back to the
debug key on the schedule and on a fork; a dry run on a throwaway tag proves both paths.
**Touches** `.github/workflows/build.yml`.
**Checks** T1. **Depends** A0X1

**Deferred to B2H1:** the AAB and the tag-derived `versionCode`. D32 says a GitHub release carrying
the artifact *is* the release, so neither is needed to tag v1.0.

### A0P4 README is the front door again · 6

**Why** the root README still describes the pre-A0P1 doc world: it links `docs/ARCHITECTURE.md`,
`docs/PLAN.md` and `docs/REVIEW.md` (two of which A0P2 deletes), omits `docs/README.md` entirely,
opens its build block with `./gradlew build` — which `CLAUDE.md` forbids — names
`:app:installDebug`, which is not a task since the flavors landed, and carries a truncated sentence
at line 47 about a plugin that was deleted.
**Done when** the front-door table points at `docs/README.md` and `CLAUDE.md`; the commands are the
ones `CLAUDE.md` § Commands lists, each naming a real variant; no orphan sentence; a stranger can
clone, build and run from this file alone.
**Touches** `README.md`.
**Read** `README.md:1-50` · `CLAUDE.md` § Commands, § Checks.
**Checks** T1. **Depends** A0P2

### A0P5 .gitignore, .gitkeep and a LICENSE · 3

**Why** `.claude/settings.local.json` and `.claude/worktrees/` are ignored only by the owner's
global gitignore and `.git/info/exclude`, so a fresh clone of the template shows them as untracked.
17 `.gitkeep` files are tracked, most in directories that now hold real sources. There is no
`LICENSE` at the root at all, while D22 says "None. All rights reserved" — the absence reads as an
oversight rather than a decision. `licenses/SourceSans3-OFL.txt` is correct and stays.
**Done when** `.gitignore` covers `.claude/settings.local.json`, `.claude/worktrees/` and
`.claude/scheduled_tasks.*`; every `.gitkeep` whose directory holds a real file is gone; a `LICENSE`
states D22 in one paragraph; `git status` on a fresh clone is clean.
**Touches** `.gitignore`, `LICENSE`, the redundant `.gitkeep` files.
**Checks** T1. **Depends** —

### A0X2 The Maestro flows start where the app starts · 3

**Why** `.maestro/sign-in.yaml` does `clearState`, `launchApp`, then `assertVisible: LoginScreen` —
but `SessionState.Onboarding` outranks the other two by design, so a cleared app opens the tour.
Every other flow does `runFlow: sign-in.yaml`, so all five are broken. They run on the weekly
schedule and on dispatch (D30), which is why nobody has noticed.
**Done when** `sign-in.yaml` passes the tour before asserting `LoginScreen`; a dispatched
`gh workflow run build.yml` run is green.
**Touches** `.maestro/sign-in.yaml`.
**Read** `.maestro/sign-in.yaml` · `app/src/main/kotlin/**/SessionState.kt`.
**Checks** T1 + a dispatched maestro run. **Depends** —

### A2T1 The dead lines in domain and network (was half of F14) · 3 · decides D46

**Why** commit `ee47eaf` already deleted `combineOutcomes`, `flatMap`, `recover` and the rest; what
is left of this task is the token-refresh scaffold and the decision that was never written down.
**Decide first** keep the scaffold, or delete it → D46. Keep is recommended and is what `F14` acted
on: it is built on Ktor's `bearer` provider, its stale-token comparison reads correctly, and it is
tested. Record it rather than re-litigate it.
**Done when** D46 is a row in `../../DECISIONS.md`;
`grep -rn 'combineOutcomes\|chainOutcomes' --include='*.kt' .` returns nothing;
`reference/{DOMAIN,SERVICES}.md` no longer describe the types `ee47eaf` deleted.
**Touches** `../../DECISIONS.md`, `reference/DOMAIN.md`, `reference/SERVICES.md`.
**Checks** T1. **Depends** —

### A1T1 Test plumbing stops being copied (was F19) · 12 · landed as commit `fe6e3bf`

**Why** it is done and the board never said so. `grep -rn '@Config(sdk' --include='*.kt' feature core service app`
returns one hit and it is KDoc prose; `build-logic/robolectric/robolectric.properties` carries the
pin; all eleven `PreviewScreenshotTest` files subclass the shared `PreviewScreenshotSpec`.
**Done when** the board line reads `[x] A1T1 … · 12 → 12`. Do not reopen the work.
**Touches** this file. **Checks** —. **Depends** —

### A0P3 Ship A · 6

**Why** a release is a tag plus a changelog block, and the block is what the release job checks for.
**Done when** every line in `docs/STATUS.md` is `[x]` or `[-]`; `../../CHANGELOG.md` carries the
`## v1.0.0 · release A · <date>` block with `Estimate · Actual · Ratio` and the `Tasks:` line; the
guard from A0X1 matches it (`grep -qF '## v1.0.0 ·' docs/CHANGELOG.md`); `docs/STATUS.md` shows
release B as the open one and links its briefs; this file stays at `docs/ai/plans/A.md` with its
board gone and its briefs intact — D48 created no archive, so nothing is moved out of sight.
**Touches** `docs/**`. **Checks** T1. **Depends** every other task

**The v1.0.0 block claims, in user words:** a way back from every screen · a category with nothing
in it says so instead of spinning · a product added to the cart stays added through a rotation ·
fields, dates and dialogs speak the device's language · forms reach their submit button at any font
size · a release carries its mapping file and refuses to publish unsigned.
