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
    // AppNavHost turns a picked product into a cart line and drives the tab badge, so it names
    // both domains. Another feature's *domain* is allowed; its presentation is not.
    implementation(projects.feature.cart.domain)
    implementation(projects.feature.catalog.domain)
    // Home's inventory count is faked in AppNavHostAnalyticsTest, so the test names the interface.
    implementation(projects.feature.inventory.domain)
    // MainActivity draws the whole app in the stored theme, so it reads the preference.
    implementation(projects.feature.settings.domain)
    // The stored `seen` flag decides whether the app opens on the tour.
    implementation(projects.feature.onboarding.domain)

    // The HttpClient's engine is chosen per flavor: fixtures on dev (D20), OkHttp elsewhere —
    // both engines come from `convention.android.application`, which owns the flavors.
    implementation(projects.service.network)

    // MainDispatcherRule + FakeLogger, and FakeAuthService, for MainViewModelTest. Every other
    // module gets the first line from `convention.feature.presentation`; :app is not one.
    testImplementation(testFixtures(projects.service.core.ui))
    testImplementation(testFixtures(projects.feature.auth.domain))
    testImplementation(testFixtures(projects.feature.settings.domain))
    testImplementation(testFixtures(projects.feature.onboarding.domain))
    testImplementation(testFixtures(projects.feature.catalog.domain))
    testImplementation(testFixtures(projects.feature.cart.domain))
    testImplementation(testFixtures(projects.feature.inventory.domain))
}
