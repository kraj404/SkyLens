package com.skylens.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.ai.ClaudeApiClient
import com.skylens.data.repository.LandmarkRepository
import com.skylens.data.repository.TripRepository
import com.skylens.domain.model.Trip
import com.skylens.geo.GeoCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripHistoryUiState(
    val trips: List<TripWithSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TripWithSummary(
    val trip: Trip,
    val aiSummary: String?,
    val landmarkCount: Int,
    val distanceKm: Double,
    val durationHours: Double
)

@HiltViewModel
class TripHistoryViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val landmarkRepository: LandmarkRepository,
    private val claudeApiClient: ClaudeApiClient,
    private val geoCalculator: GeoCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripHistoryUiState())
    val uiState: StateFlow<TripHistoryUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            tripRepository.getAllTrips().collect { trips ->
                val tripsWithSummaries = trips.map { trip ->
                    processTrip(trip)
                }

                _uiState.update {
                    it.copy(
                        trips = tripsWithSummaries,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun processTrip(trip: Trip): TripWithSummary {
        val events = tripRepository.getTripById(trip.id)?.events ?: emptyList()

        // Calculate stats
        val landmarkCount = events.distinctBy { it.landmarkId }.size

        // Get departure/arrival coordinates (simplified)
        val distanceKm = 0.0 // TODO: Calculate from route

        val durationHours = if (trip.startTime != null && trip.endTime != null) {
            (trip.endTime - trip.startTime) / (1000.0 * 60 * 60)
        } else 0.0

        // Generate AI summary if not exists
        val aiSummary = generateTripSummary(trip, events, distanceKm, durationHours)

        return TripWithSummary(
            trip = trip,
            aiSummary = aiSummary,
            landmarkCount = landmarkCount,
            distanceKm = distanceKm,
            durationHours = durationHours
        )
    }

    private suspend fun generateTripSummary(
        trip: Trip,
        events: List<com.skylens.domain.model.TripEvent>,
        distanceKm: Double,
        durationHours: Double
    ): String? {
        // Get landmark names
        val landmarkNames = events.mapNotNull { event ->
            landmarkRepository.getLandmarkById(event.landmarkId)?.name
        }.distinct()

        if (landmarkNames.isEmpty()) return null

        val result = claudeApiClient.generateTripSummary(
            departureAirport = trip.departureAirport,
            arrivalAirport = trip.arrivalAirport,
            landmarksSeen = landmarkNames,
            durationHours = durationHours,
            distanceKm = distanceKm
        )

        return result.getOrNull()
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
        }
    }

    /**
     * Export trip as GeoJSON file
     * Returns GeoJSON string or null if trip not found
     */
    suspend fun exportTripAsGeoJson(tripId: String): String? {
        return tripRepository.exportTripAsGeoJson(tripId)
    }

    suspend fun getRouteLandmarks(departure: String, arrival: String): List<com.skylens.domain.model.Landmark> {
        // Use same logic as FlightMapViewModel.initializeRoute()
        val mockCoords = mapOf(
            "LAX" to Pair(33.9416, -118.4085),
            "NRT" to Pair(35.7647, 140.3864),
            "DTW" to Pair(42.2124, -83.3534),
            "BLR" to Pair(13.1986, 77.7066),
            "FRA" to Pair(50.0379, 8.5622),
            "LHR" to Pair(51.4700, -0.4543),
            "HYD" to Pair(17.2403, 78.4294)
        )

        val depCoords = mockCoords[departure] ?: return emptyList()
        val arrCoords = mockCoords[arrival] ?: return emptyList()

        // Load all landmarks and filter by route corridor
        val allLandmarks = landmarkRepository.getAllLandmarks()
        val corridorWidthKm = 300.0

        // Simple straight line for filtering (same as FlightMapViewModel)
        return allLandmarks.filter { landmark ->
            val distToDep = geoCalculator.haversineDistance(
                depCoords.first, depCoords.second,
                landmark.latitude, landmark.longitude
            )
            val distToArr = geoCalculator.haversineDistance(
                arrCoords.first, arrCoords.second,
                landmark.latitude, landmark.longitude
            )
            // Landmark is within corridor if it's close to either endpoint or along the path
            distToDep < corridorWidthKm || distToArr < corridorWidthKm ||
            (distToDep + distToArr) < (geoCalculator.haversineDistance(
                depCoords.first, depCoords.second,
                arrCoords.first, arrCoords.second
            ) + corridorWidthKm)
        }
    }
}
