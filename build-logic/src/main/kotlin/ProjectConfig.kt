import org.gradle.api.JavaVersion

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

    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0"

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
}
