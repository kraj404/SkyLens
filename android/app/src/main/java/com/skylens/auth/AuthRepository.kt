package com.skylens.auth

import com.skylens.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val googleSignInManager: GoogleSignInManager
) {

    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(webClientId: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            // Get Google ID token
            val idTokenResult = googleSignInManager.signIn(webClientId)
            if (idTokenResult.isFailure) {
                return@withContext Result.failure(
                    idTokenResult.exceptionOrNull() ?: Exception("Google Sign-In failed")
                )
            }

            val token = idTokenResult.getOrNull()!!

            // Authenticate with Supabase
            supabaseClient.client.auth.signInWith(IDToken) {
                idToken = token
                provider = Google
            }

            val user = supabaseClient.client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("No user after sign-in"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            supabaseClient.client.auth.signOut()
        } catch (e: Exception) {
            // Ignore errors on sign out
        }
    }

    /**
     * Get current user
     */
    suspend fun getCurrentUser(): UserInfo? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        try {
            supabaseClient.client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get access token for API calls
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            supabaseClient.client.auth.currentAccessTokenOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Create anonymous user for skip sign-in
     */
    suspend fun createAnonymousUser(): UserInfo = withContext(Dispatchers.IO) {
        // Return a minimal UserInfo for anonymous mode
        // This is a workaround since UserInfo constructor is complex
        // In reality, the app will check for null user and work offline
        getCurrentUser() ?: throw Exception("No user available - app will run in offline mode")
    }
}
