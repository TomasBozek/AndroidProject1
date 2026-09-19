import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HasUnitTestBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** The single application module. Everything here is app identity or app-wide policy. */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("android-application").get().get().pluginId)
        // Coverage. Aggregated by the root build; a signal, never a gate — see build.gradle.kts.
        pluginManager.apply("org.jetbrains.kotlinx.kover")
        pluginManager.apply(libs.findPlugin("kotlin-serialization").get().get().pluginId)

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            configureCompose(this)

            defaultConfig {
                applicationId = basePackage
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = releaseVersionCode()
                versionName = releaseVersionName()

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            // One dimension, three environments. A build is `devDebug`, `prodRelease` and so on.
            flavorDimensions += "environment"
            productFlavors {
                ProjectConfig.Flavor.entries.forEach { flavor ->
                    create(flavor.flavorName) {
                        dimension = "environment"
                        applicationIdSuffix = flavor.applicationIdSuffix
                        // The launcher label, so three installed side by side are tellable apart
                        // on the home screen rather than only in Settings.
                        manifestPlaceholders["appLabel"] = flavor.label.format(appName)
                        buildConfigField("String", "BASE_URL", "\"${flavor.baseUrl}\"")
                    }
                }
            }

            // The second host, the same on every flavor (D80). The key comes from
            // `local.properties` and is `""` on a fresh clone, which builds and sees fixtures only.
            defaultConfig {
                buildConfigField("String", "TMDB_API_BASE_URL", "\"${ProjectConfig.Tmdb.API_BASE_URL}\"")
                buildConfigField("String", "TMDB_IMAGE_BASE_URL", "\"${ProjectConfig.Tmdb.IMAGE_BASE_URL}\"")
                buildConfigField("String", "TMDB_API_KEY", "\"${tmdbApiKey()}\"")
            }

            // Release signing, when there is something to sign with. `keystore.properties` is
            // gitignored and absent on a fresh clone, so this is a no-op there and `assembleRelease`
            // still produces an APK — signed with the debug key, which is the honest outcome: it
            // installs and it is obviously not a release artefact.
            val keystore = releaseKeystore()
            if (keystore != null) {
                signingConfigs.create("release") {
                    storeFile = file(keystore.getProperty("storeFile"))
                    storePassword = keystore.getProperty("storePassword")
                    keyAlias = keystore.getProperty("keyAlias")
                    keyPassword = keystore.getProperty("keyPassword")
                }
            }

            buildTypes {
                release {
                    signingConfig = signingConfigs.getByName(if (keystore != null) "release" else "debug")
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

        // One variant's unit tests, not six. See `enableUnitTest` in AndroidConventions.kt: `dev`
        // is the flavor whose fixtures the tests read, and `debug` the build type they assert.
        extensions.configure<ApplicationAndroidComponentsExtension> {
            beforeVariants { variant ->
                val tests: HasUnitTestBuilder = variant
                tests.enableUnitTest = variant.name == "devDebug"
            }
        }

        dependencies {
            add("implementation", libs.findLibrary("androidx-core-ktx").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            add("implementation", libs.findLibrary("androidx-activity-compose").get())
            // The system splash screen, which the app owns: it is installed by MainActivity
            // and themed in app/src/main/res.
            add("implementation", libs.findLibrary("androidx-core-splashscreen").get())
            // Per-app language below API 33 (D75): `AppCompatActivity` applies the stored locale in
            // `attachBaseContext`, and `AppCompatDelegate` is the store. Only :app needs it.
            add("implementation", libs.findLibrary("androidx-appcompat").get())

            add("implementation", platform(libs.findLibrary("koin-bom").get()))
            add("implementation", libs.findBundle("koin-android").get())

            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            add("implementation", libs.findLibrary("androidx-navigation3-ui").get())
            // The bottom bar, and the rail it becomes on a wide screen. Versioned by the
            // Compose BOM, which configureCompose already applies.
            add("implementation", libs.findLibrary("androidx-compose-material3-navigation-suite").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

            // The HttpClient's engine is chosen per flavor: fixtures on dev (D20), OkHttp
            // elsewhere. The flavor-specific configuration exists because `productFlavors` above
            // created it, which is why the line lives here and not in the module (D64).
            add("implementation", libs.findLibrary("ktor-client-okhttp").get())
            add("${ProjectConfig.Flavor.DEV.flavorName}Implementation", libs.findLibrary("ktor-client-mock").get())

            // No androidTest source set: UI tests run under Robolectric as unit tests, so CI needs
            // no emulator. See plan item 4.3.
            // Debug only, and it installs itself — no code in :app references it. A leaked
            // Activity or ViewModel is the failure this catches, and it is the one that never
            // shows up in a test. The stable 2.x line rather than the 3.0 alpha.
            add("debugImplementation", libs.findLibrary("leakcanary-android").get())

            add("testImplementation", platform(libs.findLibrary("koin-bom").get()))
            add("testImplementation", libs.findBundle("testing").get())
            add("testImplementation", libs.findLibrary("koin-test").get())
            add("testImplementation", libs.findLibrary("koin-test-junit4").get())
            // Tests the real OkHttp engine's on-disk cache against a real request/response cycle
            // rather than reimplementing OkHttp's cache logic with a fake.
            add("testImplementation", libs.findLibrary("okhttp-mockwebserver").get())
        }
    }
}
