plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.catalog.domain)
    // Another feature's `domain` is fair game; its `presentation` is not — see doctor.py.
    // AddProductToCart, so product detail writes to the cart in its own ViewModel's scope.
    api(projects.feature.cart.domain)

    // The fakes live with the interfaces they fake.
    testImplementation(testFixtures(projects.feature.catalog.domain))
    testImplementation(testFixtures(projects.feature.cart.domain))
}
