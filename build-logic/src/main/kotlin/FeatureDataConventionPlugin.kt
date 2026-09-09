import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** An Android library that talks to the outside world: the baseline plus coroutines. */
class FeatureDataConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.android.library")
        // A data module is where DTOs live, so @Serializable has to actually generate something
        // here. Without the plugin the annotation is inert and the failure arrives at runtime as
        // "Serializer for type argument is not found", which is a long way from the cause.
        pluginManager.apply(libs.findPlugin("kotlin-serialization").get().get().pluginId)

        dependencies {
            add("api", libs.findLibrary("kotlinx-coroutines-core").get())
            // A data module is where the app meets the outside world, so it is the layer most
            // worth testing — and feat.7 has nothing to write against without this line.
            add("testImplementation", libs.findBundle("testing").get())
            // FakeLogger, which every BaseRepository subclass needs to be constructed at all, and
            // TestDispatchers, which every source that switches at its boundary needs.
            add("testImplementation", testFixtures(project(":service:core:domain")))
            // Testing a data module means giving it a Context: a DataStore wants `filesDir`
            // exactly as a DAO wants a database file. Here rather than in
            // `convention.android.room`, where it used to be — every module with a database is
            // also a data module, so nothing lost it and the DataStore-backed ones gained it.
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("testImplementation", libs.findLibrary("androidx-test-core").get())
            // MockEngine. Test-only, so it costs a data module that never talks to a network
            // nothing, and the one that does can drive its own HTTP without a server.
            add("testImplementation", libs.findLibrary("ktor-client-mock").get())
        }
    }
}
