package com.example.androidproject1.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * A string a ViewModel can produce without holding a [Context] — resolution is deferred to the UI.
 * This is what lets state and alerts reference `R.string.*` from outside the composition.
 */
sealed interface AppString {

    fun getString(context: Context): String

    data class Resource(
        @param:StringRes val resource: Int,
        val args: List<Any> = emptyList(),
    ) : AppString {

        override fun getString(context: Context): String =
            context.getString(resource, *args.toTypedArray())
    }

    data class Raw(val value: String) : AppString {

        override fun getString(context: Context): String = value
    }

    data object Empty : AppString {

        override fun getString(context: Context): String = ""
    }
}

@Composable
fun AppString.getComposableString(): String = getString(LocalContext.current)

fun @receiver:StringRes Int.toText(vararg args: Any): AppString =
    AppString.Resource(resource = this, args = args.toList())

fun String.toText(): AppString = AppString.Raw(this)
