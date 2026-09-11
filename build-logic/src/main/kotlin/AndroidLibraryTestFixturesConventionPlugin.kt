import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Turns on AGP's `src/testFixtures/` source set for an Android library module.
 *
 * Stacked alongside whichever convention plugin the module already applies — `convention.feature.
 * data`, say — rather than folded into one of them, because most modules never ship a `Fake*` for
 * another module's tests to consume; the ones that do ask for this on top. The plain Gradle
 * `java-test-fixtures` plugin does the same job for a Kotlin/JVM module (`service/core/domain`
 * applies it directly), but collides with AGP's own `testFixturesImplementation` configuration on
 * an Android library, which is why this exists as its own plugin rather than one line.
 */
class AndroidLibraryTestFixturesConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        extensions.configure<LibraryExtension> {
            testFixtures {
                enable = true
            }
        }
    }
}
