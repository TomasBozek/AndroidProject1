import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * [AndroidLibraryComposeConventionPlugin] plus Coil and the adaptive Navigation 3 scenes.
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

            // The adaptive scene strategies for Navigation 3. Here rather than in
            // `:service:core:ui` beside the rest of Navigation 3, because a pane layout is a
            // decision an app makes about its own screens: `service/` stays the architecture, and a
            // project that reuses it adds this the day it wants two panes. Re-exported, so a
            // destination can carry the metadata.
            add("api", libs.findLibrary("androidx-adaptive-navigation3").get())
        }
    }
}
