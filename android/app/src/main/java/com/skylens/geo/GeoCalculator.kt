package com.skylens.geo

import kotlin.math.*

object GeoCalculator {

    private const val EARTH_RADIUS_M = 6371000.0
    private const val EARTH_RADIUS_KM = 6371.0
    private const val ATMOSPHERIC_CLARITY = 0.8

    /**
     * Calculate visibility radius based on aircraft altitude
     * Accounts for Earth curvature and atmospheric conditions
     */
    fun visibilityRadiusKm(altitudeFeet: Int): Double {
        val altitudeM = altitudeFeet * 0.3048

        // Horizon distance formula: d = sqrt(2 * R * h)
        val horizonM = sqrt(2 * EARTH_RADIUS_M * altitudeM)

        // Adjust for atmospheric visibility (haze, moisture)
        return (horizonM / 1000.0) * ATMOSPHERIC_CLARITY
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculate bearing from point 1 to point 2
     * Returns bearing in degrees (0-360)
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)

        return (bearingDeg + 360) % 360
    }

    /**
     * Estimate future position based on current velocity
     * Used for predicting upcoming landmarks
     */
    fun estimateFuturePosition(
        currentLat: Double,
        currentLon: Double,
        speedKmH: Double,
        headingDeg: Double,
        minutesAhead: Int
    ): Pair<Double, Double> {
        val distanceKm = speedKmH * (minutesAhead / 60.0)
        val angularDistance = distanceKm / EARTH_RADIUS_KM

        val lat1 = Math.toRadians(currentLat)
        val lon1 = Math.toRadians(currentLon)
        val bearing = Math.toRadians(headingDeg)

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
            cos(lat1) * sin(angularDistance) * cos(bearing)
        )

        val lon2 = lon1 + atan2(
            sin(bearing) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return Pair(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    /**
     * Check if landmark is within visibility range
     */
    fun isLandmarkVisible(
        aircraftLat: Double,
        aircraftLon: Double,
        aircraftAltitudeFeet: Int,
        landmarkLat: Double,
        landmarkLon: Double
    ): Boolean {
        val distance = haversineDistance(
            aircraftLat, aircraftLon,
            landmarkLat, landmarkLon
        )
        val visibilityRadius = visibilityRadiusKm(aircraftAltitudeFeet)
        return distance <= visibilityRadius
    }

    /**
     * Calculate squared distance for efficient nearby queries
     * Used in Room queries for filtering before precise calculations
     */
    fun approximateRadiusSquared(radiusKm: Double): Double {
        // Approximate degrees per km at equator
        val degreesPerKm = 1.0 / 111.0
        val radiusDegrees = radiusKm * degreesPerKm
        return radiusDegrees * radiusDegrees
    }
}
