package com.skylens.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID

object PhotoDownloader {

    /**
     * Download an image from URL and save to cache directory
     * @param photoUrl The URL of the photo to download
     * @param landmarkId The landmark ID to organize files
     * @param context Android context
     * @return File path if successful, null otherwise
     */
    suspend fun downloadAndStore(
        photoUrl: String,
        landmarkId: String,
        context: Context,
        okHttpClient: OkHttpClient
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Download image bytes
            val request = Request.Builder()
                .url(photoUrl)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("PhotoDownloader", "Download failed: ${response.code}")
                return@withContext null
            }

            val imageBytes = response.body?.bytes() ?: return@withContext null

            // Create directory structure: cache/photos/{landmarkId}/
            val photoDir = File(context.cacheDir, "photos/$landmarkId")
            photoDir.mkdirs()

            // Generate unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(photoDir, fileName)

            // Write bytes to file
            file.writeBytes(imageBytes)

            android.util.Log.d("PhotoDownloader", "Downloaded photo to ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("PhotoDownloader", "Error downloading photo from $photoUrl", e)
            null
        }
    }

    /**
     * Download multiple photos for a landmark
     * Returns list of local file paths
     */
    suspend fun downloadMultiple(
        photoUrls: List<String>,
        landmarkId: String,
        context: Context,
        okHttpClient: OkHttpClient
    ): List<String> = withContext(Dispatchers.IO) {
        val filePaths = mutableListOf<String>()

        photoUrls.forEach { url ->
            downloadAndStore(url, landmarkId, context, okHttpClient)?.let { path ->
                filePaths.add(path)
            }
        }

        filePaths
    }

    /**
     * Check if photo exists in cache
     */
    fun hasPhotos(landmarkId: String, context: Context): Boolean {
        val photoDir = File(context.cacheDir, "photos/$landmarkId")
        return photoDir.exists() && photoDir.listFiles()?.isNotEmpty() == true
    }

    /**
     * Get cached photo files for a landmark
     */
    fun getCachedPhotos(landmarkId: String, context: Context): List<File> {
        val photoDir = File(context.cacheDir, "photos/$landmarkId")
        return photoDir.listFiles()?.toList() ?: emptyList()
    }
}
