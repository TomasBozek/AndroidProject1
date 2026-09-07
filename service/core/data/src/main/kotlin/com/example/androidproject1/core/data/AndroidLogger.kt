package com.example.androidproject1.core.data

import android.util.Log
import com.example.androidproject1.core.domain.Logger

/** [Logger] backed by `android.util.Log`. */
class AndroidLogger(private val tag: String = "App") : Logger {

    override fun withTag(tag: String): Logger = AndroidLogger(tag)

    override fun d(throwable: Throwable?, message: () -> String) {
        Log.d(tag, message(), throwable)
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        Log.w(tag, message(), throwable)
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        Log.e(tag, message(), throwable)
    }
}
