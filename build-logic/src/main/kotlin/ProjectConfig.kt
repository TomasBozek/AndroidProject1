import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * The SDK and Java levels every module shares. Changing `MIN_SDK` used to be one edit per module.
 *
 * These are deliberately not in `gradle.properties`: they are typed here, and a typo is a
 * compile error rather than a silently ignored string.
 */
object ProjectConfig {

    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 29

    /** The daemon runs JDK 25 and AGP 9 needs 17 to run at all, so 17 is the sensible floor. */
    val JAVA_VERSION = JavaVersion.VERSION_17

    // Robolectric's API level is not here: it is `build-logic/robolectric/robolectric.properties`,
    // because Robolectric reads that format itself off the test classpath. One edit, same as these.

    /**
     * What a build that is not on a release tag is versioned as.
     *
     * Every ordinary build — a pull request, a local `installDevDebug` — is 1 / "1.0", because a
     * number that moves on every commit makes two debug APKs impossible to tell apart from the
     * outside and says nothing true. A build on a `v*` tag takes its version from that tag
     * instead; see [Project.releaseVersionName] below.
     */
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0"

    /** Release tags are `v1.2.0`. The `v` is stripped; the rest is the `versionName` verbatim. */
    const val VERSION_TAG_PREFIX = "v"

    /** Falls back only so an unconfigured build fails with a namespace clash rather than silently. */
    const val BASE_PACKAGE_PROPERTY = "basePackage"

    /** The launcher label, which the flavors below decorate. */
    const val APP_NAME_PROPERTY = "appName"

    /**
     * The three environments an app is built for, and the only thing that differs between them.
     *
     * `dev` and `staging` carry an application-id suffix so all three install side by side — the
     * usual reason a tester cannot reproduce something is that they only have one of them. The
     * base URL is a placeholder until 6.1 introduces a client; the point today is that a screen
     * reads `BuildConfig.BASE_URL` and never a literal.
     */
    enum class Flavor(
        val flavorName: String,
        val applicationIdSuffix: String?,
        val label: String,
        val baseUrl: String,
    ) {
        DEV("dev", ".dev", "%s Dev", "https://dev.example.com/"),
        STAGING("staging", ".staging", "%s Staging", "https://staging.example.com/"),
        PROD("prod", null, "%s", "https://api.example.com/"),
    }

    /**
     * The one host that is not [Flavor.baseUrl]: TMDB, which the movies feature reads on every
     * flavor (D80). The key is not here — it is `tmdb.apiKey` in `local.properties`, untracked,
     * and [TMDB_API_KEY_PROPERTY] is only its name.
     */
    object Tmdb {

        const val API_BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w342"
        const val TMDB_API_KEY_PROPERTY = "tmdb.apiKey"
    }
}

/**
 * The `versionName`: the tag HEAD is on without its `v`, or [ProjectConfig.VERSION_NAME].
 *
 * From the tag rather than from a constant someone remembers to edit, so cutting a release is
 * `git tag v1.2.0 && git push --tags` and the APK cannot disagree with the tag it was built from.
 */
internal fun Project.releaseVersionName(): String =
    git("describe", "--tags", "--exact-match", "--match", "${ProjectConfig.VERSION_TAG_PREFIX}*")
        ?.removePrefix(ProjectConfig.VERSION_TAG_PREFIX)
        ?: ProjectConfig.VERSION_NAME

/**
 * The `versionCode`: the release tag parsed as `major.minor.patch`, or [ProjectConfig.VERSION_CODE].
 *
 * Not the commit count: that only ever goes up on the history it is counted from, not on the
 * history a hotfix branches from, so a hotfix tagged off an older tag could produce a code lower
 * than one a store has already seen — and a store refuses to accept it. Parsing the tag itself
 * fixes that: `v1.2.3` becomes `10203`, two digits each for minor and patch, so `v1.2.1` sorts
 * above `v1.2.0` and below `v1.3.0` regardless of how the two branches' histories compare.
 */
internal fun Project.releaseVersionCode(): Int = versionCodeFor(releaseVersionName())

/**
 * The pure half of [releaseVersionCode]: `major.minor.patch` → `major * 10_000 + minor * 100 +
 * patch`. [ProjectConfig.VERSION_CODE] is the fallback for anything that is not exactly that shape
 * — no tag (`versionName` is [ProjectConfig.VERSION_NAME]), a component that is not a non-negative
 * integer, or a minor/patch of 100 or more, which the two-digit encoding cannot carry — so a
 * malformed tag fails safe to the constant rather than producing a wrong number.
 */
internal fun versionCodeFor(versionName: String): Int {
    val components = versionName.split(".").map { it.toIntOrNull() ?: return ProjectConfig.VERSION_CODE }
    if (components.size != 3 || components.any { it < 0 }) return ProjectConfig.VERSION_CODE
    val (major, minor, patch) = components
    if (minor > 99 || patch > 99) return ProjectConfig.VERSION_CODE
    return major * 10_000 + minor * 100 + patch
}

/**
 * `git` at configuration time, or `null` — HEAD is not on a tag, this is a source zip with no
 * `.git`, or git is not installed. Never an exception: a fallback version builds, a broken
 * configuration phase does not.
 *
 * The value is captured in the configuration cache entry, so a tag created after a configured
 * build is picked up on the next cache miss. CI checks out fresh, and locally the answer is the
 * fallback either way.
 */
private fun Project.git(vararg arguments: String): String? = runCatching {
    val output = providers.exec {
        workingDir = rootProject.projectDir
        commandLine("git", *arguments)
        isIgnoreExitValue = true
    }
    if (output.result.get().exitValue != 0) return@runCatching null
    output.standardOutput.asText.get().trim().ifEmpty { null }
}.getOrNull()
