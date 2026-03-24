package com.skylens.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skylens.geo.GeoCalculator
import com.skylens.location.FlightTracker
import com.skylens.location.LocationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideGeoCalculator(): GeoCalculator {
        return GeoCalculator
    }

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context
    ): LocationProvider {
        return LocationProvider(context)
    }

    @Provides
    @Singleton
    fun provideFlightTracker(
        locationProvider: LocationProvider,
        geoCalculator: GeoCalculator
    ): FlightTracker {
        return FlightTracker(locationProvider, geoCalculator)
    }
}
