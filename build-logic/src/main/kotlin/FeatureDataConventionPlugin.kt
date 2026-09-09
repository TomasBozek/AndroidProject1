import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** An Android library that talks to the outside world: the baseline plus coroutines. */
class FeatureDataConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library")

        dependencies {
            add("api", libs.findLibrary("kotlinx-coroutines-core").get())
            // A data module is where the app meets the outside world, so it is the layer most
            // worth testing — and feat.7 has nothing to write against without this line.
            add("testImplementation", libs.findBundle("testing").get())
        }
    }
}
