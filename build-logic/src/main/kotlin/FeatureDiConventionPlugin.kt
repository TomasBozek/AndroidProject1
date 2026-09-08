import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** A module whose only job is Koin registration: the baseline plus Koin. */
class FeatureDiConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library")

        dependencies {
            // `api`, not `implementation`: the module object this layer exists to publish is a
            // Koin `Module`, so Koin is in its signature. `buildHealth` agrees.
            add("api", platform(libs.findLibrary("koin-bom").get()))
            add("api", libs.findBundle("koin-android").get())
        }
    }
}
