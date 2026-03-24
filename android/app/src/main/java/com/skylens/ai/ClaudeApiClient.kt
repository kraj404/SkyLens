package com.skylens.ai

import kotlinx.coroutines.Dispatchers
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
class ClaudeApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private val apiKey = "sk-ant-placeholder" // TODO: Load from BuildConfig
    private val baseUrl = "https://api.anthropic.com/v1"
    private val model = "claude-haiku-4.5-20251001"

    /**
     * Generate a story for a landmark
     */
    suspend fun generateLandmarkStory(
        landmarkName: String,
        landmarkType: String,
        elevation: Int?,
        country: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey == "sk-ant-placeholder") {
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

            val response = callClaude(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.success(generateMockStory(landmarkName, landmarkType, elevation, country))
        }
    }

    private fun generateMockStory(name: String, type: String, elevation: Int?, country: String?): String {
        val elevText = elevation?.let { " standing at ${(it * 3.28084).toInt()} feet ($it meters)" } ?: ""
        val countryText = country?.let { " in $it" } ?: ""

        // Generate historically rich context based on landmark type
        val historicalContext = when (type.uppercase()) {
            "MOUNTAIN" -> "This majestic peak$elevText has been a sacred landmark for centuries. " +
                    "Ancient travelers used it as a navigation point, and local communities consider it culturally significant. " +
                    "Its distinctive profile is visible from aircraft cruising at 35,000 feet, offering passengers a spectacular view."

            "CITY" -> "This historic city$countryText has been a cultural and economic hub for over a millennium. " +
                    "From your airplane window, you can see its urban sprawl and architectural landmarks. " +
                    "The city has witnessed countless historical events and remains a vibrant center of human civilization."

            "MONUMENT", "HISTORICAL_SITE" -> "This UNESCO World Heritage site$countryText represents centuries of human achievement. " +
                    "Built during an era of great cultural flourishing, it stands as a testament to architectural mastery. " +
                    "Visible from the air, its unique structure has made it a landmark for pilots and travelers for generations."

            "VOLCANO" -> "This active volcano$elevText has shaped the landscape and history of the region for millennia. " +
                    "Its eruptions have both destroyed and created, enriching the soil and forming new land. " +
                    "From your aircraft, you can observe its crater and the patterns of ancient lava flows."

            "RIVER", "LAKE" -> "This vital waterway$countryText has sustained civilizations for thousands of years. " +
                    "It served as a major trade route connecting distant cultures and enabling the growth of cities along its banks. " +
                    "From above, its winding path through the landscape tells the story of geological time."

            "TEMPLE" -> "This sacred site$countryText has been a center of spiritual practice for centuries. " +
                    "Pilgrims have traveled great distances to visit its hallowed grounds. " +
                    "Its architecture reflects the artistic and religious traditions of its builders, visible even from high altitude."

            "GLACIER" -> "This ancient glacier$elevText contains ice that has been accumulating for tens of thousands of years. " +
                    "It serves as a natural archive of Earth's climate history. " +
                    "From the air, you can see its brilliant blue ice and the moraines carved by its slow movement."

            "DESERT" -> "This vast desert$countryText has challenged and shaped human societies for millennia. " +
                    "Ancient trade caravans crossed these sands, connecting distant civilizations. " +
                    "From your window, observe its sweeping dunes and the play of light on the endless terrain."

            "CANYON" -> "This dramatic gorge$elevText was carved over millions of years by water and wind erosion. " +
                    "Its layered rock walls reveal Earth's geological history like pages in a book. " +
                    "From aircraft altitude, the canyon's immense scale and colorful strata create an unforgettable view."

            "ISLAND" -> "This island$countryText has developed unique ecosystems and cultures in isolation. " +
                    "Its strategic location has made it important throughout maritime history. " +
                    "From the air, you can appreciate its geology and the contrast between land and surrounding waters."

            else -> "$name$countryText$elevText is a remarkable $type with deep historical and cultural significance. " +
                    "This landmark has guided travelers for centuries and remains an important geographic reference point. " +
                    "From your airplane window, you're witnessing a view that connects you to generations of explorers and adventurers."
        }

        return "$name$countryText is visible from your flight path. $historicalContext"
    }

    /**
     * Generate real-time flight narrator commentary
     */
    suspend fun generateFlightNarration(
        currentRegion: String,
        nearbyLandmarks: List<String>,
        altitude: Int,
        context: String = ""
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

            val response = callClaude(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Answer user questions about landmarks
     */
    suspend fun answerLandmarkQuestion(
        question: String,
        currentPosition: String,
        nearbyLandmarks: List<String>,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<JSONObject>()

            // Add system context
            val systemPrompt = buildString {
                append("You are an AI assistant helping airplane passengers. ")
                append("Current position: $currentPosition. ")
                if (nearbyLandmarks.isNotEmpty()) {
                    append("Visible landmarks: ${nearbyLandmarks.joinToString(", ")}. ")
                }
                append("Answer concisely and fascinatingly.")
            }

            // Add conversation history
            conversationHistory.forEach { (userMsg, assistantMsg) ->
                messages.add(JSONObject().apply {
                    put("role", "user")
                    put("content", userMsg)
                })
                messages.add(JSONObject().apply {
                    put("role", "assistant")
                    put("content", assistantMsg)
                })
            }

            // Add current question
            messages.add(JSONObject().apply {
                put("role", "user")
                put("content", question)
            })

            val response = callClaudeWithMessages(systemPrompt, messages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate trip summary after flight
     */
    suspend fun generateTripSummary(
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

            val response = callClaude(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate AI caption for landmark photo
     */
    suspend fun generatePhotoCaption(
        landmarkName: String,
        photoDescription: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Write a 30-word poetic caption for a photo of $landmarkName. $photoDescription"
            val response = callClaude(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate prediction context for upcoming landmark
     */
    suspend fun generatePredictionContext(
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

            val response = callClaude(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Core Claude API call method
     */
    private suspend fun callClaude(prompt: String): String {
        val requestBody = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1024)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val request = Request.Builder()
            .url("$baseUrl/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Claude API error: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from Claude API")

        val json = JSONObject(responseBody)
        val contentArray = json.getJSONArray("content")
        val textContent = contentArray.getJSONObject(0)

        return textContent.getString("text")
    }

    /**
     * Claude API call with conversation history
     */
    private suspend fun callClaudeWithMessages(
        systemPrompt: String,
        messages: List<JSONObject>
    ): String {
        val requestBody = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", JSONArray(messages))
        }

        val request = Request.Builder()
            .url("$baseUrl/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Claude API error: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from Claude API")

        val json = JSONObject(responseBody)
        val contentArray = json.getJSONArray("content")
        val textContent = contentArray.getJSONObject(0)

        return textContent.getString("text")
    }
}
