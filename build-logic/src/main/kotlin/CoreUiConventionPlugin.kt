import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * [AndroidLibraryComposeConventionPlugin] plus Coil.
 *
 * `:core:ui` is Coil's one consumer — `AppImage` is the one place a feature gets a remote image —
 * so the library lives in the one plugin that module applies rather than in every Compose module's
 * plugin for `doctor.py` to forbid on the rest. A feature cannot import it even by mistake now:
 * it is not on the compile classpath, which is a stronger guarantee than a grep.
 */
class CoreUiConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library.compose")

        dependencies {
            add("implementation", libs.findLibrary("coil-compose").get())
            add("implementation", libs.findLibrary("coil-network").get())
        }
    }
}
