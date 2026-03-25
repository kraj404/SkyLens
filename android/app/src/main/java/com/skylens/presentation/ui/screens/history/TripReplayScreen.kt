package com.skylens.presentation.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skylens.domain.model.FlightPosition
import com.skylens.presentation.ui.components.MapLibreMapView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripReplayScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLandmarkDetail: (String) -> Unit = {},
    viewModel: TripHistoryViewModel = hiltViewModel()
) {
    var trip by remember { mutableStateOf<com.skylens.domain.model.Trip?>(null) }
    var landmarks by remember { mutableStateOf<List<com.skylens.domain.model.Landmark>>(emptyList()) }
    var showLandmarkSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var currentPositionIndex by remember { mutableStateOf(0) }
    var replayPositions by remember { mutableStateOf<List<FlightPosition>>(emptyList()) }

    // Load trip directly from repository
    LaunchedEffect(tripId, viewModel) {
        android.util.Log.d("TripReplayScreen", "Loading trip: $tripId")

        // First check cached trips
        val cachedTrip = viewModel.uiState.value.trips.find { it.trip.id == tripId }?.trip

        if (cachedTrip != null) {
            android.util.Log.d("TripReplayScreen", "Found trip in cache: ${cachedTrip.departureAirport} -> ${cachedTrip.arrivalAirport}")
            trip = cachedTrip
            replayPositions = generateReplayPositions(
                cachedTrip.departureAirport,
                cachedTrip.arrivalAirport,
                100
            )
            // Load all route landmarks (same as FlightMapScreen does)
            landmarks = viewModel.getRouteLandmarks(cachedTrip.departureAirport, cachedTrip.arrivalAirport)
            android.util.Log.d("TripReplayScreen", "Loaded ${landmarks.size} landmarks for route")
            isLoading = false
        } else {
            // Wait a bit for trips to load, then check again
            android.util.Log.d("TripReplayScreen", "Trip not in cache, waiting for load...")
            kotlinx.coroutines.delay(500)

            val loadedTrip = viewModel.uiState.value.trips.find { it.trip.id == tripId }?.trip
            if (loadedTrip != null) {
                android.util.Log.d("TripReplayScreen", "Trip loaded: ${loadedTrip.departureAirport} -> ${loadedTrip.arrivalAirport}")
                trip = loadedTrip
                replayPositions = generateReplayPositions(
                    loadedTrip.departureAirport,
                    loadedTrip.arrivalAirport,
                    100
                )
                // Load all route landmarks
                landmarks = viewModel.getRouteLandmarks(loadedTrip.departureAirport, loadedTrip.arrivalAirport)
                android.util.Log.d("TripReplayScreen", "Loaded ${landmarks.size} landmarks for route")
                isLoading = false
            } else {
                android.util.Log.e("TripReplayScreen", "Trip still not found after waiting, going back")
                isLoading = false
                onNavigateBack()
            }
        }
    }

    // Playback animation
    LaunchedEffect(isPlaying, playbackSpeed, currentPositionIndex) {
        if (isPlaying && currentPositionIndex < replayPositions.size - 1) {
            delay((100 / playbackSpeed).toLong())
            currentPositionIndex++
        } else if (currentPositionIndex >= replayPositions.size - 1) {
            isPlaying = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replay: ${trip?.departureAirport ?: ""} → ${trip?.arrivalAirport ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (trip != null && replayPositions.isNotEmpty()) {
                // Map showing replay
                MapLibreMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentPosition = replayPositions.getOrNull(currentPositionIndex),
                    landmarks = landmarks,
                    routePoints = replayPositions.map { Pair(it.latitude, it.longitude) },
                    onLandmarkClick = { landmark ->
                        android.util.Log.d("TripReplayScreen", "Landmark clicked: ${landmark.name}")
                        onNavigateToLandmarkDetail(landmark.id)
                    }
                )

                // Landmark Count Badge (clickable) - shows total route landmarks
                if (landmarks.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            android.util.Log.d("TripReplayScreen", "POI badge clicked!")
                            showLandmarkSheet = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .padding(top = 80.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = "🏔️ ${landmarks.size}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Playback controls at bottom
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Progress indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentPositionIndex + 1} / ${replayPositions.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${(currentPositionIndex.toFloat() / replayPositions.size * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = currentPositionIndex.toFloat() / replayPositions.size.coerceAtLeast(1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current position info
                        replayPositions.getOrNull(currentPositionIndex)?.let { position ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Altitude",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(position.altitude * 3.28084).toInt()} ft", // Convert m to ft
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Speed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${position.speed.toInt()} km/h",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Control buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Restart button
                            OutlinedButton(
                                onClick = {
                                    currentPositionIndex = 0
                                    isPlaying = false
                                }
                            ) {
                                Text("Restart")
                            }

                            // Play/Pause button
                            FilledTonalButton(
                                onClick = { isPlaying = !isPlaying }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) {
                                        androidx.compose.material.icons.Icons.Default.PlayArrow
                                    } else {
                                        androidx.compose.material.icons.Icons.Default.PlayArrow
                                    },
                                    contentDescription = if (isPlaying) "Pause" else "Play"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPlaying) "Pause" else "Play")
                            }

                            // Speed control
                            OutlinedButton(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        1f -> 2f
                                        2f -> 4f
                                        4f -> 8f
                                        else -> 1f
                                    }
                                }
                            ) {
                                Text("${playbackSpeed}x")
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Landmark List Bottom Sheet
    if (showLandmarkSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLandmarkSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Route Landmarks (${landmarks.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(landmarks) { landmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    showLandmarkSheet = false
                                    onNavigateToLandmarkDetail(landmark.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = landmark.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${landmark.type.name} • ${landmark.country ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                landmark.elevationM?.let { elevation ->
                                    Text(
                                        text = "Elevation: ${elevation}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                landmark.aiStory?.let { story ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = story.take(150) + if (story.length > 150) "..." else "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Helper function to generate replay positions from route
private fun generateReplayPositions(
    departure: String,
    arrival: String,
    numPositions: Int
): List<FlightPosition> {
    // Default coordinates for known airports
    val airportCoords = mapOf(
        "LAX" to Pair(33.9416, -118.4085),
        "NRT" to Pair(35.7647, 140.3864),
        "DTW" to Pair(42.2124, -83.3534),
        "BLR" to Pair(13.1986, 77.7066),
        "FRA" to Pair(50.0379, 8.5622),
        "LHR" to Pair(51.4700, -0.4543),
        "HYD" to Pair(17.2403, 78.4294)
    )

    val depCoords = airportCoords[departure] ?: Pair(0.0, 0.0)
    val arrCoords = airportCoords[arrival] ?: Pair(0.0, 0.0)

    val start = FlightPosition(depCoords.first, depCoords.second, 0, 0f, 0f, 10f, 0L)
    val end = FlightPosition(arrCoords.first, arrCoords.second, 10668, 850f, 270f, 10f, 0L)

    return List(numPositions) { i ->
        val fraction = i.toFloat() / (numPositions - 1)

        // Linear interpolation
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lon = start.longitude + (end.longitude - start.longitude) * fraction
        val alt = (start.altitude + (end.altitude - start.altitude) * fraction).toInt()
        val speed = start.speed + (end.speed - start.speed) * fraction
        val heading = start.heading + (end.heading - start.heading) * fraction

        FlightPosition(
            latitude = lat,
            longitude = lon,
            altitude = alt,
            speed = speed,
            heading = heading,
            accuracy = 10f,
            timestamp = System.currentTimeMillis() + (i * 60000L) // 1 min intervals
        )
    }
}
