package com.skylens.data.repository

import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.entities.LandmarkEntity
import com.skylens.data.remote.WikimediaApiClient
import com.skylens.domain.model.Landmark
import com.skylens.domain.model.LandmarkType
import com.skylens.geo.GeoCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LandmarkRepository @Inject constructor(
    private val landmarkDao: LandmarkDao,
    private val geoCalculator: GeoCalculator,
    private val wikimediaApiClient: WikimediaApiClient
) {

    fun searchLandmarks(query: String, limit: Int = 50): Flow<List<Landmark>> {
        return landmarkDao.searchLandmarks(query, limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getTopLandmarks(limit: Int = 1000): Flow<List<Landmark>> {
        return landmarkDao.getTopLandmarks(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getLandmarksNearPosition(
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        limit: Int = 100
    ): List<Landmark> {
        val radiusSquared = geoCalculator.approximateRadiusSquared(radiusKm)
        val entities = landmarkDao.getLandmarksNearby(latitude, longitude, radiusSquared, limit)

        // Filter with precise distance calculation
        return entities.mapNotNull { entity ->
            val distance = geoCalculator.haversineDistance(
                latitude, longitude,
                entity.latitude, entity.longitude
            )
            if (distance <= radiusKm) {
                entity.toDomainModel()
            } else null
        }.sortedBy { landmark ->
            geoCalculator.haversineDistance(
                latitude, longitude,
                landmark.latitude, landmark.longitude
            )
        }
    }

    suspend fun getLandmarkById(id: String): Landmark? {
        return landmarkDao.getLandmarkById(id)?.toDomainModel()
    }

    suspend fun insertLandmarks(landmarks: List<Landmark>) {
        landmarkDao.insertAllLandmarks(landmarks.map { it.toEntity() })
    }

    suspend fun getLandmarkCount(): Int {
        return landmarkDao.getLandmarkCount()
    }

    suspend fun getAllLandmarks(): List<Landmark> {
        return landmarkDao.getTopLandmarks(10000).map { entities ->
            entities.map { it.toDomainModel() }
        }.first()
    }

    /**
     * Fetch and cache photo for a landmark from Wikimedia
     */
    suspend fun fetchAndCachePhoto(landmarkId: String): String? {
        val landmark = getLandmarkById(landmarkId) ?: return null

        // Skip if already has photo
        if (landmark.photoUrls.isNotEmpty()) {
            return landmark.photoUrls.first()
        }

        // Fetch from Wikimedia
        val photoUrl = wikimediaApiClient.getPhotoUrl(landmark.name) ?: return null

        // Update database with photo URL
        val photosJson = JSONArray(listOf(photoUrl)).toString()
        landmarkDao.updateLandmarkPhoto(landmarkId, photosJson)

        return photoUrl
    }

    /**
     * Batch fetch photos for multiple landmarks (background task)
     */
    suspend fun batchFetchPhotos(landmarks: List<Landmark>) {
        landmarks.forEach { landmark ->
            if (landmark.photoUrls.isEmpty()) {
                try {
                    fetchAndCachePhoto(landmark.id)
                } catch (e: Exception) {
                    android.util.Log.e("LandmarkRepo", "Failed to fetch photo for ${landmark.name}", e)
                }
            }
        }
    }

    // Extension functions for mapping
    private fun LandmarkEntity.toDomainModel(): Landmark {
        val photos = photoUrls?.let {
            try {
                val jsonArray = JSONArray(it)
                List(jsonArray.length()) { i -> jsonArray.getString(i) }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        val files = photoFiles?.let {
            try {
                val jsonArray = JSONArray(it)
                List(jsonArray.length()) { i -> jsonArray.getString(i) }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Landmark(
            id = id,
            name = name,
            type = LandmarkType.fromString(type),
            latitude = latitude,
            longitude = longitude,
            elevationM = elevationM,
            importanceScore = importanceScore,
            wikiId = wikiId,
            country = country,
            aiStory = aiStory,
            photoUrls = photos,
            photoFiles = files,
            generalFact = generalFact,
            historicalFact = historicalFact
        )
    }

    private fun Landmark.toEntity(): LandmarkEntity {
        val photosJson = if (photoUrls.isNotEmpty()) {
            JSONArray(photoUrls).toString()
        } else null

        val filesJson = if (photoFiles.isNotEmpty()) {
            JSONArray(photoFiles).toString()
        } else null

        return LandmarkEntity(
            id = id,
            name = name,
            type = type.name,
            latitude = latitude,
            longitude = longitude,
            elevationM = elevationM,
            importanceScore = importanceScore,
            wikiId = wikiId,
            country = country,
            aiStory = aiStory,
            photoUrls = photosJson,
            photoFiles = filesJson,
            generalFact = generalFact,
            historicalFact = historicalFact
        )
    }
}
