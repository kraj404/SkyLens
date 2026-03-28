package com.skylens.ai

/**
 * Abstraction for AI service providers (Claude, Gemini, etc.)
 */
interface AiProvider {

    /**
     * Generate a story for a landmark
     */
    suspend fun generateLandmarkStory(
        landmarkName: String,
        landmarkType: String,
        elevation: Int?,
        country: String?
    ): Result<String>

    /**
     * Generate real-time flight narrator commentary
     */
    suspend fun generateFlightNarration(
        currentRegion: String,
        nearbyLandmarks: List<String>,
        altitude: Int,
        context: String
    ): Result<String>

    /**
     * Answer user questions about landmarks
     */
    suspend fun answerLandmarkQuestion(
        question: String,
        currentPosition: String,
        nearbyLandmarks: List<String>,
        conversationHistory: List<Pair<String, String>>
    ): Result<String>

    /**
     * Generate trip summary after flight
     */
    suspend fun generateTripSummary(
        departureAirport: String,
        arrivalAirport: String,
        landmarksSeen: List<String>,
        durationHours: Double,
        distanceKm: Double
    ): Result<String>

    /**
     * Generate AI caption for landmark photo
     */
    suspend fun generatePhotoCaption(
        landmarkName: String,
        photoDescription: String
    ): Result<String>

    /**
     * Generate prediction context for upcoming landmark
     */
    suspend fun generatePredictionContext(
        landmarkName: String,
        landmarkType: String,
        minutesUntilVisible: Int
    ): Result<String>

    /**
     * Generate general fact about a landmark
     */
    suspend fun generateGeneralFact(
        landmarkName: String,
        landmarkType: String
    ): Result<String>

    /**
     * Generate historical fact about a landmark
     */
    suspend fun generateHistoricalFact(
        landmarkName: String,
        landmarkType: String
    ): Result<String>

    /**
     * Get provider name for display
     */
    fun getProviderName(): String
}

enum class AiProviderType {
    CLAUDE,
    GEMINI
}
