import com.android.build.api.dsl.TestExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * A `com.android.test` module — today only `:baselineprofile`.
 *
 * It exists for the same reason the others do: the SDK levels, the Java target and the namespace
 * belong in one place, not in a build file. What stays in the module is what is genuinely its own:
 * which project it measures, and which flavour of it.
 */
class AndroidTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("android-test").get().get().pluginId)

        extensions.configure<TestExtension> {
            configureAndroid(this)
            defaultConfig {
                targetSdk = ProjectConfig.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
