package com.dsp.disiplinpro.viewmodel.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsp.disiplinpro.data.model.ChatMessage
import com.dsp.disiplinpro.data.service.GeminiChatManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val geminiChatManager: GeminiChatManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        addMessage(
            ChatMessage.AiMessage(
                id = UUID.randomUUID().toString(),
                content = "Halo! Saya asisten DisiplinPro. Apa yang dapat saya bantu hari ini? Anda dapat bertanya tentang tugas, jadwal, atau fitur aplikasi."
            )
        )
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val messageText = _inputText.value.trim()
        if (messageText.isEmpty() || _isLoading.value) return

        _inputText.value = ""

        val userMessage = ChatMessage.UserMessage(
            id = UUID.randomUUID().toString(),
            content = messageText
        )
        addMessage(userMessage)

        val loadingMessageId = UUID.randomUUID().toString()
        addMessage(
            ChatMessage.AiMessage(
                id = loadingMessageId,
                content = "",
                isLoading = true
            )
        )

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = geminiChatManager.getChatResponse(messageText)

                removeMessage(loadingMessageId)

                addMessage(
                    ChatMessage.AiMessage(
                        id = UUID.randomUUID().toString(),
                        content = response
                    )
                )
            } catch (e: Exception) {
                removeMessage(loadingMessageId)

                addMessage(
                    ChatMessage.SystemMessage(
                        id = UUID.randomUUID().toString(),
                        content = "Terjadi kesalahan: ${e.message ?: "Unknown error"}",
                        isError = true
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun removeMessage(messageId: String) {
        _messages.value = _messages.value.filter { it.id != messageId }
    }

    fun clearChat() {
        val welcomeMessage = _messages.value.firstOrNull()
        _messages.value = welcomeMessage?.let { listOf(it) } ?: emptyList()

        geminiChatManager.clearChatHistory()
    }
}