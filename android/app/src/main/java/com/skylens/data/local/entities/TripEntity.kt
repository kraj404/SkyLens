package com.skylens.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String?,

    @ColumnInfo(name = "departure_airport")
    val departureAirport: String,

    @ColumnInfo(name = "arrival_airport")
    val arrivalAirport: String,

    @ColumnInfo(name = "route_geojson")
    val routeGeoJson: String, // LineString GeoJSON

    @ColumnInfo(name = "start_time")
    val startTime: Long?,

    @ColumnInfo(name = "end_time")
    val endTime: Long?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_events")
data class TripEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "trip_id")
    val tripId: String,

    @ColumnInfo(name = "landmark_id")
    val landmarkId: String,

    @ColumnInfo(name = "event_time")
    val eventTime: Long,

    @ColumnInfo(name = "distance_km")
    val distanceKm: Float?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
