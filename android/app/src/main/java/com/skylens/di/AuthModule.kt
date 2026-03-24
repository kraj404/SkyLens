package com.skylens.di

import android.content.Context
import com.skylens.auth.AuthRepository
import com.skylens.auth.GoogleSignInManager
import com.skylens.data.remote.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSupabaseClientProvider(): SupabaseClientProvider {
        return SupabaseClientProvider()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(
        @ApplicationContext context: Context
    ): GoogleSignInManager {
        return GoogleSignInManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        supabaseClient: SupabaseClientProvider,
        googleSignInManager: GoogleSignInManager
    ): AuthRepository {
        return AuthRepository(supabaseClient, googleSignInManager)
    }
}
