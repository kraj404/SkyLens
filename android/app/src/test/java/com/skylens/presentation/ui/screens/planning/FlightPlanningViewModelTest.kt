package com.skylens.presentation.ui.screens.planning

import com.skylens.data.repository.AirportRepository
import com.skylens.domain.model.Airport
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlightPlanningViewModelTest {

    private lateinit var viewModel: FlightPlanningViewModel
    private lateinit var airportRepository: AirportRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        airportRepository = mockk()
        viewModel = FlightPlanningViewModel(airportRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(emptyList(), state.searchResults)
        assertEquals(null, state.selectedDeparture)
        assertEquals(null, state.selectedArrival)
        assertFalse(state.isSearching)
    }

    @Test
    fun `canStartFlight should return false when no airports selected`() {
        assertFalse(viewModel.canStartFlight())
    }

    @Test
    fun `canStartFlight should return false when only departure selected`() {
        val lax = Airport("LAX", "KLAX", "Los Angeles International", 33.9416, -118.4085)
        viewModel.selectDepartureAirport(lax)

        assertFalse(viewModel.canStartFlight())
    }

    @Test
    fun `canStartFlight should return false when only arrival selected`() {
        val nrt = Airport("NRT", "RJAA", "Tokyo Narita", 35.7647, 140.3864)
        viewModel.selectArrivalAirport(nrt)

        assertFalse(viewModel.canStartFlight())
    }

    @Test
    fun `canStartFlight should return true when both airports selected`() {
        val lax = Airport("LAX", "KLAX", "Los Angeles International", 33.9416, -118.4085)
        val nrt = Airport("NRT", "RJAA", "Tokyo Narita", 35.7647, 140.3864)

        viewModel.selectDepartureAirport(lax)
        viewModel.selectArrivalAirport(nrt)

        assertTrue(viewModel.canStartFlight())
    }

    @Test
    fun `searchAirports should update state with results`() = runTest {
        val query = "Los Angeles"
        val mockAirports = listOf(
            Airport("LAX", "KLAX", "Los Angeles International", 33.9416, -118.4085)
        )

        coEvery { airportRepository.searchAirports(query, 20) } returns flowOf(mockAirports)

        viewModel.searchAirports(query)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(query, state.searchQuery)
        assertEquals(mockAirports, state.searchResults)
        assertFalse(state.isSearching)
    }
}
