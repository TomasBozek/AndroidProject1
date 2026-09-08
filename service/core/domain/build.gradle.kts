plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // Ships FakeLogger to every module that constructs something taking a Logger. It belongs here
    // because Logger does; consume with `testImplementation(testFixtures(projects...))`.
    `java-test-fixtures`
}
