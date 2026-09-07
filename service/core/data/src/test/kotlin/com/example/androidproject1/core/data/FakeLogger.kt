package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.Logger

/** Swallows everything — these tests assert on results, not on log output. */
class FakeLogger : Logger {
    override fun withTag(tag: String): Logger = this
    override fun d(throwable: Throwable?, message: () -> String) = Unit
    override fun w(throwable: Throwable?, message: () -> String) = Unit
    override fun e(throwable: Throwable?, message: () -> String) = Unit
}
