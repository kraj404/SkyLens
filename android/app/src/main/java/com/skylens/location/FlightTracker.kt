package com.skylens.location

import android.location.Location
import com.skylens.domain.model.FlightPosition
import com.skylens.geo.GeoCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class FlightTracker @Inject constructor(
    private val locationProvider: LocationProvider,
    private val geoCalculator: GeoCalculator
) {

    private var lastLocation: Location? = null
    private var velocityKmH: Double = 0.0
    private var heading: Float = 0f
    private val gpsFilter = GpsFilter() // Add Kalman filter

    /**
     * Track flight with enriched position data
     */
    fun trackFlight(): Flow<FlightPosition> {
        return locationProvider.startLocationUpdates().map { location ->
            val flightPosition = processLocation(location)
            lastLocation = location
            flightPosition
        }
    }

    private fun processLocation(location: Location): FlightPosition {
        // Apply Kalman filter for smoothing
        val (smoothedLat, smoothedLon) = gpsFilter.filter(
            location.latitude,
            location.longitude,
            location.accuracy
        )

        // Calculate velocity if we have previous location
        val speed = calculateSpeed(location)

        // Estimate altitude (GPS altitude is unreliable, default to 35k ft)
        val altitude = estimateAltitude(location)

        // Calculate heading
        val currentHeading = if (location.hasBearing()) {
            location.bearing
        } else {
            calculateHeading(location)
        }

        velocityKmH = speed
        heading = currentHeading

        return FlightPosition(
            latitude = smoothedLat,
            longitude = smoothedLon,
            altitude = altitude,
            speed = speed.toFloat(),
            heading = currentHeading,
            accuracy = location.accuracy,
            timestamp = location.time
        )
    }

    private fun calculateSpeed(location: Location): Double {
        val last = lastLocation ?: return 0.0

        if (location.hasSpeed()) {
            // Convert m/s to km/h
            return location.speed * 3.6
        }

        // Calculate from position change
        val distance = geoCalculator.haversineDistance(
            last.latitude, last.longitude,
            location.latitude, location.longitude
        )

        val timeDiffSeconds = (location.time - last.time) / 1000.0
        if (timeDiffSeconds > 0) {
            val distancePerHour = (distance / timeDiffSeconds) * 3600
            return distancePerHour
        }

        return velocityKmH // Return last known velocity
    }

    private fun calculateHeading(location: Location): Float {
        val last = lastLocation ?: return heading

        val bearing = geoCalculator.calculateBearing(
            last.latitude, last.longitude,
            location.latitude, location.longitude
        )

        return bearing.toFloat()
    }

    private fun estimateAltitude(location: Location): Int {
        // GPS altitude is unreliable in aircraft
        // Use speed to estimate cruising altitude
        return when {
            velocityKmH < 200 -> 0 // On ground or low altitude
            velocityKmH < 500 -> 10000 // Climbing/descending
            velocityKmH < 700 -> 25000 // Mid-altitude cruise
            else -> 35000 // High-altitude cruise
        }
    }

    /**
     * Predict future position based on current velocity
     */
    fun predictFuturePosition(minutesAhead: Int): Pair<Double, Double>? {
        val last = lastLocation ?: return null

        return geoCalculator.estimateFuturePosition(
            last.latitude,
            last.longitude,
            velocityKmH,
            heading.toDouble(),
            minutesAhead
        )
    }

    /**
     * Check if currently in flight (based on speed)
     */
    fun isInFlight(): Boolean {
        return velocityKmH > 300 // > 300 km/h suggests flight
    }
}
