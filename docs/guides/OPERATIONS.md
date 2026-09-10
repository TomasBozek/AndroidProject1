# Operations

How a release is cut. Crash reporting, analytics and translations join this file when
`docs/ARCHITECTURE.md` is split — task A0P2.

## Releasing

A release is a tag. Nothing in the repository records a version: `versionName` is the tag without
its `v`, `versionCode` is the commit count, and any build not on a `v*` tag is 1 / `"1.0"`.

1. The release's last task closes the plan: every board line `[x]` or `[-]`, the block written into
   [../spec/CHANGELOG.md](../spec/CHANGELOG.md), the plan moved into `../archive/plans/`.
2. Run the end-to-end flows first — they are weekly by default, and `gh workflow run build.yml`
   starts them on demand. An emulator boot plus the flows is fifteen minutes or more.
3. Tag and push:

   ```bash
   git tag v0.1.0 && git push origin v0.1.0
   ```

4. CI takes over: the release job refuses the tag if the changelog has no block for it, then builds
   the signed release artifact and publishes a GitHub release whose notes are generated from the
   commits since the previous tag. That is why a commit is titled `<id> <title>` — the release page
   is the commit log, read by someone who was not here for it.

Without the signing secrets the build falls back to the debug key. The four secrets are
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`.

**A hotfix to a shipped release** keeps that release's letter — `A0X1` — goes through lane 0 with no
open plan, ships as a patch tag and gets its own short block in the changelog.
