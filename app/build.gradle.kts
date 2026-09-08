plugins {
    alias(libs.plugins.convention.android.application)
}

dependencies {
    // Pulls in every core layer and every feature's `di` module, and through them the whole graph.
    // `implementation`, not `api`: nothing consumes the application module.
    implementation(projects.core.di)
}
