import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:service:core:data` — [FeatureDataConventionPlugin] plus DataStore, which is the one store the
 * module's own sources name (`:service:core:data` holds the preference-backed bases). Named for the
 * module so a copied `service/` brings its build with it (D64).
 */
class ServiceCoreDataConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.feature.data")

        dependencies {
            add("api", libs.findLibrary("androidx-datastore-preferences").get())
        }
    }
}
