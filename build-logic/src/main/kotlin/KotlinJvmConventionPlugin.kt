import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * A plain Kotlin/JVM module: no manifest, no AAR, no lint pass, and `android.*` unavailable at the
 * compiler level rather than by convention. Intended for every `domain` module — see plan item 1.2.
 */
class KotlinJvmConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("kotlin-jvm").get().get().pluginId)

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = ProjectConfig.JAVA_VERSION
            targetCompatibility = ProjectConfig.JAVA_VERSION
        }

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
