plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeCartRepository: the badge lives in :app, so more than this feature tests against it.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
