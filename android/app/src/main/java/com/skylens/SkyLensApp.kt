package com.skylens.app

import android.app.Application
import com.skylens.data.local.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SkyLensApp : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
            } catch (e: Exception) {
                android.util.Log.e("SkyLensApp", "Error seeding database", e)
            }
        }
    }
}
