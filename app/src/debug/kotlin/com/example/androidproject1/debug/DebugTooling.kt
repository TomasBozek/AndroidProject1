package com.example.androidproject1.debug

import android.app.Application
import android.os.StrictMode

/**
 * What the `debug` build type adds to the process before anything else runs: StrictMode, so a
 * main-thread disk read — DataStore's first read behind `MainViewModel`, a Room query that lost its
 * dispatcher — is logged the first time it happens rather than noticed the day a real device
 * stutters.
 *
 * `penaltyLog()` and not `penaltyDeath()`: LeakCanary and Compose's tooling both trip the VM
 * policy in ways that are theirs to fix, and a debug build that dies on someone else's violation
 * is a debug build nobody runs. The `release` source set's copy is a no-op, the way
 * `DebugMenu` is per flavor.
 */
fun Application.installDebugTooling() {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build(),
    )
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build(),
    )
}
