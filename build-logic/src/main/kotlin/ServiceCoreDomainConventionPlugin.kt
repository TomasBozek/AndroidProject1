import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `:service:core:domain` — the architecture's pure half, plus the fixtures it ships.
 *
 * Named for the module rather than folded into `convention.kotlin.jvm`, because a project that
 * copies `service/` gets its build with it (D64): the module file is a `plugins` block and nothing
 * else, and what it needs is here.
 */
class ServiceCoreDomainConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.kotlin.jvm")
        // Ships FakeLogger to every module that constructs something taking a Logger. It belongs
        // here because Logger does; consume with `testImplementation(testFixtures(projects...))`.
        pluginManager.apply("java-test-fixtures")

        dependencies {
            // TestDispatchers ships from here for the same reason FakeLogger does —
            // DispatcherProvider is declared in this module — and it is the one fixture needing a
            // library to compile.
            add("testFixturesImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
