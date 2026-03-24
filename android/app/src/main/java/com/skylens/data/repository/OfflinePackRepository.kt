package com.skylens.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class OfflinePack(
    val routeKey: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val packSizeMB: Int,
    val landmarkCount: Int,
    val tileCount: Int,
    val downloadedAt: Long?
)

@Singleton
class OfflinePackRepository @Inject constructor(
    // TODO: Inject DAO and API client
) {

    private val downloadedPacks = mutableMapOf<String, OfflinePack>()

    suspend fun getOfflinePack(departure: String, arrival: String): OfflinePack? = withContext(Dispatchers.IO) {
        val routeKey = "$departure-$arrival"
        downloadedPacks[routeKey]
    }

    suspend fun markPackDownloaded(departure: String, arrival: String) = withContext(Dispatchers.IO) {
        val routeKey = "$departure-$arrival"
        downloadedPacks[routeKey] = OfflinePack(
            routeKey = routeKey,
            departureAirport = departure,
            arrivalAirport = arrival,
            packSizeMB = 487,
            landmarkCount = 2134,
            tileCount = 8543,
            downloadedAt = System.currentTimeMillis()
        )
    }

    suspend fun getAllDownloadedPacks(): List<OfflinePack> = withContext(Dispatchers.IO) {
        downloadedPacks.values.toList()
    }

    suspend fun deleteOfflinePack(routeKey: String) = withContext(Dispatchers.IO) {
        downloadedPacks.remove(routeKey)
        // TODO: Delete from Room database and local storage
    }
}
