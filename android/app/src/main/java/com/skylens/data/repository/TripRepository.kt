package com.skylens.data.repository

import com.skylens.data.local.dao.TripDao
import com.skylens.data.local.entities.TripEntity
import com.skylens.data.local.entities.TripEventEntity
import com.skylens.domain.model.Trip
import com.skylens.domain.model.TripEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val landmarkRepository: LandmarkRepository
) {

    fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getTripById(tripId: String): Trip? {
        val tripEntity = tripDao.getTripById(tripId) ?: return null
        val events = tripDao.getTripEvents(tripId).map { it.toDomainModel() }
        return tripEntity.toDomainModel(events)
    }

    fun getTripsByUser(userId: String): Flow<List<Trip>> {
        return tripDao.getTripsByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun saveTrip(trip: Trip): String {
        val tripId = trip.id.ifEmpty { UUID.randomUUID().toString() }
        val tripEntity = trip.copy(id = tripId).toEntity()
        tripDao.insertTrip(tripEntity)

        trip.events.forEach { event ->
            val eventEntity = event.toEntity()
            tripDao.insertTripEvent(eventEntity)
        }

        return tripId
    }

    suspend fun addTripEvent(tripId: String, landmarkId: String, distanceKm: Float?) {
        val event = TripEventEntity(
            id = UUID.randomUUID().toString(),
            tripId = tripId,
            landmarkId = landmarkId,
            eventTime = System.currentTimeMillis(),
            distanceKm = distanceKm
        )
        tripDao.insertTripEvent(event)
    }

    suspend fun deleteTrip(tripId: String) {
        tripDao.deleteTrip(tripId)
    }

    /**
     * Export trip as GeoJSON format
     * Returns GeoJSON string containing route and landmark waypoints
     */
    suspend fun exportTripAsGeoJson(tripId: String): String? {
        val trip = getTripById(tripId) ?: return null

        // Build GeoJSON FeatureCollection
        val geojson = buildString {
            append("""{"type":"FeatureCollection","features":[""")

            // Add route as LineString
            append("""{"type":"Feature","properties":{"type":"route","departure":"${trip.departureAirport}","arrival":"${trip.arrivalAirport}","startTime":${trip.startTime},"endTime":${trip.endTime ?: "null"}},"geometry":""")
            append(trip.routeGeoJson)
            append("}")

            // Add each landmark event as Point
            trip.events.forEachIndexed { index, event ->
                // Fetch landmark details
                val landmark = landmarkRepository.getLandmarkById(event.landmarkId)
                if (landmark != null) {
                    append(""",{"type":"Feature","properties":{"type":"landmark","name":"${landmark.name}","landmarkType":"${landmark.type.name}","eventTime":${event.eventTime},"distance":${event.distanceKm ?: "null"}},"geometry":{"type":"Point","coordinates":[${landmark.longitude},${landmark.latitude}]}}""")
                }
            }

            append("]}")
        }

        return geojson
    }

    private fun TripEntity.toDomainModel(events: List<TripEvent> = emptyList()) = Trip(
        id = id,
        userId = userId,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        routeGeoJson = routeGeoJson,
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt,
        events = events
    )

    private fun Trip.toEntity() = TripEntity(
        id = id,
        userId = userId,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        routeGeoJson = routeGeoJson,
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt
    )

    private fun TripEventEntity.toDomainModel() = TripEvent(
        id = id,
        tripId = tripId,
        landmarkId = landmarkId,
        eventTime = eventTime,
        distanceKm = distanceKm,
        createdAt = createdAt
    )

    private fun TripEvent.toEntity() = TripEventEntity(
        id = id,
        tripId = tripId,
        landmarkId = landmarkId,
        eventTime = eventTime,
        distanceKm = distanceKm,
        createdAt = createdAt
    )
}
