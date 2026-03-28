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
class PixabayApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val baseUrl = "https://pixabay.com/api/"
    private val apiKey = BuildConfig.PIXABAY_API_KEY

    data class PixabayPhoto(
        val url: String,
        val thumbnailUrl: String,
        val tags: String
    )

    /**
     * Search for photos on Pixabay
     * Rate limit: 100 requests/minute
     * Returns list of photo URLs
     */
    suspend fun searchPhotos(query: String, perPage: Int = 5): List<PixabayPhoto> = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append(baseUrl)
                append("?key=$apiKey")
                append("&q=").append(query.replace(" ", "+"))
                append("&image_type=photo")
                append("&per_page=$perPage")
                append("&safesearch=true")
                append("&orientation=horizontal")
            }

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("PixabayApi", "Request failed: ${response.code}")
                return@withContext emptyList()
            }

            val json = JSONObject(response.body?.string() ?: return@withContext emptyList())
            val hits = json.getJSONArray("hits")

            val photos = mutableListOf<PixabayPhoto>()
            for (i in 0 until hits.length()) {
                val hit = hits.getJSONObject(i)
                photos.add(
                    PixabayPhoto(
                        url = hit.getString("largeImageURL"),
                        thumbnailUrl = hit.getString("webformatURL"),
                        tags = hit.getString("tags")
                    )
                )
            }

            android.util.Log.d("PixabayApi", "Found ${photos.size} photos for '$query'")
            photos
        } catch (e: Exception) {
            android.util.Log.e("PixabayApi", "Error searching photos for '$query'", e)
            emptyList()
        }
    }
}
