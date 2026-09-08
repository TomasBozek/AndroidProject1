import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** The single application module. Everything here is app identity or app-wide policy. */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("android-application").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("kotlin-serialization").get().get().pluginId)

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            configureCompose(this)

            defaultConfig {
                applicationId = basePackage
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = ProjectConfig.VERSION_CODE
                versionName = ProjectConfig.VERSION_NAME

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    // R8 on from the start: the keep-rule surface is one screen big today and grows
                    // with every reflection-based library. Rules live in src/main/keepRules/.
                    optimization {
                        enable = true
                    }
                }
            }

            // Only the application module can see the whole graph, so this is the only place the
            // dependencies' lint checks can run.
            lint {
                checkDependencies = true
            }

            buildFeatures {
                buildConfig = true
            }
        }

        dependencies {
            add("implementation", libs.findLibrary("androidx-core-ktx").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            add("implementation", libs.findLibrary("androidx-activity-compose").get())

            add("implementation", platform(libs.findLibrary("koin-bom").get()))
            add("implementation", libs.findBundle("koin-android").get())

            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            add("implementation", libs.findLibrary("androidx-navigation3-ui").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

            // No androidTest source set: UI tests run under Robolectric as unit tests, so CI needs
            // no emulator. See plan item 4.3.
            add("testImplementation", platform(libs.findLibrary("koin-bom").get()))
            add("testImplementation", libs.findBundle("testing").get())
            add("testImplementation", libs.findLibrary("koin-test").get())
            add("testImplementation", libs.findLibrary("koin-test-junit4").get())
        }
    }
}
