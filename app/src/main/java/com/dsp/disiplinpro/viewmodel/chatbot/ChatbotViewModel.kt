package com.dsp.disiplinpro.viewmodel.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsp.disiplinpro.data.model.ChatHistory
import com.dsp.disiplinpro.data.model.ChatMessage
import com.dsp.disiplinpro.data.model.ChatMessageData
import com.dsp.disiplinpro.data.repository.chatbot.ChatHistoryRepository
import com.dsp.disiplinpro.data.service.GeminiChatManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val geminiChatManager: GeminiChatManager,
    private val chatHistoryRepository: ChatHistoryRepository
) : ViewModel() {

    private val TAG = "ChatbotViewModel"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _chatHistories = MutableStateFlow<List<ChatHistory>>(emptyList())
    val chatHistories: StateFlow<List<ChatHistory>> = _chatHistories.asStateFlow()

    private val _isHistoryVisible = MutableStateFlow(false)
    val isHistoryVisible: StateFlow<Boolean> = _isHistoryVisible.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val histories = fetchChatHistories()

                if (histories.isNotEmpty()) {
                    val mostRecentChat = histories.first()
                    Log.d(TAG, "Loading most recent chat: ${mostRecentChat.id}")
                    loadChatFromHistory(mostRecentChat)
                } else {
                    Log.d(TAG, "No previous chats found, creating new chat")
                    createNewChat()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during initialization", e)
                createNewChat()
            } finally {
                _isInitializing.value = false
            }
        }
    }

    fun createNewChat() {
        _messages.value = emptyList()
        _currentChatId.value = null

        geminiChatManager.clearChatHistory()

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

                val aiMessage = ChatMessage.AiMessage(
                    id = UUID.randomUUID().toString(),
                    content = response
                )
                addMessage(aiMessage)

                if (_messages.value.size > 2) {
                    saveCurrentChat()
                }
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
        createNewChat()
    }

    fun toggleHistoryVisibility() {
        _isHistoryVisible.value = !_isHistoryVisible.value
    }

    private suspend fun fetchChatHistories(): List<ChatHistory> {
        return try {
            Log.d(TAG, "Fetching chat histories directly")
            val histories = chatHistoryRepository.getChatHistoriesList()
            _chatHistories.value = histories
            Log.d(TAG, "Fetched ${histories.size} histories")
            histories
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chat histories: ${e.message}", e)
            emptyList()
        }
    }

    fun loadChatHistories() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading chat histories")
                chatHistoryRepository.getChatHistories().collectLatest { histories ->
                    Log.d(TAG, "Loaded ${histories.size} chat histories")
                    _chatHistories.value = histories
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chat histories: ${e.message}", e)
                _chatHistories.value = emptyList()
            }
        }
    }

    private fun saveCurrentChat() {
        viewModelScope.launch {
            try {
                if (_messages.value.size < 3) {
                    Log.d(TAG, "Not saving chat with only ${_messages.value.size} messages (need at least 3)")
                    return@launch
                }

                Log.d(TAG, "Saving chat with ${_messages.value.size} messages")
                val currentId = _currentChatId.value
                if (currentId != null) {
                    Log.d(TAG, "Updating existing chat: $currentId")
                    chatHistoryRepository.updateChatHistory(currentId, _messages.value)
                } else {
                    Log.d(TAG, "Creating new chat")
                    val chatId = chatHistoryRepository.saveChatHistory(_messages.value)
                    Log.d(TAG, "New chat created with ID: $chatId")
                    _currentChatId.value = chatId
                }

                loadChatHistories()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving chat: ${e.message}", e)

                if (!e.message.toString().contains("PERMISSION_DENIED")) {
                    addMessage(
                        ChatMessage.SystemMessage(
                            id = UUID.randomUUID().toString(),
                            content = "Gagal menyimpan riwayat chat: ${e.message}",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    fun loadChatFromHistory(chatHistory: ChatHistory) {
        viewModelScope.launch {
            try {
                val messages = chatHistory.messages.map { messageData ->
                    when (messageData.type) {
                        ChatMessageData.TYPE_USER -> ChatMessage.UserMessage(
                            id = messageData.id,
                            timestamp = messageData.timestamp.toDate(),
                            content = messageData.content
                        )
                        ChatMessageData.TYPE_AI -> ChatMessage.AiMessage(
                            id = messageData.id,
                            timestamp = messageData.timestamp.toDate(),
                            content = messageData.content
                        )
                        else -> ChatMessage.SystemMessage(
                            id = messageData.id,
                            timestamp = messageData.timestamp.toDate(),
                            content = messageData.content,
                            isError = messageData.isError
                        )
                    }
                }

                _messages.value = messages
                _currentChatId.value = chatHistory.id
                _isHistoryVisible.value = false

                geminiChatManager.clearChatHistory()
                messages.forEach { message ->
                    if (message is ChatMessage.UserMessage) {
                        geminiChatManager.addToHistory("user", message.content)
                    } else if (message is ChatMessage.AiMessage) {
                        geminiChatManager.addToHistory("ai", message.content)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chat from history: ${e.message}", e)
                createNewChat()
            }
        }
    }

    fun deleteChatHistory(chatId: String) {
        viewModelScope.launch {
            try {
                chatHistoryRepository.deleteChatHistory(chatId)

                if (_currentChatId.value == chatId) {
                    createNewChat()
                }

                loadChatHistories()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting chat history: ${e.message}", e)
            }
        }
    }
}