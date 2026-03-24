package com.skylens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skylens.data.local.entities.LandmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmark(landmark: LandmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLandmarks(landmarks: List<LandmarkEntity>)

    @Query("SELECT * FROM landmarks WHERE id = :landmarkId LIMIT 1")
    suspend fun getLandmarkById(landmarkId: String): LandmarkEntity?

    @Query("""
        SELECT * FROM landmarks
        WHERE name LIKE '%' || :searchQuery || '%'
        ORDER BY importance_score DESC
        LIMIT :limit
    """)
    fun searchLandmarks(searchQuery: String, limit: Int = 50): Flow<List<LandmarkEntity>>

    @Query("""
        SELECT * FROM landmarks
        ORDER BY importance_score DESC
        LIMIT :limit
    """)
    fun getTopLandmarks(limit: Int = 1000): Flow<List<LandmarkEntity>>

    // Simplified distance query for offline mode
    // Note: This is approximate, not true geodesic distance
    @Query("""
        SELECT * FROM landmarks
        WHERE (
            (latitude - :lat) * (latitude - :lat) +
            (longitude - :lon) * (longitude - :lon)
        ) < :radiusSquared
        ORDER BY importance_score DESC
        LIMIT :limit
    """)
    suspend fun getLandmarksNearby(
        lat: Double,
        lon: Double,
        radiusSquared: Double,
        limit: Int = 100
    ): List<LandmarkEntity>

    @Query("SELECT COUNT(*) FROM landmarks")
    suspend fun getLandmarkCount(): Int

    @Query("DELETE FROM landmarks")
    suspend fun deleteAllLandmarks()
}
