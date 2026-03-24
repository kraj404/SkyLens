package com.skylens.presentation.ui.screens.planning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Valid IATA codes for testing (common airports)
private val VALID_IATA_CODES = setOf(
    "LAX", "JFK", "ORD", "DFW", "DEN", "SFO", "SEA", "LAS", "MCO", "EWR",
    "ATL", "IAH", "PHX", "BOS", "MSP", "DTW", "PHL", "LGA", "BWI", "DCA",
    "FRA", "LHR", "CDG", "AMS", "MAD", "FCO", "MUC", "ZRH", "VIE", "BRU",
    "NRT", "HND", "PEK", "PVG", "HKG", "SIN", "ICN", "BKK", "KUL", "DEL",
    "BLR", "BOM", "SYD", "MEL", "DXB", "DOH", "AUH", "CAI", "JNB", "GRU"
)

fun isValidIataCode(code: String): Boolean {
    val trimmed = code.trim().uppercase()
    return trimmed.length == 3 && trimmed.all { it.isLetter() } && VALID_IATA_CODES.contains(trimmed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightPlanningScreen(
    onNavigateToMap: (departure: String, arrival: String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    var departureText by remember { mutableStateOf("") }
    var arrivalText by remember { mutableStateOf("") }
    var showDepartureError by remember { mutableStateOf(false) }
    var showArrivalError by remember { mutableStateOf(false) }

    val departureTrimmed = departureText.trim().uppercase()
    val arrivalTrimmed = arrivalText.trim().uppercase()

    val isDepartureValid = departureTrimmed.isEmpty() || isValidIataCode(departureTrimmed)
    val isArrivalValid = arrivalTrimmed.isEmpty() || isValidIataCode(arrivalTrimmed)

    val canProceed = departureTrimmed.isNotEmpty() &&
                     arrivalTrimmed.isNotEmpty() &&
                     isDepartureValid &&
                     isArrivalValid &&
                     departureTrimmed != arrivalTrimmed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Your Flight") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Departure Airport:")
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = departureText,
                onValueChange = {
                    departureText = it.take(3).uppercase()
                    showDepartureError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter LAX, JFK, FRA...") },
                singleLine = true,
                isError = !isDepartureValid && departureText.isNotEmpty(),
                supportingText = {
                    if (!isDepartureValid && departureText.isNotEmpty()) {
                        Text(
                            text = "Invalid airport code. Try LAX, JFK, FRA, NRT, etc.",
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Arrival Airport:")
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = arrivalText,
                onValueChange = {
                    arrivalText = it.take(3).uppercase()
                    showArrivalError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter BLR, NRT, LHR...") },
                singleLine = true,
                isError = !isArrivalValid && arrivalText.isNotEmpty(),
                supportingText = {
                    if (!isArrivalValid && arrivalText.isNotEmpty()) {
                        Text(
                            text = "Invalid airport code. Try BLR, NRT, LHR, FRA, etc.",
                            color = Color.Red
                        )
                    } else if (departureTrimmed.isNotEmpty() && departureTrimmed == arrivalTrimmed) {
                        Text(
                            text = "Arrival must be different from departure",
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (canProceed) {
                        onNavigateToMap(departureTrimmed, arrivalTrimmed)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canProceed
            ) {
                Text("Download Pack & Start Flight")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateToHistory() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Trip History")
            }
        }
    }
}
