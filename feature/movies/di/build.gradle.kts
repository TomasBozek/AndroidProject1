plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    // `api` only for presentation: a feature's destinations are the surface :app assembles the nav
    // graph from, and they reach it through :core:di. The other layers are this module's own
    // business — it registers them, it does not re-export them.
    api(projects.feature.movies.presentation)

    implementation(projects.feature.movies.data)
    implementation(projects.feature.movies.domain)
}
