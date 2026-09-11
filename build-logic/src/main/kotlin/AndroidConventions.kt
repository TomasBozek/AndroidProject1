import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
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
 * The release keystore's credentials, or `null` when `keystore.properties` is not there.
 *
 * The file is gitignored on purpose — a keystore and its passwords do not belong in a repository,
 * and a template that shipped one would teach the opposite. CI writes it from secrets before
 * building a release; a fresh clone simply does not have it, and everything still assembles.
 *
 * Required keys: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. A file that is present
 * but missing one is an error rather than a silent fall back to the debug key — a release signed
 * with the wrong key is worse than a build that stops.
 */
internal fun Project.releaseKeystore(): java.util.Properties? {
    val file = rootProject.file("keystore.properties")
    if (!file.exists()) return null
    val properties = java.util.Properties().apply { file.inputStream().use(::load) }
    val missing = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        .filter { properties.getProperty(it).isNullOrBlank() }
    if (missing.isNotEmpty()) {
        error("keystore.properties is missing: ${missing.joinToString(", ")}")
    }
    return properties
}

/**
 * The launcher label, from `appName` in gradle.properties. The flavors decorate it rather than
 * each shipping a `app_name` string of their own.
 */
internal val Project.appName: String
    get() = providers.gradleProperty(ProjectConfig.APP_NAME_PROPERTY).orNull
        ?: error(
            "Missing `${ProjectConfig.APP_NAME_PROPERTY}` in gradle.properties — the flavors " +
                "derive their launcher labels from it.",
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
/**
 * Why the two Android convention plugins switch `enableUnitTest` off for all but one variant.
 *
 * A unit test here is a JVM test against the module's own classes, and it does not know which
 * variant compiled them: `testReleaseUnitTest` runs the same assertions as `testDebugUnitTest`,
 * and `:app` ran them again once per flavor on top of that — 923 executions for 498 tests, which
 * was most of a `./gradlew build`. The one variant is `debug` for a library and `devDebug` for
 * `:app`, because `dev` is the flavor whose fixtures the tests read (D20).
 *
 * What that gives up is small and is covered elsewhere. A test that would fail only under
 * `release` would have to branch on `BuildConfig.DEBUG`; a test that would fail only under `prod`
 * or `staging` would have to reach into that flavor's source set, which is two files. Both are
 * compiled by the pull-request gate and assembled, linted and minified by the release job.
 *
 * `enableUnitTest = false` removes the component rather than skipping its task, so `test` and
 * `check` depend only on what is left — there is no exclusion to remember anywhere else.
 *
 * Both call sites assign through a `HasUnitTestBuilder` local rather than on the variant builder
 * directly. On AGP 9.4.0 `variant.enableUnitTest` does not compile — `Unresolved reference` — even
 * though `LibraryVariantBuilder` and `ApplicationVariantBuilder` both list `HasUnitTestBuilder`
 * among their supertypes and neither redeclares the property. Naming the interface that declares
 * it is what makes it resolve, so the local is load-bearing rather than style.
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

    configureRobolectricSdk(extension)

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

    dependencies.apply {
        add("api", platform(libs.findLibrary("androidx-compose-bom").get()))
        add("api", libs.findBundle("compose-core").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        // Images. Declared here because that is where library dependencies live, but only
        // :core:ui may import it — `AppImage` is the one place features get a remote image, and
        // doctor.py fails on a `coil3` import anywhere under feature/.
        add("implementation", libs.findLibrary("coil-compose").get())
        add("implementation", libs.findLibrary("coil-network").get())

        // Compose's test rule plus Robolectric, so anything composable in this module can be
        // asserted by `./gradlew test` with no emulator. `convention.feature.presentation` adds
        // the same for a feature's screens; this covers :core:ui and :service:core:ui, which
        // apply the compose library plugin directly.
        add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
        add("testImplementation", libs.findLibrary("robolectric").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
    }

    // Compose's test rule reads real resources.
    extension.testOptions.unitTests.isIncludeAndroidResources = true

    configureComposeCompiler()
}

/**
 * Puts the shared `robolectric.properties` on every module's test resources.
 *
 * Robolectric reads that file from the test classpath, so one copy in `build-logic/robolectric/`
 * pins the API level for every module at once — the same shape as `compose-stability.conf` beside
 * it, and it travels with `service/` because `export_service.py` copies `build-logic/`. Before
 * this, the pin was `private const val ROBOLECTRIC_SDK` and the paragraph explaining it in each of
 * 51 test files.
 *
 * A directory of its own rather than `build-logic/` itself, because a source directory takes
 * everything under it and `build-logic` is a Gradle build. `findByName` rather than `getByName`
 * so a module with no `test` source set is a no-op rather than a configuration-time failure.
 */
internal fun Project.configureRobolectricSdk(extension: CommonExtension) {
    extension.sourceSets.findByName("test")?.resources?.srcDir(
        rootProject.file("build-logic/robolectric"),
    )
}

/** The Gradle property that turns the compiler's stability reports on: `-PcomposeMetrics`. */
private const val COMPOSE_METRICS_PROPERTY = "composeMetrics"

/**
 * What the Compose compiler is told, and what it is asked to report back.
 *
 * The stability configuration is always applied — it changes which composables can skip, so a
 * build that read it and a build that did not would generate different code. The metrics are
 * behind `-PcomposeMetrics`, because they add a compiler pass and a directory of CSV per module to
 * every ordinary build:
 *
 * ```
 * ./gradlew assembleDevDebug -PcomposeMetrics
 * cat build/compose-reports/feature-home-presentation/presentation-classes.txt
 * ```
 *
 * Reports land under the root build directory, one sub-directory per module: the compiler names
 * its files after the module's own name, and eleven modules called `presentation` would otherwise
 * overwrite each other's report and leave one.
 */
internal fun Project.configureComposeCompiler() {
    val metrics = providers.gradleProperty(COMPOSE_METRICS_PROPERTY).isPresent
    val slug = path.removePrefix(":").replace(':', '-')

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // What `@Immutable` cannot say, because the type is in a Kotlin/JVM module or in the
        // standard library. See the file's own comments — each line there is a promise.
        stabilityConfigurationFiles.add(
            rootProject.layout.projectDirectory.file("build-logic/compose-stability.conf"),
        )

        if (metrics) {
            metricsDestination.set(rootProject.layout.buildDirectory.dir("compose-metrics/$slug"))
            reportsDestination.set(rootProject.layout.buildDirectory.dir("compose-reports/$slug"))
        }
    }
}
