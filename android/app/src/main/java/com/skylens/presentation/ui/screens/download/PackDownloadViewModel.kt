package com.skylens.presentation.ui.screens.download

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skylens.data.repository.OfflinePackRepository
import com.skylens.workers.PackDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class PackDownloadUiState {
    object Initial : PackDownloadUiState()
    object Checking : PackDownloadUiState()
    data class ReadyToDownload(
        val packSizeMB: Int,
        val landmarkCount: Int,
        val photoCount: Int
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
    private val offlinePackRepository: OfflinePackRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<PackDownloadUiState>(PackDownloadUiState.Initial)
    val uiState: StateFlow<PackDownloadUiState> = _uiState.asStateFlow()

    private var currentRoute: Pair<String, String>? = null
    private var workRequestId: UUID? = null

    fun startDownload(departure: String, arrival: String) {
        currentRoute = departure to arrival
        viewModelScope.launch {
            _uiState.value = PackDownloadUiState.Checking

            try {
                // Check if pack already exists
                val existingPack = offlinePackRepository.getOfflinePack(departure, arrival)

                if (existingPack != null && existingPack.status == "completed") {
                    // Pack already exists, skip to completed
                    _uiState.value = PackDownloadUiState.Completed
                    return@launch
                }

                // Show estimated pack info
                _uiState.value = PackDownloadUiState.ReadyToDownload(
                    packSizeMB = 50, // Estimate: 50 landmarks × 2 photos × 500KB
                    landmarkCount = 50, // Typical route
                    photoCount = 100 // 2 per landmark
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
                // Create WorkManager request
                val workRequest = OneTimeWorkRequestBuilder<PackDownloadWorker>()
                    .setInputData(workDataOf(
                        "departure" to route.first,
                        "arrival" to route.second
                    ))
                    .build()

                workRequestId = workRequest.id

                // Enqueue the work
                WorkManager.getInstance(context).enqueue(workRequest)

                // Observe progress
                observeWorkProgress(workRequest.id)
            } catch (e: Exception) {
                _uiState.value = PackDownloadUiState.Error(
                    e.message ?: "Failed to start download"
                )
            }
        }
    }

    private fun observeWorkProgress(workId: UUID) {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfoByIdFlow(workId)
                .collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
                            val progress = workInfo.progress.getFloat("progress", 0f)
                            _uiState.value = PackDownloadUiState.Downloading(
                                progress = progress,
                                currentTask = "Downloading photos and facts..."
                            )
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            _uiState.value = PackDownloadUiState.Completed
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            _uiState.value = PackDownloadUiState.Error("Download failed")
                        }
                        else -> {}
                    }
                }
        }
    }

    fun cancelDownload() {
        workRequestId?.let { id ->
            WorkManager.getInstance(context).cancelWorkById(id)
            _uiState.value = PackDownloadUiState.Initial
        }
    }
}
