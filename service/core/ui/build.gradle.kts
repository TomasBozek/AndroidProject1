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
    // SavedStateHandle + toRoute(): BaseViewModel reads typed navigation arguments.
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.androidx.navigation.compose)

    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.bundles.testing)
}
