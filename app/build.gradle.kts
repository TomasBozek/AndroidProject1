plugins {
    alias(libs.plugins.convention.android.application)
}

dependencies {
    // Pulls in every core layer and every feature's `di` module, and through them the whole graph.
    // `implementation`, not `api`: nothing consumes the application module.
    implementation(projects.core.di)

    // Declared rather than reached transitively through :core:di: MainActivity uses AppTheme,
    // and MainViewModel uses AuthService.
    implementation(projects.core.ui)
    implementation(projects.feature.auth.domain)

    // The HttpClient's engine is chosen per flavor: fixtures on dev (D20), OkHttp elsewhere.
    implementation(projects.service.network)
    implementation(libs.ktor.client.okhttp)
    devImplementation(libs.ktor.client.mock)

    // MainDispatcherRule + FakeLogger, and FakeAuthService, for MainViewModelTest. Every other
    // module gets the first line from `convention.feature.presentation`; :app is not one.
    testImplementation(testFixtures(projects.service.core.ui))
    testImplementation(testFixtures(projects.feature.auth.domain))

    // The generated startup profile. The module that produces it never ships.
    baselineProfile(projects.baselineprofile)
}
