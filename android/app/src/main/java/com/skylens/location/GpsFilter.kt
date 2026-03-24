package com.skylens.location

import kotlin.math.sqrt

/**
 * Simple 1D Kalman filter for GPS coordinate smoothing
 */
class KalmanFilter(
    private var processNoise: Double = 0.0001, // Q
    private var measurementNoise: Double = 0.1 // R
) {
    private var estimate: Double = 0.0
    private var errorCovariance: Double = 1.0
    private var isInitialized = false

    fun filter(measurement: Double, measurementAccuracy: Double = measurementNoise): Double {
        if (!isInitialized) {
            estimate = measurement
            isInitialized = true
            return estimate
        }

        // Prediction step
        val predictedEstimate = estimate
        val predictedErrorCovariance = errorCovariance + processNoise

        // Update step
        val kalmanGain = predictedErrorCovariance / (predictedErrorCovariance + measurementAccuracy)
        estimate = predictedEstimate + kalmanGain * (measurement - predictedEstimate)
        errorCovariance = (1 - kalmanGain) * predictedErrorCovariance

        return estimate
    }

    fun reset() {
        isInitialized = false
        errorCovariance = 1.0
    }
}

/**
 * GPS filter for smoothing latitude and longitude
 */
class GpsFilter {
    private val latitudeFilter = KalmanFilter()
    private val longitudeFilter = KalmanFilter()

    fun filter(latitude: Double, longitude: Double, accuracy: Float): Pair<Double, Double> {
        val accuracyNormalized = (accuracy / 10.0).coerceIn(0.01, 10.0)

        val smoothedLat = latitudeFilter.filter(latitude, accuracyNormalized)
        val smoothedLon = longitudeFilter.filter(longitude, accuracyNormalized)

        return Pair(smoothedLat, smoothedLon)
    }

    fun reset() {
        latitudeFilter.reset()
        longitudeFilter.reset()
    }
}
