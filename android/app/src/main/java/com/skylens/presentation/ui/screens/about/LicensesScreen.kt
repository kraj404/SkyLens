package com.skylens.presentation.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class License(
    val library: String,
    val license: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onNavigateBack: () -> Unit
) {
    val licenses = listOf(
        License(
            library = "Jetpack Compose",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose"
        ),
        License(
            library = "Material 3",
            license = "Apache License 2.0",
            url = "https://m3.material.io/"
        ),
        License(
            library = "Hilt",
            license = "Apache License 2.0",
            url = "https://dagger.dev/hilt/"
        ),
        License(
            library = "Room Database",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/room"
        ),
        License(
            library = "MapLibre Native",
            license = "BSD 2-Clause License",
            url = "https://maplibre.org/"
        ),
        License(
            library = "Supabase Kotlin",
            license = "Apache License 2.0",
            url = "https://github.com/supabase-community/supabase-kt"
        ),
        License(
            library = "Coil Image Loading",
            license = "Apache License 2.0",
            url = "https://coil-kt.github.io/coil/"
        ),
        License(
            library = "Accompanist Permissions",
            license = "Apache License 2.0",
            url = "https://google.github.io/accompanist/"
        ),
        License(
            library = "Kotlin Coroutines",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        License(
            library = "OkHttp",
            license = "Apache License 2.0",
            url = "https://square.github.io/okhttp/"
        ),
        License(
            library = "Retrofit",
            license = "Apache License 2.0",
            url = "https://square.github.io/retrofit/"
        ),
        License(
            library = "Gson",
            license = "Apache License 2.0",
            url = "https://github.com/google/gson"
        ),
        License(
            library = "Play Services Location",
            license = "Apache License 2.0",
            url = "https://developers.google.com/android/guides/overview"
        ),
        License(
            library = "OpenStreetMap",
            license = "Open Data Commons Open Database License (ODbL)",
            url = "https://www.openstreetmap.org/copyright"
        ),
        License(
            library = "OpenFlights Airport Data",
            license = "Open Database License (ODbL)",
            url = "https://openflights.org/data.html"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "SkyLens is built with the following open source libraries and data sources:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(licenses) { license ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = license.library,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = license.license,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = license.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Thank You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We're grateful to all the open source maintainers and contributors who make projects like SkyLens possible. Your work enables developers worldwide to build better software.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
                        )
                    }
                }
            }
        }
    }
}
