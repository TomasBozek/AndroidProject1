import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** [AndroidLibraryConventionPlugin] plus Compose: the compiler plugin, the BOM and the bundle. */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("convention.android.library")
            extensions.configure<LibraryExtension> { configureCompose(this) }
            configureScreenshots()
        }
    }
}

/**
 * Roborazzi and the preview scanner: `recordRoborazziDebug` and `verifyRoborazziDebug`, plus what
 * `PreviewScreenshotTest` imports. Here rather than in `configureCompose` so that the task and the
 * libraries that feed it arrive together — `:app` applies Compose too and has no previews to
 * record.
 *
 * The goldens are scanned from the `@Preview` functions that already exist rather than listed, so
 * `ui-tooling` is a test dependency as well as a debug one: it carries the annotation the scanner
 * reads off the test classpath.
 */
private fun Project.configureScreenshots() {
    pluginManager.apply(libs.findPlugin("roborazzi").get().get().pluginId)

    dependencies {
        add("testImplementation", libs.findLibrary("roborazzi").get())
        add("testImplementation", libs.findLibrary("roborazzi-compose").get())
        add("testImplementation", libs.findLibrary("roborazzi-junit").get())
        add("testImplementation", libs.findLibrary("roborazzi-preview-scanner").get())
        add("testImplementation", libs.findLibrary("preview-scanner").get())
        add("testImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
