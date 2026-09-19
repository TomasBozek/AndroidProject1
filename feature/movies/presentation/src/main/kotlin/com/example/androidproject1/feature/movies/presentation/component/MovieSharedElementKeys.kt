package com.example.androidproject1.feature.movies.presentation.component

/**
 * The key the movie row and the movie detail share (D76): the poster, built from the id, so a
 * row and the screen it opens name the same picture and no other row does. Not a composable, so
 * not a component — but here rather than in a screen file, because a screen file holds the
 * screen and its previews and nothing else.
 */
fun moviePosterKey(movieId: Int): String = "movie/$movieId/poster"
