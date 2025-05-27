package com.dsp.disiplinpro.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatHistory(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val lastMessage: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val messages: List<ChatMessageData> = emptyList()
)

data class ChatMessageData(
    val id: String = "",
    val type: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isError: Boolean = false
) {
    companion object {
        const val TYPE_USER = "user"
        const val TYPE_AI = "ai"
        const val TYPE_SYSTEM = "system"
    }
}