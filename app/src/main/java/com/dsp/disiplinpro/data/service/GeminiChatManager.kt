package com.dsp.disiplinpro.data.service

import android.content.Context
import com.dsp.disiplinpro.R
import com.dsp.disiplinpro.data.repository.AppDataRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiChatManager @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val context: Context
) {
    private val generativeModel by lazy {
        val apiKey = context.getString(R.string.gemini_api_key)
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }

    private val chatHistory = mutableListOf<Pair<String, String>>()

    suspend fun getChatResponse(userQuery: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val appContext = appDataRepository.getContextForGemini()
                chatHistory.add(Pair("user", userQuery))
                val prompt = buildPrompt(appContext, userQuery)
                val response = sendPromptToGemini(prompt)
                val aiResponse = response.text ?: ""
                if (aiResponse.isNotEmpty()) {
                    chatHistory.add(Pair("ai", aiResponse))
                }

                aiResponse
            } catch (e: Exception) {
                "Maaf, terjadi kesalahan saat berkomunikasi dengan AI: ${e.message}"
            }
        }
    }

    private suspend fun sendPromptToGemini(prompt: String): GenerateContentResponse {
        val content = content {
            text(prompt)
        }

        return generativeModel.generateContent(content)
    }

    private fun buildPrompt(appContext: String, userQuery: String): String {
        return """
            Kamu adalah asisten AI dalam aplikasi DisiplinPro, sebuah aplikasi manajemen tugas dan disiplin.
            
            Berikut adalah data dari aplikasi DisiplinPro:
            
            $appContext
            
            Riwayat percakapan dengan pengguna:
            ${formatChatHistory()}
            
            Berdasarkan informasi di atas, jawab pertanyaan pengguna:
            $userQuery
            
            Berikan jawaban yang relevan, ringkas, dan dalam Bahasa Indonesia yang sopan.
            Jika ada informasi yang tidak tersedia dalam data yang diberikan, sampaikan bahwa kamu tidak memiliki informasi tersebut.
            Jika diminta untuk membuat, mengubah, atau menghapus tugas, beri tahu pengguna bahwa mereka perlu melakukannya melalui antarmuka aplikasi.
        """.trimIndent()
    }

    private fun formatChatHistory(): String {
        val recentHistory = chatHistory.takeLast(10)

        if (recentHistory.isEmpty()) {
            return "Tidak ada riwayat percakapan sebelumnya."
        }

        return buildString {
            recentHistory.forEach { (role, message) ->
                append("${if (role == "user") "Pengguna" else "AI"}: $message\n")
            }
        }
    }

    fun clearChatHistory() {
        chatHistory.clear()
    }
}