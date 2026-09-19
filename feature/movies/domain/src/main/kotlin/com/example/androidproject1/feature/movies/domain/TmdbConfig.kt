package com.example.androidproject1.feature.movies.domain

/**
 * Where TMDB is and how to be let in (D80). Bound by `:app`, which owns `BuildConfig`; the key is
 * `""` on a clone with no `tmdb.apiKey` in `local.properties`, and such a build sees fixtures only.
 */
data class TmdbConfig(
    val apiBaseUrl: String,
    val imageBaseUrl: String,
    val apiKey: String,
) {

    val hasKey: Boolean get() = apiKey.isNotBlank()
}
