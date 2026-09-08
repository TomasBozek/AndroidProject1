package com.example.androidproject1.core.ui.test

import com.example.androidproject1.core.domain.Logger

/**
 * A [Logger] that records instead of printing, so a test can assert on what was logged — or, far
 * more often, ignore it entirely and just satisfy a constructor.
 */
class FakeLogger : Logger {

    data class Entry(val level: Level, val throwable: Throwable?, val message: String)

    enum class Level { DEBUG, WARN, ERROR }

    val entries = mutableListOf<Entry>()

    val warnings: List<Entry> get() = entries.filter { it.level == Level.WARN }

    val errors: List<Entry> get() = entries.filter { it.level == Level.ERROR }

    override fun withTag(tag: String): Logger = this

    override fun d(throwable: Throwable?, message: () -> String) = record(Level.DEBUG, throwable, message)

    override fun w(throwable: Throwable?, message: () -> String) = record(Level.WARN, throwable, message)

    override fun e(throwable: Throwable?, message: () -> String) = record(Level.ERROR, throwable, message)

    private fun record(level: Level, throwable: Throwable?, message: () -> String) {
        entries += Entry(level, throwable, message())
    }
}
