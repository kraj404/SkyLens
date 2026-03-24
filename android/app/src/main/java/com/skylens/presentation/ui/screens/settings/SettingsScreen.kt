package com.skylens.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit = {}
) {
    var useMockGps by remember { mutableStateOf(true) }
    var enableNotifications by remember { mutableStateOf(true) }
    var useMetricUnits by remember { mutableStateOf(false) }
    var enableBackgroundTracking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Flight Tracking Section
            SettingsSection(title = "Flight Tracking") {
                SettingsSwitch(
                    title = "Use Mock GPS",
                    description = "Simulate flight for testing (disable for real flights)",
                    checked = useMockGps,
                    onCheckedChange = { useMockGps = it }
                )

                SettingsSwitch(
                    title = "Background Tracking",
                    description = "Continue tracking when app is in background",
                    checked = enableBackgroundTracking,
                    onCheckedChange = { enableBackgroundTracking = it }
                )
            }

            Divider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitch(
                    title = "Landmark Alerts",
                    description = "Get notified when landmarks become visible",
                    checked = enableNotifications,
                    onCheckedChange = { enableNotifications = it }
                )
            }

            Divider()

            // Units Section
            SettingsSection(title = "Units") {
                SettingsSwitch(
                    title = "Use Metric",
                    description = "Show altitude in meters, speed in km/h",
                    checked = useMetricUnits,
                    onCheckedChange = { useMetricUnits = it }
                )
            }

            Divider()

            // Data Section
            SettingsSection(title = "Data") {
                SettingsButton(
                    title = "Clear Cache",
                    description = "Delete cached map tiles and photos",
                    onClick = { /* TODO: Implement */ }
                )

                SettingsButton(
                    title = "Export Trips",
                    description = "Export trip history as JSON",
                    onClick = { /* TODO: Implement */ }
                )
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    description = "1.0.0 (Beta)"
                )

                SettingsButton(
                    title = "About SkyLens",
                    description = "App info, credits, and licenses",
                    onClick = onNavigateToAbout
                )

                SettingsButton(
                    title = "Privacy Policy",
                    description = "View our privacy policy",
                    onClick = { /* TODO: Open browser */ }
                )

                SettingsButton(
                    title = "Open Source Licenses",
                    description = "Third-party software licenses",
                    onClick = { /* TODO: Show licenses */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
