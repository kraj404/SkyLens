package com.skylens.location

import android.location.Location
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpsFilterTest {

    private lateinit var latitudeFilter: KalmanFilter
    private lateinit var longitudeFilter: KalmanFilter

    @Before
    fun setup() {
        latitudeFilter = KalmanFilter()
        longitudeFilter = KalmanFilter()
    }

    @Test
    fun `kalman filter should smooth noisy GPS data`() {
        // Simulate noisy GPS readings around 35.0 latitude
        val noisyReadings = listOf(
            35.0, 35.05, 34.95, 35.02, 34.98, 35.01, 34.99, 35.03
        )

        val filteredReadings = noisyReadings.map { reading ->
            latitudeFilter.filter(reading, measurementAccuracy = 10.0)
        }

        // Filtered values should be smoother (less variance)
        val noisyVariance = calculateVariance(noisyReadings)
        val filteredVariance = calculateVariance(filteredReadings)

        assertTrue(filteredVariance < noisyVariance,
            "Filtered variance ($filteredVariance) should be less than noisy variance ($noisyVariance)")
    }

    @Test
    fun `kalman filter should converge to true value`() {
        val trueValue = 40.0
        val measurements = List(20) { trueValue + (Math.random() - 0.5) * 0.1 }

        val filteredValues = measurements.map { measurement ->
            latitudeFilter.filter(measurement, measurementAccuracy = 5.0)
        }

        // Last filtered value should be very close to true value
        val lastFiltered = filteredValues.last()
        assertTrue((lastFiltered - trueValue).let { kotlin.math.abs(it) } < 0.05,
            "Final filtered value ($lastFiltered) should be close to true value ($trueValue)")
    }

    @Test
    fun `kalman filter should handle high accuracy measurements`() {
        val highAccuracyReading = 35.6762
        val lowAccuracy = 100.0  // 100m uncertainty
        val highAccuracy = 5.0    // 5m uncertainty

        // Process with low accuracy
        val result1 = latitudeFilter.filter(highAccuracyReading, lowAccuracy)

        // Reset and process with high accuracy
        val filter2 = KalmanFilter()
        val result2 = filter2.filter(highAccuracyReading, highAccuracy)

        // High accuracy measurement should have more influence
        assertTrue(kotlin.math.abs(result2 - highAccuracyReading) <
                   kotlin.math.abs(result1 - highAccuracyReading))
    }

    @Test
    fun `kalman filter should handle sudden movement`() {
        // Simulate aircraft taking off (sudden altitude change)
        val readings = listOf(0.0, 0.0, 0.0, 500.0, 1000.0, 1500.0)

        val filteredReadings = readings.map { reading ->
            latitudeFilter.filter(reading, measurementAccuracy = 10.0)
        }

        // Filter should track the change (not stay at 0)
        assertTrue(filteredReadings.last() > 500.0,
            "Filter should track sudden movement: ${filteredReadings.last()}")
    }

    @Test
    fun `gps filter should smooth location updates`() {
        val gpsFilter = GpsFilter()

        // Simulate GPS readings with noise
        val locations = listOf(
            createLocation(35.6762, 139.6503, 10f),
            createLocation(35.6765, 139.6505, 10f), // Slight movement
            createLocation(35.6761, 139.6504, 15f), // Noisy reading
            createLocation(35.6768, 139.6506, 10f)
        )

        val filteredLocations = locations.map { location ->
            gpsFilter.filterLocation(location)
        }

        // Filtered locations should exist
        assertTrue(filteredLocations.all { it != null })

        // Check smoothness - variance should be lower
        val originalLats = locations.map { it.latitude }
        val filteredLats = filteredLocations.mapNotNull { it?.latitude }

        val originalVariance = calculateVariance(originalLats)
        val filteredVariance = calculateVariance(filteredLats)

        assertTrue(filteredVariance <= originalVariance,
            "Filtered should be smoother: original=$originalVariance, filtered=$filteredVariance")
    }

    private fun createLocation(lat: Double, lon: Double, accuracy: Float): Location {
        return Location("test").apply {
            latitude = lat
            longitude = lon
            this.accuracy = accuracy
            time = System.currentTimeMillis()
        }
    }

    private fun calculateVariance(values: List<Double>): Double {
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average()
    }
}
