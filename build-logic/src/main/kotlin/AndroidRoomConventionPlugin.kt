import androidx.room.gradle.RoomExtension
import com.android.build.api.dsl.LibraryExtension
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

        val schemas = "$projectDir/schemas"

        extensions.configure<RoomExtension> {
            // Schemas are committed, so a migration arrives as a reviewable diff rather than a
            // binary surprise — and Room can then test the migration against the old schema.
            schemaDirectory(schemas)
        }

        extensions.configure<LibraryExtension> {
            // `MigrationTestHelper` reads a committed schema out of the *assets* of whatever is
            // running it. The Room Gradle plugin puts them in `androidTest`'s; this puts them in
            // the unit test's, so the migration test runs under Robolectric with `./gradlew test`
            // and CI still needs no emulator — the same trade every screen test here makes.
            sourceSets.getByName("test").assets.srcDir(schemas)
            @Suppress("UnstableApiUsage")
            testOptions.unitTests.isIncludeAndroidResources = true
        }

        // The schema directory is written by `copyRoomSchemas`, and a plain `srcDir` tells Gradle
        // nothing about that. Without this the merge can run first, and the run right after a
        // version bump validates against yesterday's schemas — a test that is wrong exactly when
        // it matters.
        tasks.matching { it.name.startsWith("merge") && it.name.endsWith("UnitTestAssets") }
            .configureEach { dependsOn("copyRoomSchemas") }

        dependencies {
            // Robolectric and androidx-test-core, which a DAO test needs for a real SQLite and a
            // Context, arrive with `convention.feature.data` — see there.
            add("api", libs.findLibrary("androidx-room-runtime").get())
            add("api", libs.findLibrary("androidx-room-ktx").get())
            add("ksp", libs.findLibrary("androidx-room-compiler").get())
            // MigrationTestHelper, which opens a database at an old committed schema and runs the
            // migration into the current one. First-party and test-only, so D27 is satisfied by
            // the same line that names it.
            add("testImplementation", libs.findLibrary("androidx-room-testing").get())
        }
    }
}
