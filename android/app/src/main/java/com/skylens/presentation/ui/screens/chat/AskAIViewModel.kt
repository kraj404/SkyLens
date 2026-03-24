package com.skylens.presentation.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.ai.ClaudeApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AskAIViewModel @Inject constructor(
    private val claudeApiClient: ClaudeApiClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val conversationHistory = mutableListOf<Pair<String, String>>()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message immediately
        val userMessage = ChatMessage(
            text = text,
            isUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                // Call Claude API for real response
                val result = claudeApiClient.answerLandmarkQuestion(
                    question = text,
                    currentPosition = "In flight", // TODO: Pass actual position
                    nearbyLandmarks = emptyList(), // TODO: Pass nearby landmarks
                    conversationHistory = conversationHistory
                )

                if (result.isSuccess) {
                    val aiResponse = result.getOrNull()!!

                    // Add to conversation history
                    conversationHistory.add(text to aiResponse)

                    val aiMessage = ChatMessage(
                        text = aiResponse,
                        isUser = false
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                } else {
                    // Fallback to mock response if API fails
                    val mockResponse = generateMockResponse(text)
                    val aiMessage = ChatMessage(
                        text = mockResponse,
                        isUser = false
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false,
                        error = "Using offline mode (API unavailable)"
                    )
                }
            } catch (e: Exception) {
                // Fallback to mock response
                val mockResponse = generateMockResponse(text)
                val aiMessage = ChatMessage(
                    text = mockResponse,
                    isUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false,
                    error = "Using offline mode: ${e.message}"
                )
            }
        }
    }

    private fun generateMockResponse(userMessage: String): String {
        // Mock responses based on keywords for testing
        return when {
            userMessage.contains("mount", ignoreCase = true) ||
            userMessage.contains("mountain", ignoreCase = true) -> {
                "Mountains are majestic natural landmarks formed through tectonic plate movements over millions of years. They offer stunning views from aircraft windows, especially during clear weather conditions."
            }
            userMessage.contains("city", ignoreCase = true) ||
            userMessage.contains("urban", ignoreCase = true) -> {
                "Cities visible from aircraft appear as intricate patterns of lights and structures. The grid-like layout of streets and the clustering of tall buildings in downtown areas create distinctive patterns that help identify major metropolitan areas."
            }
            userMessage.contains("river", ignoreCase = true) ||
            userMessage.contains("lake", ignoreCase = true) -> {
                "Water bodies are excellent navigation aids for pilots and create beautiful reflections when viewed from above. Rivers follow the natural contours of the land, while lakes often fill ancient glacial basins or volcanic craters."
            }
            userMessage.contains("how high", ignoreCase = true) ||
            userMessage.contains("altitude", ignoreCase = true) -> {
                "Commercial aircraft typically cruise between 30,000 and 42,000 feet (9,000-13,000 meters). At this altitude, you can see approximately 200-250 kilometers to the horizon on a clear day."
            }
            userMessage.contains("what can i see", ignoreCase = true) ||
            userMessage.contains("visible", ignoreCase = true) -> {
                "From your current position, you should look for prominent landmarks like mountains, coastlines, major cities, and large water bodies. The visibility depends on weather conditions, time of day, and your altitude."
            }
            userMessage.contains("thank", ignoreCase = true) ||
            userMessage.contains("thanks", ignoreCase = true) -> {
                "You're welcome! Feel free to ask me anything about the landmarks, geography, or what you're seeing during your flight. I'm here to enhance your flying experience!"
            }
            else -> {
                "That's an interesting question! While I don't have real-time access to Claude API yet, I can tell you that this app will soon be able to provide detailed information about landmarks, geographical features, and interesting facts about what you see during your flight. Try asking about mountains, cities, rivers, or what's visible from your window!"
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
