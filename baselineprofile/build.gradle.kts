plugins {
    alias(libs.plugins.convention.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    // What this module measures, and which flavour of it. Both are genuinely its own — everything
    // else is in the convention plugin.
    targetProjectPath = ":app"
    defaultConfig {
        // :app has three flavours and this module has none, so it says which one it exercises.
        // dev is the one that is always installable. (Profile generation needs API 28+; the
        // shared minSdk of 29 already clears that, so there is nothing to override.)
        missingDimensionStrategy("environment", "dev")
    }
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
