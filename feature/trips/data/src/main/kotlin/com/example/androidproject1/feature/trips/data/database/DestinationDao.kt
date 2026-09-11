package com.example.androidproject1.feature.trips.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {

    @Query("SELECT COUNT(*) FROM destinations")
    suspend fun count(): Int

    @Query("SELECT * FROM destinations ORDER BY name ASC")
    fun observeDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE id = :destinationId")
    suspend fun getDestination(destinationId: String): DestinationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(destinations: List<DestinationEntity>)
}
