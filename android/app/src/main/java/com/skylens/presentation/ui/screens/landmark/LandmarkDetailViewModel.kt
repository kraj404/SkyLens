package com.skylens.presentation.ui.screens.landmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.data.repository.LandmarkRepository
import com.skylens.domain.model.Landmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LandmarkDetailUiState(
    val landmark: Landmark? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LandmarkDetailViewModel @Inject constructor(
    private val landmarkRepository: LandmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandmarkDetailUiState())
    val uiState: StateFlow<LandmarkDetailUiState> = _uiState.asStateFlow()

    fun loadLandmark(landmarkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val landmark = landmarkRepository.getLandmarkById(landmarkId)

                if (landmark != null) {
                    _uiState.value = _uiState.value.copy(
                        landmark = landmark,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Landmark not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load landmark: ${e.message}"
                )
            }
        }
    }
}
