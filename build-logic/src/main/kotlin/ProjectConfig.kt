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
}
