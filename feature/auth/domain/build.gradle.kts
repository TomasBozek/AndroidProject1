plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeAuthService: the session is cross-cutting, so :feature:settings needs it too.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
