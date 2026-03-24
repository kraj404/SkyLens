package com.skylens.domain.model

data class FlightPosition(
    val latitude: Double,
    val longitude: Double,
    val altitude: Int,        // meters
    val speed: Float,         // km/h
    val heading: Float,       // degrees
    val accuracy: Float,      // meters
    val timestamp: Long
)
