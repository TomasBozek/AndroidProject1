package com.example.androidproject1.service.core.domain

/**
 * Where a product event goes to be counted.
 *
 * Free of `android.*` and of any vendor, like everything else in this module, and for the same
 * reason [ErrorTracker] is: **the repo carries no analytics SDK and no vendor config file**. A
 * template that shipped one would make every project that starts from it either use that vendor or
 * unpick it first. What it carries is the seam.
 *
 * The screen view is the one event every product wants and the one nobody should have to remember
 * to send. `AppScaffold` already knows every screen's id, so it reports [screen] itself — a screen
 * that passes `screenId` is measured, and there is no per-screen call to forget. Everything else is
 * a deliberate [event] at the point it happens.
 */
interface Analytics {

    /**
     * A screen was shown. The id is the screen's own test identifier — `"SettingsScreen"` — so the
     * name in the funnel, the name the screen reader says and the name a Maestro flow asserts are
     * one name rather than three that drift.
     */
    fun screen(id: String)

    /**
     * Something the user did that the product wants counted.
     *
     * @param params keep them few and low-cardinality. A parameter that can take a user's id or a
     * free-text field turns one event into millions and is unusable in every vendor's console.
     */
    fun event(name: String, params: Map<String, Any?> = emptyMap())

    companion object {

        /**
         * Counts nothing. The default in tests, in previews, and wherever no analytics has been
         * provided — which is why [screen] can be reported unconditionally without a build that
         * measures nothing paying for it.
         */
        val NoOp: Analytics = object : Analytics {
            override fun screen(id: String) = Unit

            override fun event(name: String, params: Map<String, Any?>) = Unit
        }
    }
}

/**
 * The default binding: events go to the log and nowhere else.
 *
 * A real implementation rather than a placeholder — in a debug build seeing the funnel in logcat is
 * what you want, and in a project that never adds a vendor it stays correct. Swapping in GA4,
 * Amplitude or Mixpanel is a different binding in `:app`; see CLAUDE.md.
 */
class LoggingAnalytics(logger: Logger) : Analytics {

    private val logger = logger.withTag("Analytics")

    override fun screen(id: String) {
        logger.d { "screen $id" }
    }

    override fun event(name: String, params: Map<String, Any?>) {
        logger.d { if (params.isEmpty()) name else "$name $params" }
    }
}
