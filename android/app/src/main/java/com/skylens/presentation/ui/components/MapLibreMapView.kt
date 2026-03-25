package com.skylens.presentation.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.skylens.domain.model.FlightPosition
import com.skylens.domain.model.Landmark
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun MapLibreMapView(
    modifier: Modifier = Modifier,
    currentPosition: FlightPosition?,
    landmarks: List<Landmark> = emptyList(),
    routePoints: List<Pair<Double, Double>> = emptyList(),
    onMapReady: (MapLibreMap) -> Unit = {},
    onLandmarkClick: (Landmark) -> Unit = {}
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    // Remember map state to prevent recreation
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var lastLandmarkCount by remember { mutableStateOf(0) }
    var lastRouteSize by remember { mutableStateOf(0) }
    var hasInitializedCamera by remember { mutableStateOf(false) }
    var aircraftMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            android.util.Log.d("MapLibreMapView", "Update called: landmarks=${landmarks.size}, route=${routePoints.size}, mapInstance=${mapInstance != null}")

            // Only update if map is not initialized or data changed significantly
            val shouldUpdate = mapInstance == null ||
                               landmarks.size != lastLandmarkCount ||
                               routePoints.size != lastRouteSize

            android.util.Log.d("MapLibreMapView", "shouldUpdate=$shouldUpdate (lastLandmarkCount=$lastLandmarkCount, lastRouteSize=$lastRouteSize)")

            if (!shouldUpdate) {
                // Just update position marker
                mapInstance?.let { map ->
                    currentPosition?.let { position ->
                        android.util.Log.d("MapLibreMapView", "Updating position only: ${position.latitude}, ${position.longitude}")

                        // Remove old aircraft marker
                        aircraftMarker?.let { map.removeMarker(it) }

                        // Add new aircraft marker at updated position
                        val newMarker = map.addMarker(
                            org.maplibre.android.annotations.MarkerOptions()
                                .position(org.maplibre.android.geometry.LatLng(position.latitude, position.longitude))
                                .title("✈️ Your Position")
                                .snippet("Alt: ${position.altitude}ft, Speed: ${position.speed}km/h")
                        )
                        aircraftMarker = newMarker
                    }
                }
                return@AndroidView
            }

            android.util.Log.d("MapLibreMapView", "Triggering full map update")
            view.getMapAsync { map ->
                mapInstance = map

                // Load map style
                map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->

                    // Clear existing markers
                    map.clear()

                    // Add route line if available
                    if (routePoints.isNotEmpty()) {
                        val linePoints = routePoints.map { Point.fromLngLat(it.second, it.first) }
                        val lineString = LineString.fromLngLats(linePoints)

                        style.addSource(GeoJsonSource("route-source", FeatureCollection.fromFeature(Feature.fromGeometry(lineString))))
                        style.addLayer(
                            LineLayer("route-layer", "route-source")
                                .withProperties(
                                    PropertyFactory.lineColor(Color.BLUE),
                                    PropertyFactory.lineWidth(3f)
                                )
                        )

                        // Fit camera to route ONLY on first load
                        if (!hasInitializedCamera && routePoints.isNotEmpty()) {
                            val boundsBuilder = LatLngBounds.Builder()
                            routePoints.forEach { boundsBuilder.include(LatLng(it.first, it.second)) }
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                            hasInitializedCamera = true
                            android.util.Log.d("MapLibreMapView", "Camera initialized to fit route bounds")
                        }
                    }

                    // Add landmark markers with click handling
                    val markerMap = mutableMapOf<Long, Landmark>()
                    landmarks.forEach { landmark ->
                        val snippet = buildString {
                            append(landmark.type.name)
                            landmark.country?.let { append(" • $it") }
                            landmark.elevationM?.let { append("\n${(it * 3.28084).toInt()} ft") }
                            landmark.aiStory?.let { story ->
                                if (story.isNotEmpty()) {
                                    append("\n\n")
                                    append(story.take(100))
                                    if (story.length > 100) append("...")
                                }
                            }
                        }

                        val markerOptions = MarkerOptions()
                            .position(LatLng(landmark.latitude, landmark.longitude))
                            .title(landmark.name)
                            .snippet(snippet)

                        val marker = map.addMarker(markerOptions)
                        marker?.let { markerMap[it.id] = landmark }
                    }
                    android.util.Log.d("MapLibreMapView", "Added ${landmarks.size} landmark markers")

                    // Set marker click listener
                    map.setOnMarkerClickListener { marker ->
                        android.util.Log.d("MapLibreMapView", "Marker clicked: ${marker.title}")
                        markerMap[marker.id]?.let { landmark ->
                            onLandmarkClick(landmark)
                        }
                        // Return true to consume the event and keep marker visible
                        true
                    }

                    // Add aircraft marker
                    currentPosition?.let { position ->
                        val newAircraftMarker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(position.latitude, position.longitude))
                                .title("✈️ Your Position")
                                .snippet("Alt: ${position.altitude}ft, Speed: ${position.speed}km/h")
                        )
                        aircraftMarker = newAircraftMarker

                        // Center on aircraft if no route
                        if (routePoints.isEmpty()) {
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(position.latitude, position.longitude))
                                .zoom(8.0)
                                .build()
                        }
                    }

                    onMapReady(map)

                    // Update tracking counts AFTER map is fully loaded
                    lastLandmarkCount = landmarks.size
                    lastRouteSize = routePoints.size
                    android.util.Log.d("MapLibreMapView", "Map fully loaded with ${landmarks.size} landmarks and ${routePoints.size} route points")
                }
            }
        }
    )
}

@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(mapView) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}
