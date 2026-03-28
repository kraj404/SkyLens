package com.skylens.presentation.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.ai.AiStoryManager
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
    private val aiStoryManager: AiStoryManager
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
            // Call AiStoryManager which uses the configured provider (Gemini or Claude)
            val result = aiStoryManager.answerQuestion(
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get response: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
