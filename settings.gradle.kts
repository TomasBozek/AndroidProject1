pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidProject1"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

/**
 * A layer of a `service`, `core` or `feature` module. [projectPath] is the Gradle path segment,
 * [filePath] the matching directory on disk.
 */
sealed class ModuleSuffix(name: String) {

    val projectPath: String = name
    val filePath: String = name.replace(":", "/")

    data object Di : ModuleSuffix("di")
    data object Domain : ModuleSuffix("domain")
    data object Infrastructure : ModuleSuffix("infrastructure")
    data object Data : ModuleSuffix("data")
    data object Presentation : ModuleSuffix("presentation")
    data object Ui : ModuleSuffix("ui")
}

/**
 * Includes a module and fails fast, at settings time, if its directory or build file is missing.
 * This turns a mistyped or half-generated module into a clear error instead of a confusing
 * "project not found" later in the build.
 */
fun includeModule(fullName: String, path: String) {
    include(fullName)
    val project = project(fullName)
    project.projectDir = File(settingsDir, path)
    require(project.projectDir.isDirectory) { "${project.projectDir} doesn't exist" }
    require(project.buildFile.isFile) { "${project.buildFile} doesn't exist" }
}

/**
 * Includes a layer of a reusable `service` module. These modules are deliberately free of any
 * app-specific code so that the whole `service/<name>` directory can be copied into another
 * project as-is.
 */
fun includeServiceModule(name: String, vararg suffixes: ModuleSuffix) {
    suffixes.forEach { includeModule(":service:$name:${it.projectPath}", "service/$name/${it.filePath}") }
}

fun includeCoreModule(vararg suffixes: ModuleSuffix) {
    suffixes.forEach { includeModule(":core:${it.projectPath}", "core/${it.filePath}") }
}

fun includeFeatureModule(name: String, vararg suffixes: ModuleSuffix) {
    suffixes.forEach { includeModule(":feature:$name:${it.projectPath}", "feature/$name/${it.filePath}") }
}

include(":app")

// The reusable architecture: DataResult, BaseRepository, BaseViewModel, UiState, Screen().
// Nothing here knows about this app's features, theme or DI graph.
includeServiceModule(
    "core",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Ui,
)

// What is genuinely this app's: the Compose theme and the Koin registration point.
includeCoreModule(
    ModuleSuffix.Ui,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "auth",
    ModuleSuffix.Domain,
    ModuleSuffix.Infrastructure,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "home",
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "settings",
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

// Template module cloned by scripts/create_feature.py. It is included so that `./gradlew build`
// compile-verifies the generator's source template; nothing depends on it.
includeFeatureModule(
    "example",
    ModuleSuffix.Domain,
    ModuleSuffix.Infrastructure,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)
