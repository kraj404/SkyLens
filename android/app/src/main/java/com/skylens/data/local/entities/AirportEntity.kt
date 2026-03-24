package com.skylens.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airports")
data class AirportEntity(
    @PrimaryKey
    @ColumnInfo(name = "iata_code")
    val iataCode: String,

    @ColumnInfo(name = "icao_code")
    val icaoCode: String?,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "city")
    val city: String?,

    @ColumnInfo(name = "country")
    val country: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "elevation_m")
    val elevationM: Int?,

    @ColumnInfo(name = "timezone")
    val timezone: String?
)
