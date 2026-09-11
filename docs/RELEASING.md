# Releasing

How a release is cut: the tag, the four secrets, the hotfix rule.

A release is a tag. Nothing in the repository records a version: `versionName` is the tag without
its `v`, `versionCode` is that tag's `major.minor.patch` packed two digits per component (`1.2.3` →
`10203`, so a hotfix sorts correctly against the version it patches), and any build not on a `v*`
tag is 1 / `"1.0"`.

1. The release's last task closes the plan: every board line `[x]` or `[-]`, the block written into
   [CHANGELOG.md](CHANGELOG.md), and the plan's board cut out of it into [STATUS.md](STATUS.md).
2. Run the end-to-end flows first — they are weekly by default, and `gh workflow run build.yml`
   starts them on demand. An emulator boot plus the flows is fifteen minutes or more.
3. Tag and push:

   ```bash
   git tag v1.0.0 && git push origin v1.0.0
   ```

4. CI takes over: the release job refuses the tag unless [CHANGELOG.md](CHANGELOG.md) holds a
   `## <tag> ·` heading, then builds the signed release artifact and publishes a GitHub release
   whose notes are that block, byte for byte. Notes are never generated from the commit log — the
   first tag of a repository has no previous tag to diff against, and the block is already written
   in the words someone using the app would recognise.

The four secrets are `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`.
**A tag without them is refused**, before anything is built: an AAB signed with the CI debug key
looks exactly like a real one on the release page. The weekly run and a fork have no secrets and
fall back to the debug key, because those builds exist to keep R8 honest, not to publish.

The release carries two files: the AAB — an Android App Bundle, `bundleProdRelease`'s output and
what a store expects, rather than an APK — and the R8 mapping it was built with, named for the tag.
A minified stack trace is unreadable without it, and the names are rewritten on every build — so
the copy attached to a release is the only one that will ever fit that AAB.

**A hotfix to a shipped release** keeps that release's letter — `A0X1` — goes through lane 0 with no
open plan, ships as a patch tag and gets its own short block in the changelog.
