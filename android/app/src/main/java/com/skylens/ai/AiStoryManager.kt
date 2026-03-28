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
            android.util.Log.d("AiStoryManager", "getFlightNarration called - region: $currentRegion, landmarks: ${nearbyLandmarks.size}, altitude: $altitude")

            // Check rate limits
            if (!aiRateLimiter.checkAndConsume("narration")) {
                val fallback = "You're flying over $currentRegion at $altitude feet. ${nearbyLandmarks.firstOrNull() ?: "Beautiful views"} nearby."
                android.util.Log.d("AiStoryManager", "Rate limited - returning fallback: $fallback")
                return@withContext Result.success(fallback)
            }

            android.util.Log.d("AiStoryManager", "Rate limit OK - calling AI provider")
            val provider = getActiveProvider()
            val result = provider.generateFlightNarration(
                currentRegion, nearbyLandmarks, altitude, context
            )

            if (result.isSuccess) {
                android.util.Log.d("AiStoryManager", "AI provider returned: ${result.getOrNull()?.take(50)}...")
                // Record usage (estimate: ~150 input + ~80 output)
                aiRateLimiter.recordUsage(
                    requestType = "narration",
                    inputTokens = 150,
                    outputTokens = 80,
                    success = true
                )
                return@withContext result
            } else {
                // API failed - return fallback instead of propagating error
                val fallback = "You're flying over $currentRegion at $altitude feet. ${nearbyLandmarks.joinToString(", ")} visible from your window."
                android.util.Log.w("AiStoryManager", "AI provider failed, using fallback: ${result.exceptionOrNull()?.message}")
                return@withContext Result.success(fallback)
            }
        } catch (e: Exception) {
            android.util.Log.e("AiStoryManager", "getFlightNarration exception", e)
            // Return fallback on any exception
            val fallback = "You're flying over $currentRegion at $altitude feet. Enjoy the spectacular views!"
            return@withContext Result.success(fallback)
        }
    }

    /**
     * Answer general questions about landmarks and geography
     */
    suspend fun answerQuestion(
        question: String,
        currentPosition: String,
        nearbyLandmarks: List<String>,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AiStoryManager", "answerQuestion called: $question")

            // Check rate limits
            if (!aiRateLimiter.checkAndConsume("chat")) {
                val fallback = "I'm here to help answer questions about landmarks and geography. Please ask about specific locations, mountains, cities, or what you can see from your flight!"
                android.util.Log.d("AiStoryManager", "Rate limited - returning fallback")
                return@withContext Result.success(fallback)
            }

            android.util.Log.d("AiStoryManager", "Rate limit OK - calling AI provider")
            val provider = getActiveProvider()
            val result = provider.answerLandmarkQuestion(
                question = question,
                currentPosition = currentPosition,
                nearbyLandmarks = nearbyLandmarks,
                conversationHistory = conversationHistory
            )

            if (result.isSuccess) {
                android.util.Log.d("AiStoryManager", "AI provider returned answer: ${result.getOrNull()?.take(50)}...")
                // Record usage
                aiRateLimiter.recordUsage(
                    requestType = "chat",
                    inputTokens = 200,
                    outputTokens = 150,
                    success = true
                )
                return@withContext result
            } else {
                // API failed - return helpful fallback
                val fallback = "I can help you learn about landmarks and geography! Try asking about specific places, mountains, cities, or what's visible from your current location."
                android.util.Log.w("AiStoryManager", "AI provider failed, using fallback: ${result.exceptionOrNull()?.message}")
                return@withContext Result.success(fallback)
            }
        } catch (e: Exception) {
            android.util.Log.e("AiStoryManager", "answerQuestion exception", e)
            val fallback = "I'm here to answer questions about landmarks and geography during your flight!"
            return@withContext Result.success(fallback)
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

    /**
     * Generate general fact for a landmark
     */
    suspend fun generateGeneralFact(landmarkName: String, landmarkType: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!aiRateLimiter.checkAndConsume("fact")) {
                return@withContext null
            }

            val provider = getActiveProvider()
            val result = provider.generateGeneralFact(landmarkName, landmarkType)

            if (result.isSuccess) {
                aiRateLimiter.recordUsage(
                    requestType = "fact",
                    inputTokens = 50,
                    outputTokens = 80,
                    success = true
                )
                result.getOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AiStoryManager", "Failed to generate general fact", e)
            null
        }
    }

    /**
     * Generate historical fact for a landmark
     */
    suspend fun generateHistoricalFact(landmarkName: String, landmarkType: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!aiRateLimiter.checkAndConsume("fact")) {
                return@withContext null
            }

            val provider = getActiveProvider()
            val result = provider.generateHistoricalFact(landmarkName, landmarkType)

            if (result.isSuccess) {
                aiRateLimiter.recordUsage(
                    requestType = "fact",
                    inputTokens = 50,
                    outputTokens = 80,
                    success = true
                )
                result.getOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AiStoryManager", "Failed to generate historical fact", e)
            null
        }
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
