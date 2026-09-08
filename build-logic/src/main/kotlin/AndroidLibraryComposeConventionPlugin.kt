import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** [AndroidLibraryConventionPlugin] plus Compose: the compiler plugin, the BOM and the bundle. */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library")
        extensions.configure<LibraryExtension> { configureCompose(this) }
    }
}
