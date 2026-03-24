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
    viewModel: TripHistoryViewModel = hiltViewModel()
) {
    val tripWithSummary = viewModel.uiState.collectAsState().value.trips.find { it.trip.id == tripId }
    val trip = tripWithSummary?.trip
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var currentPositionIndex by remember { mutableStateOf(0) }
    var replayPositions by remember { mutableStateOf<List<FlightPosition>>(emptyList()) }

    // Load trip events and generate replay positions
    LaunchedEffect(tripId) {
        trip?.let {
            // TODO: Load actual trip events from database
            // For now, generate mock replay data from route
            replayPositions = generateReplayPositions(
                it.departureAirport,
                it.arrivalAirport,
                100 // 100 positions for smooth animation
            )
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
            if (trip != null && replayPositions.isNotEmpty()) {
                // Map showing replay
                MapLibreMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentPosition = replayPositions.getOrNull(currentPositionIndex),
                    landmarks = emptyList(), // TODO: Load landmarks from trip events
                    routePoints = replayPositions.map { Pair(it.latitude, it.longitude) }
                )

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
}

// Helper function to generate replay positions from route
private fun generateReplayPositions(
    departure: String,
    arrival: String,
    numPositions: Int
): List<FlightPosition> {
    // Mock positions for demo - in production, load from trip_events table
    // This creates a simple interpolated route

    // Example coordinates (would come from airports table)
    val mockRoutes = mapOf(
        "LAX-NRT" to Pair(
            FlightPosition(33.9416, -118.4085, 0, 0f, 0f, 10f, 0L),
            FlightPosition(35.7647, 140.3864, 10668, 850f, 270f, 10f, 0L) // 35000ft = 10668m
        ),
        "DTW-BLR" to Pair(
            FlightPosition(42.2124, -83.3534, 0, 0f, 0f, 10f, 0L),
            FlightPosition(13.1986, 77.7066, 11582, 900f, 90f, 10f, 0L) // 38000ft = 11582m
        )
    )

    val routeKey = "$departure-$arrival"
    val (start, end) = mockRoutes[routeKey] ?: return emptyList()

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
