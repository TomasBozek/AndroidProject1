package com.example.androidproject1

/**
 * What an incoming link asks for, before anything has been looked up.
 *
 * `:app` is the only module that can hold this: a link names a screen in a feature, and turning one
 * into a back stack means knowing about several of them at once. Keeping it here is what stops a
 * feature having to know the app's URL scheme.
 */
sealed interface DeepLink {

    data class Product(val productId: String) : DeepLink
}

/**
 * The app's own URL scheme, parsed.
 *
 * A plain string in, so this is an ordinary JVM function with no `android.net.Uri` to stub: the
 * scheme itself is checked in `MainActivity`, next to the string resource the manifest's
 * intent filter uses, and by the time a URI reaches here the system has already matched it.
 */
object DeepLinks {

    /** `<scheme>://product/<id>`. */
    private const val HOST_PRODUCT = "product"

    fun parse(uri: String?): DeepLink? {
        val path = uri?.substringAfter(SCHEME_SEPARATOR, missingDelimiterValue = "")
            ?.split("/")
            ?.filter { it.isNotBlank() }
            ?: return null

        return when {
            path.size == 2 && path[0] == HOST_PRODUCT -> DeepLink.Product(productId = path[1])
            else -> null
        }
    }

    private const val SCHEME_SEPARATOR = "://"
}
