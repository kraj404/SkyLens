package com.skylens.data.remote

import com.skylens.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PexelsApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val baseUrl = "https://api.pexels.com/v1"
    private val apiKey = BuildConfig.PEXELS_API_KEY

    data class PexelsPhoto(
        val url: String,
        val thumbnailUrl: String,
        val photographer: String
    )

    /**
     * Search for photos on Pexels
     * Rate limit: Unlimited (but recommended to keep reasonable)
     * Returns list of photo URLs
     */
    suspend fun searchPhotos(query: String, perPage: Int = 5): List<PexelsPhoto> = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append(baseUrl)
                append("/search")
                append("?query=").append(query.replace(" ", "+"))
                append("&per_page=$perPage")
                append("&orientation=landscape")
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("PexelsApi", "Request failed: ${response.code}")
                return@withContext emptyList()
            }

            val json = JSONObject(response.body?.string() ?: return@withContext emptyList())
            val photos = json.getJSONArray("photos")

            val results = mutableListOf<PexelsPhoto>()
            for (i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                val src = photo.getJSONObject("src")
                results.add(
                    PexelsPhoto(
                        url = src.getString("large"),
                        thumbnailUrl = src.getString("medium"),
                        photographer = photo.getString("photographer")
                    )
                )
            }

            android.util.Log.d("PexelsApi", "Found ${results.size} photos for '$query'")
            results
        } catch (e: Exception) {
            android.util.Log.e("PexelsApi", "Error searching photos for '$query'", e)
            emptyList()
        }
    }
}
