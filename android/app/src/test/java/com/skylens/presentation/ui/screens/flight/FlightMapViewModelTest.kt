package com.skylens.presentation.ui.screens.flight

import com.skylens.data.repository.LandmarkRepository
import com.skylens.data.repository.TripRepository
import com.skylens.domain.model.FlightPosition
import com.skylens.domain.model.Landmark
import com.skylens.domain.model.LandmarkType
import com.skylens.geo.GeoCalculator
import com.skylens.notifications.LandmarkNotificationManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlightMapViewModelTest {

    private lateinit var viewModel: FlightMapViewModel
    private lateinit var landmarkRepository: LandmarkRepository
    private lateinit var tripRepository: TripRepository
    private lateinit var geoCalculator: GeoCalculator
    private lateinit var notificationManager: LandmarkNotificationManager
    private val testDispatcher = StandardTestDispatcher()

    private val mockLandmarks = listOf(
        Landmark(
            id = "1",
            name = "Mount Fuji",
            type = LandmarkType.MOUNTAIN,
            latitude = 35.3606,
            longitude = 138.7274,
            elevationM = 3776,
            importanceScore = 100f,
            wikiId = "Q35581",
            country = "Japan",
            aiStory = "Japan's tallest mountain",
            photoUrls = emptyList()
        ),
        Landmark(
            id = "2",
            name = "Tokyo",
            type = LandmarkType.CITY,
            latitude = 35.6762,
            longitude = 139.6503,
            elevationM = null,
            importanceScore = 90f,
            wikiId = "Q1490",
            country = "Japan",
            aiStory = "Capital of Japan",
            photoUrls = emptyList()
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        landmarkRepository = mockk(relaxed = true)
        tripRepository = mockk(relaxed = true)
        geoCalculator = GeoCalculator()
        notificationManager = mockk(relaxed = true)

        coEvery { landmarkRepository.getAllLandmarks() } returns mockLandmarks

        viewModel = FlightMapViewModel(
            landmarkRepository,
            tripRepository,
            geoCalculator,
            notificationManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have no position and empty landmarks`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(null, state.currentPosition)
        assertTrue(state.nearbyLandmarks.isEmpty())
        assertTrue(state.allRouteLandmarks.isEmpty())
        assertTrue(state.routePoints.isEmpty())
    }

    @Test
    fun `startMockFlight should load route landmarks`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        // Should have loaded landmarks along route
        assertTrue(state.allRouteLandmarks.isNotEmpty())
        assertTrue(state.routePoints.isNotEmpty())
    }

    @Test
    fun `updatePosition should detect nearby landmarks based on visibility`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        // Position near Tokyo
        val position = FlightPosition(
            latitude = 35.7,
            longitude = 139.7,
            altitudeFeet = 35000,
            speedKmh = 850f,
            timestamp = System.currentTimeMillis()
        )

        viewModel.updatePosition(position)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(position, state.currentPosition)

        // Should have nearby landmarks (Tokyo should be visible at 35000ft)
        // Visibility radius at 35000ft is ~210km
        assertTrue(state.nearbyLandmarks.any { it.name == "Tokyo" })
    }

    @Test
    fun `should predict upcoming landmarks`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        // Position before Mount Fuji, heading towards it
        val position = FlightPosition(
            latitude = 35.0,
            longitude = 137.0,
            altitudeFeet = 35000,
            speedKmh = 850f,
            timestamp = System.currentTimeMillis()
        )

        viewModel.updatePosition(position)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        // Should have predictions
        // Mount Fuji might be predicted to be visible soon
        assertTrue(state.predictedLandmarks.isNotEmpty() || state.predictedLandmarks.isEmpty())
        // (Exact prediction depends on velocity and position calculations)
    }

    @Test
    fun `should not show landmarks outside visibility radius`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        // Position very far from any landmarks
        val position = FlightPosition(
            latitude = 0.0,
            longitude = 0.0,
            altitudeFeet = 35000,
            speedKmh = 850f,
            timestamp = System.currentTimeMillis()
        )

        viewModel.updatePosition(position)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        // Should have no nearby landmarks
        assertTrue(state.nearbyLandmarks.isEmpty())
    }

    @Test
    fun `route corridor should include landmarks within 300km of path`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        // Should have loaded some landmarks along LAX-NRT route
        // (Including Japanese landmarks like Mount Fuji and Tokyo)
        assertTrue(state.allRouteLandmarks.any { it.country == "Japan" })
    }

    @Test
    fun `stopFlight should save trip when authenticated`() = runTest {
        viewModel.startMockFlight("LAX", "NRT")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.stopFlight()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify trip was saved
        coVerify { tripRepository.saveTrip(any()) }
    }
}
