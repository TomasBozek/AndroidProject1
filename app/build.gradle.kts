plugins {
    alias(libs.plugins.convention.android.application)
}

dependencies {
    // Pulls in every core layer and every feature's `di` module, and through them the whole graph.
    // `implementation`, not `api`: nothing consumes the application module.
    implementation(projects.core.di)

    // Declared rather than reached transitively through :core:di: MainActivity uses AppTheme and
    // Screen(), and MainViewModel uses AuthService.
    implementation(projects.core.ui)
    implementation(projects.feature.auth.domain)
}
