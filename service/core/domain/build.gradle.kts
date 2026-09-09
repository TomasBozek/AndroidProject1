plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // Ships FakeLogger to every module that constructs something taking a Logger. It belongs here
    // because Logger does; consume with `testImplementation(testFixtures(projects...))`.
    `java-test-fixtures`
}

dependencies {
    // TestDispatchers ships from here for the same reason FakeLogger does — DispatcherProvider is
    // declared in this module — and it is the one fixture needing a library to compile.
    testFixturesImplementation(libs.kotlinx.coroutines.test)
}
