package com.example.androidproject1.core.ui.text

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Text a ViewModel can produce without holding a [Context]; resolution is deferred to the UI.
 * This is what lets state and alerts reference `R.string.*` from outside the composition.
 */
sealed interface UiText {

    fun resolve(context: Context): String

    data class Resource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {

        override fun resolve(context: Context): String = context.getString(id, *args.toTypedArray())
    }

    /**
     * A quantity string, resolved through `getQuantityString`.
     *
     * [quantity] picks the plural form and is **not** passed to the format arguments — a string
     * that shows the number needs it in [args] as well, exactly as the platform API requires.
     */
    data class Plural(
        @param:PluralsRes val id: Int,
        val quantity: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {

        override fun resolve(context: Context): String =
            context.resources.getQuantityString(id, quantity, *args.toTypedArray())
    }

    data class Literal(val value: String) : UiText {

        override fun resolve(context: Context): String = value
    }

    data object Empty : UiText {

        override fun resolve(context: Context): String = ""
    }
}

@Composable
fun UiText.resolve(): String = resolve(LocalContext.current)

fun @receiver:StringRes Int.toUiText(vararg args: Any): UiText =
    UiText.Resource(id = this, args = args.toList())

fun String.toUiText(): UiText = UiText.Literal(this)

/**
 * Deliberately not another `toUiText` overload: `R.string.x.toUiText(count)` would then resolve to
 * the plural one — a non-vararg parameter wins over a vararg — and a string resource would be read
 * as a plural at runtime. The name is longer; the trap is gone.
 */
fun @receiver:PluralsRes Int.toPluralUiText(quantity: Int, vararg args: Any): UiText =
    UiText.Plural(id = this, quantity = quantity, args = args.toList())
