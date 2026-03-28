package com.skylens.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.skylens.app.R
import com.skylens.ai.AiStoryManager
import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.dao.OfflinePackDao
import com.skylens.data.local.dao.TripDao
import com.skylens.data.local.entities.OfflinePackEntity
import com.skylens.data.local.entities.TripEntity
import com.skylens.data.remote.PixabayApiClient
import com.skylens.data.remote.PexelsApiClient
import com.skylens.data.remote.WikimediaApiClient
import com.skylens.data.repository.AirportRepository
import com.skylens.geo.GeoCalculator
import com.skylens.util.PhotoDownloader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.util.UUID

@HiltWorker
class PackDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val airportRepository: AirportRepository,
    private val landmarkDao: LandmarkDao,
    private val offlinePackDao: OfflinePackDao,
    private val tripDao: TripDao,
    private val pixabayApiClient: PixabayApiClient,
    private val pexelsApiClient: PexelsApiClient,
    private val wikimediaApiClient: WikimediaApiClient,
    private val aiStoryManager: AiStoryManager,
    private val geoCalculator: GeoCalculator,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pack_download"
        private const val CHANNEL_NAME = "Offline Pack Downloads"
    }

    override suspend fun doWork(): Result {
        try {
            val departure = inputData.getString("departure") ?: return Result.failure()
            val arrival = inputData.getString("arrival") ?: return Result.failure()
            val route = "$departure-$arrival"

            android.util.Log.d("PackDownloadWorker", "Starting download for route: $route")

            // Create notification channel
            createNotificationChannel()

            // Set foreground service with initial notification
            setForeground(createForegroundInfo(route, 0f, 0, 0))

            // Create pack record
            val packId = UUID.randomUUID().toString()
            val pack = OfflinePackEntity(
                id = packId,
                route = route,
                departure = departure,
                arrival = arrival,
                landmarkCount = 0,
                photoCount = 0,
                sizeBytes = 0L,
                status = "downloading",
                progress = 0f,
                downloadedAt = System.currentTimeMillis()
            )
            offlinePackDao.insert(pack)

            // Get landmarks along route
            val landmarks = getLandmarksAlongRoute(departure, arrival)
            android.util.Log.d("PackDownloadWorker", "Found ${landmarks.size} landmarks for route")

            if (landmarks.isEmpty()) {
                offlinePackDao.update(pack.copy(status = "failed"))
                return Result.failure()
            }

            var totalPhotos = 0
            var totalBytes = 0L

            // Download photos for each landmark
            landmarks.forEachIndexed { index, landmark ->
                android.util.Log.d("PackDownloadWorker", "Processing landmark ${index + 1}/${landmarks.size}: ${landmark.name}")

                // Try multi-source photo fetch
                val photoUrls = fetchPhotosMultiSource(landmark.name)

                if (photoUrls.isNotEmpty()) {
                    // Download photos to cache
                    val filePaths = PhotoDownloader.downloadMultiple(
                        photoUrls = photoUrls.take(2), // Download up to 2 photos
                        landmarkId = landmark.id,
                        context = context,
                        okHttpClient = okHttpClient
                    )

                    if (filePaths.isNotEmpty()) {
                        // Update landmark with photo file paths
                        val filesJson = JSONArray(filePaths).toString()
                        landmarkDao.updateLandmarkPhotoFiles(landmark.id, filesJson)

                        totalPhotos += filePaths.size

                        // Estimate file sizes
                        filePaths.forEach { path ->
                            totalBytes += java.io.File(path).length()
                        }
                    }
                }

                // Generate facts with AI (if not already present)
                if (landmark.generalFact == null || landmark.historicalFact == null) {
                    generateFactsForLandmark(landmark)
                }

                // Update progress
                val progress = (index + 1).toFloat() / landmarks.size
                setProgress(workDataOf("progress" to progress))
                setForeground(createForegroundInfo(route, progress, index + 1, landmarks.size))

                offlinePackDao.update(pack.copy(
                    progress = progress,
                    landmarkCount = landmarks.size,
                    photoCount = totalPhotos,
                    sizeBytes = totalBytes
                ))

                // Small delay to avoid rate limits
                delay(100)
            }

            // Mark as completed
            offlinePackDao.update(pack.copy(
                status = "completed",
                progress = 1f,
                landmarkCount = landmarks.size,
                photoCount = totalPhotos,
                sizeBytes = totalBytes
            ))

            // Create trip history entry
            createTripHistoryEntry(departure, arrival, packId, landmarks)

            // Show completion notification
            showCompletionNotification(route, landmarks.size, totalPhotos, totalBytes)

            android.util.Log.d("PackDownloadWorker", "Pack download completed: ${landmarks.size} landmarks, $totalPhotos photos")
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PackDownloadWorker", "Pack download failed", e)
            return Result.failure()
        }
    }

    private suspend fun getLandmarksAlongRoute(departure: String, arrival: String): List<com.skylens.data.local.entities.LandmarkEntity> {
        // Get airport coordinates
        val depAirport = airportRepository.getAirportByIataCode(departure) ?: return emptyList()
        val arrAirport = airportRepository.getAirportByIataCode(arrival) ?: return emptyList()

        // Generate route points using great circle
        val routePoints = geoCalculator.generateGreatCircleRoute(
            startLat = depAirport.latitude,
            startLon = depAirport.longitude,
            endLat = arrAirport.latitude,
            endLon = arrAirport.longitude,
            numPoints = 200
        )

        // Find landmarks near route
        val allLandmarks = landmarkDao.getAllLandmarksSync()
        val routeLandmarks = mutableSetOf<com.skylens.data.local.entities.LandmarkEntity>()

        for (point in routePoints) {
            val nearby = allLandmarks.filter { landmark ->
                val distance = geoCalculator.haversineDistance(
                    point.first, point.second,
                    landmark.latitude, landmark.longitude
                )
                distance <= 200.0 // 200km radius from route
            }
            routeLandmarks.addAll(nearby)
        }

        return routeLandmarks.sortedByDescending { it.importanceScore }.take(50)
    }

    private suspend fun fetchPhotosMultiSource(landmarkName: String): List<String> {
        // Try Pixabay first (100/min)
        val pixabayPhotos = pixabayApiClient.searchPhotos(landmarkName, 2)
        if (pixabayPhotos.isNotEmpty()) {
            android.util.Log.d("PackDownloadWorker", "Found ${pixabayPhotos.size} photos from Pixabay for $landmarkName")
            return pixabayPhotos.map { it.url }
        }

        // Fallback to Pexels (unlimited)
        val pexelsPhotos = pexelsApiClient.searchPhotos(landmarkName, 2)
        if (pexelsPhotos.isNotEmpty()) {
            android.util.Log.d("PackDownloadWorker", "Found ${pexelsPhotos.size} photos from Pexels for $landmarkName")
            return pexelsPhotos.map { it.url }
        }

        // Final fallback to Wikimedia
        val wikimediaPhoto = wikimediaApiClient.getPhotoUrl(landmarkName)
        if (wikimediaPhoto != null) {
            android.util.Log.d("PackDownloadWorker", "Found photo from Wikimedia for $landmarkName")
            return listOf(wikimediaPhoto)
        }

        android.util.Log.w("PackDownloadWorker", "No photos found for $landmarkName from any source")
        return emptyList()
    }

    private suspend fun generateFactsForLandmark(landmark: com.skylens.data.local.entities.LandmarkEntity) {
        try {
            // Generate general fact
            if (landmark.generalFact == null) {
                val generalFact = aiStoryManager.generateGeneralFact(landmark.name, landmark.type)
                if (generalFact != null) {
                    landmarkDao.updateLandmarkGeneralFact(landmark.id, generalFact)
                }
            }

            // Generate historical fact
            if (landmark.historicalFact == null) {
                val historicalFact = aiStoryManager.generateHistoricalFact(landmark.name, landmark.type)
                if (historicalFact != null) {
                    landmarkDao.updateLandmarkHistoricalFact(landmark.id, historicalFact)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PackDownloadWorker", "Failed to generate facts for ${landmark.name}", e)
        }
    }

    private fun createForegroundInfo(route: String, progress: Float, current: Int, total: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Downloading Offline Pack")
            .setContentText("$route - ${(progress * 100).toInt()}% complete")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun showCompletionNotification(route: String, landmarks: Int, photos: Int, sizeBytes: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val sizeMB = sizeBytes / (1024 * 1024)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("✅ Offline Pack Ready")
            .setContentText("$route downloaded • $landmarks landmarks • $photos photos • ${sizeMB}MB")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress of offline pack downloads"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun createTripHistoryEntry(departure: String, arrival: String, packId: String, landmarks: List<com.skylens.data.local.entities.LandmarkEntity>) {
        try {
            // Check if trip already exists
            val existingTrip = tripDao.getTripByRoute(departure, arrival)

            val tripId = existingTrip?.id ?: UUID.randomUUID().toString()

            // Get airport coordinates for route GeoJSON
            val depAirport = airportRepository.getAirportByIataCode(departure)
            val arrAirport = airportRepository.getAirportByIataCode(arrival)

            if (depAirport != null && arrAirport != null) {
                // Generate route GeoJSON
                val routePoints = geoCalculator.generateGreatCircleRoute(
                    startLat = depAirport.latitude,
                    startLon = depAirport.longitude,
                    endLat = arrAirport.latitude,
                    endLon = arrAirport.longitude,
                    numPoints = 100
                )

                val routeGeoJson = org.maplibre.geojson.LineString.fromLngLats(
                    routePoints.map { org.maplibre.geojson.Point.fromLngLat(it.second, it.first) }
                ).toJson()

                if (existingTrip == null) {
                    // Create new trip
                    val trip = TripEntity(
                        id = tripId,
                        userId = null,
                        departureAirport = departure,
                        arrivalAirport = arrival,
                        routeGeoJson = routeGeoJson,
                        startTime = null,
                        endTime = null,
                        createdAt = System.currentTimeMillis()
                    )
                    tripDao.insertTrip(trip)
                    android.util.Log.d("PackDownloadWorker", "Created new trip history entry: $departure → $arrival")
                } else {
                    android.util.Log.d("PackDownloadWorker", "Trip already exists: $departure → $arrival")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PackDownloadWorker", "Failed to create trip history entry", e)
        }
    }
}
