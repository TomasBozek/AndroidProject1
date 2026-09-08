import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

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

    // Shared configuration; see lint.xml at the repo root.
    extension.lint.lintConfig = rootProject.file("lint.xml")
    extension.lint.warningsAsErrors = false
    extension.lint.abortOnError = true
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
    }
}
