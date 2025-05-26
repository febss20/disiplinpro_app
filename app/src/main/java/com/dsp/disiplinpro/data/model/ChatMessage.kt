package com.dsp.disiplinpro.data.model

import java.util.Date

sealed class ChatMessage {
    abstract val id: String
    abstract val timestamp: Date
    abstract val content: String

    data class UserMessage(
        override val id: String,
        override val timestamp: Date = Date(),
        override val content: String
    ) : ChatMessage()

    data class AiMessage(
        override val id: String,
        override val timestamp: Date = Date(),
        override val content: String,
        val isLoading: Boolean = false
    ) : ChatMessage()

    data class SystemMessage(
        override val id: String,
        override val timestamp: Date = Date(),
        override val content: String,
        val isError: Boolean = false
    ) : ChatMessage()
}