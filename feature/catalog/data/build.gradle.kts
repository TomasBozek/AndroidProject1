plugins {
    alias(libs.plugins.convention.feature.data)
    alias(libs.plugins.convention.android.room)
    // Ships FakeRemoteCatalogDataSource: a network error is a settable field, not a MockEngine.
    alias(libs.plugins.convention.android.library.testfixtures)
}

dependencies {
    api(projects.service.core.data)
    api(projects.feature.catalog.domain)
    api(projects.service.network)
}
