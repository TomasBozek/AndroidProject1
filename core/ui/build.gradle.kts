plugins {
    alias(libs.plugins.convention.android.library.compose)
}

dependencies {
    // Re-exported so a feature's `presentation` module needs only `api(projects.core.ui)` to get
    // both this app's theme and the reusable architecture in `:service:core:ui`.
    api(projects.service.core.ui)
}
