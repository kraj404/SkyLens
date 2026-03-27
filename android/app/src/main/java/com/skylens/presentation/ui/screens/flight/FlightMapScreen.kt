package com.skylens.presentation.ui.screens.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skylens.presentation.ui.components.MapLibreMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightMapScreen(
    departure: String,
    arrival: String,
    onNavigateBack: () -> Unit,
    onNavigateToAskAI: () -> Unit = {},
    onNavigateToLandmarkDetail: (String, List<String>) -> Unit = { _, _ -> },
    viewModel: FlightMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLandmarkSheet by remember { mutableStateOf(false) }

    // Initialize route (but don't start tracking)
    LaunchedEffect(departure, arrival) {
        android.util.Log.d("FlightMapScreen", "Initializing route: $departure -> $arrival")
        viewModel.initializeRoute(departure, arrival)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$departure → $arrival") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAskAI) {
                        Icon(Icons.Default.Info, contentDescription = "Ask AI")
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
            // Map with route and landmarks
            MapLibreMapView(
                modifier = Modifier.fillMaxSize(),
                currentPosition = uiState.currentPosition,
                landmarks = uiState.allRouteLandmarks, // Show ALL route landmarks
                routePoints = uiState.routePoints,
                onLandmarkClick = { landmark ->
                    android.util.Log.d("FlightMapScreen", "Landmark clicked: ${landmark.name}")
                    val landmarkIds = uiState.allRouteLandmarks.map { it.id }
                    onNavigateToLandmarkDetail(landmark.id, landmarkIds)
                }
            )

            // Flight Status Bar
            uiState.currentPosition?.let { position ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Alt: ${position.altitude ?: 0} ft",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Speed: ${position.speed ?: 0} km/h",
                                style = MaterialTheme.typography.bodySmall
                            )
                            uiState.gpsAccuracy?.let { accuracy ->
                                Text(
                                    text = "GPS: ±${accuracy.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (uiState.isTracking) "● TRACKING" else "○ STOPPED",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Landmark Count Badge (clickable) - shows total route landmarks
            if (uiState.allRouteLandmarks.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        android.util.Log.d("FlightMapScreen", "POI badge clicked!")
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
                        text = "🏔️ ${uiState.allRouteLandmarks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // GPS Mode Toggle (only show when not tracking)
            if (!uiState.isTracking) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .padding(top = 160.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isRealGps) "📡 Real GPS" else "🎮 Mock",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = uiState.isRealGps,
                            onCheckedChange = { viewModel.setGpsMode(it) }
                        )
                    }
                }
            }

            // Start/Stop Flight Button
            FloatingActionButton(
                onClick = {
                    if (uiState.isTracking) {
                        viewModel.stopFlight()
                    } else {
                        viewModel.startFlightTracking(departure, arrival)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = if (uiState.isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (uiState.isTracking) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isTracking) "Stop Flight" else "Start Flight"
                )
            }

            // AI Narrator Box
            uiState.aiNarration?.let { narration ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🤖 AI Narrator",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = narration,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Prediction Alert
            uiState.predictedLandmarks.firstOrNull()?.let { predicted ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .padding(top = 80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🔔 Upcoming",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = predicted.landmark.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Visible in ${predicted.visibleInMinutes} min",
                            style = MaterialTheme.typography.bodySmall
                        )
                        predicted.aiPreview?.let { preview ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
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
                    text = "Route Landmarks (${uiState.allRouteLandmarks.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(uiState.allRouteLandmarks) { landmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    showLandmarkSheet = false
                                    val landmarkIds = uiState.allRouteLandmarks.map { it.id }
                                    onNavigateToLandmarkDetail(landmark.id, landmarkIds)
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
