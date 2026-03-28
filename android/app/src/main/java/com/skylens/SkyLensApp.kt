package com.skylens.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.skylens.data.local.DatabaseSeeder
import com.skylens.data.service.PhotoFetchService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SkyLensApp : Application(), Configuration.Provider {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var photoFetchService: PhotoFetchService

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("SkyLensApp", "onCreate() called - starting database seeding")

        // Seed database with sample airports and landmarks on first launch
        applicationScope.launch {
            try {
                android.util.Log.d("SkyLensApp", "Seeding airports...")
                databaseSeeder.seedAirportsIfEmpty()
                android.util.Log.d("SkyLensApp", "Airports seeded successfully")

                android.util.Log.d("SkyLensApp", "Seeding landmarks...")
                databaseSeeder.seedLandmarksIfEmpty()
                android.util.Log.d("SkyLensApp", "Landmarks seeded successfully")

                // Start background photo fetching after seeding
                android.util.Log.d("SkyLensApp", "Starting photo fetch service...")
                photoFetchService.startBackgroundPhotoFetch()
            } catch (e: Exception) {
                android.util.Log.e("SkyLensApp", "Error seeding database", e)
            }
        }
    }
}
