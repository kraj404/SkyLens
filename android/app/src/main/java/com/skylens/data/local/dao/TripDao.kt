package com.skylens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skylens.data.local.entities.TripEntity
import com.skylens.data.local.entities.TripEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripEvent(event: TripEventEntity)

    @Query("SELECT * FROM trips ORDER BY created_at DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("""
        SELECT * FROM trip_events
        WHERE trip_id = :tripId
        ORDER BY event_time ASC
    """)
    suspend fun getTripEvents(tripId: String): List<TripEventEntity>

    @Query("SELECT * FROM trips WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTripsByUser(userId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE departure_airport = :departure AND arrival_airport = :arrival LIMIT 1")
    suspend fun getTripByRoute(departure: String, arrival: String): TripEntity?

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)
}
