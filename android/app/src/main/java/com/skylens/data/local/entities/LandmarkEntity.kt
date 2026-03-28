package com.skylens.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "landmarks",
    indices = [Index(value = ["latitude", "longitude"])]
)
data class LandmarkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String, // mountain, city, monument, river, etc.

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "elevation_m")
    val elevationM: Int?,

    @ColumnInfo(name = "importance_score")
    val importanceScore: Float,

    @ColumnInfo(name = "wiki_id")
    val wikiId: String?,

    @ColumnInfo(name = "country")
    val country: String?,

    @ColumnInfo(name = "ai_story")
    val aiStory: String?, // Cached Claude API response

    @ColumnInfo(name = "photo_urls")
    val photoUrls: String?, // JSON array as string

    @ColumnInfo(name = "photo_files")
    val photoFiles: String?, // JSON array of local file paths

    @ColumnInfo(name = "general_fact")
    val generalFact: String?,

    @ColumnInfo(name = "historical_fact")
    val historicalFact: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
