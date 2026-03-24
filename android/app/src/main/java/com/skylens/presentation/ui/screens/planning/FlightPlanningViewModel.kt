package com.skylens.presentation.ui.screens.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.data.repository.AirportRepository
import com.skylens.domain.model.Airport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlightPlanningUiState(
    val searchQuery: String = "",
    val searchResults: List<Airport> = emptyList(),
    val selectedDeparture: Airport? = null,
    val selectedArrival: Airport? = null,
    val isSearching: Boolean = false
)

@HiltViewModel
class FlightPlanningViewModel @Inject constructor(
    private val airportRepository: AirportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightPlanningUiState())
    val uiState: StateFlow<FlightPlanningUiState> = _uiState.asStateFlow()

    fun searchAirports(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = true) }

        viewModelScope.launch {
            airportRepository.searchAirports(query, limit = 20).collect { results ->
                _uiState.update {
                    it.copy(searchResults = results, isSearching = false)
                }
            }
        }
    }

    fun selectDepartureAirport(airport: Airport) {
        _uiState.update { it.copy(selectedDeparture = airport) }
    }

    fun selectArrivalAirport(airport: Airport) {
        _uiState.update { it.copy(selectedArrival = airport) }
    }

    fun canStartFlight(): Boolean {
        return _uiState.value.selectedDeparture != null &&
                _uiState.value.selectedArrival != null
    }
}
