import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Everything a feature's screens need. This is the twelve dependency lines that used to be repeated
 * in every `presentation` module, including the test fixtures — so a generated screen's test
 * compiles with no dependency edit at all.
 */
class FeaturePresentationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library.compose")
        pluginManager.apply(libs.findPlugin("kotlin-serialization").get().get().pluginId)

        dependencies {
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())

            add("implementation", platform(libs.findLibrary("koin-bom").get()))
            add("implementation", libs.findBundle("koin-android").get())

            // @Serializable routes.
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            add("implementation", libs.findLibrary("androidx-navigation-compose").get())

            add("testImplementation", libs.findBundle("testing").get())
            // A screen that takes route arguments decodes them through an android.os.Bundle, so its
            // test needs Robolectric. Here rather than per module, because `create_screen.py
            // --with-args` generates such a test into any feature and edits no build file.
            add("testImplementation", libs.findLibrary("robolectric").get())
            // MainDispatcherRule and FakeLogger, so no screen test rewrites Dispatchers.setMain.
            add("testImplementation", testFixtures(project(":service:core:ui")))
        }
    }
}
