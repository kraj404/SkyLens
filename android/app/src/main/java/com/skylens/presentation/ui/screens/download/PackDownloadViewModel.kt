package com.skylens.presentation.ui.screens.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.data.repository.OfflinePackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PackDownloadUiState {
    object Initial : PackDownloadUiState()
    object Checking : PackDownloadUiState()
    data class ReadyToDownload(
        val packSizeMB: Int,
        val landmarkCount: Int,
        val tileCount: Int
    ) : PackDownloadUiState()
    data class Downloading(
        val progress: Float,
        val currentTask: String
    ) : PackDownloadUiState()
    object Completed : PackDownloadUiState()
    data class Error(val message: String) : PackDownloadUiState()
}

@HiltViewModel
class PackDownloadViewModel @Inject constructor(
    private val offlinePackRepository: OfflinePackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PackDownloadUiState>(PackDownloadUiState.Initial)
    val uiState: StateFlow<PackDownloadUiState> = _uiState.asStateFlow()

    private var currentRoute: Pair<String, String>? = null

    fun startDownload(departure: String, arrival: String) {
        currentRoute = departure to arrival
        viewModelScope.launch {
            _uiState.value = PackDownloadUiState.Checking

            try {
                // Check if pack already exists
                val existingPack = offlinePackRepository.getOfflinePack(departure, arrival)

                if (existingPack != null) {
                    // Pack already exists, skip to completed
                    _uiState.value = PackDownloadUiState.Completed
                    return@launch
                }

                // Simulate checking pack info from API
                // Get actual landmark count from database
                delay(500)

                val actualLandmarkCount = try {
                    // Query landmarks in a rough corridor (for now just query a region)
                    50 // Simplified count for mock
                } catch (e: Exception) {
                    20
                }

                // Show pack info with realistic numbers
                _uiState.value = PackDownloadUiState.ReadyToDownload(
                    packSizeMB = 25, // More realistic size
                    landmarkCount = actualLandmarkCount,
                    tileCount = 500 // Reduced tile count
                )
            } catch (e: Exception) {
                _uiState.value = PackDownloadUiState.Error(
                    e.message ?: "Failed to check pack availability"
                )
            }
        }
    }

    fun confirmDownload() {
        val route = currentRoute ?: return
        viewModelScope.launch {
            try {
                // Simulate download with progress updates
                val tasks = listOf(
                    "Downloading landmarks..." to 0.2f,
                    "Downloading map tiles..." to 0.6f,
                    "Downloading photos..." to 0.8f,
                    "Extracting pack..." to 0.95f,
                    "Finalizing..." to 1.0f
                )

                for ((task, targetProgress) in tasks) {
                    _uiState.value = PackDownloadUiState.Downloading(
                        progress = targetProgress,
                        currentTask = task
                    )

                    // Simulate download time
                    delay(2000)

                    // TODO: Replace with actual download logic:
                    // 1. Download ZIP from backend API
                    // 2. Extract landmarks, tiles, airports
                    // 3. Insert into Room database
                    // 4. Mark pack as downloaded
                }

                // Mark as completed in repository
                offlinePackRepository.markPackDownloaded(route.first, route.second)

                _uiState.value = PackDownloadUiState.Completed
            } catch (e: Exception) {
                _uiState.value = PackDownloadUiState.Error(
                    e.message ?: "Download failed"
                )
            }
        }
    }
}
