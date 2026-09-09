import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Room and its KSP processor.
 *
 * Applied *beside* `convention.feature.data` by the modules that own a database, rather than
 * folded into it: most `data` modules talk to DataStore or a network and would pay for an
 * annotation processor they never use.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("androidx.room")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<RoomExtension> {
            // Schemas are committed, so a migration arrives as a reviewable diff rather than a
            // binary surprise — and Room can then test the migration against the old schema.
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            // Robolectric and androidx-test-core, which a DAO test needs for a real SQLite and a
            // Context, arrive with `convention.feature.data` — see there.
            add("api", libs.findLibrary("androidx-room-runtime").get())
            add("api", libs.findLibrary("androidx-room-ktx").get())
            add("ksp", libs.findLibrary("androidx-room-compiler").get())
        }
    }
}
