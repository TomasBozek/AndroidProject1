package com.example.androidproject1.service.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * "Pick something on screen B, hand it back to screen A."
 *
 * Navigation 3 has no `previousBackStackEntry` to write onto, and a back stack of plain keys has
 * nowhere to hang a value — so without this every feature invents its own channel, and the ones
 * that use a `SavedStateHandle` stop working the moment the responder is reached from two places.
 *
 * The store lives **above** the entries, in a [rememberSaveable] at the nav host, so it outlives
 * the responder being popped and survives process death with the back stack it belongs to. A
 * result is delivered exactly once: [NavResultEffect] consumes it, so returning to the requester
 * later does not replay a selection the user already made.
 *
 * A value must be something a `Bundle` can hold — a primitive, `String`, `Parcelable` or
 * `Serializable` — for the same reason a route argument must be.
 */
@Stable
class NavResultStore internal constructor(initial: Map<String, Any?> = emptyMap()) {

    private val results = mutableStateMapOf<String, Any?>().apply { putAll(initial) }

    /** The responder's half. Pop after calling it; the requester reads on its next composition. */
    fun set(key: String, value: Any?) {
        results[key] = value
    }

    /** Observable read. Reading in composition is what makes the requester recompose. */
    fun peek(key: String): Any? = results[key]

    /** Reads and removes, so a result is handled once. */
    fun consume(key: String): Any? = if (results.containsKey(key)) results.remove(key) else null

    internal fun snapshot(): HashMap<String, Any?> = HashMap(results)

    internal companion object {

        val Saver: Saver<NavResultStore, HashMap<String, Any?>> = Saver(
            save = { it.snapshot() },
            restore = { NavResultStore(it) },
        )
    }
}

val LocalNavResultStore = staticCompositionLocalOf<NavResultStore> {
    error("No NavResultStore. Wrap the NavDisplay in ProvideNavResultStore().")
}

/**
 * Installs the store. Goes around the nav display, not inside an entry: an entry is destroyed when
 * it is popped, which is exactly when the result has to still exist.
 */
@Composable
fun ProvideNavResultStore(content: @Composable () -> Unit) {
    val store = rememberSaveable(saver = NavResultStore.Saver) { NavResultStore() }
    CompositionLocalProvider(LocalNavResultStore provides store, content = content)
}

/**
 * The requester's half: [onResult] runs once, the first time a result appears under [key].
 *
 * A callback rather than a returned value, so there is no "remember to clear it" step — the state
 * a forgotten clear produces is a screen that re-handles the same selection on every recomposition.
 * Named as an effect rather than `rememberNavResult` because it returns nothing and its whole
 * purpose is the side effect, which is the same reason `LaunchedEffect` is spelled that way.
 */
@Composable
fun <T : Any> NavResultEffect(key: String, onResult: (T) -> Unit) {
    val store = LocalNavResultStore.current
    // Peek in composition, consume in the effect. Removing during composition would be a side
    // effect in a function that may be re-run or discarded — and a discarded composition would
    // take the user's selection with it.
    val pending = store.peek(key)
    LaunchedEffect(pending) {
        if (pending == null) return@LaunchedEffect
        @Suppress("UNCHECKED_CAST")
        (store.consume(key) as? T)?.let(onResult)
    }
}

/**
 * The responder's half: `setNavResult(value)`, then pop.
 *
 * The key arrives as a route argument, so the responder does not know or care who asked — which is
 * what lets one picker serve several callers.
 */
@Composable
fun rememberNavResultSender(key: String): (Any?) -> Unit {
    val store = LocalNavResultStore.current
    return remember(store, key) { { value: Any? -> store.set(key, value) } }
}
