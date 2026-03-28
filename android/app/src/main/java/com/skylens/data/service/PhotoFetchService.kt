package com.skylens.data.service

import android.util.Log
import com.skylens.data.repository.LandmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoFetchService @Inject constructor(
    private val landmarkRepository: LandmarkRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isFetching = false

    /**
     * Start background photo fetching for all landmarks without photos
     * Runs at low priority to avoid blocking main app functionality
     */
    fun startBackgroundPhotoFetch() {
        if (isFetching) return

        scope.launch {
            isFetching = true
            Log.d("PhotoFetchService", "Starting background photo fetch")

            try {
                val allLandmarks = landmarkRepository.getAllLandmarks()
                val landmarksWithoutPhotos = allLandmarks.filter { it.photoUrls.isEmpty() }

                Log.d("PhotoFetchService", "Found ${landmarksWithoutPhotos.size} landmarks without photos")

                // Fetch photos slowly to avoid rate limits (max 10/min)
                landmarksWithoutPhotos.take(50).forEachIndexed { index, landmark ->
                    try {
                        val photoUrl = landmarkRepository.fetchAndCachePhoto(landmark.id)
                        if (photoUrl != null) {
                            Log.d("PhotoFetchService", "Fetched photo for ${landmark.name}")
                        } else {
                            Log.d("PhotoFetchService", "No photo found for ${landmark.name}")
                        }

                        // Rate limit: 6 second delay = 10 photos/min
                        if (index < landmarksWithoutPhotos.size - 1) {
                            delay(6000)
                        }
                    } catch (e: Exception) {
                        Log.e("PhotoFetchService", "Failed to fetch photo for ${landmark.name}", e)
                    }
                }

                Log.d("PhotoFetchService", "Background photo fetch completed")
            } finally {
                isFetching = false
            }
        }
    }

    fun isCurrentlyFetching(): Boolean = isFetching
}
