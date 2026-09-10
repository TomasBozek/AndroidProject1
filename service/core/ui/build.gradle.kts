plugins {
    alias(libs.plugins.convention.android.library.compose)
}

android {
    // Keeps this module's string names from silently colliding with a consuming app's. The only
    // Android configuration left in a module build file, because it is the only one that is not
    // shared — see the convention plugins in build-logic/.
    resourcePrefix = "core_"

    // Ships MainDispatcherRule to every module that tests a ViewModel, so none of them re-writes
    // Dispatchers.setMain/resetMain. Consume with `testImplementation(testFixtures(projects...))`.
    testFixtures {
        enable = true
    }
}

dependencies {
    api(projects.service.core.domain)

    api(libs.androidx.activity.compose)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    // Navigation 3, re-exported: a feature's XDestination.kt names `NavKey` and
    // `EntryProviderScope` while depending only on this app's own ui module.
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)

    // Re-exported: a module that takes these fixtures for MainDispatcherRule gets FakeLogger too,
    // so a screen test still needs one testFixtures line rather than two.
    testFixturesApi(testFixtures(projects.service.core.domain))

    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)

    // The screenshot harness every `presentation` module's PreviewScreenshotTest subclasses.
    // `Api`, not `Implementation`: the base class names Roborazzi and the preview scanner in its
    // own signature, so a consumer compiling against it needs them too. Test-only, so none of it
    // reaches a release build — and it travels with `service/`, which is the point: a project that
    // copies this directory gets the screenshot convention with it, not just the architecture.
    testFixturesApi(platform(libs.androidx.compose.bom))
    testFixturesApi(libs.androidx.compose.ui.test.junit4)
    testFixturesApi(libs.roborazzi)
    testFixturesApi(libs.roborazzi.compose)
    testFixturesApi(libs.roborazzi.preview.scanner)
    testFixturesApi(libs.preview.scanner)
    testFixturesImplementation(libs.robolectric)

    testImplementation(testFixtures(projects.service.core.domain))
    testImplementation(libs.bundles.testing)
}
