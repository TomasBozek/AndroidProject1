package com.example.androidproject1.service.core.domain

/**
 * Minimal logging abstraction, kept free of `android.*` so this module stays the one layer that
 * knows nothing about the platform. Implementations live a layer down, in `:service:core:data`.
 */
interface Logger {

    fun withTag(tag: String): Logger

    fun d(throwable: Throwable? = null, message: () -> String = { "" })

    fun w(throwable: Throwable? = null, message: () -> String = { "" })

    fun e(throwable: Throwable? = null, message: () -> String = { "" })
}
