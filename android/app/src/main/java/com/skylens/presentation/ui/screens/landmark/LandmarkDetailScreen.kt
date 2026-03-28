package com.skylens.presentation.ui.screens.landmark

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.skylens.domain.model.LandmarkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkDetailScreen(
    landmarkId: String,
    landmarkIds: List<String> = emptyList(), // List of all landmark IDs for navigation
    onNavigateBack: () -> Unit,
    onNavigateToLandmark: (String) -> Unit = {}, // Navigate to another landmark
    viewModel: LandmarkDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Find current landmark index in the list
    val currentIndex = landmarkIds.indexOf(landmarkId)
    val hasPrevious = currentIndex > 0
    val hasNext = currentIndex >= 0 && currentIndex < landmarkIds.size - 1

    // Load landmark details
    androidx.compose.runtime.LaunchedEffect(landmarkId) {
        viewModel.loadLandmark(landmarkId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.landmark?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.landmark?.let { landmark ->
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("Check out ${landmark.name}")
                                    landmark.country?.let { append(" in $it") }
                                    append("!\n\n")
                                    landmark.aiStory?.let { append(it.take(200)) }
                                    append("\n\nShared from SkyLens")
                                }

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share landmark"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Sticky navigation buttons at bottom
            if ((hasPrevious || hasNext) && uiState.landmark != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (hasPrevious) {
                            OutlinedButton(
                                onClick = { onNavigateToLandmark(landmarkIds[currentIndex - 1]) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Previous"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        if (hasNext) {
                            OutlinedButton(
                                onClick = { onNavigateToLandmark(landmarkIds[currentIndex + 1]) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Next"
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading landmark",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                uiState.landmark != null -> {
                    LandmarkDetailContent(
                        landmark = uiState.landmark!!,
                        hasPrevious = hasPrevious,
                        hasNext = hasNext,
                        onPrevious = {
                            if (hasPrevious) {
                                onNavigateToLandmark(landmarkIds[currentIndex - 1])
                            }
                        },
                        onNext = {
                            if (hasNext) {
                                onNavigateToLandmark(landmarkIds[currentIndex + 1])
                            }
                        },
                        onOpenWikipedia = { wikiId ->
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://en.wikipedia.org/wiki/$wikiId")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LandmarkDetailContent(
    landmark: com.skylens.domain.model.Landmark,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onOpenWikipedia: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Map showing landmark location
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(200.dp)
            ) {
                com.skylens.presentation.ui.components.MapLibreMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentPosition = null,
                    landmarks = listOf(landmark),
                    routePoints = emptyList()
                )
            }
        }

        // Facts Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📍 Facts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location (one line)
                    Text(
                        text = "Location: ${landmark.country ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Coordinates (second line)
                    Text(
                        text = "Coordinates: ${String.format("%.2f", landmark.latitude)}°N, ${String.format("%.2f", landmark.longitude)}°E",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // General Fact
                    Text(
                        text = "🗻 General",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = landmark.generalFact ?: "Loading fact...",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Historical Fact
                    Text(
                        text = "📜 Historical",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = landmark.historicalFact ?: "Loading fact...",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
                    )

                    // Type and Elevation info
                    landmark.elevationM?.let { elevation ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${landmark.type.name} • Elevation: ${elevation}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Photo Gallery (at least 2 photos)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "📸 Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val photos = landmark.photoUrls + landmark.photoFiles.map { "file://$it" }
                if (photos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(photos) { photoUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Photo of ${landmark.name}",
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            text = "No photos available yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // AI Story (if available)
        landmark.aiStory?.let { story ->
            item {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🤖",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Story",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = story,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
                        )
                    }
                }
            }
        }

        // Wikipedia Link
        landmark.wikiId?.let { wikiId ->
            item {
                OutlinedButton(
                    onClick = { onOpenWikipedia(wikiId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = android.R.drawable.ic_menu_info_details
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Read more on Wikipedia")
                }
            }
        }
    }
}
