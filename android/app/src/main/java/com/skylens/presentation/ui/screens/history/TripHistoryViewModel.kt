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
}
