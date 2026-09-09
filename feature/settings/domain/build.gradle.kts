plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeThemeRepository: the preference is read on Settings and again at the root, so
    // :app's tests need the same fake this feature's do.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
