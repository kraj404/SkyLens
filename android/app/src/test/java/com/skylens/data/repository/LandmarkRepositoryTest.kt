package com.skylens.data.repository

import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.entities.LandmarkEntity
import com.skylens.domain.model.LandmarkType
import com.skylens.geo.GeoCalculator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LandmarkRepositoryTest {

    private lateinit var repository: LandmarkRepository
    private lateinit var landmarkDao: LandmarkDao
    private lateinit var geoCalculator: GeoCalculator

    @Before
    fun setup() {
        landmarkDao = mockk()
        geoCalculator = GeoCalculator()
        repository = LandmarkRepository(landmarkDao, geoCalculator)
    }

    @Test
    fun `getLandmarksNearPosition should return landmarks sorted by distance`() = runTest {
        val latitude = 35.6762
        val longitude = 139.6503
        val radiusKm = 200.0

        val mockEntities = listOf(
            LandmarkEntity(
                id = "1",
                name = "Mount Fuji",
                type = "mountain",
                latitude = 35.3606,
                longitude = 138.7274,
                elevationM = 3776,
                importanceScore = 100f,
                wikiId = "Q35581",
                country = "Japan",
                aiStory = "Japan's tallest peak",
                photoUrls = null
            ),
            LandmarkEntity(
                id = "2",
                name = "Tokyo Tower",
                type = "monument",
                latitude = 35.6586,
                longitude = 139.7454,
                elevationM = 333,
                importanceScore = 80f,
                wikiId = "Q186262",
                country = "Japan",
                aiStory = "Famous landmark",
                photoUrls = null
            )
        )

        coEvery {
            landmarkDao.getLandmarksNearby(any(), any(), any(), any())
        } returns mockEntities

        val result = repository.getLandmarksNearPosition(latitude, longitude, radiusKm)

        // Should return landmarks
        assertTrue(result.isNotEmpty())

        // Should be sorted by distance (Tokyo Tower closer than Mount Fuji)
        assertEquals("Tokyo Tower", result[0].name)
        assertEquals("Mount Fuji", result[1].name)

        // Verify DAO was called with correct approximation
        coVerify {
            landmarkDao.getLandmarksNearby(
                latitude = latitude,
                longitude = longitude,
                radiusSquared = any(),
                limit = 100
            )
        }
    }

    @Test
    fun `getLandmarkById should return landmark when exists`() = runTest {
        val landmarkId = "test-id"
        val mockEntity = LandmarkEntity(
            id = landmarkId,
            name = "Test Landmark",
            type = "city",
            latitude = 40.7128,
            longitude = -74.0060,
            elevationM = null,
            importanceScore = 50f,
            wikiId = null,
            country = "USA",
            aiStory = null,
            photoUrls = null
        )

        coEvery { landmarkDao.getLandmarkById(landmarkId) } returns mockEntity

        val result = repository.getLandmarkById(landmarkId)

        assertNotNull(result)
        assertEquals("Test Landmark", result.name)
        assertEquals(LandmarkType.CITY, result.type)
    }

    @Test
    fun `getLandmarkById should return null when not exists`() = runTest {
        coEvery { landmarkDao.getLandmarkById(any()) } returns null

        val result = repository.getLandmarkById("nonexistent")

        assertEquals(null, result)
    }

    @Test
    fun `landmarks with null photo urls should map to empty list`() = runTest {
        val mockEntity = LandmarkEntity(
            id = "1",
            name = "Test",
            type = "city",
            latitude = 0.0,
            longitude = 0.0,
            elevationM = null,
            importanceScore = 0f,
            wikiId = null,
            country = null,
            aiStory = null,
            photoUrls = null
        )

        coEvery { landmarkDao.getLandmarkById("1") } returns mockEntity

        val result = repository.getLandmarkById("1")

        assertNotNull(result)
        assertTrue(result.photoUrls.isEmpty())
    }

    @Test
    fun `landmarks with JSON photo urls should parse correctly`() = runTest {
        val mockEntity = LandmarkEntity(
            id = "1",
            name = "Test",
            type = "city",
            latitude = 0.0,
            longitude = 0.0,
            elevationM = null,
            importanceScore = 0f,
            wikiId = null,
            country = null,
            aiStory = null,
            photoUrls = """["https://example.com/photo1.jpg","https://example.com/photo2.jpg"]"""
        )

        coEvery { landmarkDao.getLandmarkById("1") } returns mockEntity

        val result = repository.getLandmarkById("1")

        assertNotNull(result)
        assertEquals(2, result.photoUrls.size)
        assertEquals("https://example.com/photo1.jpg", result.photoUrls[0])
    }

    @Test
    fun `getLandmarksNearPosition should filter landmarks outside radius`() = runTest {
        val latitude = 0.0
        val longitude = 0.0
        val radiusKm = 50.0

        // One landmark inside, one outside radius
        val mockEntities = listOf(
            LandmarkEntity(
                id = "1",
                name = "Near",
                type = "city",
                latitude = 0.1,  // ~11km away
                longitude = 0.1,
                elevationM = null,
                importanceScore = 50f,
                wikiId = null,
                country = null,
                aiStory = null,
                photoUrls = null
            ),
            LandmarkEntity(
                id = "2",
                name = "Far",
                type = "city",
                latitude = 1.0,  // ~111km away
                longitude = 1.0,
                elevationM = null,
                importanceScore = 50f,
                wikiId = null,
                country = null,
                aiStory = null,
                photoUrls = null
            )
        )

        coEvery {
            landmarkDao.getLandmarksNearby(any(), any(), any(), any())
        } returns mockEntities

        val result = repository.getLandmarksNearPosition(latitude, longitude, radiusKm)

        // Should only return the near landmark
        assertEquals(1, result.size)
        assertEquals("Near", result[0].name)
    }
}
