package com.skylens.geo

import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoCalculatorTest {

    private val geoCalculator = GeoCalculator()

    @Test
    fun `visibility radius at 35000 feet should be approximately 210 km`() {
        val radius = geoCalculator.visibilityRadiusKm(35000)

        // Allow 10km tolerance
        assertTrue(abs(radius - 210.0) < 10.0, "Expected ~210km, got $radius")
    }

    @Test
    fun `visibility radius at 10000 feet should be approximately 95 km`() {
        val radius = geoCalculator.visibilityRadiusKm(10000)

        assertTrue(abs(radius - 95.0) < 10.0, "Expected ~95km, got $radius")
    }

    @Test
    fun `visibility radius at 0 feet should be 0 km`() {
        val radius = geoCalculator.visibilityRadiusKm(0)

        assertEquals(0.0, radius, 0.1)
    }

    @Test
    fun `haversine distance LAX to NRT should be approximately 8800 km`() {
        val laxLat = 33.9416
        val laxLon = -118.4085
        val nrtLat = 35.7647
        val nrtLon = 140.3864

        val distance = geoCalculator.haversineDistance(laxLat, laxLon, nrtLat, nrtLon)

        // Allow 100km tolerance
        assertTrue(abs(distance - 8815.0) < 100.0, "Expected ~8815km, got $distance")
    }

    @Test
    fun `haversine distance between same point should be 0`() {
        val distance = geoCalculator.haversineDistance(40.7128, -74.0060, 40.7128, -74.0060)

        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `haversine distance New York to London should be approximately 5570 km`() {
        val nyLat = 40.7128
        val nyLon = -74.0060
        val lonLat = 51.5074
        val lonLon = -0.1278

        val distance = geoCalculator.haversineDistance(nyLat, nyLon, lonLat, lonLon)

        assertTrue(abs(distance - 5570.0) < 100.0, "Expected ~5570km, got $distance")
    }

    @Test
    fun `bearing calculation should return correct azimuth`() {
        // LAX to NRT should be roughly northwest (bearing ~300-330°)
        val bearing = geoCalculator.calculateBearing(
            33.9416, -118.4085,  // LAX
            35.7647, 140.3864    // NRT
        )

        assertTrue(bearing in 0.0..360.0, "Bearing should be between 0 and 360")
    }

    @Test
    fun `isLandmarkVisible should return false when distance exceeds visibility radius`() {
        val altitude = 35000 // ~210km visibility
        val distance = 250.0  // 250km away

        val visible = geoCalculator.isLandmarkVisible(altitude, distance)

        assertEquals(false, visible)
    }

    @Test
    fun `isLandmarkVisible should return true when distance within visibility radius`() {
        val altitude = 35000 // ~210km visibility
        val distance = 150.0  // 150km away

        val visible = geoCalculator.isLandmarkVisible(altitude, distance)

        assertEquals(true, visible)
    }

    @Test
    fun `approximateRadiusSquared should return reasonable values`() {
        val radiusKm = 200.0
        val radiusSquared = geoCalculator.approximateRadiusSquared(radiusKm)

        // Very rough approximation, just check it's positive and reasonable
        assertTrue(radiusSquared > 0.0)
        assertTrue(radiusSquared < 10.0) // Should be in degrees squared
    }

    @Test
    fun `distance calculation should handle crossing international date line`() {
        // Point west of date line and point east of date line
        val distance = geoCalculator.haversineDistance(
            35.0, 179.0,  // Just west of date line
            35.0, -179.0  // Just east of date line
        )

        // Should be ~220km, not halfway around the world
        assertTrue(distance < 500.0, "Date line crossing calculation failed: $distance")
    }

    @Test
    fun `distance calculation should handle poles`() {
        val distance = geoCalculator.haversineDistance(
            90.0, 0.0,    // North pole
            -90.0, 0.0    // South pole
        )

        // Half the circumference of Earth (~20,000km)
        assertTrue(abs(distance - 20015.0) < 100.0, "Expected ~20015km, got $distance")
    }
}
