package com.skylens.presentation.ui.screens.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.ai.ClaudeApiClient
import com.skylens.data.repository.AirportRepository
import com.skylens.data.repository.LandmarkRepository
import com.skylens.data.repository.TripRepository
import com.skylens.domain.model.FlightPosition
import com.skylens.domain.model.Landmark
import com.skylens.domain.model.PredictedLandmark
import com.skylens.geo.GeoCalculator
import com.skylens.location.FlightTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.*

data class FlightMapUiState(
    val currentPosition: FlightPosition? = null,
    val nearbyLandmarks: List<Landmark> = emptyList(),
    val allRouteLandmarks: List<Landmark> = emptyList(), // All landmarks along the route
    val predictedLandmarks: List<PredictedLandmark> = emptyList(),
    val aiNarration: String? = null,
    val isTracking: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTripId: String? = null,
    val routePoints: List<Pair<Double, Double>> = emptyList() // Lat/Lon pairs for route line
)

@HiltViewModel
class FlightMapViewModel @Inject constructor(
    private val airportRepository: AirportRepository,
    private val flightTracker: FlightTracker,
    private val landmarkRepository: LandmarkRepository,
    private val tripRepository: TripRepository,
    private val claudeApiClient: ClaudeApiClient,
    private val geoCalculator: GeoCalculator,
    private val notificationManager: com.skylens.notifications.LandmarkNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightMapUiState())
    val uiState: StateFlow<FlightMapUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private var narrationJob: Job? = null
    private var currentTripId: String? = null
    private var mockFlightJob: Job? = null

    /**
     * Initialize route (show landmarks, but don't start tracking)
     */
    fun initializeRoute(departureAirport: String, arrivalAirport: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val mockCoords = mapOf(
                    "LAX" to Pair(33.9416, -118.4085),
                    "NRT" to Pair(35.7647, 140.3864),
                    "DTW" to Pair(42.2124, -83.3534),
                    "BLR" to Pair(13.1986, 77.7066),
                    "FRA" to Pair(50.0379, 8.5622),
                    "LHR" to Pair(51.4700, -0.4543)
                )

                val depCoords = mockCoords[departureAirport] ?: Pair(33.9416, -118.4085)
                val arrCoords = mockCoords[arrivalAirport] ?: Pair(35.7647, 140.3864)

                // Generate CURVED great circle route (not straight line!)
                val routePoints = calculateGreatCircleRoute(
                    depCoords.first, depCoords.second,
                    arrCoords.first, arrCoords.second,
                    numPoints = 50
                )

                // Load ALL landmarks from database
                val allLandmarks = landmarkRepository.getAllLandmarks()
                android.util.Log.d("FlightMapViewModel", "Loaded ${allLandmarks.size} total landmarks from database")

                // Filter landmarks along route corridor (300km width)
                val corridorWidthKm = 300.0
                val routeLandmarks = allLandmarks.filter { landmark ->
                    routePoints.any { (routeLat, routeLon) ->
                        val distance = geoCalculator.haversineDistance(
                            routeLat, routeLon,
                            landmark.latitude, landmark.longitude
                        )
                        distance < corridorWidthKm
                    }
                }

                // Remove duplicates and filter out countries
                val filteredLandmarks = routeLandmarks
                    .distinctBy { it.id }
                    .filter { landmark ->
                        // Exclude country-level landmarks
                        !landmark.name.equals("India", ignoreCase = true) &&
                        !landmark.name.equals("United States", ignoreCase = true) &&
                        !landmark.name.equals("China", ignoreCase = true) &&
                        (landmark.type != com.skylens.domain.model.LandmarkType.OTHER ||
                        landmark.elevationM != null) // Keep if has elevation (real landmark)
                    }

                android.util.Log.d("FlightMapViewModel", "Route: $departureAirport -> $arrivalAirport, Points: ${routePoints.size}, Landmarks: ${filteredLandmarks.size}")

                _uiState.update {
                    it.copy(
                        routePoints = routePoints,
                        allRouteLandmarks = filteredLandmarks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("FlightMapViewModel", "Error initializing route", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Calculate great circle route between two points (curved, not straight!)
     */
    private fun calculateGreatCircleRoute(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        numPoints: Int
    ): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()

        // Convert to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Calculate angular distance
        val dLon = lon2Rad - lon1Rad
        val y = sqrt(
            (cos(lat2Rad) * sin(dLon)).pow(2) +
            (cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)).pow(2)
        )
        val x = sin(lat1Rad) * sin(lat2Rad) + cos(lat1Rad) * cos(lat2Rad) * cos(dLon)
        val d = atan2(y, x)

        // Interpolate along great circle
        for (i in 0 until numPoints) {
            val fraction = i.toDouble() / (numPoints - 1)
            val a = sin((1 - fraction) * d) / sin(d)
            val b = sin(fraction * d) / sin(d)

            val x = a * cos(lat1Rad) * cos(lon1Rad) + b * cos(lat2Rad) * cos(lon2Rad)
            val y = a * cos(lat1Rad) * sin(lon1Rad) + b * cos(lat2Rad) * sin(lon2Rad)
            val z = a * sin(lat1Rad) + b * sin(lat2Rad)

            val latRad = atan2(z, sqrt(x * x + y * y))
            val lonRad = atan2(y, x)

            points.add(Pair(Math.toDegrees(latRad), Math.toDegrees(lonRad)))
        }

        return points
    }

    /**
     * Calculate bearing between two points
     */
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        val bearing = atan2(y, x)

        return ((Math.toDegrees(bearing) + 360) % 360).toFloat()
    }

    /**
     * Start flight tracking
     */
    fun startFlight(departureAirport: String, arrivalAirport: String) {
        if (_uiState.value.isTracking) return

        // Create and save new trip
        currentTripId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val trip = com.skylens.domain.model.Trip(
                    id = currentTripId!!,
                    userId = null, // Anonymous for now
                    departureAirport = departureAirport,
                    arrivalAirport = arrivalAirport,
                    routeGeoJson = "", // TODO: Add GeoJSON
                    startTime = now,
                    endTime = null,
                    createdAt = now,
                    events = emptyList()
                )
                tripRepository.saveTrip(trip)
                android.util.Log.d("FlightMapViewModel", "Trip created and saved: $currentTripId")
            } catch (e: Exception) {
                android.util.Log.e("FlightMapViewModel", "Error saving trip", e)
            }
        }

        _uiState.update { it.copy(isTracking = true, currentTripId = currentTripId) }

        // Start location tracking
        trackingJob = viewModelScope.launch {
            flightTracker.trackFlight().collect { position ->
                _uiState.update { it.copy(currentPosition = position) }

                // Update nearby landmarks
                updateNearbyLandmarks(position)

                // Update predictions
                updatePredictions(position)
            }
        }

        // Start AI narrator (updates every 30 seconds)
        startAINarrator()
    }

    /**
     * Stop flight tracking
     */
    fun stopFlight() {
        trackingJob?.cancel()
        narrationJob?.cancel()
        mockFlightJob?.cancel()
        _uiState.update { it.copy(isTracking = false) }

        // Finalize trip
        viewModelScope.launch {
            currentTripId?.let { tripId ->
                tripRepository.getTripById(tripId)?.let { trip ->
                    val updatedTrip = trip.copy(endTime = System.currentTimeMillis())
                    tripRepository.saveTrip(updatedTrip)
                }
            }
        }
    }

    private suspend fun updateNearbyLandmarks(position: FlightPosition) {
        val altitude = position.altitude ?: 35000
        val visibilityRadius = geoCalculator.visibilityRadiusKm(altitude)

        val landmarks = landmarkRepository.getLandmarksNearPosition(
            position.latitude,
            position.longitude,
            visibilityRadius,
            limit = 50
        )

        // Check for newly visible landmarks and send notifications
        val previousLandmarks = _uiState.value.nearbyLandmarks
        val newlyVisible = landmarks.filter { landmark ->
            previousLandmarks.none { it.id == landmark.id }
        }

        newlyVisible.take(1).forEach { landmark ->
            notificationManager.showLandmarkNowVisibleNotification(landmark)
        }

        _uiState.update { it.copy(nearbyLandmarks = landmarks) }

        // Log landmark sightings
        landmarks.take(5).forEach { landmark ->
            currentTripId?.let { tripId ->
                val distance = geoCalculator.haversineDistance(
                    position.latitude, position.longitude,
                    landmark.latitude, landmark.longitude
                )
                tripRepository.addTripEvent(tripId, landmark.id, distance.toFloat())
            }
        }
    }

    private suspend fun updatePredictions(position: FlightPosition) {
        if (position.speed == null || position.heading == null) return

        val predictions = mutableListOf<PredictedLandmark>()
        val altitude = position.altitude ?: 35000
        val visibilityRadius = geoCalculator.visibilityRadiusKm(altitude)

        // Check positions 2, 5, and 10 minutes ahead
        for (minutesAhead in listOf(2, 5, 10)) {
            val futurePos = flightTracker.predictFuturePosition(minutesAhead) ?: continue
            val (futureLat, futureLon) = futurePos

            val nearbyFuture = landmarkRepository.getLandmarksNearPosition(
                futureLat,
                futureLon,
                visibilityRadius,
                limit = 20
            )

            nearbyFuture.forEach { landmark ->
                // Check if not already visible
                val currentDistance = geoCalculator.haversineDistance(
                    position.latitude, position.longitude,
                    landmark.latitude, landmark.longitude
                )

                if (currentDistance > visibilityRadius) {
                    // Generate AI preview for top predictions
                    predictions.add(
                        PredictedLandmark(
                            landmark = landmark,
                            visibleInMinutes = minutesAhead,
                            confidence = 0.8f
                        )
                    )
                }
            }
        }

        _uiState.update {
            it.copy(predictedLandmarks = predictions.distinctBy { p -> p.landmark.id }.take(3))
        }

        // Send notifications for upcoming landmarks (only for 5-minute predictions)
        predictions.filter { it.visibleInMinutes == 5 }.take(1).forEach { predicted ->
            notificationManager.showUpcomingLandmarkNotification(
                predicted.landmark,
                predicted.visibleInMinutes
            )
        }

        // Generate AI context for top prediction
        predictions.firstOrNull()?.let { predicted ->
            generatePredictionAI(predicted)
        }
    }

    private fun generatePredictionAI(predicted: PredictedLandmark) {
        viewModelScope.launch {
            val result = claudeApiClient.generatePredictionContext(
                predicted.landmark.name,
                predicted.landmark.type.name,
                predicted.visibleInMinutes
            )

            if (result.isSuccess) {
                // Update prediction with AI context
                val updated = predicted.copy(aiPreview = result.getOrNull())
                _uiState.update { state ->
                    val updatedPredictions = state.predictedLandmarks.map {
                        if (it.landmark.id == predicted.landmark.id) updated else it
                    }
                    state.copy(predictedLandmarks = updatedPredictions)
                }
            }
        }
    }

    /**
     * Start AI narrator that provides ongoing commentary
     */
    private fun startAINarrator() {
        narrationJob = viewModelScope.launch {
            while (true) {
                delay(30000) // Update every 30 seconds

                val position = _uiState.value.currentPosition ?: continue
                val landmarks = _uiState.value.nearbyLandmarks.take(5)

                if (landmarks.isNotEmpty()) {
                    val result = claudeApiClient.generateFlightNarration(
                        currentRegion = landmarks.first().country ?: "unknown region",
                        nearbyLandmarks = landmarks.map { it.name },
                        altitude = position.altitude ?: 35000
                    )

                    if (result.isSuccess) {
                        _uiState.update { it.copy(aiNarration = result.getOrNull()) }
                    }
                }
            }
        }
    }

    /**
     * Generate AI story for specific landmark
     */
    fun generateStoryForLandmark(landmark: Landmark) {
        viewModelScope.launch {
            if (landmark.aiStory != null) {
                // Already cached
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val result = claudeApiClient.generateLandmarkStory(
                landmarkName = landmark.name,
                landmarkType = landmark.type.name,
                elevation = landmark.elevationM,
                country = landmark.country
            )

            _uiState.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                // Cache the story in database
                val updatedLandmark = landmark.copy(aiStory = result.getOrNull())
                landmarkRepository.insertLandmarks(listOf(updatedLandmark))
            }
        }
    }

    /**
     * Ask AI a question
     */
    fun askAI(question: String) {
        viewModelScope.launch {
            val position = _uiState.value.currentPosition ?: return@launch
            val landmarks = _uiState.value.nearbyLandmarks

            _uiState.update { it.copy(isLoading = true) }

            val result = claudeApiClient.answerLandmarkQuestion(
                question = question,
                currentPosition = "${position.latitude}, ${position.longitude}",
                nearbyLandmarks = landmarks.map { it.name }
            )

            _uiState.update { it.copy(isLoading = false) }

            // Handle response (store in chat history, etc.)
        }
    }

    /**
     * Start a mock flight simulation for testing
     */
    fun startMockFlight(departureCode: String, arrivalCode: String) {
        mockFlightJob?.cancel()

        // Create and save trip
        currentTripId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Fetch airport coordinates
                val departure = airportRepository.getAirportByIataCode(departureCode)
                val arrival = airportRepository.getAirportByIataCode(arrivalCode)

                if (departure == null || arrival == null) {
                    android.util.Log.e("FlightMapViewModel", "Airport not found: $departureCode or $arrivalCode")
                    _uiState.update { it.copy(isLoading = false, error = "Airport not found") }
                    return@launch
                }

                android.util.Log.d("FlightMapViewModel", "Airports loaded: ${departure.name} -> ${arrival.name}")

                // Calculate great circle route (100 points)
                val routePoints = calculateGreatCircleRoute(
                    departure.latitude, departure.longitude,
                    arrival.latitude, arrival.longitude,
                    100
                )

                // Save trip to database
                val trip = com.skylens.domain.model.Trip(
                    id = currentTripId!!,
                    userId = null,
                    departureAirport = departureCode,
                    arrivalAirport = arrivalCode,
                    routeGeoJson = "",
                    startTime = now,
                    endTime = null,
                    createdAt = now,
                    events = emptyList()
                )
                tripRepository.saveTrip(trip)
                android.util.Log.d("FlightMapViewModel", "Trip created and saved: $currentTripId")

                _uiState.update { it.copy(
                    routePoints = routePoints,
                    isLoading = false,
                    isTracking = true,
                    currentTripId = currentTripId
                ) }

                android.util.Log.d("FlightMapViewModel", "Route calculated with ${routePoints.size} points")

                // Load all landmarks once
                val allLandmarks = landmarkRepository.getAllLandmarks()
                android.util.Log.d("FlightMapViewModel", "Loaded ${allLandmarks.size} landmarks from database")

                // Calculate all landmarks along the route (within corridor)
                val corridorWidthKm = 300.0 // 300km corridor width
                val routeLandmarks = allLandmarks.filter { landmark ->
                    routePoints.any { (routeLat, routeLon) ->
                        val distance = geoCalculator.haversineDistance(
                            routeLat, routeLon,
                            landmark.latitude, landmark.longitude
                        )
                        distance < corridorWidthKm
                    }
                }

                android.util.Log.d("FlightMapViewModel", "Found ${routeLandmarks.size} landmarks along route corridor")

                _uiState.update { it.copy(allRouteLandmarks = routeLandmarks) }

                // Simulate flight along route
                mockFlightJob = viewModelScope.launch {
                    routePoints.forEachIndexed { index, (lat, lon) ->
                        val progress = index.toFloat() / routePoints.size

                        // Mock flight position
                        val position = FlightPosition(
                            latitude = lat,
                            longitude = lon,
                            altitude = 35000, // 35,000 feet
                            speed = 850f, // 850 km/h
                            heading = calculateBearing(
                                lat, lon,
                                routePoints.getOrNull(index + 1)?.first ?: lat,
                                routePoints.getOrNull(index + 1)?.second ?: lon
                            ),
                            accuracy = 10f,
                            timestamp = System.currentTimeMillis()
                        )

                        _uiState.update { it.copy(currentPosition = position) }

                        // Find nearby landmarks
                        val visibilityKm = geoCalculator.visibilityRadiusKm(35000)
                        val nearby = allLandmarks.filter { landmark ->
                            val distance = geoCalculator.haversineDistance(
                                lat, lon,
                                landmark.latitude, landmark.longitude
                            )
                            distance < visibilityKm
                        }.sortedBy { landmark ->
                            geoCalculator.haversineDistance(
                                lat, lon,
                                landmark.latitude, landmark.longitude
                            )
                        }.take(10)

                        _uiState.update { it.copy(nearbyLandmarks = nearby) }

                        android.util.Log.d("FlightMapViewModel", "Position $index/${routePoints.size}: $lat, $lon - ${nearby.size} landmarks nearby")

                        delay(1000) // Update every 1 second for smooth animation
                    }

                    // Flight complete - finalize trip
                    currentTripId?.let { tripId ->
                        tripRepository.getTripById(tripId)?.let { trip ->
                            val updatedTrip = trip.copy(endTime = System.currentTimeMillis())
                            tripRepository.saveTrip(updatedTrip)
                            android.util.Log.d("FlightMapViewModel", "Trip finalized: $tripId")
                        }
                    }

                    _uiState.update { it.copy(isTracking = false) }
                    android.util.Log.d("FlightMapViewModel", "Mock flight completed")
                }

            } catch (e: Exception) {
                android.util.Log.e("FlightMapViewModel", "Error starting mock flight", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

}
