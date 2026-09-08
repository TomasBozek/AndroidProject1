package com.example.androidproject1.core.ui.text

import android.content.Context
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
