package com.skylens.ai

import com.skylens.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) : AiProvider {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "gemini-2.0-flash"
    private val maxRetries = 3

    override fun getProviderName(): String = "Gemini"

    override suspend fun generateLandmarkStory(
        landmarkName: String,
        landmarkType: String,
        elevation: Int?,
        country: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey == "your-gemini-api-key" || apiKey.isBlank()) {
                return@withContext Result.success(generateMockStory(landmarkName, landmarkType, elevation, country))
            }

            val prompt = buildString {
                append("Write a captivating 150-word story about $landmarkName")
                if (country != null) append(" in $country")
                append(". ")
                if (elevation != null) append("It stands at $elevation meters elevation. ")
                append("Focus on what makes it interesting to see from an airplane window. ")
                append("Include historical, cultural, and geological significance.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.success(generateMockStory(landmarkName, landmarkType, elevation, country))
        }
    }

    override suspend fun generateFlightNarration(
        currentRegion: String,
        nearbyLandmarks: List<String>,
        altitude: Int,
        context: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("You are an AI flight narrator. ")
                append("The passenger is flying at $altitude feet over $currentRegion. ")
                if (nearbyLandmarks.isNotEmpty()) {
                    append("Nearby landmarks: ${nearbyLandmarks.joinToString(", ")}. ")
                }
                if (context.isNotEmpty()) {
                    append("Additional context: $context. ")
                }
                append("Provide a 50-word engaging commentary about what they're seeing below. ")
                append("Be conversational and fascinating.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun answerLandmarkQuestion(
        question: String,
        currentPosition: String,
        nearbyLandmarks: List<String>,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = buildString {
                append("You are an AI assistant helping airplane passengers. ")
                append("Current position: $currentPosition. ")
                if (nearbyLandmarks.isNotEmpty()) {
                    append("Visible landmarks: ${nearbyLandmarks.joinToString(", ")}. ")
                }
                append("Answer concisely and fascinatingly.\n\n")

                // Add conversation history
                conversationHistory.forEach { (userMsg, assistantMsg) ->
                    append("User: $userMsg\n")
                    append("Assistant: $assistantMsg\n")
                }

                append("User: $question")
            }

            val response = callGemini(fullPrompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateTripSummary(
        departureAirport: String,
        arrivalAirport: String,
        landmarksSeen: List<String>,
        durationHours: Double,
        distanceKm: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Generate an engaging trip summary for a flight from $departureAirport to $arrivalAirport. ")
                append("Duration: ${String.format("%.1f", durationHours)} hours, ")
                append("Distance: ${String.format("%.0f", distanceKm)} km. ")
                append("Landmarks seen: ${landmarksSeen.joinToString(", ")}. ")
                append("Create a 200-word summary highlighting the journey's most fascinating aspects. ")
                append("Include geographic, cultural, and historical insights. ")
                append("Make it feel like an adventure story.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generatePhotoCaption(
        landmarkName: String,
        photoDescription: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Write a 30-word poetic caption for a photo of $landmarkName. $photoDescription"
            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generatePredictionContext(
        landmarkName: String,
        landmarkType: String,
        minutesUntilVisible: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("$landmarkName (a $landmarkType) will be visible in $minutesUntilVisible minutes. ")
                append("Write a 40-word teaser about why it's worth watching for. ")
                append("Be exciting and specific.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate general fact about a landmark
     */
    override suspend fun generateGeneralFact(
        landmarkName: String,
        landmarkType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Write a single interesting fact about $landmarkName (a $landmarkType). ")
                append("Keep it to 50 words. ")
                append("Focus on geography, formation, or current significance. ")
                append("Do not include historical information.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate historical fact about a landmark
     */
    override suspend fun generateHistoricalFact(
        landmarkName: String,
        landmarkType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Write a single historical fact about $landmarkName (a $landmarkType). ")
                append("Keep it to 50 words. ")
                append("Focus on historical events, cultural significance, or ancient usage. ")
                append("Be specific with dates or periods when possible.")
            }

            val response = callGemini(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Core Gemini API call method with retry logic
     */
    private suspend fun callGemini(prompt: String): String {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("$baseUrl/models/$model:generateContent?key=$apiKey")
                    .addHeader("content-type", "application/json")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                        ?: throw Exception("Empty response from Gemini API")

                    val json = JSONObject(responseBody)
                    val candidates = json.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val textPart = parts.getJSONObject(0)

                    return textPart.getString("text")
                }

                // Handle rate limiting and server errors with retry
                if (response.code in listOf(429, 500, 503)) {
                    lastException = Exception("Gemini API error: ${response.code}")
                    if (attempt < maxRetries - 1) {
                        val delayMs = 1000L * (1 shl attempt)
                        delay(delayMs)
                        return@repeat
                    }
                }

                throw Exception("Gemini API error: ${response.code}")
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val delayMs = 1000L * (1 shl attempt)
                    delay(delayMs)
                } else {
                    throw e
                }
            }
        }

        throw lastException ?: Exception("Unknown error")
    }

    private fun generateMockStory(name: String, type: String, elevation: Int?, country: String?): String {
        val elevText = elevation?.let { " standing at ${(it * 3.28084).toInt()} feet ($it meters)" } ?: ""
        val countryText = country?.let { " in $it" } ?: ""

        return when (type.uppercase()) {
            "MOUNTAIN" -> "This majestic peak$elevText has been a sacred landmark for centuries. " +
                    "Ancient travelers used it as a navigation point, and local communities consider it culturally significant. " +
                    "Its distinctive profile is visible from aircraft cruising at 35,000 feet, offering passengers a spectacular view."
            "CITY" -> "This historic city$countryText has been a cultural and economic hub for over a millennium. " +
                    "From your airplane window, you can see its urban sprawl and architectural landmarks."
            else -> "$name$countryText$elevText is a remarkable $type with deep historical and cultural significance."
        }
    }
}
