plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeFavouritesRepository: Home shows favourites too, so :feature:home needs it as well.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
