package com.example.androidproject1.service.core.domain.test

import com.example.androidproject1.service.core.domain.ErrorTracker

/**
 * Records what would have been reported, beside the interface it fakes.
 *
 * [users] is a list rather than a latest-value, because the order matters: signing out has to send
 * `null`, and asserting only the final value would pass even if it never did.
 */
class FakeErrorTracker : ErrorTracker {

    val users = mutableListOf<String?>()
    val recorded = mutableListOf<Throwable>()
    val logs = mutableListOf<String>()

    override fun recordNonFatal(throwable: Throwable, message: String?) {
        recorded += throwable
    }

    override fun log(message: String) {
        logs += message
    }

    override fun setUser(id: String?) {
        users += id
    }
}
