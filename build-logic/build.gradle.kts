import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.androidproject1.buildlogic"

// 17 is Gradle's own baseline for plugin code and is unrelated to the modules' Java level, which
// ProjectConfig.JAVA_VERSION sets.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // compileOnly: these plugins are on the consuming build's classpath already — the convention
    // plugin only needs their DSL types to compile against.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    // Room's Gradle extension (the `room { schemaDirectory(...) }` block) and KSP's, so the
    // convention plugin compiles against their DSL without putting them on a module's classpath.
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)

    testImplementation(libs.junit)
}

// One entry per plugin id. The `convention.` prefix means init_project.py never has to rename them
// when it rewrites the base package.
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "convention.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "convention.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "convention.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("coreUi") {
            id = "convention.core.ui"
            implementationClass = "CoreUiConventionPlugin"
        }
        register("androidLibraryTestFixtures") {
            id = "convention.android.library.testfixtures"
            implementationClass = "AndroidLibraryTestFixturesConventionPlugin"
        }
        register("kotlinJvm") {
            id = "convention.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
        register("androidRoom") {
            id = "convention.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("serviceNetwork") {
            id = "convention.service.network"
            implementationClass = "ServiceNetworkConventionPlugin"
        }
        register("featureData") {
            id = "convention.feature.data"
            implementationClass = "FeatureDataConventionPlugin"
        }
        register("featureDi") {
            id = "convention.feature.di"
            implementationClass = "FeatureDiConventionPlugin"
        }
        register("featurePresentation") {
            id = "convention.feature.presentation"
            implementationClass = "FeaturePresentationConventionPlugin"
        }
    }
}
