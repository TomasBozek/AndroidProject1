plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    // The HttpClient is assembled here; the engine arrives from :app.
    implementation(projects.service.network)
    // `api`, and deliberately: this is the app's single aggregation point, so the destinations each
    // feature's di module re-exports reach :app through here. `buildHealth` reads that as a
    // violation because nothing in Koin.kt's signature mentions them; see CLAUDE.md.
    api(projects.feature.auth.di)
    api(projects.feature.home.di)
    api(projects.feature.settings.di)
    api(projects.feature.catalog.di)
    api(projects.feature.gallery.di)
    api(projects.feature.cart.di)
    api(projects.feature.profile.di)

    implementation(projects.service.core.data)
    implementation(projects.service.core.domain)
}
