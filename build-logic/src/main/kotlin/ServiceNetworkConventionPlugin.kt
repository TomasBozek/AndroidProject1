import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:service:network` — the HTTP port, as a plain Kotlin/JVM module.
 *
 * Kotlin/JVM rather than an Android library for the same reason `:service:core:domain` is: nothing
 * here touches `android.*`, and the compiler enforcing that is what keeps the module copyable into
 * another project. The engine is not a dependency of this module at all — the client factory takes
 * one — so `:app` supplies OkHttp and a test supplies `MockEngine`.
 */
class ServiceNetworkConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.kotlin.jvm")
        pluginManager.apply(libs.findPlugin("kotlin-serialization").get().get().pluginId)

        dependencies {
            add("api", libs.findBundle("ktor-client").get())
            add("api", libs.findLibrary("kotlinx-serialization-json").get())

            add("testImplementation", libs.findBundle("testing").get())
            // MockEngine: every test here drives the client without a server.
            add("testImplementation", libs.findLibrary("ktor-client-mock").get())
        }
    }
}
