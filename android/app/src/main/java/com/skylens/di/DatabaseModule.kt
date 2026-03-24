package com.skylens.di

import android.content.Context
import androidx.room.Room
import com.skylens.data.local.SkyLensDatabase
import com.skylens.data.local.dao.AirportDao
import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSkyLensDatabase(
        @ApplicationContext context: Context
    ): SkyLensDatabase {
        return Room.databaseBuilder(
            context,
            SkyLensDatabase::class.java,
            "skylens.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAirportDao(database: SkyLensDatabase): AirportDao {
        return database.airportDao()
    }

    @Provides
    @Singleton
    fun provideLandmarkDao(database: SkyLensDatabase): LandmarkDao {
        return database.landmarkDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(database: SkyLensDatabase): TripDao {
        return database.tripDao()
    }
}
