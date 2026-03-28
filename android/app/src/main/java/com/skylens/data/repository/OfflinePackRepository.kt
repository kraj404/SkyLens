package com.skylens.data.repository

import com.skylens.data.local.dao.OfflinePackDao
import com.skylens.data.local.entities.OfflinePackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class OfflinePack(
    val routeKey: String,
    val departureAirport: String,
    val arrivalAirport: String,
    val packSizeMB: Int,
    val landmarkCount: Int,
    val photoCount: Int,
    val downloadedAt: Long?,
    val status: String,
    val progress: Float
)

@Singleton
class OfflinePackRepository @Inject constructor(
    private val offlinePackDao: OfflinePackDao
) {

    suspend fun getOfflinePack(departure: String, arrival: String): OfflinePack? = withContext(Dispatchers.IO) {
        val routeKey = "$departure-$arrival"
        offlinePackDao.getPackByRoute(routeKey)?.toDomainModel()
    }

    fun observeOfflinePack(departure: String, arrival: String): Flow<OfflinePack?> {
        val routeKey = "$departure-$arrival"
        return offlinePackDao.observePackByRoute(routeKey).map { it?.toDomainModel() }
    }

    suspend fun getAllDownloadedPacks(): List<OfflinePack> = withContext(Dispatchers.IO) {
        offlinePackDao.getCompletedPacks().map { it.toDomainModel() }
    }

    suspend fun deleteOfflinePack(routeKey: String) = withContext(Dispatchers.IO) {
        offlinePackDao.deletePackByRoute(routeKey)
        // TODO: Delete cached photo files from filesystem
    }

    private fun OfflinePackEntity.toDomainModel() = OfflinePack(
        routeKey = route,
        departureAirport = departure,
        arrivalAirport = arrival,
        packSizeMB = (sizeBytes / (1024 * 1024)).toInt(),
        landmarkCount = landmarkCount,
        photoCount = photoCount,
        downloadedAt = downloadedAt,
        status = status,
        progress = progress
    )
}
