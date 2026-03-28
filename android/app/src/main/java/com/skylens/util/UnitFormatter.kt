package com.skylens.util

object UnitFormatter {

    fun formatAltitude(altitudeFeet: Int, useMetric: Boolean): String {
        return if (useMetric) {
            val meters = (altitudeFeet * 0.3048).toInt()
            "$meters m"
        } else {
            "$altitudeFeet ft"
        }
    }

    fun formatSpeed(speedKmh: Float, useMetric: Boolean): String {
        return if (useMetric) {
            "${speedKmh.toInt()} km/h"
        } else {
            val mph = (speedKmh * 0.621371).toInt()
            "$mph mph"
        }
    }

    fun formatDistance(distanceKm: Double, useMetric: Boolean): String {
        return if (useMetric) {
            String.format("%.1f km", distanceKm)
        } else {
            val miles = distanceKm * 0.621371
            String.format("%.1f mi", miles)
        }
    }

    fun formatElevation(elevationM: Int, useMetric: Boolean): String {
        return if (useMetric) {
            "$elevationM m"
        } else {
            val feet = (elevationM * 3.28084).toInt()
            "$feet ft"
        }
    }
}
