import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** The version catalog, so a convention plugin declares dependencies the way a module used to. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * The base package, from `basePackage` in gradle.properties. `init_project.py` rewrites that one
 * line instead of a `namespace` in every module.
 */
internal val Project.basePackage: String
    get() = providers.gradleProperty(ProjectConfig.BASE_PACKAGE_PROPERTY).orNull
        ?: error(
            "Missing `${ProjectConfig.BASE_PACKAGE_PROPERTY}` in gradle.properties — the convention " +
                "plugins derive every module's namespace from it.",
        )

/**
 * `:feature:auth:presentation` becomes `<base>.feature.auth.presentation`, `:app` becomes `<base>`.
 *
 * A module that needs something else still sets `namespace` itself and keeps it; this only fills
 * in the blank.
 */
internal fun Project.namespaceFromPath(): String {
    val segments = path.removePrefix(":").split(":").filter { it.isNotEmpty() }
    return if (segments == listOf("app")) basePackage else (listOf(basePackage) + segments).joinToString(".")
}

/**
 * The Android configuration every module shares — the block that used to be copied 24 times.
 *
 * Written against `CommonExtension`'s property getters rather than its `lint { }` / `defaultConfig { }`
 * blocks: those action forms are declared on `LibraryExtension` and `ApplicationExtension`
 * separately, not on the interface both share.
 */
internal fun Project.configureAndroid(extension: CommonExtension) {
    if (extension.namespace == null) {
        extension.namespace = namespaceFromPath()
    }

    extension.compileSdk {
        version = release(ProjectConfig.COMPILE_SDK)
    }

    extension.defaultConfig.minSdk = ProjectConfig.MIN_SDK

    extension.compileOptions.sourceCompatibility = ProjectConfig.JAVA_VERSION
    extension.compileOptions.targetCompatibility = ProjectConfig.JAVA_VERSION

    configureKotlinJvmTarget()

    // Shared configuration; see lint.xml at the repo root.
    extension.lint.lintConfig = rootProject.file("lint.xml")
    extension.lint.warningsAsErrors = false
    extension.lint.abortOnError = true
}

/**
 * Kotlin's `jvmTarget`, stated rather than left to a default.
 *
 * An Android module inherits it from `compileOptions` and only warns when the two drift apart; a
 * plain Kotlin/JVM module does not inherit it at all and fails the build, because the daemon's JDK
 * is 25 and `compileJava` is on [ProjectConfig.JAVA_VERSION].
 */
internal fun Project.configureKotlinJvmTarget() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(ProjectConfig.JAVA_VERSION.toString()))
    }
}

/**
 * Compose: the compiler plugin, the build feature and the BOM-pinned artifacts.
 *
 * `api` rather than `implementation` because a Compose module's public surface is composables
 * taking Compose types — `:core:ui` re-exports it deliberately, and every other consumer already
 * had it on the compile classpath.
 */
internal fun Project.configureCompose(extension: CommonExtension) {
    pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)

    extension.buildFeatures.compose = true
    // Must be set before the screenshot plugin is applied — it reads the flag while applying,
    // and the message it gives when the flag is missing does not say so.
    @Suppress("UnstableApiUsage")
    extension.experimentalProperties["android.experimental.enableScreenshotTest"] = true
    // Renders every @Preview in the `screenshotTest` source set and diffs it against a committed
    // golden. `updateDebugScreenshotTest` records, `validateDebugScreenshotTest` checks.
    pluginManager.apply(libs.findPlugin("android-screenshot").get().get().pluginId)
    // Gradle 9 fails a Test task that discovers nothing. A module with no previews in its
    // screenshotTest source set is normal here, so let it pass rather than gate on emptiness.
    tasks.withType(Test::class.java).configureEach {
        failOnNoDiscoveredTests.set(false)
        // The screenshot engine is a JUnit Platform engine; the rest of the repo is JUnit 4, which
        // AGP configures by default. Only the screenshot tasks switch.
        if (name.endsWith("ScreenshotTest")) {
            useJUnitPlatform()
        }
    }

    dependencies.apply {
        add("api", platform(libs.findLibrary("androidx-compose-bom").get()))
        add("api", libs.findBundle("compose-core").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        // Images. Declared here because that is where library dependencies live, but only
        // :core:ui may import it — `AppImage` is the one place features get a remote image, and
        // doctor.py fails on a `coil3` import anywhere under feature/.
        add("implementation", libs.findLibrary("coil-compose").get())
        add("implementation", libs.findLibrary("coil-network").get())
        add("screenshotTestImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        // The plugin's JUnit Platform engine. It lands on the classpath by itself on AGP 8.x; on
        // AGP 9 it does not, and without it the task discovers nothing and silently records no
        // goldens. Declared here so a module cannot be quietly missing it.
        add("screenshotTestRuntimeOnly", libs.findLibrary("android-screenshot-validation-engine").get())
    }
}
