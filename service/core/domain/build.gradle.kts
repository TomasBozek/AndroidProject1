plugins {
    alias(libs.plugins.convention.feature.data)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
