package com.dsp.disiplinpro.data.repository.chatbot

import android.util.Log
import com.dsp.disiplinpro.data.model.ChatHistory
import com.dsp.disiplinpro.data.model.ChatMessage
import com.dsp.disiplinpro.data.model.ChatMessageData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatHistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "ChatHistoryRepository"

    private fun getChatHistoryCollection(userId: String) =
        firestore.collection("users").document(userId).collection("chatHistory")

    suspend fun getChatHistoriesList(): List<ChatHistory> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "anonymous"

            Log.d(TAG, "getChatHistoriesList: Fetching for user $userId")

            val snapshot = getChatHistoryCollection(userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val histories = snapshot.toObjects(ChatHistory::class.java)
            Log.d(TAG, "getChatHistoriesList: Found ${histories.size} histories")
            return@withContext histories
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat histories list", e)
            return@withContext emptyList<ChatHistory>()
        }
    }

    suspend fun saveChatHistory(messages: List<ChatMessage>, title: String = ""): String = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "saveChatHistory: User not logged in, using anonymous ID")
                val anonymousId = "anonymous"
                return@withContext saveHistoryWithUserId(messages, anonymousId, title)
            } else {
                val userId = currentUser.uid
                Log.d(TAG, "saveChatHistory: Saving chat for user $userId")
                return@withContext saveHistoryWithUserId(messages, userId, title)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chat history", e)
            throw e
        }
    }

    private suspend fun saveHistoryWithUserId(messages: List<ChatMessage>, userId: String, title: String): String {
        val historyTitle = if (title.isNotEmpty()) title else {
            val firstUserMessage = messages.filterIsInstance<ChatMessage.UserMessage>().firstOrNull()
            firstUserMessage?.content?.take(30)?.plus(if (firstUserMessage.content.length > 30) "..." else "")
                ?: "Chat on ${Timestamp.now()}"
        }

        val messageDataList = messages.map { message ->
            when (message) {
                is ChatMessage.UserMessage -> ChatMessageData(
                    id = message.id,
                    type = ChatMessageData.TYPE_USER,
                    content = message.content,
                    timestamp = Timestamp(message.timestamp)
                )
                is ChatMessage.AiMessage -> ChatMessageData(
                    id = message.id,
                    type = ChatMessageData.TYPE_AI,
                    content = message.content,
                    timestamp = Timestamp(message.timestamp)
                )
                is ChatMessage.SystemMessage -> ChatMessageData(
                    id = message.id,
                    type = ChatMessageData.TYPE_SYSTEM,
                    content = message.content,
                    timestamp = Timestamp(message.timestamp),
                    isError = message.isError
                )
            }
        }

        val lastMessage = messages.lastOrNull()?.content ?: ""
        val chatId = UUID.randomUUID().toString()
        val chatHistory = ChatHistory(
            id = chatId,
            userId = userId,
            title = historyTitle,
            lastMessage = lastMessage,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            messages = messageDataList
        )

        Log.d(TAG, "Saving chat history with ID: $chatId for user: $userId")
        getChatHistoryCollection(userId).document(chatId).set(chatHistory).await()

        return chatId
    }

    suspend fun getChatHistories(): Flow<List<ChatHistory>> = flow {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "anonymous"

            Log.d(TAG, "getChatHistories: Fetching for user $userId")

            val snapshot = getChatHistoryCollection(userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val histories = snapshot.toObjects(ChatHistory::class.java)
            Log.d(TAG, "getChatHistories: Found ${histories.size} histories")
            emit(histories)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat histories", e)
            emit(emptyList<ChatHistory>())
        }
    }

    suspend fun getChatHistoryById(historyId: String): ChatHistory? = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "anonymous"

            val document = getChatHistoryCollection(userId).document(historyId).get().await()
            val history = document.toObject(ChatHistory::class.java)
            Log.d(TAG, "getChatHistoryById: $historyId - Found: ${history != null}")
            return@withContext history
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat history by ID: $historyId", e)
            return@withContext null
        }
    }

    suspend fun deleteChatHistory(historyId: String) = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "anonymous"

            Log.d(TAG, "deleteChatHistory: Deleting $historyId for user $userId")
            getChatHistoryCollection(userId).document(historyId).delete().await()
            Log.d(TAG, "deleteChatHistory: Successfully deleted $historyId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting chat history: $historyId", e)
            throw e
        }
    }

    suspend fun updateChatHistory(historyId: String, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: "anonymous"

            Log.d(TAG, "updateChatHistory: Updating $historyId with ${messages.size} messages for user $userId")

            val messageDataList = messages.map { message ->
                when (message) {
                    is ChatMessage.UserMessage -> ChatMessageData(
                        id = message.id,
                        type = ChatMessageData.TYPE_USER,
                        content = message.content,
                        timestamp = Timestamp(message.timestamp)
                    )
                    is ChatMessage.AiMessage -> ChatMessageData(
                        id = message.id,
                        type = ChatMessageData.TYPE_AI,
                        content = message.content,
                        timestamp = Timestamp(message.timestamp)
                    )
                    is ChatMessage.SystemMessage -> ChatMessageData(
                        id = message.id,
                        type = ChatMessageData.TYPE_SYSTEM,
                        content = message.content,
                        timestamp = Timestamp(message.timestamp),
                        isError = message.isError
                    )
                }
            }

            val lastMessage = messages.lastOrNull()?.content ?: ""

            val updateData = mapOf(
                "messages" to messageDataList,
                "lastMessage" to lastMessage,
                "updatedAt" to Timestamp.now()
            )

            getChatHistoryCollection(userId).document(historyId).update(updateData).await()
            Log.d(TAG, "updateChatHistory: Successfully updated $historyId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating chat history: $historyId", e)
            throw e
        }
    }
}