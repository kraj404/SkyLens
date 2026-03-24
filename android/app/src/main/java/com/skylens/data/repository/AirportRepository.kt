package com.skylens.data.repository

import com.skylens.data.local.dao.AirportDao
import com.skylens.data.local.entities.AirportEntity
import com.skylens.domain.model.Airport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AirportRepository @Inject constructor(
    private val airportDao: AirportDao
) {

    fun searchAirports(query: String, limit: Int = 20): Flow<List<Airport>> {
        return airportDao.searchAirports(query, limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getAirportByCode(iataCode: String): Airport? {
        return airportDao.getAirportByCode(iataCode)?.toDomainModel()
    }

    suspend fun getAirportByIataCode(iataCode: String): Airport? {
        return getAirportByCode(iataCode)
    }

    fun getAllAirports(): Flow<List<Airport>> {
        return airportDao.getAllAirports().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertAirports(airports: List<Airport>) {
        airportDao.insertAllAirports(airports.map { it.toEntity() })
    }

    suspend fun getAirportCount(): Int {
        return airportDao.getAirportCount()
    }

    private fun AirportEntity.toDomainModel() = Airport(
        iataCode = iataCode,
        icaoCode = icaoCode,
        name = name,
        city = city,
        country = country,
        latitude = latitude,
        longitude = longitude,
        elevationM = elevationM,
        timezone = timezone
    )

    private fun Airport.toEntity() = AirportEntity(
        iataCode = iataCode,
        icaoCode = icaoCode,
        name = name,
        city = city,
        country = country,
        latitude = latitude,
        longitude = longitude,
        elevationM = elevationM,
        timezone = timezone
    )
}
