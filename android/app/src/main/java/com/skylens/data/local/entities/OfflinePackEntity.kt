package com.skylens.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_packs")
data class OfflinePackEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "route")
    val route: String, // "LAX-NRT"

    @ColumnInfo(name = "departure")
    val departure: String,

    @ColumnInfo(name = "arrival")
    val arrival: String,

    @ColumnInfo(name = "landmark_count")
    val landmarkCount: Int,

    @ColumnInfo(name = "photo_count")
    val photoCount: Int,

    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,

    @ColumnInfo(name = "status")
    val status: String, // "downloading", "completed", "failed"

    @ColumnInfo(name = "progress")
    val progress: Float, // 0.0 to 1.0

    @ColumnInfo(name = "downloaded_at")
    val downloadedAt: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
