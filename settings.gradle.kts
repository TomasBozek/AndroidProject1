pluginManagement {
    // The convention plugins the modules apply. An included build rather than `buildSrc`, so it
    // has its own settings file and can be copied into another project alongside `service/` —
    // see scripts/export_service.py.
    includeBuild("build-logic")
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
    data object Data : ModuleSuffix("data")
    data object Presentation : ModuleSuffix("presentation")
    data object Ui : ModuleSuffix("ui")
}

/** Includes a module, failing at settings time if its directory or build file is missing. */
fun includeModule(fullName: String, path: String) {
    include(fullName)
    val project = project(fullName)
    project.projectDir = File(settingsDir, path)
    require(project.projectDir.isDirectory) { "${project.projectDir} doesn't exist" }
    require(project.buildFile.isFile) { "${project.buildFile} doesn't exist" }
}

/** Includes a layer of a reusable `service` module — copyable into another project as-is. */
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

// The reusable architecture. Nothing here knows this app's features, theme or DI graph.
includeServiceModule(
    "core",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Ui,
)

// Reusable too, but flat rather than layered: it is one port to the outside world, so there is no
// domain/data split to make. `includeServiceModule` takes layer suffixes, which this has none of.
includeModule(":service:network", "service/network")

// What is genuinely this app's: the Compose theme and the Koin registration point.
includeCoreModule(
    ModuleSuffix.Ui,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "auth",
    ModuleSuffix.Domain,
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
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "catalog",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "gallery",
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "cart",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "profile",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "devmenu",
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "onboarding",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "trips",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

includeFeatureModule(
    "inventory",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)

// Cloned by scripts/create_feature.py. Included so `./gradlew test` keeps the template compiling:
// nothing depends on it, so its own module tests are what prove the generators' source still builds.
includeFeatureModule(
    "template",
    ModuleSuffix.Domain,
    ModuleSuffix.Data,
    ModuleSuffix.Presentation,
    ModuleSuffix.Di,
)
