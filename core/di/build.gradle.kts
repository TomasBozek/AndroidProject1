plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    // `api`, and deliberately: this is the app's single aggregation point, so the destinations each
    // feature's di module re-exports reach :app through here. `buildHealth` reads that as a
    // violation because nothing in Koin.kt's signature mentions them; see CLAUDE.md.
    api(projects.feature.auth.di)
    api(projects.feature.home.di)
    api(projects.feature.settings.di)
    api(projects.feature.catalog.di)
    api(projects.feature.gallery.di)

    implementation(projects.service.core.data)
    implementation(projects.service.core.domain)
}
