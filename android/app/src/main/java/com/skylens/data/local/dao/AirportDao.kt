package com.skylens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skylens.data.local.entities.AirportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirport(airport: AirportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAirports(airports: List<AirportEntity>)

    @Query("SELECT * FROM airports WHERE iata_code = :iataCode LIMIT 1")
    suspend fun getAirportByCode(iataCode: String): AirportEntity?

    @Query("""
        SELECT * FROM airports
        WHERE name LIKE '%' || :searchQuery || '%'
        OR city LIKE '%' || :searchQuery || '%'
        OR iata_code LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
        LIMIT :limit
    """)
    fun searchAirports(searchQuery: String, limit: Int = 20): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airports ORDER BY name ASC")
    fun getAllAirports(): Flow<List<AirportEntity>>

    @Query("SELECT COUNT(*) FROM airports")
    suspend fun getAirportCount(): Int
}
