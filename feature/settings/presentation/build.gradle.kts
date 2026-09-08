plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    // Another feature's `domain` is fair game; its `presentation` is not — see doctor.py.
    api(projects.feature.auth.domain)
}
