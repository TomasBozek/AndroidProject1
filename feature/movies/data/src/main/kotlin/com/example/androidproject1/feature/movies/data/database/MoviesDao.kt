package com.example.androidproject1.feature.movies.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MoviesDao {

    @Query("SELECT * FROM movies WHERE page = :page ORDER BY position ASC")
    fun observePage(page: Int): Flow<List<MovieEntity>>

    /** `null` until the page has been written at least once. */
    @Query("SELECT * FROM movie_pages WHERE page = :page")
    fun observePageMarker(page: Int): Flow<MoviePageEntity?>

    @Query("SELECT * FROM movies ORDER BY page ASC, position ASC")
    suspend fun allMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun movie(id: Int): MovieEntity?

    @Query("SELECT * FROM movie_details WHERE id = :id")
    suspend fun detail(id: Int): MovieDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageMarker(marker: MoviePageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: MovieDetailEntity)

    @Query("DELETE FROM movies WHERE page = :page")
    suspend fun deletePage(page: Int)

    @Query("DELETE FROM movies")
    suspend fun deleteMovies()

    @Query("DELETE FROM movie_pages")
    suspend fun deletePageMarkers()

    /**
     * One page, replaced as a unit and stamped. A movie that moved from page 2 to page 1 between
     * two fetches is one row with a primary key, so the insert's REPLACE moves it rather than
     * leaving a copy behind on the page it left.
     */
    @Transaction
    suspend fun replacePage(page: Int, movies: List<MovieEntity>, marker: MoviePageEntity) {
        deletePage(page)
        insertMovies(movies)
        insertPageMarker(marker)
    }

    /** The refresh: every row and every marker gone, then page 1 and its marker — one transaction. */
    @Transaction
    suspend fun replaceAll(movies: List<MovieEntity>, marker: MoviePageEntity) {
        deleteMovies()
        deletePageMarkers()
        insertMovies(movies)
        insertPageMarker(marker)
    }
}
