plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    // `api` only for presentation: a feature's destinations are the surface :app assembles the nav
    // graph from, and they reach it through :core:di. The other layers are this module's own
    // business — it registers them, it does not re-export them.
    api(projects.feature.profile.presentation)

    implementation(projects.feature.profile.data)
    implementation(projects.feature.profile.domain)
}
