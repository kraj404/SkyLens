package com.skylens.domain.model

data class Airport(
    val iataCode: String,
    val icaoCode: String?,
    val name: String,
    val city: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Int?,
    val timezone: String?
) {
    /**
     * Display name for UI: "LAX - Los Angeles International Airport"
     */
    fun displayName(): String = "$iataCode - $name"

    /**
     * Short display for compact views: "LAX (Los Angeles)"
     */
    fun shortName(): String = city?.let { "$iataCode ($it)" } ?: iataCode
}

data class Landmark(
    val id: String,
    val name: String,
    val type: LandmarkType,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Int?,
    val importanceScore: Float,
    val wikiId: String?,
    val country: String?,
    val aiStory: String?,
    val photoUrls: List<String>,
    val photoFiles: List<String> = emptyList(),
    val generalFact: String? = null,
    val historicalFact: String? = null
)

enum class LandmarkType {
    MOUNTAIN,
    CITY,
    MONUMENT,
    RIVER,
    LAKE,
    TEMPLE,
    HISTORICAL_SITE,
    NATURAL_WONDER,
    VOLCANO,
    DESERT,
    GLACIER,
    CANYON,
    ISLAND,
    OTHER;

    companion object {
        fun fromString(value: String): LandmarkType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

data class Trip(
    val id: String,
    val userId: String?,
    val departureAirport: String,
    val arrivalAirport: String,
    val routeGeoJson: String,
    val startTime: Long?,
    val endTime: Long?,
    val createdAt: Long,
    val events: List<TripEvent> = emptyList()
)

data class TripEvent(
    val id: String,
    val tripId: String,
    val landmarkId: String,
    val eventTime: Long,
    val distanceKm: Float?,
    val createdAt: Long
)

data class PredictedLandmark(
    val landmark: Landmark,
    val visibleInMinutes: Int,
    val confidence: Float,
    val aiPreview: String? = null
)
