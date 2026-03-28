package com.skylens.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skylens.data.local.dao.AiUsageDao
import com.skylens.data.local.dao.AirportDao
import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.dao.OfflinePackDao
import com.skylens.data.local.dao.TripDao
import com.skylens.data.local.entities.AiUsageEntity
import com.skylens.data.local.entities.AirportEntity
import com.skylens.data.local.entities.LandmarkEntity
import com.skylens.data.local.entities.OfflinePackEntity
import com.skylens.data.local.entities.TripEntity
import com.skylens.data.local.entities.TripEventEntity

@Database(
    entities = [
        AirportEntity::class,
        LandmarkEntity::class,
        TripEntity::class,
        TripEventEntity::class,
        AiUsageEntity::class,
        OfflinePackEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SkyLensDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun landmarkDao(): LandmarkDao
    abstract fun tripDao(): TripDao
    abstract fun aiUsageDao(): AiUsageDao
    abstract fun offlinePackDao(): OfflinePackDao

    companion object {
        const val DATABASE_NAME = "skylens.db"
    }
}
