import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
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

        // A screen test runs under Robolectric as a plain unit test, so CI needs no emulator.
        // Compose's test rule reads real resources, which is what this switch is for.
        extensions.configure<LibraryExtension> {
            @Suppress("UnstableApiUsage")
            testOptions.unitTests.isIncludeAndroidResources = true
        }

        dependencies {
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())

            add("implementation", platform(libs.findLibrary("koin-bom").get()))
            add("implementation", libs.findBundle("koin-android").get())

            // @Serializable routes.
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

            add("testImplementation", libs.findBundle("testing").get())
            // A screen test: Compose's test rule plus Robolectric, so `./gradlew test` covers
            // behaviour a ViewModel test cannot — what is on screen, and what a tap does.
            add("testImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
            // MainDispatcherRule and FakeLogger, so no screen test rewrites Dispatchers.setMain.
            add("testImplementation", testFixtures(project(":service:core:ui")))
        }
    }
}
