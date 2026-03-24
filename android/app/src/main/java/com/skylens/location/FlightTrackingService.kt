package com.skylens.location

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.skylens.app.R
import com.skylens.data.repository.LandmarkRepository
import com.skylens.data.repository.TripRepository
import com.skylens.geo.GeoCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class FlightTrackingService : Service() {

    @Inject
    lateinit var flightTracker: FlightTracker

    @Inject
    lateinit var landmarkRepository: LandmarkRepository

    @Inject
    lateinit var tripRepository: TripRepository

    @Inject
    lateinit var geoCalculator: GeoCalculator

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trackingJob: Job? = null
    private var currentTripId: String? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "flight_tracking_channel"
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        const val EXTRA_TRIP_ID = "trip_id"

        fun start(context: Context, tripId: String) {
            val intent = Intent(context, FlightTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_TRIP_ID, tripId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FlightTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                currentTripId = intent.getStringExtra(EXTRA_TRIP_ID)
                startForeground(NOTIFICATION_ID, createNotification())
                startTracking()
            }
            ACTION_STOP_TRACKING -> {
                stopTracking()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        trackingJob = serviceScope.launch {
            flightTracker.trackFlight().collect { position ->
                // Update notification with current position
                val notification = createNotification(
                    "Alt: ${position.altitude}ft, Speed: ${position.speed}km/h"
                )
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Log landmark sightings
                currentTripId?.let { tripId ->
                    val visibilityKm = geoCalculator.visibilityRadiusKm(position.altitude ?: 35000)
                    val nearbyLandmarks = landmarkRepository.getLandmarksNearPosition(
                        position.latitude,
                        position.longitude,
                        visibilityKm,
                        limit = 10
                    )

                    nearbyLandmarks.forEach { landmark ->
                        val distance = geoCalculator.haversineDistance(
                            position.latitude, position.longitude,
                            landmark.latitude, landmark.longitude
                        )
                        tripRepository.addTripEvent(tripId, landmark.id, distance.toFloat())
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        currentTripId?.let { tripId ->
            serviceScope.launch {
                tripRepository.getTripById(tripId)?.let { trip ->
                    tripRepository.saveTrip(trip.copy(endTime = System.currentTimeMillis()))
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flight Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows your current flight position"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String = "Tracking your flight..."): Notification {
        val stopIntent = Intent(this, FlightTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SkyLens Flight Tracking")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }
}
