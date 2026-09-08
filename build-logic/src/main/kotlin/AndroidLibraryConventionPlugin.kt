import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The Android library baseline: SDK levels, Java target, the shared lint configuration and the
 * derived namespace. No dependencies — a module applying only this one brings its own.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("android-library").get().get().pluginId)
        // Coverage. Aggregated by the root build; a signal, never a gate — see build.gradle.kts.
        pluginManager.apply("org.jetbrains.kotlinx.kover")
        extensions.configure<LibraryExtension> { configureAndroid(this) }
    }
}
