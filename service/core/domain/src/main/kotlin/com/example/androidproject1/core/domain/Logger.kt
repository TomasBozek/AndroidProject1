package com.example.androidproject1.core.domain

/**
 * Minimal logging abstraction so that domain and data classes can log without depending on
 * the Android framework at their call sites.
 *
 * The interface deliberately stays here and free of `android.*`: this module is the one layer that
 * knows nothing about the platform. Implementations live a layer down —
 * see `AndroidLogger` in `:service:core:data`.
 */
interface Logger {

    fun withTag(tag: String): Logger

    fun d(throwable: Throwable? = null, message: () -> String = { "" })

    fun w(throwable: Throwable? = null, message: () -> String = { "" })

    fun e(throwable: Throwable? = null, message: () -> String = { "" })
}
