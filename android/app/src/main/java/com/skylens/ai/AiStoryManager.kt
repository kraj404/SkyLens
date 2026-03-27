package com.skylens.ai

import com.skylens.data.local.dao.AiUsageDao
import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.entities.LandmarkEntity
import com.skylens.data.preferences.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiStoryManager @Inject constructor(
    private val claudeApiClient: ClaudeApiClient,
    private val geminiApiClient: GeminiApiClient,
    private val landmarkDao: LandmarkDao,
    private val aiRateLimiter: AiRateLimiter,
    private val aiUsageDao: AiUsageDao,
    private val settingsDataStore: SettingsDataStore
) {

    private suspend fun getActiveProvider(): AiProvider {
        return when (settingsDataStore.getAiProvider()) {
            AiProviderType.CLAUDE -> claudeApiClient
            AiProviderType.GEMINI -> geminiApiClient
        }
    }

    /**
     * Get story for landmark with cache-first strategy
     * Returns cached story if available, generates new one if not
     */
    suspend fun getStoryForLandmark(landmarkId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val landmark = landmarkDao.getLandmarkById(landmarkId)
            if (landmark?.aiStory != null && landmark.aiStory.isNotBlank()) {
                return@withContext Result.success(landmark.aiStory)
            }

            // Check rate limits
            if (!aiRateLimiter.checkAndConsume("story")) {
                // Return mock story if rate limited
                return@withContext Result.success(
                    generateFallbackStory(landmark)
                )
            }

            // Generate new story
            val provider = getActiveProvider()
            val result = provider.generateLandmarkStory(
                landmarkName = landmark?.name ?: "Unknown",
                landmarkType = landmark?.type?.toString() ?: "LANDMARK",
                elevation = landmark?.elevationM,
                country = landmark?.country
            )

            if (result.isSuccess) {
                val story = result.getOrNull()!!

                // Cache in database
                landmark?.let {
                    landmarkDao.updateLandmarkStory(landmarkId, story)
                }

                // Record usage (estimate tokens: ~200 input + ~200 output)
                aiRateLimiter.recordUsage(
                    requestType = "story",
                    inputTokens = 200,
                    outputTokens = 200,
                    success = true
                )

                Result.success(story)
            } else {
                Result.success(generateFallbackStory(landmark))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate prediction context with rate limiting
     */
    suspend fun getPredictionContext(
        landmarkName: String,
        landmarkType: String,
        minutesUntilVisible: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check rate limits
            if (!aiRateLimiter.checkAndConsume("prediction")) {
                return@withContext Result.success(
                    "Get ready! $landmarkName will be visible soon on your right."
                )
            }

            val provider = getActiveProvider()
            val result = provider.generatePredictionContext(
                landmarkName, landmarkType, minutesUntilVisible
            )

            if (result.isSuccess) {
                // Record usage (estimate: ~100 input + ~60 output)
                aiRateLimiter.recordUsage(
                    requestType = "prediction",
                    inputTokens = 100,
                    outputTokens = 60,
                    success = true
                )
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate flight narration with rate limiting
     */
    suspend fun getFlightNarration(
        currentRegion: String,
        nearbyLandmarks: List<String>,
        altitude: Int,
        context: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check rate limits
            if (!aiRateLimiter.checkAndConsume("narration")) {
                return@withContext Result.success(
                    "You're flying over $currentRegion at $altitude feet. ${nearbyLandmarks.firstOrNull() ?: "Beautiful views"} nearby."
                )
            }

            val provider = getActiveProvider()
            val result = provider.generateFlightNarration(
                currentRegion, nearbyLandmarks, altitude, context
            )

            if (result.isSuccess) {
                // Record usage (estimate: ~150 input + ~80 output)
                aiRateLimiter.recordUsage(
                    requestType = "narration",
                    inputTokens = 150,
                    outputTokens = 80,
                    success = true
                )
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get usage statistics for display
     */
    suspend fun getUsageStats(): UsageStats = withContext(Dispatchers.IO) {
        val startOfDay = getStartOfDayMillis()
        val todayRequests = aiUsageDao.getTotalRequestsToday(startOfDay)
        val todayCost = aiUsageDao.getTotalCostToday(startOfDay) ?: 0f
        val totalCost = aiUsageDao.getTotalCostAllTime() ?: 0f
        val remainingBudget = aiRateLimiter.getRemainingBudgetToday()

        UsageStats(
            requestsToday = todayRequests,
            costToday = todayCost,
            costAllTime = totalCost,
            remainingBudget = remainingBudget
        )
    }

    private fun generateFallbackStory(landmark: LandmarkEntity?): String {
        return landmark?.let {
            "${it.name} is a remarkable ${it.type.toString().lowercase()} ${it.country?.let { c -> "in $c " } ?: ""}with deep historical and cultural significance."
        } ?: "This landmark has guided travelers for centuries."
    }

    private fun getStartOfDayMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

data class UsageStats(
    val requestsToday: Int,
    val costToday: Float,
    val costAllTime: Float,
    val remainingBudget: Float
)
