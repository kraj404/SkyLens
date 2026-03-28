package com.skylens.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WikimediaApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val baseUrl = "https://en.wikipedia.org/w/api.php"
    private val userAgent = "SkyLens/1.0 (Android; contact: developer@skylens.app)"

    /**
     * Fetch photo URL for a landmark from Wikimedia Commons
     * Returns null if no photo found
     */
    suspend fun getPhotoUrl(landmarkName: String): String? = withContext(Dispatchers.IO) {
        try {
            // Search for Wikipedia page
            val pageId = searchWikipediaPage(landmarkName) ?: return@withContext null

            // Get main image directly from page (returns full URL now)
            getPageMainImage(pageId)
        } catch (e: Exception) {
            android.util.Log.e("WikimediaApi", "Error fetching photo for $landmarkName", e)
            null
        }
    }

    private suspend fun searchWikipediaPage(query: String): Int? {
        val url = buildString {
            append(baseUrl)
            append("?action=query")
            append("&format=json")
            append("&list=search")
            append("&srsearch=").append(query.replace(" ", "+"))
            append("&srlimit=1")
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", userAgent)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val json = JSONObject(response.body?.string() ?: return null)
        val searchResults = json.getJSONObject("query").getJSONArray("search")

        return if (searchResults.length() > 0) {
            searchResults.getJSONObject(0).getInt("pageid")
        } else {
            null
        }
    }

    private suspend fun getPageMainImage(pageId: Int): String? {
        val url = buildString {
            append(baseUrl)
            append("?action=query")
            append("&format=json")
            append("&pageids=$pageId")
            append("&prop=pageimages")
            append("&pithumbsize=800")
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", userAgent)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val json = JSONObject(response.body?.string() ?: return null)
        val pages = json.getJSONObject("query").getJSONObject("pages")
        val pageData = pages.getJSONObject(pageId.toString())

        // Return thumbnail source directly
        return if (pageData.has("thumbnail")) {
            pageData.getJSONObject("thumbnail").getString("source")
        } else {
            null
        }
    }

    // Remove unused getImageUrl method - not needed since thumbnail has direct URL

    /**
     * Batch fetch photos for multiple landmarks
     * Returns map of landmark name to photo URL
     */
    suspend fun batchGetPhotos(landmarkNames: List<String>): Map<String, String> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, String>()

        landmarkNames.forEach { name ->
            getPhotoUrl(name)?.let { url ->
                results[name] = url
            }
        }

        results
    }

    /**
     * Fetch Wikipedia article intro for facts
     * Returns first paragraph of Wikipedia article
     */
    suspend fun getArticleIntro(landmarkName: String): String? = withContext(Dispatchers.IO) {
        try {
            val pageId = searchWikipediaPage(landmarkName) ?: return@withContext null

            val url = buildString {
                append(baseUrl)
                append("?action=query")
                append("&format=json")
                append("&pageids=$pageId")
                append("&prop=extracts")
                append("&exintro=true")
                append("&explaintext=true")
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", userAgent)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val json = JSONObject(response.body?.string() ?: return@withContext null)
            val pages = json.getJSONObject("query").getJSONObject("pages")
            val pageData = pages.getJSONObject(pageId.toString())

            if (pageData.has("extract")) {
                val extract = pageData.getString("extract")
                // Return first paragraph (up to 300 chars)
                val firstParagraph = extract.split("\n").firstOrNull() ?: extract
                if (firstParagraph.length > 300) {
                    firstParagraph.take(300) + "..."
                } else {
                    firstParagraph
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("WikimediaApi", "Error fetching article intro for $landmarkName", e)
            null
        }
    }
}
