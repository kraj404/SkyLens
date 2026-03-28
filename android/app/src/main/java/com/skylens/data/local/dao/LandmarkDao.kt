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

    @Query("UPDATE landmarks SET ai_story = :aiStory WHERE id = :landmarkId")
    suspend fun updateLandmarkStory(landmarkId: String, aiStory: String)

    @Query("UPDATE landmarks SET photo_urls = :photoUrls WHERE id = :landmarkId")
    suspend fun updateLandmarkPhoto(landmarkId: String, photoUrls: String?)

    @Query("UPDATE landmarks SET photo_files = :photoFiles WHERE id = :landmarkId")
    suspend fun updateLandmarkPhotoFiles(landmarkId: String, photoFiles: String?)

    @Query("UPDATE landmarks SET general_fact = :generalFact WHERE id = :landmarkId")
    suspend fun updateLandmarkGeneralFact(landmarkId: String, generalFact: String)

    @Query("UPDATE landmarks SET historical_fact = :historicalFact WHERE id = :landmarkId")
    suspend fun updateLandmarkHistoricalFact(landmarkId: String, historicalFact: String)

    @Query("SELECT * FROM landmarks WHERE id = :landmarkId LIMIT 1")
    suspend fun getLandmarkById(landmarkId: String): LandmarkEntity?

    @Query("SELECT * FROM landmarks")
    suspend fun getAllLandmarksSync(): List<LandmarkEntity>

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
