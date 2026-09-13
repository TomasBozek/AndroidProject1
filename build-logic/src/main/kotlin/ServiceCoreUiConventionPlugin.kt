import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:service:core:ui` — the MVI shell, Navigation 3 re-exported, and the test fixtures every
 * `presentation` module's tests stand on.
 *
 * Named for the module so a copied `service/` brings its build with it (D64). The fixtures are the
 * reason this is the largest plugin: `MainDispatcherRule` and the screenshot harness ship from
 * here so that no consuming module re-writes `Dispatchers.setMain` or names Roborazzi itself.
 */
class ServiceCoreUiConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library.compose")
        // `src/testFixtures/`, for MainDispatcherRule and the screenshot harness below. Consume
        // with `testImplementation(testFixtures(projects...))`.
        pluginManager.apply("convention.android.library.testfixtures")

        dependencies {
            add("api", libs.findLibrary("androidx-activity-compose").get())
            add("api", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
            add("api", libs.findLibrary("androidx-lifecycle-viewmodel-savedstate").get())
            // Navigation 3, re-exported: a feature's XDestination.kt names `NavKey` and
            // `EntryProviderScope` while depending only on this app's own ui module.
            add("api", libs.findLibrary("androidx-navigation3-runtime").get())
            add("api", libs.findLibrary("androidx-navigation3-ui").get())
            add("api", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

            add("testFixturesImplementation", libs.findLibrary("junit").get())
            add("testFixturesImplementation", libs.findLibrary("kotlinx-coroutines-test").get())

            // The screenshot harness every `presentation` module's PreviewScreenshotTest
            // subclasses. `Api`, not `Implementation`: the base class names Roborazzi and the
            // preview scanner in its own signature, so a consumer compiling against it needs them
            // too. Test-only, so none of it reaches a release build — and it travels with
            // `service/`, which is the point: a project that copies this directory gets the
            // screenshot convention with it, not just the architecture.
            add("testFixturesApi", platform(libs.findLibrary("androidx-compose-bom").get()))
            add("testFixturesApi", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            add("testFixturesApi", libs.findLibrary("roborazzi").get())
            add("testFixturesApi", libs.findLibrary("roborazzi-compose").get())
            add("testFixturesApi", libs.findLibrary("roborazzi-preview-scanner").get())
            add("testFixturesApi", libs.findLibrary("preview-scanner").get())
            add("testFixturesImplementation", libs.findLibrary("robolectric").get())

            add("testImplementation", libs.findBundle("testing").get())
        }
    }
}
